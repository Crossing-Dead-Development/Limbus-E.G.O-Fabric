# Limbus 屬性 GUI 顯示 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 玩家身上的 10 種記憶體屬性鏡射成原版狀態效果顯示在 GUI；動作列的屬性文字全部移除，改為在被打的目標身上噴該屬性顏色的粒子。

**Architecture:** 新增 10 個**純顯示**的原版 `StatusEffect` 空殼（`MirrorStatusEffect`），由 `StatusMirror` 掛在 `StatusManager` 既有的 10 tick 排程上，每輪只掃線上玩家，比對 `StatusState` 與玩家身上的鏡射效果後套用／移除（無限持續、amplifier = potency − 1）。同步決策與粒子觸發條件收在純函式 `StatusDisplayLogic`，可不載入 Minecraft 測試。`StatusEffect` enum 改為只帶 RGB，三個消費者（tooltip、粒子、效果註冊）共用同一來源。

**Tech Stack:** Java 21、Minecraft 1.21.4、Yarn `1.21.4+build.8`、Fabric Loader 0.16.9、Fabric API 0.119.4+1.21.4、JUnit 5.11.4。

**Spec:** `docs/superpowers/specs/2026-09-09-status-gui-display-design.md`

---

## Global Constraints

這些規則適用於**每一個** task，不再逐項重複：

1. **只做呈現層。** 不得修改任何戰鬥數值、乘區、消耗規則。`StatusManager` 仍是唯一真相；註冊的原版效果**不掛屬性修飾符、不覆寫 `applyUpdateEffect`**。
2. **鏡射效果一律無限持續**（`StatusEffectInstance.INFINITE`），不得用 `count × 20` 之類的倒數。
3. **不在 `apply` / `consume` / `refresh` 逐點插同步呼叫**，只走 `StatusMirror` 的週期同步。
4. **迅捷（HASTE）與束縛（BIND）不註冊鏡射效果**，它們已是原版速度／緩速 wrapper。
5. **所有玩家可見文字都必須是翻譯鍵**。新增鍵一律同時寫進 `src/main/resources/assets/limbusego/lang/zh_tw.json` 與 `en_us.json`，兩檔鍵集必須完全相同；`effect.limbusego.<path>` 的值必須與既有 `status.limbusego.<path>` **一字不差**。
6. **命名衝突提醒**：本專案的 `me.yisang.limbusego.status.StatusEffect` 是 Limbus 屬性 enum，與 `net.minecraft.entity.effect.StatusEffect` 同名。在同時需要兩者的檔案裡，**原版類別一律寫全名**，不要 import。
7. **不要呼叫 `Bootstrap.initialize()`**，測試只碰純 Java 物件與 `Text` / `Style`。`StatusDisplayLogic` 的簽名刻意不依賴 Minecraft 型別。
8. **JSON 檔尾不留逗號**，新鍵緊接在 `status.limbusego.charge` 之後插入，不要重排既有內容。
9. **每個 task 結束都要 commit**，訊息格式沿用 repo 慣例：`<type>: <中文摘要> / <English summary>`，並附上：
   ```
   Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>
   ```
   直接在 `master` 上開發，不開分支。
10. **建置與測試指令**（Windows，repo 根目錄）：
    - 編譯：`./gradlew.bat build -x test`
    - 全部測試：`./gradlew.bat test`
    - 單一測試：`./gradlew.bat test --tests "me.yisang.limbusego.status.StatusDisplayLogicTest"`

## 檔案結構

