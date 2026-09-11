# E.G.O 提取機 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 80 件 E.G.O 飾品獲得單機可玩的正規取得管道——打怪掉 Enkephalin 與殘影，投入提取機方塊，隨機取得該階級的飾品。

**Architecture:** 隨機性與平衡數值全部隔離在無 Minecraft 依賴的 `ExtractionLogic`；`ExtractionPools` 從 `GiftRegistry` 依階級分池。方塊層仿熔爐：`ExtractorBlockEntity` 三槽庫存 + tick 進度 + NBT 存讀，`ExtractorScreenHandler` 用 `PropertyDelegate` 同步進度，客戶端 `ExtractorScreen` 只畫背景與進度條，沒有按鈕、沒有自訂封包。掉落用 `LootTableEvents.MODIFY` 動態注入，不覆寫原生 JSON。方塊用 vanilla `crafting_shaped` 合成。

**Tech Stack:** Java 21、Minecraft 1.21.4、Yarn `1.21.4+build.8`、Fabric Loader 0.16.9、Fabric API 0.119.4+1.21.4（`fabric-loot-api-v3`、`fabric-object-builder-api-v1`、`fabric-screen-handler-api-v1`、`fabric-transitive-access-wideners-v1`）、Accessories 1.2.19-beta、JUnit 5.11.4、Python 3 + Pillow（產占位圖）。

**Spec:** `docs/superpowers/specs/2026-08-11-ego-extractor-design.md`

---

## Global Constraints

這些規則適用於**每一個** task，不再逐項重複：

1. **數值全部是 `public static final` 常數，不引入 config 系統。** 消耗／時間／機率一律照 spec §3.2、§3.3：
   - 階級 I~IV 的 Enkephalin 消耗：`8 / 16 / 32 / 64`；提取時間：`100 / 160 / 240 / 400` tick。
   - 掉落：Enkephalin 所有生物 `25%`；dark `2%`、faint `5%`、twinkling `8%`、brilliant `100%`。
2. **`ExtractionLogic` 不得 import 任何 `net.minecraft` 類別**，只吃 int／double／boolean、只吐 int／boolean。
3. **BlockEntity tick 裡不得丟例外**；任何不能跑的情況都是靜默暫停 `progress = 0`。
4. **不覆寫任何原生 loot table JSON**，只走 `LootTableEvents.MODIFY`；不從 vanilla 資源包複製任何 PNG。
5. **所有玩家可見文字都必須是翻譯鍵**，新鍵同時寫進 `src/main/resources/assets/limbusego/lang/zh_tw.json` 與 `en_us.json`，兩檔鍵集必須完全相同（`LangParityTest` 把關）。**JSON 檔尾不留逗號**。
6. **不要呼叫 `Bootstrap.initialize()`**，測試只碰純 Java 物件。
7. **物品／方塊一律進本模組的「E.G.O 飾品」創造頁籤**（`ModItemGroups.GIFTS_GROUP_KEY`），不進原版頁籤。
8. **每個 task 結束都要 commit**，訊息格式沿用 repo 慣例：`<type>: <中文摘要> / <English summary>`，並附上：
   ```
   Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>
   ```
   直接在 `master` 上開發，不開分支。
9. **建置與測試指令**（Windows，repo 根目錄）：
   - 編譯：`./gradlew.bat build -x test`
   - 全部測試：`./gradlew.bat test`
   - 單一測試：`./gradlew.bat test --tests "me.yisang.limbusego.extractor.ExtractionLogicTest"`
   - 開遊戲：`./gradlew.bat runClient`（背景執行；用戶關閉遊戲後回傳 exit code `0xC000041D`，這是正常關閉不是 crash）。
