# Phase 2 飾品系統設計（Limbus-E.G.O-Fabric）

日期：2026-07-04
狀態：已與專案擁有者確認定案

## 1. 目標

把 Paper 插件 `Limbus-E.G.O` v1.3.0 的 **80 件 E.G.O 飾品**＋**殘影升級系統**移植進 Fabric 模組，
飾品欄接 **Accessories API**，一次做完 80 件後發 **v0.2.0**。數值全數對照插件原始碼逐一核對。

## 2. 飾品物品

- 80 件飾品各為原生 `Item`（工廠註冊於 `me.yisang.limbusego.gift`），材質從
  [Limbus-E.G.O-ResourcePack](https://github.com/Crossing-Dead-Development/Limbus-E.G.O-ResourcePack) 轉入模組內建 assets（`limbusego` namespace）
- 收錄於「E.G.O 飾品」自訂創造頁籤（`limbusego:gifts`，Phase 1 已建）；不進原版頁籤
- 每件帶自訂 Data Component `limbusego:gift_level`（Integer 0~3），記錄殘影升級等級

## 3. 佩戴（Accessories API）

- 註冊單一自訂欄位群組 `limbusego:ego_gift`，數量 **5 格通用欄**，任何 E.G.O 飾品皆可放入（對映插件 5 通用欄）
- 玩家由 Accessories 原生介面佩戴；身上不做 3D 渲染
- `/accessories` 指令：捷徑開啟 Accessories 佩戴介面

## 4. 效果分派（GiftDispatcher）

中央 `GiftDispatcher` 把插件 `Accessory` 的 8 種鉤子對映到 Fabric，查詢「已佩戴的 E.G.O 飾品」用 Accessories capability：

| 插件鉤子 | Fabric 對映 |
|---|---|
| `onPassiveTick` | Accessories 每 tick 佩戴回呼 |
| `onAttack` / `onDamaged` / `onAnyDamage` | 沿用 Phase 1 `LivingEntityMixin`（傷害流程中查佩戴飾品再分派，可改傷害值） |
| `onKill` / `onDeath` | `ServerLivingEntityEvents.AFTER_DEATH`（killer / 死者） |
| `onInteract` | `UseItemCallback` |
| `onQuit` | `ServerPlayConnectionEvents.DISCONNECT`（清冷卻 map） |

每件飾品一個類別，繼承 `BaseGift`（對映插件 `BaseAccessory`）：
- `applyScaled(target, effect, p, c, src)`：potency 依**佩戴物品的等級元件**倍率放大後施加
- `apply(...)`：不放大
- `gate(player, ms)`：每玩家冷卻閘
- `tier(id)` / `isVestige(id)`：階級與殘影判定 helper

## 5. 殘影升級（鐵砧）

- 升級等級存**飾品物品的 Data Component** `limbusego:gift_level`（0~3），佩戴時效果依此放大
- 升級倍率依等級（對照插件 `getUpgradeMultiplier`：0/1/2/3 級的倍率）
- 5 種殘影材料，階級對應：`dark_vestige`→I、`faint_vestige`→II、`twinkling_vestige`→III、`brilliant_vestige`→IV
- **鐵砧升級**：左格飾品 ＋ 右格對應階殘影 → 輸出等級 +1 的飾品，消耗一個殘影；
  階級不符或已達上限 3 則無輸出。以 mixin 攔 `AnvilScreenHandler` 的配方結果（`updateResult`）
- 升級後在 lore 顯示等級（對照插件 `msg.upgrade_lore`）

## 6. 指令（Brigadier，沿用 Phase 1 模式）

`/limbusego gift`：

| 指令 | 說明 | 權限 |
|---|---|---|
| `gift give <玩家> <id> [數量]` | 給予飾品／殘影 | 權限 2 |
| `gift give <id> [數量]` | 給自己 | 權限 2 |
| `gift category` | 飾品圖鑑（依等級／依體系兩種排序） | 所有人 |
| `gift admin` | 管理員 GUI（點擊取得） | 權限 2 |
| `/accessories` | 開 Accessories 佩戴介面（捷徑） | 所有人 |

## 7. 實作策略（一次做完 80 件再發 v0.2.0）

1. **框架**：Accessories 相依串接、5 格欄位註冊、`gift_level` Data Component、`BaseGift`／`GiftDispatcher` 骨架；先用 2~3 件試水，驗證佩戴→效果→升級全鏈路
2. **80 件飾品**：依插件 9 體系分組移植（燒傷／流血／沉淪／破裂／震顫／呼吸法／輔助／便利／原創），每組一個 commit，數值對照插件
3. **殘影**：5 種材料＋鐵砧升級 mixin
4. **指令與 GUI**：圖鑑（雙排序）／管理／give
5. **收尾**：資源包材質轉入、雙語 README 更新、最終冒煙、發 v0.2.0

## 8. 測試

- 每組移植後 `runServer` 佩戴實測（佩戴→觸發→數值）
- 純數值運算（升級倍率、階級校驗、等級元件讀寫）拆為不依賴 MC 的類別，附 JUnit

## 9. 明確不做（YAGNI）

- 不移植「飾品欄開啟工具」道具（Accessories 原生介面取代）
- 不移植插翅虎／終末鳥組合包
- 不做飾品 3D 身上渲染
- 不做 Paper ↔ Fabric 存檔互轉
