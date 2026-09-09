# 莊嚴哀悼雙持 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 讓莊嚴哀悼必須黑白雙持才能射擊，改為點一下打一發、兩把各自冷卻 1.2 秒的輪流擊發。

**Architecture:** `SolemnLamentItem` 從 `CrossbowItem` 改為普通 `Item`，上弦流程整個移除；配對判定與選槍規則抽成不碰 Minecraft 的純函式 `SolemnLamentLogic` 以便單元測試；擊發沿用既有的 `WeaponEvents.fireSolemnLament` 彈道，只有觸發方式改變。

**Tech Stack:** Java 21、Minecraft 1.21.4、Yarn `1.21.4+build.8`、Fabric Loader 0.16.9、Fabric API 0.119.4+1.21.4、JUnit 5.11.4。

**Spec:** `docs/superpowers/specs/2026-09-09-solemn-lament-dual-wield-design.md`

---

## Global Constraints

1. **不改數值**：黑白兩把命中後的傷害與屬性（黑 8 傷／凋零 II 4 秒／沉淪 4·3、白 4 傷／失明 3 秒／沉淪 3·2）維持不變，彈道、飛行速度、命中判定（`WeaponEvents.tickProjectiles`）一律不動。
2. **冷卻常數**：每把各自 **24 tick（1.2 秒）**，定義為 `SolemnLamentItem.COOLDOWN_TICKS`，不得散落成字面量。
3. **創造模式一律仍需彈藥。** 這是本 spec 的明確決定；不要為創造模式加例外（天退星刀是免彈藥的，兩者不一致是已知的，本次不調整）。
4. **所有玩家可見文字都必須是翻譯鍵**，同時寫進 `src/main/resources/assets/limbusego/lang/zh_tw.json` 與 `en_us.json`，兩檔鍵集必須完全相同（`LangParityTest` 會把關）。
5. **測試不得載入 Minecraft**：不呼叫 `Bootstrap.initialize()`、不建立 `ItemStack`、不碰 `Registries`。`net.minecraft.util.Hand` 是純 enum，可以直接用。
6. **每個 task 結束都要 commit**，訊息格式：`<type>: <中文摘要> / <English summary>`，並附上：
   ```
   Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>
   ```
7. **建置與測試指令**（Windows，repo 根目錄）：
   - 編譯：`./gradlew.bat build -x test`
   - 全部測試：`./gradlew.bat test`
   - 單一測試：`./gradlew.bat test --tests "me.yisang.limbusego.item.SolemnLamentLogicTest"`
   - 開遊戲：`./gradlew.bat runClient`

---

## File Structure

| 檔案 | 建立／修改／刪除 | 職責 |
|---|---|---|
| `src/main/java/me/yisang/limbusego/item/SolemnLamentLogic.java` | 建立 | 配對判定與選槍規則的純函式 |
| `src/test/java/me/yisang/limbusego/item/SolemnLamentLogicTest.java` | 建立 | 上述兩個函式的單元測試 |
| `src/main/java/me/yisang/limbusego/item/SolemnLamentItem.java` | 改寫 | 改繼承 `Item`；`use()` 實作配對、選槍、彈藥、擊發、冷卻 |
| `src/main/java/me/yisang/limbusego/mixin/CrossbowItemMixin.java` | 刪除 | 只服務於已移除的上弦流程 |
| `src/main/resources/limbusego.mixins.json` | 修改 | 移除 `CrossbowItemMixin` 條目 |
| `src/main/java/me/yisang/limbusego/mixin/client/PlayerEntityRendererMixin.java` | 修改 | 改為配對時雙手 `CROSSBOW_HOLD` |
| `src/main/java/me/yisang/limbusego/item/WeaponTooltips.java` | 修改 | 改寫 `solemn_lament.*` 說明行 |
| `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json` | 修改 | 新增提示訊息與 tooltip 鍵，移除作廢的上弦相關鍵 |
| `README.md` / `README.en.md` | 修改 | 更新武器表的黑白兩列 |

---

### Task 1: 配對判定與選槍規則（純函式）

**Files:**
- Create: `src/main/java/me/yisang/limbusego/item/SolemnLamentLogic.java`
- Test: `src/test/java/me/yisang/limbusego/item/SolemnLamentLogicTest.java`

