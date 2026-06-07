# bilimiao2 Compose Desktop 迁移计划

> 核心策略：**优先将 `bilimiao-comm` 转为不依赖 Android 的 KMP 通用模块**，再逐步迁移上层模块
> 数据库方案：Room KMP
> ViewModel 方案：AndroidX Lifecycle ViewModel KMP
> 创建日期：2026-06-01

---

## 总体架构目标

```
当前结构：                          目标结构：
┌──────────┐                     ┌──────────────┐
│   app    │                     │   app (Android)  │  app-desktop (JVM)
├──────────┤                     ├──────────────┤
│compose   │                     │  compose (KMP)   │
├──────────┤                     ├──────────────┤
│  comm    │  ← 全部依赖 Android  │  comm (KMP)      │  ← commonMain 无 Android 依赖
├──────────┤                     ├──────────────┤
│DanmakuFM │                     │DanmakuFM (保留)  │
└──────────┘                     └──────────────┘
```

## 迁移总览

```
Phase 1: bilimiao-comm 转为 KMP 通用模块（本计划核心）
  Step 1  ── Gradle 多平台结构搭建
  Step 2  ── 实体层（Entity）迁移到 commonMain
  Step 3  ── 纯 Kotlin 工具迁移到 commonMain
  Step 4  ── 网络层抽象（接口 → commonMain，实现 → 平台）
  Step 5  ── API 层迁移到 commonMain
  Step 6  ── Protobuf/gRPC 迁移到 commonMain
  Step 7  ── 数据库层迁移（SQLiteOpenHelper → Room KMP）
  Step 8  ── 持久化层迁移（DataStore / SharedPreferences → Room KMP）
  Step 9  ── 剩余工具类迁移
  Step 10 ── Store 层重构（ViewModel KMP 化 + 去 Context 依赖）
  Step 11 ── BilimiaoCommApp 重构
  Step 12 ── commonMain 零 Android 依赖验证

Phase 2: bilimiao-compose 转为 KMP（后续）
Phase 3: 创建 Desktop 入口 app-desktop（后续）
Phase 4: 播放器 / 弹幕 / WebView 替换（后续）
Phase 5: 清理与打包（后续）
```

---

# Phase 1: bilimiao-comm 转为 KMP 通用模块

---

## Step 1: Gradle 多平台结构搭建

### 目标
将 `bilimiao-comm` 从纯 Android Library 改造为 KMP 模块，添加 `commonMain`、`androidMain`、`jvmMain` 三个源集。改造后 Android 端编译必须仍然通过。

### 1.1 修改根 build.gradle.kts

```kotlin
// build.gradle.kts (根)
plugins {
    // 保留现有
    id("com.android.library") version "..." apply false
    // 添加 KMP
    id("org.jetbrains.kotlin.multiplatform") version "2.0.20" apply false
    id("com.google.devtools.ksp") version "2.0.20-1.0.25" apply false
    id("androidx.room") version "2.7.0-alpha13" apply false
}
```

### 1.2 修改 bilimiao-comm/build.gradle.kts

```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    jvm("desktop")  // Desktop JVM target

    sourceSets {
        commonMain.dependencies {
            // 多平台依赖
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation("com.squareup.okhttp3:okhttp:4.10.0")  // OkHttp 支持 JVM
            implementation("pro.streem.pbandk:pbandk-runtime:0.16.0")

            // Lifecycle ViewModel KMP（支持 Android/Desktop/iOS）
            implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

            // Room KMP
            implementation("androidx.room:room-runtime:2.7.0-alpha13")
            implementation("androidx.sqlite:sqlite-bundled:2.5.0-alpha13")

            // Kodein DI（纯 JVM 版）
            implementation("org.kodein.di:kodein-di:7.12.0")
        }

        androidMain.dependencies {
            implementation("androidx.datastore:datastore-preferences:1.1.1")
            implementation("com.kongzue.dialogx:DialogX:0.0.50.beta37")
            implementation("com.github.bumptech.glide:glide:4.13.2")
        }

        desktopMain.dependencies {
            // Desktop 特有依赖（后续按需添加）
        }
    }
}

android {
    namespace = "com.a10miaomiao.bilimiao.comm"
    compileSdk = 35
    defaultConfig { minSdk = 23 }
}

room {
    schemaDirectory("$projectDir/schemas")
}
```

### 1.3 创建源集目录

```
bilimiao-comm/src/
├── main/                          ← 现有 Android 代码，后续逐步移出
│   ├── java/
│   ├── proto/
│   └── AndroidManifest.xml
├── commonMain/
│   └── kotlin/
│       └── com/a10miaomiao/bilimiao/comm/
├── androidMain/
│   └── kotlin/
│       └── com/a10miaomiao/bilimiao/comm/
└── desktopMain/
    └── kotlin/
        └── com/a10miaomiao/bilimiao/comm/
```

### 1.4 初始文件分配策略

> **关键原则**：先将文件从 `src/main/java/` 移动到对应源集，不要修改文件内容。每一步只移动一类文件，确认编译通过后再继续。

初始状态下，所有现有文件保留在 `src/main/java/`（它同时被 `androidMain` 和 `desktopMain` 引用，但这会导致 Desktop 编译失败）。因此需要**先移动纯 Kotlin 文件到 commonMain**，再逐步处理有 Android 依赖的文件。

### 验证
- `./gradlew :bilimiao-comm:assembleDebug` 通过（Android 回归）
- 目录结构已创建

---

## Step 2: 实体层迁移到 commonMain

### 目标
将 111 个实体类文件移入 `commonMain`，消除 `Parcelable` 依赖。

### 2.1 移动纯 Kotlin 实体（94 个文件，零改动）

这些文件只使用 `kotlinx.serialization.Serializable`，**无需任何代码修改**，直接移动：

```
# 移动命令示例（批量）
src/main/java/com/a10miaomiao/bilimiao/comm/entity/  →  commonMain/kotlin/com/a10miaomiao/bilimiao/comm/entity/
```

涉及目录：
- `entity/archive/` — 全部
- `entity/article/` — 全部
- `entity/audio/` — 全部
- `entity/auth/` — 全部
- `entity/bangumi/` — 部分（DimensionXInfo 除外，需 Step 2.2）
- `entity/comm/` — 全部（BadgeInfo 除外）
- `entity/history/` — 全部
- `entity/home/` — 全部
- `entity/live/` — 全部
- `entity/media/` — 全部
- `entity/message/` — 全部
- `entity/miao/` — 全部
- `entity/player/` — 部分（PlayList* 除外）
- `entity/region/` — 全部（RegionInfo/RegionChildrenInfo 除外）
- `entity/search/` — 全部
- `entity/user/` — 部分（MemberInfo/UserEmoteInfo 除外）
- `entity/video/` — 部分（6 个 @Parcelize 文件除外）

### 2.2 修改并移动 @Parcelize 实体（17 个文件）

对每个文件执行以下操作：

```kotlin
// Before
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Serializable
data class SeasonInfo(...) : Parcelable

// After（移除 2 行 import，移除 @Parcelize 注解和 : Parcelable）
@Serializable
data class SeasonInfo(...)
```

**完整文件清单**：

