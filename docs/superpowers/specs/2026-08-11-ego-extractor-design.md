# E.G.O 提取機設計

日期：2026-08-11
狀態：已實作（計畫：docs/superpowers/plans/2026-09-11-ego-extractor.md）

## 1. 目標

讓 Fabric 版的 80 件 E.G.O 飾品有一條**單機可玩的正規取得管道**：打怪產出材料 → 投入提取機 → 隨機取得指定階級的飾品。

現況是飾品與殘影**只能靠 `/limbusego gift give` 指令取得**——沒有 loot table、沒有合成、沒有任何遊戲內產出。這代表單人或小型存檔實質上玩不到飾品系統，鐵砧升級（`GiftUpgrade`）也因為殘影拿不到而形同虛設。本 spec 一次補上這兩個缺口。

**原則：新增的隨機性與平衡數值全部隔離在無 Minecraft 依賴的純函式裡**，照 `GiftUpgradeLogic`、`EnvironmentSanityLogic`、`StatusState` 的既有慣例，可用 JUnit 直接驗證。

### 範圍界線

本 spec 只涵蓋**提取機**（方塊、GUI、抽獎邏輯、材料掉落）。Paper 版另有紡錘抽獎箱與購買商店箱，不在本版範圍（見 §9）。

## 2. Repo 現況

| 事實 | 依據 |
|---|---|
| 80 件飾品已全數註冊，且每件帶階級 1~4 | `ModGifts.register()`；`BaseGift.tier()` |
| 飾品與殘影**沒有任何遊戲內取得管道** | 全 repo 無 loot table、無 `crafting_shaped` JSON；`GiftRegistry` 只被指令與 GUI 使用 |
| 殘影 4 件已註冊，階級 I~IV | `Vestiges.DARK/FAINT/TWINKLING/BRILLIANT_VESTIGE`、`Vestiges.tierOf()` |
| 鐵砧升級要求殘影階級 == 飾品階級 | `GiftUpgradeLogic.resolveUpgrade()`：`if (vestigeTier != giftTier) return -1` |
| **Repo 一個方塊都沒有** | 無 `ModBlocks`、無 `net.minecraft.block` 匯入、無 blockstate JSON |
| **客戶端進入點是空的** | `LimbusEGOClient.onInitializeClient()` 空實作 |
| 既有 GUI 全是伺服端 vanilla 容器 | `GiftGui`、`WeaponCatalogGui`、`WeaponAdminGui` 皆用 `GenericContainerScreenHandler` + 攔截點擊 |
| 純邏輯單元測試的既有慣例 | `GiftUpgradeLogicTest`、`StatusStateTest`、`EnvironmentSanityLogicTest`（皆不載入 Minecraft） |

本 spec 要開的新技術路線有四條：**Block／BlockEntity＋ScreenHandler／客戶端 Screen／loot table 注入**。

## 3. 設計決定

### 3.1 經濟：單一貨幣 ＋ 階級觸媒

- **Enkephalin（腦啡肽）** `limbusego:enkephalin`：通用貨幣，唯一的新物品。堆疊上限 64。
- **殘影**（現有 4 件）：階級觸媒，一次提取消耗 1 個，**決定產出階級**。

觸媒**沿用現有殘影而非新開核心素材**。理由：

1. 殘影本來就分 I~IV 階，且 `GiftUpgradeLogic` 已經建立「殘影階級 == 飾品階級」的語意，投入 twinkling（III）抽 Tier III 池完全一致，玩家不必學第二套階級對照。
2. 殘影目前也沒有取得管道，共用掉落注入等於一次補兩個缺口。
3. 少開 4 件新物品與 4 張材質。

**接受的後果**：升級既有飾品與抽新飾品會搶同一份殘影。這是刻意的資源取捨，不是缺陷。

### 3.2 消耗與時間

| 產出階級 | 觸媒 | Enkephalin | 提取時間 |
|---|---|---|---|
| I | dark_vestige | 8 | 5 秒（100 tick） |
| II | faint_vestige | 16 | 8 秒（160 tick） |
| III | twinkling_vestige | 32 | 12 秒（240 tick） |
| IV | brilliant_vestige | 64 | 20 秒（400 tick） |

