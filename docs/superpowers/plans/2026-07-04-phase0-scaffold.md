# Limbus-E.G.O-Fabric Phase 0 腳手架 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立可編譯、可進遊戲的空 Fabric 模組專案，含 Accessories API 相依、CI、README，並推上 `Crossing-Dead-Development/Limbus-E.G.O-Fabric`。

**Architecture:** 單一 Fabric Loom Gradle 專案，mod id `limbusego`，主入口 `me.yisang.limbusego.LimbusEGOMod`＋客戶端入口 `LimbusEGOClient`。本階段只交付骨架：入口類、空 mixin 設定、fabric.mod.json、資源骨架。後續 Phase 1-3 在此骨架上疊加。

**Tech Stack:** Minecraft 1.21.4、Fabric Loom 1.9-SNAPSHOT、Fabric Loader 0.16.9、Yarn 1.21.4+build.8、Fabric API 0.119.4+1.21.4、Accessories API（wisp-forest）、Java 21、GitHub Actions。

## Global Constraints

- 專案路徑：`C:\Users\User\IdeaProjects\Limbus-E.G.O-Fabric`（git repo 已 init，spec 已 commit）
- mod id：`limbusego`；包名：`me.yisang.limbusego`；archives base name：`limbus-ego-fabric`
- 版本號自 `0.1.0` 起（Phase 1 完成才發 pre-release）
- Java 21；所有檔案 UTF-8
- 參考來源：舊專案 `C:\Users\User\IdeaProjects\LimbusEGOWeapons-Fabric`（gradle wrapper 直接複製自它）
- Windows 環境使用 PowerShell 指令；gradle 指令一律用 `.\gradlew.bat`
- README 遵循現役 repo 慣例：繁中為主、`README.en.md` 英文版（Phase 0 先建繁中版，英文版隨 Phase 1 發版前補）

---

### Task 1: Gradle 建置骨架

**Files:**
- Create: `gradle.properties`
- Create: `settings.gradle`
- Create: `build.gradle`
- Create: `.gitignore`
- Copy: `gradlew.bat`、`gradle/wrapper/*`（來自舊專案）

**Interfaces:**
- Produces: 可執行 `.\gradlew.bat build` 的 Gradle 專案；`accessories_version` property 供 Task 2 的 fabric.mod.json 相依宣告使用

- [ ] **Step 1: 複製 gradle wrapper**

```powershell
$src = "C:\Users\User\IdeaProjects\LimbusEGOWeapons-Fabric"
$dst = "C:\Users\User\IdeaProjects\Limbus-E.G.O-Fabric"
Copy-Item "$src\gradlew.bat" $dst
Copy-Item "$src\gradle" $dst -Recurse
```

- [ ] **Step 2: 查詢 Accessories API 最新 1.21.4 Fabric 版本**

```powershell
(Invoke-RestMethod 'https://api.modrinth.com/v2/project/accessories/version?game_versions=%5B%221.21.4%22%5D&loaders=%5B%22fabric%22%5D')[0].version_number
```

記下輸出（形如 `1.2.14-beta+1.21.4`），下一步填入 `accessories_version`。

- [ ] **Step 3: 寫 `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true

# Fabric / Minecraft
minecraft_version=1.21.4
yarn_mappings=1.21.4+build.8
loader_version=0.16.9

# Mod
mod_version=0.1.0
maven_group=me.yisang
archives_base_name=limbus-ego-fabric

# Dependencies
fabric_version=0.119.4+1.21.4
accessories_version=<Step 2 查到的版本號>
```

- [ ] **Step 4: 寫 `settings.gradle`**

```groovy
pluginManagement {
    repositories {
        maven {
            name = 'Fabric'
            url = 'https://maven.fabricmc.net/'
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
rootProject.name = 'Limbus-E.G.O-Fabric'
```

- [ ] **Step 5: 寫 `build.gradle`**

```groovy
plugins {
    id 'fabric-loom' version '1.9-SNAPSHOT'
}

version = project.mod_version
group   = project.maven_group

base {
    archivesName = project.archives_base_name
}

repositories {
    maven {
        name = 'Wisp Forest'
        url = 'https://maven.wispforest.io/releases/'
    }
    maven {
        name = 'su5ed (sinytra)'
        url = 'https://maven.su5ed.dev/releases'
    }
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings  "net.fabricmc:yarn:${project.yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
    modImplementation "io.wispforest:accessories-fabric:${project.accessories_version}"
}

processResources {
    inputs.property "version",           project.version
    inputs.property "loader_version",    project.loader_version
    filteringCharset "UTF-8"

    filesMatching("fabric.mod.json") {
        expand "version":        project.version,
               "loader_version": project.loader_version
    }
}

def targetJavaVersion = 21

tasks.withType(JavaCompile).configureEach {
    it.options.encoding = "UTF-8"
    it.options.release   = targetJavaVersion
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}
```

