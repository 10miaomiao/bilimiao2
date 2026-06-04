package cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer


import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_ITEM_SPACE
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerGestureScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerZoomablePolicyScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.SupportedPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

// 比较轻柔的动画窗格
val DEFAULT_SOFT_ANIMATION_SPEC = tween<Float>(400)

/**
 * @program: TransformPreviewer
 *
 * @description:
 *
 * @author: JVZIYAOYAO
 *
 * @create: 2023-12-11 20:21
 **/

/**
 * 用于控制转换效果图层与图片列表浏览图层
 *
 * @property scope 协程作用域
 * @property defaultAnimationSpec 默认动画窗格
 * @property itemStateMap 用于获取transformItemState
 * @property getKey 根据下标获取唯一标识的方法
 * @constructor
 *
 * @param pagerState
 */
open class TransformPreviewerState(
    // 协程作用域
    private val scope: CoroutineScope,
    // 默认动画窗格
    var defaultAnimationSpec: AnimationSpec<Float> = DEFAULT_SOFT_ANIMATION_SPEC,
    // 预览状态
    pagerState: SupportedPagerState,
    // 用于获取transformItemState
    private var itemStateMap: ItemStateMap,
    // 获取当前key
    val getKey: (Int) -> Any,
) : PopupPreviewerState(pagerState) {

    val itemContentVisible = mutableStateOf(false)

    val containerSize = mutableStateOf(Size.Zero)

    val displayWidth = Animatable(0F)

    val displayHeight = Animatable(0F)

    val displayOffsetX = Animatable(0F)

    val displayOffsetY = Animatable(0F)

    var offsetSize = Size(0f, 0f)

    // 查找key关联的transformItem
    private fun findTransformItem(key: Any): TransformItemState? {
        return itemStateMap[key]
    }

    // 根据index查询key
    fun findTransformItemByIndex(index: Int): TransformItemState? {
        val key = getKey(index)
        return findTransformItem(key)
    }

    val enterIndex = mutableStateOf<Int?>(null)

    val mountedFlow = MutableStateFlow(false)

    val decorationAlpha = Animatable(0F)

    val previewerAlpha = Animatable(0F)

    private suspend fun awaitMounted() {
        mountedFlow.takeWhile { !it }.collect { }
    }

    private suspend fun enterTransformInternal(
        index: Int,
        animationSpec: AnimationSpec<Float>? = null
    ) {
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        val itemState = findTransformItemByIndex(index)
        if (itemState != null) {
            itemState.apply {
                stateOpenStart()

                mountedFlow.value = false

                enterIndex.value = index
                // 设置动画开始的位置
                displayWidth.snapTo(blockSize.width.toFloat())
                displayHeight.snapTo(blockSize.height.toFloat())
                displayOffsetX.snapTo(blockPosition.x)
                displayOffsetY.snapTo(blockPosition.y)
                itemContentVisible.value = true

                // 关闭修饰图层
                decorationAlpha.snapTo(0F)
                previewerAlpha.snapTo(0F)
                // 开启viewer图层
                animateContainerVisibleState = MutableTransitionState(true)

                val displaySize = if (intrinsicSize != null && intrinsicSize!!.isSpecified) {
                    getDisplaySize(intrinsicSize!!, containerSize.value)
                } else {
                    getDisplaySize(containerSize.value, containerSize.value)
                }
//                val displaySize = getDisplaySize(intrinsicSize ?: Size.Zero, containerSize.value)
                val targetX = (containerSize.value.width - displaySize.width).div(2) + offsetSize.width
                val targetY = (containerSize.value.height - displaySize.height).div(2) + offsetSize.height
//                val animationSpec = tween<Float>(600)f
                try {
                    listOf(
                        scope.async {
                            decorationAlpha.animateTo(1F, currentAnimationSpec)
                        },
                        scope.async {
                            displayWidth.animateTo(displaySize.width, currentAnimationSpec)
                        },
                        scope.async {
                            displayHeight.animateTo(displaySize.height, currentAnimationSpec)
                        },
                        scope.async {
                            displayOffsetX.animateTo(targetX, currentAnimationSpec)
                        },
                        scope.async {
                            displayOffsetY.animateTo(targetY, currentAnimationSpec)
                        },
                    ).awaitAll()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                previewerAlpha.snapTo(1F)

                updateState(animating = false, visible = false, visibleTarget = true)

                val alreadyScroll2Current = try {
                    // 切换页面到index
                    pagerState.scrollToPage(index)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }

                // 等待挂载成功
                if (alreadyScroll2Current) awaitMounted()
                // 动画结束，开启预览
                itemContentVisible.value = false
                // 恢复
                enterIndex.value = null

//                stateOpenEnd()
                updateState(animating = false, visible = true, visibleTarget = null)
            }
        } else {
            open(index)
        }
    }

    private var enterTransformJob: Job? = null

    suspend fun enterTransform(
        index: Int,
        animationSpec: AnimationSpec<Float>? = null,
    ) {
        enterTransformJob = scope.launch {
            enterTransformInternal(index, animationSpec)
        }
        enterTransformJob?.join()
    }

    internal fun cancelEnterTransform() {
        enterTransformJob?.cancel()
        enterIndex.value = null
    }

    suspend fun exitTransform(animationSpec: AnimationSpec<Float>? = null) {
        // 取消开启动画
        cancelEnterTransform()
        // 获取当前页码
        val index = currentPage
        // 同步动画开始的位置
        val itemState = findTransformItemByIndex(index)
        if (itemState != null && itemState.blockSize != IntSize.Zero) {
            itemState.apply {
                stateCloseStart()

                val displaySize = getDisplaySize(intrinsicSize ?: Size.Zero, containerSize.value)
                val displayX = (containerSize.value.width - displaySize.width).div(2)
                val displayY = (containerSize.value.height - displaySize.height).div(2)
                var targetSize = displaySize
                var targetX = displayX
                var targetY = displayY
                zoomableViewState.value?.apply {
                    targetSize = displaySize * scale.value
                    targetX =
                        offsetX.value + displayX - (targetSize.width - displaySize.width).div(2)
                    targetY =
                        offsetY.value + displayY - (targetSize.height - displaySize.height).div(2)
                }
                displayWidth.snapTo(targetSize.width)
                displayHeight.snapTo(targetSize.height)
                displayOffsetX.snapTo(targetX)
                displayOffsetY.snapTo(targetY)

                // 启动关闭
                exitFromCurrentState(itemState, animationSpec)

                stateCloseEnd()
            }
        } else {
            close()
        }
    }

    internal suspend fun exitFromCurrentState(
        itemState: TransformItemState,
        animationSpec: AnimationSpec<Float>? = null,
    ) {
        val currentAnimationSpec = animationSpec ?: defaultAnimationSpec
        // 动画结束，开启预览
        itemContentVisible.value = true
        // 关闭viewer图层
        previewerAlpha.snapTo(0F)

        try {
            itemState.apply {
                listOf(
                    scope.async {
                        decorationAlpha.animateTo(0F, currentAnimationSpec)
                    },
                    scope.async {
                        displayWidth.animateTo(blockSize.width.toFloat(), currentAnimationSpec)
                    },
                    scope.async {
                        displayHeight.animateTo(blockSize.height.toFloat(), currentAnimationSpec)
                    },
                    scope.async {
                        displayOffsetX.animateTo(blockPosition.x, currentAnimationSpec)
                    },
                    scope.async {
                        displayOffsetY.animateTo(blockPosition.y, currentAnimationSpec)
                    },
                ).awaitAll()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 关闭viewer图层
        animateContainerVisibleState = MutableTransitionState(false)
        // 关闭图层
        itemContentVisible.value = false
    }

    override suspend fun openAction(
        index: Int,
        enterTransition: EnterTransition?,
    ) {
        // 显示修饰图层
        decorationAlpha.snapTo(1F)
        previewerAlpha.snapTo(1F)
        super.openAction(index, enterTransition)
    }

}

/**
 * 获取一个组件在容器中完全显示时的大小
 *
 * @param contentSize 组件的固有大小
 * @param containerSize 容器大小
 * @return 返回显示的大小
 */
fun getDisplaySize(contentSize: Size, containerSize: Size): Size {
    val containerRatio = containerSize.run {
        width.div(height)
    }
    val contentRatio = contentSize.run {
        width.div(height)
    }
    val widthFixed = contentRatio > containerRatio
    val scale1x = if (widthFixed) {
        containerSize.width.div(contentSize.width)
    } else {
        containerSize.height.div(contentSize.height)
    }
    return Size(
        width = contentSize.width.times(scale1x),
        height = contentSize.height.times(scale1x),
    )
}

/**
 * 转换过程中的转换动效图层
 *
 * @param state 用于控制转换效果图层与图片列表浏览图层
 * @param debugMode 调试模式
 */
@Composable
fun TransformContentLayer(
    state: TransformPreviewerState,
    debugMode: Boolean = false,
) {
    LocalDensity.current.apply {
        state.apply {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val maxWidthPx = maxWidth.toPx()
                val maxHeightPx = maxHeight.toPx()
                val fitSize = getDisplaySize(
                    containerSize = Size(maxWidthPx, maxHeightPx),
                    contentSize = Size(displayWidth.value, displayHeight.value),
                )
                if (fitSize.isSpecified) {
                    val targetScaleX = displayWidth.value.div(fitSize.width)
                    val targetScaleY = displayHeight.value.div(fitSize.height)
                    val actionColor = Color.Green
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = targetScaleX
                                scaleY = targetScaleY
                                translationX = displayOffsetX.value
                                translationY = displayOffsetY.value
                                transformOrigin = TransformOrigin(0F, 0F)
                            }
                            .size(
                                width = fitSize.width.toDp(),
                                height = fitSize.height.toDp()
                            )
                            .run {
                                if (debugMode) border(width = 2.dp, color = actionColor) else this
                            }
                    ) {
                        val item = findTransformItemByIndex(enterIndex.value ?: currentPage)
                        item?.blockCompose?.invoke(item.key)
                        if (debugMode) Text(text = "Transform", color = actionColor)
                    }
                } else {
                    if (debugMode) Text(text = "FitSize unspecified", color = Color.Yellow)
                }
            }
        }
    }
}

