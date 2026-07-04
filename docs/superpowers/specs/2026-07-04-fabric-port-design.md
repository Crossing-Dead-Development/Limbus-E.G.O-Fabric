# Limbus-E.G.O-Fabric 移植設計

日期：2026-07-04
狀態：已與專案擁有者確認定案

## 1. 專案定位

- **新 repo**：`Crossing-Dead-Development/Limbus-E.G.O-Fabric`；本機路徑 `C:\Users\User\IdeaProjects\Limbus-E.G.O-Fabric`
- **形態**：雙端完整模組（客戶端＋伺服器都需安裝）
- **目標**：與 Paper 插件 `Limbus-E.G.O` v1.3.0 功能 1:1 對齊 —— 8 武器、12 屬性體系、SAN 理智值、80 飾品＋殘影升級、三種箱子（gacha／紡錘／商店）、雙語言、圖鑑與管理 GUI
- **環境**：Minecraft 1.21.4、Fabric Loader 0.16.x、Fabric API（0.119.4+1.21.4 起）、Java 21
- **mod id**：`limbusego`；包名 `me.yisang.limbusego`（與插件對齊，類別近乎同名對照移植）
- **必備前置**：Fabric API、**Accessories API**（wisp-forest，1.21.4 對應版本，實作時查 Modrinth 最新）
- 舊 `LimbusEGOWeapons-Fabric` 的已驗證基礎（物品註冊、彈幕實體、`SoundManagerMixin` 音效攔截、創造模式物品欄分頁）搬入重整；舊專案於本專案首個可用版後封存

## 2. Paper → Fabric 技術對應

| 插件側 | Fabric 側 |
|---|---|
| PDC 物品資料 | 原生自訂物品類別＋Data Component（`CUSTOM_DATA`）；不再需要 CustomModelData 與伺服器資源包 |
| Bukkit 事件（攻擊/受擊/擊殺/互動） | Fabric API 事件（`AttackEntityCallback`、`ServerLivingEntityEvents` 等）＋必要處 mixin 補洞 |
| BukkitScheduler | `ServerTickEvents` 統一 tick 排程器 |
| SAN BossBar | `ServerBossBar` |
| SAN 攻擊/攻速屬性修飾符 | 原版 `EntityAttributeModifier` |
| 箱子綁定（世界座標 → 箱型） | 以座標為鍵的 `PersistentState` 存檔 |
| 玩家飾品佩戴（5 通用欄） | **Accessories API** 自訂欄位 `limbusego:ego_gift` × 5 格 |
| 殘影升級等級（玩家 × 飾品 id） | Fabric Data Attachment API（persistent，掛玩家） |
| 自製 GUI（圖鑑/管理/箱子） | `ScreenHandler` 伺服器驅動箱型 GUI，照搬插件版面 |
| 飾品佩戴選單 | 改開 Accessories 原生佩戴介面（不自製 27 格 GUI） |
| 指令樹 `/limbusego`（別名 `lego`）＋`/accessories` | Brigadier；`limbus.admin` → permission level；全層 Tab 補完保留 |
| LangManager 雙語言熱切換 | 照搬 LangManager（語言檔入 mod 資源＋config 覆寫）；物品名另走客戶端 lang JSON |
| ProtocolLib 音效攔截（莊嚴哀悼） | 沿用舊 Fabric 模組 `SoundManagerMixin` |
| 材質 | 從 Limbus-E.G.O-ResourcePack 轉入 mod 內建 assets |

12 屬性 `StatusManager` 與 `SanityManager` 皆為純 in-memory＋原版 API 操作，邏輯逐行對照移植。

## 3. 飾品欄設計（Accessories API）

- 註冊自訂欄位類型 `limbusego:ego_gift`，數量 5，任何 E.G.O 飾品皆可放入——與插件版 5 個通用欄 1:1 對應
- 玩家從 Accessories 物品欄分頁佩戴；身上不做 3D 渲染（維持插件版行為）
- 80 件飾品各自實作 Accessories `Accessory` 介面（tick、裝備/卸下回呼）；攻擊/受擊/擊殺鉤子走 Fabric 事件層，查詢佩戴中飾品改用 Accessories capability API
- 殘影升級語意不變：升級屬於「玩家對該飾品的熟練度」，存 Data Attachment，與佩戴中的物品堆疊無關
- `/limbusego gift menu` 與 `/accessories` 保留，行為改為打開 Accessories 佩戴介面

## 4. 分階段計畫

| 階段 | 內容 | 產出 |
|---|---|---|
| Phase 0 | repo、Gradle（Fabric Loom）、mod 骨架、README、CI 雛形、首次 push | 可編譯空模組 |
| Phase 1 | 12 屬性體系 → SAN → 8 武器（彈幕、音效、材質）→ `/limbusego weapon` 指令＋武器圖鑑/管理 GUI | v0.1.x pre-release，可玩 |
| Phase 2 | Accessories 欄位註冊 → 殘影升級持久化 → 80 件飾品效果（逐批，數值對照插件）→ `/limbusego gift` 指令 | v0.2.x |
| Phase 3 | 三種箱子系統 → 飾品圖鑑/管理 GUI → 語言切換完整化 → README 中英同步 | v1.0.0 正式 Release |

每階段結束發 GitHub Release（沿用既有發布流程慣例）。

## 5. 測試與驗證

- 每階段 `gradle runClient` / `runServer` 雙端實測
- 每個武器/飾品移植時逐項核對插件原始碼數值（傷害、機率、冷卻、殘影放大係數）
- 純邏輯（屬性運算、SAN 增減規則）拆為不依賴 MC 的類別，附 JUnit 測試

## 6. 明確不做（YAGNI）

- 不做 Paper ↔ Fabric 存檔互轉（各自獨立）
- 不支援 1.21.4 以外的 Minecraft 版本
- 不做飾品 3D 身上渲染
