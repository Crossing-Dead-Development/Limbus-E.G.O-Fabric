# 飾品名稱與描述樣式 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 80 件飾品的名稱依階級上色（I 灰、II 綠、III 藍、IV 金），46 件有風味台詞的 lore 恢復插件的每件專屬顏色。

**Architecture:** 新增 `GiftStyles` 集中飾品的**呈現**資料（階級色、風味行數、風味顏色），`ModGifts` 回歸純註冊。名稱樣式走既有的 `ItemNameMixin`（`Item.getName` 攔截），武器查無時再問 `GiftStyles.styledName`；階級來源是 `GiftRegistry.byId(id).tier()`，與 Shift 底部標籤同源。46 筆描述顏色從 Paper 插件建構子第 4 參數機械式擷取。

**Tech Stack:** Java 21、Minecraft 1.21.4、Yarn `1.21.4+build.8`、Fabric Loader 0.16.9、Fabric API 0.119.4+1.21.4、Accessories 1.2.19-beta、JUnit 5.11.4。

**Spec:** `docs/superpowers/specs/2026-09-09-gift-styling-design.md`

---

## Global Constraints

這些規則適用於**每一個** task，不再逐項重複：

1. **只做呈現層。** 不改飾品機制、數值、tooltip 內容；`WeaponStyles` 與武器名稱樣式**不動**。
2. **階級不另建表**：名稱顏色一律由 `GiftRegistry.byId(id).tier()` 推導，禁止在 `GiftStyles` 裡寫 id → tier 的對照。
3. **不做階級 lore 行**（spec §3.4），不新增任何翻譯鍵、不新增任何文案。
4. **`Rarity.EPIC` 保留**，名稱顏色由 mixin 蓋過。
5. **不要呼叫 `Bootstrap.initialize()`**，測試只碰純 Java 物件與 `Text` / `Style`；涉及 80 件飾品清單時，比照既有做法用 `GiftDescriptionCoverageTest.registered()`（解析 `ModGifts.java` 再反射實例化）。
6. **每個 task 結束都要 commit**，訊息格式沿用 repo 慣例：`<type>: <中文摘要> / <English summary>`，並附上：
   ```
   Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>
   ```
   直接在 `master` 上開發，不開分支。
7. **建置與測試指令**（Windows，repo 根目錄）：
   - 編譯：`./gradlew.bat build -x test`
   - 全部測試：`./gradlew.bat test`
   - 單一測試：`./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftStylesTest"`

## 檔案結構

| 檔案 | 動作 | 職責 |
|---|---|---|
| `src/main/java/me/yisang/limbusego/gift/GiftStyles.java` | 新增 | 飾品呈現資料：`tierColor`、`styledName`、`flavorLines`、`flavorColor` |
| `src/main/java/me/yisang/limbusego/gift/ModGifts.java` | 修改 | 移出 `FLAVOR_LINES`；lore 行數與顏色改查 `GiftStyles` |
| `src/main/java/me/yisang/limbusego/mixin/ItemNameMixin.java` | 修改 | 武器查無時再問 `GiftStyles.styledName` |
| `src/test/java/me/yisang/limbusego/gift/GiftStylesTest.java` | 新增 | 階級色、風味表覆蓋率、顏色合法性 |
| `docs/superpowers/specs/2026-09-09-ego-tooltips-design.md` | 修改 | 修正 §1 那句「插件每件飾品有專屬名稱色」 |

---

### Task 1: `GiftStyles` 呈現資料與測試

**Files:**
- Create: `src/main/java/me/yisang/limbusego/gift/GiftStyles.java`
- Test: `src/test/java/me/yisang/limbusego/gift/GiftStylesTest.java`

**Interfaces:**
- Consumes: `GiftRegistry.byId(String) → BaseGift`（既有）、`BaseGift.tier() → int`（既有）、`GiftDescriptionCoverageTest.registered() → Map<String, BaseGift>`（既有，package-private static）。
- Produces:
  - `static int GiftStyles.tierColor(int tier)` — 1~4 → 灰／綠／藍／金；其他 → `0xAAAAAA`。
  - `static Text GiftStyles.styledName(String id)` — 非飾品回傳 `null`。
  - `static int GiftStyles.flavorLines(String id)` — 無風味台詞回傳 `0`。
  - `static int GiftStyles.flavorColor(String id)` — 查無回傳 `0xAAAAAA`（原版 `Formatting.GRAY`）。
  - `static Set<String> GiftStyles.flavorIds()` — 46 筆風味表的 id（供測試與 `ModGifts`）。

