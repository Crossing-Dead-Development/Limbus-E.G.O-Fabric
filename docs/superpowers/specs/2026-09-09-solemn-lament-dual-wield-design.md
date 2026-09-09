# 莊嚴哀悼雙持設計

日期：2026-09-09
狀態：設計已通過，待寫實作計畫

## 1. 目標

把莊嚴哀悼從「單手弩」改為**必須黑白雙持才能射擊的雙槍**，左右輪流發射。

玩家回饋指出現在的莊嚴哀悼「不能雙持」「右鍵射擊、左鍵也能正常用，有點反常識」。改為雙持後，這把武器有了明確的使用姿態：湊齊黑白兩把才是完整型態，而輪流發射本身就是射速的來源。

### 範圍界線

**涵蓋**：雙持判定、點射式擊發、輪流與冷卻、彈藥消耗時機、未配對時的回饋、手臂姿勢，以及連帶必須同步的 tooltip 與 README。

**不涵蓋**：

- 黑白兩把命中後的傷害與屬性數值（維持 8/凋零/沉淪4·3 與 4/失明/沉淪3·2）
- 生蝶亡蝶的取得方式
- 彈道、飛行速度、命中判定（`WeaponEvents.tickProjectiles` 不動）
- 其他武器

## 2. Repo 現況

| 事實 | 依據 |
|---|---|
| `SolemnLamentItem` 繼承 `CrossbowItem`，走兩段式上弦 | `SolemnLamentItem` |
| **目前完全沒有冷卻** | 全專案 `getItemCooldownManager` 無任何 solemn 相關呼叫 |
| 唯一的限速是上弦時間被改成 2 tick | `CrossbowItemMixin.limbusego$quickLoad` |
| README 宣稱「1.2s 冷卻」，與程式不符 | 承襲自 Paper 插件，Fabric 版未實作 |
| 彈藥在**上弦時**消耗（vanilla 弩行為） | `CrossbowItem` 裝填流程；`fireSolemnLament` 註解「彈藥已於弩上弦時消耗」 |
| 物品模型的 `using_item` 兩分支指向同一模型 | `assets/limbusego/items/solemn_lament_black.json` |
| `CrossbowItemMixin` 只做兩件事：改上弦時間、換裝填音 | `CrossbowItemMixin` |
| 手臂姿勢靠 mixin 補回（vanilla 寫死 `Items.CROSSBOW`） | `PlayerEntityRendererMixin` |
| 擊發邏輯與彈道已獨立於物品類別 | `WeaponEvents.fireSolemnLament` / `tickProjectiles` |
| 已有 tooltip 系統，武器說明住在集中表 | `WeaponTooltips`（2026-09-09 tooltip spec） |

## 3. 設計決定

### 3.1 脫離 `CrossbowItem`

上弦流程移除後，弩基底只剩包袱：`CHARGED_PROJECTILES` 元件、`getProjectiles`／`getHeldProjectiles`、裝填狀態機。改為繼承普通 `Item`。

連帶**刪除 `CrossbowItemMixin`**——它存在的唯一理由是改上弦時間與裝填音，兩者都隨上弦消失。少一個注入 vanilla class 的 mixin 是淨賺。

模型無損失：`using_item` 的兩個分支本來就指向同一個模型。

### 3.2 雙持判定

成立條件：**主手與副手各一把莊嚴哀悼，且一黑一白**。

黑+黑、白+白、單持、以及任一手為其他物品，皆不成立。

判定寫成一個純函式，與 Minecraft 的手部 API 分離，便於單元測試：

```java
/** 主手與副手是否構成合法的黑白配對。 */
public static boolean isPaired(boolean mainIsSolemn, boolean mainIsBlack,
                               boolean offIsSolemn, boolean offIsBlack) {
    return mainIsSolemn && offIsSolemn && mainIsBlack != offIsBlack;
}
```

### 3.3 擊發與輪流

**修正記錄（實作後）**：本節原本設計了一個 `pickHand` 選槍函式，前提是 `Item.use()` 會在
主手冷卻時照樣被呼叫、由我們自己決定改打副手。**這個前提是錯的。**

