# Player移植到Compose - 修改文档

## 目标
将Player从原生ScaffoldView移植到Compose中管理，只移植Player，不移植其他组件。

## 问题背景
原架构：Player挂载在原生ScaffoldView上，通过PlayerBehavior控制位置和动画
目标架构：Player由ComposeScaffold管理，原生ScaffoldView只作为容器

## 核心问题及解决方案

### 问题1：全屏白屏
- **原因**：原生ScaffoldView在全屏时会隐藏content（设置height=0, width=0），而ComposeView作为content被隐藏，导致Player也看不到
- **解决方案**：添加`hideContentOnFullScreen`属性，让原生ScaffoldView在全屏时不隐藏content

### 问题2：overlay视图（completionView等）显示时机
- **原因**：初始时错误地让overlay根据showPlayer显示
- **解决方案**：overlay视图默认隐藏，由各自的Controller（CompletionBoxController等）控制显示

## 关键文件修改

### 1. bilimiao-compose 模块新增文件

#### ComposeScaffold.kt
- 位置：`bilimiao-compose/src/main/java/cn/a10miaomiao/bilimiao/compose/components/layout/ComposeScaffold.kt`
- 功能：Compose版本的Scaffold，管理Player层、内容层、Mask层
- 关键逻辑：
  - Player在全屏时fillMaxSize()
  - 非全屏时使用smallModePlayerCurrentHeight
  - overlay视图默认隐藏(View.GONE)

#### StartViewWrapper.kt
- 位置：`bilimiao-compose/src/main/java/cn/a10miaomiao/bilimiao/compose/StartViewWrapper.kt`
- 新增属性：
  ```kotlin
  var playerView: View? = null
  var completionView: View? = null
  var errorMessageView: View? = null
  var areaLimitView: View? = null
  var loadingView: View? = null
  var showPlayer: Boolean = false
  var orientation: Int = 0  // 1=VERTICAL, 2=HORIZONTAL
  var fullScreenPlayer: Boolean = false
  var smallModePlayerMinHeight: Int = 0
  var smallModePlayerCurrentHeight: Int = 0
  ```

### 2. bilimiao-compose 模块修改文件

#### ComposeFragment.kt
- 使用ComposeScaffold替换原来的Box
- 通过LaunchedEffect同步Player状态到ComposeScaffoldState

### 3. app 模块修改文件

#### MainUi.kt
- 移除mPlayerLayout（不再添加到原生ScaffoldView）
- 单独暴露overlay视图：
  ```kotlin
  val mCompletionView: View by lazy { ... }
  val mErrorMessageView: View by lazy { ... }
  val mAreaLimitView: View by lazy { ... }
  val mLoadingView: View by lazy { ... }
  ```

#### MainActivity.kt
- `setupPlayerViewInWrapper()` - 传递Player视图和状态到StartViewWrapper
- 设置`ui.root.hideContentOnFullScreen = false` 防止全屏时隐藏content

#### PlayerViews.kt
- 添加方法支持外部注入Player视图：
  ```kotlin
  fun setVideoPlayer(player: DanmakuVideoPlayer?)
  fun setOverlayViews(loading, completion, errorMessage, areaLimit)
  ```

#### PlayerDelegate2.kt
- 添加`setVideoPlayerView()`方法

#### LoadingBoxController.kt, CompletionBoxController.kt, ErrorMessageBoxController.kt, AreaLimitBoxController.kt
- 构造函数添加view参数，支持从外部传入

#### ScaffoldView.kt
- 添加`onFullScreenPlayerChanged`回调
- 添加`hideContentOnFullScreen`属性（默认true）

#### ContentBehavior.kt
- 修改：当`parent.hideContentOnFullScreen`为false时，全屏不隐藏content

## 状态同步流程

```
原生ScaffoldView/PlayerDelegate
    ↓
MainActivity.setupPlayerViewInWrapper()
    ↓
StartViewWrapper (属性)
    ↓ LaunchedEffect
ComposeScaffoldState
    ↓ 状态驱动
ComposeScaffold UI
```

## 待解决问题

1. **横屏时浮动可拖拽窗口**
   - 原生通过PlayerBehaviorDelegate实现拖拽
   - 需要在Compose中实现类似逻辑或保留原生PlayerBehavior

2. **竖屏时视频窗口高度计算**
   - 需要确保smallModePlayerMinHeight等值正确同步

## 测试要点

1. ✅ 竖屏播放正常
2. ✅ 播放完成显示completionView
3. ✅ 全屏播放（不再白屏）
4. ⏳ 横屏浮动窗口拖拽