- [ ] **Step 1: 寫失敗測試**

```java
package me.yisang.limbusego.gift;

import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 飾品呈現資料的把關：階級色對齊插件、風味顏色表與風味台詞表一一對應。
 *
 * <p>不碰 Minecraft registry。飾品清單用 {@link GiftDescriptionCoverageTest#registered()}
 * 解析 {@code ModGifts.java} 取得。
 */
class GiftStylesTest {

    /** 與 GiftFlavorTest.WITH_FLAVOR 相同的 46 件；此處重複列出是為了讓兩份表互相把關。 */
    private static final Set<String> WITH_FLAVOR = Set.of(
            "ardent_flower", "ashes_to_ashes", "dust_to_dust", "hot_n_juicy_drumstick",
            "pain_of_stifled_rage", "crystallized_blood", "la_manchaland_all_day_pass", "la_manchaland_standard_pass",
            "mask_of_the_parade", "black_sheet_music", "broken_compass", "cold_illusion",
            "distant_star", "frozen_cries", "rags", "rest",
            "tangled_bones", "dry_to_the_bone_breast", "ebony_brooch", "harestride",
            "moon_in_the_water", "strange_glyph_talisman", "sour_liquor_aroma", "piece_of_crumbled_egg",
            "cask_spirits", "clear_mirror_calm_water", "emerald_elytra", "dreaming_electric_sheep",
            "illusory_hunt", "hardship", "phantom_pain", "tenacity_bolus",
            "the_book_of_vengeance", "plume_of_proof", "spicebush_branch", "trauma_shield",
            "blue_zippo_lighter", "golden_urn", "homeward", "lithograph",
            "oracle", "piece_of_relationship", "flower_mound", "jin_gang_bolus",
            "piece_of_a_torn_summer", "tranquil_lotus_bolus");

    private static final Set<String> TWO_LINE = Set.of("phantom_pain", "spicebush_branch", "flower_mound");

    @Test
    void tierColorsMatchPlugin() {
        assertEquals(0xAAAAAA, GiftStyles.tierColor(1));
        assertEquals(0x55FF55, GiftStyles.tierColor(2));
        assertEquals(0x55AAFF, GiftStyles.tierColor(3));
        assertEquals(0xFFD700, GiftStyles.tierColor(4));
    }

    @Test
    void unknownTierFallsBackToGray() {
        assertEquals(0xAAAAAA, GiftStyles.tierColor(0));
        assertEquals(0xAAAAAA, GiftStyles.tierColor(5));
        assertEquals(0xAAAAAA, GiftStyles.tierColor(-1));
    }

    @Test
    void everyFlavorColorIdIsARegisteredGift() {
        var registered = GiftDescriptionCoverageTest.registered().keySet();
        for (String id : GiftStyles.flavorIds()) {
            assertTrue(registered.contains(id), "風味顏色表含有未註冊的 id：" + id);
        }
    }

    @Test
    void flavorColorTableCoversEveryFlavoredGift() {
        assertEquals(WITH_FLAVOR, GiftStyles.flavorIds(), "風味顏色表的 id 集合必須正好是 46 件有台詞的飾品");
    }

    @Test
    void flavorLinesMatchFlavorTable() {
        for (String id : WITH_FLAVOR) {
            assertEquals(TWO_LINE.contains(id) ? 2 : 1, GiftStyles.flavorLines(id), id + " 的 lore 行數");
        }
        assertEquals(0, GiftStyles.flavorLines("special_contract"), "無台詞的飾品行數應為 0");
        assertEquals(0, GiftStyles.flavorLines("not_a_gift"));
    }

    @Test
    void flavorColorsAreOpaqueRgb() {
        for (String id : GiftStyles.flavorIds()) {
            int rgb = GiftStyles.flavorColor(id);
            assertTrue(rgb >= 0 && rgb <= 0xFFFFFF, id + " 的顏色超出 24 位元：" + Integer.toHexString(rgb));
        }
        assertEquals(0xAAAAAA, GiftStyles.flavorColor("special_contract"), "查無顏色應回退灰色");
    }

    @Test
    void styledNameIsNullForNonGift() {
        // 測試環境沒有註冊任何飾品，GiftRegistry 為空 → 一律 null，mixin 維持原行為
        assertNull(GiftStyles.styledName("solemn_lament_black"));
        assertNull(GiftStyles.styledName("not_a_gift"));
        assertNull(GiftStyles.styledName(null));
    }

    @Test
    void styledNameUsesItemTranslationKeyAndTierColor() {
        var text = GiftStyles.styledName("ardent_flower", 3);
        var content = (TranslatableTextContent) text.getContent();
        assertEquals("item.limbusego.ardent_flower", content.getKey());
        assertEquals(0x55AAFF, text.getStyle().getColor().getRgb());
        assertEquals(Boolean.FALSE, text.getStyle().isItalic(), "名稱不可斜體");
    }
}
```

