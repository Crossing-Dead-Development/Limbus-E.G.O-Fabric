# Limbus-E.G.O-Fabric — Limbus E.G.O 統一模組（Fabric）

繁體中文 | [English](README.en.md)

將邊獄公司（Limbus Company）的 E.G.O 武器與 E.G.O 飾品帶進 Minecraft 的 Fabric 模組，
由 Paper 插件 [Limbus-E.G.O](https://github.com/Crossing-Dead-Development/Limbus-E.G.O) v1.3.0 完整移植而來。

- **版本**：0.1.0（Phase 1）
- **Minecraft 版本**：1.21.4
- **Loader**：Fabric Loader 0.16.9+
- **必備前置**：[Fabric API](https://modrinth.com/mod/fabric-api)、[Accessories](https://modrinth.com/mod/accessories)
- **安裝端**：客戶端與伺服器**都需要**安裝本模組與前置
- **Java**：21

## 路線圖

| 階段 | 內容 | 狀態 |
|------|------|------|
| Phase 1（v0.1.x） | 12 屬性體系、SAN 理智值、9 種 E.G.O 武器、`/limbusego weapon` 指令與圖鑑 | ✅ 完成 |
| Phase 2（v0.2.x） | Accessories 飾品欄、殘影鐵砧升級、**80 件 E.G.O 飾品（全部完成）**；`/limbusego gift` 指令、真材質為收尾中 | 🚧 進行中 |
| Phase 3（v1.0.0） | 提取箱／紡錘抽獎箱／購買商店箱、飾品圖鑑、語言切換 | ⬜ 未開始 |

## 武器一覽

物品收錄於「E.G.O 武器」自訂創造模式頁籤，材質內建、免資源包。

| 武器 | 屬性 | 機制 |
|---|---|---|
| 莊嚴哀悼（黑） | — | 右鍵消耗蝴蝶石英發射彈幕（1.2s 冷卻）；命中 8 傷＋凋零 II＋沉淪 4p/3c |
| 莊嚴哀悼（白） | — | 同上，命中 4 傷＋失明＋沉淪 3p/2c |
| 聖宣盾牌 | — | 持有時每 5 tick 半徑 5 格緩速 II＋束縛，自身補守護（上限 3） |
| 擬態 | +12 / −3.2 | 10% 暴擊 +40~90 傷、吸血 25%，暴擊給自己強壯 3p/4c |
| DaCapo | +7 / −2.4 | 取消一般攻擊改連擊：普通 5×1.5、特殊 3×5.0，AoE 3.5 格，每擊沉淪 1p/1c |
| 環指筆刷 | +7 / −2.4 | 右鍵造 3.5 傷＋隨機負面＋Limbus 屬性 1p/3c；1.5s 內雙擊 ×2、單擊突進 |
| W 公司匕首 | +4 / −1.6 | 命中疊充能（上限 10p，每擊 1p/5c，滿層續 count），20% 過載 +1p/1c |
| 天退星刀 | +8 / −2.4 | 右鍵蓄力 1s 衝刺（傷 8＋燒 3s＋震顫 5p/6c＋燒傷 4p/3c）；潛行蓄力 3s 猛擊（傷 18＋凋零 II＋震顫 8p/6c＋燒傷 6p/4c） |
| 薄暝 | +9 / −2.4 ＋射程 1.5 | 瀕死增傷（→×2.5）＋30% 真傷；潛行蓄力 1.5s 暮光斬（扇形＋凋零＋破裂 5p/2c） |
| 提比婭 | +10 / −2.8 ＋射程 1.0 | 疊流血 3p/2c＋Melody 增傷（每 3potency +3%，上限 30%）；潛行蓄力 2s 解剖斬（+12p/6c 流血並強制引爆 3 次） |
| 著影揮刀 | +9 / −2.6 | 疊呼吸法提高爆擊率；低血（<3 心）潛行右鍵目標 → 5 連斬 |

彈藥：**生蝶、亡蝶**（莊嚴哀悼）、**虎標彈／猛虎標彈**（天退星刀）。

## E.G.O 飾品一覽

共 **80 件飾品**，佩戴於 [Accessories](https://modrinth.com/mod/accessories) 的 5 格通用欄（`limbusego:ego_gift`），收錄於「E.G.O 飾品」自訂創造模式頁籤，材質內建、免資源包。飾品分 9 個體系，機制全對照 Paper 插件 v1.3.0：

| 體系 | 數量 | 代表機制 |
|---|---|---|
| 燒傷 burn | 8 | 攻擊施加/延長燒傷、火焰增傷 |
| 流血 bleed | 6 | 施加流血、吸血、擊殺擴散 |
| 沉淪 sinking | 10 | 施加沉淪、對沉淪/抑鬱目標增傷 |
| 破裂 rupture | 11 | 施加/延長破裂、束縛掛靠、擊殺擴散 |
| 震顫 tremor | 6 | 施加震顫、連鎖打擊、死亡落雷反擊 |
| 呼吸法 poise | 7 | 攻擊自身疊呼吸法、群體增益、回 SAN |
| 輔助 support | 15 | 強壯/守護/迅捷增益、瞬移背刺、免死 |
| 便利 qol | 12 | 吸取掉落物與經驗、免死回血、複製掉落、天氣增益 |
| 原創 original | 5 | 免疫飢餓/火焰、擊殺擴散沉淪、被動再生與回 SAN |

**殘影鐵砧升級**：以四階殘影（漆黑／黯淡／閃爍／璀璨 → 階級 1~4）在鐵砧升級同階飾品，等級 1→3 逐級提高效果倍率（1.25 / 1.50 / 2.00）。

## 12 屬性體系

每個實體追蹤 `(potency, count)` 雙軸，全 in-memory、死亡/卸載自動清除。

| 屬性 | 效果 |
|------|------|
| 流血 BLEED | 帶血者攻擊時消耗 1 count → 對自己造 potency × 0.5 真傷 |
| 燒傷 BURN | 每 2 秒消耗 1 count → potency 真傷（DoT） |
| 易損 FRAGILE | 承傷乘 (1 + potency × 15%) |
| 強壯 POWER | 出手乘 (1 + potency × 10%)；每次出手 −1 count |
| 沉淪 SINKING | 受擊消耗 1 count → potency 真傷 + 玩家 SAN −1（觸底轉憂鬱 ×1.5）；potency 越高移速越低（−2%/potency，上限 −50%） |
| 破裂 RUPTURE | 受擊消耗 1 count → potency × 2 真傷 |
| 震顫 TREMOR | potency ≥ 5 受擊 → 爆發：消耗全部造 potency × 3 真傷 + 派生燒傷 5p/3c |
| 守護 PROTECTION | 承傷乘 (1 − potency × 5%)，在易損之前套用 |
| 迅捷 HASTE | Speed potion wrapper（amplifier = potency−1，duration = count 秒） |
| 束縛 BIND | Slowness potion wrapper，同上 |
| 呼吸法 POISE | 出手 min(60%, potency × 5%) 機率爆擊 ×1.75；每次出手 −1 count |
| 充能 CHARGE | 出手乘 (1 + potency × 3%)；每次出手 −1 count |

**理智值 SAN**：每位玩家一條 BossBar（範圍 −45 ~ +45），命中／受擊／沉淪增減 SAN，脫戰後負值自動回升、進食可回 SAN，並微調攻擊力與移速；−30 以下恐慌、−45 理智觸底會疊加負面狀態效果。死亡重生後 SAN 歸零。

## 指令

`/limbusego`（別名 `/lego`）：

| 指令 | 說明 | 權限 |
|---|---|---|
| `/limbusego weapon give <玩家> <id> [數量]` | 給予武器 | 權限 2 |
| `/limbusego weapon catalog` | 開啟武器圖鑑（唯讀） | 所有人 |
| `/limbusego weapon admin` | 開啟武器管理員 GUI（點擊取得） | 權限 2 |
| `/limbusego weapon <id>` | 直接給自己一把 | 權限 2 |

## 與 Paper 插件版的差異

- 物品為原生模組物品，**不需要伺服器資源包**（材質內建）
- 飾品佩戴改用 [Accessories](https://modrinth.com/mod/accessories) 欄位（5 格通用欄），不再是 `/accessories` 自製 GUI，**「飾品欄開啟工具」道具不再存在**（Phase 2）
- 物品收錄於兩個自訂創造模式頁籤（「E.G.O 武器」／「E.G.O 飾品」），不進原版頁籤
- **不移植「插翅虎」與「終末鳥」組合包物品**
- 兩邊存檔互不相通

## 開發

```
.\gradlew.bat build      # 編譯
.\gradlew.bat runClient  # 啟動開發客戶端
.\gradlew.bat runServer  # 啟動開發伺服器
```

## 授權

MIT
