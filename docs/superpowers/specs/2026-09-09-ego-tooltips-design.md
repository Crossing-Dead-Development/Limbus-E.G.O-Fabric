# E.G.O 武器／飾品 Tooltip 設計

日期：2026-09-09
狀態：設計已通過，待寫實作計畫

## 1. 目標

讓玩家看得懂手上的東西在幹嘛。

玩家回饋顯示：DaCapo、著影揮刀、W 公司匕首「不清楚作用」；環指筆刷的前衝要亂按才會發現；莊嚴哀悼的左右鍵操作反直覺；80 件飾品「好麻煩，不試了」。

**這些機制全部都已經實作完成，只是沒有傳達給玩家。** 本 spec 不新增、不修改任何玩法或數值，只補上呈現層。

### 範圍界線

**涵蓋**：14 件武器／彈藥物品 + 80 件飾品的 Shift 展開式 tooltip。

**不涵蓋**：

- 模組內建說明書 / guidebook（另開 spec）
- 任何數值平衡調整（玩家對天退星刀蓄力速度的抱怨屬於偏好差異，不是需要 buff 的證據）
- 飾品名稱與描述顏色（插件名稱一律白色、每件有專屬**描述**色；Fabric 版當時統一 `Rarity.EPIC` 與灰色描述。屬於呈現層債，已由 `2026-09-09-gift-styling-design.md` 處理）
- E.G.O 提取機（見 `2026-08-11-ego-extractor-design.md`）

## 2. Repo 現況

| 事實 | 依據 |
|---|---|
| 全專案**沒有任何** `appendTooltip` / `ItemTooltipCallback` | `grep` 無結果 |
| 武器說明掛在 `LORE` 資料元件，鍵為 `item.limbusego.<id>.lore.<n>` | `WeaponStyles.apply()` |
| 武器 lore 大多是**風味台詞**，只有 2 把混入了機制說明 | 天退星刀 `.lore.1`、薄暝 `.lore.1`／`.lore.2` |
| 飾品說明掛在 `LORE`，鍵為 `item.limbusego.<id>.desc`，80 筆中英文齊全 | `ModGifts.reg()` |
| 飾品 `.desc` 是**單行密集格式**，用 `｜` 分隔，永遠展開，數值為 Lv.0 固定值 | 例：`被動：免疫火焰傷害｜攻擊：施加燒傷 2·2｜攻擊燒傷中且生命低於 30% 的目標：+30% 傷害` |
| 飾品**風味台詞未被移植** | Paper 插件 `BaseAccessory` 建構子第 5 參數，Fabric 版只帶了 effect |
| 80 件飾品中 **46 件有風味台詞、34 件本來就沒有**（插件的 `BaseAccessory` 有 6 參數與 4 參數兩種建構子） | 掃描 `Limbus-E.G.O/.../gifts/*.java` 統計 |
| 46 件中英文風味文有 43 筆，缺 3 筆 | `lang/gifts/en_US.yml`：缺 `flower_mound`、`phantom_pain`、`spicebush_branch` |
| 12 屬性名稱**寫死中文 + legacy 色碼**，無翻譯鍵 | `StatusEffect` enum：`BURN("燒傷", "§6")` |
| `BaseGift.multiplier(stack)` 只讀元件，**客戶端可算** | `GiftUpgradeLogic.multiplier(stack.getOrDefault(GIFT_LEVEL, 0))` |
| `GiftRegistry` 提供 `byItem(Item)` 反查 | `GiftRegistry` |
| 武器數值住在 `WeaponEvents`，不在 item class | 各 `*Item.java` 為空殼，僅 javadoc |
| 已有純邏輯單元測試慣例（不載入 Minecraft） | `GiftUpgradeLogicTest`、`StatusStateTest`、`EnvironmentSanityLogicTest` |
| `ItemTooltipCallback` 可用 | fabric-item-api-v1 11.4.0，簽章 `getTooltip(ItemStack, Item.TooltipContext, TooltipType, List<Text>)` |

## 3. 設計決定

### 3.1 為什麼不用 `LORE` 元件

Shift 判定是純客戶端狀態（`Screen.hasShiftDown()`）。`LORE` 是伺服端資料元件，內容在物品建立時就固定、永遠顯示，無法折疊，也無法反映佩戴者當下的升級等級。

改用 Fabric 的客戶端 `ItemTooltipCallback`：一個 listener 覆蓋全部物品，不必去改 80 個 gift class 的註冊，也不動 `WeaponStyles` 的名稱 mixin。

查不到說明的物品完全不處理，原版物品不受影響。

### 3.2 版面