- [ ] **Step 2: 跑測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftStylesTest"`
Expected: 編譯失敗，`GiftStyles` 不存在。

- [ ] **Step 3: 寫 `GiftStyles`**

46 筆顏色由以下指令從插件原始碼擷取（已執行，結果貼在下方；46 筆、40 種相異，與 spec §2 一致）：

```
python: 對 C:\Users\User\IdeaProjects\Limbus-E.G.O\src\main\java\me\yisang\limbusego\gift\gifts\*.java
       抓 super(plugin, "<id>", "<name>", "&#RRGGBB", ...) 的第 4 參數
```

```java
package me.yisang.limbusego.gift;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.Map;
import java.util.Set;

/**
 * 飾品的呈現資料集中對照：名稱樣式、風味台詞行數、風味台詞顏色。
 * 與武器側「{@code ModItems} 註冊、{@code WeaponStyles} 管樣式」的分工一致。
 *
 * <p>名稱樣式**不能**經 {@code Item.Settings.component(ITEM_NAME, …)} 設定（1.21.4 會被無條件蓋掉），
 * 改由 {@code ItemNameMixin} 攔截 {@code Item.getName(ItemStack)} 回傳 {@link #styledName(String)}。
 *
 * <p>階級不另建表：一律讀 {@link GiftRegistry#byId(String)} 的 {@link BaseGift#tier()}，
 * 與 Shift 底部標籤同一來源。
 */
public final class GiftStyles {

    private GiftStyles() {}

    /** 插件 {@code GiftsModule.TIER_COLORS}：I 灰、II 綠、III 藍、IV 金。索引 0 為超出範圍時的回退。 */
    private static final int[] TIER_COLORS = {0xAAAAAA, 0xAAAAAA, 0x55FF55, 0x55AAFF, 0xFFD700};

    /** 查無風味顏色時的回退，即原本寫死的 {@code Formatting.GRAY}。 */
    private static final int DEFAULT_FLAVOR_COLOR = 0xAAAAAA;

    /**
     * 有風味台詞的飾品 → lore 行數。來源為 Paper 插件 {@code BaseAccessory} 的 6 參數建構子；
     * 其餘 34 件在插件中本來就沒有台詞，刻意不補寫。
     */
    private static final Map<String, Integer> FLAVOR_LINES = Map.ofEntries(
        Map.entry("ardent_flower", 1),
        Map.entry("ashes_to_ashes", 1),
        Map.entry("black_sheet_music", 1),
        Map.entry("blue_zippo_lighter", 1),
        Map.entry("broken_compass", 1),
        Map.entry("cask_spirits", 1),
        Map.entry("clear_mirror_calm_water", 1),
        Map.entry("cold_illusion", 1),
        Map.entry("crystallized_blood", 1),
        Map.entry("distant_star", 1),
        Map.entry("dreaming_electric_sheep", 1),
        Map.entry("dry_to_the_bone_breast", 1),
        Map.entry("dust_to_dust", 1),
        Map.entry("ebony_brooch", 1),
        Map.entry("emerald_elytra", 1),
        Map.entry("flower_mound", 2),
        Map.entry("frozen_cries", 1),
        Map.entry("golden_urn", 1),
        Map.entry("hardship", 1),
        Map.entry("harestride", 1),
        Map.entry("homeward", 1),
        Map.entry("hot_n_juicy_drumstick", 1),
        Map.entry("illusory_hunt", 1),
        Map.entry("jin_gang_bolus", 1),
        Map.entry("la_manchaland_all_day_pass", 1),
        Map.entry("la_manchaland_standard_pass", 1),
        Map.entry("lithograph", 1),
        Map.entry("mask_of_the_parade", 1),
        Map.entry("moon_in_the_water", 1),
        Map.entry("oracle", 1),
        Map.entry("pain_of_stifled_rage", 1),
        Map.entry("phantom_pain", 2),
        Map.entry("piece_of_a_torn_summer", 1),
        Map.entry("piece_of_crumbled_egg", 1),
        Map.entry("piece_of_relationship", 1),
        Map.entry("plume_of_proof", 1),
        Map.entry("rags", 1),
        Map.entry("rest", 1),
        Map.entry("sour_liquor_aroma", 1),
        Map.entry("spicebush_branch", 2),
        Map.entry("strange_glyph_talisman", 1),
        Map.entry("tangled_bones", 1),
        Map.entry("tenacity_bolus", 1),
        Map.entry("the_book_of_vengeance", 1),
        Map.entry("tranquil_lotus_bolus", 1),
        Map.entry("trauma_shield", 1)
    );

    /**
     * 風味台詞顏色，逐筆取自 Paper 插件各飾品建構子的第 4 參數 {@code descColor}（{@code &#RRGGBB}）。
     * 46 筆、40 種相異顏色。
     */
    private static final Map<String, Integer> FLAVOR_COLORS = Map.ofEntries(
        Map.entry("ardent_flower", 0xFF7000),
        Map.entry("ashes_to_ashes", 0x9A9A9A),
        Map.entry("black_sheet_music", 0xFFFFFF),
        Map.entry("blue_zippo_lighter", 0x44D8DB),
        Map.entry("broken_compass", 0x0772AB),
        Map.entry("cask_spirits", 0xA8BE78),
        Map.entry("clear_mirror_calm_water", 0x56BBDB),
        Map.entry("cold_illusion", 0x33CF4F),
        Map.entry("crystallized_blood", 0xFF0000),
        Map.entry("distant_star", 0x00DAFF),
        Map.entry("dreaming_electric_sheep", 0x9863E7),
        Map.entry("dry_to_the_bone_breast", 0xD77F00),
        Map.entry("dust_to_dust", 0x9A9A9A),
        Map.entry("ebony_brooch", 0x5B1365),
        Map.entry("emerald_elytra", 0x16B569),
        Map.entry("flower_mound", 0xF1B1B1),
        Map.entry("frozen_cries", 0x4498DB),
        Map.entry("golden_urn", 0xDA8F24),
        Map.entry("hardship", 0x9E9E41),
        Map.entry("harestride", 0xAAD179),
        Map.entry("homeward", 0x8EC58E),
        Map.entry("hot_n_juicy_drumstick", 0xD77F00),
        Map.entry("illusory_hunt", 0xC6CDEF),
        Map.entry("jin_gang_bolus", 0x9F8B07),
        Map.entry("la_manchaland_all_day_pass", 0xFCD05C),
        Map.entry("la_manchaland_standard_pass", 0xFCD05C),
        Map.entry("lithograph", 0x169876),
        Map.entry("mask_of_the_parade", 0x9928BB),
        Map.entry("moon_in_the_water", 0x8EC5F0),
        Map.entry("oracle", 0xB3F2F9),
        Map.entry("pain_of_stifled_rage", 0xDA3D24),
        Map.entry("phantom_pain", 0x656565),
        Map.entry("piece_of_a_torn_summer", 0x5FE2C5),
        Map.entry("piece_of_crumbled_egg", 0x33CF4F),
        Map.entry("piece_of_relationship", 0x444444),
        Map.entry("plume_of_proof", 0x4498DB),
        Map.entry("rags", 0x755E42),
        Map.entry("rest", 0xFFFFFF),
        Map.entry("sour_liquor_aroma", 0x00BC6B),
        Map.entry("spicebush_branch", 0xFFF29B),
        Map.entry("strange_glyph_talisman", 0x8E5608),
        Map.entry("tangled_bones", 0x969696),
        Map.entry("tenacity_bolus", 0x0C440C),
        Map.entry("the_book_of_vengeance", 0xB900FF),
        Map.entry("tranquil_lotus_bolus", 0xE07F9A),
        Map.entry("trauma_shield", 0xCBCBCB)
    );

    /** 階級 1~4 的名稱顏色；超出範圍回退灰色（比照插件 {@code tierColor} 的預設）。 */
    public static int tierColor(int tier) {
        return tier >= 1 && tier < TIER_COLORS.length ? TIER_COLORS[tier] : TIER_COLORS[0];
    }

    /** 飾品 id 的樣式化顯示名稱（依階級上色、無斜體）；非飾品回傳 {@code null}。 */
    public static Text styledName(String id) {
        if (id == null) return null;
        BaseGift gift = GiftRegistry.byId(id);
        if (gift == null) return null;
        return styledName(id, gift.tier());
    }

    /** 給定階級的樣式化名稱；拆出來讓測試不必填充 {@link GiftRegistry}。 */
    static Text styledName(String id, int tier) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(tierColor(tier))).withItalic(false);
        return Text.translatable("item.limbusego." + id).setStyle(style);
    }

    /** 風味台詞行數；沒有台詞的飾品回傳 0。 */
    public static int flavorLines(String id) {
        return FLAVOR_LINES.getOrDefault(id, 0);
    }

    /** 風味台詞顏色；查無回退灰色（即改動前的外觀）。 */
    public static int flavorColor(String id) {
        return FLAVOR_COLORS.getOrDefault(id, DEFAULT_FLAVOR_COLOR);
    }

    /** 風味顏色表涵蓋的 id，供測試與註冊端核對。 */
    public static Set<String> flavorIds() {
        return FLAVOR_COLORS.keySet();
    }
}
```

