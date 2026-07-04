# Limbus-E.G.O-Fabric — Limbus E.G.O 統一模組（Fabric）

繁體中文 | [English](README.en.md)

將邊獄公司（Limbus Company）的 E.G.O 武器與 E.G.O 飾品帶進 Minecraft 的 Fabric 模組，
由 Paper 插件 [Limbus-E.G.O](https://github.com/Crossing-Dead-Development/Limbus-E.G.O) v1.3.0 完整移植而來。

> ⚠️ 開發中：目前為 Phase 0 腳手架，尚無遊戲內容。功能將分三階段移植，見下方路線圖。

- **Minecraft 版本**：1.21.4
- **Loader**：Fabric Loader 0.16.9+
- **必備前置**：[Fabric API](https://modrinth.com/mod/fabric-api)、[Accessories](https://modrinth.com/mod/accessories)
- **安裝端**：客戶端與伺服器**都需要**安裝本模組與前置
- **Java**：21

## 路線圖

| 階段 | 內容 | 狀態 |
|------|------|------|
| Phase 1（v0.1.x） | 12 屬性體系、SAN 理智值、8 種 E.G.O 武器、`/limbusego weapon` 指令與圖鑑 | 🚧 進行中 |
| Phase 2（v0.2.x） | Accessories 飾品欄、殘影升級、80 件 E.G.O 飾品、`/limbusego gift` 指令 | ⬜ 未開始 |
| Phase 3（v1.0.0） | 提取箱／紡錘抽獎箱／購買商店箱、飾品圖鑑、語言切換 | ⬜ 未開始 |

## 與 Paper 插件版的差異

- 物品為原生模組物品，**不需要伺服器資源包**（材質內建）
- 飾品佩戴改用 [Accessories](https://modrinth.com/mod/accessories) 欄位（5 格通用欄），不再是 `/accessories` 自製 GUI，**「飾品欄開啟工具」道具不再存在**
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
