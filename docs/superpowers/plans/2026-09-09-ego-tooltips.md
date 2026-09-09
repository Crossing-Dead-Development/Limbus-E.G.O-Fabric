# E.G.O Tooltip Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 讓 14 件武器／彈藥與 80 件 E.G.O 飾品在遊戲中按住 Shift 就能看到分段的具體機制說明，飾品數值隨升級等級變動。

**Architecture:** 一個客戶端 `ItemTooltipCallback` listener 統一插行；武器說明來自 `WeaponTooltips` 集中表，飾品說明來自各 `BaseGift` 子類覆寫的 `describe(int level)`。所有文字走翻譯鍵，中英文同步，由單元測試把關覆蓋率。

**Tech Stack:** Java 21、Minecraft 1.21.4、Yarn `1.21.4+build.8`、Fabric Loader 0.16.9、Fabric API 0.119.4+1.21.4（`fabric-item-api-v1` 11.4.0）、Accessories 1.2.19-beta、JUnit 5.11.4。

**Spec:** `docs/superpowers/specs/2026-09-09-ego-tooltips-design.md`

---

## Global Constraints

這些規則適用於**每一個** task，不再逐項重複：

1. **只做呈現層。** 不得修改任何玩法邏輯或數值。若發現 tooltip 與程式邏輯不符，**改 tooltip 去符合程式**，不要改程式；並在 commit message 註明該差異。
2. **所有玩家可見文字都必須是翻譯鍵**，禁止硬編中文或英文字串。鍵一律同時寫進 `src/main/resources/assets/limbusego/lang/zh_tw.json` 與 `en_us.json`，兩檔鍵集必須完全相同。
3. **翻譯鍵命名**：
   - 武器／飾品說明：`tooltip.limbusego.<item_id>.<section>` 與 `tooltip.limbusego.<item_id>.<section>.<n>`
   - 共用 UI：`tooltip.limbusego.hint`、`tooltip.limbusego.tag.ego`、`tooltip.limbusego.tag.gift`
   - 屬性名稱：`status.limbusego.<lowercase_enum_name>`
   - 飾品風味台詞：`item.limbusego.<gift_id>.lore.0`
4. **不要呼叫 `Bootstrap.initialize()`**，也不要在測試中使用 `GiftRegistry` 或建立 `ItemStack`。它會凍結 registry 並讓飾品註冊失敗。測試只碰純 Java 物件、`Text.translatable()` 與 `Style`。
5. **JSON 檔尾不留逗號**，鍵依現有檔案的分組順序插入（武器區、飾品區、tooltip 區），不要重排既有內容。
6. **每個 task 結束都要 commit**，訊息格式沿用 repo 慣例：`<type>: <中文摘要> / <English summary>`，並附上：
   ```
   Co-Authored-By: Claude Opus 5 <noreply@anthropic.com>
   ```
7. **建置與測試指令**（Windows，repo 根目錄）：
   - 編譯：`./gradlew.bat build -x test`
   - 全部測試：`./gradlew.bat test`
   - 單一測試：`./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`

### 飾品說明轉換程序（Task 6–14 共用）

每件飾品的機制說明**已經存在**於 `src/main/resources/assets/limbusego/lang/zh_tw.json` 的 `item.limbusego.<id>.desc`（英文在 `en_us.json`）。工作是把那一行密集文字拆成結構化的 `describe(int level)`，而不是重新發明內容。

轉換規則：

1. 用全形直線 `｜` 切開 `.desc`，每一段成為一個 section。
2. 每段的冒號 `：` 之前是**觸發條件**（section 標題），之後是**效果**（body 行）。若某段沒有冒號，整段當成 body，掛在前一個 section 之下。
3. 逐段對照該飾品的 `.java` 原始碼確認數值正確。**以程式碼為準**，`.desc` 若與程式不符，以程式為準並在 commit 訊息註明。
4. 若程式碼中該數值有經過 `multiplier(self)` 或 `applyScaled(...)`，`describe(int level)` 必須用 `GiftUpgradeLogic.multiplier(level)` 算出對應值，不可寫死。
5. 屬性名稱一律用 `TooltipFormat.status(StatusEffect.XXX)`，不要在翻譯字串裡直接寫「燒傷」。
6. 完成一組後，把該組 id 加進 `GiftDescriptionCoverageTest` 的白名單（見 Task 5）。

worked example（`ashes_to_ashes`，`.desc` 為 `攻擊燒傷中目標：疊加燒傷 2·1`）：

```java
@Override
public List<Text> describe(int level) {
    return List.of(
        TooltipFormat.section("tooltip.limbusego.ashes_to_ashes.attack"),
        TooltipFormat.body("tooltip.limbusego.ashes_to_ashes.attack.stack",
                TooltipFormat.status(StatusEffect.BURN),
                TooltipFormat.potency(2, 1)));
}
```

```json
"tooltip.limbusego.ashes_to_ashes.attack": "攻擊燒傷中的目標",
"tooltip.limbusego.ashes_to_ashes.attack.stack": "疊加 %s %s"
```

```json
"tooltip.limbusego.ashes_to_ashes.attack": "On hit vs a burning target",
"tooltip.limbusego.ashes_to_ashes.attack.stack": "Stack %s %s"
```

---

## File Structure

| 檔案 | 建立／修改 | 職責 |
|---|---|---|
| `src/main/java/me/yisang/limbusego/status/StatusEffect.java` | 修改 | 加 `translationKey()` |
| `src/main/java/me/yisang/limbusego/client/TooltipFormat.java` | 建立 | 段落／內文／提示列／屬性／威力次數的樣式包裝 |
| `src/main/java/me/yisang/limbusego/client/EgoTooltipHandler.java` | 建立 | 註冊 `ItemTooltipCallback`，判斷 Shift，插行 |
| `src/main/java/me/yisang/limbusego/client/LimbusEGOClient.java` | 修改 | 呼叫 `EgoTooltipHandler.register()` |
| `src/main/java/me/yisang/limbusego/item/WeaponTooltips.java` | 建立 | id → 武器說明行集中表 |
| `src/main/java/me/yisang/limbusego/item/WeaponStyles.java` | 修改 | 移除天退／薄暝的機制 lore 顏色項 |
| `src/main/java/me/yisang/limbusego/gift/BaseGift.java` | 修改 | 加 `describe(ItemStack)` / `describe(int)` |
| `src/main/java/me/yisang/limbusego/gift/ModGifts.java` | 修改 | `LORE` 由 `.desc` 改掛 `.lore.0` |
| `src/main/java/me/yisang/limbusego/gift/gifts/*.java` | 修改 ×80 | 各自覆寫 `describe(int)` |
| `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json` | 修改 | 新增 tooltip／status／飾品風味鍵，移除 `.desc` |
| `src/test/java/me/yisang/limbusego/client/TooltipFormatTest.java` | 建立 | 樣式與鍵格式 |
| `src/test/java/me/yisang/limbusego/LangParityTest.java` | 建立 | 中英文鍵集一致 |
| `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java` | 建立 | 80 件皆有說明、鍵皆存在 |
| `src/test/java/me/yisang/limbusego/gift/GiftDescriptionScalingTest.java` | 建立 | 升級縮放數值正確 |
| `src/test/java/me/yisang/limbusego/item/WeaponTooltipsTest.java` | 建立 | 14 件武器皆有說明、鍵皆存在 |
| `src/test/java/me/yisang/limbusego/LangKeys.java` | 建立 | 測試共用：讀取 lang JSON 的鍵集 |

---

### Task 1: 屬性名稱翻譯鍵

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/status/StatusEffect.java`
- Modify: `src/main/resources/assets/limbusego/lang/zh_tw.json`
- Modify: `src/main/resources/assets/limbusego/lang/en_us.json`
- Test: `src/test/java/me/yisang/limbusego/status/StatusEffectKeyTest.java`

**Interfaces:**
- Produces: `StatusEffect.translationKey()` → `String`，格式 `status.limbusego.<lowercase enum name>`。後續所有 task 用它取得屬性名稱翻譯鍵。

- [ ] **Step 1: 寫失敗測試**

建立 `src/test/java/me/yisang/limbusego/status/StatusEffectKeyTest.java`：

```java
package me.yisang.limbusego.status;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusEffectKeyTest {

    @Test
    void keyFollowsConvention() {
        assertEquals("status.limbusego.burn", StatusEffect.BURN.translationKey());
        assertEquals("status.limbusego.poise", StatusEffect.POISE.translationKey());
        assertEquals("status.limbusego.charge", StatusEffect.CHARGE.translationKey());
    }

    @Test
    void everyStatusHasDistinctKey() {
        long distinct = java.util.Arrays.stream(StatusEffect.values())
                .map(StatusEffect::translationKey)
                .distinct()
                .count();
        assertEquals(StatusEffect.values().length, distinct);
    }
}
```

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.status.StatusEffectKeyTest"`
Expected: FAIL，編譯錯誤 `cannot find symbol: method translationKey()`

- [ ] **Step 3: 實作**

在 `StatusEffect.java` 的建構子之後加入（保留現有 `zh`、`color` 欄位不動）：

```java
    /** tooltip 用的翻譯鍵，例：status.limbusego.burn。 */
    public String translationKey() {
        return "status.limbusego." + name().toLowerCase(java.util.Locale.ROOT);
    }
```