- [ ] **Step 4: 跑測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftStylesTest"`
Expected: 8 tests PASS。

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/GiftStyles.java src/test/java/me/yisang/limbusego/gift/GiftStylesTest.java
git commit -m "feat: 飾品呈現資料 GiftStyles / Add GiftStyles with tier colours and 46 ported flavor colours"
```

---

### Task 2: `ModGifts` 改查 `GiftStyles`

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/ModGifts.java`

**Interfaces:**
- Consumes: `GiftStyles.flavorLines(String)`、`GiftStyles.flavorColor(String)`（Task 1）。
- Produces: 無新介面；`ModGifts.register()` / `ordered()` / `byId()` 簽名不變。

- [ ] **Step 1: 刪除 `ModGifts.FLAVOR_LINES`**

整段刪除欄位（含其 javadoc 的三行註解），從

```java
    /**
     * 有風味台詞的飾品 → lore 行數。來源為 Paper 插件 {@code BaseAccessory} 的 6 參數建構子；
```

到該 `Map.ofEntries(...)` 的 `);` 為止。

- [ ] **Step 2: `reg()` 改查 `GiftStyles`**

把

```java
        int flavorLines = FLAVOR_LINES.getOrDefault(name, 0);
        if (flavorLines > 0) {
            List<Text> lore = new ArrayList<>(flavorLines);
            for (int i = 0; i < flavorLines; i++) {
                lore.add(Text.translatable("item.limbusego." + name + ".lore." + i)
                        .setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false)));
            }
            settings.component(DataComponentTypes.LORE, new LoreComponent(lore));
        }
```