| 檔案 | 動作 | 職責 |
|---|---|---|
| `src/main/java/me/yisang/limbusego/status/StatusDisplayLogic.java` | 新增 | 純函式：同步決策（ADD/UPDATE/REMOVE/NONE）與粒子觸發條件 |
| `src/main/java/me/yisang/limbusego/status/StatusEffect.java` | 修改 | 加 `rgb`，刪 `zh`、`color` |
| `src/main/java/me/yisang/limbusego/tooltip/TooltipFormat.java` | 修改 | 刪 `legacyToRgb`，改讀 `effect.rgb` |
| `src/main/java/me/yisang/limbusego/status/StatusManager.java` | 修改 | 刪四種動作列訊息與 `sendActionBar`；`apply` 改噴粒子；排程加掛 `StatusMirror` |
| `src/main/java/me/yisang/limbusego/Messages.java` | 修改 | 刪 `STATUS_*` 六條常數（四條訊息 + 兩條只被它們用的格式／標籤） |
| `src/main/java/me/yisang/limbusego/status/MirrorStatusEffect.java` | 新增 | 無行為的原版效果空殼 |
| `src/main/java/me/yisang/limbusego/status/ModStatusEffects.java` | 新增 | 註冊 10 個效果；Limbus enum → `RegistryEntry` 對照 |
| `src/main/java/me/yisang/limbusego/status/StatusMirror.java` | 新增 | 掃線上玩家，依 `StatusDisplayLogic` 套用／移除鏡射效果 |
| `src/main/java/me/yisang/limbusego/LimbusEGOMod.java` | 修改 | 呼叫 `ModStatusEffects.register()` |
| `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json` | 修改 | 新增 10 個 `effect.limbusego.*` |
| `src/test/java/me/yisang/limbusego/status/StatusDisplayLogicTest.java` | 新增 | 6 個決策／粒子測試 |
| `src/test/java/me/yisang/limbusego/status/StatusEffectColourTest.java` | 新增 | 10 個鏡射屬性 rgb 非零且互異 |
| `src/test/java/me/yisang/limbusego/LangKeys.java` | 修改 | 加讀取鍵值的能力 |
| `src/test/java/me/yisang/limbusego/LangParityTest.java` | 修改 | 加 `effect.*` / `status.*` 值一致斷言 |

---

### Task 1: `StatusDisplayLogic` 純決策函式

**Files:**
- Create: `src/main/java/me/yisang/limbusego/status/StatusDisplayLogic.java`
- Test: `src/test/java/me/yisang/limbusego/status/StatusDisplayLogicTest.java`

**Interfaces:**
- Consumes: 無。
- Produces:
  - `enum StatusDisplayLogic.Action { ADD, UPDATE, REMOVE, NONE }`
  - `record StatusDisplayLogic.Decision(Action action, int amplifier)`
  - `static Decision StatusDisplayLogic.decide(int potency, Integer currentAmplifier)` — `currentAmplifier == null` 表示玩家身上目前沒有該鏡射效果。
  - `static boolean StatusDisplayLogic.shouldSpawnParticles(Object source, Object target)`

- [x] **Step 1: 寫失敗測試**

```java
package me.yisang.limbusego.status;

import org.junit.jupiter.api.Test;

import static me.yisang.limbusego.status.StatusDisplayLogic.Action.*;
import static org.junit.jupiter.api.Assertions.*;

class StatusDisplayLogicTest {

    @Test
    void addsWhenAbsent() {
        var d = StatusDisplayLogic.decide(3, null);
        assertEquals(ADD, d.action());
        assertEquals(2, d.amplifier());
    }

    @Test
    void updatesWhenAmplifierDiffers() {
        var d = StatusDisplayLogic.decide(5, 2);
        assertEquals(UPDATE, d.action());
        assertEquals(4, d.amplifier());
    }

    @Test
    void updatesWhenPotencyDrops() {
        // 原版 addStatusEffect 不會降 amplifier，所以降級也必須是 UPDATE 而非 NONE
        var d = StatusDisplayLogic.decide(1, 2);
        assertEquals(UPDATE, d.action());
        assertEquals(0, d.amplifier());
    }

    @Test
    void noopWhenUnchanged() {
        assertEquals(NONE, StatusDisplayLogic.decide(3, 2).action());
    }

    @Test
    void removesWhenPotencyGone() {
        assertEquals(REMOVE, StatusDisplayLogic.decide(0, 2).action());
    }

    @Test
    void noopWhenNothingOnEitherSide() {
        assertEquals(NONE, StatusDisplayLogic.decide(0, null).action());
    }

    @Test
    void particlesOnlyForOthers() {
        Object a = new Object();
        Object b = new Object();
        assertFalse(StatusDisplayLogic.shouldSpawnParticles(null, a), "無 source 不冒");
        assertFalse(StatusDisplayLogic.shouldSpawnParticles(a, a), "source == target 不冒");
        assertTrue(StatusDisplayLogic.shouldSpawnParticles(a, b), "打別人才冒");
    }
}
```

- [x] **Step 2: 跑測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.status.StatusDisplayLogicTest"`
Expected: 編譯失敗，`StatusDisplayLogic` 不存在。

- [x] **Step 3: 寫最小實作**