| # | 文件 | 当前状态 | 操作 |
|---|---|---|---|
| 1 | `entity/bangumi/DimensionXInfo.kt` | `@Parcelize` 无 `@Serializable` | 移除 `@Parcelize`，添加 `@Serializable` |
| 2 | `entity/bangumi/EpisodeInfo.kt` | `@Parcelize @Serializable` | 移除 `@Parcelize` 和 `: Parcelable` |
| 3 | `entity/bangumi/SeasonInfo.kt` | `@Parcelize @Serializable` | 移除 `@Parcelize` 和 `: Parcelable` |
| 4 | `entity/comm/bili/BadgeInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 5 | `entity/player/PlayListFrom.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 6 | `entity/player/PlayListInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 7 | `entity/player/PlayListItemInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 8 | `entity/region/RegionChildrenInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 9 | `entity/region/RegionInfo.kt` | `@Parcelize @Serializable` | 移除 `@Parcelize` 和 `: Parcelable` |
| 10 | `entity/user/MemberInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 11 | `entity/user/UserEmoteInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 12 | `entity/video/VideoOwnerInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 13 | `entity/video/VideoStatInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 14 | `entity/video/UgcEpisodeInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 15 | `entity/video/UgcSeasonInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 16 | `entity/video/UgcSectionInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |
| 17 | `entity/video/VideoCommentReplyInfo.kt` | `@Parcelize` | 移除 `@Parcelize`，添加 `@Serializable` |

注意：引用链也要确保 `@Serializable`，如 `PlayListInfo` 引用 `PlayListFrom` + `PlayListItemInfo`，`EpisodeInfo` 引用 `BadgeInfo` + `DimensionXInfo`。

### 2.3 移除 kotlin-parcelize 插件

在确认 `bilimiao-comm` 中无 `@Parcelize` 使用后：
```kotlin
// bilimiao-comm/build.gradle.kts
// 删除: id("kotlin-parcelize")
```

### 验证
- `commonMain/kotlin/.../entity/` 下 111 个文件
- 全局搜索 `@Parcelize` 在 bilimiao-comm 中结果为 0
- `./gradlew :bilimiao-comm:assembleDebug` 通过
- `./gradlew :bilimiao-comm:desktopMainClasses` 通过（entity 层可在 Desktop 编译）

---

## Step 3: 纯 Kotlin 工具迁移到 commonMain

### 目标
将不含任何 `android.*` 依赖的文件先行移入 `commonMain`。

### 可直接移动的文件（零改动）

| 文件 | 原因 |
|---|---|
| `miao/MiaoJson.kt` | 纯 `kotlinx.serialization` |
| `utils/MiaoEncryptDecrypt.kt` | 纯 Kotlin XOR 加密 |
| `utils/BiliGeetestUtil.kt` | 纯 Kotlin 接口 |
| `network/BiliHeaders.kt` | 纯常量对象 |
| `network/GRPCMethod.kt` | 纯 Kotlin 数据类 |
| `datastore/SettingConstants.kt` | 纯常量对象 |
| `exception/` 目录下所有文件 | 纯 Kotlin 异常类 |
| `mypage/SearchConfigInfo.kt` | 如仅含数据类定义 |

### 验证
- `./gradlew :bilimiao-comm:desktopMainClasses` 通过
- 移动的文件在 commonMain 中可被 androidMain 和 desktopMain 引用

---

## Step 4: 网络层抽象

### 目标
将网络层的 3 处 Android 依赖（CookieManager、Build 信息、Base64）抽象为接口，核心逻辑移入 `commonMain`，平台实现在 `androidMain` / `desktopMain`。

### 4.1 创建平台抽象接口（commonMain）

```
commonMain/kotlin/com/a10miaomiao/bilimiao/comm/platform/
├── CookieProvider.kt
├── DeviceInfoProvider.kt
├── AppContextProvider.kt
└── Base64Provider.kt
```

```kotlin
// commonMain/.../platform/CookieProvider.kt
package com.a10miaomiao.bilimiao.comm.platform

interface CookieProvider {
    fun getCookie(url: String?): String
    fun setCookie(domain: String, cookie: String)
    fun removeAll()
}
```

```kotlin
// commonMain/.../platform/DeviceInfoProvider.kt
package com.a10miaomiao.bilimiao.comm.platform

interface DeviceInfoProvider {
    val brand: String       // "Xiaomi" / "Windows"
    val model: String       // "Mi 10" / "Desktop"
    val osVersion: String   // "13" / "10.0"
    val systemUserAgent: String  // 完整 User-Agent
}
```

```kotlin
// commonMain/.../platform/AppContextProvider.kt
package com.a10miaomiao.bilimiao.comm.platform

import java.io.File

interface AppContextProvider {
    val filesDir: File
    val appName: String
}
```

```kotlin
// commonMain/.../platform/Base64Provider.kt
package com.a10miaomiao.bilimiao.comm.platform

interface Base64Provider {
    fun encode(data: ByteArray, noPadding: Boolean = false): String
    fun decode(str: String): ByteArray
}
```

### 4.2 创建 Android 平台实现（androidMain）

```kotlin
// androidMain/.../platform/AndroidCookieProvider.kt
package com.a10miaomiao.bilimiao.comm.platform

import android.webkit.CookieManager

class AndroidCookieProvider : CookieProvider {
    private val manager = CookieManager.getInstance()
    override fun getCookie(url: String?) = manager.getCookie(url) ?: ""
    override fun setCookie(domain: String, cookie: String) {
        manager.setCookie(domain, cookie)
        manager.flush()
    }
    override fun removeAll() {
        manager.removeAllCookies(null)
        manager.flush()
    }
}
```

```kotlin
// androidMain/.../platform/AndroidDeviceInfoProvider.kt
package com.a10miaomiao.bilimiao.comm.platform

import android.content.Context
import android.os.Build
import android.webkit.WebSettings

class AndroidDeviceInfoProvider(private val context: Context) : DeviceInfoProvider {
    override val brand = Build.BRAND
    override val model = Build.MODEL
    override val osVersion = Build.VERSION.RELEASE
    override val systemUserAgent: String
        get() = try {
            WebSettings.getDefaultUserAgent(context)
        } catch (e: Exception) {
            System.getProperty("http.agent") ?: ""
        }
}
```

```kotlin
// androidMain/.../platform/AndroidBase64Provider.kt
package com.a10miaomiao.bilimiao.comm.platform

import android.util.Base64

class AndroidBase64Provider : Base64Provider {
    override fun encode(data: ByteArray, noPadding: Boolean): String {
        val flags = if (noPadding) Base64.NO_PADDING or Base64.NO_WRAP else Base64.DEFAULT
        return Base64.encodeToString(data, flags)
    }
    override fun decode(str: String): ByteArray = Base64.decode(str, Base64.DEFAULT)
}
```

```kotlin
// androidMain/.../platform/AndroidAppContextProvider.kt
package com.a10miaomiao.bilimiao.comm.platform

import android.app.Application
import java.io.File

class AndroidAppContextProvider(private val app: Application) : AppContextProvider {
    override val filesDir: File get() = app.filesDir
    override val appName = "bilimiao"
}
```