/**
 * 转换效果在切换到实际显示图层前的占位图层
 *
 * @param page 当前页码
 * @param state 图层控制对象
 * @param debugMode 是否处于调试模式
 */
@Composable
fun TransformContentForPage(
    page: Int,
    state: TransformPreviewerState,
    debugMode: Boolean = false,
) {
    state.apply {
        val density = LocalDensity.current
        val item = findTransformItemByIndex(page)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            density.apply {
                item?.apply {
                    val containerSize = Size(maxWidth.toPx(), maxHeight.toPx())
                    intrinsicSize?.run {
                        if (isSpecified) Size(width, height) else containerSize
                    }?.let { contentSize ->
                        val displaySize = getDisplaySize(
                            containerSize = containerSize,
                            contentSize = contentSize,
                        )
                        val actionColor = Color.Cyan
                        Box(
                            modifier = Modifier
                                .size(
                                    width = displaySize.width.toDp(),
                                    height = displaySize.height.toDp(),
                                )
                                .run {
                                    if (debugMode) border(
                                        width = 2.dp,
                                        color = actionColor
                                    ) else this
                                }
                                .align(Alignment.Center),
                        ) {
                            blockCompose.invoke(item.key)
                            if (debugMode) Text(text = "TransformForPage", color = actionColor)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 通过这个对象可以自定义预览图层
 *
 * @property previewerDecoration 图层修饰
 * @property background 背景图层
 * @property foreground 前景图层
 */
class TransformLayerScope(
    var previewerDecoration: @Composable (innerBox: @Composable () -> Unit) -> Unit =
        @Composable { innerBox -> innerBox() },
    var background: @Composable () -> Unit = {},
    var foreground: @Composable () -> Unit = {},
)

/**
 * 支持弹出转换动画的图片预览组件
 *
 * @param modifier 图层修饰
 * @param state 状态对象
 * @param itemSpacing 图片间的间隔
 * @param beyondViewportPageCount 页面外缓存个数
 * @param enter 调用open时的进入动画
 * @param exit 调用close时的退出动画
 * @param debugMode 调试模式
 * @param detectGesture 检测手势
 * @param previewerLayer 容器的图层修饰
 * @param zoomablePolicy 缩放图层的修饰
 */
@Composable
fun TransformPreviewer(
    modifier: Modifier = Modifier,
    state: TransformPreviewerState,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    beyondViewportPageCount: Int = DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT,
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    debugMode: Boolean = false,
    detectGesture: PagerGestureScope = PagerGestureScope(),
    previewerLayer: TransformLayerScope = TransformLayerScope(),
    zoomablePolicy: @Composable PagerZoomablePolicyScope.(page: Int) -> Boolean,
) {
    state.apply {
        Box(modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                containerSize.value = it.toSize()
            }) {
            PopupPreviewer(
                modifier = modifier.fillMaxSize(),
                state = this@apply,
                detectGesture = detectGesture,
                enter = enter,
                exit = exit,
                itemSpacing = itemSpacing,
                beyondViewportPageCount = beyondViewportPageCount,
                zoomablePolicy = { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        val zoomableMounted = remember { mutableStateOf(false) }
                        if (!zoomableMounted.value) {
                            TransformContentForPage(
                                page = page,
                                state = state,
                                debugMode = debugMode
                            )
                        }
                        zoomableMounted.value = zoomablePolicy(page)
                        LaunchedEffect(zoomableMounted.value) {
//                            Log.i(
//                                "TAG",
//                                "TransformBody TransformPreviewer: ${enterIndex.value == page} ~ ${zoomableMounted.value} ~ $page"
//                            )
                            if (enterIndex.value == page && zoomableMounted.value) {
//                                delay(1000)
                                mountedFlow.emit(true)
                            }
                        }
                        // 如果等待mounted时间很久，用户切换页面过快导致开启的页面被移除
                        DisposableEffect(Unit) {
                            onDispose {
                                if (enterIndex.value == page && zoomableMounted.value) {
                                    if (!mountedFlow.value) {
                                        mountedFlow.value = true
                                    }
                                }
                            }
                        }
                    }
                },
                previewerDecoration = { innerBox ->
                    @Composable
                    fun capsuleLayer(content: @Composable () -> Unit) {
                        Box(
                            modifier = Modifier
                                .alpha(decorationAlpha.value)
                        ) { content() }
                    }
                    previewerLayer.apply {
                        capsuleLayer { background() }
                        previewerDecoration {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(previewerAlpha.value)
                            ) {
                                innerBox()
                            }
                        }
                        capsuleLayer { foreground() }
                    }
                }
            )

            if (itemContentVisible.value && previewerAlpha.value != 1F) {
                TransformContentLayer(state = state, debugMode = debugMode)
            }
        }
    }

}

/**
 * 用于实现Previewer变换效果的小图装载容器
 *
 * @param modifier 图层修饰
 * @param key 唯一标识
 * @param itemState 该组件的状态与控制对象
 * @param transformState 预览组件的状态与控制对象
 * @param content 需要显示的实际内容
 */
@Composable
fun TransformItemView(
    modifier: Modifier = Modifier,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    transformState: TransformPreviewerState,
    content: @Composable (Any) -> Unit,
) {
    transformState.apply {
        val currentPageKey = try {
            getKey(currentPage)
        } catch (e: Exception) {
            null
        }
        val isCurrentPage = currentPageKey != key
        TransformItemView(
            modifier = modifier,
            key = key,
            itemState = itemState,
            itemVisible = if (!itemContentVisible.value) {
                if (previewerAlpha.value == 1F) {
                    if (!visible) true else isCurrentPage
                } else true
            } else {
                if (previewerAlpha.value == 1F) {
                    isCurrentPage
                } else {
                    if (enterIndex.value != null) {
                        getKey(enterIndex.value!!) != key
                    } else isCurrentPage
                }
            },
            content = content,
        )
    }
}