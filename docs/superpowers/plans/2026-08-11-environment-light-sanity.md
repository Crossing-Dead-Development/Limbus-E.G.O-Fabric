# 環境光照 × SAN 實作計畫

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 讓 SAN 與 Minecraft 原生環境亮度互動——低光照緩慢下降、明亮緩慢回復（只到 0）、中性亮度不變——並順帶讓 SAN 跨登出持久化。

**Architecture:** 判定邏輯抽成無 Minecraft 依賴的純函式 `EnvironmentSanityLogic`（可用 JUnit 直接測），`SanityManager` 只負責讀光照、呼叫它、套用結果。掛在**既有**的 `ServerScheduler.every(40, ...)` 迴圈上，不新增排程。持久化用 Fabric Data Attachment API 把一個 int 存進玩家 NBT。

**Tech Stack:** Fabric 1.21.4、Java 21、JUnit 5、`fabric-data-attachment-api-v1`（已在 `fabric-api` 依賴樹內，版本 1.6.2+e99da0f704）。

**設計文件：** `docs/superpowers/specs/2026-08-11-environment-light-sanity-design.md`

## Global Constraints

- Minecraft 1.21.4 / Yarn 1.21.4+build.8 / Java 21。
- **Windows + Git Bash；Gradle wrapper 只有 `./gradlew.bat`**（沒有 `gradlew` shell script）。
- 註解一律繁體中文，與 Repo 既有風格一致。
- **不引入 config 系統。** 所有可調數值一律 `public static final` 常數。理由見 spec §7。
- 純邏輯類別**不得 import 任何 `net.minecraft.*`**，否則 JUnit 測試會因缺少 Minecraft classpath 而爆炸。這是 `GiftUpgradeLogic` / `StatusState` 的既有慣例。
- 不改動既有的戰鬥 SAN 規則（命中 +1/2 次、受擊 -1/2 次）、debuff 門檻（-30/-45）、屬性 modifier、BossBar。
- 不加維度特例。終界因 `getAmbientDarkness()` 為 0 而全域算亮，是 spec §6 明確接受的後果。

## 已驗證的 API 事實（實作時可直接依賴）

| 事實 | 驗證方式 |
|---|---|
| `WorldView.getLightLevel(BlockPos)` 是 default method，實作為 `getLightLevel(pos, getAmbientDarkness())` | `javap -c net.minecraft.world.WorldView` 反組譯確認 |
| `World implements WorldView`，故 `player.getWorld().getLightLevel(pos)` 可用 | 同上 |
| `AttachmentRegistry.createPersistent(Identifier, Codec<A>)` 存在 | `fabric-data-attachment-api-v1-1.6.2` sources jar |
| `AttachmentTarget` 由 Fabric 以 mixin 實作在 `Entity` 上，故 `ServerPlayerEntity` 直接有 `getAttachedOrElse` / `setAttached` | `AttachmentTarget` javadoc |
| attachment 預設**不**在玩家死亡時複製（需 `copyOnDeath()` 才會），但**會**在跨維度／回終界時複製 | `AttachmentType` javadoc |

最後一條正是我們要的：死亡歸零（既有 `resetOnRespawn` 行為）自動成立，跨維度保值也自動成立。

## 檔案結構

| 檔案 | 動作 | 職責 |
|---|---|---|
| `src/main/java/me/yisang/limbusego/status/EnvironmentSanityLogic.java` | 新增 | 純函式：由 (光照, 曝光值, SAN, 是否脫戰) 算出 (新曝光值, SAN 變化量)。無 Minecraft 依賴。 |
| `src/test/java/me/yisang/limbusego/status/EnvironmentSanityLogicTest.java` | 新增 | 上者的 JUnit 測試 |
| `src/main/java/me/yisang/limbusego/status/SanityAttachments.java` | 新增 | SAN 持久化 attachment type 的註冊 |
| `src/main/java/me/yisang/limbusego/status/SanityManager.java` | 修改 | 讀光照、呼叫純函式、套用結果；`exposure` map；attachment 讀寫 |
| `src/main/java/me/yisang/limbusego/LimbusEGOMod.java` | 修改 | 在 `sanity.start()` 前註冊 attachment |

## 對 spec §8 的一處刻意簡化