Tier III 的 32 個 Enkephalin 對齊 Paper 版 `gacha.lunacy-cost: 32`，其餘按階級等比擴散。

### 3.3 掉落注入

用 `LootTableEvents.MODIFY` 動態附加掉落池，**不覆寫任何原生 loot table JSON**，與其他 mod 的相容性最好。

| 材料 | 來源 | 機率 |
|---|---|---|
| Enkephalin ×1 | **所有生物**（敵對＋溫和） | 25% |
| dark_vestige（I） | 一般敵對生物（殭屍、骷髏、蜘蛛、苦力怕…） | 2% |
| faint_vestige（II） | 地獄／掠奪者類（豬布林、烈焰人、掠奪者、疣豬獸…） | 5% |
| twinkling_vestige（III） | 終界人、凋零骷髏、喚魔者 | 8% |
| brilliant_vestige（IV） | 終界龍、凋零、監守者 | 100% |

兩點刻意的取捨：

- **溫和生物也掉 Enkephalin**，代表動物農場會成為穩定產線。這與主題相合（Enkephalin 是可量產的能量單位，不是稀有戰利品），且階級鎖在殘影上，農場最多加速 Tier I~II。
- **Tier IV 只從三個 Boss 掉**（Paper 版是 10% 隨機出 T4）。改成「打贏 Boss 才碰得到頂階」，讓單機進程有明確里程碑。

數值全部是 `public static final` 常數，不引入 config 系統（沿用環境光照 spec §7 的立場）。

### 3.4 機器行為：仿熔爐，無按鈕

三個槽位：**觸媒槽**（只收殘影）、**Enkephalin 槽**、**產出槽**。

啟動條件全滿足就自動跑，任一條件破掉就暫停並把進度歸零——與熔爐一致。**不做「開始」按鈕**：省掉一條客戶端 → 伺服端的互動封包路線，客戶端 Screen 只需畫進度條。

進度用 `PropertyDelegate`（`[0]=progress`、`[1]=maxProgress`）同步，這是 vanilla 熔爐的標準做法。

### 3.5 方塊取得

一般工作台合成，用 vanilla `crafting_shaped` JSON——**不需要自訂 Recipe Type**：

```
鐵錠  紅石  鐵錠
石英  鐵砧  石英
鐵錠  石英  鐵錠
```

硬度 3.5、需鎬開採、破壞掉落自身（`block_loot` JSON）。

## 4. 元件切分

| 元件 | 職責 | 依賴 |
|---|---|---|
| `ExtractionLogic`（新） | 純函式：`canStart(觸媒階級, enkephalin 數量, 產出槽是否可放, 池大小)` → boolean；`costOf(階級)`、`durationOf(階級)`；`pick(池大小, roll)` → 池索引 | **無 Minecraft** |
| `ExtractionPools`（新） | 階級 → 該階飾品清單。從 `GiftRegistry.all()` 依 `BaseGift.tier()` 分組建表 | MC（僅 `Item`） |
| `ExtractorBlock`（新） | 方塊本體、朝向、右鍵開 GUI、破壞噴出內容物 | MC |
| `ExtractorBlockEntity`（新） | 3 槽庫存、tick 進度、NBT 存讀、完成時產出 | MC |
| `ExtractorScreenHandler`（新） | 槽位配置、`PropertyDelegate` 同步、快速移動（Shift 點擊）規則 | MC |
| `ExtractorScreen`（新，client） | 背景圖與進度條繪製 | MC client |
| `LootInjection`（新） | `LootTableEvents.MODIFY` 注入 Enkephalin 與殘影 | Fabric API |
| `ModBlocks`（新） | Block、BlockEntityType、ScreenHandlerType 註冊 | MC |
| `ModItems`（改） | 新增 `ENKEPHALIN` | — |
| `LimbusEGOMod`（改） | 初始化時呼叫 `ModBlocks.register()`、`LootInjection.register()` | — |
| `LimbusEGOClient`（改） | `HandledScreens.register` 綁定 Screen | MC client |

切開的理由：`ExtractionLogic` 只吃 int 與 double、只吐 int 與 boolean，機率與平衡數值能離線測；`ExtractorBlockEntity` 退化成「讀槽位 → 呼叫純函式 → 套用結果」，與 `SanityManager` 現在的形狀一致。`ExtractionPools` 從 `GiftRegistry` 反推池子，代表日後新增飾品會自動進池，不必維護第二份清單。

