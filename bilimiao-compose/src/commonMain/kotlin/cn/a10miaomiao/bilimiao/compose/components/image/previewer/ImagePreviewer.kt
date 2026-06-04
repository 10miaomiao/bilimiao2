package cn.a10miaomiao.bilimiao.compose.components.image.previewer

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import cn.a10miaomiao.bilimiao.compose.components.image.pager.ImageLoading
import cn.a10miaomiao.bilimiao.compose.components.image.pager.ProceedPresentation
import cn.a10miaomiao.bilimiao.compose.components.image.pager.defaultImageLoading
import cn.a10miaomiao.bilimiao.compose.components.image.pager.defaultProceedPresentation
import cn.a10miaomiao.bilimiao.compose.components.image.viewer.ModelProcessor
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_ITEM_SPACE
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerGestureScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.DEFAULT_PREVIEWER_ENTER_TRANSITION
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.DEFAULT_PREVIEWER_EXIT_TRANSITION
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.Previewer
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.PreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformLayerScope

/**
 * 默认的容器背景
 */
val defaultPreviewBackground: (@Composable () -> Unit) = {
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
    )
}

/**
 * 图片弹出预览组件
 *
 * @param modifier 图层修饰
 * @param state 控件状态与控制对象
 * @param itemSpacing 每一页的间隔
 * @param beyondViewportPageCount 超出视口的页面缓存的个数
 * @param enter 不使用转换效果时的弹出动效
 * @param exit 不使用转换效果时的退出动效
 * @param debugMode 调试模式，显示图层标识等
 * @param detectGesture 手势监听对象
 * @param processor 用于解析图像数据的方法，可以自定义
 * @param imageLoader 图像加载器，支持的图像类型与ImageViewer一致，如果需要支持其他类型的数据可以自定义processor
 * @param imageLoading 图像未完成加载时的占位
 * @param imageModelProcessor 用于控制ZoomableView、Loading等图层的切换逻辑，可以自定义
 * @param previewerLayer 预览器容器的自定义，可设置背景、前景等
 * @param pageDecoration 每一页的图层修饰，可以用来设置页面的前景、背景等
 */
@Composable
fun ImagePreviewer(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    state: PreviewerState,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    beyondViewportPageCount: Int = DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT,
    enter: EnterTransition = DEFAULT_PREVIEWER_ENTER_TRANSITION,
    exit: ExitTransition = DEFAULT_PREVIEWER_EXIT_TRANSITION,
    debugMode: Boolean = false,
    detectGesture: PagerGestureScope = PagerGestureScope(),
    processor: ModelProcessor = ModelProcessor(),
    imageLoader: @Composable (Int) -> Pair<Any?, Size?>,
    imageLoading: ImageLoading? = defaultImageLoading,
    proceedPresentation: ProceedPresentation = defaultProceedPresentation,
    previewerLayer: TransformLayerScope = TransformLayerScope(
        background = defaultPreviewBackground
    ),
    pageDecoration: @Composable (page: Int, innerPage: @Composable () -> Boolean) -> Boolean
    = { _, innerPage -> innerPage() },
) {
    Previewer(
        modifier = modifier,
        contentPadding = contentPadding,
        state = state,
        previewerLayer = previewerLayer,
        itemSpacing = itemSpacing,
        beyondViewportPageCount = beyondViewportPageCount,
        enter = enter,
        exit = exit,
        debugMode = debugMode,
        detectGesture = detectGesture,
        zoomablePolicy = { page ->
            pageDecoration.invoke(page) decoration@{
                val (model, size) = imageLoader.invoke(page)
                proceedPresentation.invoke(this, model, size, processor, imageLoading)
            }
        }
    )
}