10. **Bash 工具的 heredoc 會把 `\\` 折成 `\`**——含反斜線跳脫的 Java 字串（正規表達式等）請用 Edit 工具寫。本計畫沒有這種字串。

## 檔案結構

全部新元件放新套件 `me.yisang.limbusego.extractor`（客戶端 Screen 放 `me.yisang.limbusego.client`，與 `EgoTooltipHandler` 同處）。

| 檔案 | 動作 | 職責 |
|---|---|---|
| `src/main/java/me/yisang/limbusego/extractor/ExtractionLogic.java` | 新增 | 純函式：`costOf`、`durationOf`、`canStart`、`pick` |
| `src/main/java/me/yisang/limbusego/extractor/ExtractionPools.java` | 新增 | 階級 → 該階飾品物品清單；啟動時 log 四階池大小 |
| `src/main/java/me/yisang/limbusego/extractor/ExtractorScreenHandler.java` | 新增 | 3 個機器槽 + 36 個玩家槽、`PropertyDelegate`、Shift 快速移動 |
| `src/main/java/me/yisang/limbusego/extractor/ExtractorBlockEntity.java` | 新增 | 三槽庫存、tick 進度、NBT、完成時產出 |
| `src/main/java/me/yisang/limbusego/extractor/ExtractorBlock.java` | 新增 | 方塊本體、水平朝向、右鍵開 GUI、破壞噴出內容物 |
| `src/main/java/me/yisang/limbusego/extractor/ModBlocks.java` | 新增 | Block、BlockItem、BlockEntityType、ScreenHandlerType 註冊 |
| `src/main/java/me/yisang/limbusego/extractor/LootInjection.java` | 新增 | `LootTableEvents.MODIFY` 注入 Enkephalin 與殘影 |
| `src/main/java/me/yisang/limbusego/client/ExtractorScreen.java` | 新增 | 背景圖與進度條 |
| `src/main/java/me/yisang/limbusego/item/ModItems.java` | 修改 | 新增 `ENKEPHALIN` |
| `src/main/java/me/yisang/limbusego/item/ModItemGroups.java` | 修改 | 飾品頁籤加 Enkephalin 與提取機 |
| `src/main/java/me/yisang/limbusego/LimbusEGOMod.java` | 修改 | 呼叫 `ModBlocks.register()`、`ExtractionPools.build()`、`LootInjection.register()` |
| `src/main/java/me/yisang/limbusego/client/LimbusEGOClient.java` | 修改 | `HandledScreens.register` |
| `src/main/resources/assets/limbusego/items/{enkephalin,extractor}.json` | 新增 | 1.21.4 item model definition |
| `src/main/resources/assets/limbusego/models/item/enkephalin.json` | 新增 | 物品模型 |
| `src/main/resources/assets/limbusego/models/block/extractor.json` | 新增 | `orientable` 方塊模型 |
| `src/main/resources/assets/limbusego/blockstates/extractor.json` | 新增 | 四向 variants |
| `src/main/resources/assets/limbusego/textures/item/enkephalin.png` | 新增 | 占位圖（腳本生成） |
| `src/main/resources/assets/limbusego/textures/block/extractor_{top,side,front}.png` | 新增 | 占位圖（腳本生成） |
| `src/main/resources/assets/limbusego/textures/gui/extractor.png` | 新增 | 256×256 GUI 背景 + 進度條占位圖（腳本生成） |
| `src/main/resources/data/limbusego/recipe/extractor.json` | 新增 | 工作台配方 |
| `src/main/resources/data/limbusego/loot_table/blocks/extractor.json` | 新增 | 破壞掉落自身 |
| `src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json` | 新增 | 需鎬開採 |
| `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json` | 修改 | `item.limbusego.enkephalin`、`block.limbusego.extractor`、`container.limbusego.extractor` |
| `src/test/java/me/yisang/limbusego/extractor/ExtractionLogicTest.java` | 新增 | 純邏輯測試 |
| `README.md`、`README.en.md` | 修改 | 提取機章節；順手修正殘影名稱 |

槽位索引（`ExtractorBlockEntity` 與 `ExtractorScreenHandler` 共用）：

| 索引 | 常數 | 內容 | GUI 座標 |
|---|---|---|---|
| 0 | `SLOT_CATALYST` | 殘影 | (56, 17) |
| 1 | `SLOT_ENKEPHALIN` | Enkephalin | (56, 53) |
| 2 | `SLOT_OUTPUT` | 產出飾品 | (116, 35) |

`PropertyDelegate` 索引：`[0] = progress`、`[1] = maxProgress`。

---

### Task 1: `ExtractionLogic` 純函式

**Files:**
- Create: `src/main/java/me/yisang/limbusego/extractor/ExtractionLogic.java`
- Test: `src/test/java/me/yisang/limbusego/extractor/ExtractionLogicTest.java`

**Interfaces:**
- Consumes: 無。
- Produces:
  - `static int ExtractionLogic.costOf(int tier)` — 1~4 → `8/16/32/64`；越界 → `-1`。
  - `static int ExtractionLogic.durationOf(int tier)` — 1~4 → `100/160/240/400`；越界 → `-1`。
  - `static boolean ExtractionLogic.canStart(int tier, int enkephalin, boolean outputFree, int poolSize)`。
  - `static int ExtractionLogic.pick(int poolSize, double roll)` — `[0, poolSize)` 的索引，永不越界。

- [ ] **Step 1: 寫失敗測試**

```java
package me.yisang.limbusego.extractor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtractionLogicTest {

    @Test
    void costMatchesSpec() {
        assertEquals(8, ExtractionLogic.costOf(1));
        assertEquals(16, ExtractionLogic.costOf(2));
        assertEquals(32, ExtractionLogic.costOf(3));
        assertEquals(64, ExtractionLogic.costOf(4));
    }

    @Test
    void durationMatchesSpec() {
        assertEquals(100, ExtractionLogic.durationOf(1));
        assertEquals(160, ExtractionLogic.durationOf(2));
        assertEquals(240, ExtractionLogic.durationOf(3));
        assertEquals(400, ExtractionLogic.durationOf(4));
    }

    @Test
    void outOfRangeTierIsInvalid() {
        for (int tier : new int[]{-1, 0, 5, 99}) {
            assertEquals(-1, ExtractionLogic.costOf(tier), "costOf(" + tier + ")");
            assertEquals(-1, ExtractionLogic.durationOf(tier), "durationOf(" + tier + ")");
        }
    }

    @Test
    void startsWhenEverythingIsReady() {
        assertTrue(ExtractionLogic.canStart(1, 8, true, 10));
        assertTrue(ExtractionLogic.canStart(4, 64, true, 1));
        assertTrue(ExtractionLogic.canStart(2, 999, true, 3), "Enkephalin 超過所需也可以跑");
    }

    @Test
    void refusesInvalidTier() {
        // Vestiges.tierOf 對非殘影回 -1
        assertFalse(ExtractionLogic.canStart(-1, 64, true, 10));
        assertFalse(ExtractionLogic.canStart(0, 64, true, 10));
        assertFalse(ExtractionLogic.canStart(5, 64, true, 10));
    }

    @Test
    void refusesInsufficientEnkephalin() {
        assertFalse(ExtractionLogic.canStart(1, 7, true, 10));
        assertFalse(ExtractionLogic.canStart(3, 31, true, 10));
        assertFalse(ExtractionLogic.canStart(1, 0, true, 10));
    }

    @Test
    void refusesWhenOutputBlocked() {
        assertFalse(ExtractionLogic.canStart(1, 8, false, 10));
    }

    @Test
    void refusesEmptyPool() {
        assertFalse(ExtractionLogic.canStart(1, 8, true, 0));
        assertFalse(ExtractionLogic.canStart(1, 8, true, -3));
    }

    @Test
    void pickCoversBothEnds() {
        assertEquals(0, ExtractionLogic.pick(10, 0.0));
        assertEquals(9, ExtractionLogic.pick(10, 0.999999));
        assertEquals(5, ExtractionLogic.pick(10, 0.5));
    }

    @Test
    void pickNeverOverflowsEvenAtOne() {
        // Random.nextDouble() 理論上 < 1.0，但防禦性地把 1.0 也夾在最後一格
        assertEquals(9, ExtractionLogic.pick(10, 1.0));
        assertEquals(9, ExtractionLogic.pick(10, 1.7));
        assertEquals(0, ExtractionLogic.pick(10, -0.2));
    }

    @Test
    void pickSingletonPoolIsAlwaysZero() {
        for (double roll : new double[]{0.0, 0.3, 0.99, 1.0}) {
            assertEquals(0, ExtractionLogic.pick(1, roll));
        }
    }
}
```

- [ ] **Step 2: 跑測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.extractor.ExtractionLogicTest"`
Expected: 編譯失敗，`ExtractionLogic` 不存在。

- [ ] **Step 3: 寫最小實作**

```java
package me.yisang.limbusego.extractor;

/**
 * 提取機的純決策函式：消耗、時間、可否啟動、池內抽選。
 *
 * <p>刻意不依賴任何 Minecraft 型別（照 {@code GiftUpgradeLogic} 慣例），
 * 平衡數值可直接用 JUnit 驗證。方塊層 {@link ExtractorBlockEntity} 只負責
 * 「讀槽位 → 呼叫本類 → 套用結果」。
 */
public final class ExtractionLogic {

    /** 索引 = 階級（1~4），索引 0 不用。Tier III 的 32 對齊 Paper 版 gacha.lunacy-cost。 */
    public static final int[] ENKEPHALIN_COST = {0, 8, 16, 32, 64};
    /** 索引 = 階級（1~4），單位 tick。 */
    public static final int[] DURATION_TICKS = {0, 100, 160, 240, 400};

    public static final int MIN_TIER = 1;
    public static final int MAX_TIER = 4;

    private ExtractionLogic() {}

    public static boolean isValidTier(int tier) {
        return tier >= MIN_TIER && tier <= MAX_TIER;
    }

    /** 該階級一次提取消耗的 Enkephalin；階級越界回 -1。 */
    public static int costOf(int tier) {
        return isValidTier(tier) ? ENKEPHALIN_COST[tier] : -1;
    }

    /** 該階級一次提取所需 tick；階級越界回 -1。 */
    public static int durationOf(int tier) {
        return isValidTier(tier) ? DURATION_TICKS[tier] : -1;
    }

    /**
     * @param tier       觸媒槽殘影的階級；非殘影為 -1
     * @param enkephalin Enkephalin 槽目前數量
     * @param outputFree 產出槽是否可放（空）
     * @param poolSize   該階級飾品池大小
     */
    public static boolean canStart(int tier, int enkephalin, boolean outputFree, int poolSize) {
        if (!isValidTier(tier)) return false;
        if (enkephalin < ENKEPHALIN_COST[tier]) return false;
        if (!outputFree) return false;
        return poolSize > 0;
    }

    /** 把 [0,1) 的亂數映射到池索引；任何輸入都夾在 [0, poolSize-1]。 */
    public static int pick(int poolSize, double roll) {
        if (poolSize <= 1) return 0;
        int idx = (int) Math.floor(roll * poolSize);
        if (idx < 0) return 0;
        if (idx >= poolSize) return poolSize - 1;
        return idx;
    }
}
```

