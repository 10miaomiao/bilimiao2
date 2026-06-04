package cn.a10miaomiao.bilimiao.compose.components.image.pager


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import cn.a10miaomiao.bilimiao.compose.components.image.viewer.AnyComposable
import cn.a10miaomiao.bilimiao.compose.components.image.viewer.ModelProcessor
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.DEFAULT_ITEM_SPACE
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerGestureScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.PagerZoomablePolicyScope
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.ZoomablePager
import cn.a10miaomiao.bilimiao.compose.components.zoomable.pager.ZoomablePagerState

/**
 * 基于Pager实现的图片浏览器
 *
 * @param modifier 图层修饰
 * @param pagerState 控件状态与控制对象
 * @param itemSpacing 每一页的间隔
 * @param beyondViewportPageCount 超出视口的页面缓存的个数
 * @param userScrollEnabled 是否允许页面滚动
 * @param detectGesture 手势监听对象
 * @param processor 用于解析图像数据的方法，可以自定义
 * @param imageLoader 图像加载器，支持的图像类型与ImageViewer一致，如果需要支持其他类型的数据可以自定义processor
 * @param imageLoading 图像未完成加载时的占位
 * @param proceedPresentation 用于控制ZoomableView、Loading等图层的切换逻辑，可以自定义
 * @param pageDecoration 每一页的图层修饰，可以用来设置页面的前景、背景等
 */
@Composable
fun ImagePager(
    modifier: Modifier = Modifier,
    pagerState: ZoomablePagerState,
    itemSpacing: Dp = DEFAULT_ITEM_SPACE,
    beyondViewportPageCount: Int = DEFAULT_BEYOND_VIEWPORT_ITEM_COUNT,
    userScrollEnabled: Boolean = true,
    detectGesture: PagerGestureScope = PagerGestureScope(),
    processor: ModelProcessor = ModelProcessor(),
    imageLoader: @Composable (Int) -> Pair<Any?, Size?>,
    imageLoading: ImageLoading? = defaultImageLoading,
    proceedPresentation: ProceedPresentation = defaultProceedPresentation,
    pageDecoration: @Composable (page: Int, innerPage: @Composable () -> Unit) -> Unit
    = { _, innerPage -> innerPage() },
) {
    ZoomablePager(
        modifier = modifier,
        state = pagerState,
        itemSpacing = itemSpacing,
        beyondViewportPageCount = beyondViewportPageCount,
        userScrollEnabled = userScrollEnabled,
        detectGesture = detectGesture,
    ) { page ->
        pageDecoration.invoke(page) {
            val (model, size) = imageLoader.invoke(page)
            proceedPresentation.invoke(this, model, size, processor, imageLoading)
        }
    }
}

/**
 * 用于控制ZoomableView、Loading等图层的切换
 */
typealias ProceedPresentation = @Composable PagerZoomablePolicyScope.(
    model: Any?,
    size: Size?,
    processor: ModelProcessor,
    imageLoading: ImageLoading?,
) -> Boolean

/**
 * 默认ImageModelProcessor
 */
val defaultProceedPresentation: ProceedPresentation = { model, size, processor, imageLoading ->
    // TODO 这里是否要添加渐变动画?
    if (model != null && model is AnyComposable && size == null) {
        model.composable.invoke()
        true
    } else if (model != null && size != null) {
        ZoomablePolicy(intrinsicSize = size) {
            processor.Deploy(model = model, state = it)
        }
        size.isSpecified
    } else {
        imageLoading?.invoke()
        false
    }
}

/**
 * 图像未完成加载时的占位
 */
typealias ImageLoading = @Composable () -> Unit

/**
 * 默认ImageLoading
 */
val defaultImageLoading: ImageLoading = {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = Color.LightGray,
        )
    }
}