- [ ] **Step 4: 執行測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.status.StatusEffectKeyTest"`
Expected: PASS

- [ ] **Step 5: 加入 lang 條目**

在兩個 lang 檔的最後一個既有鍵之後、結尾 `}` 之前插入（記得替前一行補逗號）。

`zh_tw.json`：

```json
  "status.limbusego.bleed": "流血",
  "status.limbusego.burn": "燒傷",
  "status.limbusego.fragile": "易損",
  "status.limbusego.power": "強壯",
  "status.limbusego.sinking": "沉淪",
  "status.limbusego.rupture": "破裂",
  "status.limbusego.tremor": "震顫",
  "status.limbusego.protection": "守護",
  "status.limbusego.haste": "迅捷",
  "status.limbusego.bind": "束縛",
  "status.limbusego.poise": "呼吸法",
  "status.limbusego.charge": "充能"
```

`en_us.json`：

```json
  "status.limbusego.bleed": "Bleed",
  "status.limbusego.burn": "Burn",
  "status.limbusego.fragile": "Fragile",
  "status.limbusego.power": "Power",
  "status.limbusego.sinking": "Sinking",
  "status.limbusego.rupture": "Rupture",
  "status.limbusego.tremor": "Tremor",
  "status.limbusego.protection": "Protection",
  "status.limbusego.haste": "Haste",
  "status.limbusego.bind": "Bind",
  "status.limbusego.poise": "Poise",
  "status.limbusego.charge": "Charge"
```

- [ ] **Step 6: 確認 JSON 合法**

Run: `python -c "import json;[json.load(open(p,encoding='utf-8')) for p in ['src/main/resources/assets/limbusego/lang/zh_tw.json','src/main/resources/assets/limbusego/lang/en_us.json']];print('ok')"`
Expected: 印出 `ok`

- [ ] **Step 7: Commit**

```bash
git add src/main/java/me/yisang/limbusego/status/StatusEffect.java src/test/java/me/yisang/limbusego/status/StatusEffectKeyTest.java src/main/resources/assets/limbusego/lang/
git commit -m "feat: 12 屬性名稱翻譯鍵 / Add translation keys for the 12 statuses"
```

---

### Task 2: lang 鍵測試工具與中英文對等測試

**Files:**
- Create: `src/test/java/me/yisang/limbusego/LangKeys.java`
- Create: `src/test/java/me/yisang/limbusego/LangParityTest.java`

**Interfaces:**
- Consumes: Task 1 寫入的 `status.limbusego.*` 鍵。
- Produces: `LangKeys.zhTw()` → `Set<String>`、`LangKeys.enUs()` → `Set<String>`、`LangKeys.assertKeyExists(String key)`。後續所有測試用它驗證翻譯鍵存在。

- [ ] **Step 1: 寫失敗測試**

建立 `src/test/java/me/yisang/limbusego/LangParityTest.java`：

```java
package me.yisang.limbusego;

import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class LangParityTest {

    @Test
    void bothLanguagesHaveIdenticalKeySets() {
        var zh = LangKeys.zhTw();
        var en = LangKeys.enUs();

        var missingInEn = new TreeSet<>(zh);
        missingInEn.removeAll(en);
        var missingInZh = new TreeSet<>(en);
        missingInZh.removeAll(zh);

        assertTrue(missingInEn.isEmpty(), "en_us.json 缺少：" + missingInEn);
        assertTrue(missingInZh.isEmpty(), "zh_tw.json 缺少：" + missingInZh);
    }

    @Test
    void statusKeysPresent() {
        for (var s : me.yisang.limbusego.status.StatusEffect.values()) {
            LangKeys.assertKeyExists(s.translationKey());
        }
    }
}
```

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.LangParityTest"`
Expected: FAIL，編譯錯誤 `cannot find symbol: class LangKeys`

- [ ] **Step 3: 實作工具類**

建立 `src/test/java/me/yisang/limbusego/LangKeys.java`。不使用 JSON 函式庫（避免依賴問題），用正規式抽鍵——lang 檔是機器維護的扁平字串對映，這樣就夠：

```java
package me.yisang.limbusego;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** 測試共用：讀取 lang JSON 的鍵集。不載入 Minecraft。 */
public final class LangKeys {

    private static final Path DIR = Path.of("src/main/resources/assets/limbusego/lang");
    private static final Pattern KEY = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:");

    private static Set<String> zh;
    private static Set<String> en;

    private LangKeys() {}

    public static synchronized Set<String> zhTw() {
        if (zh == null) zh = read("zh_tw.json");
        return zh;
    }

    public static synchronized Set<String> enUs() {
        if (en == null) en = read("en_us.json");
        return en;
    }

    /** 斷言某個翻譯鍵在中英文 lang 檔都存在。 */
    public static void assertKeyExists(String key) {
        assertTrue(zhTw().contains(key), "zh_tw.json 缺少翻譯鍵：" + key);
        assertTrue(enUs().contains(key), "en_us.json 缺少翻譯鍵：" + key);
    }

    private static Set<String> read(String file) {
        try {
            Set<String> keys = new LinkedHashSet<>();
            for (String line : Files.readAllLines(DIR.resolve(file), StandardCharsets.UTF_8)) {
                Matcher m = KEY.matcher(line);
                if (m.find()) keys.add(m.group(1));
            }
            if (keys.isEmpty()) throw new IllegalStateException("讀不到任何翻譯鍵：" + file);
            return keys;
        } catch (IOException e) {
            throw new IllegalStateException("讀取 lang 檔失敗：" + file, e);
        }
    }
}
```

- [ ] **Step 4: 執行測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.LangParityTest"`
Expected: PASS。若 FAIL 且訊息指出既有鍵不對等，那是本 task 之前就存在的缺漏——補齊它再繼續。

- [ ] **Step 5: 確認工作目錄假設成立**

`LangKeys` 用相對路徑讀檔，依賴 Gradle test 的工作目錄是專案根目錄。

Run: `./gradlew.bat test --tests "me.yisang.limbusego.LangParityTest" -i` 並確認沒有 `讀不到任何翻譯鍵` 例外。
若失敗，在 `build.gradle` 的 `test { }` 區塊加入 `workingDir = rootProject.projectDir` 後重跑。

- [ ] **Step 6: Commit**

```bash
git add src/test/java/me/yisang/limbusego/LangKeys.java src/test/java/me/yisang/limbusego/LangParityTest.java
git commit -m "test: lang 鍵集對等檢查 / Add lang key parity test"
```

---

### Task 3: TooltipFormat 樣式工具

**Files:**
- Create: `src/main/java/me/yisang/limbusego/client/TooltipFormat.java`
- Test: `src/test/java/me/yisang/limbusego/client/TooltipFormatTest.java`

**Interfaces:**
- Consumes: `StatusEffect.translationKey()`（Task 1）。
- Produces:
  - `TooltipFormat.section(String key, Object... args)` → `Text`：金色段落標題，前綴 `▸ `
  - `TooltipFormat.body(String key, Object... args)` → `Text`：灰色內文，前綴兩個半形空格
  - `TooltipFormat.hint()` → `Text`：深灰斜體提示列，鍵 `tooltip.limbusego.hint`
  - `TooltipFormat.egoTag()` → `Text`：武器底部標籤，鍵 `tooltip.limbusego.tag.ego`
  - `TooltipFormat.giftTag(int tier)` → `Text`：飾品底部標籤，鍵 `tooltip.limbusego.tag.gift`，參數為羅馬數字 I~IV
  - `TooltipFormat.status(StatusEffect effect)` → `Text`：套用該屬性顏色的名稱
  - `TooltipFormat.potency(int potency, int count)` → `Text`：威力／次數，鍵 `tooltip.limbusego.potency`

- [ ] **Step 1: 寫失敗測試**

建立 `src/test/java/me/yisang/limbusego/client/TooltipFormatTest.java`：

```java
package me.yisang.limbusego.client;

import me.yisang.limbusego.LangKeys;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TooltipFormatTest {

    @Test
    void sectionIsGoldAndPrefixed() {
        var text = TooltipFormat.section("tooltip.limbusego.hint");
        assertEquals(Formatting.GOLD.getColorValue().intValue(), text.getStyle().getColor().getRgb());
        assertTrue(text.getString().startsWith("\u25B8 "), "段落標題需以 ▸ 開頭，實得：" + text.getString());
    }

    @Test
    void bodyIsGrayAndIndented() {
        var text = TooltipFormat.body("tooltip.limbusego.hint");
        assertEquals(Formatting.GRAY.getColorValue().intValue(), text.getStyle().getColor().getRgb());
        assertTrue(text.getString().startsWith("  "), "內文需縮排兩格，實得：" + text.getString());
    }

    @Test
    void bodyIsNeverItalic() {
        // Minecraft 對 LORE 預設套斜體；我們的行必須明確關掉，否則版面不一致
        assertEquals(Boolean.FALSE, TooltipFormat.body("tooltip.limbusego.hint").getStyle().isItalic());
        assertEquals(Boolean.FALSE, TooltipFormat.section("tooltip.limbusego.hint").getStyle().isItalic());
    }

    @Test
    void statusUsesItsOwnTranslationKeyAndColor() {
        var burn = TooltipFormat.status(StatusEffect.BURN);
        var content = (TranslatableTextContent) burn.getContent();
        assertEquals("status.limbusego.burn", content.getKey());
        assertNotNull(burn.getStyle().getColor(), "屬性名稱必須有顏色");
    }

    @Test
    void giftTagRendersRomanTier() {
        assertTrue(TooltipFormat.giftTag(1).getString().contains("I"));
        assertTrue(TooltipFormat.giftTag(4).getString().contains("IV"));
    }

    @Test
    void sharedKeysExistInBothLanguages() {
        LangKeys.assertKeyExists("tooltip.limbusego.hint");
        LangKeys.assertKeyExists("tooltip.limbusego.tag.ego");
        LangKeys.assertKeyExists("tooltip.limbusego.tag.gift");
        LangKeys.assertKeyExists("tooltip.limbusego.potency");
    }
}
```

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.client.TooltipFormatTest"`
Expected: FAIL，編譯錯誤 `cannot find symbol: class TooltipFormat`