- [ ] **Step 4: 跑測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.extractor.ExtractionLogicTest"`
Expected: 11 tests PASS。

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/yisang/limbusego/extractor/ExtractionLogic.java src/test/java/me/yisang/limbusego/extractor/ExtractionLogicTest.java
git commit -m "feat: 提取機純決策函式 / Add ExtractionLogic with cost, duration, start and pick rules"
```

---

### Task 2: Enkephalin 物品與 `ExtractionPools`

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/item/ModItems.java`
- Modify: `src/main/java/me/yisang/limbusego/item/ModItemGroups.java`
- Create: `src/main/java/me/yisang/limbusego/extractor/ExtractionPools.java`
- Modify: `src/main/java/me/yisang/limbusego/LimbusEGOMod.java`
- Create: `src/main/resources/assets/limbusego/items/enkephalin.json`、`models/item/enkephalin.json`、`textures/item/enkephalin.png`
- Modify: `src/main/resources/assets/limbusego/lang/zh_tw.json`、`en_us.json`

**Interfaces:**
- Consumes: `GiftRegistry.all() → Collection<BaseGift>`、`BaseGift.tier()`、`ModGifts.byId() → Map<String, Item>`、`BaseGift.id()`（既有）。
- Produces:
  - `public static Item ModItems.ENKEPHALIN`。
  - `static void ExtractionPools.build()` — 在 `ModGifts.register()` 之後呼叫一次。
  - `static List<Item> ExtractionPools.of(int tier)` — 查無回空 `List`。

- [ ] **Step 1: 註冊 Enkephalin**

`ModItems.java` 欄位區加：

```java
    /** 提取機通用貨幣（腦啡肽）。 */
    public static Item ENKEPHALIN;
```

`register()` 的 `MOD_ICON = ...` 之後加：

```java
        ENKEPHALIN = reg("enkephalin", new Item(key("enkephalin").maxCount(64).rarity(Rarity.UNCOMMON)));
```

- [ ] **Step 2: 物品資產**

`src/main/resources/assets/limbusego/items/enkephalin.json`：

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "limbusego:item/enkephalin"
  }
}
```

`src/main/resources/assets/limbusego/models/item/enkephalin.json`：

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "limbusego:item/enkephalin"
  }
}
```

占位材質用 Python 生成（16×16，青綠色圓形能量塊），存到 `src/main/resources/assets/limbusego/textures/item/enkephalin.png`：

```python
from PIL import Image, ImageDraw
im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
d.ellipse((2, 2, 13, 13), fill=(70, 220, 200, 255), outline=(20, 90, 80, 255))
d.ellipse((5, 4, 8, 7), fill=(200, 255, 245, 255))
im.save("src/main/resources/assets/limbusego/textures/item/enkephalin.png")
```

- [ ] **Step 3: 翻譯鍵**

`zh_tw.json` 在 `"item.limbusego.brilliant_vestige"` 那行之後插入（注意逗號）：

```json
  "item.limbusego.enkephalin": "腦啡肽",
```

`en_us.json` 同位置：

```json
  "item.limbusego.enkephalin": "Enkephalin",
```

- [ ] **Step 4: `ExtractionPools`**

```java
package me.yisang.limbusego.extractor;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.gift.GiftRegistry;
import me.yisang.limbusego.gift.ModGifts;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 提取機的抽獎池：階級 → 該階全部飾品物品。
 *
 * <p>從 {@link GiftRegistry} 依 {@link BaseGift#tier()} 反推，不維護第二份清單——
 * 日後新增飾品會自動進池。{@link #build()} 在 {@code ModGifts.register()} 之後呼叫一次，
 * 並把四階池大小寫進日誌，空池在啟動階段就看得見。
 */
public final class ExtractionPools {

    private static final List<List<Item>> POOLS = new ArrayList<>();

    private ExtractionPools() {}

    public static void build() {
        POOLS.clear();
        for (int t = 0; t <= ExtractionLogic.MAX_TIER; t++) POOLS.add(new ArrayList<>());

        Map<String, Item> byId = ModGifts.byId();
        for (BaseGift gift : GiftRegistry.all()) {
            int tier = gift.tier();
            Item item = byId.get(gift.id());
            if (item == null || !ExtractionLogic.isValidTier(tier)) continue;
            POOLS.get(tier).add(item);
        }
        for (int t = ExtractionLogic.MIN_TIER; t <= ExtractionLogic.MAX_TIER; t++) {
            POOLS.set(t, List.copyOf(POOLS.get(t)));
        }
        LimbusEGOMod.LOGGER.info("提取機抽獎池：I={} II={} III={} IV={}",
                of(1).size(), of(2).size(), of(3).size(), of(4).size());
    }

    /** 該階級的飾品池；階級越界或尚未 build 回空清單。 */
    public static List<Item> of(int tier) {
        if (!ExtractionLogic.isValidTier(tier) || tier >= POOLS.size()) return List.of();
        return POOLS.get(tier);
    }
}
```

先確認 `BaseGift` 有 `id()`：

Run: `grep -n "public String id()" src/main/java/me/yisang/limbusego/gift/BaseGift.java`
Expected: 一行。

- [ ] **Step 5: 初始化與頁籤**

`LimbusEGOMod.onInitialize()` 在 `me.yisang.limbusego.gift.Vestiges.register();` 之後加：

```java
        me.yisang.limbusego.extractor.ExtractionPools.build();
```

`ModItemGroups` 飾品頁籤的 `entries` 末尾（`BRILLIANT_VESTIGE` 之後）加：

```java
                            // 提取機材料
                            entries.add(ModItems.ENKEPHALIN);
```

- [ ] **Step 6: 編譯並跑全部測試**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL（`LangParityTest` 過代表兩檔鍵集一致）。

