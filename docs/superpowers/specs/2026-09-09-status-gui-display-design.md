# Limbus 屬性的 GUI 顯示設計

日期：2026-09-09
狀態：已實作（計畫：docs/superpowers/plans/2026-09-11-status-gui-display.md）

## 1. 目標

讓玩家看得見自己身上的 Limbus 屬性，並讓打出去的屬性有世界內的回饋。

目前 12 種屬性除了迅捷／束縛之外全部只存在記憶體，玩家唯一的線索是一閃即逝的動作列文字。結果是：身上疊了幾層燒傷、還剩幾層充能，完全無從得知。

本 spec 做兩件事：

1. 玩家身上的屬性鏡射成原版狀態效果，出現在物品欄與 HUD 的效果列。
2. 動作列的屬性文字全部移除，改為在目標身上噴該屬性顏色的粒子。

### 範圍界線

**涵蓋**：10 個純顯示效果的註冊、玩家專屬的鏡射同步、動作列訊息移除、施加粒子、顏色來源收斂。

**不涵蓋**：

- 效果圖示材質（本版接受缺失材質方格，之後另做）
- 迅捷／束縛（已是原版速度／緩速的包裝，本來就顯示）
- 怪物身上的 GUI 效果（只有粒子）
- 任何戰鬥數值或機制改動
- 薄暝與 W 公司匕首的傷害調整（另開一份）

## 2. Repo 現況

| 事實 | 依據 |
|---|---|
| 12 種屬性中，**迅捷與束縛已是 potion wrapper**，直接對映 SPEED／SLOWNESS | `StatusManager.applyPotionWrapper` |
| 其餘 10 種純存在記憶體，GUI 完全看不到 | `StatusState`（`EnumMap<StatusEffect, int[]>`） |
| `(potency, count)` 語意：count 是**剩餘觸發次數**，不是時間 | `StatusState` 類別註解 |
| `StatusState.snapshot()` 已存在且註明「供顯示層唯讀迭代使用」 | `StatusState` |
| 已有每 10 tick 的排程（燒傷 DoT 分桶） | `StatusManager.start()` 的 `ServerScheduler.every(10, ...)` |
| 動作列訊息共四種 | `showEffectApplied`、`showDamage`、`STATUS_POISE_CRIT`、`STATUS_TREMOR_BURST` |
| `Messages` 的這幾條是**寫死的中文**，不是翻譯鍵 | `Messages.STATUS_POISE_CRIT = "§3§l✦ 呼吸法爆擊..."` |
| 屬性顏色目前只有 legacy 色碼字串 | `StatusEffect` enum：`BURN("燒傷", "§6")` |
| `TooltipFormat` 得自己把 legacy 色碼轉 RGB | `TooltipFormat.legacyToRgb` |
| 專案已在用 `DustParticleEffect` 上色 | `WeaponEvents`：`new DustParticleEffect(color, 1.2f)` |
| 沉淪已透過屬性修飾符影響移速 | `StatusManager.syncSinkingSpeed` |

## 3. 設計決定

### 3.1 純顯示效果，`StatusManager` 仍是唯一真相

註冊的原版效果**沒有任何行為**：不掛屬性修飾符、不覆寫 `applyUpdateEffect`。它們只是給 GUI 看的殼。

理由：Limbus 的 `(potency, count)` 語意——尤其「命中時消耗一層」——原版效果表達不了。讓原版效果接手實際效果等於重寫戰鬥核心，會連帶影響 80 件飾品與全部武器。

### 3.2 註冊哪 10 個

| 分類 | 屬性 |
|---|---|
| `StatusEffectCategory.HARMFUL` | 流血 bleed、燒傷 burn、易損 fragile、沉淪 sinking、破裂 rupture、震顫 tremor |
| `StatusEffectCategory.BENEFICIAL` | 強壯 power、守護 protection、呼吸法 poise、充能 charge |

**迅捷與束縛不註冊**：它們已經以原版速度／緩速的身分顯示在 GUI，再加一層鏡射會變成兩個圖示並列表達同一件事。代價是玩家看到的名字是「速度」而非「迅捷」，本版接受。

