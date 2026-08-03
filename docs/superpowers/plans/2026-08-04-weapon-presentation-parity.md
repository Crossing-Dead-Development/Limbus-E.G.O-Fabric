# 武器呈現層對齊插件版 實作計畫（名稱＋Lore＋樣式）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 讓 13 個武器物品的顯示名稱帶顏色＋粗體、並顯示雙語 lore，對齊 `Limbus-E.G.O` 插件的呈現。

**Architecture:** 新增 `WeaponStyles` 集中對照表（id → 名稱顏色/粗體 + 各行 lore 顏色），在 `ModItems` 註冊時對每個武器 `Item.Settings` 掛上 `ITEM_NAME` 與 `LORE` 兩個 Data Component（文字走翻譯鍵保留雙語，顏色由 component 疊 `Style`）。lang JSON 補齊名稱修正與 lore 鍵。

**Tech Stack:** Fabric 1.21.4、Java 21、Minecraft Data Components（`ITEM_NAME`/`LORE`）、`Text.translatable` + `Style`。

## Global Constraints

- mod id：`limbusego`；翻譯鍵前綴 `item.limbusego.<id>`（與現有一致）
- 真相來源：`Limbus-E.G.O` v1.4.1 的 `lang/weapons/{zh_TW,en_US}.yml`（僅取純文字，去除 `&`/`&#`/`&x` 顏色碼；顏色改由 component 套用）
- 不做逐字漸層：漸層項以代表色近似（solemn.lore 用 `#D8D8D8`）
- 無單元測試框架：每個實作任務以 `./gradlew build`（Git Bash 用 `./gradlew.bat`）編譯通過為自動檢查，最後一個任務做遊戲內目視驗證
- 排除插翅虎（chatuhu）／終末鳥（apocalypse）組合包道具
- 顏色以 `TextColor.fromRgb(0xRRGGBB)` 表示；粗體 `Style.withBold(true)`；所有名稱與 lore 一律 `.withItalic(false)`

## 武器樣式對照（權威資料，供 Task 1／Task 3 使用）

| id | 名稱色 | 粗體 | lore 行數與各行色 |
|---|---|---|---|
| solemn_lament_black | 0x333333 | 是 | 1：0xD8D8D8 |
| solemn_lament_white | 0xFFFFFF | 是 | 1：0xD8D8D8 |
| solemn_shield | 0xFFFFFF | 是 | 1：0xD8D8D8 |
| mimicry | 0xFF0000 | 否 | 1：0xFF0000 |
| dacapo | 0xFFFFFF | 否 | 1：0xFFFFFF |
| ring_brush | 0xFFFFFF | 否 | 1：0xFF9500 |
| tiantui_star | 0xE67E22 | 否 | 2：0xAAAAAA, 0x555555 |
| tiger_mark | 0xE67E22 | 否 | 1：0xAAAAAA |
| savage_tiger_mark | 0xC0392B | 否 | 1：0xAAAAAA |
| twilight | 0xFFD700 | 否 | 3：0xAAAAAA, 0x555555, 0x555555 |
| tibia | 0x8B0000 | 否 | 1：0xAAAAAA |
| w_corp_knife | 0x66E1FF | 否 | 1：0xB3F0FF |
| bladesinger | 0xAEDBFF | 否 | 1：0xD0E7FF |

---

## Task 1: 新增 `WeaponStyles` 集中對照與套用工具

**Files:**
- Create: `src/main/java/me/yisang/limbusego/item/WeaponStyles.java`

**Interfaces:**
- Produces: `WeaponStyles.apply(Item.Settings s, String id) -> Item.Settings`（對已知武器 id 掛 `ITEM_NAME`＋`LORE` component；未知 id 原樣返回）

- [ ] **Step 1: 建立 `WeaponStyles.java`**