改成

```java
        int flavorLines = GiftStyles.flavorLines(name);
        if (flavorLines > 0) {
            Style loreStyle = Style.EMPTY
                    .withColor(TextColor.fromRgb(GiftStyles.flavorColor(name)))
                    .withItalic(false);
            List<Text> lore = new ArrayList<>(flavorLines);
            for (int i = 0; i < flavorLines; i++) {
                lore.add(Text.translatable("item.limbusego." + name + ".lore." + i).setStyle(loreStyle));
            }
            settings.component(DataComponentTypes.LORE, new LoreComponent(lore));
        }
```

- [ ] **Step 3: 整理 import**

- 刪除 `import net.minecraft.util.Formatting;`
- 新增 `import net.minecraft.text.TextColor;`
- `java.util.Map` 與 `java.util.LinkedHashMap` 仍被 `BY_ID` 使用，保留。

- [ ] **Step 4: 確認沒有殘留**

Run: `grep -n "FLAVOR_LINES\|Formatting" src/main/java/me/yisang/limbusego/gift/ModGifts.java`
Expected: 無輸出。

- [ ] **Step 5: 編譯並跑全部測試**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL（`GiftFlavorTest`、`GiftDescriptionCoverageTest` 解析的是 `reg(...)` 呼叫，不受影響）。

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/ModGifts.java
git commit -m "refactor: 飾品 lore 顏色改查 GiftStyles / Look up gift lore lines and colours from GiftStyles"
```

---

### Task 3: `ItemNameMixin` 加入飾品名稱樣式

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/mixin/ItemNameMixin.java`