- [ ] **Step 7: Commit**

```bash
git add src/main/java/me/yisang/limbusego/item/ModItems.java src/main/java/me/yisang/limbusego/item/ModItemGroups.java src/main/java/me/yisang/limbusego/extractor/ExtractionPools.java src/main/java/me/yisang/limbusego/LimbusEGOMod.java src/main/resources/assets/limbusego/items/enkephalin.json src/main/resources/assets/limbusego/models/item/enkephalin.json src/main/resources/assets/limbusego/textures/item/enkephalin.png src/main/resources/assets/limbusego/lang/zh_tw.json src/main/resources/assets/limbusego/lang/en_us.json
git commit -m "feat: Enkephalin 物品與提取機抽獎池 / Add the Enkephalin item and tier-based extraction pools"
```

---

### Task 3: `ExtractorScreenHandler` 與 ScreenHandlerType 註冊

先做 ScreenHandler 是因為 BlockEntity 的 `createScreenHandler` 需要它；本 task 用 `SimpleInventory` 即可獨立編譯。

**Files:**
- Create: `src/main/java/me/yisang/limbusego/extractor/ExtractorScreenHandler.java`
- Create: `src/main/java/me/yisang/limbusego/extractor/ModBlocks.java`（本 task 只放 ScreenHandlerType，Task 4 再補方塊）
- Modify: `src/main/java/me/yisang/limbusego/LimbusEGOMod.java`

**Interfaces:**
- Consumes: `Vestiges.isVestige(Item)`、`ModItems.ENKEPHALIN`。
- Produces:
  - `ExtractorScreenHandler(int syncId, PlayerInventory playerInv)` — 客戶端建構子（空 `SimpleInventory(3)` + `ArrayPropertyDelegate(2)`）。
  - `ExtractorScreenHandler(int syncId, PlayerInventory playerInv, Inventory inventory, PropertyDelegate delegate)` — 伺服端建構子。
  - `int ExtractorScreenHandler.getProgress()`、`int getMaxProgress()`。
  - 常數 `SLOT_CATALYST = 0`、`SLOT_ENKEPHALIN = 1`、`SLOT_OUTPUT = 2`、`MACHINE_SLOTS = 3`。
  - `public static ScreenHandlerType<ExtractorScreenHandler> ModBlocks.EXTRACTOR_SCREEN_HANDLER`。

- [ ] **Step 1: 寫 `ExtractorScreenHandler`**

```java
package me.yisang.limbusego.extractor;

import me.yisang.limbusego.gift.Vestiges;
import me.yisang.limbusego.item.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

/**
 * 提取機的容器：3 個機器槽（觸媒／Enkephalin／產出）+ 36 個玩家槽。
 * 進度用 {@link PropertyDelegate} 同步（[0]=progress、[1]=maxProgress），與原版熔爐同做法。
 * 沒有按鈕，所以不需要任何自訂封包。
 */
public class ExtractorScreenHandler extends ScreenHandler {

    public static final int SLOT_CATALYST = 0;
    public static final int SLOT_ENKEPHALIN = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int MACHINE_SLOTS = 3;

    private static final int PLAYER_SLOTS = 36;
    private static final int PROPERTY_COUNT = 2;

    private final Inventory inventory;
    private final PropertyDelegate delegate;

    /** 客戶端建構子：由 ScreenHandlerType 透過封包建立，庫存與進度隨後同步。 */
    public ExtractorScreenHandler(int syncId, PlayerInventory playerInv) {
        this(syncId, playerInv, new SimpleInventory(MACHINE_SLOTS), new ArrayPropertyDelegate(PROPERTY_COUNT));
    }

    /** 伺服端建構子：由 {@link ExtractorBlockEntity#createScreenHandler} 傳入實際庫存與進度。 */
    public ExtractorScreenHandler(int syncId, PlayerInventory playerInv, Inventory inventory, PropertyDelegate delegate) {
        super(ModBlocks.EXTRACTOR_SCREEN_HANDLER, syncId);
        checkSize(inventory, MACHINE_SLOTS);
        checkDataCount(delegate, PROPERTY_COUNT);
        this.inventory = inventory;
        this.delegate = delegate;
        inventory.onOpen(playerInv.player);

        addSlot(new Slot(inventory, SLOT_CATALYST, 56, 17) {
            @Override public boolean canInsert(ItemStack stack) { return Vestiges.isVestige(stack.getItem()); }
        });
        addSlot(new Slot(inventory, SLOT_ENKEPHALIN, 56, 53) {
            @Override public boolean canInsert(ItemStack stack) { return stack.isOf(ModItems.ENKEPHALIN); }
        });
        addSlot(new Slot(inventory, SLOT_OUTPUT, 116, 35) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });

        // 玩家背包 3×9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // 快捷列
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        addProperties(delegate);
    }

    public int getProgress() {
        return delegate.get(0);
    }

    public int getMaxProgress() {
        return delegate.get(1);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    /** Shift 點擊：機器槽 → 玩家；玩家 → 依物品種類進觸媒或 Enkephalin 槽，否則背包 ⇄ 快捷列。 */
    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasStack()) return ItemStack.EMPTY;

        ItemStack original = slot.getStack();
        ItemStack copy = original.copy();
        int playerStart = MACHINE_SLOTS;
        int playerEnd = MACHINE_SLOTS + PLAYER_SLOTS;

        if (slotIndex < MACHINE_SLOTS) {
            if (!insertItem(original, playerStart, playerEnd, true)) return ItemStack.EMPTY;
            slot.onQuickTransfer(original, copy);
        } else {
            boolean moved;
            if (Vestiges.isVestige(original.getItem())) {
                moved = insertItem(original, SLOT_CATALYST, SLOT_CATALYST + 1, false);
            } else if (original.isOf(ModItems.ENKEPHALIN)) {
                moved = insertItem(original, SLOT_ENKEPHALIN, SLOT_ENKEPHALIN + 1, false);
            } else if (slotIndex < playerStart + 27) {
                moved = insertItem(original, playerStart + 27, playerEnd, false);   // 背包 → 快捷列
            } else {
                moved = insertItem(original, playerStart, playerStart + 27, false); // 快捷列 → 背包
            }
            if (!moved) return ItemStack.EMPTY;
        }

        if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        if (original.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTakeItem(player, original);
        return copy;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }
}
```

- [ ] **Step 2: `ModBlocks` 先放 ScreenHandlerType**

```java
package me.yisang.limbusego.extractor;

import me.yisang.limbusego.LimbusEGOMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

/** 提取機的方塊、BlockEntityType、ScreenHandlerType 註冊。 */
public final class ModBlocks {

    public static ScreenHandlerType<ExtractorScreenHandler> EXTRACTOR_SCREEN_HANDLER;

    private ModBlocks() {}

    public static void register() {
        // ScreenHandlerType 建構子由 fabric-transitive-access-wideners 開放
        EXTRACTOR_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, LimbusEGOMod.id("extractor"),
                new ScreenHandlerType<>(ExtractorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    }
}
```