```java
package me.yisang.limbusego.status;

/**
 * 屬性顯示層的純決策函式：鏡射效果該做什麼、施加粒子該不該冒。
 *
 * <p>刻意不依賴任何 Minecraft 型別，方便單元測試。實際的 addStatusEffect /
 * spawnParticles 由 {@link StatusMirror} 與 {@link StatusManager} 執行。
 */
public final class StatusDisplayLogic {

    public enum Action { ADD, UPDATE, REMOVE, NONE }

    /** amplifier 只在 ADD / UPDATE 時有意義，其餘為 0。 */
    public record Decision(Action action, int amplifier) {}

    private StatusDisplayLogic() {}

    /**
     * @param potency          StatusState 中該屬性目前的威力；0 表示沒有
     * @param currentAmplifier 玩家身上鏡射效果目前的 amplifier；null 表示沒有該效果
     */
    public static Decision decide(int potency, Integer currentAmplifier) {
        if (potency <= 0) {
            return currentAmplifier == null
                    ? new Decision(Action.NONE, 0)
                    : new Decision(Action.REMOVE, 0);
        }
        int wanted = potency - 1;
        if (currentAmplifier == null) return new Decision(Action.ADD, wanted);
        if (currentAmplifier == wanted) return new Decision(Action.NONE, wanted);
        return new Decision(Action.UPDATE, wanted);
    }

    /** 只有「打出去」（有施術者且不是自己）才在目標身上冒粒子。 */
    public static boolean shouldSpawnParticles(Object source, Object target) {
        return source != null && !source.equals(target);
    }
}
```

- [x] **Step 4: 跑測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.status.StatusDisplayLogicTest"`
Expected: 7 tests PASS。

- [x] **Step 5: Commit**

```bash
git add src/main/java/me/yisang/limbusego/status/StatusDisplayLogic.java src/test/java/me/yisang/limbusego/status/StatusDisplayLogicTest.java
git commit -m "feat: 屬性顯示層決策函式 / Add StatusDisplayLogic for mirror sync and particle decisions"
```

---

### Task 2: `StatusEffect` 加 `rgb`，`TooltipFormat` 改讀它

本 task **先不刪** `zh` / `color`（`StatusManager` 還在用，Task 3 一併清）。

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/status/StatusEffect.java`
- Modify: `src/main/java/me/yisang/limbusego/tooltip/TooltipFormat.java:49-52, 80-86`
- Test: `src/test/java/me/yisang/limbusego/status/StatusEffectColourTest.java`

**Interfaces:**
- Produces: `public final int StatusEffect.rgb` — 0xRRGGBB。

- [x] **Step 1: 寫失敗測試**

```java
package me.yisang.limbusego.status;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class StatusEffectColourTest {

    /** 會被鏡射成原版效果的 10 個屬性（HASTE / BIND 除外）。 */
    private static final EnumSet<StatusEffect> MIRRORED = EnumSet.of(
            StatusEffect.BLEED, StatusEffect.BURN, StatusEffect.FRAGILE, StatusEffect.SINKING,
            StatusEffect.RUPTURE, StatusEffect.TREMOR, StatusEffect.POWER, StatusEffect.PROTECTION,
            StatusEffect.POISE, StatusEffect.CHARGE);

    @Test
    void mirroredColoursAreNonZeroAndDistinct() {
        var seen = new HashSet<Integer>();
        for (var e : MIRRORED) {
            assertNotEquals(0, e.rgb, e + " 的 rgb 不可為 0（會與黑色／未設定混淆）");
            assertTrue(seen.add(e.rgb), e + " 的 rgb 與其他屬性重複：" + Integer.toHexString(e.rgb));
        }
        assertEquals(10, seen.size());
    }

    @Test
    void rgbFitsIn24Bits() {
        for (var e : StatusEffect.values()) {
            assertEquals(0, e.rgb & 0xFF000000, e + " 的 rgb 不可帶 alpha");
        }
    }
}
```

- [x] **Step 2: 跑測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.status.StatusEffectColourTest"`
Expected: 編譯失敗，`rgb` 不存在。

- [x] **Step 3: enum 加 `rgb`**

色值直接取自原本 legacy 色碼在原版 `Formatting` 的 RGB，確保 tooltip 顏色不變：

```java
package me.yisang.limbusego.status;

public enum StatusEffect {
    BLEED("流血", "§c", 0xFF5555),
    BURN("燒傷", "§6", 0xFFAA00),
    FRAGILE("易損", "§d", 0xFF55FF),
    POWER("強壯", "§e", 0xFFFF55),
    SINKING("沉淪", "§5", 0xAA00AA),
    RUPTURE("破裂", "§4", 0xAA0000),
    TREMOR("震顫", "§b", 0x55FFFF),
    PROTECTION("守護", "§a", 0x55FF55),
    HASTE("迅捷", "§f", 0xFFFFFF),
    BIND("束縛", "§8", 0x555555),
    POISE("呼吸法", "§3", 0x00AAAA),
    CHARGE("充能", "§9", 0x5555FF);

    public final String zh;
    public final String color;
    /** 0xRRGGBB，tooltip、粒子、鏡射效果共用的唯一顏色來源。 */
    public final int rgb;

    StatusEffect(String zh, String color, int rgb) {
        this.zh = zh;
        this.color = color;
        this.rgb = rgb;
    }

    /** tooltip 用的翻譯鍵，例：status.limbusego.burn。 */
    public String translationKey() {
        return "status.limbusego." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
```