實際的 vanilla 行為（`1.21.4` 位元組碼確認）：

- `ClientPlayerInteractionManager.interactItem` 在呼叫 `Item.use` **之前**先檢查
  `ItemCooldownManager.isCoolingDown(stack)`，冷卻中直接回傳 `ActionResult.PASS`。
- `MinecraftClient.doItemUse` 走訪 `Hand.values()`（主手、副手），只在結果
  `isAccepted()` 時中斷；`PASS` 與 `FAIL` 都不算 accepted。

因此**輪流是 vanilla 免費提供的**，不需要任何選槍邏輯：

| 狀態 | vanilla 行為 | 結果 |
|---|---|---|
| 兩把都可用 | 主手 `use` 回 `SUCCESS`，迴圈中止 | 打主手（「優先主手」是免費的） |
| 主手冷卻 | 主手被擋、回 `PASS` → 續試副手 | 打副手 |
| 兩把都冷卻 | 兩手都回 `PASS` | 什麼都不做 |

`SolemnLamentItem.use()` 因此只需處理「傳進來的這隻手」：確認配對 → 檢查彈藥 →
擊發該手的槍 → 對該手的 stack 設冷卻。`SolemnLamentLogic` 只留 `isPaired`。

每把各自 **1.2 秒（24 tick）** 冷卻，連點的結果：

```
t=0.0s  主手（黑）發射，黑冷卻至 1.2s
t=0.6s  黑仍在冷卻 → vanilla 自動落到副手（白）發射，白冷卻至 1.8s
t=1.2s  黑冷卻結束 → 黑發射
```

即穩定的 黑→白→黑→白、每 0.6 秒一發。

已知副作用：**停手超過 1.2 秒後，下一發永遠從主手那把開始**，而非接續上次順序。
此行為可接受且可預測。

未配對時的提示只由主手送出，否則黑+黑之類的組合會在同一次點擊收到兩則訊息
（`FAIL` 不中斷 vanilla 的手部迴圈）。

### 3.4 彈藥與音效

每次擊發消耗一枚生蝶、亡蝶（自背包尋找，沿用 `WeaponEvents.findButterfly`）。背包無彈藥時不擊發、不進冷卻。

**消耗時機從「上弦時」改為「擊發時」**，因為已無上弦階段。

音效：擊發播 `SOLEMN_SHOOT`，緊接著播 `SOLEMN_QUICK_LOAD_3` 作為再上膛聲。原本綁在上弦流程的裝填音資產因此不會浪費。

### 3.5 未配對時的行為

右鍵不擊發、不消耗彈藥、不進冷卻，並在動作列顯示提示（翻譯鍵 `msg.limbusego.solemn_lament.need_pair`，中英文各一）。

左鍵近戰維持原樣：物品的攻擊力與攻速不變，單持仍可當一般武器揮擊。

### 3.6 手臂姿勢

`PlayerEntityRendererMixin` 目前的條件：上弦中 → `CROSSBOW_CHARGE`、已裝填 → `CROSSBOW_HOLD`。

改為：**成功配對時，雙手皆回 `CROSSBOW_HOLD`**。視覺上即雙手持槍舉起的姿態。未配對時不介入，維持一般持物姿勢。

`CROSSBOW_CHARGE` 分支隨上弦流程一併移除。

### 3.7 連帶必須同步的內容

| 項目 | 處理 |
|---|---|
| `WeaponTooltips` 的 `solemn_lament_*` 說明 | 現有文案描述「長按右鍵上弦，放開射出」「上弦時消耗一枚生蝶、亡蝶」，全部作廢。改寫為雙持需求、點射、輪流、冷卻 |
| `README.md` / `README.en.md` 武器表 | 更新黑白兩列的機制描述，並移除不存在的「1.2s 冷卻」舊述（新版本確實有 1.2s，但語意不同：是每把各自的冷卻） |

## 4. 元件

