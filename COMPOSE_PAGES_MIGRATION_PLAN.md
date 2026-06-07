# bilimiao-compose Pages KMP 迁移计划

> 目标：将 `bilimiao-compose/src/androidMain` 中的 pages 迁移到 `commonMain`，实现跨平台共享
> 创建日期：2026-06-06
> 更新日期：2026-06-07

---

## 迁移进度总览

```
Phase 1: ✅ 完成 — 纯 Compose 页面直接迁移（61 个文件）
Phase 2: ✅ 完成 — 轻量修复后迁移（4 个文件）
Phase 3: ✅ 完成 — 平台抽象 + 深度重构迁移（2 个文件）
Phase 4: ✅ 完成 — 循环引用修复迁移（3 个文件）
Phase 5: ✅ 完成 — ProxyHelper 抽象 + proxy 页面迁移（3 个文件）
Phase 6: ✅ 完成 — AppInfo 抽象 + 设置页面迁移（3 个文件）
Phase 7: ✅ 完成 — Android View Menu → Compose Menu（2 个文件）
Phase 8: ✅ 完成 — Clipboard/Share → PlatformContext（1 个文件 + 2 个部分重构）
Phase 9: ✅ 完成 — BiliJsBridge/PlatformInfo 抽象 + 页面迁移（3 个文件 + 部分重构）
Phase 10: ✅ 完成 — DownloadManager 抽象 + 下载页面重构 + H5LoginPage 迁移
Phase 11: ✅ 完成 — LocalPlayerSource 移至 commonMain + 3 页面迁移（DownloadListPage/DownloadDetailPage/TestPage）
Phase 12: ✅ 完成 — VideoPlayerSource 移至 commonMain + 2 页面迁移（DownloadBangumiCreatePage/VideoDetailViewModel）
```

| 指标 | 迁移前 | 当前 | 变化 |
|------|--------|------|------|
| androidMain pages | 86 | 0 | **-86** |
| commonMain pages | 63 | 149 | **+86** |

---

## 迁移完成

所有 bilimiao-compose pages 已从 androidMain 迁移至 commonMain。

---

## 已完成的迁移记录

### Phase 1: 直接迁移（61 个文件）
dynamic/ (9), video/ (12), user/ (16), community/ (5), mine/ (5), rank/ (2), setting/ (4), home/ (1), auth/ (1), lyric/ (2), bangumi/ (1), download/ (2), search/ (1)

### Phase 2: 修复后迁移（4 个文件）
- `HistoryPage.kt` — 删除未使用的 `import android.content.Context`
- `HomePopularContent.kt` — `SettingPreferences.mapData(context)` → `mapPreferences`
- `PlayListPage.kt` — 创建 `HapticFeedback` expect/actual 抽象
- `ReplyEditDialog.kt` — 误判为 RED，实际无 Android 依赖

### Phase 3: 平台抽象迁移（2 个文件）
- `SMSLoginPage.kt` — BiliGeetestUtil → GeetestVerifier 抽象
- `LoginPage.kt` — 同上 + H5LoginPage 导航改为 URI 方式

### Phase 4: 循环引用修复（1 个文件）
- `TelVerifyPage.kt` — 删除未使用的 HomePage import

### Phase 5: ProxyHelper 抽象迁移（3 个文件）
- `AddProxyServerPage.kt` — ProxyHelper → ProxyRepository 抽象
- `EditProxyServerPage.kt` — 同上
- `SelectProxyServerPage.kt` — 同上 + ProxyHelper.version → StateFlow
- `ProxySettingPage.kt` — 修复已有 commonMain 文件中的 Android 依赖

### Phase 6: AppInfo 抽象迁移（3 个文件）
- `AboutPage.kt` — Activity/PackageManager → AppInfo, Intent → PlatformContext.openUrl
- `SettingPage.kt` — LocalContext → AppInfo + FileStorage + PlatformContext
- `HomePage.kt` — MaterialAlertDialogBuilder → Compose AlertDialog, SharedPreferences → FileStorage

### Phase 7: Android View Menu 迁移（2 个文件）
- `DanmakuSettingPage.kt` — 无 Android 依赖，直接迁移
- `DanmakuDisplaySettingPage.kt` — Build.VERSION.SDK_INT 移除，mapData → mapPreferences