- [ ] **Step 3: 實作**

建立 `src/main/java/me/yisang/limbusego/client/TooltipFormat.java`：

```java
package me.yisang.limbusego.client;

import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

/**
 * tooltip 行的樣式包裝。集中在此，避免 80 個 describe() 各自拼樣式。
 *
 * <p>注意：本類別的方法只建構 {@link Text}，不接觸 Minecraft registry，
 * 因此可在單元測試中直接使用，無須 Bootstrap。
 */
public final class TooltipFormat {

    private static final String[] ROMAN = {"", "I", "II", "III", "IV"};

    private TooltipFormat() {}

    /** 金色段落標題，前綴 ▸。 */
    public static Text section(String key, Object... args) {
        return prefixed("▸ ", key, Formatting.GOLD.getColorValue(), args);
    }

    /** 灰色內文，縮排兩格。 */
    public static Text body(String key, Object... args) {
        return prefixed("  ", key, Formatting.GRAY.getColorValue(), args);
    }

    /** 收合狀態的提示列（深灰斜體）。 */
    public static Text hint() {
        return Text.translatable("tooltip.limbusego.hint")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Formatting.DARK_GRAY.getColorValue())).withItalic(true));
    }

    /** 武器底部標籤。 */
    public static Text egoTag() {
        return styled(Text.translatable("tooltip.limbusego.tag.ego"), Formatting.DARK_GRAY.getColorValue(), false);
    }

    /** 飾品底部標籤，tier 為 1~4。 */
    public static Text giftTag(int tier) {
        String roman = tier >= 1 && tier < ROMAN.length ? ROMAN[tier] : String.valueOf(tier);
        return styled(Text.translatable("tooltip.limbusego.tag.gift", roman), Formatting.DARK_GRAY.getColorValue(), false);
    }

    /** 套用該屬性專屬顏色的名稱。 */
    public static Text status(StatusEffect effect) {
        return styled(Text.translatable(effect.translationKey()), legacyToRgb(effect.color), false);
    }

    /** 威力／次數，例：威力2・次數1。 */
    public static Text potency(int potency, int count) {
        return styled(Text.translatable("tooltip.limbusego.potency", potency, count), Formatting.WHITE.getColorValue(), false);
    }

    private static Text prefixed(String prefix, String key, int color, Object... args) {
        MutableText text = Text.literal(prefix).append(Text.translatable(key, args));
        return styled(text, color, false);
    }

    private static MutableText styled(MutableText text, int rgb, boolean italic) {
        return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withItalic(italic));
    }

    /** StatusEffect 的 §x legacy 色碼 → RGB。未知色碼回傳白色。 */
    private static int legacyToRgb(String legacy) {
        if (legacy == null || legacy.length() < 2) return Formatting.WHITE.getColorValue();
        Formatting f = Formatting.byCode(legacy.charAt(1));
        Integer rgb = f == null ? null : f.getColorValue();
        return rgb == null ? Formatting.WHITE.getColorValue() : rgb;
    }
}
```

注意 `section()` 上面那段三元式是筆誤的產物——實作時直接寫成：

```java
    public static Text section(String key, Object... args) {
        return prefixed("\u25B8 ", key, Formatting.GOLD.getColorValue(), args);
    }
```

- [ ] **Step 4: 加入共用 lang 條目**

`zh_tw.json`：

```json
  "tooltip.limbusego.hint": "[Shift] 查看詳細資訊",
  "tooltip.limbusego.tag.ego": "E.G.O",
  "tooltip.limbusego.tag.gift": "E.G.O 飾品・階級 %s",
  "tooltip.limbusego.potency": "威力%s・次數%s"
```

`en_us.json`：

```json
  "tooltip.limbusego.hint": "[Shift] for details",
  "tooltip.limbusego.tag.ego": "E.G.O",
  "tooltip.limbusego.tag.gift": "E.G.O Gift \u00b7 Tier %s",
  "tooltip.limbusego.potency": "%s potency \u00b7 %s count"
```

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.client.TooltipFormatTest"`
Expected: PASS

若 `text.getString()` 對 `Text.translatable` 回傳的是鍵名而非翻譯後文字，那是預期的——測試斷言的是前綴與樣式，不是翻譯結果。

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/client/TooltipFormat.java src/test/java/me/yisang/limbusego/client/TooltipFormatTest.java src/main/resources/assets/limbusego/lang/
git commit -m "feat: tooltip 樣式工具 TooltipFormat / Add TooltipFormat tooltip styling helper"
```

---

### Task 4: 武器說明表與 tooltip listener

**Files:**
- Create: `src/main/java/me/yisang/limbusego/item/WeaponTooltips.java`
- Create: `src/main/java/me/yisang/limbusego/client/EgoTooltipHandler.java`
- Modify: `src/main/java/me/yisang/limbusego/client/LimbusEGOClient.java`
- Modify: `src/main/java/me/yisang/limbusego/item/WeaponStyles.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Test: `src/test/java/me/yisang/limbusego/item/WeaponTooltipsTest.java`

**Interfaces:**
- Consumes: `TooltipFormat.*`（Task 3）、`LangKeys.assertKeyExists`（Task 2）。
- Produces:
  - `WeaponTooltips.of(String itemId)` → `List<Text>`（查無回傳空清單）
  - `WeaponTooltips.ids()` → `Set<String>`
  - `EgoTooltipHandler.register()` → `void`

- [ ] **Step 1: 寫失敗測試**

建立 `src/test/java/me/yisang/limbusego/item/WeaponTooltipsTest.java`：

```java
package me.yisang.limbusego.item;

import me.yisang.limbusego.LangKeys;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WeaponTooltipsTest {

    /** 必須全部有說明的武器與彈藥 id。 */
    private static final Set<String> EXPECTED = Set.of(
            "solemn_lament_black", "solemn_lament_white", "solemn_shield", "butterfly_quartz",
            "mimicry", "dacapo", "ring_brush", "tiantui_star", "tiger_mark", "savage_tiger_mark",
            "twilight", "tibia", "w_corp_knife", "bladesinger");

    @Test
    void everyWeaponHasDetails() {
        for (String id : EXPECTED) {
            assertFalse(WeaponTooltips.of(id).isEmpty(), "武器缺少說明：" + id);
        }
    }

    @Test
    void unknownItemReturnsEmpty() {
        assertTrue(WeaponTooltips.of("diamond_sword").isEmpty());
        assertTrue(WeaponTooltips.of("").isEmpty());
    }

    @Test
    void everyReferencedKeyExistsInBothLanguages() {
        for (String id : WeaponTooltips.ids()) {
            collectKeys(WeaponTooltips.of(id)).forEach(LangKeys::assertKeyExists);
        }
    }

    private static List<String> collectKeys(List<Text> lines) {
        return lines.stream()
                .flatMap(t -> java.util.stream.Stream.concat(java.util.stream.Stream.of(t), t.getSiblings().stream()))
                .map(Text::getContent)
                .filter(TranslatableTextContent.class::isInstance)
                .map(c -> ((TranslatableTextContent) c).getKey())
                .toList();
    }
}
```

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.item.WeaponTooltipsTest"`
Expected: FAIL，編譯錯誤 `cannot find symbol: class WeaponTooltips`

- [ ] **Step 3: 建立 WeaponTooltips 骨架與第一把武器**

建立 `src/main/java/me/yisang/limbusego/item/WeaponTooltips.java`：

```java
package me.yisang.limbusego.item;

import me.yisang.limbusego.client.TooltipFormat;
import me.yisang.limbusego.status.StatusEffect;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 武器／彈藥的 Shift 展開說明集中表。
 *
 * <p>武器數值住在 {@code WeaponEvents}，不在各 {@code *Item} class，
 * 因此說明放集中表而非 per-class，與 {@link WeaponStyles} 的慣例一致。
 *
 * <p>**修改武器數值時務必同步這裡。**
 */
public final class WeaponTooltips {

    private static final String P = "tooltip.limbusego.";

    private static final Map<String, List<Text>> TABLE = Map.ofEntries(
        Map.entry("ring_brush", List.of(
            TooltipFormat.section(P + "ring_brush.strike"),
            TooltipFormat.body(P + "ring_brush.strike.damage", 3.5f),
            TooltipFormat.body(P + "ring_brush.strike.debuff"),
            TooltipFormat.body(P + "ring_brush.strike.status", TooltipFormat.potency(1, 3)),
            TooltipFormat.section(P + "ring_brush.double"),
            TooltipFormat.body(P + "ring_brush.double.effect"),
            TooltipFormat.section(P + "ring_brush.dash"),
            TooltipFormat.body(P + "ring_brush.dash.effect")))
    );

    private WeaponTooltips() {}

    /** 查表；查無回傳空清單。 */
    public static List<Text> of(String itemId) {
        return TABLE.getOrDefault(itemId, List.of());
    }

    /** 表中所有 id。 */
    public static Set<String> ids() {
        return TABLE.keySet();
    }
}
```