### 4.3 创建 Desktop 平台实现（desktopMain）

```kotlin
// desktopMain/.../platform/JvmCookieProvider.kt
package com.a10miaomiao.bilimiao.comm.platform

class JvmCookieProvider : CookieProvider {
    private val store = mutableMapOf<String, String>()
    override fun getCookie(url: String?): String {
        if (url == null) return ""
        return store.entries
            .filter { url.contains(it.key) }
            .joinToString("; ") { it.value }
    }
    override fun setCookie(domain: String, cookie: String) {
        store[domain] = cookie
    }
    override fun removeAll() { store.clear() }
}
```

```kotlin
// desktopMain/.../platform/JvmDeviceInfoProvider.kt
package com.a10miaomiao.bilimiao.comm.platform

class JvmDeviceInfoProvider : DeviceInfoProvider {
    override val brand = System.getProperty("os.name") ?: "Unknown"
    override val model = System.getProperty("os.arch") ?: "Unknown"
    override val osVersion = System.getProperty("os.version") ?: "Unknown"
    override val systemUserAgent =
        "Mozilla/5.0 (${System.getProperty("os.name")}; ${System.getProperty("os.arch")}) BilimiaoDesktop/2.5.0"
}
```

```kotlin
// desktopMain/.../platform/JvmBase64Provider.kt
package com.a10miaomiao.bilimiao.comm.platform

import java.util.Base64

class JvmBase64Provider : Base64Provider {
    override fun encode(data: ByteArray, noPadding: Boolean): String {
        val encoder = if (noPadding) Base64.getEncoder().withoutPadding() else Base64.getEncoder()
        return encoder.encodeToString(data)
    }
    override fun decode(str: String): ByteArray = Base64.getDecoder().decode(str)
}
```

```kotlin
// desktopMain/.../platform/JvmAppContextProvider.kt
package com.a10miaomiao.bilimiao.comm.platform

import java.io.File

class JvmAppContextProvider : AppContextProvider {
    override val filesDir = File(System.getProperty("user.home"), ".bilimiao").also { it.mkdirs() }
    override val appName = "bilimiao"
}
```

### 4.4 修改网络层核心文件 → commonMain

**修改 `network/ApiHelper.kt`**:

```kotlin
// Before
import android.os.Build

val USER_AGENT = """
    |...model/${Build.MODEL}...osVer/${Build.VERSION.RELEASE}...
""".trimMargin()

// After
import com.a10miaomiao.bilimiao.comm.platform.DeviceInfoProvider

// 移除 object 中的顶层初始化，改为运行时注入
lateinit var deviceInfoProvider: DeviceInfoProvider

val USER_AGENT get() = """
    |Mozilla/5.0 BiliDroid/1.45.0 (bbcallen@gmail.com)
    |os/android model/${deviceInfoProvider.model}
    |mobi_app/android_hd build/${BUILD_VERSION}
    |channel/bili innerVer/${BUILD_VERSION}
    |osVer/${deviceInfoProvider.osVersion} network/2
""".trimMargin().replace("\n", "")
```

**修改 `network/BiliGRPCConfig.kt`**:

```kotlin
// Before
import android.os.Build
import android.util.Base64
import android.webkit.WebSettings

fun getDeviceBin(): String {
    val msg = Device(..., brand = Build.BRAND, model = Build.MODEL, osver = Build.VERSION.RELEASE)
    return toBase64(msg.encodeToByteArray())
}
fun toBase64(data: ByteArray): String = Base64.encodeToString(data, Base64.NO_PADDING or Base64.NO_WRAP)
fun getSystemUserAgent(): String { ... WebSettings.getDefaultUserAgent(...) ... }

// After
import com.a10miaomiao.bilimiao.comm.platform.Base64Provider
import com.a10miaomiao.bilimiao.comm.platform.DeviceInfoProvider

lateinit var base64Provider: Base64Provider

fun getDeviceBin(): String {
    val msg = Device(..., brand = deviceInfoProvider.brand, model = deviceInfoProvider.model, osver = deviceInfoProvider.osVersion)
    return toBase64(msg.encodeToByteArray())
}
fun toBase64(data: ByteArray): String = base64Provider.encode(data, noPadding = true)
fun getSystemUserAgent(): String = deviceInfoProvider.systemUserAgent
```

**修改 `network/MiaoHttp.kt`**:

```kotlin
// Before
import android.webkit.CookieManager
private val cookieManager by lazy { CookieManager.getInstance() }
private fun getCookie(url: String?): String = cookieManager?.getCookie(url) ?: ""

// After
import com.a10miaomiao.bilimiao.comm.platform.CookieProvider

lateinit var cookieProvider: CookieProvider
private fun getCookie(url: String?): String = cookieProvider.getCookie(url)
```

**修改 `network/BiliGRPCHttp.kt`**:
- 无 `android.*` import，但依赖 `BiliGRPCConfig` 和 `BilimiaoCommApp`
- 将 `BiliGRPCHttp.kt` 移入 commonMain（依赖已在 commonMain 的接口）

### 4.5 移动修改后的文件

```
network/ApiHelper.kt      → commonMain  （移除 android.os.Build，使用 deviceInfoProvider）
network/BiliGRPCConfig.kt  → commonMain  （移除 android.os.Build/Base64/WebSettings，使用 provider）
network/MiaoHttp.kt        → commonMain  （移除 CookieManager，使用 cookieProvider）
network/BiliGRPCHttp.kt    → commonMain  （无直接 android 依赖）
network/BiliHeaders.kt     → commonMain  （已在 Step 3 移动）
network/GRPCMethod.kt      → commonMain  （已在 Step 3 移动）
```

### 验证
- `commonMain` 中 `network/` 目录下 6 个文件
- `commonMain` 中无 `import android.*`
- `./gradlew :bilimiao-comm:desktopMainClasses` 通过

---

## Step 5: API 层迁移到 commonMain

### 目标
将 15 个 API 定义文件移入 `commonMain`。12 个已是纯 Kotlin，3 个需小幅修改。

### 5.1 直接移动的文件（12 个，零改动）

这些文件只使用 `MiaoHttp` 和 `ApiHelper`（已在 commonMain）：
- `apis/ArchiveApi.kt`
- `apis/ArticleAPI.kt`
- `apis/AudioAPI.kt`
- `apis/BangumiAPI.kt`
- `apis/CommentApi.kt`
- `apis/LiveApi.kt`
- `apis/MessageAPI.kt`
- `apis/RegionAPI.kt`
- `apis/SearchApi.kt`
- `apis/UserApi.kt`
- `apis/UserRelationApi.kt`
- `apis/VideoAPI.kt`

### 5.2 需修改的文件（3 个）

**`apis/PlayerAPI.kt`**:
```kotlin
// Before
import android.os.SystemClock  // 用于 session timing
// After
// 替换为 System.currentTimeMillis() 或 kotlin.time
```

**`apis/HomeApi.kt`**:
```kotlin
// Before
import android.os.Build
val device: String = Build.DEVICE
// After
val device: String = deviceInfoProvider.model  // 使用 platform 抽象
```