## 5. 資料流

一次提取：

1. `ExtractorBlockEntity.tick`（僅伺服端）讀觸媒槽 → `Vestiges.tierOf()` 得階級。
2. 呼叫 `ExtractionLogic.canStart(階級, Enkephalin 槽數量, 產出槽可放, ExtractionPools.of(階級).size())`。
3. 不可跑 → `progress = 0`、`markDirty()`、return。
4. 可跑 → `progress++`；`maxProgress = ExtractionLogic.durationOf(階級)`。
5. `progress >= maxProgress` → 消耗 1 殘影 ＋ `costOf(階級)` 個 Enkephalin；
   `ExtractionPools.of(階級)` 取得池，`ExtractionLogic.pick(池大小, random.nextDouble())` 得索引，
   把該飾品放進產出槽，`progress = 0`，播放完成音效。
6. 產出槽被取走後自動接著跑下一次。

`progress` 與 `maxProgress` 透過 `PropertyDelegate` 同步；客戶端 Screen 依 `progress / maxProgress` 畫進度條寬度。

## 6. 錯誤處理

| 情況 | 行為 |
|---|---|
| 觸媒槽非殘影／空 | 靜默暫停，`progress = 0` |
| Enkephalin 不足 | 同上 |
| 產出槽已有其他物品或已滿 | 同上 |
| `ExtractionPools.of(階級)` 查無資料 | 回空清單，`canStart` 為 false；**啟動時就 log 各階池大小**，空池在 mod 初始化階段就看得見，不等玩家踩到 |
| 區塊未載入 | 不 tick（vanilla 熔爐同行為，不特別處理） |

任何情況都不丟例外——BlockEntity tick 裡的例外會讓存檔難以救回。

## 7. 材質與資產

- 方塊材質（上／側／前）與 GUI 背景圖 176×166 PNG，**先產一版原創占位圖**（灰底面板＋槽位框），由腳本生成。
- **不從 vanilla 資源包複製 PNG**，避免把 Mojang 資產散布進 repo。
- 正式美術之後蓋掉同名檔案即可，程式不需改動。

## 8. 測試

**JUnit（無 Minecraft）**——`ExtractionLogicTest`：

- `canStart` 的四條失敗路徑：階級無效（`tierOf` 回 -1）、Enkephalin 不足、產出槽不可放、池為空。
- `costOf` / `durationOf` 對四個階級的映射，含階級越界回退行為。
- `pick` 的邊界：`roll = 0.0` → 索引 0；`roll` 趨近 1.0 → 最後一個索引（**不得越界**）；池大小為 1 時恆回 0。

**遊戲內驗收（`runClient`）**：

1. 合成提取機並放置，右鍵開啟 GUI，三個槽位與進度條正常顯示。
2. 投入 dark_vestige ＋ 8 個 Enkephalin → 5 秒後產出槽出現一件 Tier I 飾品。
3. 產出槽塞滿時機器停住，取走後自動續跑。
4. 關閉 GUI 後再打開，進度有繼續前進（離開 GUI 不中斷）。
5. 破壞方塊，槽內物品全部噴出。
6. 殺死一般怪物數十隻，能穩定取得 Enkephalin 與偶發 dark_vestige。
7. 存檔重開，機器內容物與進度保留。
8. 啟動日誌顯示四階池大小，加總為 80。

## 9. 不在本版範圍

- **重複飾品分解回 Enkephalin**：需要另一套價值表，且會影響掉落率平衡，等提取機的數值定案再談。
- **保底機制**：階級已由觸媒決定，池內隨機的挫折感遠低於 Paper 的全隨機抽獎，先不做。
- **紡錘抽獎箱／購買商店箱**：Paper 版的另外兩種箱子，與提取機是獨立玩法，各自值得一份 spec。
- **datapack 可設定的配方**：抽獎池是「階級 → 一組飾品」而非確定性 input→output，套進 vanilla Recipe 模型很彆扭，等有整合包需求再評估。
- **config 系統**：數值定案前不引入，沿用環境光照 spec §7 的立場。