對應 lang，`zh_tw.json`：

```json
  "tooltip.limbusego.ring_brush.strike": "筆刷一擊（右鍵）",
  "tooltip.limbusego.ring_brush.strike.damage": "對目標造成 %s 傷害",
  "tooltip.limbusego.ring_brush.strike.debuff": "施加一種隨機負面效果",
  "tooltip.limbusego.ring_brush.strike.status": "施加一種隨機屬性 %s",
  "tooltip.limbusego.ring_brush.double": "雙擊（1.5 秒內再次右鍵同一目標）",
  "tooltip.limbusego.ring_brush.double.effect": "上述效果觸發 2 次",
  "tooltip.limbusego.ring_brush.dash": "前衝（右鍵未命中目標）",
  "tooltip.limbusego.ring_brush.dash.effect": "向前突進一段距離"
```

`en_us.json`：

```json
  "tooltip.limbusego.ring_brush.strike": "Brush Strike (right-click)",
  "tooltip.limbusego.ring_brush.strike.damage": "Deals %s damage to the target",
  "tooltip.limbusego.ring_brush.strike.debuff": "Applies one random vanilla debuff",
  "tooltip.limbusego.ring_brush.strike.status": "Applies one random status at %s",
  "tooltip.limbusego.ring_brush.double": "Double Strike (right-click the same target within 1.5s)",
  "tooltip.limbusego.ring_brush.double.effect": "The above effects trigger twice",
  "tooltip.limbusego.ring_brush.dash": "Dash (right-click without hitting a target)",
  "tooltip.limbusego.ring_brush.dash.effect": "Lunge forward a short distance"
```

- [ ] **Step 4: 補完其餘 13 件**

依同樣格式補完 `EXPECTED` 中其餘 13 個 id。**數值來源優先序：`src/main/java/me/yisang/limbusego/event/WeaponEvents.java`（760 行，權威） > `README.md` 的「武器一覽」表 > 各 `*Item.java` 的 javadoc。**

`README.md` 武器表的現有摘要（用來確認沒漏掉機制，但數值仍以 `WeaponEvents` 為準）：

| 武器 id | 機制摘要 |
|---|---|
| `solemn_lament_black` | 右鍵消耗蝴蝶石英發射彈幕（1.2s 冷卻）；命中 8 傷＋凋零 II＋沉淪 4p/3c |
| `solemn_lament_white` | 同上，命中 4 傷＋失明＋沉淪 3p/2c |
| `solemn_shield` | 持有時每 5 tick 半徑 5 格緩速 II＋束縛，自身補守護（上限 3） |
| `butterfly_quartz` | 莊嚴哀悼的彈藥 |
| `mimicry` | 10% 暴擊 +40~90 傷、吸血 25%，暴擊給自己強壯 3p/4c |
| `dacapo` | 取消一般攻擊改連擊：普通 5×1.5、特殊 3×5.0，AoE 3.5 格，每擊沉淪 1p/1c |
| `tiantui_star` | 右鍵蓄力 1s 衝刺（傷 8＋燒 3s＋震顫 5p/6c＋燒傷 4p/3c）；潛行蓄力 3s 猛擊（傷 18＋凋零 II＋震顫 8p/6c＋燒傷 6p/4c） |
| `tiger_mark` | 天退星刀的彈藥 |
| `savage_tiger_mark` | 天退星刀的強化彈藥 |
| `twilight` | 瀕死增傷（→×2.5）＋30% 真傷；潛行蓄力 1.5s 暮光斬（扇形＋凋零＋破裂 5p/2c） |
| `tibia` | 疊流血 3p/2c＋Melody 增傷（每 3 potency +3%，上限 30%）；潛行蓄力 2s 解剖斬（+12p/6c 流血並強制引爆 3 次） |
| `w_corp_knife` | 命中疊充能（上限 10p，每擊 1p/5c，滿層續 count），20% 過載 +1p/1c |
| `bladesinger` | 疊呼吸法提高爆擊率；低血（<3 心）潛行右鍵目標 → 5 連斬 |

特別注意這兩件玩家回報過的 UX 問題，說明務必寫清楚：

- `solemn_lament_black` / `solemn_lament_white`：玩家反映「右鍵射擊、左鍵也能近戰」反直覺，且會消耗子彈。section 標題要明說是右鍵發射、消耗蝴蝶石英。
- `solemn_shield`：玩家誤以為是棺材、只能近戰。說明要點出它是持有型光環，不需主動使用。

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.item.WeaponTooltipsTest"`
Expected: PASS

- [ ] **Step 6: 移除重複的機制 lore**

天退星刀與薄暝的機制說明已進 `WeaponTooltips`，移除舊的 lore 行避免重複顯示。

從兩個 lang 檔刪除這三個鍵：`item.limbusego.tiantui_star.lore.1`、`item.limbusego.twilight.lore.1`、`item.limbusego.twilight.lore.2`。

在 `WeaponStyles.java` 的 `SPECS` 中把對應項的 `loreColors` 縮成只剩第 0 行：

```java
        Map.entry("tiantui_star",        new Spec(0xE67E22, false, new int[]{0xAAAAAA})),
        Map.entry("twilight",            new Spec(0xFFD700, false, new int[]{0xAAAAAA})),
```

- [ ] **Step 7: 實作 tooltip listener**

建立 `src/main/java/me/yisang/limbusego/client/EgoTooltipHandler.java`：

```java
package me.yisang.limbusego.client;

import me.yisang.limbusego.gift.BaseGift;
import me.yisang.limbusego.gift.GiftRegistry;
import me.yisang.limbusego.item.WeaponTooltips;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * 客戶端 tooltip：未按 Shift 顯示提示列，按住 Shift 展開機制說明。
 *
 * <p>放在客戶端而非 LORE 元件，因為 Shift 是純客戶端狀態，而且飾品說明
 * 需要依 stack 的升級等級即時計算。
 */
public final class EgoTooltipHandler {

    private EgoTooltipHandler() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            List<Text> details = detailsFor(stack);
            if (details.isEmpty()) return;

            if (Screen.hasShiftDown()) {
                lines.add(Text.empty());
                lines.addAll(details);
                lines.add(Text.empty());
                lines.add(footerFor(stack));
            } else {
                lines.add(Text.empty());
                lines.add(TooltipFormat.hint());
            }
        });
    }

    private static List<Text> detailsFor(net.minecraft.item.ItemStack stack) {
        BaseGift gift = GiftRegistry.byItem(stack.getItem());
        if (gift != null) return gift.describe(stack);

        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (!"limbusego".equals(id.getNamespace())) return List.of();
        return WeaponTooltips.of(id.getPath());
    }

    private static Text footerFor(net.minecraft.item.ItemStack stack) {
        BaseGift gift = GiftRegistry.byItem(stack.getItem());
        return gift != null ? TooltipFormat.giftTag(gift.tier()) : TooltipFormat.egoTag();
    }
}
```

在 `LimbusEGOClient.onInitializeClient()` 中呼叫：

```java
    @Override
    public void onInitializeClient() {
        EgoTooltipHandler.register();
    }
```

- [ ] **Step 8: 編譯**

Run: `./gradlew.bat build -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: 遊戲內驗證行序**

Vanilla 會在 callback 之前把 `LORE` 元件的內容加入 `lines`，但實際順序需要親眼確認。

Run: `./gradlew.bat runClient`

檢查：
1. 創造模式取得環指筆刷，確認收合時是「名稱 → 不及格。 → 空行 → `[Shift] 查看詳細資訊`」。
2. 按住 Shift，確認詳細區塊出現在風味台詞**之後**，且底部有 `E.G.O`。
3. 拿原版鑽石劍，確認 tooltip 沒有任何多餘行。

**若我們的行出現在風味台詞之前**：改為計算插入位置而非 `lines.add()`——找出最後一個既有行的索引後用 `lines.add(index, ...)`。把實際觀察到的順序寫進 `EgoTooltipHandler` 的註解。

- [ ] **Step 10: Commit**

```bash
git add src/main/java/me/yisang/limbusego/item/WeaponTooltips.java src/main/java/me/yisang/limbusego/item/WeaponStyles.java src/main/java/me/yisang/limbusego/client/ src/test/java/me/yisang/limbusego/item/WeaponTooltipsTest.java src/main/resources/assets/limbusego/lang/
git commit -m "feat: 武器 Shift 展開式 tooltip / Add shift-expandable weapon tooltips"
```

---

### Task 5: 飾品說明鉤子與覆蓋率測試骨架

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/BaseGift.java`
- Create: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`

**Interfaces:**
- Consumes: `TooltipFormat.*`（Task 3）、`GiftUpgradeLogic.multiplier(int)`（既有）。
- Produces:
  - `BaseGift.describe(ItemStack self)` → `List<Text>`（final，供 `EgoTooltipHandler` 呼叫）
  - `BaseGift.describe(int level)` → `List<Text>`（子類覆寫點，預設空清單）
  - `BaseGift.scaled(int potency, int level)` → `int`（protected static，供子類算出與 `applyScaled` 一致的顯示值）
  - `GiftDescriptionCoverageTest.DONE`：已完成組別的 id 白名單，Task 6–14 逐步擴充

- [ ] **Step 1: 寫失敗測試**

建立 `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`。它讀 `ModGifts.java` 原始碼取得 id ↔ class 配對再反射實例化，因此涵蓋的正好是實際註冊的 80 件，且完全不碰 Minecraft registry：

```java
package me.yisang.limbusego.gift;