- [ ] **Step 3: 初始化**

`LimbusEGOMod.onInitialize()` 在 `ModItems.register();` **之後、`ModGifts.register()` 之前**加：

```java
        me.yisang.limbusego.extractor.ModBlocks.register();
```

- [ ] **Step 4: 編譯**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL。若 `new ScreenHandlerType<>` 報「has private access」，代表 access widener 沒生效——確認 `build.gradle` 的 `fabric_version` 是 `0.119.4+1.21.4`，且未把 `fabric-api` 換成單一模組。

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/yisang/limbusego/extractor/ExtractorScreenHandler.java src/main/java/me/yisang/limbusego/extractor/ModBlocks.java src/main/java/me/yisang/limbusego/LimbusEGOMod.java
git commit -m "feat: 提取機容器與 ScreenHandlerType / Add ExtractorScreenHandler and register its screen handler type"
```

---

### Task 4: 方塊、BlockEntity、資產、配方

**Files:**
- Create: `src/main/java/me/yisang/limbusego/extractor/ExtractorBlockEntity.java`
- Create: `src/main/java/me/yisang/limbusego/extractor/ExtractorBlock.java`
- Modify: `src/main/java/me/yisang/limbusego/extractor/ModBlocks.java`
- Modify: `src/main/java/me/yisang/limbusego/item/ModItemGroups.java`
- Create: `assets/limbusego/blockstates/extractor.json`、`models/block/extractor.json`、`items/extractor.json`、`textures/block/extractor_{top,side,front}.png`
- Create: `data/limbusego/recipe/extractor.json`、`data/limbusego/loot_table/blocks/extractor.json`、`data/minecraft/tags/block/mineable/pickaxe.json`
- Modify: `lang/zh_tw.json`、`lang/en_us.json`

**Interfaces:**
- Consumes: `ExtractionLogic.*`、`ExtractionPools.of(int)`、`Vestiges.tierOf(Item)`、`ModItems.ENKEPHALIN`、`ExtractorScreenHandler`（伺服端建構子與槽位常數）、`ModBlocks.EXTRACTOR_SCREEN_HANDLER`。
- Produces:
  - `public static Block ModBlocks.EXTRACTOR`、`public static Item ModBlocks.EXTRACTOR_ITEM`、`public static BlockEntityType<ExtractorBlockEntity> ModBlocks.EXTRACTOR_BLOCK_ENTITY`。
  - `static void ExtractorBlockEntity.tick(World, BlockPos, BlockState, ExtractorBlockEntity)`。

- [ ] **Step 1: `ExtractorBlockEntity`**

```java
package me.yisang.limbusego.extractor;

import me.yisang.limbusego.gift.Vestiges;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * 提取機方塊實體：三槽庫存 + 進度。行為仿熔爐——條件全滿足自動跑，任一破掉就歸零暫停。
 *
 * <p>本類只做「讀槽位 → 呼叫 {@link ExtractionLogic} → 套用結果」，
 * 所有數值與可否啟動的判斷都在純函式裡。tick 內不丟例外。
 */
public class ExtractorBlockEntity extends LockableContainerBlockEntity {

    private static final String NBT_PROGRESS = "Progress";

    private DefaultedList<ItemStack> stacks = DefaultedList.ofSize(ExtractorScreenHandler.MACHINE_SLOTS, ItemStack.EMPTY);
    private int progress;
    private int maxProgress;

    private final PropertyDelegate delegate = new PropertyDelegate() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                default -> { }
            }
        }
        @Override public int size() { return 2; }
    };

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.EXTRACTOR_BLOCK_ENTITY, pos, state);
    }

    // ── tick ────────────────────────────────────────────────────────

    /** 僅伺服端（由 {@link ExtractorBlock#getTicker} 保證）。 */
    public static void tick(World world, BlockPos pos, BlockState state, ExtractorBlockEntity be) {
        ItemStack catalyst = be.stacks.get(ExtractorScreenHandler.SLOT_CATALYST);
        ItemStack enkephalin = be.stacks.get(ExtractorScreenHandler.SLOT_ENKEPHALIN);
        ItemStack output = be.stacks.get(ExtractorScreenHandler.SLOT_OUTPUT);

        int tier = Vestiges.tierOf(catalyst.getItem());
        List<Item> pool = ExtractionPools.of(tier);
        boolean canStart = ExtractionLogic.canStart(tier, enkephalin.getCount(), output.isEmpty(), pool.size());

        if (!canStart) {
            if (be.progress != 0) {
                be.progress = 0;
                be.markDirty();
            }
            return;
        }

        be.maxProgress = ExtractionLogic.durationOf(tier);
        be.progress++;
        if (be.progress < be.maxProgress) {
            be.markDirty();
            return;
        }

        // 完成：扣材料、抽飾品、放產出
        catalyst.decrement(1);
        enkephalin.decrement(ExtractionLogic.costOf(tier));
        int idx = ExtractionLogic.pick(pool.size(), world.random.nextDouble());
        be.stacks.set(ExtractorScreenHandler.SLOT_OUTPUT, new ItemStack(pool.get(idx)));
        be.progress = 0;
        be.markDirty();
        world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, 1.2f);
    }

    // ── Inventory ───────────────────────────────────────────────────

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return stacks;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    /** 觸媒槽只收殘影、Enkephalin 槽只收 Enkephalin、產出槽不收（漏斗等自動化也受限）。 */
    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            case ExtractorScreenHandler.SLOT_CATALYST -> Vestiges.isVestige(stack.getItem());
            case ExtractorScreenHandler.SLOT_ENKEPHALIN -> stack.isOf(me.yisang.limbusego.item.ModItems.ENKEPHALIN);
            default -> false;
        };
    }

    // ── NamedScreenHandlerFactory ───────────────────────────────────

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.limbusego.extractor");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new ExtractorScreenHandler(syncId, playerInventory, this, delegate);
    }

    // ── NBT ─────────────────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, stacks, registries);
        nbt.putInt(NBT_PROGRESS, progress);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        stacks = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, stacks, registries);
        progress = nbt.getInt(NBT_PROGRESS);
    }
}
```

- [ ] **Step 2: `ExtractorBlock`**

```java
package me.yisang.limbusego.extractor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** 提取機方塊：水平朝向、右鍵開 GUI、破壞時噴出槽內物品。 */
public class ExtractorBlock extends BlockWithEntity {

    public static final MapCodec<ExtractorBlock> CODEC = createCodec(ExtractorBlock::new);