- [x] **Step 4: `TooltipFormat.status` 改讀 `rgb`，刪 `legacyToRgb`**

把

```java
    /** 套用該屬性專屬顏色的名稱。 */
    public static Text status(StatusEffect effect) {
        return styled(Text.translatable(effect.translationKey()), legacyToRgb(effect.color), false);
    }
```

改成

```java
    /** 套用該屬性專屬顏色的名稱。 */
    public static Text status(StatusEffect effect) {
        return styled(Text.translatable(effect.translationKey()), effect.rgb, false);
    }
```

並整段刪除檔尾的 `legacyToRgb` 方法（含其 javadoc）。`Formatting` import 仍被其他方法使用，保留。

- [x] **Step 5: 跑測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.status.StatusEffectColourTest" --tests "me.yisang.limbusego.tooltip.TooltipFormatTest"`
Expected: 全部 PASS（`statusUsesItsOwnTranslationKeyAndColor` 仍過）。

- [x] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/status/StatusEffect.java src/main/java/me/yisang/limbusego/tooltip/TooltipFormat.java src/test/java/me/yisang/limbusego/status/StatusEffectColourTest.java
git commit -m "refactor: 屬性顏色收斂為 RGB 欄位 / Give StatusEffect a single rgb colour source"
```

---

### Task 3: 動作列訊息 → 目標粒子；刪 `zh` / `color` 與 `Messages` 常數

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/status/StatusManager.java`
- Modify: `src/main/java/me/yisang/limbusego/Messages.java:9-15`
- Modify: `src/main/java/me/yisang/limbusego/status/StatusEffect.java`

**Interfaces:**
- Consumes: `StatusDisplayLogic.shouldSpawnParticles(Object, Object)`（Task 1）、`StatusEffect.rgb`（Task 2）。
- Produces: `StatusManager.apply(...)` 對外簽名**不變**；`hurtTrue` / `scheduleTrueDamage` / `dealTrueDamage` 的 `label` 參數保留（57 個呼叫點，避免無謂改動），只是不再用來顯示。

- [x] **Step 1: `StatusManager.apply` 改噴粒子**

把 `apply(LivingEntity, StatusEffect, int, int, ServerPlayerEntity)` 中兩處 `showEffectApplied(target, effect, potency, count, source);` 都改為 `spawnAppliedParticles(target, effect, source);`。

方法 javadoc 由「並讓 source（施術者）看到 ActionBar 反饋」改為「若 source 不是 target 本人，在 target 身上噴該屬性顏色的粒子」。

- [x] **Step 2: 移除 `onDamage` 裡的兩處動作列送出**

刪除 POISE 爆擊的：

```java
            if (crit && attacker instanceof ServerPlayerEntity pa) {
                sendActionBar(pa, Messages.fmt(Messages.STATUS_POISE_CRIT, POISE_CRIT_MULT));
            }
```

刪除 TREMOR 爆發的：

```java
                if (src != null) {
                    sendActionBar(src, Messages.fmt(Messages.STATUS_TREMOR_BURST, tremorPotency));
                }
```

刪掉後 `crit` 區域變數只在 `mult *= POISE_CRIT_MULT` 那段用到，把 `boolean crit = false;` 與 `crit = true;` 一併移除，保留乘算。

- [x] **Step 3: `dealTrueDamage` 不再呼叫 `showDamage`**

刪除 `dealTrueDamage` 末尾的 `showDamage(target, source, amount, label);`。javadoc「null label = 憂鬱傷害」保留（呼叫端語意不變）。

- [x] **Step 4: 替換顯示區段**

把 `// ── 顯示 ──` 區段裡的 `showEffectApplied`、`showDamage`、`sendActionBar` 三個方法整段刪除，換成：

```java
    // ── 顯示 ────────────────────────────────────────────────────────

    /**
     * 屬性「打出去」時在目標身上噴該屬性顏色的粒子。
     * 自身屬性靠 {@link StatusMirror} 的 GUI 鏡射回饋，不重複；
     * source 為 null 的系統派生（環境、DoT 結算）也不冒。
     */
    private void spawnAppliedParticles(LivingEntity target, StatusEffect effect, ServerPlayerEntity source) {
        if (!StatusDisplayLogic.shouldSpawnParticles(source, target)) return;
        if (!(target.getWorld() instanceof ServerWorld world)) return;
        world.spawnParticles(new DustParticleEffect(effect.rgb, 1.2f),
                target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ(),
                12, 0.3, 0.4, 0.3, 0);
    }
```