Spec §8 寫純函式回傳三個值 `(新曝光值, SAN 變化量, 是否抑制回復)`。實作時把「脫戰回復」也一併折進 `SAN 變化量`，回傳兩個值即可——呼叫端只要無腦套用 delta，且「回復不得越過 0」這條不變式可以在純函式裡一次保證、一次測到。功能等價，介面更小。

---

### Task 1: EnvironmentSanityLogic 純判定邏輯

**Files:**
- Create: `src/main/java/me/yisang/limbusego/status/EnvironmentSanityLogic.java`
- Test: `src/test/java/me/yisang/limbusego/status/EnvironmentSanityLogicTest.java`

**Interfaces:**
- Consumes: 無（本任務為起點）
- Produces:
  - `public static final int EnvironmentSanityLogic.LIGHT_DARK_MAX = 4`
  - `public static final int EnvironmentSanityLogic.LIGHT_BRIGHT_MIN = 9`
  - `public static final int EnvironmentSanityLogic.EXPOSURE_THRESHOLD = 15`
  - `public record EnvironmentSanityLogic.Result(int exposure, int sanDelta)`
  - `public static Result EnvironmentSanityLogic.step(int light, int exposure, int san, boolean outOfCombat)`

- [x] **Step 1: 寫失敗的測試**

建立 `src/test/java/me/yisang/limbusego/status/EnvironmentSanityLogicTest.java`：

```java
package me.yisang.limbusego.status;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentSanityLogicTest {

    // ── 門檻邊界 ──────────────────────────────────────────────────────────

    @Test
    void lightFourIsDarkAndFiveIsNeutral() {
        // 光照 4（午夜露天）：曝光值往負走
        assertEquals(-1, EnvironmentSanityLogic.step(4, 0, 0, false).exposure());
        // 光照 5：中性，曝光值 0 維持 0
        assertEquals(0, EnvironmentSanityLogic.step(5, 0, 0, false).exposure());
    }

    @Test
    void lightEightIsNeutralAndNineIsBright() {
        assertEquals(0, EnvironmentSanityLogic.step(8, 0, 0, false).exposure());
        assertEquals(1, EnvironmentSanityLogic.step(9, 0, 0, false).exposure());
    }

    // ── 累計器 ────────────────────────────────────────────────────────────

    @Test
    void darkDropsOneSanOnlyAfterThresholdSteps() {
        int exposure = 0;
        // 前 14 步：只累積，不動 SAN
        for (int i = 0; i < EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1; i++) {
            EnvironmentSanityLogic.Result r =
                    EnvironmentSanityLogic.step(0, exposure, 0, false);
            assertEquals(0, r.sanDelta(), "第 " + (i + 1) + " 步不該動 SAN");
            exposure = r.exposure();
        }
        // 第 15 步：扣 1 點，累計器歸零
        EnvironmentSanityLogic.Result last =
                EnvironmentSanityLogic.step(0, exposure, 0, false);
        assertEquals(-1, last.sanDelta());
        assertEquals(0, last.exposure(), "跨門檻後累計器必須歸零");
    }

    @Test
    void neutralDecaysExposureTowardZero() {
        assertEquals(4, EnvironmentSanityLogic.step(7, 5, 0, false).exposure());
        assertEquals(-4, EnvironmentSanityLogic.step(7, -5, 0, false).exposure());
        assertEquals(0, EnvironmentSanityLogic.step(7, 0, 0, false).exposure());
    }

    // ── 明亮回復只到 0 ────────────────────────────────────────────────────

    @Test
    void brightRecoveryAppliesWhenSanIsNegative() {
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        EnvironmentSanityLogic.Result r =
                EnvironmentSanityLogic.step(15, atThreshold, -5, false);
        assertEquals(1, r.sanDelta());
        assertEquals(0, r.exposure());
    }

    @Test
    void brightRecoveryDoesNothingWhenSanIsZero() {
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        EnvironmentSanityLogic.Result r =
                EnvironmentSanityLogic.step(15, atThreshold, 0, false);
        assertEquals(0, r.sanDelta(), "SAN 已是 0，明亮不得使其變正");
        assertEquals(0, r.exposure(), "累計器仍須歸零");
    }

    @Test
    void brightRecoveryDoesNothingWhenSanIsPositive() {
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        assertEquals(0, EnvironmentSanityLogic.step(15, atThreshold, 20, false).sanDelta());
    }

    // ── 脫戰回復與黑暗抑制 ────────────────────────────────────────────────

    @Test
    void outOfCombatRecoveryAppliesWhenNotDark() {
        // 中性亮度、脫戰、SAN 為負 → +1
        assertEquals(1, EnvironmentSanityLogic.step(7, 0, -5, true).sanDelta());
    }

    @Test
    void darkSuppressesOutOfCombatRecovery() {
        // 黑暗中脫戰也不回復
        assertEquals(0, EnvironmentSanityLogic.step(0, 0, -5, true).sanDelta());
    }

    @Test
    void outOfCombatRecoveryDoesNothingWhenSanIsZero() {
        assertEquals(0, EnvironmentSanityLogic.step(7, 0, 0, true).sanDelta());
    }

    @Test
    void inCombatMeansNoOutOfCombatRecovery() {
        assertEquals(0, EnvironmentSanityLogic.step(7, 0, -5, false).sanDelta());
    }

    // ── 不變式：回復永不越過 0 ────────────────────────────────────────────

    @Test
    void combinedRecoveryNeverPushesSanAboveZero() {
        // 明亮 + 脫戰 + 曝光值剛好跨門檻，兩條回復可能同時觸發
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        for (int san = -3; san <= 0; san++) {
            EnvironmentSanityLogic.Result r =
                    EnvironmentSanityLogic.step(15, atThreshold, san, true);
            assertTrue(san + r.sanDelta() <= 0,
                    "SAN " + san + " + delta " + r.sanDelta() + " 不得越過 0");
        }
    }

    @Test
    void combinedRecoveryReachesZeroExactly() {
        int atThreshold = EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1;
        EnvironmentSanityLogic.Result r =
                EnvironmentSanityLogic.step(15, atThreshold, -2, true);
        assertEquals(2, r.sanDelta(), "門檻回復 +1 與脫戰回復 +1 應同時生效");
    }

    // ── 黑暗掉落不受 SAN 值影響（夾制由 SanityManager.setSan 負責） ────────

    @Test
    void darkStillReturnsNegativeDeltaAtFloor() {
        int atThreshold = -(EnvironmentSanityLogic.EXPOSURE_THRESHOLD - 1);
        assertEquals(-1, EnvironmentSanityLogic.step(0, atThreshold, -45, false).sanDelta());
    }
}
```