```java
package me.yisang.limbusego.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 武器名稱／lore 樣式集中對照。文字走翻譯鍵（雙語），顏色與粗體由此掛上 component。
 * 對照來源：Limbus-E.G.O 插件 lang/weapons。漸層項以代表色近似。
 */
public final class WeaponStyles {

    private WeaponStyles() {}

    /** 一把武器的樣式：名稱色、是否粗體、各行 lore 顏色（長度＝lore 行數）。 */
    private record Spec(int nameColor, boolean bold, int[] loreColors) {}

    private static final Map<String, Spec> SPECS = Map.ofEntries(
        Map.entry("solemn_lament_black", new Spec(0x333333, true,  new int[]{0xD8D8D8})),
        Map.entry("solemn_lament_white", new Spec(0xFFFFFF, true,  new int[]{0xD8D8D8})),
        Map.entry("solemn_shield",       new Spec(0xFFFFFF, true,  new int[]{0xD8D8D8})),
        Map.entry("mimicry",             new Spec(0xFF0000, false, new int[]{0xFF0000})),
        Map.entry("dacapo",              new Spec(0xFFFFFF, false, new int[]{0xFFFFFF})),
        Map.entry("ring_brush",          new Spec(0xFFFFFF, false, new int[]{0xFF9500})),
        Map.entry("tiantui_star",        new Spec(0xE67E22, false, new int[]{0xAAAAAA, 0x555555})),
        Map.entry("tiger_mark",          new Spec(0xE67E22, false, new int[]{0xAAAAAA})),
        Map.entry("savage_tiger_mark",   new Spec(0xC0392B, false, new int[]{0xAAAAAA})),
        Map.entry("twilight",            new Spec(0xFFD700, false, new int[]{0xAAAAAA, 0x555555, 0x555555})),
        Map.entry("tibia",               new Spec(0x8B0000, false, new int[]{0xAAAAAA})),
        Map.entry("w_corp_knife",        new Spec(0x66E1FF, false, new int[]{0xB3F0FF})),
        Map.entry("bladesinger",         new Spec(0xAEDBFF, false, new int[]{0xD0E7FF}))
    );

    /** 對已知武器 id 掛上 ITEM_NAME 與 LORE component。未知 id 原樣返回。 */
    public static Item.Settings apply(Item.Settings s, String id) {
        Spec spec = SPECS.get(id);
        if (spec == null) return s;

        Style nameStyle = Style.EMPTY
                .withColor(TextColor.fromRgb(spec.nameColor))
                .withBold(spec.bold)
                .withItalic(false);
        Text name = Text.translatable("item.limbusego." + id).setStyle(nameStyle);
        s.component(DataComponentTypes.ITEM_NAME, name);

        int[] colors = spec.loreColors;
        if (colors.length > 0) {
            List<Text> lore = new ArrayList<>(colors.length);
            for (int i = 0; i < colors.length; i++) {
                Style ls = Style.EMPTY.withColor(TextColor.fromRgb(colors[i])).withItalic(false);
                lore.add(Text.translatable("item.limbusego." + id + ".lore." + i).setStyle(ls));
            }
            s.component(DataComponentTypes.LORE, new LoreComponent(lore));
        }
        return s;
    }
}
```

- [ ] **Step 2: 編譯確認類別可建置**

Run: `./gradlew.bat compileJava -q`
Expected: 無錯誤結束（exit 0）。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/me/yisang/limbusego/item/WeaponStyles.java
git commit -m "feat: 新增 WeaponStyles 武器名稱/lore 樣式集中對照 / Add WeaponStyles"
```

---

## Task 2: `ModItems` 註冊時套用 `WeaponStyles`

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/item/ModItems.java`

**Interfaces:**
- Consumes: `WeaponStyles.apply(Item.Settings, String)`（Task 1）

**背景：** `ModItems.register()` 對每個武器以 `key("<id>")…` 建 `Item.Settings` 再包成 `Item` 或子類別。將每個武器的 `key("<id>")` 外層包上 `WeaponStyles.apply(key("<id>"), "<id>")`，其餘 `.maxCount/.rarity/.component(...)` 鏈式呼叫不變。

- [ ] **Step 1: 對 13 個武器 id 套用**

對下列每個武器，將其 `Item.Settings` 起點由 `key("<id>")` 改為 `WeaponStyles.apply(key("<id>"), "<id>")`：
`solemn_lament_black`, `solemn_lament_white`, `solemn_shield`, `mimicry`, `dacapo`, `ring_brush`, `tiantui_star`, `tiger_mark`, `savage_tiger_mark`, `twilight`, `tibia`, `w_corp_knife`, `bladesinger`。

範例（solemn_lament_black，其餘同理）：

```java
// 之前：
SOLEMN_LAMENT_BLACK = reg("solemn_lament_black",
        new SolemnLamentItem(true, key("solemn_lament_black").maxCount(1).rarity(Rarity.EPIC)));
// 之後：
SOLEMN_LAMENT_BLACK = reg("solemn_lament_black",
        new SolemnLamentItem(true, WeaponStyles.apply(key("solemn_lament_black"), "solemn_lament_black")
                .maxCount(1).rarity(Rarity.EPIC)));
```

