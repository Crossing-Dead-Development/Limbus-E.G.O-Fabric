# Phase 1 核心戰鬥 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把插件的 12 屬性體系、SAN 理智值、8 種 E.G.O 武器、`/limbusego weapon` 指令與圖鑑/管理 GUI 移植進 Fabric 模組，發 v0.1.0 pre-release。

**Architecture:** 屬性/SAN 為純伺服端 in-memory 系統：`StatusState`（純 Java，可單元測試）＋`StatusManager`/`SanityManager`（掛 Fabric 事件與 tick）。傷害乘區用一支 `LivingEntityMixin` 攔截 `damage()`。武器為原生 `Item` 子類別，材質內建。權威版數值以插件原始碼 `C:\Users\User\IdeaProjects\Limbus-E.G.O\src\main\java\me\yisang\limbusego\` 為準——**移植時逐檔對照，不憑記憶重寫**。

**Tech Stack:** 沿用 Phase 0 骨架（Loom 1.9 / MC 1.21.4 / Yarn / Fabric API / Accessories）。測試：JUnit 5（`useJUnitPlatform`）。

## Global Constraints

- 插件原始碼路徑（權威參考）：`C:\Users\User\IdeaProjects\Limbus-E.G.O\src\main\java\me\yisang\limbusego\`
- 舊 Fabric 模組（API 寫法參考）：`C:\Users\User\IdeaProjects\LimbusEGOWeapons-Fabric\src\main\java\me\yisang\limbusweapons\`
- 所有數值（傷害係數、機率、冷卻、閾值）**必須與插件版一致**；如插件內有註解說明語意，一併保留
- 物品只進自訂頁籤 `limbusego:weapons`；**不移植**插翅虎（Chatuhu）/終末鳥（Apocalypse Bird）組合包物品
- 每個 Task 結束：`.\gradlew.bat build` 綠 → commit；涉及遊戲行為的 Task 加 `runServer` 或 `runClient` 冒煙驗證
- 訊息文字 Phase 1 先硬編碼繁中常數集中在 `Messages` 類（Phase 3 才做語言切換），鍵名對齊插件 lang 檔

---

### Task 1: StatusEffect + StatusState（純邏輯＋單元測試）

**Files:**
- Create: `src/main/java/me/yisang/limbusego/status/StatusEffect.java`（照抄插件版，包含 zh/color 欄位）
- Create: `src/main/java/me/yisang/limbusego/status/StatusState.java`（照抄插件版——本來就零 Bukkit 相依）
- Create: `src/test/java/me/yisang/limbusego/status/StatusStateTest.java`
- Modify: `build.gradle`（加 JUnit 5 測試相依與 `test { useJUnitPlatform() }`）

**Interfaces:**
- Produces: `StatusEffect`（12 值 enum，欄位 `zh`、`color`）；`StatusState`：`add(e,potency,count)`、`potency(e)`、`count(e)`、`consume(e,n)→int`、`isEmpty()`、`snapshot()`

**Steps:**
- [ ] 照抄兩個類（包不變 `me.yisang.limbusego.status`）
- [ ] 寫測試：add 疊加、consume 部分/耗盡移除（potency 歸零）、consume 不存在回 0、isEmpty、snapshot 深拷貝
- [ ] `.\gradlew.bat test` 綠 → commit

### Task 2: 伺服端基礎設施（tick 排程、ActionBar、真傷、傷害乘區 mixin）

**Files:**
- Create: `src/main/java/me/yisang/limbusego/status/StatusManager.java`（對照插件版移植）
- Create: `src/main/java/me/yisang/limbusego/mixin/LivingEntityMixin.java`
- Create: `src/main/java/me/yisang/limbusego/Messages.java`（Phase 1 訊息常數）
- Modify: `limbusego.mixins.json`（登記 `LivingEntityMixin`）、`LimbusEGOMod.java`（初始化順序：Sanity → Status → 事件註冊）

**API 對應（本 Task 的移植字典）:**
- `runTaskTimer(…,10,10)` → `ServerTickEvents.END_SERVER_TICK`＋自計 tick 計數器（每 10 tick 跑一桶，4 桶輪流）
- `EntityDamageByEntityEvent`（HIGH，可改傷害）→ `LivingEntityMixin` 以 `@ModifyVariable` 注入 `LivingEntity.damage(ServerWorld, DamageSource, float)`，來源實體取自 `DamageSource.getAttacker()`；防遞迴改用執行緒安全的 `Set<UUID> inTrueDamage` 取代 Bukkit metadata
- 真傷 `target.damage(amount)` → `target.damage(world, world.getDamageSources().generic(), amount)` 包在防遞迴 Set 內；`runTask`（下一 tick）→ 排入自建的 next-tick queue（END_SERVER_TICK drain）
- ActionBar → `player.sendMessage(Text.literal(msg), true)`
- `Attribute.MOVEMENT_SPEED` modifier → `EntityAttributes.MOVEMENT_SPEED`＋`EntityAttributeModifier(Identifier, amount, Operation.ADD_MULTIPLIED_BASE)`（對應 MULTIPLY_SCALAR_1）
- potion wrapper（HASTE/BIND）→ `StatusEffects.SPEED / SLOWNESS`
- `EntityDeathEvent` → `ServerLivingEntityEvents.AFTER_DEATH`

**Steps:**
- [ ] 移植 StatusManager 全部規則（BLEED/BURN/FRAGILE/POWER/SINKING/RUPTURE/TREMOR/PROTECTION/HASTE/BIND/POISE/CHARGE），常數逐一對照
- [ ] mixin 登記、`runServer` 冒煙（`/effect`＋打殭屍手動驗證 log 不 crash）
- [ ] build 綠 → commit

### Task 3: SanityManager + SanityListener 移植

**Files:**
- Create: `src/main/java/me/yisang/limbusego/status/SanityManager.java`
- Create: `src/main/java/me/yisang/limbusego/mixin/HungerManagerMixin.java`（進食回 SAN：攔 `HungerManager.add(int,float)` 正增量）
- Modify: `LimbusEGOMod.java`

**API 對應:**
- BossBar → `ServerBossBar`（`BossBar.Color.BLUE/RED/PURPLE`、`Style.NOTCHED_10`）
- Join/Quit → `ServerPlayConnectionEvents.JOIN / DISCONNECT`
- Respawn → `ServerPlayerEvents.AFTER_RESPAWN`（alive=false 時 resetOnRespawn）
- 每 40 tick recoveryTick → 共用 Task 2 的 tick 排程器
- `System.currentTimeMillis()` 脫戰計時 → 保留（wall-clock 無妨）
- 音效 → `SoundEvents.ENTITY_WITHER_AMBIENT / ENTITY_WITHER_SPAWN`

**Steps:**
- [ ] 移植全部規則（±45 夾住、2 命中 +1 / 2 受擊 -1、脫戰 10s 負值回復、-20 警示每跨 10 響一次、-30 debuff、-45 憂鬱、屬性微調 ±0.3%/±0.15%、進食回 SAN、重生歸零）
- [ ] `runClient` 手動驗證 BossBar 顯示與變色
- [ ] build 綠 → commit

### Task 4: 物品基礎設施（ModItems、雙頁籤、ModSounds、assets）

**Files:**
- Create: `src/main/java/me/yisang/limbusego/item/ModItems.java`、`ModItemGroups.java`、`ModSounds.java`
- Create: assets（models/items/textures/sounds）——材質與音效從舊 Fabric 模組 `assets/*` 搬入並改 namespace 為 `limbusego`（排除 chatuhu / apocalypse_bird）
- Modify: lang 檔（物品顯示名，繁中＋英文）

**Interfaces:**
- Produces: `ModItems.register(String id, Function<Settings,Item>, Settings)` 註冊模式（照舊 Fabric 模組 `ModItems` 寫法）；頁籤 `WEAPONS_GROUP`（icon：莊嚴哀悼黑）與 `GIFTS_GROUP`（icon：暫用 icon.png 物品，Phase 2 換）

**Steps:**
- [ ] 註冊骨架＋兩頁籤＋音效事件；assets 搬移改名；build → `runClient` 確認頁籤出現
- [ ] commit

### Task 5: 武器第一批 — 莊嚴哀悼（黑/白）＋蝴蝶石英＋聖宣盾牌＋音效抑制 mixin

參考：插件 `solemnlament.java`、`SoundSuppressor.java`；舊模組 `SolemnLamentItem`、`ButterflyQuartzItem`、`SolemnShieldItem`、`SoundManagerMixin`、彈幕實作。
數值以**插件 v1.3.0** 為準（舊模組可能落後——衝突時以插件為準）。彈幕命中需串 Task 2 的屬性系統（插件版有施加屬性的話照施）。

- [ ] 移植 4 物品＋彈幕實體＋音效；`runClient` 實測射擊/光環
- [ ] build 綠 → commit

### Task 6: 武器第二批 — 擬態、DaCapo、環刷（ringbrush）、W社匕首（WCorpKnife）

參考：插件 `mimicry.java`、`dacapo.java`、`ringbrush.java`、`WCorpKnife.java`。攻擊鉤子用 `AttackEntityCallback`＋`ServerLivingEntityEvents.AFTER_DAMAGE`，乘區/屬性施加走 StatusManager 公開 API。

- [ ] 逐武器移植＋實測 → build 綠 → commit（可一武器一 commit）

### Task 7: 武器第三批 — 天推星（TiantuiStar）、暮暉（Twilight）、提比婭（Tibia/ShadowBladesinger）

參考：插件 `TiantuiStar.java`（396 行，含蓄力/衝刺/音效）、`TwilightWeapon.java`、`TibiaWeapon.java`＋`ShadowBladesinger.java`。這批最複雜，允許拆多個 commit。

- [ ] 逐武器移植＋實測 → build 綠 → commit

### Task 8: `/limbusego weapon` 指令樹＋圖鑑/管理 GUI

參考：插件 `CommandRouter.java`、`WeaponCatalogGUI.java`、`WeaponAdminGUI.java`；舊模組 `EgoCommand`、兩個 GUI 類。
Brigadier：`weapon give <player> <id> [count]`（permission level 2）、`weapon catalog`、`weapon admin`（level 2）、`weapon <id>`（level 2）；id 建議 provider 補完。別名 `lego`。GUI 用 `SimpleGui`／原生 `ScreenHandler`（照舊模組做法）。

- [ ] 指令＋Tab 補完＋GUI 移植；`runClient` 實測全指令
- [ ] build 綠 → commit

### Task 9: 收尾與 v0.1.0 pre-release

- [ ] README（雙語）更新：Phase 1 狀態改 ✅、武器清單章節（照插件 README 武器段落改寫為模組版）
- [ ] `gradle.properties` 確認 `mod_version=0.1.0`
- [ ] `.\gradlew.bat build` → 全綠；`runServer`＋`runClient` 最終冒煙
- [ ] push；CI 綠後 `gh release create v0.1.0 --prerelease` 附 jar