import me.yisang.limbusego.LangKeys;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GiftDescriptionCoverageTest {

    /** 已完成 describe() 的飾品 id。每完成一組就把該組 id 加進來。 */
    static final Set<String> DONE = Set.of();

    private static final Path MOD_GIFTS =
            Path.of("src/main/java/me/yisang/limbusego/gift/ModGifts.java");
    private static final Pattern REG =
            Pattern.compile("reg\\(\"([a-z0-9_]+)\"\\s*,\\s*new\\s+([A-Za-z0-9_]+)\\s*\\(");

    @Test
    void modGiftsParsesToEightyEntries() {
        assertEquals(80, registered().size(), "解析 ModGifts.java 得到的飾品數不符");
    }

    @Test
    void completedGiftsHaveNonEmptyDescription() {
        registered().forEach((id, gift) -> {
            if (!DONE.contains(id)) return;
            assertFalse(gift.describe(0).isEmpty(), "飾品缺少說明：" + id);
        });
    }

    @Test
    void completedGiftKeysExistInBothLanguages() {
        registered().forEach((id, gift) -> {
            if (!DONE.contains(id)) return;
            for (int level = 0; level <= 3; level++) {
                translationKeys(gift.describe(level)).forEach(LangKeys::assertKeyExists);
            }
        });
    }

    /** id → 已實例化的飾品，來源為 ModGifts.java 的註冊列表。 */
    static Map<String, BaseGift> registered() {
        try {
            String src = Files.readString(MOD_GIFTS, StandardCharsets.UTF_8);
            Map<String, BaseGift> out = new LinkedHashMap<>();
            Matcher m = REG.matcher(src);
            while (m.find()) {
                String className = "me.yisang.limbusego.gift.gifts." + m.group(2);
                Object instance = Class.forName(className).getDeclaredConstructor().newInstance();
                out.put(m.group(1), (BaseGift) instance);
            }
            return out;
        } catch (IOException | ReflectiveOperationException e) {
            throw new IllegalStateException("無法從 ModGifts.java 建立飾品實例", e);
        }
    }

    /** 遞迴取出所有翻譯鍵（含 sibling 與 translatable 參數）。 */
    static List<String> translationKeys(List<Text> lines) {
        return lines.stream().flatMap(GiftDescriptionCoverageTest::flatten)
                .map(Text::getContent)
                .filter(TranslatableTextContent.class::isInstance)
                .map(c -> ((TranslatableTextContent) c).getKey())
                .toList();
    }

    private static Stream<Text> flatten(Text text) {
        Stream<Text> self = Stream.of(text);
        Stream<Text> siblings = text.getSiblings().stream().flatMap(GiftDescriptionCoverageTest::flatten);
        Stream<Text> args = text.getContent() instanceof TranslatableTextContent t
                ? Stream.of(t.getArgs()).filter(Text.class::isInstance).map(Text.class::cast)
                        .flatMap(GiftDescriptionCoverageTest::flatten)
                : Stream.empty();
        return Stream.concat(self, Stream.concat(siblings, args));
    }
}
```

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: FAIL，編譯錯誤 `cannot find symbol: method describe(int)`

- [ ] **Step 3: 加入 describe 鉤子**

在 `BaseGift.java` 的 `multiplier(ItemStack)` 附近加入：

```java
    // ── tooltip 說明 ─────────────────────────────────────────────────

    /** 供客戶端 tooltip 呼叫；取出升級等級後轉呼 {@link #describe(int)}。 */
    public final java.util.List<net.minecraft.text.Text> describe(ItemStack self) {
        return describe(self.getOrDefault(ModComponents.GIFT_LEVEL, 0));
    }

    /**
     * 此飾品在指定升級等級（0~3）的 Shift 展開說明。子類覆寫此方法。
     *
     * <p>數值若在邏輯中經過 {@link #multiplier} 或 {@code applyScaled}，此處必須用
     * {@link GiftUpgradeLogic#multiplier(int)} 算出對應值，不可寫死。
     */
    public java.util.List<net.minecraft.text.Text> describe(int level) {
        return java.util.List.of();
    }

    /** applyScaled 的 potency 放大規則，供 describe() 顯示與邏輯一致的數值。 */
    protected static int scaled(int potency, int level) {
        return (int) Math.round(potency * GiftUpgradeLogic.multiplier(level));
    }
```

- [ ] **Step 4: 執行測試確認通過**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: PASS（`DONE` 為空，只有 `modGiftsParsesToEightyEntries` 實質斷言）

若 `modGiftsParsesToEightyEntries` 失敗且數字不是 80，先確認正規式是否漏掉某些寫法，再繼續。

- [ ] **Step 5: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/BaseGift.java src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java
git commit -m "feat: 飾品說明鉤子 describe(int) / Add gift describe(int) tooltip hook"
```

---

### Task 6: 飾品風味台詞移植

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/ModGifts.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Test: `src/test/java/me/yisang/limbusego/gift/GiftFlavorTest.java`

**Interfaces:**
- Consumes: `LangKeys`（Task 2）、`GiftDescriptionCoverageTest.registered()`（Task 5）。
- Produces: 46 個 `item.limbusego.<id>.lore.0` 翻譯鍵；`ModGifts.reg()` 改掛該鍵。

**背景：** Paper 插件的 `BaseAccessory` 有兩種建構子。6 參數版帶名稱色與風味台詞，4 參數版沒有。80 件 Fabric 飾品中 46 件有風味台詞、34 件本來就沒有。**不要替那 34 件杜撰台詞。**

- [ ] **Step 1: 擷取風味台詞**

建立暫存腳本 `scripts/extract_flavor.py`（本 task 結束後刪除，不進 commit）：

```python
import re, glob, json, os

SRC = r"C:/Users/User/IdeaProjects/Limbus-E.G.O/src/main/java/me/yisang/limbusego/gift/gifts"
EN = r"C:/Users/User/IdeaProjects/Limbus-E.G.O/src/main/resources/lang/gifts/en_US.yml"
FAB = "src/main/java/me/yisang/limbusego/gift/ModGifts.java"

BS = chr(92) + chr(92)
STR = '"((?:[^"' + BS + ']|' + BS + '.)*)"'
p6 = re.compile(r'super\s*\(\s*plugin\s*,\s*' + r'\s*,\s*'.join([STR] * 5), re.S)

zh = {}
for f in sorted(glob.glob(os.path.join(SRC, "*.java"))):
    m = p6.search(open(f, encoding='utf-8').read())
    if m:
        zh[m.group(1)] = m.group(4)

en, cur = {}, None
for line in open(EN, encoding='utf-8').read().splitlines():
    m = re.match(r'^  ([a-z0-9_]+):\s*$', line)
    if m:
        cur = m.group(1)
        continue
    m = re.match(r'^    description:\s*\[?"(.*?)"\]?\s*$', line)
    if m and cur:
        en[cur] = re.sub(r'^&#[0-9A-Fa-f]{6}', '', m.group(1))   # 去掉顏色前綴

ids = re.findall(r'reg\("([a-z0-9_]+)"', open(FAB, encoding='utf-8').read())
out = {i: {"zh": zh[i], "en": en.get(i)} for i in ids if i in zh}
json.dump(out, open("flavor.json", "w", encoding="utf-8"), ensure_ascii=False, indent=1)
print("with flavor:", len(out), "missing en:", [k for k, v in out.items() if not v["en"]])
```

Run: `python scripts/extract_flavor.py`
Expected: `with flavor: 46 missing en: ['flower_mound', 'phantom_pain', 'spicebush_branch']`

若數字不符，**停下來回報**，不要繼續——代表插件原始碼與此計畫的假設不一致。

- [ ] **Step 2: 寫失敗測試**

建立 `src/test/java/me/yisang/limbusego/gift/GiftFlavorTest.java`：

```java
package me.yisang.limbusego.gift;

import me.yisang.limbusego.LangKeys;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GiftFlavorTest {

    /** 插件中帶有風味台詞的 46 件（6 參數建構子）。其餘 34 件本來就沒有，不補寫。 */
    private static final Set<String> WITH_FLAVOR = Set.of(
            /* Step 1 產出的 flavor.json 的 46 個 key，逐一填入 */);

    @Test
    void flavorKeysExistInBothLanguages() {
        assertEquals(46, WITH_FLAVOR.size(), "有風味台詞的飾品應為 46 件");
        for (String id : WITH_FLAVOR) {
            LangKeys.assertKeyExists("item.limbusego." + id + ".lore.0");
        }
    }

    @Test
    void giftsWithoutFlavorHaveNoLoreKey() {
        GiftDescriptionCoverageTest.registered().keySet().forEach(id -> {
            if (WITH_FLAVOR.contains(id)) return;
            assertFalse(LangKeys.zhTw().contains("item.limbusego." + id + ".lore.0"),
                    "此飾品在插件中沒有風味台詞，不應杜撰：" + id);
        });
    }

    @Test
    void obsoleteDescKeysRemoved() {
        GiftDescriptionCoverageTest.registered().keySet().forEach(id -> {
            assertFalse(LangKeys.zhTw().contains("item.limbusego." + id + ".desc"),
                    "舊的密集單行 .desc 應已移除：" + id);
            assertFalse(LangKeys.enUs().contains("item.limbusego." + id + ".desc"),
                    "舊的密集單行 .desc 應已移除：" + id);
        });
    }
}
```

