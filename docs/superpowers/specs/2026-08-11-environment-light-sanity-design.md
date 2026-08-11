# 環境光照 × SAN 設計

日期：2026-08-11
狀態：設計已通過，待寫實作計畫

## 1. 目標

讓既有的 SAN 系統（-45 ~ +45）開始與 Minecraft 原生環境互動：長時間處於低光照環境時 SAN 緩慢下降，明亮環境緩慢恢復，中間亮度不變。

目的不是懲罰玩家，而是讓洞穴、夜間探索、廢棄礦坑、深暗之域自然帶來精神壓力，同時讓火把、營地、玩家住宅自然具有「安全區」的感覺。

**原則：使用 Minecraft 原生的環境亮度判定，不自建另一套光照系統。**

### 範圍界線

本 spec **只涵蓋環境光照 × SAN**。企劃書中的 E.G.O 提取系統（提取機、腦啡肽、核心素材、Recipe、GUI）另開一份 spec，理由見 §7。

## 2. Repo 現況

| 事實 | 依據 |
|---|---|
| 已有每 2 秒掃全體玩家的排程 | `SanityManager.start()` 的 `ServerScheduler.every(40, ...)` → `recoveryTick(p)` |
| 脫戰回復：脫戰 10s 後 SAN<0 每 2 秒 +1 | `SanityManager.recoveryTick()` |
| SAN **完全不持久化** | `san` 是 `ConcurrentHashMap`；`onQuit()` 直接 `san.remove()`，`onJoin()` 用 `putIfAbsent(uuid, 0)` |
| SAN 正值給屬性 buff | `ATK_PER_SAN = 0.003`、`SPD_PER_SAN = 0.0015`，極值 +13.5% 攻擊 / +6.75% 移速 |
| 純邏輯單元測試的既有慣例 | `GiftUpgradeLogicTest`、`StatusStateTest`（皆不載入 Minecraft） |
| 已有 mixin 基礎設施 | `limbusego.mixins.json` + `LivingEntityMixin` |
| **沒有任何 config 系統** | 無 config 套件、無讀檔、無 Cloth Config 依賴 |

## 3. 設計決定

### 3.1 亮度門檻

用 `World.getLightLevel(BlockPos)`（內部為 `getLightLevel(pos, getAmbientDarkness())`），它會把時間納入計算。實測對應：

| 環境 | `getLightLevel` |
|---|---|
| 洞穴（無光源） | 0 |
| 午夜露天 | 4（天空光 15 − ambient darkness 11） |
| 火把附近 | 10 ~ 14 |
| 白天露天 | 15 |

因此門檻取：

```java
private static final int LIGHT_DARK_MAX   = 4;   // ≤4 視為黑暗
private static final int LIGHT_BRIGHT_MIN = 9;   // ≥9 視為明亮
                                                 // 5~8 為中性緩衝
```

這讓「洞穴」與「夜晚露天」都算暗、「點了火把的家」算亮，不需自訂分級。

### 3.2 曝光累計器

不直接每 2 秒改 SAN，而是累計：

```java
private static final int EXPOSURE_THRESHOLD = 15;  // 15 × 2s = 30s 動 1 點 SAN
private final Map<UUID, Integer> exposure = new ConcurrentHashMap<>();
```

`exposure` 正值累積明亮、負值累積黑暗。好處：短暫走過陰影完全無感；調整速度只要改一個常數，不必動 tick 間隔。

**速度後果**：洞穴待 10 分鐘 → SAN -20（觸發既有警告提示）；22 分鐘 → 觸底 -45。

### 3.3 黑暗中暫停脫戰回復

現有脫戰回復（每 2 秒 +1）遠快於任何「緩慢」的黑暗掉落。若不處理，黑暗機制在負值區完全失效。

**解法：低於黑暗門檻時，不執行 `cur < 0 → +1` 的脫戰回復。** 語意為「黑暗中無法平復」。這同時把「回到明亮處才能休息」的安全區感覺直接做出來。