（若 `accessories-fabric` 解析失敗，改查 Modrinth 該版本檔案的 maven 座標；Wisp Forest 文件：https://docs.wispforest.io/accessories/home/）

- [ ] **Step 6: 寫 `.gitignore`**

```gitignore
.gradle/
build/
out/
run/
.idea/
*.iml
.DS_Store
```

- [ ] **Step 7: 驗證 Gradle 可設定（尚無原始碼，build 應成功產出空 jar）**

Run: `.\gradlew.bat build --no-daemon`
Expected: `BUILD SUCCESSFUL`（首次會下載相依，需數分鐘）

- [ ] **Step 8: Commit**

```powershell
git add -A
git commit -m "build: Fabric Loom 建置骨架（1.21.4 / Loader 0.16.9 / Accessories API）"
```

---

### Task 2: Mod 入口與資源骨架

**Files:**
- Create: `src/main/java/me/yisang/limbusego/LimbusEGOMod.java`
- Create: `src/client/java/me/yisang/limbusego/client/LimbusEGOClient.java`（Loom 預設 split sources；若 build 找不到 client source set，改放 `src/main/java/me/yisang/limbusego/client/`，`fabric.mod.json` 同步指向）
- Create: `src/main/resources/fabric.mod.json`
- Create: `src/main/resources/limbusego.mixins.json`
- Create: `src/main/resources/limbusego.client.mixins.json`
- Create: `src/main/resources/assets/limbusego/lang/zh_tw.json`
- Create: `src/main/resources/assets/limbusego/lang/en_us.json`
- Copy: `src/main/resources/assets/limbusego/icon.png`（沿用舊專案 `assets/limbusweapons/icon.png`）

**Interfaces:**
- Consumes: Task 1 的 Gradle 專案與 `accessories_version`
- Produces: `LimbusEGOMod.MOD_ID = "limbusego"`、`LimbusEGOMod.LOGGER`、`LimbusEGOMod.id(String path)`（回傳 `Identifier`）— Phase 1 起所有註冊都用這三個成員

- [ ] **Step 1: 寫主入口 `LimbusEGOMod.java`**

```java
package me.yisang.limbusego;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimbusEGOMod implements ModInitializer {
    public static final String MOD_ID = "limbusego";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Limbus E.G.O Fabric 初始化完成");
    }
}
```

- [ ] **Step 2: 寫客戶端入口 `LimbusEGOClient.java`**

```java
package me.yisang.limbusego.client;

import net.fabricmc.api.ClientModInitializer;

public class LimbusEGOClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
    }
}
```

- [ ] **Step 3: 寫 `fabric.mod.json`**

```json
{
  "schemaVersion": 1,
  "id": "limbusego",
  "version": "${version}",
  "name": "Limbus E.G.O",
  "description": "Limbus Company E.G.O weapons & gifts — Fabric port of the LimbusEGO Paper plugin.",
  "authors": ["yisang"],
  "contact": {
    "sources": "https://github.com/Crossing-Dead-Development/Limbus-E.G.O-Fabric"
  },
  "license": "MIT",
  "icon": "assets/limbusego/icon.png",
  "environment": "*",
  "entrypoints": {
    "main": ["me.yisang.limbusego.LimbusEGOMod"],
    "client": ["me.yisang.limbusego.client.LimbusEGOClient"]
  },
  "mixins": [
    "limbusego.mixins.json",
    {
      "config": "limbusego.client.mixins.json",
      "environment": "client"
    }
  ],
  "depends": {
    "fabricloader": ">=${loader_version}",
    "fabric-api": "*",
    "accessories": "*",
    "minecraft": "~1.21.4"
  }
}
```

- [ ] **Step 4: 寫兩個空 mixin 設定**

`limbusego.mixins.json`：

```json
{
  "required": true,
  "package": "me.yisang.limbusego.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [],
  "injectors": {
    "defaultRequire": 1
  }
}
```

`limbusego.client.mixins.json`：

```json
{
  "required": true,
  "package": "me.yisang.limbusego.mixin.client",
  "compatibilityLevel": "JAVA_21",
  "client": [],
  "injectors": {
    "defaultRequire": 1
  }
}
```

