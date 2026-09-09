# 飾品名稱與描述樣式設計

日期：2026-09-09
狀態：設計已通過，待寫實作計畫

## 1. 目標

把 Paper 插件的飾品配色移植過來：名稱依階級上色，風味描述恢復每件專屬的顏色。

目前 80 件飾品在創造頁籤裡是一片統一的淡紫名字配統一灰描述，看不出階級、也看不出插件原本用顏色做的區分。

### 一項更正

先前的 tooltip spec（`2026-09-09-ego-tooltips-design.md` §1 範圍界線）寫著「插件每件飾品有專屬名稱色，Fabric 版目前統一 `Rarity.EPIC`」。**這句話是錯的。**

讀 `BaseAccessory` 後確認：

```java
// 第 4 參數是 descColor —— 描述的顏色，不是名稱色
protected BaseAccessory(GiftsModule plugin, String id, String name,
                        String descColor, String description, String effect)

// 名稱一律白色，插件沒有 per-gift 名稱色
public String getDisplayName() { return plugin.color("&#FFFFFF" + localizedName()); }
```

本 spec 依實際的插件行為設計，並在完成後修正 tooltip spec 的那句描述。

### 範圍界線

**涵蓋**：飾品名稱依階級上色、46 筆描述顏色移植、呈現資料從 `ModGifts` 收攏到新的 `GiftStyles`。

**不涵蓋**：

- 飾品圖示材質
- 武器的名稱樣式（`WeaponStyles` 不動）
- 插件 lore 第一行的「階級 N」文字行（理由見 §3.4）
- 飾品的機制、數值、tooltip 內容

## 2. Repo 現況

| 事實 | 依據 |
|---|---|
| 名稱樣式必須走 `Item.getName` mixin | `ItemNameMixin` 類別註解：1.21.4 的 `Item.Settings` 會用翻譯鍵的無樣式 item_name 蓋掉手動設的 `ITEM_NAME` 元件 |
| `ItemNameMixin` 目前只問 `WeaponStyles.styledName(id)`，查無即維持原行為 | `ItemNameMixin` |
| `WeaponStyles.SPECS` 只有 14 個武器 id，飾品完全不在內 | `WeaponStyles` |
| 因此飾品名稱從未被樣式化，顯示為 `Rarity.EPIC` 的淡紫 | `ModGifts.reg()` 的 `.rarity(Rarity.EPIC)` |
| 武器同樣掛 `Rarity.EPIC` 卻能顯示自訂色 | `ModItems`：`solemn_lament_black` 為 `Rarity.EPIC`，`WeaponStyles` 給 `0x333333` |
| 風味 lore 的顏色寫死為 `Formatting.GRAY` | `ModGifts.reg()` |
| `ModGifts` 同時負責註冊與呈現（`FLAVOR_LINES` 46 筆表掛在其中） | `ModGifts` |
| 飾品階級來源為 `BaseGift.tier()`，Shift 底部標籤已在用 | `TooltipFormat.giftTag(gift.tier())` |
| 插件階級色：I 灰、II 綠、III 藍、IV 金 | `GiftsModule.TIER_COLORS = {"", "&#AAAAAA", "&#55FF55", "&#55AAFF", "&#FFD700"}` |
| 插件的 46 筆 `descColor` 全為合法 `&#RRGGBB`，共 40 種相異顏色 | 掃描 `Limbus-E.G.O/.../gifts/*.java` 統計 |

## 3. 設計決定

### 3.1 名稱依階級上色

| 階級 | 顏色 |
|---|---|
| I | `0xAAAAAA` 灰 |
| II | `0x55FF55` 綠 |
| III | `0x55AAFF` 藍 |
| IV | `0xFFD700` 金 |

數值取自插件的 `TIER_COLORS`。插件把這組顏色用在 lore 的階級行；Fabric 版把它移到名稱上——同一組設計語彙，但一眼就能在創造頁籤裡分辨階級。

**不採用「一律白色」**（插件的實際名稱色）：80 件全白的清單沒有層次，而階級色是現成且有意義的資訊。

### 3.2 走既有的 mixin 路徑

`ItemNameMixin` 改為依序詢問：

```java
Text styled = WeaponStyles.styledName(path);
if (styled == null) styled = GiftStyles.styledName(path);
if (styled != null) cir.setReturnValue(styled);
```

`GiftStyles.styledName(id)` 透過 `GiftRegistry.byId(id)` 取得 `BaseGift.tier()`，再套對應顏色。**階級不另建表**——與 Shift 底部標籤同一個來源，不可能分岔。