**Interfaces:**
- Consumes: 無（本 task 不依賴任何既有程式）。
- Produces:
  - `SolemnLamentLogic.isPaired(boolean mainIsSolemn, boolean mainIsBlack, boolean offIsSolemn, boolean offIsBlack)` → `boolean`
  - `SolemnLamentLogic.pickHand(boolean mainReady, boolean offReady)` → `Optional<Hand>`（`net.minecraft.util.Hand`）

- [ ] **Step 1: 寫失敗測試**

建立 `src/test/java/me/yisang/limbusego/item/SolemnLamentLogicTest.java`：

```java
package me.yisang.limbusego.item;

import net.minecraft.util.Hand;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SolemnLamentLogicTest {

    @Test
    void pairingRequiresOneBlackAndOneWhite() {
        // 主手黑 + 副手白
        assertTrue(SolemnLamentLogic.isPaired(true, true, true, false));
        // 主手白 + 副手黑
        assertTrue(SolemnLamentLogic.isPaired(true, false, true, true));
    }

    @Test
    void sameColourIsNotPaired() {
        assertFalse(SolemnLamentLogic.isPaired(true, true, true, true), "黑+黑不應成立");
        assertFalse(SolemnLamentLogic.isPaired(true, false, true, false), "白+白不應成立");
    }

    @Test
    void singleWieldIsNotPaired() {
        assertFalse(SolemnLamentLogic.isPaired(true, true, false, false), "副手非莊嚴哀悼");
        assertFalse(SolemnLamentLogic.isPaired(false, false, true, true), "主手非莊嚴哀悼");
        assertFalse(SolemnLamentLogic.isPaired(false, false, false, false), "兩手都不是");
    }

    @Test
    void pickHandPrefersMainWhenBothReady() {
        assertEquals(Optional.of(Hand.MAIN_HAND), SolemnLamentLogic.pickHand(true, true));
    }

    @Test
    void pickHandFallsBackToOffHand() {
        assertEquals(Optional.of(Hand.OFF_HAND), SolemnLamentLogic.pickHand(false, true));
    }

    @Test
    void pickHandReturnsEmptyWhenBothOnCooldown() {
        assertEquals(Optional.empty(), SolemnLamentLogic.pickHand(false, false));
    }

    @Test
    void alternationEmergesFromCooldownAlone() {
        // 模擬連點：每把冷卻 24 tick，每 12 tick 點一次，應交替
        // ready[0]=主手, ready[1]=副手；-1 代表不在冷卻
        int[] readyAt = {0, 0};
        Hand[] fired = new Hand[4];
        int now = 0;
        for (int i = 0; i < 4; i++) {
            Optional<Hand> pick = SolemnLamentLogic.pickHand(readyAt[0] <= now, readyAt[1] <= now);
            fired[i] = pick.orElseThrow();
            readyAt[fired[i] == Hand.MAIN_HAND ? 0 : 1] = now + 24;
            now += 12;
        }
        assertArrayEquals(
                new Hand[]{Hand.MAIN_HAND, Hand.OFF_HAND, Hand.MAIN_HAND, Hand.OFF_HAND},
                fired,
                "每 12 tick 點一次應穩定交替");
    }
}
```

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.item.SolemnLamentLogicTest"`
Expected: FAIL，編譯錯誤 `cannot find symbol: class SolemnLamentLogic`

- [ ] **Step 3: 實作**

建立 `src/main/java/me/yisang/limbusego/item/SolemnLamentLogic.java`：

```java
package me.yisang.limbusego.item;

import net.minecraft.util.Hand;

import java.util.Optional;

/**
 * 莊嚴哀悼雙持的判定規則。
 *
 * <p>刻意只吃 boolean、不碰 {@code ItemStack} 或 registry，因此可直接單元測試，
 * 無須 Minecraft bootstrap。由 {@link SolemnLamentItem} 負責把 stack 轉成這些旗標。
 */
public final class SolemnLamentLogic {

    private SolemnLamentLogic() {}

    /** 主手與副手是否構成合法的黑白配對（兩手都是莊嚴哀悼，且顏色相異）。 */
    public static boolean isPaired(boolean mainIsSolemn, boolean mainIsBlack,
                                   boolean offIsSolemn, boolean offIsBlack) {
        return mainIsSolemn && offIsSolemn && mainIsBlack != offIsBlack;
    }

    /**
     * 回傳這次應擊發的手；兩把都在冷卻時回傳 empty。
     *
     * <p>刻意不儲存「輪到誰」：兩把各自冷卻的情況下，優先主手就會自然產生交替，
     * 且不會與實際冷卻狀態不同步。副作用是停手超過一輪冷卻後，下一發必從主手開始。
     */
    public static Optional<Hand> pickHand(boolean mainReady, boolean offReady) {
        if (mainReady) return Optional.of(Hand.MAIN_HAND);
        if (offReady) return Optional.of(Hand.OFF_HAND);
        return Optional.empty();
    }
}
```

