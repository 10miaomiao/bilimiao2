# AGENTS.md - Agent Guidelines for Bilimiao

本文件为在此代码仓库中工作的 AI 代理（agent）提供指导。

**重要：请全程使用中文回答用户问题。**

## 项目概述

bilimiao（哔哩喵~）是哔哩哔哩的第三方安卓客户端，使用 Kotlin 开发。项目正在积极迁移至 Kotlin 多平台（KMP），通过 Compose Multiplatform 支持桌面端（JVM）。技术栈：Jetpack Compose UI、Kodein 依赖注入、OkHttp 网络请求、kotlinx.serialization JSON 序列化。

## 构建命令

```bash
# 调试构建
./gradlew assembleFullDebug          # 完整版（含百度统计、极验验证）
./gradlew assembleFossDebug          # 开源版

# 发布构建
./gradlew assembleFullRelease
./gradlew assembleFossRelease
./gradlew -Pchannel=Github assembleFullRelease  # 指定渠道

# 桌面端
./gradlew :desktop-app:run           # 运行桌面应用

# 测试
./gradlew test
./gradlew testFullDebugUnitTest
./gradlew test --tests "com.a10miaomiao.bilimiao.ExampleUnitTest.addition_isCorrect"

# 代码检查
./gradlew lint
./gradlew app:lintFullDebug

# 清理
./gradlew clean
```

## 模块架构

```
app/                    - 安卓应用入口（MainActivity、Service）
desktop-app/            - 桌面端应用入口（Main.kt）
bilimiao-comm/          - KMP：核心共享库（API、实体、Store、网络、平台抽象）
bilimiao-compose/       - KMP：所有 Compose UI（页面、组件、导航、主题）
bilimiao-download/      - 安卓：下载功能
bilimiao-cover/         - 安卓：封面图片处理
bilimiao-appwidget/     - 安卓：桌面小组件
DanmakuFlameMaster/     - 安卓：弹幕引擎
danmaku-engine/         - KMP：跨平台弹幕引擎
benchmark/              - 安卓：性能基准测试
grpc-generator/         - Protobuf/gRPC 代码生成
```

### KMP 源集结构

`bilimiao-comm` 和 `bilimiao-compose` 均使用 KMP，包含三个源集：
- **commonMain** - 平台无关代码（无 Android 依赖）
- **androidMain** - Android 平台实现
- **desktopMain** - JVM 桌面端实现

**迁移状态**：全部约 150 个 Compose 页面已迁移至 commonMain。

## 核心架构模式

### 依赖注入（Kodein）

DI 容器在入口点创建（`app/MainActivity`、`desktop-app/Main.kt`），Store 注册为单例：

```kotlin
val di = DI.lazy {
    bindSingleton { AppStore(di) }
    bindSingleton { PlayerStore(di) }
    bindSingleton { UserStore(di) }
}
```

在 Composable 中使用 `localDI()` 和 `rememberInstance()`：
```kotlin
val store: AppStore by rememberInstance()
```

ViewModel 使用 `diViewModel` 辅助函数（`bilimiao-compose/common/DiViewModel.kt`）：
```kotlin
val viewModel = diViewModel { di -> MyViewModel(di) }
```

### 平台抽象

通过两种模式抽象平台特定功能：

1. **expect/actual**（用于简单函数、Composable、属性）：
   - `BackHandler`、`WebViewContainer`、`HapticFeedback`、`ImageCacheHelper`、`ImageSaveHelper`、`navigateDeepLink`、`appDataStore`、`MiaoLogger` 等
   - commonMain 中声明 expect，androidMain/desktopMain 中提供 actual 实现

2. **接口 + DI 绑定**（用于复杂服务）：
   - `PlatformContext`、`PlatformProviders`（bilimiao-comm 层）
   - `GeetestVerifier`、`ProxyRepository`、`AppInfo`、`FileStorage`、`DownloadManager`、`VideoPlayerSource` 工厂（bilimiao-compose 层）
   - commonMain 定义接口，androidMain/desktopMain 提供实现，在入口点通过 Kodein 绑定

### 导航系统

使用 Jetpack Compose Navigation 类型化路由。每个页面是 `ComposePage` 的子类（data class 或 object）：

```kotlin
// 页面定义
@Serializable
data class VideoDetailPage(val id: String) : ComposePage() {
    @Composable
    override fun Content() { /* ... */ }
}

// 在 BilimiaoPageRoute.kt 中注册路由
composable<VideoDetailPage>()

// 导航调用
pageNavigation.navigate(VideoDetailPage(id = "BV1xx411c7mu"))
pageNavigation.navigateByUri("bilimiao://video/BV1xx411c7mu")
```