- [x] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.status.EnvironmentSanityLogicTest"`
Expected: 編譯失敗，訊息包含 `cannot find symbol: class EnvironmentSanityLogic`

- [x] **Step 3: 寫最小實作**

建立 `src/main/java/me/yisang/limbusego/status/EnvironmentSanityLogic.java`：

```java
package me.yisang.limbusego.status;

/**
 * 環境光照對 SAN 影響的純數值邏輯（不依賴 Minecraft，供 JUnit 直接驗證）。
 *
 * <p>規則（每 2 秒呼叫一次，見 {@link SanityManager} 的 {@code recoveryTick}）：
 * <ul>
 *   <li>光照 ≤ {@link #LIGHT_DARK_MAX} 視為黑暗，曝光值 -1</li>
 *   <li>光照 ≥ {@link #LIGHT_BRIGHT_MIN} 視為明亮，曝光值 +1</li>
 *   <li>中間為中性，曝光值向 0 收斂 1</li>
 *   <li>曝光值絕對值達 {@link #EXPOSURE_THRESHOLD} 時動 1 點 SAN，並歸零</li>
 *   <li>明亮回復只能回到 0，正值 SAN 一律靠戰鬥命中取得</li>
 *   <li>黑暗中暫停脫戰回復（「黑暗中無法平復」）</li>
 * </ul>
 *
 * <p>SAN 下限夾制不在此處理，由 {@link SanityManager#setSan} 負責。
 */
public final class EnvironmentSanityLogic {

    /** 光照 ≤ 此值視為黑暗。4 = 午夜露天（天空光 15 − ambient darkness 11）；洞穴為 0。 */
    public static final int LIGHT_DARK_MAX = 4;

    /** 光照 ≥ 此值視為明亮。火把附近為 10~14。 */
    public static final int LIGHT_BRIGHT_MIN = 9;

    /** 曝光值達此絕對值即動 1 點 SAN。15 × 2 秒 = 30 秒 1 點。 */
    public static final int EXPOSURE_THRESHOLD = 15;

    /**
     * 一次判定的結果。
     *
     * @param exposure 新的曝光累計值
     * @param sanDelta SAN 變化量（已保證回復不越過 0）
     */
    public record Result(int exposure, int sanDelta) {}

    private EnvironmentSanityLogic() {}

    /**
     * 計算一次（每 2 秒）的環境判定。
     *
     * @param light        當前方塊位置的環境亮度（0~15，含時間影響）
     * @param exposure     當前曝光累計值（正=明亮累積、負=黑暗累積）
     * @param san          當前 SAN
     * @param outOfCombat  是否已脫戰（從未戰鬥亦視為脫戰）
     */
    public static Result step(int light, int exposure, int san, boolean outOfCombat) {
        boolean dark = light <= LIGHT_DARK_MAX;
        boolean bright = light >= LIGHT_BRIGHT_MIN;

        // 1. 曝光累計；中性亮度向 0 收斂，讓黑暗累積在離開洞穴後自然消散
        int next;
        if (dark) next = exposure - 1;
        else if (bright) next = exposure + 1;
        else if (exposure > 0) next = exposure - 1;
        else if (exposure < 0) next = exposure + 1;
        else next = 0;

        // 2. 跨門檻 → 動 1 點 SAN，累計器歸零
        int delta = 0;
        if (next <= -EXPOSURE_THRESHOLD) {
            delta -= 1;
            next = 0;
        } else if (next >= EXPOSURE_THRESHOLD) {
            if (san < 0) delta += 1;   // 明亮回復只到 0
            next = 0;
        }

        // 3. 脫戰回復（既有規則），黑暗中暫停
        if (!dark && outOfCombat && san + delta < 0) delta += 1;

        return new Result(next, delta);
    }
}
```