**`apis/AuthApi.kt`**:
```kotlin
// Before
import android.webkit.CookieManager
val cookie = CookieManager.getInstance().getCookie(url)
// After
val cookie = cookieProvider.getCookie(url)
```

### 验证
- `commonMain/kotlin/.../apis/` 下 15 个文件
- `./gradlew :bilimiao-comm:desktopMainClasses` 通过

---

## Step 6: Protobuf/gRPC 迁移到 commonMain

### 目标
将 66 个 proto 文件和 gRPC 相关代码移入 commonMain。

### 操作

1. **Proto 文件移动**:
```
src/main/proto/  →  commonMain/proto/
```

2. **gRPC Generator 保持不变** — `grpc-generator` 模块是纯 JVM Java 工具，无需修改

3. **生成的代码** 会自动进入 commonMain（pbandk 已是 KMP 库）

### 验证
- Proto 文件在 commonMain/proto/ 下
- 生成的 Kotlin 代码在 commonMain 中可编译

---

## Step 7: 数据库层迁移（Room KMP）

### 目标
将 4 个 `SQLiteOpenHelper` 类替换为 Room KMP Entity + DAO，移入 commonMain。

### 7.1 创建 Room Entity（commonMain）

```kotlin
// commonMain/.../db/entity/FilterTagEntity.kt
package com.a10miaomiao.bilimiao.comm.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_tag")
data class FilterTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
```

```kotlin
// commonMain/.../db/entity/FilterWordEntity.kt
@Entity(tableName = "filter_world")  // 保留原表名兼容旧数据
data class FilterWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyword: String
)
```

```kotlin
// commonMain/.../db/entity/FilterUpperEntity.kt
@Entity(tableName = "filter_upper")
data class FilterUpperEntity(
    @PrimaryKey val mid: Long,
    val name: String
)
```

```kotlin
// commonMain/.../db/entity/SearchHistoryEntity.kt
@Entity(tableName = "PreventKeyWord2")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyword: String,
    val type: String = "video"
)
```

```kotlin
// commonMain/.../db/entity/KeyValueEntity.kt
// 用于替代 SharedPreferences（buvid、proxy_upos 等少量 KV 数据）
@Entity(tableName = "key_value_store")
data class KeyValueEntity(
    @PrimaryKey val key: String,
    val value: String
)
```

### 7.2 创建 Room DAO（commonMain）

```kotlin
// commonMain/.../db/dao/FilterTagDao.kt
package com.a10miaomiao.bilimiao.comm.db.dao

import androidx.room.*
import com.a10miaomiao.bilimiao.comm.db.entity.FilterTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterTagDao {
    @Query("SELECT name FROM filter_tag ORDER BY id ASC")
    fun queryAllNames(): Flow<List<String>>

    @Insert
    suspend fun insert(tag: FilterTagEntity)

    @Query("UPDATE filter_tag SET name = :newName WHERE name = :oldName")
    suspend fun updateTagName(oldName: String, newName: String)

    @Query("DELETE FROM filter_tag WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("DELETE FROM filter_tag WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM filter_tag")
    suspend fun deleteAll()
}
```

```kotlin
// commonMain/.../db/dao/FilterWordDao.kt
@Dao
interface FilterWordDao {
    @Query("SELECT keyword FROM filter_world ORDER BY id ASC")
    fun queryAllKeywords(): Flow<List<String>>

    @Insert
    suspend fun insert(word: FilterWordEntity)

    @Query("UPDATE filter_world SET keyword = :new WHERE keyword = :old")
    suspend fun updateKeyword(old: String, new: String)

    @Query("DELETE FROM filter_world WHERE keyword = :keyword")
    suspend fun deleteByKeyword(keyword: String)

    @Query("DELETE FROM filter_world WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM filter_world")
    suspend fun deleteAll()
}
```

```kotlin
// commonMain/.../db/dao/FilterUpperDao.kt
@Dao
interface FilterUpperDao {
    @Query("SELECT * FROM filter_upper")
    fun queryAll(): Flow<List<FilterUpperEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(upper: FilterUpperEntity)

    @Query("DELETE FROM filter_upper WHERE mid = :mid")
    suspend fun deleteByMid(mid: Long)
}
```

```kotlin
// commonMain/.../db/dao/SearchHistoryDao.kt
@Dao
interface SearchHistoryDao {
    @Query("SELECT keyword FROM PreventKeyWord2 ORDER BY id DESC")
    fun queryAllKeywords(): Flow<List<String>>

    @Insert
    suspend fun insert(history: SearchHistoryEntity)

    @Query("DELETE FROM PreventKeyWord2 WHERE keyword = :keyword")
    suspend fun deleteByKeyword(keyword: String)

    @Query("DELETE FROM PreventKeyWord2 WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM PreventKeyWord2")
    suspend fun deleteAll()
}
```

```kotlin
// commonMain/.../db/dao/KeyValueDao.kt
@Dao
interface KeyValueDao {
    @Query("SELECT value FROM key_value_store WHERE `key` = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putValue(entity: KeyValueEntity)
}
```

### 7.3 创建 Room Database（commonMain）

```kotlin
// commonMain/.../db/BilimiaoDatabase.kt
package com.a10miaomiao.bilimiao.comm.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.a10miaomiao.bilimiao.comm.db.dao.*
import com.a10miaomiao.bilimiao.comm.db.entity.*

@Database(
    entities = [
        FilterTagEntity::class,
        FilterWordEntity::class,
        FilterUpperEntity::class,
        SearchHistoryEntity::class,
        KeyValueEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class BilimiaoDatabase : RoomDatabase() {
    abstract fun filterTagDao(): FilterTagDao
    abstract fun filterWordDao(): FilterWordDao
    abstract fun filterUpperDao(): FilterUpperDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun keyValueDao(): KeyValueDao
}
```

### 7.4 创建平台 DatabaseBuilder（androidMain / desktopMain）

```kotlin
// androidMain/.../db/DatabaseBuilder.kt
package com.a10miaomiao.bilimiao.comm.db

import android.content.Context
import androidx.room.Room

fun buildDatabase(context: Context): BilimiaoDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        BilimiaoDatabase::class.java,
        "filter_db"
    ).build()
}
```

```kotlin
// desktopMain/.../db/DatabaseBuilder.kt
package com.a10miaomiao.bilimiao.comm.db

import androidx.room.Room
import java.io.File

fun buildDatabase(): BilimiaoDatabase {
    val dbDir = File(System.getProperty("user.home"), ".bilimiao")
    dbDir.mkdirs()
    return Room.databaseBuilder<BilimiaoDatabase>(
        name = File(dbDir, "filter_db.db").absolutePath
    ).build()
}
```

### 7.5 删除旧文件
- 删除 `src/main/java/.../db/FilterTagDB.kt`
- 删除 `src/main/java/.../db/FilterWordDB.kt`
- 删除 `src/main/java/.../db/FilterUpperDB.kt`
- 删除 `src/main/java/.../db/SearchHistoryDB.kt`

### 验证
- `commonMain/.../db/` 下包含 Entity、DAO、Database 定义
- `./gradlew :bilimiao-comm:desktopMainClasses` 通过

---

## Step 8: 持久化层迁移