`syncSinkingSpeed` 保持原位不動。

- [x] **Step 5: 整理 import**

- 刪除 `import me.yisang.limbusego.Messages;` 與 `import net.minecraft.text.Text;`。
- 新增 `import net.minecraft.particle.DustParticleEffect;`。

- [x] **Step 6: `Messages` 刪六條 `status.*` 常數**

刪除 `STATUS_APPLIED`、`STATUS_DAMAGE_TARGET`、`STATUS_DAMAGE_SOURCE`、`STATUS_POISE_CRIT`、`STATUS_TREMOR_BURST`、`STATUS_DEPRESSION`（最後一條只被 `showDamage` 用，一併成死碼）以及 `// status.*` 註解行。`sanity.*` 區段與 `fmt` 不動。

- [x] **Step 7: `StatusEffect` 刪 `zh` / `color`**

```java
package me.yisang.limbusego.status;

public enum StatusEffect {
    BLEED(0xFF5555),
    BURN(0xFFAA00),
    FRAGILE(0xFF55FF),
    POWER(0xFFFF55),
    SINKING(0xAA00AA),
    RUPTURE(0xAA0000),
    TREMOR(0x55FFFF),
    PROTECTION(0x55FF55),
    HASTE(0xFFFFFF),
    BIND(0x555555),
    POISE(0x00AAAA),
    CHARGE(0x5555FF);

    /** 0xRRGGBB，tooltip、粒子、鏡射效果共用的唯一顏色來源。 */
    public final int rgb;

    StatusEffect(int rgb) {
        this.rgb = rgb;
    }

    /** tooltip 用的翻譯鍵，例：status.limbusego.burn。 */
    public String translationKey() {
        return "status.limbusego." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
```

- [x] **Step 8: 確認沒有殘留引用**

Run: `grep -rn "\.zh\b\|\.color\b\|STATUS_APPLIED\|STATUS_DAMAGE\|STATUS_POISE_CRIT\|STATUS_TREMOR_BURST\|STATUS_DEPRESSION\|sendActionBar" src/main/java/me/yisang/limbusego/status src/main/java/me/yisang/limbusego/Messages.java src/main/java/me/yisang/limbusego/tooltip`
Expected: 無輸出。

- [x] **Step 9: 編譯並跑全部測試**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL，所有測試通過。

- [x] **Step 10: Commit**

```bash
git add src/main/java/me/yisang/limbusego/status/StatusManager.java src/main/java/me/yisang/limbusego/Messages.java src/main/java/me/yisang/limbusego/status/StatusEffect.java
git commit -m "feat: 屬性動作列文字改為目標粒子 / Replace status action-bar text with particles on the target"
```

---

### Task 4: 註冊 10 個純顯示效果與翻譯鍵

**Files:**
- Create: `src/main/java/me/yisang/limbusego/status/MirrorStatusEffect.java`
- Create: `src/main/java/me/yisang/limbusego/status/ModStatusEffects.java`
- Modify: `src/main/java/me/yisang/limbusego/LimbusEGOMod.java:37`
- Modify: `src/main/resources/assets/limbusego/lang/zh_tw.json`、`en_us.json`
- Modify: `src/test/java/me/yisang/limbusego/LangKeys.java`
- Modify: `src/test/java/me/yisang/limbusego/LangParityTest.java`

**Interfaces:**
- Consumes: `StatusEffect.rgb`。
- Produces:
  - `static final List<StatusEffect> ModStatusEffects.MIRRORED` — 10 個會鏡射的 Limbus 屬性，順序固定。
  - `static RegistryEntry<net.minecraft.entity.effect.StatusEffect> ModStatusEffects.entry(StatusEffect)` — 不在 `MIRRORED` 者回傳 `null`。
  - `static void ModStatusEffects.register()`。
  - `static Map<String,String> LangKeys.zhTwValues()` / `enUsValues()`（測試用）。

- [x] **Step 1: 寫失敗測試——翻譯值一致**

`LangKeys.java` 新增值讀取（鍵集讀法不變）：