- [x] **Step 4: 執行測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.status.EnvironmentSanityLogicTest"`
Expected: PASS，14 個測試全綠

- [x] **Step 5: 確認既有測試未被破壞**

Run: `./gradlew.bat test`
Expected: PASS（含既有的 `GiftUpgradeLogicTest`、`StatusStateTest`）

- [x] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/status/EnvironmentSanityLogic.java src/test/java/me/yisang/limbusego/status/EnvironmentSanityLogicTest.java
git commit -m "feat: 環境光照 SAN 判定純邏輯 / Environment light sanity logic"
```

---

### Task 2: 把光照判定接進 SanityManager

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/status/SanityManager.java`（`recoveryTick` 約 86-95 行；欄位區約 54-60 行；`onQuit` 約 224-232 行）

**Interfaces:**
- Consumes: `EnvironmentSanityLogic.step(int, int, int, boolean)` → `Result(int exposure, int sanDelta)`（Task 1）
- Produces: 無新公開 API。`SanityManager` 行為改變：`recoveryTick` 現在同時處理環境判定與脫戰回復。

- [x] **Step 1: 新增 exposure 欄位**

在 `SanityManager` 既有的 map 欄位區（`lastFood` 之後）加入：

```java
    /** 環境曝光累計值：正=明亮累積、負=黑暗累積。不持久化，重登重置無害。 */
    private final Map<UUID, Integer> exposure = new ConcurrentHashMap<>();
```

- [x] **Step 2: 改寫 recoveryTick**

把既有的：

```java
    private void recoveryTick(ServerPlayerEntity p) {
        // 低理智 debuff 定期補刷（讓玩家離開閾值後自然消退）
        if (getSan(p) <= DEBUFF_THRESHOLD) applyLowSanDebuffs(p);

        Long lc = lastCombat.get(p.getUuid());
        if (lc == null) return;
        if (System.currentTimeMillis() - lc < OUT_COMBAT_MS) return;
        int cur = getSan(p);
        if (cur < 0) setSan(p, cur + 1);
    }
```

整段換成：