### 目标
将 AndroidX DataStore 和 SharedPreferences 替换为 Room KMP 的 `key_value_store` 表。

### 8.1 重写 SettingPreferences → commonMain

```kotlin
// Before (androidMain)
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

object SettingPreferences {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    suspend fun edit(context: Context, transform: suspend (MutablePreferences) -> Unit) {
        context.dataStore.edit { transform(it) }
    }

    val IsBestRegion = booleanPreferencesKey("is_best_region")
    // ... 50+ preference keys
}

// After (commonMain)
package com.a10miaomiao.bilimiao.comm.datastore

import com.a10miaomiao.bilimiao.comm.db.dao.KeyValueDao
import com.a10miaomiao.bilimiao.comm.db.entity.KeyValueEntity

object SettingPreferences {

    // 读取
    suspend fun getString(dao: KeyValueDao, key: String, default: String = ""): String {
        return dao.getValue("s_$key") ?: default
    }

    suspend fun getBoolean(dao: KeyValueDao, key: String, default: Boolean = false): Boolean {
        return dao.getValue("s_$key")?.toBoolean() ?: default
    }

    suspend fun getInt(dao: KeyValueDao, key: String, default: Int = 0): Int {
        return dao.getValue("s_$key")?.toIntOrNull() ?: default
    }

    suspend fun getLong(dao: KeyValueDao, key: String, default: Long = 0L): Long {
        return dao.getValue("s_$key")?.toLongOrNull() ?: default
    }

    suspend fun getFloat(dao: KeyValueDao, key: String, default: Float = 0f): Float {
        return dao.getValue("s_$key")?.toFloatOrNull() ?: default
    }

    // 写入
    suspend fun putString(dao: KeyValueDao, key: String, value: String) {
        dao.putValue(KeyValueEntity("s_$key", value))
    }

    suspend fun putBoolean(dao: KeyValueDao, key: String, value: Boolean) {
        dao.putValue(KeyValueEntity("s_$key", value.toString()))
    }

    suspend fun putInt(dao: KeyValueDao, key: String, value: Int) {
        dao.putValue(KeyValueEntity("s_$key", value.toString()))
    }

    suspend fun putLong(dao: KeyValueDao, key: String, value: Long) {
        dao.putValue(KeyValueEntity("s_$key", value.toString()))
    }

    suspend fun putFloat(dao: KeyValueDao, key: String, value: Float) {
        dao.putValue(KeyValueEntity("s_$key", value.toString()))
    }

    // 偏好设置键名常量（替代原来的 PreferencesKey）
    const val IS_BEST_REGION = "is_best_region"
    const val IS_LOCK_SCREEN_ORIENTATION_PORTRAIT = "is_lock_screen_orientation_portrait"
    const val IS_AUTO_CHECK_VERSION = "is_auto_check_version"
    const val IGNORE_UPDATE_VERSION_CODE = "ignore_update_version_code"

    const val HOME_RECOMMEND_SHOW = "home_recommend_show"
    const val HOME_POPULAR_SHOW = "home_popular_show"
    const val HOME_POPULAR_CARRY_TOKEN = "home_popular_carry_token"
    const val HOME_RECOMMEND_LIST_STYLE = "home_recommend_list_style"
    const val HOME_ENTRY_VIEW = "home_entry_view"

    const val THEME_COLOR = "theme_color"
    const val THEME_TYPE = "theme_type"
    const val THEME_DARK_MODE = "theme_dark_mode"
    const val THEME_APP_BAR_TYPE = "theme_app_bar_type"

    const val PLAYER_DECODER = "player_decoder"
    const val PLAYER_QUALITY = "player_quality"
    const val PLAYER_SPEED = "player_speed"
    const val PLAYER_SCREEN_TYPE = "player_screen_type"
    const val PLAYER_FNVAL = "player_fnval"
    const val PLAYER_BACKGROUND = "player_background"
    const val PLAYER_PROXY = "player_proxy"
    const val PLAYER_OPEN_MODE = "player_open_mode"
    const val PLAYER_ORDER = "player_order"
    const val PLAYER_ORDER_RANDOM = "player_order_random"
    const val PLAYER_NOTIFICATION = "player_notification"
    const val PLAYER_FULL_MODE = "player_full_mode"
    const val PLAYER_BOTTOM_PROGRESS_BAR_SHOW = "player_bottom_progress_bar_show"
    const val PLAYER_SPEED_VALUES = "player_speed_values"
    const val PLAYER_AUDIO_FOCUS = "player_audio_focus"
    const val PLAYER_SUBTITLE_SHOW = "player_subtitle_show"
    const val PLAYER_AI_SUBTITLE_SHOW = "player_ai_subtitle_show"
    const val PLAYER_SMALL_SHOW_AREA = "player_small_show_area"
    const val PLAYER_HOLD_SHOW_AREA = "player_hold_show_area"
    const val PLAYER_SMALL_DRAGGABLE = "player_small_draggable"
    const val PLAYER_AUTO_STOP_DURATION = "player_auto_stop_duration"

    const val DANMAKU_ENABLE = "danmaku_enable"
    const val DANMAKU_SYS_FONT = "danmaku_sys_font"
    const val DANMAKU_TIME_SYNC = "danmaku_time_sync"

    // 弹幕模式配置键生成函数
    fun danmakuEnable(mode: String) = "${mode}_danmaku_enable"
    fun danmakuShow(mode: String) = "${mode}_danmaku_show"
    fun danmakuR2lShow(mode: String) = "${mode}_danmaku_r2l_show"
    fun danmakuFtShow(mode: String) = "${mode}_danmaku_ft_show"
    fun danmakuFbShow(mode: String) = "${mode}_danmaku_fb_show"
    fun danmakuSpecialShow(mode: String) = "${mode}_danmaku_special_show"
    fun danmakuFontSize(mode: String) = "${mode}_danmaku_fontsize"
    fun danmakuOpacity(mode: String) = "${mode}_danmaku_opacity"
    fun danmakuSpeed(mode: String) = "${mode}_danmaku_speed"
    fun danmakuMaxLines(mode: String) = "${mode}_danmaku_max_lines"
    fun danmakuR2lMaxLine(mode: String) = "${mode}_danmaku_r2l_max_line"
    fun danmakuFtMaxLine(mode: String) = "${mode}_danmaku_ft_max_line"
    fun danmakuFbMaxLine(mode: String) = "${mode}_danmaku_fb_max_line"
}
```

### 8.2 迁移 SharedPreferences 数据（buvid、proxy_upos）→ commonMain

**`BilimiaoCommApp.kt` 中的 `getBilibiliBuvid()`**:
```kotlin
// Before
val sp = app.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
var buvid = sp.getString("buvid", "")!!

// After
suspend fun getBilibiliBuvid(): String {
    if (_bilibiliBuvid.isNotBlank()) return _bilibiliBuvid
    var buvid = keyValueDao.getValue("buvid") ?: ""
    if (buvid.isBlank()) {
        buvid = ApiHelper.generateBuvid()
        keyValueDao.putValue(KeyValueEntity("buvid", buvid))
    }
    _bilibiliBuvid = buvid
    return buvid
}
```