### Phase 8: Clipboard/Share 迁移（1 个文件 + 2 个部分重构）
- `BangumiDetailPage.kt` — ClipboardManager/Intent/BiliUrlMatcher → PlatformContext lambda
- `UserSpaceViewModel.kt` — 同上（部分重构，仍在 androidMain）
- `VideoDetailViewModel.kt` — 同上 + mapData → mapPreferences（部分重构，仍在 androidMain）

### Phase 9: BiliJsBridge/PlatformInfo 抽象迁移（3 个文件 + 2 个部分重构）
- `UserSpaceViewModel.kt` — 删除 WebPage 依赖，改用 navigateByUri → 移入 commonMain
- `WebPage.kt` — BiliJsBridge 重构为 lambda 化 + PlatformInfo 替代 Build.MODEL → 移入 commonMain
- `LyricPage.kt` — Base64 → kotlin.io.encoding，PopupMenu → Compose DropdownMenu → 移入 commonMain
- `VideoDetailViewModel.kt` — Activity/CoverActivity → lambda 抽象（部分重构，仍在 androidMain）
- `BiliJsBridge.kt` — ComposeHostBridge → lambda 化，移入 commonMain
- `H5LoginPage.kt` — 更新 BiliJsBridge 构造调用

### 新建的平台抽象（22 个文件）

| 抽象 | commonMain | androidMain | desktopMain |
|------|-----------|-------------|-------------|
| **HapticFeedback** | `HapticFeedback.kt` | `HapticFeedbackAndroid.kt` | `HapticFeedbackDesktop.kt` |
| **WebViewContainer** | `WebViewContainer.kt` + `WebViewHandle.kt` | `WebViewContainerAndroid.kt` | `WebViewContainerDesktop.kt` |
| **GeetestVerifier** | `GeetestVerifier.kt` | `GeetestVerifierAndroid.kt` | `GeetestVerifierDesktop.kt` |
| **DownloadManager** | `DownloadManager.kt` | `DownloadManagerAndroid.kt` | `DownloadManagerDesktop.kt` |
| **LocalPlayerSource** | `LocalPlayerSource.kt` + `LocalPlayerSourceFactory.kt` | `LocalPlayerSourceFactoryAndroid.kt` | `LocalPlayerSourceFactoryDesktop.kt` |
| **BrowserCookie** | `BrowserCookie.kt` | `BrowserCookieAndroid.kt` | `BrowserCookieDesktop.kt` |
| **ProxyRepository** | `ProxyRepository.kt` | `ProxyRepositoryAndroid.kt` | `ProxyRepositoryDesktop.kt` |
| **AppInfo** | `AppInfo.kt` | `AppInfoAndroid.kt` | `AppInfoDesktop.kt` |
| **FileStorage** | `FileStorage.kt` | `FileStorageAndroid.kt` | `FileStorageDesktop.kt` |
| **PlatformInfo** | `PlatformInfo.kt` | `PlatformInfoAndroid.kt` | `PlatformInfoDesktop.kt` |
| **BiliJsBridge** | `BiliJsBridge.kt`（lambda 化） | — | — |
| **VideoPlayerSource** | `VideoPlayerSource.kt` | `VideoPlayerSourceFactoryAndroid.kt` | `VideoPlayerSourceFactoryDesktop.kt` |
| **PlatformContext** | `PlatformContext.kt` | `AndroidPlatformContext.kt` | `DesktopPlatformContext.kt` |

### DI 注册
- `MainActivity.kt` — 添加 `GeetestVerifier`、`ProxyRepository`、`AppInfo`、`FileStorage`、`DownloadManager` 绑定

### 路由修改
- `BilimiaoPageRoute.kt` — 为 `H5LoginPage` 和 `TelVerifyPage` 注册 deep link

### Phase 10: DownloadManager 抽象 + 下载页面重构 + H5LoginPage 迁移
**目标**：将 `DownloadService`（bilimiao-download 模块）依赖抽象为 `DownloadManager` 接口，消除 commonMain 对 Android 模块的依赖；将 H5LoginPage 迁移至 commonMain（Desktop 端 WebView 空白占位）

**新建文件**：
- `commonMain/.../download/entry/BiliDownloadEntryInfo.kt` — 下载条目数据类
- `commonMain/.../download/entry/BiliDownloadEntryAndPathInfo.kt` — 下载条目+路径
- `commonMain/.../download/entry/CurrentDownloadInfo.kt` — 当前下载信息（自定义 `Double.format` 替代 `DecimalFormat`）
- `commonMain/.../download/entry/BiliDownloadMediaFileInfo.kt` — 媒体文件信息密封类
- `commonMain/.../download/DownloadManager.kt` — 下载管理器接口
- `androidMain/.../download/DownloadManagerAndroid.kt` — Android 实现（桥接 DownloadService + 双向类型映射）
- `desktopMain/.../download/DownloadManagerDesktop.kt` — Desktop 空实现

