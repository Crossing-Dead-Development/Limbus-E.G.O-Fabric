# 武器行為＋音效對齊插件版 實作計畫（計畫二）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 讓每把武器的戰鬥數值（傷害／冷卻／機率／狀態效果／投射物）與音效，逐把對齊 `Limbus-E.G.O` 插件。

**Architecture:** Fabric 戰鬥邏輯集中在 `WeaponEvents.java`（724 行，各武器一個 `handleXxx`）＋各 `*Item` 類別的 `use`／子類別行為。音效集中在 `ModSounds.java`＋`assets/<ns>/sounds.json`＋`.ogg`。每把武器：讀插件對應 `.java` 抽出權威數值 → 對照 Fabric handler → 列差異表 → 只修有差異者。音效先建基礎（註冊＋補 ogg）再由各 handler 引用。

**Tech Stack:** Fabric 1.21.4、Java 21、`WeaponEvents`（server tick／attack callback）、`ModSounds`（`SoundEvent` 註冊）、`sounds.json`。

## Global Constraints

- 真相來源：`C:\Users\User\IdeaProjects\Limbus-E.G.O`（合併版插件 v1.4.1）的武器 `.java`。數值一律以插件為準。
- **只修與插件有差異的部分**，不重寫既有可運作邏輯（YAGNI）；每把武器先產出「插件值→Fabric 現值→是否修正」差異表，再改。
- 音效 id／命名空間沿用插件（如 `solemnlament:solemn.hit`、`tiantui_star:tiantui.dash`）；音量／音高與插件一致。
- `.ogg` 音檔來源：優先 `Limbus-E.G.O` 插件的 resourcepack 目錄；若無則 `D:\找不到自己ㄉ電腦\MC\LSMP\Github\Limbus-E.G.O-ResourcePack` 或 `Limbus E.G.O Weapons` 的 `resourcepack/assets/<ns>/sounds/`。
- 排除插翅虎（chatuhu）／終末鳥（apocalypse）組合包道具與其專屬音效（`twilight:apocalypse_bird`、tiantui 的 `chatuhu` 相關）。
- 無單元測試框架：每個任務以 `./gradlew.bat build -q`（exit 0）為自動檢查；行為正確性由差異表佐證＋最後遊戲內驗證。
- Windows／Git Bash；Gradle wrapper 為 `./gradlew.bat`。

## 音效對照（權威，供 Task 1）

插件武器實際 `playSound` 的音效 id → Fabric 現況：

| 插件音效 id | Fabric ModSounds | 狀態 |
|---|---|---|
| solemnlament:solemn.load | SOLEMN_LOAD | 已註冊（WeaponEvents 未使用，待查是否該用） |
| solemnlament:solemn.shoot | SOLEMN_SHOOT | 已註冊＋使用 |
| solemnlament:solemn.hit | SOLEMN_HIT | 已註冊＋使用 |
| solemnlament:solemn.quick_load.* | — | **缺** |
| solemnlament:butterflies | — | **缺** |
| solemnlament:butterflies_hit | — | **缺** |
| tiantui_star:tiantui.charge_tiger | TIANTUI_CHARGE_TIGER | 已註冊＋使用 |
| tiantui_star:tiantui.charge_savage_1/2/3 | TIANTUI_CHARGE_SAV_* | 已註冊＋使用 |
| tiantui_star:tiantui.dash | TIANTUI_DASH | 已註冊＋使用 |
| tiantui_star:tiantui.slash | TIANTUI_SLASH | 已註冊＋使用 |
| tiantui_star:lei | — | **缺**（雷／召喚音？待查插件用途） |
| 其他武器（mimicry/dacapo/twilight/tibia/wcorp/bladesinger/ringbrush）| — | **待 Task 1 逐檔確認插件是否 playSound；缺則補** |

目前 Fabric 僅有 `assets/solemnlament/sounds.json`。

## 武器 → 檔案對照（供各行為任務）