把 Step 1 產出的 46 個 id 填進 `WITH_FLAVOR`。

- [ ] **Step 3: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftFlavorTest"`
Expected: FAIL，訊息為 `zh_tw.json 缺少翻譯鍵：item.limbusego.<id>.lore.0`

- [ ] **Step 4: 寫入 lang**

依 `flavor.json` 把 46 筆寫進兩個 lang 檔的飾品區，鍵為 `item.limbusego.<id>.lore.0`。

三個缺英文的自行補譯，中文原句為：
- `flower_mound`、`phantom_pain`、`spicebush_branch` — 譯文貼合原句語氣即可，不要加料。

- [ ] **Step 5: 移除舊的 .desc 鍵**

從兩個 lang 檔刪除全部 80 個 `item.limbusego.<id>.desc`。這些內容將在 Task 7–15 逐組轉成結構化說明；**在轉換完成前先把原文複製一份到 `docs/superpowers/plans/gift-desc-backup.md` 以免遺失**（該檔在 Task 15 完成後刪除）。

- [ ] **Step 6: 改掛 LORE**

`ModGifts.reg()` 中：

```java
    private static Item reg(String name, BaseGift gift) {
        Text flavor = Text.translatable("item.limbusego." + name + ".lore.0")
                .setStyle(Style.EMPTY.withColor(Formatting.GRAY).withItalic(false));
        Item.Settings settings = new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, LimbusEGOMod.id(name)))
                .maxCount(1)
                .rarity(Rarity.EPIC);
        if (HAS_FLAVOR.contains(name)) {
            settings.component(DataComponentTypes.LORE, new LoreComponent(java.util.List.of(flavor)));
        }
        Item item = new Item(settings);
        ...
    }
```

並在 `ModGifts` 中加入 `HAS_FLAVOR`（同 Step 2 的 46 個 id 集合）。沒有風味台詞的 34 件不掛 `LORE`，否則遊戲內會顯示原始翻譯鍵。

- [ ] **Step 7: 執行測試確認通過**

Run: `./gradlew.bat test`
Expected: 全部 PASS

- [ ] **Step 8: 遊戲內驗證**

Run: `./gradlew.bat runClient`

檢查：取一件有台詞的（`ashes_to_ashes`）與一件沒台詞的（`carmilla`），確認前者顯示台詞、後者只有名稱，**兩者都不會出現 `item.limbusego.xxx.lore.0` 這種原始鍵名**。

- [ ] **Step 9: Commit**

```bash
rm scripts/extract_flavor.py flavor.json
git add src/main/java/me/yisang/limbusego/gift/ModGifts.java src/test/java/me/yisang/limbusego/gift/GiftFlavorTest.java src/main/resources/assets/limbusego/lang/ docs/superpowers/plans/gift-desc-backup.md
git commit -m "feat: 移植 46 件飾品風味台詞 / Port flavor text for 46 gifts from the Paper plugin"
```

---

### Task 7: 燒傷組說明（8 件）

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/gifts/{ArdentFlower,AshesToAshes,BloodflameSword,DustToDust,GlimpseOfFlames,HotNJuicyDrumstick,PainOfStifledRage,RoyalJellyPerfume}.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Modify: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`

**Interfaces:**
- Consumes: `BaseGift.describe(int)`（Task 5）、`TooltipFormat.*`（Task 3）、`gift-desc-backup.md` 的原文（Task 6）。
- Produces: 8 件燒傷飾品的 `describe(int)` 實作。

- [ ] **Step 1: 把本組 id 加進覆蓋率白名單（使測試失敗）**

`GiftDescriptionCoverageTest.DONE` 改為：

```java
    static final Set<String> DONE = Set.of(
            "ardent_flower", "ashes_to_ashes", "bloodflame_sword", "dust_to_dust",
            "glimpse_of_flames", "hot_n_juicy_drumstick", "pain_of_stifled_rage", "royal_jelly_perfume");
```

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: FAIL，`飾品缺少說明：ardent_flower` 等 8 筆

- [ ] **Step 3: 逐件實作**

依 Global Constraints 的「飾品說明轉換程序」處理這 8 件。本組的 `.desc` 原文（來自 `gift-desc-backup.md`）：

| id | `.desc` |
|---|---|
| `ardent_flower` | 被動：免疫火焰傷害｜攻擊：施加燒傷 2·2｜攻擊燒傷中且生命低於30%的目標：+30% 傷害 |
| `ashes_to_ashes` | 攻擊燒傷中目標：疊加燒傷 2·1 |
| `bloodflame_sword` | 攻擊：施加燒傷 3·2｜獲得 1 點 SAN |
| `dust_to_dust` | 攻擊：施加燒傷 3·2｜擊殺：對 3 格內敵人擴散燒傷 3·2 |
| `glimpse_of_flames` | 攻擊燒傷中目標：引爆燒傷造成真傷並施加易損 1·2 |
| `hot_n_juicy_drumstick` | 被動：飽食度不流失｜攻擊燒傷中目標：延長燒傷 2 層 |
| `pain_of_stifled_rage` | 攻擊燒傷中目標：獲得強壯 2·1；否則施加燒傷 2·2 |
| `royal_jelly_perfume` | 被動：附近蜜蜂不攻擊｜受擊：對燒傷中的攻擊者 -15% 傷害；並施加燒傷 2·2 |

`ardent_flower` 的完整寫法（多段 + 條件段）：

```java
    @Override
    public List<Text> describe(int level) {
        return List.of(
            TooltipFormat.section("tooltip.limbusego.ardent_flower.passive"),
            TooltipFormat.body("tooltip.limbusego.ardent_flower.passive.immune"),
            TooltipFormat.section("tooltip.limbusego.ardent_flower.attack"),
            TooltipFormat.body("tooltip.limbusego.ardent_flower.attack.burn",
                    TooltipFormat.status(StatusEffect.BURN),
                    TooltipFormat.potency(scaled(2, level), 2)),
            TooltipFormat.section("tooltip.limbusego.ardent_flower.execute"),
            TooltipFormat.body("tooltip.limbusego.ardent_flower.execute.bonus", 30));
    }
```

其中 `scaled(int base, int level)` 是 Task 5 加在 `BaseGift` 的輔助方法，對應 `applyScaled` 的取整規則。

**注意**：`ardent_flower` 的 +30% 是否隨升級縮放，以 `ArdentFlower.java` 的實際程式為準；若有 `multiplier`，`30` 要改成算出來的值。

- [ ] **Step 4: 加入 lang 條目**

本組所有新鍵同時寫進 `zh_tw.json` 與 `en_us.json`。

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test`
Expected: 全部 PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/ src/test/java/me/yisang/limbusego/gift/ src/main/resources/assets/limbusego/lang/
git commit -m "feat: 燒傷組飾品說明 / Add tooltips for the 8 burn gifts"
```

---

### Task 8: 流血組說明（6 件）

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/gifts/{CrystallizedBlood,LaManchalandAllDayPass,LaManchalandStandardPass,MaskOfTheParade,Millarca,SanguineBlossomBolus}.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Modify: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`

**Interfaces:**
- Consumes: `BaseGift.describe(int)`、`BaseGift.scaled(int, int)`（Task 5）、`TooltipFormat.*`。
- Produces: 6 件流血飾品的 `describe(int)` 實作。

- [ ] **Step 1: 把本組 id 加進 `DONE`（使測試失敗）**

在既有集合後追加 `"crystallized_blood", "la_manchaland_all_day_pass", "la_manchaland_standard_pass", "mask_of_the_parade", "millarca", "sanguine_blossom_bolus"`。

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: FAIL，`飾品缺少說明：crystallized_blood` 等 6 筆

- [ ] **Step 3: 逐件實作**

依 Global Constraints 的轉換程序處理。本組特別注意 `crystallized_blood`，它的 `.desc` 明確標示有升級縮放：

> 被動：每 5 秒消耗全部流血層數回復生命（回復量為層數一半，上限 4，隨升級提升）

因此上限 `4` 必須寫成 `GiftUpgradeLogic.multiplier(level)` 算出的值，並與 `CrystallizedBlood.java` 的實際程式對照確認取整方式。

- [ ] **Step 4: 加入 lang 條目**

本組 `describe(int)` 引用的每個新鍵，都依 Global Constraints 的「飾品說明轉換程序」與命名規則，同時寫進 `zh_tw.json` 與 `en_us.json`。英文不是直譯中文：section 標題用祈使／條件句（`On hit`、`While standing still`），body 用完整敘述句。

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test`
Expected: 全部 PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/ src/test/java/me/yisang/limbusego/gift/ src/main/resources/assets/limbusego/lang/
git commit -m "feat: 流血組飾品說明 / Add tooltips for the 6 bleed gifts"
```

---