**重构文件**：
- `DownloadListItem.kt` — `CurrentDownloadInfo` import 改为 commonMain 版本
- `DownloadDetailItem.kt` — 同上
- `VideoDownloadDialog.kt` — `DownloadService` → `DownloadManager`，`BiliDownloadEntryInfo` → common 版本
- `DownloadListPage.kt` — `DownloadService.getService()` → `DownloadManager` DI 注入
- `DownloadDetailPage.kt` — 同上 + 移除 `DownloadService` 直接调用
- `DownloadBangumiCreatePage.kt` — 同上
- `VideoDetailViewModel.kt` — `getDownloadService` lambda → `DownloadManager` DI 注入

**删除文件**：
- `DownloadServiceProxy.kt` — 被 `DownloadManager` 接口取代

**DI 注册**：
- `MainActivity.kt` — 添加 `bindSingleton<DownloadManager> { DownloadManagerAndroid(this@MainActivity) }`

**H5LoginPage 迁移**：
- 从 androidMain 移至 commonMain
- `Build.MODEL/VERSION` → `platformInfo`
- `AndroidView` + `WebView` → `WebViewContainer`（Desktop 端显示"WebView is not supported"占位）
- `ComposeHostBridge` → lambda 模式（`runOnUiThread`、`shareText`）
- `BilimiaoCommApp.commApp.saveAuthInfo` → `BilimiaoCommCore.instance.saveAuthInfo`

### Phase 11: LocalPlayerSource + 3 页面迁移
**目标**：将 LocalPlayerSource 移至 commonMain，迁移 DownloadListPage、DownloadDetailPage、TestPage

**新建文件**：
- `commonMain/.../download/LocalPlayerSource.kt` — 通用本地播放源（继承 BasePlayerSource，无弹幕）
- `commonMain/.../download/LocalPlayerSourceFactory.kt` — expect fun createLocalPlayerSource
- `androidMain/.../download/LocalPlayerSourceFactoryAndroid.kt` — actual 实现（含 DanmakuProvider）
- `desktopMain/.../download/LocalPlayerSourceFactoryDesktop.kt` — actual 实现（无弹幕）
- `commonMain/.../platform/BrowserCookie.kt` — expect fun getBrowserCookie
- `androidMain/.../platform/BrowserCookieAndroid.kt` — actual（CookieManager）
- `desktopMain/.../platform/BrowserCookieDesktop.kt` — actual（空字符串）

**迁移文件**：
- `DownloadDetailPage.kt` — 移至 commonMain，`LocalPlayerSource` → `createLocalPlayerSource`，移除 ComposeHostBridge
- `DownloadListPage.kt` — 移至 commonMain，ClipboardManager/Intent → PlatformContext lambda
- `TestPage.kt` — 移至 commonMain，CookieManager → `getBrowserCookie`，BilimiaoCommApp → BilimiaoCommCore

### Phase 12: VideoPlayerSource 移至 commonMain + 2 页面迁移
**目标**：将 `VideoPlayerSource` 移至 commonMain（与 LocalPlayerSource 相同模式），迁移最后 2 个 androidMain 页面

**新建文件**：
- `bilimiao-comm/commonMain/.../player/VideoPlayerSource.kt` — 通用视频播放源（无 DanmakuProvider）
- `bilimiao-comm/androidMain/.../player/VideoPlayerSourceFactoryAndroid.kt` — actual 实现（含 DanmakuProvider + 弹幕解析）
- `bilimiao-comm/desktopMain/.../player/VideoPlayerSourceFactoryDesktop.kt` — actual 实现（无弹幕）

**删除文件**：
- `bilimiao-comm/androidMain/.../player/VideoPlayerSource.kt` — 被 commonMain 版本取代

**迁移文件**：
- `DownloadBangumiCreatePage.kt` — 移至 commonMain，`PreferenceManager.getDefaultSharedPreferences()` → `mapPreferences { it[SettingPreferences.PlayerQuality] }`，移除 `Context` 依赖
- `VideoDetailViewModel.kt` — 移至 commonMain，`SettingPreferences.mapData(activity)` → `mapPreferences`