- [ ] **Step 4: 執行測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.item.SolemnLamentLogicTest"`
Expected: PASS，7 個測試

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/yisang/limbusego/item/SolemnLamentLogic.java src/test/java/me/yisang/limbusego/item/SolemnLamentLogicTest.java
git commit -m "feat: 莊嚴哀悼雙持判定與選槍規則 / Add Solemn Lament pairing and hand-picking rules"
```

---

### Task 2: 改寫 SolemnLamentItem 為點射式雙槍

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/item/SolemnLamentItem.java`（整份改寫）
- Delete: `src/main/java/me/yisang/limbusego/mixin/CrossbowItemMixin.java`
- Modify: `src/main/resources/limbusego.mixins.json`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`

**Interfaces:**
- Consumes: `SolemnLamentLogic.isPaired(...)`、`SolemnLamentLogic.pickHand(...)`（Task 1）；既有的 `WeaponEvents.fireSolemnLament(PlayerEntity, ServerWorld, boolean isBlack, Hand)`、`WeaponEvents.findButterfly(PlayerEntity)`（查無回傳 `null`）、`ModSounds.SOLEMN_QUICK_LOAD_3`。
- Produces: `SolemnLamentItem.COOLDOWN_TICKS`（`public static final int`，值 24）；`SolemnLamentItem.isBlack`（`public final boolean`，維持不變，`PlayerEntityRendererMixin` 與 `WeaponTooltips` 會用到）。

- [ ] **Step 1: 改寫物品類別**

把 `SolemnLamentItem.java` 整份換成：

```java
package me.yisang.limbusego.item;