**`ProxyHelper.kt`**:
```kotlin
// Before
val sp = context.getSharedPreferences(BilimiaoCommApp.APP_NAME, Context.MODE_PRIVATE)
sp.edit().putString(KEY_UPOS, uposName).apply()

// After
suspend fun saveUposName(uposName: String) {
    keyValueDao.putValue(KeyValueEntity(KEY_UPOS, uposName))
}

suspend fun uposName(): String {
    return keyValueDao.getValue(KEY_UPOS) ?: "none"
}
```

### 8.3 修改文件存储路径（auth_hd、proxy_server_list.json）

```kotlin
// Before
private val authFilePath get() = app.filesDir.path + "/auth_hd"

// After — 通过 appContextProvider 获取路径
private val authFilePath get() = File(appContextProvider.filesDir, "auth_hd").absolutePath
```

### 验证
- `SettingPreferences` 在 commonMain 中，无 `import android.*`
- `./gradlew :bilimiao-comm:desktopMainClasses` 通过

---

## Step 9: 剩余工具类迁移

### 目标
将 utils/ 目录下剩余的工具类移入 commonMain 或保留为平台特定实现。

### 9.1 需修改后移入 commonMain 的文件

**`utils/UrlUtil.kt`** — 移除 `android.net.Uri`：
```kotlin
// Before
import android.net.Uri
fun getQueryParameter(url: String, key: String): String? {
    return Uri.parse(url).getQueryParameter(key)
}

// After — 使用纯 Kotlin 解析
fun getQueryParameter(url: String, key: String): String? {
    val queryStart = url.indexOf('?')
    if (queryStart == -1) return null
    return url.substring(queryStart + 1)
        .split("&")
        .map { it.split("=", limit = 2) }
        .find { it[0] == key }
        ?.getOrNull(1)
        ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
}
```

**`utils/RSAUtil.kt`** — 移除 `android.util.Base64`：
```kotlin
// Before
import android.util.Base64
return Base64.encodeToString(data, Base64.NO_WRAP)

// After
import java.util.Base64
return Base64.getEncoder().encodeToString(data)
```

**`utils/AESUtil.kt`** — 移除 `android.content.Context` 和 `android.util.Base64`：
```kotlin
// Before
import android.content.Context
import android.util.Base64
import com.a10miaomiao.bilimiao.comm.R
fun getKey(key: String, context: Context): SecretKey { ... context.getString(R.string.xxx) ... }

// After — key 改为从外部传入，不依赖 Context 和 R 资源
fun getKey(key: String): SecretKey { ... }
// Base64 改用 java.util.Base64
```

**`utils/MiaoLogger.kt`** — 移除 `android.util.Log`：
```kotlin
// Before
import android.util.Log
Log.d(tag, message)

// After — 使用 expect/actual 或接口
// commonMain
interface Logger {
    fun d(tag: String, message: String)
    fun debug(tag: String, message: String)
}
var logger: Logger = object : Logger {
    override fun d(tag: String, message: String) { println("[$tag] $message") }
    override fun debug(tag: String, message: String) { println("[$tag] $message") }
}

// androidMain 可提供 AndroidLogger 实现使用 Log.d
```

### 9.2 保留在平台源集的文件

**`utils/BiliUrlMatcher.kt`** — 使用 `CustomTabsIntent`（Android）和 `Desktop.browse()`（JVM）：
```
commonMain — 放 URL 匹配逻辑（正则部分）
androidMain — 放浏览器打开逻辑
desktopMain — 放浏览器打开逻辑
```

**`utils/GlideCacheUtil.kt`** — 深度依赖 Glide：
```
androidMain — 保留（后续用 Coil 替代时再迁移）
```

**`utils/ImageSaveUtil.kt`** — 深度依赖 Android 权限、MediaStore、Bitmap：
```
androidMain — 保留
desktopMain — 需要新建 Desktop 实现（保存文件到本地即可）
```

### 验证
- `commonMain/.../utils/` 下包含：`MiaoEncryptDecrypt.kt`、`UrlUtil.kt`、`RSAUtil.kt`、`AESUtil.kt`、`MiaoLogger.kt`、`NumberUtil.kt`
- `./gradlew :bilimiao-comm:desktopMainClasses` 通过

---

## Step 10: Store 层重构

### 目标
将 11 个 Store 类从 Android 特有的 `ViewModel` + `Context` 依赖重构为 KMP ViewModel，保留 `viewModelScope`，移入 commonMain。

### 10.1 BaseStore 接口改造

```kotlin
// Before (androidMain)
import android.content.Context
import androidx.lifecycle.ViewModel

interface BaseStore<T> : DIAware {
    val stateFlow: MutableStateFlow<T>
    fun copyState(): T
    open fun init(context: Context) {}
    fun setState(block: T.() -> Unit) { stateFlow.value = copyState().apply(block) }
}

// After (commonMain)
import androidx.lifecycle.ViewModel

interface BaseStore<T> {
    val stateFlow: MutableStateFlow<T>
    val state: T get() = stateFlow.value
    fun copyState(): T
    fun setState(block: T.() -> Unit) {
        stateFlow.value = copyState().apply(block)
    }
}
```

变化点：
- 移除 `DIAware` 接口
- 移除 `init(context: Context)` 方法
- `ViewModel` 保留在 Store 类的继承中（来自 KMP 版 `androidx.lifecycle.ViewModel`）

### 10.2 重构示例：AppStore

```kotlin
// Before
class AppStore(override val di: DI) : ViewModel(), BaseStore<AppStore.State> {
    private val context: Context by instance()

    override fun init(context: Context) {
        super.init(context)
        SettingPreferences.launch(viewModelScope) {
            context.dataStore.data.collect {
                val themeType = it[ThemeType] ?: SettingConstants.THEME_TYPE_DEFAULT
                setState { theme = ThemeSettingState(...) }
            }
        }
    }

    fun setDarkMode(mode: Int) {
        viewModelScope.launch {
            SettingPreferences.edit(context) { it[ThemeDarkMode] = mode }
        }
    }
}

// After (commonMain)
class AppStore(
    private val keyValueDao: KeyValueDao
) : ViewModel(), BaseStore<AppStore.State> {

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    init {
        // viewModelScope 来自 KMP 版 ViewModel，无需更改
        viewModelScope.launch {
            val themeType = SettingPreferences.getInt(keyValueDao, SettingPreferences.THEME_TYPE)
            val themeColor = SettingPreferences.getLong(keyValueDao, SettingPreferences.THEME_COLOR, 0xFFFB7299)
            setState {
                theme = ThemeSettingState(
                    color = themeColor.toInt(),
                    type = themeType,
                    darkMode = SettingPreferences.getInt(keyValueDao, SettingPreferences.THEME_DARK_MODE),
                    appBarType = SettingPreferences.getInt(keyValueDao, SettingPreferences.THEME_APP_BAR_TYPE),
                )
                home = HomeSettingState(
                    showPopular = SettingPreferences.getBoolean(keyValueDao, SettingPreferences.HOME_POPULAR_SHOW, true),
                    showRecommend = SettingPreferences.getBoolean(keyValueDao, SettingPreferences.HOME_RECOMMEND_SHOW, true),
                    entryView = SettingPreferences.getInt(keyValueDao, SettingPreferences.HOME_ENTRY_VIEW),
                )
            }
        }
    }

    fun setDarkMode(mode: Int) {
        viewModelScope.launch {
            SettingPreferences.putInt(keyValueDao, SettingPreferences.THEME_DARK_MODE, mode)
        }
    }

    fun setThemeColor(color: Long, type: Int) {
        viewModelScope.launch {
            SettingPreferences.putLong(keyValueDao, SettingPreferences.THEME_COLOR, color)
            SettingPreferences.putInt(keyValueDao, SettingPreferences.THEME_TYPE, type)
        }
    }
}
```