    public ExtractorBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(HorizontalFacingBlock.FACING, net.minecraft.util.math.Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(HorizontalFacingBlock.FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(HorizontalFacingBlock.FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    /** BlockWithEntity 預設 INVISIBLE，必須改回 MODEL 才會畫方塊模型。 */
    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ExtractorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) return null;
        return validateTicker(type, ModBlocks.EXTRACTOR_BLOCK_ENTITY, ExtractorBlockEntity::tick);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof ExtractorBlockEntity be) {
            player.openHandledScreen(be);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof ExtractorBlockEntity be) {
            ItemScatterer.spawn(world, pos, be);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
```

- [ ] **Step 3: `ModBlocks` 補齊註冊**

整個檔案改為：

```java
package me.yisang.limbusego.extractor;

import me.yisang.limbusego.LimbusEGOMod;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;

/** 提取機的方塊、BlockItem、BlockEntityType、ScreenHandlerType 註冊。 */
public final class ModBlocks {

    public static Block EXTRACTOR;
    public static Item EXTRACTOR_ITEM;
    public static BlockEntityType<ExtractorBlockEntity> EXTRACTOR_BLOCK_ENTITY;
    public static ScreenHandlerType<ExtractorScreenHandler> EXTRACTOR_SCREEN_HANDLER;

    private ModBlocks() {}

    public static void register() {
        var id = LimbusEGOMod.id("extractor");

        EXTRACTOR = Registry.register(Registries.BLOCK, id, new ExtractorBlock(AbstractBlock.Settings.create()
                .registryKey(RegistryKey.of(RegistryKeys.BLOCK, id))
                .strength(3.5f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)));

        EXTRACTOR_ITEM = Registry.register(Registries.ITEM, id, new BlockItem(EXTRACTOR, new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
                .useBlockPrefixedTranslationKey()));

        EXTRACTOR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, id,
                FabricBlockEntityTypeBuilder.create(ExtractorBlockEntity::new, EXTRACTOR).build());

        // ScreenHandlerType 建構子由 fabric-transitive-access-wideners 開放
        EXTRACTOR_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, id,
                new ScreenHandlerType<>(ExtractorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
    }
}
```

- [ ] **Step 4: 頁籤**

`ModItemGroups` 飾品頁籤 `entries.add(ModItems.ENKEPHALIN);` 之後加：

```java
                            entries.add(me.yisang.limbusego.extractor.ModBlocks.EXTRACTOR_ITEM);
```

- [ ] **Step 5: 方塊資產**

`assets/limbusego/blockstates/extractor.json`：

```json
{
  "variants": {
    "facing=north": { "model": "limbusego:block/extractor" },
    "facing=east":  { "model": "limbusego:block/extractor", "y": 90 },
    "facing=south": { "model": "limbusego:block/extractor", "y": 180 },
    "facing=west":  { "model": "limbusego:block/extractor", "y": 270 }
  }
}
```

`assets/limbusego/models/block/extractor.json`：

```json
{
  "parent": "minecraft:block/orientable",
  "textures": {
    "top": "limbusego:block/extractor_top",
    "front": "limbusego:block/extractor_front",
    "side": "limbusego:block/extractor_side"
  }
}
```

`assets/limbusego/items/extractor.json`：

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "limbusego:block/extractor"
  }
}
```

占位材質（三張 16×16）用 Python 生成到 `assets/limbusego/textures/block/`：

```python
from PIL import Image, ImageDraw
import random
random.seed(7)
base = "src/main/resources/assets/limbusego/textures/block/"

def plate(seed):
    im = Image.new("RGBA", (16, 16))
    d = ImageDraw.Draw(im)
    for y in range(16):
        for x in range(16):
            g = 78 + random.randint(-6, 6)
            im.putpixel((x, y), (g, g, g + 4, 255))
    d.rectangle((0, 0, 15, 15), outline=(40, 40, 44, 255))
    return im, d

im, d = plate(1); im.save(base + "extractor_side.png")
im, d = plate(2); d.rectangle((3, 3, 12, 12), outline=(120, 120, 126, 255)); im.save(base + "extractor_top.png")
im, d = plate(3)
d.rectangle((3, 4, 12, 11), fill=(20, 24, 30, 255), outline=(110, 110, 116, 255))
d.rectangle((5, 6, 10, 9), fill=(70, 220, 200, 255))
im.save(base + "extractor_front.png")
```

- [ ] **Step 6: 資料包：配方、掉落、標籤**

`data/limbusego/recipe/extractor.json`（spec §3.5 的排列）：

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "IRI",
    "QAQ",
    "IQI"
  ],
  "key": {
    "I": "minecraft:iron_ingot",
    "R": "minecraft:redstone",
    "Q": "minecraft:quartz",
    "A": "minecraft:anvil"
  },
  "result": {
    "id": "limbusego:extractor",
    "count": 1
  }
}
```

`data/limbusego/loot_table/blocks/extractor.json`：

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "rolls": 1,
      "entries": [
        { "type": "minecraft:item", "name": "limbusego:extractor" }
      ],
      "conditions": [
        { "condition": "minecraft:survives_explosion" }
      ]
    }
  ]
}
```

`data/minecraft/tags/block/mineable/pickaxe.json`：

```json
{
  "replace": false,
  "values": [
    "limbusego:extractor"
  ]
}
```

- [ ] **Step 7: 翻譯鍵**

`zh_tw.json` 在 `"item.limbusego.enkephalin"` 之後插入：

```json
  "block.limbusego.extractor": "E.G.O 提取機",
  "container.limbusego.extractor": "E.G.O 提取機",
```

`en_us.json` 同位置：

```json
  "block.limbusego.extractor": "E.G.O Extractor",
  "container.limbusego.extractor": "E.G.O Extractor",
```

- [ ] **Step 8: 編譯並跑全部測試**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 9: 起遊戲冒煙測試**

Run: `./gradlew.bat runClient`（背景）。進單人世界開作弊，從「E.G.O 飾品」頁籤拿提取機放下：方塊有模型、朝向隨玩家、右鍵會開一個**沒有背景圖**的容器（Task 5 才畫）——只確認不 crash、三個槽位可放殘影與 Enkephalin。若右鍵 crash 且訊息含 `No screen registered`，那是預期的（Task 5 補），繼續。

- [ ] **Step 10: Commit**

```bash
git add src/main/java/me/yisang/limbusego/extractor src/main/java/me/yisang/limbusego/item/ModItemGroups.java src/main/resources/assets/limbusego/blockstates src/main/resources/assets/limbusego/models/block src/main/resources/assets/limbusego/items/extractor.json src/main/resources/assets/limbusego/textures/block src/main/resources/data/limbusego src/main/resources/data/minecraft src/main/resources/assets/limbusego/lang
git commit -m "feat: 提取機方塊與方塊實體 / Add the Extractor block, block entity, recipe and assets"
```

---

### Task 5: 客戶端 `ExtractorScreen`

**Files:**
- Create: `src/main/java/me/yisang/limbusego/client/ExtractorScreen.java`
- Modify: `src/main/java/me/yisang/limbusego/client/LimbusEGOClient.java`
- Create: `src/main/resources/assets/limbusego/textures/gui/extractor.png`

**Interfaces:**
- Consumes: `ExtractorScreenHandler.getProgress()` / `getMaxProgress()`、`ModBlocks.EXTRACTOR_SCREEN_HANDLER`。

GUI 圖版面（256×256 PNG，只用左上 176×166 當背景；進度條的「滿版」圖放在 (176, 0)，24×17）：

| 元素 | 位置（相對背景左上） |
|---|---|
| 觸媒槽框 | (55, 16) 18×18 |
| Enkephalin 槽框 | (55, 52) 18×18 |
| 產出槽框 | (115, 34) 18×18 |
| 進度條（空） | (79, 35) 24×17，畫在背景圖裡 |
| 進度條（滿，另存） | 圖內 (176, 0) 24×17，依進度裁寬度蓋上去 |

- [ ] **Step 1: 產 GUI 占位圖**

```python
from PIL import Image, ImageDraw
W, H = 176, 166
im = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
# 面板
d.rounded_rectangle((0, 0, W - 1, H - 1), radius=3, fill=(198, 198, 198, 255), outline=(55, 55, 55, 255))
d.line((1, 1, W - 2, 1), fill=(255, 255, 255, 255)); d.line((1, 1, 1, H - 2), fill=(255, 255, 255, 255))

def slot(x, y):
    d.rectangle((x, y, x + 17, y + 17), fill=(139, 139, 139, 255))
    d.line((x, y, x + 17, y), fill=(55, 55, 55, 255)); d.line((x, y, x, y + 17), fill=(55, 55, 55, 255))
    d.line((x, y + 17, x + 17, y + 17), fill=(255, 255, 255, 255)); d.line((x + 17, y, x + 17, y + 17), fill=(255, 255, 255, 255))

slot(55, 16); slot(55, 52); slot(115, 34)
# 玩家背包 3x9 + 快捷列
for r in range(3):
    for c in range(9): slot(7 + c * 18, 83 + r * 18)
for c in range(9): slot(7 + c * 18, 141)
# 空進度箭頭（灰）
def arrow(x, y, color):
    d.rectangle((x, y + 6, x + 15, y + 10), fill=color)
    d.polygon([(x + 15, y + 2), (x + 23, y + 8), (x + 15, y + 14)], fill=color)
arrow(79, 35, (120, 120, 120, 255))
# 滿進度箭頭（青綠），存在 (176,0)
arrow(176, 0, (70, 220, 200, 255))
im.save("src/main/resources/assets/limbusego/textures/gui/extractor.png")
```

- [ ] **Step 2: `ExtractorScreen`**

```java
package me.yisang.limbusego.client;

import me.yisang.limbusego.LimbusEGOMod;
import me.yisang.limbusego.extractor.ExtractorScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/** 提取機 GUI：背景圖 + 依進度裁寬的箭頭。沒有按鈕。 */
public class ExtractorScreen extends HandledScreen<ExtractorScreenHandler> {

    private static final Identifier TEXTURE = LimbusEGOMod.id("textures/gui/extractor.png");
    private static final int ARROW_X = 79, ARROW_Y = 35, ARROW_W = 24, ARROW_H = 17;
    private static final int ARROW_FULL_U = 176, ARROW_FULL_V = 0;

    public ExtractorScreen(ExtractorScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
        playerInventoryTitleY = backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        ctx.drawTexture(RenderLayer::getGuiTextured, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);

        int max = handler.getMaxProgress();
        int progress = handler.getProgress();
        if (max > 0 && progress > 0) {
            int w = Math.min(ARROW_W, progress * ARROW_W / max);
            ctx.drawTexture(RenderLayer::getGuiTextured, TEXTURE,
                    x + ARROW_X, y + ARROW_Y, ARROW_FULL_U, ARROW_FULL_V, w, ARROW_H, 256, 256);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
    }
}
```

- [ ] **Step 3: 綁定 Screen**

`LimbusEGOClient.onInitializeClient()` 加：

```java
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(
                me.yisang.limbusego.extractor.ModBlocks.EXTRACTOR_SCREEN_HANDLER, ExtractorScreen::new);
```

（`HandledScreens.register` 由 transitive access widener 開放。）

- [ ] **Step 4: 編譯**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/yisang/limbusego/client/ExtractorScreen.java src/main/java/me/yisang/limbusego/client/LimbusEGOClient.java src/main/resources/assets/limbusego/textures/gui/extractor.png
git commit -m "feat: 提取機 GUI 畫面 / Add the client Extractor screen with a progress arrow"
```

---

### Task 6: 掉落注入 `LootInjection`

**Files:**
- Create: `src/main/java/me/yisang/limbusego/extractor/LootInjection.java`
- Modify: `src/main/java/me/yisang/limbusego/LimbusEGOMod.java`

**Interfaces:**
- Consumes: `ModItems.ENKEPHALIN`、`Vestiges.DARK/FAINT/TWINKLING/BRILLIANT_VESTIGE`。
- Produces: `static void LootInjection.register()`。

- [ ] **Step 1: 寫 `LootInjection`**

```java
package me.yisang.limbusego.extractor;

import me.yisang.limbusego.gift.Vestiges;
import me.yisang.limbusego.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用 {@code LootTableEvents.MODIFY} 動態附加 Enkephalin 與殘影掉落，不覆寫任何原生 JSON。
 *
 * <p>Enkephalin：所有生物（SpawnGroup 非 MISC）25%。殘影四階各自對應一組怪物（spec §3.3）；
 * Tier IV 只從三個 Boss 掉，讓單機進程有明確里程碑。
 */
public final class LootInjection {

