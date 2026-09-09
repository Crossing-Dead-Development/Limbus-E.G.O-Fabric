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

### 3.3 擊發與選槍

`use()` 只在 `Hand.MAIN_HAND` 觸發，副手回傳 `PASS`，避免一次點擊打出兩發。

**選槍規則（無狀態）**：挑目前不在冷卻中的那把；兩把都可用時優先主手；兩把都在冷卻則不做任何事。

搭配每把各自 1.2 秒（24 tick）冷卻，連續點擊的自然結果是：

```
t=0.0s  主手（黑）發射，黑進入冷卻至 1.2s
t=0.6s  黑仍在冷卻 → 副手（白）發射，白冷卻至 1.8s
t=1.2s  黑冷卻結束 → 黑發射
t=1.8s  白冷卻結束 → 白發射
```

即穩定的 黑→白→黑→白、每 0.6 秒一發。

**選擇無狀態規則而非儲存「輪到誰」的理由**：不需要跨登出持久化、不會與實際冷卻狀態不同步、不需要在玩家離線時清理。已知副作用：**停手超過 1.2 秒後，下一發永遠從主手那把開始**，而非接續上次順序。此行為可接受且可預測。

選槍規則同樣寫成純函式：

```java
/** 回傳應擊發的手；兩把都在冷卻時回傳 empty。 */
public static Optional<Hand> pickHand(boolean mainReady, boolean offReady) {
    if (mainReady) return Optional.of(Hand.MAIN_HAND);
    if (offReady) return Optional.of(Hand.OFF_HAND);
    return Optional.empty();
}
```

冷卻透過 vanilla `ItemCooldownManager` 設在對應的 `ItemStack` 上，因此玩家能直接看到物品欄的灰色冷卻覆蓋。

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
| `SolemnLamentItem` | `item/` | 改繼承 `Item`；`use()` 實作配對判定、選槍、擊發、冷卻、彈藥 |
| `SolemnLamentLogic` | `item/` | **新增**。`isPaired` 與 `pickHand` 兩個純函式，供物品呼叫與單元測試 |
| `CrossbowItemMixin` | `mixin/` | **刪除** |
| `PlayerEntityRendererMixin` | `mixin/client/` | 改為配對時雙手 `CROSSBOW_HOLD` |
| `WeaponEvents.fireSolemnLament` | `event/` | 不變（彈道與命中效果沿用）；彈藥消耗改由 `SolemnLamentItem` 負責 |
| `WeaponTooltips` | `item/` | 改寫 `solemn_lament.*` 說明行 |

資料流：

```
主手右鍵
  → SolemnLamentLogic.isPaired(主手, 副手)
      否 → 動作列提示，結束
      是 → SolemnLamentLogic.pickHand(主手冷卻?, 副手冷卻?)
              empty → 結束
              hand  → 找彈藥
                        無 → 結束
                        有 → 消耗 1 枚
                             WeaponEvents.fireSolemnLament(該手的 isBlack)
                             該手 ItemStack 進 1.2 秒冷卻
                             播擊發音 + 再上膛音
```

## 5. 錯誤處理

- 副手為 `Hand.OFF_HAND` 的 `use()` 呼叫一律 `PASS`，確保一次點擊只打一發。
- 右鍵指向方塊或實體時，vanilla 可能先消耗該次互動而不呼叫 `use()`。此為既有武器共通行為，本 spec 不處理。
- 背包無彈藥：不擊發、不進冷卻、不提示（與其他彈藥武器一致，避免洗版）。
- **創造模式一律仍需彈藥**。這是明確決定而非沿襲：現行的弩基底在創造模式下的彈藥行為由 vanilla 決定、並不明確，改為普通 `Item` 後由我們自己負責，統一要求彈藥比較好推理。天退星刀目前是創造模式免彈藥（`TiantuiStarItem` 的 `!user.getAbilities().creativeMode`），兩者不一致是已知的，本 spec 不一併調整。

## 6. 測試

沿用「純邏輯、不載入 Minecraft」慣例。`SolemnLamentLogic` 的兩個函式不接觸 `ItemStack` 或 registry，可直接測試。

| 測試 | 斷言 |
|---|---|
| `SolemnLamentLogicTest.pairingRequiresOneOfEach` | 黑+白、白+黑成立；黑+黑、白+白、單持、非莊嚴哀悼皆不成立 |
| `SolemnLamentLogicTest.pickHandPrefersMainWhenBothReady` | 兩把皆可用 → 主手 |
| `SolemnLamentLogicTest.pickHandFallsBackToOffHand` | 主手冷卻中、副手可用 → 副手 |
| `SolemnLamentLogicTest.pickHandReturnsEmptyWhenBothOnCooldown` | 兩把皆冷卻 → empty |
| `WeaponTooltipsTest`（既有） | 改寫後的說明鍵仍在中英文 lang 中存在 |

`pickHand` 回傳 `Hand`（Minecraft enum），但該 enum 為純常數、不需 bootstrap，測試可直接使用。

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