### Task 9: 沉淪組說明（10 件）

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/gifts/{ArtisticSense,BlackSheetMusic,BrokenCompass,ColdIllusion,DistantStar,FrozenCries,MentalCorruptionBoostingGas,Rags,Rest,TangledBones}.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Modify: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`

**Interfaces:**
- Consumes: `BaseGift.describe(int)`、`BaseGift.scaled(int, int)`、`TooltipFormat.*`。
- Produces: 10 件沉淪飾品的 `describe(int)` 實作。

- [ ] **Step 1: 把本組 id 加進 `DONE`（使測試失敗）**

追加 `"artistic_sense", "black_sheet_music", "broken_compass", "cold_illusion", "distant_star", "frozen_cries", "mental_corruption_boosting_gas", "rags", "rest", "tangled_bones"`。

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: FAIL，10 筆缺說明

- [ ] **Step 3: 逐件實作**

`rest` 是本組唯一同時有被動與百分比縮放的，完整寫法：

```java
    @Override
    public List<Text> describe(int level) {
        int pct = Math.round((float) Math.min(0.30, 0.15 * GiftUpgradeLogic.multiplier(level)) * 100);
        return List.of(
            TooltipFormat.section("tooltip.limbusego.rest.passive"),
            TooltipFormat.body("tooltip.limbusego.rest.passive.regen"),
            TooltipFormat.section("tooltip.limbusego.rest.attack"),
            TooltipFormat.body("tooltip.limbusego.rest.attack.bonus",
                    TooltipFormat.status(StatusEffect.SINKING), pct));
    }
```

```json
"tooltip.limbusego.rest.passive": "靜止時",
"tooltip.limbusego.rest.passive.regen": "獲得生命再生 I",
"tooltip.limbusego.rest.attack": "攻擊時",
"tooltip.limbusego.rest.attack.bonus": "對 %s 中的目標 +%s%% 傷害"
```

```json
"tooltip.limbusego.rest.passive": "While standing still",
"tooltip.limbusego.rest.passive.regen": "Gain Regeneration I",
"tooltip.limbusego.rest.attack": "On hit",
"tooltip.limbusego.rest.attack.bonus": "+%s%% damage against %s targets"
```

注意 `%%` 才會輸出字面的百分號。中英文的參數順序不同是可以的——`Text.translatable` 支援 `%1$s` 明確索引，若順序需要調換就用索引形式。

- [ ] **Step 4: 加入 lang 條目**

本組 `describe(int)` 引用的每個新鍵，都依 Global Constraints 的「飾品說明轉換程序」與命名規則，同時寫進 `zh_tw.json` 與 `en_us.json`。英文不是直譯中文：section 標題用祈使／條件句（`On hit`、`While standing still`），body 用完整敘述句。

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test`
Expected: 全部 PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/ src/test/java/me/yisang/limbusego/gift/ src/main/resources/assets/limbusego/lang/
git commit -m "feat: 沉淪組飾品說明 / Add tooltips for the 10 sinking gifts"
```

---

### Task 10: 破裂組說明（11 件）

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/gifts/{DryToTheBoneBreast,EbonyBrooch,FlowerInTheMirror,Harestride,MoonInTheWater,Ruin,SmokingGunpowder,StrangeGlyphInscriptions,StrangeGlyphTalisman,Thunderbranch,ChiefButlersSecretArts}.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Modify: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`

**Interfaces:**
- Consumes: `BaseGift.describe(int)`、`BaseGift.scaled(int, int)`、`TooltipFormat.*`。
- Produces: 11 件破裂飾品的 `describe(int)` 實作。

- [ ] **Step 1: 把本組 id 加進 `DONE`（使測試失敗）**

追加 `"dry_to_the_bone_breast", "ebony_brooch", "flower_in_the_mirror", "harestride", "moon_in_the_water", "ruin", "smoking_gunpowder", "strange_glyph_inscriptions", "strange_glyph_talisman", "thunderbranch", "chief_butlers_secret_arts"`。

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: FAIL，11 筆缺說明

- [ ] **Step 3: 逐件實作**

依轉換程序處理。本組含機率型（`thunderbranch` 10% 召喚閃電）與擊殺擴散型（`strange_glyph_talisman` 對 5 格內敵人擴散破裂 3·2），section 標題要分別寫成「攻擊時」與「擊殺時」，讓玩家看得出觸發時機不同。

- [ ] **Step 4: 加入 lang 條目**

本組 `describe(int)` 引用的每個新鍵，都依 Global Constraints 的「飾品說明轉換程序」與命名規則，同時寫進 `zh_tw.json` 與 `en_us.json`。英文不是直譯中文：section 標題用祈使／條件句（`On hit`、`While standing still`），body 用完整敘述句。

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test`
Expected: 全部 PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/ src/test/java/me/yisang/limbusego/gift/ src/main/resources/assets/limbusego/lang/
git commit -m "feat: 破裂組飾品說明 / Add tooltips for the 11 rupture gifts"
```

---

### Task 11: 震顫組說明（6 件）

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/gifts/{GreenSpirit,NixieDivergence,SourLiquorAroma,Sownpour,PieceOfCrumbledEgg,HandheldMirror}.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Modify: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`

**Interfaces:**
- Consumes: `BaseGift.describe(int)`、`BaseGift.scaled(int, int)`、`TooltipFormat.*`。
- Produces: 6 件震顫飾品的 `describe(int)` 實作。

- [ ] **Step 1: 把本組 id 加進 `DONE`（使測試失敗）**

追加 `"green_spirit", "nixie_divergence", "sour_liquor_aroma", "sownpour", "piece_of_crumbled_egg", "handheld_mirror"`。

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: FAIL，6 筆缺說明

- [ ] **Step 3: 逐件實作**

本組兩件是**被動反擊型**，觸發時機容易被玩家誤解，section 標題要寫明：

- `piece_of_crumbled_egg`：`死亡時：對殺手落雷並施加震顫 5·3` → 標題寫「自身死亡時」
- `handheld_mirror`：`受擊：對攻擊者施加束縛 2·2 與脆弱 1·2` → 標題寫「受到攻擊時」

- [ ] **Step 4: 加入 lang 條目**

本組 `describe(int)` 引用的每個新鍵，都依 Global Constraints 的「飾品說明轉換程序」與命名規則，同時寫進 `zh_tw.json` 與 `en_us.json`。英文不是直譯中文：section 標題用祈使／條件句（`On hit`、`While standing still`），body 用完整敘述句。

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test`
Expected: 全部 PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/ src/test/java/me/yisang/limbusego/gift/ src/main/resources/assets/limbusego/lang/
git commit -m "feat: 震顫組飾品說明 / Add tooltips for the 6 tremor gifts"
```

---

### Task 12: 呼吸法組說明（7 件）

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/gifts/{CaskSpirits,ClearMirrorCalmWater,EmeraldElytra,Finifugality,Keenbranch,Nebulizer,CQCManual}.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Modify: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`

**Interfaces:**
- Consumes: `BaseGift.describe(int)`、`BaseGift.scaled(int, int)`、`TooltipFormat.*`。
- Produces: 7 件呼吸法飾品的 `describe(int)` 實作。

- [ ] **Step 1: 把本組 id 加進 `DONE`（使測試失敗）**

追加 `"cask_spirits", "clear_mirror_calm_water", "emerald_elytra", "finifugality", "keenbranch", "nebulizer", "cqc_manual"`。

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: FAIL，7 筆缺說明

- [ ] **Step 3: 逐件實作**

`nebulizer` 是群體增益（`被動：每 5 秒使自身與 5 格內玩家獲得呼吸法 2·2`），section 標題要寫明範圍與週期，否則玩家看不出它影響隊友。`cqc_manual` 限定近戰（`近戰攻擊：獲得呼吸法 2·2`），標題要寫「近戰攻擊時」而非「攻擊時」。

- [ ] **Step 4: 加入 lang 條目**

本組 `describe(int)` 引用的每個新鍵，都依 Global Constraints 的「飾品說明轉換程序」與命名規則，同時寫進 `zh_tw.json` 與 `en_us.json`。英文不是直譯中文：section 標題用祈使／條件句（`On hit`、`While standing still`），body 用完整敘述句。

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test`
Expected: 全部 PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/ src/test/java/me/yisang/limbusego/gift/ src/main/resources/assets/limbusego/lang/
git commit -m "feat: 呼吸法組飾品說明 / Add tooltips for the 7 poise gifts"
```

---