    public static final float ENKEPHALIN_CHANCE = 0.25f;
    public static final float DARK_CHANCE = 0.02f;
    public static final float FAINT_CHANCE = 0.05f;
    public static final float TWINKLING_CHANCE = 0.08f;
    public static final float BRILLIANT_CHANCE = 1.00f;

    /** 一般敵對生物 → dark_vestige。 */
    private static final List<EntityType<?>> TIER_I = List.of(
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK, EntityType.DROWNED,
            EntityType.SKELETON, EntityType.STRAY, EntityType.BOGGED, EntityType.SPIDER, EntityType.CAVE_SPIDER,
            EntityType.CREEPER, EntityType.SLIME, EntityType.PHANTOM, EntityType.SILVERFISH, EntityType.WITCH);
    /** 地獄／掠奪者類 → faint_vestige。 */
    private static final List<EntityType<?>> TIER_II = List.of(
            EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.ZOMBIFIED_PIGLIN, EntityType.HOGLIN, EntityType.ZOGLIN,
            EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.GHAST,
            EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER, EntityType.BREEZE);
    /** 終界人、凋零骷髏、喚魔者 → twinkling_vestige。 */
    private static final List<EntityType<?>> TIER_III = List.of(
            EntityType.ENDERMAN, EntityType.WITHER_SKELETON, EntityType.EVOKER);
    /** 三個 Boss → brilliant_vestige。 */
    private static final List<EntityType<?>> TIER_IV = List.of(
            EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.WARDEN);