```java
    private static final Pattern ENTRY = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    private static Map<String, String> zhValues;
    private static Map<String, String> enValues;

    public static synchronized Map<String, String> zhTwValues() {
        if (zhValues == null) zhValues = readValues("zh_tw.json");
        return zhValues;
    }

    public static synchronized Map<String, String> enUsValues() {
        if (enValues == null) enValues = readValues("en_us.json");
        return enValues;
    }

    private static Map<String, String> readValues(String file) {
        try {
            Map<String, String> out = new LinkedHashMap<>();
            for (String line : Files.readAllLines(DIR.resolve(file), StandardCharsets.UTF_8)) {
                Matcher m = ENTRY.matcher(line);
                if (m.find()) out.put(m.group(1), m.group(2));
            }
            if (out.isEmpty()) throw new IllegalStateException("讀不到任何翻譯值：" + file);
            return out;
        } catch (IOException e) {
            throw new IllegalStateException("讀取 lang 檔失敗：" + file, e);
        }
    }
```

補 import：`java.util.LinkedHashMap`、`java.util.Map`。

`LangParityTest.java` 新增：

```java
    /** GUI 效果名（effect.*，原版由 registry id 推導）與 tooltip 屬性名（status.*）必須是同一個詞。 */
    @Test
    void mirroredEffectNamesMatchStatusNames() {
        for (var s : me.yisang.limbusego.status.ModStatusEffects.MIRRORED) {
            String path = s.name().toLowerCase(java.util.Locale.ROOT);
            String effectKey = "effect.limbusego." + path;
            LangKeys.assertKeyExists(effectKey);
            assertEquals(LangKeys.zhTwValues().get(s.translationKey()), LangKeys.zhTwValues().get(effectKey),
                    "zh_tw：" + effectKey + " 與 " + s.translationKey() + " 值不同");
            assertEquals(LangKeys.enUsValues().get(s.translationKey()), LangKeys.enUsValues().get(effectKey),
                    "en_us：" + effectKey + " 與 " + s.translationKey() + " 值不同");
        }
    }

    @Test
    void exactlyTenStatusesAreMirrored() {
        var mirrored = me.yisang.limbusego.status.ModStatusEffects.MIRRORED;
        assertEquals(10, mirrored.size());
        assertFalse(mirrored.contains(me.yisang.limbusego.status.StatusEffect.HASTE), "迅捷已是原版速度，不鏡射");
        assertFalse(mirrored.contains(me.yisang.limbusego.status.StatusEffect.BIND), "束縛已是原版緩速，不鏡射");
    }
```

- [x] **Step 2: 跑測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.LangParityTest"`
Expected: 編譯失敗，`ModStatusEffects` 不存在。

- [x] **Step 3: `MirrorStatusEffect` 空殼**

```java
package me.yisang.limbusego.status;

import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 純顯示用的原版狀態效果空殼：沒有屬性修飾符、不覆寫 tick 行為。
 * 真正的層數與觸發都在 {@link StatusManager}，這裡只是讓 GUI 有東西可畫。
 *
 * <p>原版 {@code StatusEffect} 建構子是 protected，必須子類化才能實例化。
 */
public class MirrorStatusEffect extends net.minecraft.entity.effect.StatusEffect {
    public MirrorStatusEffect(StatusEffectCategory category, int rgb) {
        super(category, rgb);
    }
}
```

- [x] **Step 4: `ModStatusEffects` 註冊與對照**

```java
package me.yisang.limbusego.status;

import me.yisang.limbusego.LimbusEGOMod;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 10 個純顯示的原版狀態效果，讓玩家在物品欄／HUD 看到自己身上的 Limbus 屬性。
 *
 * <p>HASTE / BIND 不在此列：它們已是原版速度／緩速的 wrapper，本來就會顯示。
 * 翻譯鍵由原版從 registry id 推導為 {@code effect.limbusego.<path>}，
 * 其值必須與 tooltip 用的 {@code status.limbusego.<path>} 一致（有測試把關）。
 */
public final class ModStatusEffects {

    /** 會被鏡射的 Limbus 屬性，順序即 GUI 效果列的註冊順序。 */
    public static final List<StatusEffect> MIRRORED = List.of(
            StatusEffect.BLEED, StatusEffect.BURN, StatusEffect.FRAGILE,
            StatusEffect.SINKING, StatusEffect.RUPTURE, StatusEffect.TREMOR,
            StatusEffect.POWER, StatusEffect.PROTECTION, StatusEffect.POISE, StatusEffect.CHARGE);

    private static final Map<StatusEffect, RegistryEntry<net.minecraft.entity.effect.StatusEffect>> ENTRIES =
            new EnumMap<>(StatusEffect.class);

    private ModStatusEffects() {}

    /** Limbus 屬性 → 原版效果 registry entry；不鏡射者回傳 null。 */
    public static RegistryEntry<net.minecraft.entity.effect.StatusEffect> entry(StatusEffect effect) {
        return ENTRIES.get(effect);
    }

    public static void register() {
        if (!ENTRIES.isEmpty()) return;
        for (StatusEffect e : MIRRORED) {
            String path = e.name().toLowerCase(Locale.ROOT);
            var vanilla = new MirrorStatusEffect(categoryOf(e), e.rgb);
            ENTRIES.put(e, Registry.registerReference(Registries.STATUS_EFFECT, LimbusEGOMod.id(path), vanilla));
        }
    }

    private static StatusEffectCategory categoryOf(StatusEffect e) {
        return switch (e) {
            case POWER, PROTECTION, POISE, CHARGE -> StatusEffectCategory.BENEFICIAL;
            default -> StatusEffectCategory.HARMFUL;
        };
    }
}
```