关键文件：
- `BilimiaoPageRoute.kt` - 所有页面的路由注册
- `PageNavigation.kt` - 导航控制器封装
- `PageNavigator.kt` - 导航接口定义
- `BilibiliNavigation.kt` - URL/深链接路由解析

### 状态管理（Store 模式）

Store 继承 `ViewModel` 并实现 `BaseStore<T>`，状态通过 `MutableStateFlow<T>` 持有：

```kotlin
class AppStore(override val di: DI) : ViewModel(), BaseStore<AppStore.State> {
    data class State(var theme: ThemeSettingState? = null, ...)
    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()
    fun updateSomething() = setState { /* 修改副本 */ }
}
```

主要 Store：`AppStore`、`PlayerStore`、`UserStore`、`FilterStore`、`MessageStore`、`PlayListStore`、`TimeSettingStore`、`UserLibraryStore`

### 网络层

`MiaoHttp` 封装 OkHttp 进行 HTTP 请求，`BiliApiService` 提供 B站 API URL 构建：

```kotlin
class ArchiveApi {
    fun relation(aid: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/web-interface/archive/relation", "aid" to aid)
    }
}
```

- `MiaoHttp`（commonMain）- HTTP 客户端，自动携带 Cookie、buvid 请求头
- `BiliGRPCHttp`（commonMain）- 基于 HTTP 的 gRPC protobuf 调用
- `BiliApiService`（commonMain）- API URL 构建器，聚合所有 API 类实例

### 数据序列化

- 使用 `kotlinx.serialization` 的 `@Serializable` 注解
- `MiaoJson` 提供配置好的 `Json` 实例（ignoreUnknownKeys、isLenient）
- 实体类位于 `bilimiao-comm/src/commonMain/kotlin/com/a10miaomiao/bilimiao/comm/entity/`

### DataStore 偏好设置

`SettingPreferences`（commonMain）定义所有偏好设置键。平台特定的 `appDataStore` 通过 expect/actual 提供。

## 代码风格指南

### Kotlin 约定

1. **命名**
   - 类/对象/接口：`PascalCase`（如 `ArchiveApi`、`ResultInfo`）
   - 函数/属性：`camelCase`（如 `getCookie`、`isSuccess`）
   - 常量：`UPPER_SNAKE_CASE`（如 `GET`、`POST`）
   - 包名：全小写（如 `com.a10miaomiao.bilimiao.comm.apis`）

2. **导入**
   - 使用显式导入（同包除外，不使用通配符导入）
   - 分组：标准库、Android、第三方、项目内，组内按字母序

3. **格式化**
   - 4 空格缩进（Kotlin 默认）
   - 行宽上限 120 字符（Android Studio 默认）
   - 类/函数左花括号不换行
   - 简单函数酌情使用表达式体

4. **类型**
   - 使用 Kotlin 类型系统（无需原生包装类型）
   - 合理使用可空类型（`?`）
   - 优先 `val`，确需可变才用 `var`
   - 数据载体用 `data class`（自带 `equals`/`hashCode`/`toString`）
   - JSON 可序列化类使用 kotlinx.serialization 的 `@Serializable`

5. **空安全**
   - 使用安全调用（`?.`）和 elvis（`?:`）
   - 初始化有保证时优先 `lateinit var` 而非可空
   - 空检查用 `?.let`

6. **协程**
   - 异步操作使用 `suspend` 函数
   - 使用结构化并发（viewModelScope、lifecycleScope）
   - 用 `try-catch` 或 `CoroutineExceptionHandler` 处理异常

### Android/KMP 特定指南

1. **入口点**
   - 遵循 Android 生命周期
   - 使用 Compose，不使用 findViewById
   - 正确处理运行时权限

2. **ViewModel**
   - 使用 AndroidX ViewModel + Kodein 注入
   - 通过 `StateFlow` 暴露状态
   - 自动处理配置变更

3. **网络**
   - 使用 OkHttp 进行 HTTP 请求
   - 用 kotlinx.serialization 解析响应
   - 用自定义异常优雅处理错误
   - 使用 `MiaoLogger` 记录日志

4. **依赖注入**
   - 用 Kodein 模块组织依赖
   - 尽量使用构造函数注入
   - 昂贵依赖使用 lazy delegate

5. **错误处理**
   - 为领域错误创建自定义异常
   - 预期错误用 Result 类型或 try-catch
   - 适当记录日志

### 数据类

```kotlin
@Serializable
data class ResultInfo<T>(
    val code: Int,
    val `data`: T,
    val message: String,
    val ttl: Int,
) {
    val isSuccess get() = code == 0
}
```

### API 类

```kotlin
class ArchiveApi {
    fun relation(aid: String) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/web-interface/archive/relation",
            "aid" to aid
        )
    }
}
```

## 开发约定

