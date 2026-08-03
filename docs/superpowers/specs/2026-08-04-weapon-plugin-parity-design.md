# 武器對齊插件版設計 — 名稱／描述／行為／音效

日期：2026-08-04
狀態：設計已通過，待寫實作計畫

## 1. 目標

把 Fabric 模組 `limbusego` 的**武器**在四個面向對齊 Paper 插件，達到與插件版一致的呈現與表現：

1. **名稱** — 顏色＋粗體
2. **描述（lore）** — 風味＋機制說明，多行，雙語
3. **行為（表現）** — 傷害／冷卻／機率／狀態效果／投射物數值逐把核對
4. **音效** — 音效 id／命名空間／音量／音高對齊

### 真相來源

`Limbus-E.G.O`（合併版插件，v1.4.1，package `me.yisang.limbusego`）：

- 名稱／lore：`src/main/resources/lang/weapons/{zh_TW,en_US}.yml`
- 行為／音效：各武器 `.java`（`solemnlament.java`、`dacapo.java`、`mimicry.java`、`TwilightWeapon.java`、`TibiaWeapon.java`、`ShadowBladesinger.java` 等）

> 註：本機另有獨立 `Limbus E.G.O Weapons` v3.2.0，**不採用**；一律以 `Limbus-E.G.O` 為準。

## 2. 範圍

對齊武器（排除已明定不移植的插翅虎 chatuhu／終末鳥 apocalypse 組合包道具）：

| Fabric 物品 | 插件鍵 | 名稱樣式 | Lore |
|---|---|---|---|
| solemn_lament_black | solemn.black | `#333333` 粗體 | solemn.lore |
| solemn_lament_white | solemn.white | `#FFFFFF` 粗體 | solemn.lore |
| solemn_shield | solemn.shield | `#FFFFFF` 粗體 | solemn.lore |
| mimicry | mimicry | `#FF0000` | 單行 |
| dacapo | dacapo | `#FFFFFF` | 單行 |
| ring_brush | brush | `#FFFFFF` | 單行 |
| tiantui_star | tiantui | `#E67E22` | 2 行機制 |
| tiger_mark | tiantui.tiger_mark | `#E67E22` | 單行 |
| savage_tiger_mark | tiantui.savage | `#C0392B` | 單行 |
| twilight | twilight | `#FFD700` | 3 行機制 |
| tibia | tibia | `#8B0000` | 單行 |
| w_corp_knife | w_corp_knife | `#66E1FF` | 單行 |
| bladesinger | bladesinger | `#AEDBFF` | 單行 |

**butterflies**（生蝶／亡蝶）為 solemn 召喚物內部物品，若 Fabric 有對應物品則一併，否則略。

## 3. 名稱與 Lore 技術做法

Minecraft Java 物品名稱來自翻譯鍵（純文字）。為同時保留**雙語**與**上色**，用 **Data Component** 在註冊時掛樣式，文字仍走翻譯鍵：

- **名稱**：`DataComponentTypes.ITEM_NAME` = `Text.translatable("item.limbusego.<id>")` 疊 `Style`（顏色＋粗體）。整串單一顏色。
- **Lore**：`DataComponentTypes.LORE` = 各行 `Text.translatable("item.limbusego.<id>.lore.N")` 疊 `Style`，並 `.withItalic(false)` 去除原版斜體。

### 顏色保真取捨

- **基準：單色＋粗體**，用單一 `Style` 完美重現插件多數名稱（solemn 黑/白、twilight、tibia、w_corp_knife、bladesinger…）。
- **漸層**（butterflies 名稱、solemn.lore「人死後…」等逐字漸層）翻譯鍵無法逐字上色 → 用**代表色近似**（butterflies 白、solemn.lore 淺灰 `#D8D8D8`）。不為漸層放棄雙語。

### 集中管理

新增 `WeaponStyles`（id → 顏色、是否粗體、lore 行數）單一對照表，註冊時統一套用 ITEM_NAME／LORE component，避免樣式散落各處。

### Lang 補齊

`assets/limbusego/lang/{zh_tw,en_us}.json` 補上：

- 名稱鍵維持既有 `item.limbusego.<id>`，文字對齊插件（去掉插件顏色碼，只留純文字；顏色改由 component 上）。
- 新增 lore 鍵 `item.limbusego.<id>.lore.<N>`，內容取自插件 lore（去顏色碼）。
- 名稱文字差異需修正處：如 solemn 黑/白統一為「莊嚴哀悼」（黑白靠顏色區分，不用（黑）/（白）文字後綴）、solemn_shield「聖宣」、w_corp_knife「W公司 匕首」。

## 4. 行為核對方法

逐把把插件 `.java` 關鍵數值抽出，對照 Fabric `WeaponEvents.java`（＋各 Item 類別），列差異表後只修**確有差異**者（不動既有可運作邏輯）。

固定核對項目：

- **傷害**：基礎／派生／真傷比例
- **冷卻**：tick／秒
- **觸發機率／消耗**：彈藥、蓄力、層數上限
- **狀態效果**：種類、層數、持續
- **投射物／衝刺**：速度、存活時間、命中判定

每把武器輸出一列「插件值 → Fabric 現值 → 是否修正」。

## 5. 音效對齊方法

抽出插件所有 `playSound("<ns>:<id>", vol, pitch)`，對照 `ModSounds` 常數與 `WeaponEvents` 播放呼叫，確認：

1. **id／命名空間**一致（插件用 `solemnlament:solemn.hit` 等）
2. Fabric `sounds.json` 有註冊、`.ogg` 存在（音檔可從插件 resourcepack 的 `assets/<ns>/sounds/` 搬入 mod）
3. **音量／音高**參數一致

## 6. 執行順序

1. 建 `WeaponStyles` 對照表
2. 補齊 lang（名稱修正＋lore 雙語）
3. 註冊時掛 ITEM_NAME／LORE component
4. 逐把做行為差異表 → 修正
5. 逐把做音效差異表 → 修正（含補音檔／sounds.json）
6. 編譯部署、遊戲內驗證

## 7. 驗證

每把武器編譯後於遊戲內確認：名稱顏色與粗體、lore 顯示與斜體去除、關鍵技能表現與音效觸發。

## 8. 非目標

- 不移植插翅虎／終末鳥組合包道具
- 不做逐字漸層（以代表色近似）
- 不重寫既有可運作的行為邏輯，只修與插件有差異處