- [ ] **Step 5: 寫語言檔（兩檔同內容起步）**

`assets/limbusego/lang/zh_tw.json`：

```json
{
  "itemGroup.limbusego.main": "Limbus E.G.O"
}
```

`assets/limbusego/lang/en_us.json`：

```json
{
  "itemGroup.limbusego.main": "Limbus E.G.O"
}
```

- [ ] **Step 6: 複製 mod icon**

```powershell
New-Item -ItemType Directory -Force "C:\Users\User\IdeaProjects\Limbus-E.G.O-Fabric\src\main\resources\assets\limbusego" | Out-Null
Copy-Item "C:\Users\User\IdeaProjects\LimbusEGOWeapons-Fabric\src\main\resources\assets\limbusweapons\icon.png" "C:\Users\User\IdeaProjects\Limbus-E.G.O-Fabric\src\main\resources\assets\limbusego\icon.png"
```

- [ ] **Step 7: 驗證 build**

Run: `.\gradlew.bat build --no-daemon`
Expected: `BUILD SUCCESSFUL`，`build/libs/limbus-ego-fabric-0.1.0.jar` 產出

- [ ] **Step 8: 驗證可進伺服器（headless smoke test）**

Run: `.\gradlew.bat runServer --no-daemon`（首次會生成 `run/`；出現 `Done (…)! For help, type "help"` 前的 log 應包含 `Limbus E.G.O Fabric 初始化完成` 與 Accessories 載入訊息；看到 Done 後手動停止或輸入 stop）
Expected: 無 crash，兩條 log 都出現

- [ ] **Step 9: Commit**

```powershell
git add -A
git commit -m "feat: mod 入口骨架（limbusego，含 Accessories 相依與空 mixin 設定）"
```

---

### Task 3: CI（GitHub Actions build）

**Files:**
- Create: `.github/workflows/build.yml`

**Interfaces:**
- Consumes: Task 1-2 的可編譯專案
- Produces: 每次 push / PR 自動 build 並上傳 jar artifact

- [ ] **Step 1: 寫 `build.yml`**

```yaml
name: build

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
      - uses: gradle/actions/setup-gradle@v4
      - name: Make gradlew executable wrapper
        run: gradle wrapper --gradle-version wrapper
        continue-on-error: true
      - name: Build
        run: gradle build
      - name: Upload artifacts
        uses: actions/upload-artifact@v4
        with:
          name: mod-jars
          path: build/libs/*.jar
```

（注意：wrapper 只從舊專案複製了 `gradlew.bat`，沒有 Unix `gradlew`，所以 CI 直接用 `gradle/actions/setup-gradle` 提供的 gradle 執行。若想改用 wrapper，補複製舊專案缺少的 Unix wrapper 腳本即可——舊專案也沒有，故用此方案。）

- [ ] **Step 2: Commit**

```powershell
git add .github
git commit -m "ci: GitHub Actions build workflow"
```

---

### Task 4: README 與 repo 建立、首次 push

**Files:**
- Create: `README.md`

**Interfaces:**
- Consumes: 全部前置 Task
- Produces: 公開 repo `Crossing-Dead-Development/Limbus-E.G.O-Fabric`，main 分支含全部內容

- [ ] **Step 1: 寫 `README.md`**

```markdown
# Limbus-E.G.O-Fabric — Limbus E.G.O 統一模組（Fabric）

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
- 飾品佩戴改用 [Accessories](https://modrinth.com/mod/accessories) 欄位（5 格通用欄），不再是 `/accessories` 自製 GUI
- 兩邊存檔互不相通

## 開發

```
.\gradlew.bat build      # 編譯
.\gradlew.bat runClient  # 啟動開發客戶端
.\gradlew.bat runServer  # 啟動開發伺服器
```

## 授權

MIT
```

- [ ] **Step 2: Commit**

```powershell
git add README.md
git commit -m "docs: README（開發中聲明與路線圖）"
```

- [ ] **Step 3: 建 GitHub repo 並 push**

```powershell
gh repo create Crossing-Dead-Development/Limbus-E.G.O-Fabric --public --source . --description "Limbus Company E.G.O weapons & gifts as a Fabric mod - port of the LimbusEGO Paper plugin" --push
```

Expected: repo 建立成功，master/main 推上

- [ ] **Step 4: 驗證 CI 綠燈**

```powershell
gh run watch --repo Crossing-Dead-Development/Limbus-E.G.O-Fabric
```

Expected: build workflow 成功；若失敗，讀 log 修正後再 push