- **语言**：全程 Kotlin，代码注释和文档使用中文
- **Java 目标**：JVM 1.8 字节码，JDK 17 编译
- **SDK 版本**：Min SDK 24（app）/ 21（库），Compile SDK 36，Target SDK 35
- **包名**：`com.a10miaomiao.bilimiao.*`（bilimiao-comm），`cn.a10miaomiao.bilimiao.compose.*`（bilimiao-compose）
- **构建变体**：`full`（百度统计、极验验证、AV1 解码器）和 `foss`（开源版）
- **构建类型**：`debug`、`release`、`benchmark`
- **无 ktlint/detekt**，使用 Android Studio 格式化工具
- **Kotlin 代码风格**：gradle.properties 中设为 "official"
- **版本目录**：`gradle/libs.versions.toml` 管理所有依赖版本
- **签名配置**：`app/signing.properties`（已 gitignore）存放发布签名信息

## 目录结构参考

```
bilimiao-comm/src/commonMain/kotlin/com/a10miaomiao/bilimiao/comm/
├── apis/            # B站 API 定义
├── datastore/       # DataStore 偏好设置键
├── db/              # Room 数据库（FilterDatabase、SearchHistoryDatabase）
├── delegate/player/ # 播放器源抽象（BasePlayerSource、VideoPlayerSource、BangumiPlayerSource）
├── entity/          # API 响应数据类
├── miao/            # MiaoJson（序列化）、MiaoLogger
├── network/         # MiaoHttp、BiliApiService、BiliGRPCHttp
├── platform/        # 平台抽象（PlatformContext、CookieProvider 等）
├── store/           # 状态 Store（AppStore、PlayerStore、UserStore 等）
├── toast/           # GlobalToaster（基于 sonner）
└── utils/           # AES/RSA 加密、数字工具

bilimiao-compose/src/commonMain/kotlin/cn/a10miaomiao/bilimiao/compose/
├── base/            # ComposePage、BottomSheetState、PageSearchMethod
├── common/          # 共享工具、平台抽象、DI 辅助、导航
│   ├── auth/        # GeetestVerifier
│   ├── download/    # DownloadManager、LocalPlayerSource
│   ├── emitter/     # SharedFlowEmitter（事件总线）
│   ├── navigation/  # PageNavigation、BilibiliNavigation
│   ├── platform/    # AppInfo、FileStorage、BrowserCookie、PlatformInfo
│   ├── preference/  # DataStore Compose 集成
│   ├── proxy/       # ProxyRepository
│   └── webview/     # WebViewContainer（expect/actual）
├── components/      # 可复用 UI 组件
│   ├── appbar/      # 顶部应用栏
│   ├── dialogs/     # AnyPopDialog、AutoSheetDialog、MessageDialog
│   ├── image/       # ImagePreviewer、ImagePager
│   ├── layout/      # ComposeScaffold、AutoTwoPaneLayout
│   ├── list/        # 列表状态管理
│   ├── start/       # 首页卡片组件
│   └── zoomable/    # 可缩放图片查看器
├── pages/           # 所有应用页面（commonMain 中约 150 个文件）
│   ├── auth/        # 登录页面（二维码、短信、H5、手机验证）
│   ├── bangumi/     # 番剧详情/剧集
│   ├── community/   # 评论/回复页面
│   ├── download/    # 下载管理
│   ├── dynamic/     # 动态流
│   ├── filter/      # 内容过滤设置
│   ├── home/        # 首页
│   ├── lyric/       # 歌词显示
│   ├── message/     # 消息中心
│   ├── mine/        # 用户库（历史、收藏、关注）
│   ├── player/      # 播放器控制（发送弹幕）
│   ├── playlist/    # 播放列表管理
│   ├── rank/        # 排行榜
│   ├── search/      # 搜索结果
│   ├── setting/     # 设置页面（主题、代理、弹幕等）
│   ├── time/        # 时光机功能
│   ├── user/        # 用户空间、关注、收藏
│   ├── video/       # 视频详情和播放
│   └── web/         # 应用内浏览器
└── platform/        # PlatformContext（Compose 层）
```

### 测试结构

```
app/src/test/java/     # 单元测试（JUnit 4）
app/src/androidTest/   # 插桩测试
```

### 重要配置文件

- `gradle/libs.versions.toml` - 版本目录，管理所有依赖版本
- `app/build.gradle.kts` - app 模块构建配置
- `bilimiao-comm/build.gradle.kts` - KMP 核心库构建配置（含 protobuf/gRPC 生成）
- `bilimiao-compose/build.gradle.kts` - KMP Compose UI 构建配置（含 SVG→Compose 图标生成）
- `build.gradle.kts` - 根构建配置
- `settings.gradle.kts` - 模块注册与仓库配置