| 武器 | 插件檔（行數） | Fabric handler／位置 |
|---|---|---|
| solemn_lament | solemnlament.java (270) | `SolemnLamentItem.use` ＋ WeaponEvents 投射物（onServerTick、activeProjectiles） |
| mimicry | mimicry.java (70) | `WeaponEvents.handleMimicry` (186) |
| dacapo | dacapo.java (110) | `WeaponEvents.handleDaCapo` (224) ＋ dacapoQueue |
| ring_brush | ringbrush.java (99) | `WeaponEvents.handleRingBrush` (283) |
| tiantui_star | TiantuiStar.java (457) | `TiantuiStarItem` ＋ WeaponEvents dash（activeDashes、tiantuiSavage） |
| twilight | TwilightWeapon.java (331) | `WeaponEvents.handleTwilight` (484) ＋ twilightCd |
| tibia | TibiaWeapon.java (339) | `WeaponEvents.handleTibiaMelee` (546) ＋ tibiaCd／charge |
| w_corp_knife | WCorpKnife.java (83) | `WeaponEvents.handleWCorpKnife` (206) |
| bladesinger | ShadowBladesinger.java (180) | `WeaponEvents.handleBladesinger` (421) ＋ bladesingerCd |

---

## Task 1: 音效基礎 — 註冊缺漏音效＋補 .ogg

**Files:**
- Modify: `src/main/java/me/yisang/limbusego/item/ModSounds.java`
- Modify/Create: `src/main/resources/assets/<ns>/sounds.json`（各命名空間）
- Create: `src/main/resources/assets/<ns>/sounds/**/*.ogg`（從插件 resourcepack 複製）

**Interfaces:**
- Produces: 各缺漏音效的 `public static SoundEvent XXX` 常數（供各武器 handler 於後續任務引用）

- [ ] **Step 1: 盤點插件所有武器 playSound**

Run（列出插件所有武器 `.java` 的實際 playSound 呼叫與參數）：
`grep -rnE "playSound\(" /c/Users/User/IdeaProjects/Limbus-E.G.O/src/main/java/me/yisang/limbusego/*.java`
把每筆的「音效 id、音量、音高、觸發時機」記入報告；排除 apocalypse／chatuhu。

- [ ] **Step 2: 確認每個缺漏音效有對應 .ogg**

對 Step 1 找出但 Fabric 未註冊的音效（至少：`solemn.quick_load.*`、`butterflies`、`butterflies_hit`、`tiantui:lei`，及其他武器音效），於下列來源找 `.ogg`：
- `C:\Users\User\IdeaProjects\Limbus-E.G.O` 內 `**/resourcepack/assets/<ns>/sounds/`
- `D:\找不到自己ㄉ電腦\MC\LSMP\Github\Limbus-E.G.O-ResourcePack\assets\<ns>\sounds\`
- `C:\Users\User\IdeaProjects\Limbus E.G.O Weapons\src\main\resources\resourcepack\assets\<ns>\sounds\`

複製到 `src/main/resources/assets/<ns>/sounds/<同路徑>.ogg`。記錄每個音效的來源與目的路徑；找不到 ogg 的音效於報告標明「無音檔，暫緩」。

- [ ] **Step 3: 於 sounds.json 註冊**

對每個命名空間，在 `assets/<ns>/sounds.json` 加入條目（沿用既有 solemnlament/sounds.json 的格式），例：

```json
"solemn.quick_load.1": { "sounds": [ { "name": "solemnlament:item/crossbow/quick_charge/quick1_1", "stream": false } ] }
```

（確切 `name` 路徑依 Step 2 實際 ogg 路徑；多音變體列成陣列由遊戲隨機選。）

- [ ] **Step 4: 於 ModSounds 註冊常數**

比照既有 `SOLEMN_HIT` 等寫法，為每個缺漏音效加 `public static SoundEvent` 常數與 `register("<ns>","<path>")`。例：

```java
public static SoundEvent SOLEMN_QUICK_LOAD_1;
// in register():
SOLEMN_QUICK_LOAD_1 = register("solemnlament", "solemn.quick_load.1");
```

（依 ModSounds 既有 register 簽章調整；若其 register 只吃單一 id 字串則沿用。）

- [ ] **Step 5: 編譯**

Run: `./gradlew.bat build -q`　Expected: exit 0。

- [ ] **Step 6: Commit**

```bash
git add src/main/java/me/yisang/limbusego/item/ModSounds.java src/main/resources/assets
git commit -m "feat: 補齊武器缺漏音效註冊＋ogg / Register missing weapon sounds + oggs"
```

---

## Task 2〜10: 逐把武器行為＋音效稽核

> 每把武器一個任務，流程相同（下方為通用步驟模板）。差異僅在「插件檔」與「Fabric handler」（見上方對照表）。各任務獨立、可個別 review。

**每把武器的通用步驟：**

- [ ] **Step 1: 抽取插件權威數值**

完整讀該武器插件 `.java`（見對照表），抽出並記入報告：
- 傷害：基礎、派生、真傷比例（`damage(...)`、`setDamage`、乘數）
- 冷卻：tick／秒（cooldown 常數、`setCooldown`）
- 觸發機率／消耗：機率值、彈藥數、蓄力階段、層數上限
- 狀態效果：種類、層數、持續（BLEED/BURN/POISE… 或原版效果）
- 投射物／衝刺／扇形：速度、存活時間、範圍、命中判定
- 音效：每個 `playSound` 的 id／音量／音高／時機

- [ ] **Step 2: 讀 Fabric 現值並列差異表**

讀對應 Fabric handler（`WeaponEvents.handleXxx` 及相關常數／`*Item`），對每個 Step 1 項目寫一列：`項目 | 插件值 | Fabric 現值 | 是否修正`。差異表寫入報告。

- [ ] **Step 3: 修正差異**

只改「是否修正＝是」的項目：調整 `WeaponEvents` 常數／邏輯或 `*Item`；音效改用 Task 1 註冊的 `ModSounds.*` 常數並校正音量／音高。不動一致的部分。

- [ ] **Step 4: 編譯**

Run: `./gradlew.bat build -q`　Expected: exit 0。

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "fix: <武器> 行為/音效對齊插件 / Align <weapon> behavior & sounds"
```