```java
    private void recoveryTick(ServerPlayerEntity p) {
        // 低理智 debuff 定期補刷（讓玩家離開閾值後自然消退）
        if (getSan(p) <= DEBUFF_THRESHOLD) applyLowSanDebuffs(p);

        // 創造／旁觀模式不受環境影響（建築時不該掉 SAN）
        if (p.isCreative() || p.isSpectator()) return;

        // 從未戰鬥亦視為脫戰。環境扣 SAN 刻意不更新 lastCombat（見下方註解），
        // 若此處沿用舊的 `lc == null → return`，從未戰鬥的玩家在洞穴掉 SAN 後將永遠無法回復。
        Long lc = lastCombat.get(p.getUuid());
        boolean outOfCombat = lc == null || System.currentTimeMillis() - lc >= OUT_COMBAT_MS;

        int light = p.getWorld().getLightLevel(p.getBlockPos());
        int cur = getSan(p);
        EnvironmentSanityLogic.Result r = EnvironmentSanityLogic.step(
                light, exposure.getOrDefault(p.getUuid(), 0), cur, outOfCombat);

        exposure.put(p.getUuid(), r.exposure());

        // 刻意直接走 setSan 而非 dropSan()：dropSan 會更新 lastCombat，
        // 會讓待在洞穴的玩家被永久判定為「戰鬥中」，脫戰回復再也不會觸發。
        if (r.sanDelta() != 0) setSan(p, cur + r.sanDelta());
    }
```

- [x] **Step 3: onQuit 清掉 exposure**

在 `onQuit` 既有的 `lastFood.remove(p.getUuid());` 之後加入：

```java
        exposure.remove(p.getUuid());
```

- [x] **Step 4: 編譯**

Run: `./gradlew.bat build -q`
Expected: exit 0，無編譯錯誤

- [x] **Step 5: 確認既有測試未被破壞**

Run: `./gradlew.bat test`
Expected: PASS

- [x] **Step 6: 遊戲內驗證**

啟動 `./gradlew.bat runClient`，建立創造模式測試世界後：

1. `/gamemode survival`，挖一個無光源的地洞待著。約 30 秒 SAN 從 0 → -1；持續約 10 分鐘 SAN 應到 -20 附近並跳出既有的紫色警告訊息。
2. 放火把（`/give @s torch`）。BossBar 應停止下降；SAN 為負時約每 30 秒 +1，**升到 0 就停住不再往上**。
3. `/gamemode creative` 後在黑暗處待 1 分鐘，SAN 不變。
4. 白天在地面站著，SAN 從負值回升至 0 後停住。

- [x] **Step 7: Commit**

```bash
git add src/main/java/me/yisang/limbusego/status/SanityManager.java
git commit -m "feat: SAN 隨環境光照升降 / SAN responds to ambient light"
```

---

### Task 3: SAN 跨登出持久化

**Files:**
- Create: `src/main/java/me/yisang/limbusego/status/SanityAttachments.java`
- Modify: `src/main/java/me/yisang/limbusego/status/SanityManager.java`（`setSan` 約 101-124 行；`onJoin` 約 213-222 行）
- Modify: `src/main/java/me/yisang/limbusego/LimbusEGOMod.java`（`onInitialize` 約 43 行）

**Interfaces:**
- Consumes: 無（獨立於 Task 1、2 的邏輯）
- Produces:
  - `public static AttachmentType<Integer> SanityAttachments.SAN`
  - `public static void SanityAttachments.register()`

- [x] **Step 1: 建立 attachment 註冊類別**

建立 `src/main/java/me/yisang/limbusego/status/SanityAttachments.java`：

```java
package me.yisang.limbusego.status;

import com.mojang.serialization.Codec;
import me.yisang.limbusego.LimbusEGOMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/**
 * SAN 的持久化掛點。只存數值本身（一個 int），寫進玩家 NBT。
 *
 * <p>命中／受擊計數、脫戰計時、環境曝光累計器一律不存，重登重置無害。
 *
 * <p>刻意<b>不</b>呼叫 {@code copyOnDeath()}：Fabric 預設在玩家死亡時不複製 attachment，
 * 正好對應既有的「死亡重生 SAN 歸零」規則（{@link SanityManager#resetOnRespawn}）。
 * 跨維度與回終界時 Fabric 會自動複製，因此那些情境下 SAN 自然保值。
 */
public final class SanityAttachments {

    /** 玩家 SAN 的持久化 attachment。須於 mod 初始化時呼叫 {@link #register()} 後才可用。 */
    public static AttachmentType<Integer> SAN;

    private SanityAttachments() {}

    public static void register() {
        SAN = AttachmentRegistry.createPersistent(LimbusEGOMod.id("san"), Codec.INT);
    }
}
```