材質先不做。缺失材質會顯示為方格，這是刻意的取捨——先讓機制成立。

### 3.3 時間顯示：無限

原版效果的持續時間是秒數，Limbus 的 count 是剩餘觸發次數，兩者不可換算。

因此鏡射效果一律使用**無限持續**（`StatusEffectInstance.INFINITE`），GUI 顯示「強壯 III ∞」。層數歸零時由同步器顯式移除。

拒絕的替代方案：`duration = count × 20`（現有 potion wrapper 的做法）。倒數會自己跑完而實際層數還在，兩邊必然對不上。

`amplifier = potency - 1`，因此 potency 3 顯示為「III」。

### 3.4 週期同步，而非逐點呼叫

新增 `StatusMirror`，掛在既有的 10 tick 排程上，**只掃線上玩家**。每輪對這 10 個屬性比對 `StatusState` 與玩家目前的鏡射效果：

| 情況 | 動作 |
|---|---|
| 有層數、無鏡射效果 | 套上（amplifier = potency − 1、無限持續） |
| 有層數、amplifier 不符 | 重新套用新的 amplifier |
| 無層數、有鏡射效果 | 移除 |

**不在 `apply` / `consume` / `refresh` 逐點插同步呼叫**：`consume` 散落在十幾處呼叫點，漏掉任何一處就會在玩家身上留下永遠不消失的假效果。週期同步只有一個地方會錯。

代價：效果出現與消失最多延遲 0.5 秒。對顯示層可接受。

**只給玩家**的理由：怪物身上的鏡射會讓每隻燃燒中的殭屍冒原版效果粒子，畫面吵雜；且需要每輪掃描全世界有屬性的實體，成本高出一個量級。怪物的回饋由 §3.5 的粒子負責。

### 3.5 動作列文字 → 目標身上的粒子

移除四種屬性動作列訊息與 `sendActionBar` 本身：

| 移除項 | 位置 |
|---|---|
| 屬性施加提示 | `showEffectApplied` |
| 屬性傷害數字 | `showDamage` |
| 呼吸法爆擊 | `Messages.STATUS_POISE_CRIT` 的送出點 |
| 震顫引爆 | `Messages.STATUS_TREMOR_BURST` 的送出點 |

`Messages` 中對應的常數一併刪除。附帶好處：這幾條是寫死的中文，刪掉等於少四處未在地化的字串。

蓄力條與技能提示**留著**（它們在 `WeaponEvents`，屬於功能性 UI，拔掉玩家就看不到蓄力進度）。

**改為**：屬性施加時在目標位置噴該屬性顏色的 `DustParticleEffect`。

觸發條件：

```java
source != null && !source.equals(target)
```

即「打出去」才冒。自身屬性靠 §3.4 的 GUI 鏡射，不重複回饋；`source` 為 null 的系統派生（環境、DoT 結算）也不冒。

### 3.6 顏色收斂到 enum

`StatusEffect` 目前只帶 legacy 色碼字串，`TooltipFormat` 得自己轉 RGB。粒子與效果註冊也都需要 RGB，等於三個消費者各自轉一次。

enum 改為只帶 RGB：

```java
BURN(0xFFAA00);
```

`TooltipFormat.legacyToRgb` 隨之刪除，改讀 `effect.rgb`。

**`color` 與 `zh` 兩個欄位一併刪除。** 已確認它們的使用者只有 `showEffectApplied`、
`showDamage`（本 spec 移除）與 `TooltipFormat.legacyToRgb`（本 spec 改寫），改完即成死碼。
附帶好處：`zh` 是 enum 裡最後一處寫死的中文，刪掉之後屬性名完全由翻譯鍵決定。

### 3.7 名稱不分岔

GUI 的效果名走 `effect.limbusego.<path>`（原版從 registry id 推導），tooltip 走既有的 `status.limbusego.<path>`。

兩套鍵指同一個詞。加測試斷言這 10 對的值完全相同，避免日後改了一邊忘了另一邊。

## 4. 元件