关键变化：
- `override val di: DI` → `private val keyValueDao: KeyValueDao`（构造函数注入 DAO）
- `by instance()` 注入 Context → 完全移除，DAO 已包含所有数据访问
- `viewModelScope` → **保留不变**（KMP 版 ViewModel 自带）
- `context.dataStore.data.collect` → `SettingPreferences.getXxx(keyValueDao, ...)`
- `SettingPreferences.edit(context) { ... }` → `SettingPreferences.putXxx(keyValueDao, ...)`
- `AppCompatDelegate.setDefaultNightMode()` / `ContextCompat.getColor()` → 移到 androidMain 的扩展函数

### 10.3 重构示例：FilterStore

```kotlin
// Before
class FilterStore(override val di: DI) : ViewModel(), BaseStore<FilterStore.State> {
    private val activity: AppCompatActivity by instance()
    private val filterWordDB = FilterWordDB(activity)

    fun addWord(keyword: String) {
        filterWordDB.insert(keyword)  // 同步
        queryFilterWord()
        PopTip.show("添加成功")
    }
}

// After (commonMain)
class FilterStore(
    private val db: BilimiaoDatabase,
    private val onMessage: (String) -> Unit = {}  // 替代 PopTip.show，由平台注入
) : ViewModel(), BaseStore<FilterStore.State> {

    private val filterWordDao = db.filterWordDao()
    private val filterUpperDao = db.filterUpperDao()
    private val filterTagDao = db.filterTagDao()

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    init {
        // 使用 viewModelScope + Flow 自动观察数据变化
        viewModelScope.launch {
            filterWordDao.queryAllKeywords().collect { list ->
                setState { filterWordList = list.toMutableList() }
            }
        }
        viewModelScope.launch {
            filterUpperDao.queryAll().collect { list ->
                setState { filterUpperList = list.toMutableList() }
            }
        }
        viewModelScope.launch {
            filterTagDao.queryAllNames().collect { list ->
                setState { filterTagList = list.toMutableList() }
            }
        }
    }

    fun addWord(keyword: String) {
        viewModelScope.launch {
            filterWordDao.insert(FilterWordEntity(keyword = keyword))
            // Flow 会自动触发 UI 更新，无需手动 query
            onMessage("添加成功")
        }
    }

    fun filterWord(text: String): Boolean {
        state.filterWordList.forEach {
            if (it.length > 2 && it.startsWith('/') && it.endsWith('/')) {
                if (it.substring(1, it.length - 1).toRegex().containsMatchIn(text)) return false
            } else {
                if (it in text) return false
            }
        }
        return true
    }
}
```

关键变化：
- `ViewModel()` 继承 **保留不变**
- `AppCompatActivity by instance()` → `BilimiaoDatabase` 构造函数注入
- 同步 DB 操作 → `viewModelScope.launch` + Room DAO（suspend 函数）
- `PopTip.show(...)` → `onMessage(...)` 回调（由上层平台代码注入 Compose Snackbar 或 DialogX）

### 10.4 所有 Store 类的通用改动模式

| 改动点 | Before | After | 变化程度 |
|---|---|---|---|
| **ViewModel 继承** | `: ViewModel()` | `: ViewModel()` | **不变** |
| **viewModelScope** | `viewModelScope` | `viewModelScope` | **不变** |
| **DI 方式** | `override val di: DI` + `by instance()` | 构造函数参数注入 | 改 |
| **Context 依赖** | `private val context: Context by instance()` | 移除（DAO 已注入） | 移除 |
| **Activity 依赖** | `private val activity: AppCompatActivity by instance()` | 移除 | 移除 |
| **DIAware 接口** | `BaseStore<T> : DIAware` | `BaseStore<T>`（无 DIAware） | 移除 |
| **数据库** | `FilterWordDB(activity)` 同步调用 | `db.filterWordDao()` suspend/Flow | 改 |
| **DataStore** | `context.dataStore.data.collect` | `SettingPreferences.getXxx(dao, key)` | 改 |
| **通知 UI** | `PopTip.show("...")` | `onMessage("...")` 回调 | 改 |
| **init 方法** | `override fun init(context: Context)` | `init {}`（构造函数 init 块） | 简化 |

### 10.5 Store 实例化方式变化

```kotlin
// Before — 通过 Kodein DI 自动创建
class FilterStore(override val di: DI) : ViewModel(), BaseStore<FilterStore.State> {
    private val activity: AppCompatActivity by instance()
    // Kodein 自动解析 DI 依赖并注入
}

// After — 通过工厂函数或 DI 容器创建
// 方案 A：简单工厂函数（推荐初期使用）
fun createFilterStore(db: BilimiaoDatabase, onMessage: (String) -> Unit = {}): FilterStore {
    return FilterStore(db, onMessage)
}

// 方案 B：Kodein DI（纯 JVM 版，无需 Android 模块）
val diModule = DI {
    bindSingleton<BilimiaoDatabase> { buildDatabase() }
    bindSingleton<FilterStore> { FilterStore(instance(), instance()) }
    bindSingleton<AppStore> { AppStore(instance()) }
    // ...
}
```

### 10.6 平台特定逻辑分离

部分 Store 中有少量平台特定逻辑，需要提取到扩展函数：

```kotlin
// androidMain/.../store/AppStoreExt.kt
fun AppStore.applyDarkMode(mode: Int) {
    when (mode) {
        0 -> {
            DialogX.globalTheme = DialogX.THEME.AUTO
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        1 -> {
            DialogX.globalTheme = DialogX.THEME.LIGHT
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        2 -> {
            DialogX.globalTheme = DialogX.THEME.DARK
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}

// desktopMain/.../store/AppStoreExt.kt
fun AppStore.applyDarkMode(mode: Int) {
    // Desktop 端通过 Compose 主题切换，无需 AppCompatDelegate
}
```

```kotlin
// commonMain — AppStore 中只保留数据持久化
fun setDarkMode(mode: Int) {
    viewModelScope.launch {
        SettingPreferences.putInt(keyValueDao, SettingPreferences.THEME_DARK_MODE, mode)
    }
}

// androidMain — 调用端同时调用平台扩展
appStore.setDarkMode(mode)
appStore.applyDarkMode(mode)  // 平台特定
```

### 验证
- `commonMain/.../store/` 下包含所有 Store 类
- Store 类仍继承 `ViewModel()`，仍使用 `viewModelScope`
- 无 `import android.*`、无 `import androidx.appcompat.*`、无 `import com.kongzue.dialogx.*`
- `./gradlew :bilimiao-comm:desktopMainClasses` 通过

---