    private LootInjection() {}

    public static void register() {
        Set<RegistryKey<LootTable>> living = new HashSet<>();
        for (EntityType<?> type : Registries.ENTITY_TYPE) {
            if (type.getSpawnGroup() == SpawnGroup.MISC) continue;
            type.getLootTableKey().ifPresent(living::add);
        }
        Set<RegistryKey<LootTable>> tier1 = keysOf(TIER_I);
        Set<RegistryKey<LootTable>> tier2 = keysOf(TIER_II);
        Set<RegistryKey<LootTable>> tier3 = keysOf(TIER_III);
        Set<RegistryKey<LootTable>> tier4 = keysOf(TIER_IV);

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) return;
            if (living.contains(key)) tableBuilder.pool(chancePool(ModItems.ENKEPHALIN, ENKEPHALIN_CHANCE));
            if (tier1.contains(key)) tableBuilder.pool(chancePool(Vestiges.DARK_VESTIGE, DARK_CHANCE));
            if (tier2.contains(key)) tableBuilder.pool(chancePool(Vestiges.FAINT_VESTIGE, FAINT_CHANCE));
            if (tier3.contains(key)) tableBuilder.pool(chancePool(Vestiges.TWINKLING_VESTIGE, TWINKLING_CHANCE));
            if (tier4.contains(key)) tableBuilder.pool(chancePool(Vestiges.BRILLIANT_VESTIGE, BRILLIANT_CHANCE));
        });
    }

    private static Set<RegistryKey<LootTable>> keysOf(List<EntityType<?>> types) {
        Set<RegistryKey<LootTable>> out = new HashSet<>();
        for (EntityType<?> t : types) t.getLootTableKey().ifPresent(out::add);
        return out;
    }

    private static LootPool.Builder chancePool(Item item, float chance) {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .with(ItemEntry.builder(item));
    }
}
```

- [ ] **Step 2: 初始化**

`LimbusEGOMod.onInitialize()` 在 `me.yisang.limbusego.extractor.ExtractionPools.build();` 之後加：

```java
        me.yisang.limbusego.extractor.LootInjection.register();
```

- [ ] **Step 3: 編譯**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL。若 `EntityType.BOGGED` 或 `BREEZE` 找不到，代表 Yarn 名稱不同——用 `grep -o "public static final .*EntityType<[^>]*> [A-Z_]*;"` 對 `javap` 輸出查正確欄位名，**不要**直接刪掉該項。

- [ ] **Step 4: Commit**

```bash
git add src/main/java/me/yisang/limbusego/extractor/LootInjection.java src/main/java/me/yisang/limbusego/LimbusEGOMod.java
git commit -m "feat: Enkephalin 與殘影掉落注入 / Inject Enkephalin and vestige drops into mob loot tables"
```

---

### Task 7: 遊戲內驗收、README、spec 收尾

**Files:**
- Modify: `README.md`、`README.en.md`
- Modify: `docs/superpowers/specs/2026-08-11-ego-extractor-design.md:4`

- [ ] **Step 1: 啟動開發客戶端**

Run: `./gradlew.bat runClient`（背景）。啟動日誌應出現 `提取機抽獎池：I=… II=… III=… IV=…`，四數加總 **80**（spec §8 第 8 點）。

- [ ] **Step 2: 依 spec §8 逐項驗收**

| # | 操作 | 預期 |
|---|---|---|
| 1 | 工作台照配方合成提取機、放置、右鍵 | GUI 開啟，三槽位與灰色箭頭正常 |
| 2 | 投入 `dark_vestige` + 8 個 Enkephalin（`/give @s limbusego:enkephalin 64`） | 箭頭 5 秒內填滿，產出槽出現一件 Tier I 飾品，殘影 −1、Enkephalin −8 |
| 3 | 不取走產出，再放材料 | 機器停住；取走後自動續跑 |
| 4 | 進度到一半關閉 GUI，數秒後再開 | 進度有前進（離開 GUI 不中斷） |
| 5 | 槽內放東西後破壞方塊 | 全部噴出，方塊本身也掉落 |
| 6 | 殺 30 隻殭屍／牛 | 穩定出 Enkephalin，殭屍偶發 dark_vestige |
| 7 | 存檔退出重進 | 機器內容物與進度保留 |
| 8 | 看啟動日誌 | 四階池大小加總 80 |

任一項不符：回到對應 task 修正，重跑 `./gradlew.bat build`，再驗。

- [ ] **Step 3: README**

`README.md` 的「E.G.O 飾品一覽」章節，**殘影鐵砧升級**那段：

```
**殘影鐵砧升級**：以四階殘影（漆黑／黯淡／閃爍／璀璨 → 階級 1~4）
```

改為

```
**殘影鐵砧升級**：以四階殘影（黯淡／微茫／閃耀／輝煌 → 階級 1~4）
```

並在該段之後、「遊戲內按住 **Shift**」那段之前插入：

```markdown
**E.G.O 提取機**：飾品與殘影的正規取得管道。所有生物 25% 掉落**腦啡肽**（Enkephalin）；一般怪 2% 掉黯淡殘影、地獄／掠奪者類 5% 掉微茫殘影、終界人／凋零骷髏／喚魔者 8% 掉閃耀殘影、終界龍／凋零／監守者必掉輝煌殘影。工作台合成提取機（鐵錠、紅石、石英、鐵砧），投入一個殘影與 8／16／32／64 個腦啡肽，5～20 秒後隨機產出**該階級**的飾品。
```

`README.en.md` 對應段落（"Vestige anvil upgrades" 附近）插入：

```markdown
**E.G.O Extractor**: the in-game source of gifts and vestiges. Every mob has a 25% chance to drop **Enkephalin**; common hostiles drop Dark Vestiges (2%), Nether/illager mobs Faint (5%), Endermen/Wither Skeletons/Evokers Twinkling (8%), and the Ender Dragon/Wither/Warden always drop a Brilliant Vestige. Craft the Extractor (iron, redstone, quartz, anvil), insert one vestige plus 8/16/32/64 Enkephalin, and after 5–20 s it yields a random gift **of that tier**.
```

先用 `grep -n "殘影\|Vestige" README.md README.en.md` 找到確切位置再插。

- [ ] **Step 4: 更新 spec 狀態列**

`docs/superpowers/specs/2026-08-11-ego-extractor-design.md` 第 4 行 `狀態：設計已通過，待寫實作計畫` 改為 `狀態：已實作（計畫：docs/superpowers/plans/2026-09-11-ego-extractor.md）`。

- [ ] **Step 5: Commit**

```bash
git add README.md README.en.md docs/superpowers/specs/2026-08-11-ego-extractor-design.md
git commit -m "docs: 提取機 README 與 spec 標記為已實作 / Document the Extractor and mark its spec implemented"
```