import me.yisang.limbusego.event.WeaponEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * 莊嚴哀悼（黑／白）：**必須主手與副手各持一把、且一黑一白**才能射擊。
 *
 * <p>右鍵一下打一發，無上弦階段。兩把各自冷卻 {@link #COOLDOWN_TICKS}，
 * 選槍規則見 {@link SolemnLamentLogic#pickHand}——優先主手，因此連點會自然交替，
 * 實際射速約每 0.6 秒一發。
 *
 * <p>黑：命中 8 傷 + 凋零 II（4 秒）+ 沉淪 4p/3c　白：命中 4 傷 + 失明（3 秒）+ 沉淪 3p/2c
 * （命中效果與彈道皆在 {@link WeaponEvents}，本類別只負責觸發。）
 *
 * <p>未配對時右鍵不擊發、不消耗彈藥、不進冷卻，僅在動作列提示；左鍵近戰不受限制。
 */
public class SolemnLamentItem extends Item {

    /** 每把槍各自的冷卻，1.2 秒。 */
    public static final int COOLDOWN_TICKS = 24;

    public final boolean isBlack;

    public SolemnLamentItem(boolean isBlack, Settings settings) {
        super(settings);
        this.isBlack = isBlack;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        // 只由主手驅動，否則一次點擊會打出兩發
        if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

        ItemStack main = user.getStackInHand(Hand.MAIN_HAND);
        ItemStack off = user.getStackInHand(Hand.OFF_HAND);

        if (!SolemnLamentLogic.isPaired(stackIsSolemn(main), stackIsBlack(main),
                stackIsSolemn(off), stackIsBlack(off))) {
            if (!world.isClient) {
                user.sendMessage(Text.translatable("msg.limbusego.solemn_lament.need_pair")
                        .styled(s -> s.withColor(0xFF5555)), true);
            }
            return ActionResult.FAIL;
        }

        var cd = user.getItemCooldownManager();
        Optional<Hand> pick = SolemnLamentLogic.pickHand(!cd.isCoolingDown(main), !cd.isCoolingDown(off));
        if (pick.isEmpty()) return ActionResult.FAIL;

        // 彈藥檢查在兩端都做，避免客戶端先進冷卻但伺服端沒開槍
        ItemStack ammo = WeaponEvents.findButterfly(user);
        if (ammo == null) return ActionResult.FAIL;

        Hand firing = pick.get();
        ItemStack gun = user.getStackInHand(firing);
        cd.set(gun, COOLDOWN_TICKS);

        if (world instanceof ServerWorld sw) {
            ammo.decrement(1);
            WeaponEvents.fireSolemnLament(user, sw, stackIsBlack(gun), firing);
            // 再上膛聲：原本綁在上弦流程的資產，改接在擊發之後
            sw.playSound(null, user.getBlockPos(), ModSounds.SOLEMN_QUICK_LOAD_3,
                    SoundCategory.PLAYERS, 0.7f, 1.0f);
        }
        return ActionResult.SUCCESS;
    }

    private static boolean stackIsSolemn(ItemStack stack) {
        return stack.getItem() instanceof SolemnLamentItem;
    }

    /** 非莊嚴哀悼一律回傳 false（呼叫端會先用 stackIsSolemn 過濾）。 */
    private static boolean stackIsBlack(ItemStack stack) {
        return stack.getItem() instanceof SolemnLamentItem s && s.isBlack;
    }
}
```

- [ ] **Step 2: 刪除 CrossbowItemMixin 並從設定中移除**

```bash
git rm src/main/java/me/yisang/limbusego/mixin/CrossbowItemMixin.java
```

在 `src/main/resources/limbusego.mixins.json` 的 `mixins` 陣列中刪掉 `"CrossbowItemMixin",` 那一行，結果為：

```json
  "mixins": [
    "AnvilScreenHandlerMixin",
    "ItemNameMixin",
    "LivingEntityMixin"
  ],
```

- [ ] **Step 3: 加入提示訊息的翻譯鍵**

`zh_tw.json`：

```json
  "msg.limbusego.solemn_lament.need_pair": "需雙持莊嚴哀悼（黑與白）"
```

`en_us.json`：

```json
  "msg.limbusego.solemn_lament.need_pair": "Requires dual-wielding Solemn Lament (black and white)"
```

- [ ] **Step 4: 編譯**

Run: `./gradlew.bat build -x test`
Expected: BUILD SUCCESSFUL

若出現 `PlayerEntityRendererMixin` 相關的編譯錯誤（它引用了 `CrossbowItem.isCharged`），**先不要動它**——那是 Task 3 的工作。`CrossbowItem` 類別本身仍存在，因此該處應仍可編譯；若真的失敗，把 Task 3 提前做完再回來。

- [ ] **Step 5: 全部測試**

Run: `./gradlew.bat test`
Expected: BUILD SUCCESSFUL。`LangParityTest` 會驗證新鍵中英文都有。

- [ ] **Step 6: 遊戲內驗證擊發行為**

Run: `./gradlew.bat runClient`

逐項確認：
1. 只拿黑槍右鍵：無反應，動作列出現紅字「需雙持莊嚴哀悼（黑與白）」。
2. 主手黑、副手白右鍵：**立即**打出黑彈，沒有拉弓過程。
3. 連續點擊：黑白交替，物品欄兩把各自出現灰色冷卻覆蓋。
4. 停手 2 秒再點：從主手那把開始。
5. 丟掉全部生蝶亡蝶後右鍵：無反應，且**不會**進入冷卻。
6. 黑+黑：無反應並提示。

- [ ] **Step 7: Commit**

```bash
git add -A src
git commit -m "feat: 莊嚴哀悼改為黑白雙持點射 / Make Solemn Lament a paired dual-wield sidearm"
```

---

### Task 3: 雙持時的舉槍姿勢

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/mixin/client/PlayerEntityRendererMixin.java`

**Interfaces:**
- Consumes: `SolemnLamentLogic.isPaired(...)`（Task 1）、`SolemnLamentItem.isBlack`（Task 2）。
- Produces: 無新 API。

**背景：** 原本的條件是「上弦中 → `CROSSBOW_CHARGE`、已裝填 → `CROSSBOW_HOLD`」。Task 2 移除了上弦與 `CHARGED_PROJECTILES`，這兩個條件都不再會成立，姿勢會整個消失。改為配對成立時雙手都舉起。

- [ ] **Step 1: 改寫 mixin**

把 `PlayerEntityRendererMixin.java` 整份換成：

```java
package me.yisang.limbusego.mixin.client;

import me.yisang.limbusego.item.SolemnLamentItem;
import me.yisang.limbusego.item.SolemnLamentLogic;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 莊嚴哀悼成功黑白雙持時，雙手都套用 CROSSBOW_HOLD，呈現舉槍姿態。
 *
 * <p>vanilla 的 getArmPose 把 CROSSBOW_HOLD 寫死判 {@code stack.isOf(Items.CROSSBOW)}，
 * 自訂物品拿不到，故在此補上。未配對時不介入，維持一般持物姿勢。
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(
            method = "getArmPose(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void limbusego$solemnDualWieldPose(PlayerEntity player, ItemStack stack, Hand hand,
            CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
        if (!(stack.getItem() instanceof SolemnLamentItem)) return;

        ItemStack main = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack off = player.getStackInHand(Hand.OFF_HAND);
        if (SolemnLamentLogic.isPaired(stackIsSolemn(main), stackIsBlack(main),
                stackIsSolemn(off), stackIsBlack(off))) {
            cir.setReturnValue(BipedEntityModel.ArmPose.CROSSBOW_HOLD);
        }
    }

    private static boolean stackIsSolemn(ItemStack stack) {
        return stack.getItem() instanceof SolemnLamentItem;
    }

    private static boolean stackIsBlack(ItemStack stack) {
        return stack.getItem() instanceof SolemnLamentItem s && s.isBlack;
    }
}
```

- [ ] **Step 2: 編譯**

Run: `./gradlew.bat build -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 遊戲內驗證姿勢**

Run: `./gradlew.bat runClient`

1. 雙持黑白，按 F5 切第三人稱：**雙手**都呈舉槍姿勢。
2. 把副手的槍拿掉：姿勢恢復成一般持物。
3. 主手黑、副手黑：不應舉槍（未配對）。

- [ ] **Step 4: Commit**

```bash
git add src/main/java/me/yisang/limbusego/mixin/client/PlayerEntityRendererMixin.java
git commit -m "feat: 雙持莊嚴哀悼時雙手舉槍姿勢 / Raise both arms when Solemn Lament is paired"
```

---

### Task 4: 同步 tooltip 與 README

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/item/WeaponTooltips.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Modify: `README.md`
- Modify: `README.en.md`

**Interfaces:**
- Consumes: `SolemnLamentItem.COOLDOWN_TICKS`（Task 2，值 24 = 1.2 秒）；既有的 `TooltipFormat.section/body/status/potency`。
- Produces: 無新 API。

**背景：** 現有 tooltip 寫的是「長按右鍵上弦，放開射出」「上弦時消耗一枚生蝶、亡蝶」，Task 2 之後全部是假的。這是剛建立的 tooltip 系統第一次實戰維護——說明必須跟著程式走。

- [ ] **Step 1: 改寫 WeaponTooltips 的兩個條目**

把 `WeaponTooltips.TABLE` 中 `solemn_lament_black` 與 `solemn_lament_white` 兩個 `Map.entry` 換成：

```java
        // 莊嚴哀悼（黑）：SolemnLamentItem.use + WeaponEvents.tickProjectiles isBlack 分支
        Map.entry("solemn_lament_black", List.of(
            TooltipFormat.section(P + "solemn_lament.dual"),
            TooltipFormat.body(P + "solemn_lament.dual.shoot"),
            TooltipFormat.body(P + "solemn_lament.dual.ammo"),
            TooltipFormat.body(P + "solemn_lament.dual.cooldown", "1.2"),
            TooltipFormat.section(P + "solemn_lament.hit"),
            TooltipFormat.body(P + "solemn_lament.fire.damage", 8),
            TooltipFormat.body(P + "solemn_lament.fire.wither", 4),
            TooltipFormat.body(P + "solemn_lament.fire.status",
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(4, 3)),
            TooltipFormat.section(P + "solemn_lament.melee"),
            TooltipFormat.body(P + "solemn_lament.melee.note"))),

        // 莊嚴哀悼（白）：同上 else 分支
        Map.entry("solemn_lament_white", List.of(
            TooltipFormat.section(P + "solemn_lament.dual"),
            TooltipFormat.body(P + "solemn_lament.dual.shoot"),
            TooltipFormat.body(P + "solemn_lament.dual.ammo"),
            TooltipFormat.body(P + "solemn_lament.dual.cooldown", "1.2"),
            TooltipFormat.section(P + "solemn_lament.hit"),
            TooltipFormat.body(P + "solemn_lament.fire.damage", 4),
            TooltipFormat.body(P + "solemn_lament.fire.blind", 3),
            TooltipFormat.body(P + "solemn_lament.fire.status",
                    TooltipFormat.status(StatusEffect.SINKING), TooltipFormat.potency(3, 2)),
            TooltipFormat.section(P + "solemn_lament.melee"),
            TooltipFormat.body(P + "solemn_lament.melee.note"))),
```

- [ ] **Step 2: 新增與改寫 lang 鍵**

在兩個 lang 檔**新增**：

`zh_tw.json`：

```json
  "tooltip.limbusego.solemn_lament.dual": "雙持射擊（主手與副手各持黑、白一把）",
  "tooltip.limbusego.solemn_lament.dual.shoot": "右鍵一下打出一發，無須上弦",
  "tooltip.limbusego.solemn_lament.dual.ammo": "每發消耗一枚生蝶、亡蝶",
  "tooltip.limbusego.solemn_lament.dual.cooldown": "兩把各自冷卻 %s 秒，輪流擊發可連續輸出",
  "tooltip.limbusego.solemn_lament.hit": "命中時"
```

`en_us.json`：

```json
  "tooltip.limbusego.solemn_lament.dual": "Dual-wield fire (one black and one white, main hand and off hand)",
  "tooltip.limbusego.solemn_lament.dual.shoot": "One right-click fires one shot; no drawing",
  "tooltip.limbusego.solemn_lament.dual.ammo": "Each shot consumes one Living Butterfly, Dead Butterfly",
  "tooltip.limbusego.solemn_lament.dual.cooldown": "Each gun has its own %s second cooldown; alternate to keep firing",
  "tooltip.limbusego.solemn_lament.hit": "On hit"
```

**改寫**既有的 `solemn_lament.melee.note`（原文說「不消耗彈藥」，語意仍對，但要補上單持限制）：

`zh_tw.json`：`"tooltip.limbusego.solemn_lament.melee.note": "單持時仍可如一般武器揮擊，但無法射擊"`

`en_us.json`：`"tooltip.limbusego.solemn_lament.melee.note": "Still swings like an ordinary weapon when unpaired, but cannot fire"`

**刪除**兩個 lang 檔中作廢的鍵：`tooltip.limbusego.solemn_lament.fire`、`tooltip.limbusego.solemn_lament.fire.load`。

- [ ] **Step 3: 執行測試**

Run: `./gradlew.bat test`
Expected: BUILD SUCCESSFUL。`WeaponTooltipsTest.everyReferencedKeyExistsInBothLanguages` 會抓出漏掉的新鍵，`LangParityTest` 會抓出中英不對稱。

- [ ] **Step 4: 更新 README**

`README.md` 的武器表，把黑白兩列換成：

```markdown
| 莊嚴哀悼（黑） | — | **需黑白雙持**；右鍵一下一發、消耗蝴蝶石英，每把各自冷卻 1.2s；命中 8 傷＋凋零 II＋沉淪 4p/3c |
| 莊嚴哀悼（白） | — | 同上，命中 4 傷＋失明＋沉淪 3p/2c |
```

`README.en.md` 的對應兩列換成：

```markdown
| Solemn Lament (Black) | — | **Requires dual-wielding black + white**; one right-click per shot, consumes Butterfly Quartz, 1.2s cooldown per gun; hit deals 8 damage + Wither II + Sinking 4p/3c |
| Solemn Lament (White) | — | Same, hit deals 4 damage + Blindness + Sinking 3p/2c |
```

- [ ] **Step 5: 遊戲內驗收**

Run: `./gradlew.bat runClient`

1. 按住 Shift 看黑槍與白槍的 tooltip，內容與實際操作完全一致，沒有任何「上弦」字樣。
2. 切英文再看一次，沒有原始翻譯鍵、沒有中英夾雜。

- [ ] **Step 6: Commit**

```bash
git add -A src README.md README.en.md
git commit -m "docs: 同步莊嚴哀悼的 tooltip 與 README / Sync Solemn Lament tooltips and READMEs"
```