## Step 11: BilimiaoCommApp 重构

### 目标
将 `BilimiaoCommApp` 从依赖 `Application` 重构为依赖平台抽象接口。

### 操作

```kotlin
// Before (androidMain)
import android.app.Application
import android.content.Context
import android.webkit.CookieManager
import com.kongzue.dialogx.DialogX

class BilimiaoCommApp(val app: Application) {
    companion object { lateinit var commApp: BilimiaoCommApp }

    fun onCreate() {
        commApp = this
        readAuthInfo()
        DialogX.init(app)
    }

    fun getBilibiliBuvid(): String {
        val sp = app.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
        // ...
    }
}

// After (commonMain)
import com.a10miaomiao.bilimiao.comm.platform.AppContextProvider
import com.a10miaomiao.bilimiao.comm.platform.CookieProvider
import java.io.File

class BilimiaoCommApp(
    val appContext: AppContextProvider,
    val cookieProvider: CookieProvider,
    val database: BilimiaoDatabase,
) {
    var loginInfo: LoginInfo? = null
        private set

    private val keyValueDao get() = database.keyValueDao()
    private val authFilePath get() = File(appContext.filesDir, "auth_hd").absolutePath
    private val key = "Message Word"
    private var _bilibiliBuvid = ""

    companion object {
        lateinit var commApp: BilimiaoCommApp
        const val APP_NAME = "bilimiao"
    }

    // 平台特定初始化由 androidMain/desktopMain 调用
    fun onInit() {
        commApp = this
        readAuthInfo()
    }

    suspend fun getBilibiliBuvid(): String {
        if (_bilibiliBuvid.isNotBlank()) return _bilibiliBuvid
        var buvid = keyValueDao.getValue("buvid") ?: ""
        if (buvid.isBlank()) {
            buvid = ApiHelper.generateBuvid()
            keyValueDao.putValue(KeyValueEntity("buvid", buvid))
        }
        _bilibiliBuvid = buvid
        return buvid
    }

    fun setCookie(cookieInfo: LoginInfo.CookieInfo) {
        cookieInfo.domains.forEach { domain ->
            cookieInfo.cookies.forEach { cookie ->
                cookieProvider.setCookie(domain, cookie.getValue(domain))
            }
        }
    }

    fun saveAuthInfo(loginInfo: LoginInfo) { /* 使用文件 I/O，JVM 通用 */ }
    private fun readAuthInfo() { /* 使用文件 I/O，JVM 通用 */ }
    fun deleteAuth() { /* 使用文件 I/O + cookieProvider.removeAll() */ }
}
```

**Android 端初始化**（androidMain）:
```kotlin
// androidMain/.../BilimiaoCommAppExt.kt
fun BilimiaoCommApp.androidInit(application: Application) {
    DialogX.init(application)
    DialogX.globalStyle = MaterialYouStyle.style()
}
```

**Desktop 端初始化**（desktopMain）:
```kotlin
// desktopMain/.../BilimiaoCommAppExt.kt
fun BilimiaoCommApp.desktopInit() {
    // Desktop 无需 DialogX 初始化
}
```

### 验证
- `BilimiaoCommApp` 在 commonMain 中，无 `import android.*`
- `./gradlew :bilimiao-comm:desktopMainClasses` 通过

---

## Step 12: commonMain 零 Android 依赖验证

### 目标
确保 `bilimiao-comm` 的 `commonMain` 源集中不包含任何 `android.*` 引用。

### 验证清单

```bash
# 1. 检查 commonMain 中无 android import
grep -r "import android\." bilimiao-comm/src/commonMain/  | wc -l
# 期望结果: 0

# 2. 检查 commonMain 中无 androidx import（Room、Lifecycle ViewModel 除外）
grep -r "import androidx\." bilimiao-comm/src/commonMain/ \
  | grep -v "room" | grep -v "sqlite" | grep -v "lifecycle" | wc -l
# 期望结果: 0
# 说明：lifecycle-viewmodel 是 KMP 库，允许在 commonMain 中使用

# 3. Desktop 编译通过
./gradlew :bilimiao-comm:desktopMainClasses

# 4. Android 编译通过（回归）
./gradlew :bilimiao-comm:assembleDebug
```

### 文件分布总结

| 源集 | 内容 | 文件数（约） |
|---|---|---|
| **commonMain** | Entity(111), API(15), Network(6), DB(Room), DataStore, Utils, Store(11) + ViewModel, BilimiaoCommApp, Platform 接口 | ~160 |
| **androidMain** | Platform 实现（Cookie, DeviceInfo, Base64, AppContext），Glide 工具，DialogX 扩展，Store 平台扩展（暗色模式等） | ~15 |
| **desktopMain** | Platform 实现（Cookie, DeviceInfo, Base64, AppContext），Store 平台扩展 | ~8 |
| **src/main/proto** | Protobuf 定义文件（66 个 .proto） | 66 |

### commonMain 中允许的 KMP 依赖

| 依赖 | 用途 | 备注 |
|---|---|---|
| `androidx.lifecycle:lifecycle-viewmodel` | Store 基类，viewModelScope | KMP 版，支持 Android/Desktop/iOS |
| `androidx.room:room-runtime` | 数据库 Entity/DAO/Database | KMP 版 |
| `androidx.sqlite:sqlite-bundled` | SQLite 引擎 | KMP 版，Desktop 用 bundled |
| `org.jetbrains.kotlinx:kotlinx-coroutines-*` | 协程 | KMP |
| `org.jetbrains.kotlinx:kotlinx-serialization-*` | JSON 序列化 | KMP |
| `com.squareup.okhttp3:okhttp` | HTTP 客户端 | JVM 通用 |
| `pro.streem.pbandk:pbandk-runtime` | Protobuf | KMP |

---

# Phase 2~5: 后续规划（概要）

## Phase 2: bilimiao-compose 转为 KMP

与 Phase 1 相同的策略，将 `bilimiao-compose` 改造为 KMP 模块：
- Composable 函数本身是跨平台的（Compose Multiplatform）
- 需要替换：Glide → Coil、DialogX → Compose Dialog、navigation-compose → Voyager
- `LocalContext.current` 调用改为使用 `BilimiaoCommApp.commApp` 的平台抽象

## Phase 3: 创建 Desktop 入口 app-desktop

- 新建 `app-desktop` 模块
- Compose Desktop `application { Window { ... } }` 入口
- DI 容器配置（绑定 JVM 平台实现）
- 应用窗口布局（适配桌面宽屏）

## Phase 4: 播放器 / 弹幕 / WebView 替换

| 组件 | Android | Desktop 替代 |
|---|---|---|
| 视频播放器 | ExoPlayer/GSYVideoPlayer | vlcj 或 javafx-media |
| 弹幕引擎 | DanmakuFlameMaster | Compose Canvas 自绘 |
| WebView | android.webkit | JCEF 或 Desktop.browse() |
| 下载服务 | Android Service | 协程 + 系统通知 |

## Phase 5: 清理与打包

- 移除 `bilimiao-appwidget`（Desktop 无桌面小组件概念）
- 移除 `benchmark` 模块
- 配置 Desktop 打包（.msi / .dmg / .deb）
- 端到端测试