### Task 13: 輔助組說明（15 件）

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/gifts/{BloodyGadget,DreamingElectricSheep,DuelingManualBook3,IllusoryHunt,LateBloomersTattoo,Hardship,PhantomPain,TenacityBolus,TheBookOfVengeance,SpecialContract,PlumeOfProof,SpicebushBranch,Carmilla,ETypeDimensionalDagger,TraumaShield}.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Modify: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`

**Interfaces:**
- Consumes: `BaseGift.describe(int)`、`BaseGift.scaled(int, int)`、`TooltipFormat.*`。
- Produces: 15 件輔助飾品的 `describe(int)` 實作。

- [ ] **Step 1: 把本組 id 加進 `DONE`（使測試失敗）**

追加 `"bloody_gadget", "dreaming_electric_sheep", "dueling_manual_book_3", "illusory_hunt", "late_bloomers_tattoo", "hardship", "phantom_pain", "tenacity_bolus", "the_book_of_vengeance", "special_contract", "plume_of_proof", "spicebush_branch", "carmilla", "e_type_dimensional_dagger", "trauma_shield"`。

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: FAIL，15 筆缺說明

- [ ] **Step 3: 逐件實作**

本組是最大的一組，且含多種百分比縮放。特別注意：

- `carmilla`：`攻擊滿血目標：+20% 傷害`，程式為 `Math.min(0.30, 0.20 * m)`，**必須用 level 算出實際值**，不可寫死 20。
- `phantom_pain`：`攻擊：+15% 傷害`，同樣確認是否有 `multiplier`。
- `e_type_dimensional_dagger`、`trauma_shield`：README 提到「瞬移背刺」「免死」，這類條件觸發要把觸發條件寫在 section 標題。

- [ ] **Step 4: 加入 lang 條目**

本組 `describe(int)` 引用的每個新鍵，都依 Global Constraints 的「飾品說明轉換程序」與命名規則，同時寫進 `zh_tw.json` 與 `en_us.json`。英文不是直譯中文：section 標題用祈使／條件句（`On hit`、`While standing still`），body 用完整敘述句。

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test`
Expected: 全部 PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/ src/test/java/me/yisang/limbusego/gift/ src/main/resources/assets/limbusego/lang/
git commit -m "feat: 輔助組飾品說明 / Add tooltips for the 15 support gifts"
```

---

### Task 14: 便利組與原創組說明（17 件）

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/gift/gifts/{BlueZippoLighter,ChildWithinAFlask,GoldenUrn,Homeward,Lithograph,Oracle,Prejudice,PieceOfRelationship,RustyCommemorativeCoin,SomeonesDevice,Sunshower,TrialPlanGuide,EndlessHunger,FlowerMound,JinGangBolus,PieceOfATornSummer,TranquilLotusBolus}.java`
- Modify: `src/main/resources/assets/limbusego/lang/{zh_tw,en_us}.json`
- Modify: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionCoverageTest.java`

**Interfaces:**
- Consumes: `BaseGift.describe(int)`、`BaseGift.scaled(int, int)`、`TooltipFormat.*`。
- Produces: 12 件便利 + 5 件原創飾品的 `describe(int)` 實作。完成後 `DONE` 應含全部 80 個 id。

- [ ] **Step 1: 把本組 id 加進 `DONE`（使測試失敗）**

追加 `"blue_zippo_lighter", "child_within_a_flask", "golden_urn", "homeward", "lithograph", "oracle", "prejudice", "piece_of_relationship", "rusty_commemorative_coin", "someones_device", "sunshower", "trial_plan_guide", "endless_hunger", "flower_mound", "jin_gang_bolus", "piece_of_a_torn_summer", "tranquil_lotus_bolus"`。

同時把 `DONE` 的斷言補強——在 `GiftDescriptionCoverageTest` 加入：

```java
    @Test
    void everyRegisteredGiftIsDone() {
        var missing = new java.util.TreeSet<>(registered().keySet());
        missing.removeAll(DONE);
        assertTrue(missing.isEmpty(), "尚未撰寫說明的飾品：" + missing);
    }
```

- [ ] **Step 2: 執行測試確認失敗**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionCoverageTest"`
Expected: FAIL，17 筆缺說明

- [ ] **Step 3: 逐件實作**

便利組多為非戰鬥效果（吸取掉落物與經驗、免死回血、複製掉落、天氣增益），這類效果的觸發時機最容易讓玩家困惑，section 標題務必寫清楚是被動、擊殺時、還是條件觸發。

原創組 5 件（`endless_hunger`、`flower_mound`、`jin_gang_bolus`、`piece_of_a_torn_summer`、`tranquil_lotus_bolus`）不是 Limbus 原作內容，說明只描述實際效果即可。

- [ ] **Step 4: 加入 lang 條目**

本組 `describe(int)` 引用的每個新鍵，都依 Global Constraints 的「飾品說明轉換程序」與命名規則，同時寫進 `zh_tw.json` 與 `en_us.json`。英文不是直譯中文：section 標題用祈使／條件句（`On hit`、`While standing still`），body 用完整敘述句。

- [ ] **Step 5: 執行測試確認通過**

Run: `./gradlew.bat test`
Expected: 全部 PASS，包含新的 `everyRegisteredGiftIsDone`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/gift/ src/test/java/me/yisang/limbusego/gift/ src/main/resources/assets/limbusego/lang/
git commit -m "feat: 便利組與原創組飾品說明 / Add tooltips for the 12 qol and 5 original gifts"
```

---

### Task 15: 升級縮放驗證與收尾

**Files:**
- Create: `src/test/java/me/yisang/limbusego/gift/GiftDescriptionScalingTest.java`
- Delete: `docs/superpowers/plans/gift-desc-backup.md`
- Modify: `README.md`

**Interfaces:**
- Consumes: 全部前置 task 的產出。
- Produces: 無新 API。這是驗收 task。

- [ ] **Step 1: 寫縮放測試**

建立 `src/test/java/me/yisang/limbusego/gift/GiftDescriptionScalingTest.java`：

```java
package me.yisang.limbusego.gift;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GiftDescriptionScalingTest {

    @Test
    void restBonusMatchesActualLogicAndCaps() {
        var rest = GiftDescriptionCoverageTest.registered().get("rest");
        assertNotNull(rest);

        // 邏輯：Math.min(0.30, 0.15 * multiplier(level))
        int[] expected = new int[4];
        for (int level = 0; level <= 3; level++) {
            expected[level] = Math.round((float) Math.min(0.30, 0.15 * GiftUpgradeLogic.multiplier(level)) * 100);
        }
        assertEquals(15, expected[0], "Lv.0 應為基礎 15%");
        assertEquals(30, expected[3], "Lv.3 應被 30% 上限截斷");

        for (int level = 0; level <= 3; level++) {
            assertTrue(containsNumber(rest.describe(level), expected[level]),
                    "rest 在 Lv." + level + " 的說明應含 " + expected[level]);
        }
    }

    @Test
    void descriptionsDifferAcrossLevelsWhereScalingExists() {
        var rest = GiftDescriptionCoverageTest.registered().get("rest");
        assertNotEquals(render(rest.describe(0)), render(rest.describe(3)),
                "有縮放的飾品在 Lv.0 與 Lv.3 的說明不應相同");
    }

    private static boolean containsNumber(List<net.minecraft.text.Text> lines, int value) {
        return render(lines).contains(String.valueOf(value));
    }

    private static String render(List<net.minecraft.text.Text> lines) {
        var sb = new StringBuilder();
        lines.forEach(t -> sb.append(describeArgs(t)));
        return sb.toString();
    }

    /** Text.translatable 未翻譯時 getString() 不含參數，故直接取出參數值。 */
    private static String describeArgs(net.minecraft.text.Text text) {
        if (text.getContent() instanceof net.minecraft.text.TranslatableTextContent t) {
            var sb = new StringBuilder(t.getKey());
            for (Object arg : t.getArgs()) {
                sb.append('|').append(arg instanceof net.minecraft.text.Text nested ? describeArgs(nested) : arg);
            }
            text.getSiblings().forEach(s -> sb.append('|').append(describeArgs(s)));
            return sb.toString();
        }
        var sb = new StringBuilder(text.getString());
        text.getSiblings().forEach(s -> sb.append('|').append(describeArgs(s)));
        return sb.toString();
    }
}
```

- [ ] **Step 2: 執行測試**

Run: `./gradlew.bat test --tests "me.yisang.limbusego.gift.GiftDescriptionScalingTest"`
Expected: PASS

若 FAIL，代表 Task 9 的 `rest` 說明寫死了數值。回去改成用 `GiftUpgradeLogic.multiplier(level)` 計算。

- [ ] **Step 3: 全部測試**

Run: `./gradlew.bat test`
Expected: BUILD SUCCESSFUL，全部 PASS

- [ ] **Step 4: 遊戲內驗收**

Run: `./gradlew.bat runClient`

逐項確認（對應 spec §7）：

1. 創造頁籤翻完武器與飾品兩頁，收合時版面不臃腫，有台詞的顯示台詞、沒台詞的只有名稱。
2. 按住 Shift，每一件都給出分段機制，包含觸發方式。**特別確認玩家先前抱怨的四件：DaCapo、著影揮刀、W 公司匕首、環指筆刷。**
3. 切到英文（Options → Language → English），重新翻一次，**沒有任何一行是原始翻譯鍵、沒有中英夾雜**。
4. 用 `/limbusego` 相關指令取得殘影，在鐵砧把 `rest` 升到 Lv.2，確認 tooltip 的百分比隨之改變。
5. 拿原版鑽石劍，tooltip 與未裝模組時完全相同。

任何一項不通過就修，不要標記完成。

- [ ] **Step 5: 更新 README**

在 `README.md` 的「武器一覽」與「E.G.O 飾品一覽」章節各補一句：

```markdown
遊戲內按住 **Shift** 可展開該武器／飾品的完整機制說明；飾品的數值會反映當前殘影升級等級。
```

- [ ] **Step 6: 清理**

```bash
rm docs/superpowers/plans/gift-desc-backup.md
```

確認 `git status` 沒有殘留的暫存腳本或 `flavor.json`。

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "test: 升級縮放驗證與 tooltip 驗收 / Verify upgrade scaling and finish tooltip acceptance"
```