### 3.4 明亮回復只到 0

明亮環境是「止血」，不是「強化」。要拿正 SAN 的屬性 buff 必須靠戰鬥命中（既有規則）。若允許回到 +45，玩家在火把旁掛機 22 分鐘就能拿滿攻擊/移速 buff。

實作上即：`exposure ≥ +15` 時，僅在 `getSan(p) < 0` 才 +1。此行為與現有 `recoveryTick` 一致。

### 3.5 SAN 持久化

綁上環境後，重登歸零會成為明顯的洗 SAN 手段（在礦坑登出再登入即可清除黑暗壓力）。因此本版順帶修掉。

**只存 SAN 數值本身（一個 int）。** 命中/受擊計數（`counters`）、脫戰計時（`lastCombat`）、曝光累計器（`exposure`）都不存，重登重置無害。

現有「死亡重生 SAN 歸零」規則（`resetOnRespawn`）不變。

**跨維度不需處理** —— `SanityManager` 全部以 UUID 為 key，`ServerPlayerEntity` 實例換新不影響 map。需要處理的只有存檔進出：

- `onJoin`：讀取存檔值（取代現在的 `putIfAbsent(uuid, 0)`）
- `onQuit`：在 `san.remove()` **之前**寫入

實作優先用 Fabric Data Attachment API（`fabric-api` 已是依賴）。若實作時 1.21.4 的 API 表面有出入，退回 `ServerPlayerEntityMixin` 寫玩家 NBT —— Repo 已有 mixin 基礎設施，這條路徑確定可行。

## 4. 每 2 秒的判定流程

改寫 `SanityManager.recoveryTick(p)`：

```
若 玩家為創造/旁觀模式 → 跳過環境判定（仍保留既有 debuff 補刷邏輯）

light = p.getWorld().getLightLevel(p.getBlockPos())
dark   = light <= LIGHT_DARK_MAX
bright = light >= LIGHT_BRIGHT_MIN

# 1. 曝光累計
dark            → exposure -= 1
bright          → exposure += 1
中性(5~8)        → exposure 向 0 收斂 1（正值減 1、負值加 1、0 不動）

# 2. 跨門檻 → 動 1 點 SAN，累計器歸零
exposure <= -EXPOSURE_THRESHOLD → SAN -1，exposure = 0
exposure >= +EXPOSURE_THRESHOLD → 若 SAN < 0 則 +1，exposure = 0

# 3. 脫戰回復（既有邏輯，加上黑暗抑制）
若 !dark 且 脫戰滿 10s 且 SAN < 0 → SAN +1
```

中性區向 0 收斂的用意：走出洞穴進到中性亮度後，黑暗累積會自然消散，而不是永遠掛著等下次進洞繼續累加。

## 5. 實作陷阱（必讀）

### 5.1 不能重用 `dropSan()`

`SanityManager.dropSan()` 會執行 `lastCombat.put(p.getUuid(), currentTimeMillis())`。環境扣 SAN 若走這條路，玩家在洞穴裡會被永久判定為「戰鬥中」，脫戰回復再也不會觸發。

**環境扣 SAN 必須直接呼叫 `setSan(p, getSan(p) - 1)`。**

### 5.2 `setSan` 的副作用是刻意保留的

`setSan` 會發警告訊息、播 wither 音效、在跨越 -10 區間與 -30/-45 門檻時提示。環境掉落走 `setSan` 就會繼承這些。這是**刻意的決定**——黑暗中理智滑落時本來就該有提示——不是疏漏。

### 5.3 創造/旁觀模式跳過

建築時不該莫名掉 SAN。一個 if 的成本。

### 5.4 失明不影響判定

SAN ≤ -30 會施加 `BLINDNESS`（`applyLowSanDebuffs`）。失明是客戶端渲染效果，不改變 `getLightLevel` 回傳值，因此不會產生「越黑越失明越黑」的失控回饋圈。無需處理。