**收合（未按 Shift）**：風味台詞 + 一行灰字提示列。武器與飾品格式一致；沒有風味台詞的 34 件飾品（見 §3.5）只顯示名稱 + 提示列。

```
環指筆刷
不及格。

[Shift] 查看詳細資訊
```

**展開（按住 Shift）**：分段標題 + 精確數值 + 底部標籤。

```
環指筆刷
不及格。

▸ 筆刷一擊（右鍵）
  對目標造成 3.5 傷害
  施加隨機負面效果
  施加隨機屬性 威力2・次數3
▸ 雙擊（1.5 秒內再次右鍵同一目標）
  效果觸發 2 次
▸ 前衝（右鍵未命中目標）
  向前突進一段距離

E.G.O
```

飾品底部標籤為 `E.G.O 飾品・階級 I`（階級讀 `BaseGift.tier()`）。

段落標題金色、內文灰色縮排兩格、屬性名稱套用 `StatusEffect` 既有顏色、數值高亮。集中在 `TooltipFormat` 小工具類，避免 80 個 `describe()` 各自拼樣式。

### 3.3 說明資料住在哪裡

**飾品 → 每個 class 自己描述。**

`BaseGift` 新增鉤子：

```java
/** 供 tooltip 呼叫；由 stack 取出升級等級後轉呼 describe(int)。 */
public final List<Text> describe(ItemStack self) {
    return describe(self.getOrDefault(ModComponents.GIFT_LEVEL, 0));
}

/** 回傳此飾品在指定升級等級（0~3）的 Shift 展開說明。預設空清單。 */
public List<Text> describe(int level) { return List.of(); }
```

**子類覆寫的是 `describe(int level)`，不是 `describe(ItemStack)`。** 這個切分讓說明測試完全不需要 `ItemStack`，因而不需要 `Bootstrap.initialize()`——後者會凍結 registry、導致飾品註冊失敗（見 §6）。

有升級縮放的直接用 `GiftUpgradeLogic.multiplier(level)`，讓 tooltip 顯示這一件飾品此刻的真實數值：

```java
// Rest.java — 邏輯與說明寫在同一個檔案、相隔數行
@Override
protected float onAttack(...) {
    if (has(target, StatusEffect.SINKING)) dmg *= (float) (1.0 + Math.min(0.30, 0.15 * multiplier(self)));
    return dmg;
}

@Override
public List<Text> describe(int level) {
    int pct = Math.round((float) Math.min(0.30, 0.15 * GiftUpgradeLogic.multiplier(level)) * 100);
    return List.of(
        TooltipFormat.section("tooltip.limbusego.rest.passive"),
        TooltipFormat.body("tooltip.limbusego.rest.passive.regen"),
        TooltipFormat.section("tooltip.limbusego.rest.attack"),
        TooltipFormat.body("tooltip.limbusego.rest.attack.bonus", pct));
}
```

理由：tooltip 最常見的失敗模式是隨時間變成謊言。改平衡時數值就在正上方幾行，很難漏改。

**武器 → 一張集中表。**

武器數值住在 `WeaponEvents`，per-class `describe()` 得不到共置的好處，反而把說明散進 11 個空殼 class。改用 `WeaponTooltips`（id → 說明行）集中表，放在 `WeaponStyles` 旁邊，與現有呈現層慣例一致。

### 3.4 現有 lore 的處理

| 物品 | 處理 |
|---|---|
| 武器風味台詞 `.lore.0` | **保留不動**，收合時顯示的就是它 |
| 天退星刀 `.lore.1`、薄暝 `.lore.1`／`.lore.2` | **移除**（含 `WeaponStyles.SPECS` 的對應 `loreColors` 項），內容改寫進 `WeaponTooltips`，否則收合時會與展開內容重複 |
| 飾品 `.desc`（80 筆密集單行） | **從 `ModGifts.reg()` 的 `LORE` 元件移除**，內容拆成結構化行搬進各 class 的 `describe()`。lang 鍵 `.desc` 一併刪除 |
| 飾品風味台詞 | **新增**。從 Paper 插件 `BaseAccessory` 建構子第 5 參數機械式擷取，寫入 `item.limbusego.<id>.lore.0`，並依 3.5 掛上 `LORE` 元件 |

### 3.5 飾品風味台詞的移植

Paper 插件每個飾品建構子帶有名稱色與風味台詞：

```java
super(plugin, "ashes_to_ashes", "塵歸塵",
        "&#9A9A9A", "步入迷霧。",
        "攻擊燒傷中目標：疊加燒傷 2·1");
```

實際盤點（80 件 Fabric 飾品）：