若某武器目前用 `new Item(key("<id>")…)`（如 tiger_mark/savage_tiger_mark），同樣把 `key("<id>")` 包成 `WeaponStyles.apply(key("<id>"), "<id>")`。**不要**改 `MOD_ICON`、`butterfly_quartz` 等非武器物品。

- [ ] **Step 2: 編譯確認**

Run: `./gradlew.bat compileJava -q`
Expected: exit 0，無錯誤。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/me/yisang/limbusego/item/ModItems.java
git commit -m "feat: 武器註冊套用 WeaponStyles 名稱/lore component / Apply WeaponStyles at weapon registration"
```

---

## Task 3: 補齊雙語 lang（名稱修正＋lore 鍵）

**Files:**
- Modify: `src/main/resources/assets/limbusego/lang/zh_tw.json`
- Modify: `src/main/resources/assets/limbusego/lang/en_us.json`

**背景：** 名稱鍵 `item.limbusego.<id>` 已存在，需修正部分文字（去顏色碼、對齊插件、黑白改以顏色區分）。新增 lore 鍵 `item.limbusego.<id>.lore.<N>`（N 由 0 起）。

- [ ] **Step 1: 修正 zh_tw.json 名稱值**

將下列鍵的值改為（純文字，無顏色碼）：

```json
"item.limbusego.solemn_lament_black": "莊嚴哀悼",
"item.limbusego.solemn_lament_white": "莊嚴哀悼",
"item.limbusego.solemn_shield": "聖宣",
"item.limbusego.w_corp_knife": "W公司 匕首",
```

其餘武器名稱維持現值（已正確）：`擬態`、`DaCapo`、`環指筆刷`、`天退星刀`、`虎標彈`、`猛虎標彈`、`薄暝`、`提比婭`、`著影揮刀`。

- [ ] **Step 2: 在 zh_tw.json 新增 lore 鍵**

```json
"item.limbusego.solemn_lament_black.lore.0": "人死後會去往何方？",
"item.limbusego.solemn_lament_white.lore.0": "人死後會去往何方？",
"item.limbusego.solemn_shield.lore.0": "人死後會去往何方？",
"item.limbusego.mimicry.lore.0": "而那裡有許多聲音齊聲哭喊著同一個字──「主管」",
"item.limbusego.dacapo.lore.0": "來自廢墟的最華麗的演出，即將拉開帷幕！",
"item.limbusego.ring_brush.lore.0": "不及格。",
"item.limbusego.tiantui_star.lore.0": "填入虎標彈，蓄勢，化作奔虎。",
"item.limbusego.tiantui_star.lore.1": "右鍵：虎標彈衝刺　潛行右鍵：猛虎標彈衝刺",
"item.limbusego.tiger_mark.lore.0": "助推填充火藥。",
"item.limbusego.savage_tiger_mark.lore.0": "更猛烈的助推火藥。",
"item.limbusego.twilight.lore.0": "終末將至，黃昏執刃。",
"item.limbusego.twilight.lore.1": "越接近死亡，斬擊越致命；部分傷害無視防禦。",
"item.limbusego.twilight.lore.2": "潛行右鍵：蓄力暮光斬（前方扇形波）",
"item.limbusego.tibia.lore.0": "聽見了嗎？那由提比婭的一對肱骨與二十四根肋骨奏響的旋律！",
"item.limbusego.w_corp_knife.lore.0": "至終，此為通路。",
"item.limbusego.bladesinger.lore.0": "望月斬首──就此，氣絕吧。"
```

- [ ] **Step 3: 修正 en_us.json 名稱值**

```json
"item.limbusego.solemn_lament_black": "Solemn Lament",
"item.limbusego.solemn_lament_white": "Solemn Lament",
"item.limbusego.solemn_shield": "Sanctification",
"item.limbusego.w_corp_knife": "W Corp. Dagger",
```

其餘武器名稱對齊插件 en：`Mimicry`、`DaCapo`、`Ring-Bearing Brush`、`Tiantui Star Blade`、`Tiger Round`、`Savage Tiger Round`、`Twilight`、`Tibia`、`Shadow-Vested Bladesinger`（若現值不同則一併修正）。

- [ ] **Step 4: 在 en_us.json 新增 lore 鍵**

```json
"item.limbusego.solemn_lament_black.lore.0": "Where do the dead go?",
"item.limbusego.solemn_lament_white.lore.0": "Where do the dead go?",
"item.limbusego.solemn_shield.lore.0": "Where do the dead go?",
"item.limbusego.mimicry.lore.0": "And there, many voices cried the same word in unison — \"Manager\"",
"item.limbusego.dacapo.lore.0": "From the ruins, the most magnificent performance is about to begin!",
"item.limbusego.ring_brush.lore.0": "Failing grade.",
"item.limbusego.tiantui_star.lore.0": "Load a Tiger Round, gather momentum, and burst forth like a running tiger.",
"item.limbusego.tiantui_star.lore.1": "Right-click: Tiger dash  Sneak + right-click: Savage tiger dash",
"item.limbusego.tiger_mark.lore.0": "Propellant powder.",
"item.limbusego.savage_tiger_mark.lore.0": "A fiercer propellant powder.",
"item.limbusego.twilight.lore.0": "The end draws near; dusk takes up the blade.",
"item.limbusego.twilight.lore.1": "Closer to death, deadlier the strike; part of the damage ignores defense.",
"item.limbusego.twilight.lore.2": "Sneak + right-click: charge the Twilight Slash (fan-shaped wave)",
"item.limbusego.tibia.lore.0": "Can you hear it? The melody played by Tibia's pair of humeri and twenty-four ribs!",
"item.limbusego.w_corp_knife.lore.0": "In the end, this is the path.",
"item.limbusego.bladesinger.lore.0": "Moon-gazing decapitation — expire, here and now."
```

- [ ] **Step 5: 驗證兩個 JSON 合法**

Run: `python -c "import json; json.load(open('src/main/resources/assets/limbusego/lang/zh_tw.json',encoding='utf-8')); json.load(open('src/main/resources/assets/limbusego/lang/en_us.json',encoding='utf-8')); print('OK')"`
Expected: 輸出 `OK`（無 JSON 解析錯誤）。

- [ ] **Step 6: Commit**

```bash
git add src/main/resources/assets/limbusego/lang/zh_tw.json src/main/resources/assets/limbusego/lang/en_us.json
git commit -m "feat: 武器名稱修正＋雙語 lore（對齊插件）/ Fix weapon names + bilingual lore"
```

---

## Task 4: 建置、部署與遊戲內驗證

**Files:** 無（驗證任務）

- [ ] **Step 1: 完整建置**

Run: `./gradlew.bat build -q`
Expected: exit 0；`build/libs/limbus-ego-fabric-0.1.0.jar` 產生。

- [ ] **Step 2: 部署到 LSMP profile**

```bash
cp build/libs/limbus-ego-fabric-0.1.0.jar "/c/Users/User/AppData/Roaming/ModrinthApp/profiles/LSMP/mods/limbus-ego-fabric-0.1.0.jar"
```

- [ ] **Step 3: 遊戲內目視驗證（手動）**

關閉遊戲後重開，進創造模式「E.G.O 武器」頁籤，逐一確認：
- 名稱顏色與粗體：solemn 黑（深灰粗體）/白（白粗體）、聖宣（白粗體）、擬態（紅）、天退星刀（橘）、薄暝（金）、提比婭（暗紅）、W公司匕首（青）、著影揮刀（淺藍）。
- lore 顯示於名稱下方、無斜體、雙語隨語言切換正確。
- tiantui_star 顯示 2 行、twilight 顯示 3 行 lore。

- [ ] **Step 4: Commit（若上述無需修正）**

無程式碼變更則略過；如遊戲內發現色碼或文字錯誤，回到對應 Task 修正後再建置。

---

## Self-Review 註記

- **Spec 覆蓋**：名稱（Task 1 樣式＋Task 3 文字）、lore（Task 1 component＋Task 3 文字）、雙語（Task 3 兩檔）、集中管理（Task 1 `WeaponStyles`）、漸層近似（樣式表 solemn.lore=0xD8D8D8）、去斜體（`withItalic(false)`）均涵蓋。
- **行為與音效**不在本計畫（屬計畫二，將另行撰寫）。
- **型別一致**：`WeaponStyles.apply(Item.Settings, String)` 於 Task 1 定義、Task 2 使用，簽章一致。