| 元件 | 位置 | 變更 |
|---|---|---|
| `SolemnLamentItem` | `item/` | 改繼承 `Item`；`use()` 實作配對判定、擊發、冷卻、彈藥（不含選槍，見 §3.3） |
| `SolemnLamentLogic` | `item/` | **新增**。`isPaired` 純函式，供物品與 mixin 呼叫並單元測試（`pickHand` 於實作後移除，見 §3.3） |
| `CrossbowItemMixin` | `mixin/` | **刪除** |
| `PlayerEntityRendererMixin` | `mixin/client/` | 改為配對時雙手 `CROSSBOW_HOLD` |
| `WeaponEvents.fireSolemnLament` | `event/` | 不變（彈道與命中效果沿用）；彈藥消耗改由 `SolemnLamentItem` 負責 |
| `WeaponTooltips` | `item/` | 改寫 `solemn_lament.*` 說明行 |

資料流：

```
右鍵
  → vanilla doItemUse 依序試 MAIN_HAND、OFF_HAND
      該手冷卻中 → vanilla 回 PASS，續試下一隻手（use 不會被呼叫）
      該手可用   → SolemnLamentItem.use(hand)
                     未配對 → 主手才送動作列提示，回 FAIL
                     無彈藥 → 回 FAIL（不進冷卻）
                     否則   → 消耗 1 枚生蝶亡蝶
                              WeaponEvents.fireSolemnLament(該手的 isBlack)
                              該手 ItemStack 進 1.2 秒冷卻
                              播擊發音 + 再上膛音
                              回 SUCCESS（isAccepted → vanilla 中止手部迴圈）
```

## 5. 錯誤處理

- 一次點擊只打一發：擊發成功回傳 `SUCCESS`，`isAccepted()` 為真，vanilla 隨即中止手部迴圈，不會再試另一隻手。
- 未配對時的提示只由 `Hand.MAIN_HAND` 送出：`FAIL` 不中斷手部迴圈，若兩手都提示，黑+黑會在同一次點擊收到兩則訊息。
- 右鍵指向方塊或實體時，vanilla 可能先消耗該次互動而不呼叫 `use()`。此為既有武器共通行為，本 spec 不處理。
- 背包無彈藥：不擊發、不進冷卻、不提示（與其他彈藥武器一致，避免洗版）。
- **創造模式一律仍需彈藥**。這是明確決定而非沿襲：現行的弩基底在創造模式下的彈藥行為由 vanilla 決定、並不明確，改為普通 `Item` 後由我們自己負責，統一要求彈藥比較好推理。天退星刀目前是創造模式免彈藥（`TiantuiStarItem` 的 `!user.getAbilities().creativeMode`），兩者不一致是已知的，本 spec 不一併調整。

## 6. 測試

沿用「純邏輯、不載入 Minecraft」慣例。`SolemnLamentLogic.isPaired` 不接觸 `ItemStack` 或 registry，可直接測試。

| 測試 | 斷言 |
|---|---|
| `SolemnLamentLogicTest.pairingRequiresOneBlackAndOneWhite` | 黑+白、白+黑成立 |
| `SolemnLamentLogicTest.sameColourIsNotPaired` | 黑+黑、白+白不成立 |
| `SolemnLamentLogicTest.singleWieldIsNotPaired` | 單持、任一手非莊嚴哀悼皆不成立 |
| `WeaponTooltipsTest`（既有） | 改寫後的說明鍵仍在中英文 lang 中存在 |

**「輪流」沒有單元測試**：它是 vanilla 手部迴圈與冷卻閘門的副產品（§3.3），不是我們的程式碼，只能在遊戲內驗證。

## 7. 驗收

在遊戲內：

1. 只拿黑槍：右鍵無反應，動作列提示需雙持；左鍵仍可正常揮擊。
2. 主手黑、副手白：右鍵立即打出一發黑彈（8 傷、凋零），無拉弓過程。
3. 連續點擊：黑白交替，節奏穩定約每 0.6 秒一發，物品欄可見兩把各自的灰色冷卻。
4. 停手 2 秒後再點：從主手那把開始。
5. 背包彈藥耗盡：右鍵無反應，且未進入冷卻。
6. 黑+黑：右鍵無反應並提示，確認同色不成立。
7. 雙持時第三人稱觀察：雙手都呈舉槍姿勢。
8. 按住 Shift 查看 tooltip：說明與上述實際行為完全一致。