**Interfaces:**
- Consumes: `WeaponStyles.styledName(String)`（既有）、`GiftStyles.styledName(String)`（Task 1）。

- [ ] **Step 1: 改為依序詢問**

把

```java
        Text styled = WeaponStyles.styledName(itemId.getPath());
        if (styled != null) {
            cir.setReturnValue(styled);
        }
```

改成

```java
        String path = itemId.getPath();
        Text styled = WeaponStyles.styledName(path);
        if (styled == null) styled = GiftStyles.styledName(path);
        if (styled != null) {
            cir.setReturnValue(styled);
        }
```

新增 `import me.yisang.limbusego.gift.GiftStyles;`。

類別 javadoc 改為：

```java
/**
 * 為 limbusego 武器與飾品提供樣式化顯示名稱（顏色／粗體）。
 * 1.21.4 的 {@code Item.Settings} 會用翻譯鍵預設無樣式 item_name 蓋掉手動設的 ITEM_NAME 元件，
 * 因此改在此攔截 {@code Item.getName(ItemStack)}：先問 {@link WeaponStyles#styledName(String)}，
 * 查無再問 {@link GiftStyles#styledName(String)}（依階級上色）。兩者皆 null 維持原行為。
 */
```

方法名 `limbusego$styledWeaponName` 改為 `limbusego$styledName`。

- [ ] **Step 2: 編譯並跑全部測試**

Run: `./gradlew.bat build`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/me/yisang/limbusego/mixin/ItemNameMixin.java
git commit -m "feat: 飾品名稱依階級上色 / Colour gift names by tier via the item-name mixin"
```

---

### Task 4: 遊戲內驗收與文件收尾

**Files:**
- Modify: `docs/superpowers/specs/2026-09-09-ego-tooltips-design.md:22`
- Modify: `docs/superpowers/specs/2026-09-09-gift-styling-design.md:4`

- [ ] **Step 1: 啟動開發客戶端**

Run: `./gradlew.bat runClient`（背景執行，載入約 1–3 分鐘）。進入單人世界，開作弊。

- [ ] **Step 2: 依 spec §7 逐項驗收**

| # | 操作 | 預期 |
|---|---|---|
| 1 | 開創造頁籤翻飾品 | 名稱依階級呈灰／綠／藍／金四色 |
| 2 | 看塵歸塵（ashes_to_ashes）、安息（rest）、烈焰之花（ardent_flower） | 台詞分別為灰 `#9A9A9A`、白 `#FFFFFF`、橙 `#FF7000` |
| 3 | 看 special_contract 等無台詞飾品 | 只有名稱，無 lore 行 |
| 4 | 看武器 | 莊嚴哀悼黑仍為黑粗體、薄暝仍為金 |
| 5 | 看原版物品（鑽石劍） | 名稱不受影響 |
| 6 | 對任一飾品按住 Shift | 底部「階級 N」標籤與名稱顏色表達的階級一致 |

任一項不符：回到對應 task 修正，重跑 `./gradlew.bat build`，再驗。

- [ ] **Step 3: 修正 tooltip spec 的錯誤描述**

`docs/superpowers/specs/2026-09-09-ego-tooltips-design.md` 第 22 行

```
- 飾品名稱顏色移植（插件每件飾品有專屬顏色，Fabric 版目前統一 `Rarity.EPIC`。屬於呈現層債，但與 tooltip 無關，另開一輪）
```

改為

```
- 飾品名稱與描述顏色（插件名稱一律白色、每件有專屬**描述**色；Fabric 版當時統一 `Rarity.EPIC` 與灰色描述。屬於呈現層債，已由 `2026-09-09-gift-styling-design.md` 處理）
```

- [ ] **Step 4: 更新 spec 狀態列**

把 `docs/superpowers/specs/2026-09-09-gift-styling-design.md` 第 4 行 `狀態：設計已通過，待寫實作計畫` 改為 `狀態：已實作（計畫：docs/superpowers/plans/2026-09-11-gift-styling.md）`。

- [ ] **Step 5: Commit**

```bash
git add docs/superpowers/specs/2026-09-09-ego-tooltips-design.md docs/superpowers/specs/2026-09-09-gift-styling-design.md
git commit -m "docs: 飾品樣式 spec 標記為已實作並修正 tooltip spec / Mark gift styling spec implemented, correct tooltip spec"
```