| 類別 | 數量 | 處理 |
|---|---|---|
| 有中文風味文 | 46 | 從插件建構子第 5 參數擷取 |
| 其中有英文風味文 | 43 | 從 `lang/gifts/en_US.yml` 的 `description` 擷取，需去掉 `&#RRGGBB` 前綴 |
| 缺英文風味文 | 3 | `flower_mound`、`phantom_pain`、`spicebush_branch`，需補譯 |
| 本來就沒有風味文（插件用 4 參數建構子） | 34 | **不補寫**。收合時只顯示名稱 + 提示列 |

原創文案總量僅 3 行英文。34 件無風味文的飾品維持空白是刻意的：插件當初就沒寫，硬補會是憑空杜撰。

`ModGifts.reg()` 的 `LORE` 元件從 `.desc` 改掛 `.lore.0`，樣式沿用現有的灰色非斜體。

名稱顏色（`&#9A9A9A`）**不在本 spec 範圍**。

### 3.6 12 屬性的翻譯鍵

`StatusEffect` 目前寫死中文，英文玩家的 tooltip 會夾雜中文。

新增：

```java
BURN("燒傷", "§6");
public String translationKey() { return "status.limbusego." + name().toLowerCase(Locale.ROOT); }
```

`zh`／`color` 欄位保留不動（既有 HUD／訊息仍在用）。lang 補 12 × 2 筆。

## 4. 元件

| 元件 | 位置 | 職責 |
|---|---|---|
| `EgoTooltipHandler` | `client/` | 註冊 `ItemTooltipCallback`；判斷 Shift；查表；插行 |
| `TooltipFormat` | `client/` | 段落／內文／提示列／屬性名稱的樣式與翻譯鍵包裝 |
| `WeaponTooltips` | `item/` | id → 武器說明行的集中表 |
| `BaseGift.describe(int level)` | `gift/` | 飾品說明鉤子，80 個子類覆寫 |
| `StatusEffect.translationKey()` | `status/` | 屬性名稱在地化 |

資料流：

```
ItemTooltipCallback
  ├─ GiftRegistry.byItem(item) ─→ BaseGift.describe(stack) ─→ describe(level) ─→ List<Text>
  └─ WeaponTooltips.of(itemId)  ─────────────────────────────→ List<Text>
                                                                  │
                              Screen.hasShiftDown() ? 插入內容 : 插入提示列
```

## 5. 錯誤處理

- 查不到說明 → 不插入任何東西，包含提示列。原版物品與其他模組物品完全不受影響。
- `describe()` 回傳空清單 → 視同查不到，不顯示提示列（避免按了 Shift 什麼都沒有）。
- 缺翻譯鍵 → Minecraft 原生行為顯示鍵名本身，由 §6 的測試在 CI 攔下。

## 6. 測試

沿用既有「純邏輯、不載入 Minecraft」慣例，這由兩個設計決定達成：

1. 子類覆寫 `describe(int level)`（§3.3），測試不需要 `ItemStack`。
2. 測試**不使用 `GiftRegistry`**（填充它需要註冊物品，會碰 `Registries`）。改為讀取 `ModGifts.java` 原始碼、用正規式抽出 `reg("<id>", new <Class>())` 配對，再以反射實例化。這樣測試涵蓋的正好是實際註冊的那 80 件，新增飾品時自動納入。

**不要呼叫 `Bootstrap.initialize()`**：它會凍結 registry，反而讓飾品註冊失敗。`Text.translatable()` 與 `Style` 不需要 bootstrap。

| 測試 | 斷言 |
|---|---|
| `GiftDescriptionCoverageTest` | 反射實例化 `ModGifts.java` 註冊的 80 件，每件 `describe(0)` 非空 |
| `TooltipLangCoverageTest` | `describe()`／`WeaponTooltips` 引用的每個翻譯鍵，在 `en_us.json` 與 `zh_tw.json` 都存在 |
| `GiftDescriptionScalingTest` | 抽樣有升級縮放的飾品（如 `rest`），驗證 `describe(0..3)` 數值與實際邏輯一致，且 30% 上限有生效 |

第二項是 80 件 × 中英文這種量級最重要的防線：之後新增飾品忘了寫說明或漏翻譯會直接紅燈。

第三項防的是「tooltip 說 30% 但程式算 22.5%」這種靜默偏差。

## 7. 驗收

在遊戲內：

1. 創造頁籤翻武器與飾品，每一件收合時都有風味台詞 + 提示列，版面不臃腫。
2. 按住 Shift，每一件都給出分段的具體機制，包含觸發方式。
3. 中英文切換，沒有任何一行是原始翻譯鍵、沒有中英夾雜。
4. 拿一件有縮放的飾品在殘影鐵砧升到 Lv.2，tooltip 數值隨之改變。
5. 手上拿原版鑽石劍，tooltip 與未裝模組時完全相同。