查無（非飾品）回傳 `null`，維持既有的「不介入」語意。

`Rarity.EPIC` 保留：武器已證明 `getName` mixin 的顏色會勝出。

### 3.3 移植 46 筆描述顏色

從插件建構子第 4 參數機械式擷取（與當初擷取風味台詞同一手法），寫成 `GiftStyles` 的 id → RGB 表。

`ModGifts.reg()` 建 lore 時改為查表，查無則退回灰色。那 34 件本來就沒有風味台詞、不掛 lore，不受影響。

**不需要任何新文案**：46 筆顏色全部來自插件原始碼。

### 3.4 不做階級 lore 行

插件在 lore 第一行放依階級上色的「階級 N」。Fabric 版的階級已經出現在 Shift 展開的底部標籤，§3.1 之後名稱顏色又傳達一次，第三份是多餘的。

### 3.5 呈現資料收攏到 `GiftStyles`

`ModGifts` 目前同時做註冊與呈現：`FLAVOR_LINES`（46 筆 id → lore 行數）掛在裡面。

新增 `GiftStyles` 後，把 `FLAVOR_LINES` 一併搬過去。`ModGifts` 回歸單純註冊，與武器側「`ModItems` 註冊、`WeaponStyles` 管樣式」的分工一致。

`GiftStyles` 的職責：飾品的**呈現**——名稱樣式、風味行數、風味顏色。

## 4. 元件

| 元件 | 位置 | 變更 |
|---|---|---|
| `GiftStyles` | `gift/` | **新增**。`styledName(id)`、`flavorLines(id)`、`flavorColor(id)`、`tierColor(tier)` |
| `ItemNameMixin` | `mixin/` | 查不到武器樣式時再問 `GiftStyles` |
| `ModGifts` | `gift/` | 移出 `FLAVOR_LINES`；lore 顏色改查 `GiftStyles.flavorColor` |

資料流：

```
Item.getName(stack)
  → ItemNameMixin
      → WeaponStyles.styledName(path)   非 null → 用它
      → GiftStyles.styledName(path)
            → GiftRegistry.byId(path).tier()
            → GiftStyles.tierColor(tier)
            → Text.translatable("item.limbusego." + path) 套色

物品註冊（ModGifts.reg）
  → GiftStyles.flavorLines(id)  > 0 才掛 LORE
      → 每行套 GiftStyles.flavorColor(id)（查無 → 灰）
```

## 5. 錯誤處理

- `GiftRegistry.byId(id)` 回傳 null（該 id 不是飾品）：`styledName` 回傳 `null`，mixin 維持原行為。
- `tier` 超出 1~4：回退灰色（比照插件 `tierColor` 的預設 `&#AAAAAA`）。
- 描述顏色查無：回退灰色，即現行外觀。

## 6. 測試

沿用「純邏輯、不載入 Minecraft」慣例。`tierColor` 不接觸 registry，可直接測試；涉及 `GiftRegistry` 的部分比照既有的 `GiftFlavorTest`，以解析 `ModGifts.java` 的方式取得 id 清單。

| 測試 | 斷言 |
|---|---|
| `GiftStylesTest.tierColorsMatchPlugin` | 1~4 分別為 `0xAAAAAA`、`0x55FF55`、`0x55AAFF`、`0xFFD700` |
| `GiftStylesTest.unknownTierFallsBackToGray` | 0 與 5 → `0xAAAAAA` |
| `GiftStylesTest.everyFlavorColorIdIsARegisteredGift` | 46 筆顏色表的 id 全部在 `ModGifts.java` 的註冊清單中 |
| `GiftStylesTest.flavorColorTableCoversEveryFlavouredGift` | 有風味台詞的 46 件都查得到顏色 |
| `GiftStylesTest.flavorColorsAreOpaqueRgb` | 46 筆值皆落在 `0x000000`~`0xFFFFFF` |

名稱在遊戲內實際的顯示顏色靠驗收。

## 7. 驗收

在遊戲內：

1. 開創造頁籤翻飾品：名稱依階級呈灰／綠／藍／金四色，一眼能分辨階級。
2. 隨機挑幾件有風味台詞的（如塵歸塵、安息），台詞顏色各不相同，且與插件一致。
3. 沒有風味台詞的 34 件：只有名稱，仍無 lore 行。
4. 武器的名稱顏色完全不受影響（莊嚴哀悼黑仍為黑、薄暝仍為金）。
5. 原版物品名稱不受影響。
6. 按住 Shift：底部的階級標籤與名稱顏色所表達的階級一致。