- [x] **Step 5: 在 `LimbusEGOMod.onInitialize` 呼叫註冊**

在 `ModSounds.register();` 之後、`ServerScheduler.init();` 之前加一行：

```java
        me.yisang.limbusego.status.ModStatusEffects.register();
```

- [x] **Step 6: 加翻譯鍵**

`zh_tw.json` 在 `"status.limbusego.charge": "充能",` 之後插入：

```json
  "effect.limbusego.bleed": "流血",
  "effect.limbusego.burn": "燒傷",
  "effect.limbusego.fragile": "易損",
  "effect.limbusego.sinking": "沉淪",
  "effect.limbusego.rupture": "破裂",
  "effect.limbusego.tremor": "震顫",
  "effect.limbusego.power": "強壯",
  "effect.limbusego.protection": "守護",
  "effect.limbusego.poise": "呼吸法",
  "effect.limbusego.charge": "充能",
```

`en_us.json` 同位置插入：

```json
  "effect.limbusego.bleed": "Bleed",
  "effect.limbusego.burn": "Burn",
  "effect.limbusego.fragile": "Fragile",
  "effect.limbusego.sinking": "Sinking",
  "effect.limbusego.rupture": "Rupture",
  "effect.limbusego.tremor": "Tremor",
  "effect.limbusego.power": "Power",
  "effect.limbusego.protection": "Protection",
  "effect.limbusego.poise": "Poise",
  "effect.limbusego.charge": "Charge",
```

注意插入點後面原本的下一行仍存在，逗號不要漏；若 `status.limbusego.charge` 原本是該區最後一行且無逗號，補上逗號。

- [x] **Step 7: 跑測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.LangParityTest"`
Expected: 4 tests PASS（含既有 2 個）。

- [x] **Step 8: 編譯**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL。

- [x] **Step 9: Commit**

```bash
git add src/main/java/me/yisang/limbusego/status/MirrorStatusEffect.java src/main/java/me/yisang/limbusego/status/ModStatusEffects.java src/main/java/me/yisang/limbusego/LimbusEGOMod.java src/main/resources/assets/limbusego/lang/zh_tw.json src/main/resources/assets/limbusego/lang/en_us.json src/test/java/me/yisang/limbusego/LangKeys.java src/test/java/me/yisang/limbusego/LangParityTest.java
git commit -m "feat: 註冊 10 個純顯示的屬性鏡射效果 / Register 10 display-only mirror status effects"
```

---

### Task 5: `StatusMirror` 週期同步

**Files:**
- Create: `src/main/java/me/yisang/limbusego/status/StatusMirror.java`
- Modify: `src/main/java/me/yisang/limbusego/status/StatusManager.java`（`start()`）

**Interfaces:**
- Consumes: `StatusDisplayLogic.decide(int, Integer)`、`ModStatusEffects.MIRRORED` / `entry(StatusEffect)`、`StatusManager.get(LivingEntity)`。
- Produces: `StatusMirror(StatusManager)`、`void StatusMirror.sync(Iterable<ServerPlayerEntity> players)`。

- [x] **Step 1: 寫 `StatusMirror`**