**任務清單（依序）：**

- [ ] **Task 2: solemn_lament** — solemnlament.java → SolemnLamentItem.use＋WeaponEvents 投射物（含 solemn.load/shoot/hit、生蝶亡蝶投射與 butterflies/butterflies_hit 音效、黑白蝶傷害 8.0/4.0）
- [ ] **Task 3: mimicry** — mimicry.java → handleMimicry
- [ ] **Task 4: dacapo** — dacapo.java → handleDaCapo＋dacapoQueue（延遲派生傷害時序）
- [ ] **Task 5: ring_brush** — ringbrush.java → handleRingBrush（BRUSH 狀態池、冷卻視窗 brushLastHit）
- [ ] **Task 6: tiantui_star** — TiantuiStar.java → TiantuiStarItem＋WeaponEvents dash（虎/猛虎彈消耗、蓄力階段音效、衝刺速度/命中、lei 音效用途）
- [ ] **Task 7: twilight** — TwilightWeapon.java → handleTwilight（低血加成上限 1.5、真傷比例 0.30、暮光斬扇形波、冷卻）
- [ ] **Task 8: tibia** — TibiaWeapon.java → handleTibiaMelee＋解剖斬（流血旋律加成、引爆、蓄力、冷卻）
- [ ] **Task 9: w_corp_knife** — WCorpKnife.java → handleWCorpKnife（過載層數上限 10、+1層/+1級效果）
- [ ] **Task 10: bladesinger** — ShadowBladesinger.java → handleBladesinger（呼吸法 poise 上限 10、肉斬骨斷觸發、冷卻）

---

## Task 11: 建置、部署與遊戲內驗證

**Files:** 無

- [ ] **Step 1: 完整建置**　Run: `./gradlew.bat build -q`　Expected: exit 0。
- [ ] **Step 2: 部署**　`cp build/libs/limbus-ego-fabric-0.1.0.jar "/c/Users/User/AppData/Roaming/ModrinthApp/profiles/LSMP/mods/limbus-ego-fabric-0.1.0.jar"`
- [ ] **Step 3: 遊戲內驗證（手動）**　逐把武器測：關鍵技能傷害感、冷卻時間、狀態效果套用、音效是否播放且正確。對照插件實際手感。

---

## Self-Review 註記

- **Spec 覆蓋**：spec §4（行為五項核對）→ 各武器 Step 1-3；spec §5（音效三檢）→ Task 1＋各武器 Step 3 音效。
- **非 placeholder 說明**：行為稽核任務本質是「以指定插件檔為權威做差異比對再修」，故步驟給定確切插件檔／Fabric handler／核對清單，實際數值於執行時由差異表產出——這是稽核任務的固有形態，非未定義內容。
- **排除項**：apocalypse／chatuhu 組合包及其音效不做。
- **相依**：Task 1（音效常數）須先於 Task 2-10（handler 引用音效）。