## 6. 已知後果：維度差異

`getLightLevel` 在**終界為全域 15**（天空光滿、ambient darkness 0），因此末地會成為永久安全區；地獄則視地形而定，岩漿密集處算亮。

這與直覺相反，但**第一版接受它**。「使用原生亮度、不自建光照系統」是本設計的明確原則，加維度特例即是開始自建。未來若要修正，只需加一個 per-dimension 係數，不影響本設計的任何其他結構。

## 7. 不在本版範圍

- E.G.O 提取機（Block + BlockEntity + ScreenHandler + 客戶端 GUI）
- 腦啡肽與核心素材物品、掉落來源
- 自訂 Extraction Recipe Type

理由：這兩項技術上毫無交集，且風險差一個量級。光照×SAN 是純加法，改動集中在一個檔案；提取機則要替這個 Repo 開六條全新技術路線（Block、BlockEntity、Recipe API、ScreenHandler、客戶端 GUI、loot table）——Repo 目前**一個方塊都沒有**，`LimbusEGOClient.onInitializeClient()` 是空的，現有三個 GUI 全是伺服端 vanilla container 的插件式做法。

提取機值得獨立的 spec 與實作計畫。

**config 系統也不在本版範圍。** 四個常數用 `private static final` 放在 `SanityManager`，與既有的 `ATK_PER_SAN`、`OUT_COMBAT_MS`、`WARN_THRESHOLD` 同風格。測平衡時改常數重編譯。等數值定案再評估是否引入 config 基礎設施，避免為四個數字架一套讀檔。

## 8. 元件切分

| 元件 | 職責 | 依賴 |
|---|---|---|
| `EnvironmentSanityLogic`（新增） | 純函式：由 (光照, 曝光值, SAN, 是否脫戰) 算出 (新曝光值, SAN 變化量, 是否抑制回復) | 無 Minecraft 依賴 |
| `SanityManager`（改） | 讀光照、呼叫上者、套用結果；持有 `exposure` map | Minecraft |
| SAN 持久化掛點（新增） | `onJoin` 讀、`onQuit` 寫 | Fabric Attachment API 或 mixin |

切開的理由：`EnvironmentSanityLogic` 無 Minecraft 依賴，可照 `GiftUpgradeLogicTest` / `StatusStateTest` 的既有慣例寫純 JUnit 測試。

## 9. 測試

`EnvironmentSanityLogicTest`（純 JUnit，不載入 Minecraft）須涵蓋：

1. **門檻邊界** —— 光照 4 判定為暗、5 判定為中性；光照 8 判定為中性、9 判定為亮
2. **累計器跨門檻** —— 連續 15 次黑暗才扣 1 點 SAN，第 15 次後歸零
3. **中性收斂** —— 中性亮度使曝光值向 0 移動，正負皆然
4. **明亮回復卡在 0** —— SAN = 0 時明亮累計跨門檻不使 SAN 變正
5. **明亮回復在負值區生效** —— SAN = -5 時明亮累計跨門檻使 SAN 變 -4
6. **黑暗抑制脫戰回復** —— 黑暗且脫戰時不回復；非黑暗且脫戰時回復
7. **SAN 夾制** —— 觸底 -45 後黑暗不再繼續扣

`SanityManager` 側的整合（光照讀取、持久化、創造模式跳過）以手動遊戲內驗證為主。

## 10. 驗收標準

第一版成功的定義：

- 玩家在點了火把的住宅內待著，SAN 不會下降
- 玩家在無光源洞穴挖礦約 10 分鐘，SAN 掉到約 -20 並收到既有的警告提示
- 玩家回到地面白天，SAN 從負值緩慢回升，**停在 0**
- 玩家短暫穿過陰影（數秒）SAN 完全不變
- 玩家在洞穴登出再登入，SAN 維持登出時的值
- 玩家死亡重生，SAN 歸 0（既有行為不變）
- 創造模式玩家不受環境影響