```java
package me.yisang.limbusego.status;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 把玩家身上的 Limbus 屬性鏡射成原版狀態效果（只給 GUI 看）。
 *
 * <p>採週期同步而非在 apply / consume 逐點呼叫：consume 散落在十幾處，
 * 漏一處就會留下永遠不消失的假效果；週期同步只有這一個地方會錯。
 * 只掃線上玩家——怪物的回饋由施加粒子負責。
 *
 * <p>鏡射效果一律無限持續，層數歸零時由本類顯式移除；
 * amplifier = potency − 1。不顯示原版效果粒子（GUI 圖示才是目的）。
 */
public final class StatusMirror {
    private final StatusManager manager;

    public StatusMirror(StatusManager manager) {
        this.manager = manager;
    }

    /** 每輪對每位玩家比對 10 個鏡射屬性；StatusState 不存在時仍要清殘留效果。 */
    public void sync(Iterable<ServerPlayerEntity> players) {
        for (ServerPlayerEntity p : players) {
            StatusState state = manager.get(p);
            for (StatusEffect e : ModStatusEffects.MIRRORED) {
                RegistryEntry<net.minecraft.entity.effect.StatusEffect> entry = ModStatusEffects.entry(e);
                if (entry == null) continue;
                int potency = state == null ? 0 : state.potency(e);
                StatusEffectInstance current = p.getStatusEffect(entry);
                Integer currentAmp = current == null ? null : current.getAmplifier();
                var decision = StatusDisplayLogic.decide(potency, currentAmp);
                switch (decision.action()) {
                    case ADD -> p.addStatusEffect(instance(entry, decision.amplifier()));
                    case UPDATE -> {
                        // 原版 addStatusEffect 不會降 amplifier，先移除再套
                        p.removeStatusEffect(entry);
                        p.addStatusEffect(instance(entry, decision.amplifier()));
                    }
                    case REMOVE -> p.removeStatusEffect(entry);
                    case NONE -> { }
                }
            }
        }
    }

    private static StatusEffectInstance instance(RegistryEntry<net.minecraft.entity.effect.StatusEffect> entry, int amplifier) {
        // ambient=false, showParticles=false, showIcon=true
        return new StatusEffectInstance(entry, StatusEffectInstance.INFINITE, amplifier, false, false, true);
    }
}
```

- [x] **Step 2: 掛到 `StatusManager` 既有的 10 tick 排程**

`StatusManager` 新增欄位：

```java
    private final StatusMirror mirror = new StatusMirror(this);
```

`start()` 的排程改為同一輪先結算 DoT 再同步鏡射：

```java
    public void start() {
        ServerScheduler.every(BUCKET_INTERVAL_TICKS, server -> {
            burnTick(server.getWorlds());
            mirror.sync(server.getPlayerManager().getPlayerList());
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            // 清 SINKING 移速 modifier（玩家 attribute 會跨復活保留，要顯式移除）
            syncSinkingSpeed(entity, null);
            states.remove(entity.getUuid());
        });
    }
```

- [x] **Step 3: 編譯並跑全部測試**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL。

- [x] **Step 4: Commit**

```bash
git add src/main/java/me/yisang/limbusego/status/StatusMirror.java src/main/java/me/yisang/limbusego/status/StatusManager.java
git commit -m "feat: 玩家屬性週期鏡射到 GUI 效果列 / Mirror player statuses into the vanilla effect list every 10 ticks"
```

---

### Task 6: 遊戲內驗收與文件收尾

**Files:**
- Modify: `docs/superpowers/specs/2026-09-09-status-gui-display-design.md:4`（狀態列）

- [x] **Step 1: 啟動開發客戶端**

Run: `./gradlew.bat runClient`（背景執行，載入約 1–3 分鐘）。進入單人世界，開作弊。

- [x] **Step 2: 依 spec §7 逐項驗收**

| # | 操作 | 預期 |
|---|---|---|
| 1 | `/limbusego` 給自己施加燒傷 | 物品欄與 HUD 出現「燒傷 N ∞」，圖示為缺失材質方格 |
| 2 | 再疊燒傷提高 potency | 羅馬數字跟著變 |
| 3 | 等層數耗盡 | 效果 0.5 秒內消失 |
| 4 | 同時施加多種屬性 | 效果列同時列出多個 |
| 5 | 拿武器打怪 | 怪物身上冒對應顏色粒子（燒傷橙、流血紅、沉淪紫…） |
| 6 | 自己獲得強壯 | **只有** GUI 效果，不冒粒子 |
| 7 | 觀察動作列 | 無屬性文字；天退／薄暝／提比婭蓄力條仍在 |
| 8 | 切英文 | 效果名英文且與 Shift tooltip 的屬性名一致 |

任一項不符：回到對應 task 修正，重跑 `./gradlew.bat build`，再驗。不要在驗收清單上打勾後才修。

- [x] **Step 3: 更新 spec 狀態列**

把 spec 第 4 行 `狀態：設計已通過，待寫實作計畫` 改為 `狀態：已實作（計畫：docs/superpowers/plans/2026-09-11-status-gui-display.md）`。

- [x] **Step 4: Commit**

```bash
git add docs/superpowers/specs/2026-09-09-status-gui-display-design.md
git commit -m "docs: 屬性 GUI 顯示 spec 標記為已實作 / Mark status GUI display spec as implemented"
```