- [x] **Step 2: 在 mod 初始化時註冊**

修改 `LimbusEGOMod.onInitialize()`，把：

```java
        sanity = new SanityManager();
        sanity.start();
```

改成：

```java
        me.yisang.limbusego.status.SanityAttachments.register();
        sanity = new SanityManager();
        sanity.start();
```

- [x] **Step 3: setSan 寫入 attachment**

在 `SanityManager.setSan` 中，把既有的：

```java
        san.put(p.getUuid(), v);
```

改成：

```java
        san.put(p.getUuid(), v);
        p.setAttached(SanityAttachments.SAN, v);   // 同步寫入玩家 NBT，存檔即持久
```

每次 `setSan` 都寫入（而非只在 `onQuit` 寫），可讓伺服器異常關閉時 SAN 仍隨最近一次存檔保留。

- [x] **Step 4: onJoin 讀取 attachment**

在 `SanityManager.onJoin` 中，把既有的：

```java
        san.putIfAbsent(p.getUuid(), 0);
```

改成：

```java
        san.putIfAbsent(p.getUuid(), p.getAttachedOrElse(SanityAttachments.SAN, 0));
```

`onJoin` 後續已呼叫 `updateBossBar(p, getSan(p))`，因此讀回的值會立刻反映在 BossBar 上，不需額外處理。

- [x] **Step 5: 編譯**

Run: `./gradlew.bat build -q`
Expected: exit 0

- [x] **Step 6: 確認既有測試未被破壞**

Run: `./gradlew.bat test`
Expected: PASS

- [x] **Step 7: 遊戲內驗證**

啟動 `./gradlew.bat runClient`：

1. 生存模式讓 SAN 掉到負值。`/limbusego` 沒有設定 SAN 的子指令，最快的方式是 `/damage @s 1` 連下兩次（受擊 2 次 = -1 SAN），或沿用 Task 2 的黑暗地洞等約 1 分鐘。
2. 退回主選單、重新進入世界。BossBar 的 SAN 應維持退出時的值，**不是 0**。
3. 讓角色死亡並重生。SAN 應歸 0（既有行為不變）。
4. 走地獄門到地獄再回來。SAN 應保持不變。

- [x] **Step 8: Commit**

```bash
git add src/main/java/me/yisang/limbusego/status/SanityAttachments.java src/main/java/me/yisang/limbusego/status/SanityManager.java src/main/java/me/yisang/limbusego/LimbusEGOMod.java
git commit -m "feat: SAN 跨登出持久化 / Persist SAN across sessions"
```

---

## 驗收（對應 spec §10）

三個 Task 完成後，逐條確認：

- [x] 玩家在點了火把的住宅內待著，SAN 不會下降 → Task 2 Step 6 (2)
- [x] 無光源洞穴挖礦約 10 分鐘，SAN 掉到約 -20 並收到既有警告提示 → Task 2 Step 6 (1)
- [x] 回到地面白天，SAN 從負值緩慢回升並**停在 0** → Task 2 Step 6 (4)
- [x] 短暫穿過陰影（數秒）SAN 完全不變 → Task 1 `darkDropsOneSanOnlyAfterThresholdSteps`
- [x] 在洞穴登出再登入，SAN 維持登出時的值 → Task 3 Step 7 (2)
- [x] 死亡重生 SAN 歸 0 → Task 3 Step 7 (3)
- [x] 創造模式玩家不受環境影響 → Task 2 Step 6 (3)

## 不在本計畫範圍

- E.G.O 提取機、腦啡肽、核心素材、Extraction Recipe、機器 GUI —— 另開 spec 與計畫，理由見 spec §7。
- config 系統 —— 數值定案前不引入。
- 維度特例（終界全域算亮）—— spec §6 明確接受。