| 元件 | 位置 | 變更 |
|---|---|---|
| `ModStatusEffects` | `status/` | **新增**。註冊 10 個純顯示效果，提供 `StatusEffect`（Limbus enum）→ 原版效果的對照 |
| `MirrorStatusEffect` | `status/` | **新增**。無行為的原版效果空殼子類（`StatusEffect` 建構子為 protected，必須子類化） |
| `StatusDisplayLogic` | `status/` | **新增**。顯示決策的純函式：同步該做什麼、粒子該不該冒 |
| `StatusMirror` | `status/` | **新增**。掛排程、掃玩家、依 `StatusDisplayLogic` 的決策套用／移除 |
| `StatusManager` | `status/` | 移除四種動作列訊息與 `sendActionBar`；`showEffectApplied` 改為噴粒子 |
| `StatusEffect`（enum） | `status/` | 加 `rgb` 欄位，刪除 `color` 與 `zh` |
| `TooltipFormat` | `tooltip/` | 刪 `legacyToRgb`，改讀 `effect.rgb` |
| `Messages` | 根套件 | 刪除四條屬性訊息常數 |

資料流：

```
每 10 tick（既有排程）
  → StatusMirror：走訪線上玩家
      → StatusState.snapshot()
      → 對 10 個屬性各自：
            StatusDisplayLogic.decide(potency, 現有 amplifier 或 null)
              ADD    → addStatusEffect(無限、amplifier = potency − 1)
              UPDATE → 同上（覆蓋）
              REMOVE → removeStatusEffect
              NONE   → 不動

屬性施加時（StatusManager.apply）
  → StatusDisplayLogic.shouldSpawnParticles(source, target)
      → 在 target 位置噴 DustParticleEffect(effect.rgb)
```

## 5. 錯誤處理

- 玩家離線／死亡：`StatusManager` 既有的 `AFTER_DEATH` 已清除 states；鏡射效果隨玩家死亡由原版清除，下一輪同步自然歸位。
- `StatusState` 不存在（玩家身上無任何屬性）：同步器仍需移除殘留的鏡射效果，不可提早 return。
- 玩家被其他來源施加同名效果：不可能——這 10 個效果只有本模組會註冊與施加。
- 粒子只在伺服端產生（`ServerWorld.spawnParticles`），客戶端無需改動。

## 6. 測試

沿用「純邏輯、不載入 Minecraft」慣例。

| 測試 | 斷言 |
|---|---|
| `StatusDisplayLogicTest.addsWhenAbsent` | potency 3、無現有效果 → ADD，amplifier 2 |
| `StatusDisplayLogicTest.updatesWhenAmplifierDiffers` | potency 5、現有 amplifier 2 → UPDATE，amplifier 4 |
| `StatusDisplayLogicTest.noopWhenUnchanged` | potency 3、現有 amplifier 2 → NONE |
| `StatusDisplayLogicTest.removesWhenPotencyGone` | potency 0、有現有效果 → REMOVE |
| `StatusDisplayLogicTest.noopWhenNothingOnEitherSide` | potency 0、無現有效果 → NONE |
| `StatusDisplayLogicTest.particlesOnlyForOthers` | 無 source → 不冒；source == target → 不冒；source != target → 冒 |
| `StatusEffectColourTest` | 10 個鏡射屬性的 `rgb` 皆非零且互異 |
| `LangParityTest`（既有）＋新增 `effect.*` / `status.*` 值一致斷言 | 10 對翻譯值完全相同 |

註冊是否成功、粒子外觀、GUI 呈現靠遊戲內驗收。

## 7. 驗收

在遊戲內：

1. 用 `/limbusego` 給自己施加燒傷，物品欄與 HUD 出現「燒傷 N ∞」（圖示為缺失材質方格，預期如此）。
2. 層數提高，GUI 的羅馬數字跟著變。
3. 層數耗盡，效果在 0.5 秒內消失。
4. 同時持有多種屬性，效果列同時列出多個。
5. 拿武器打怪：怪物身上冒出對應顏色的粒子（燒傷橙、流血紅、沉淪紫…）。
6. 自己獲得強壯：**只有** GUI 效果，不冒粒子。
7. 動作列不再出現任何屬性文字；但天退／薄暝／提比婭的蓄力條仍在。
8. 切英文：效果名為英文，且與 Shift tooltip 裡的屬性名一致。
