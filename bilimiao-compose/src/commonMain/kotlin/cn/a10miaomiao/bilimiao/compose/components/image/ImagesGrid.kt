package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerController
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import cn.a10miaomiao.bilimiao.compose.components.image.provider.localImagePreviewerController
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.PreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformItemView
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.VerticalDragType
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberPreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberTransformItemState
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import kotlin.math.min

@Composable
private fun ImagesGridItem(
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    index: Int,
    imageModels: List<PreviewImageModel>,
    previewerController: ImagePreviewerController,
    previewerState: PreviewerState,
) {
    val model = imageModels[index]
    val itemState = rememberTransformItemState(
        intrinsicSize = Size(model.width, model.height),
    )
//    ScaleGrid(
//        modifier = modifier,
//        detectGesture = DetectScaleGridGesture(
//            onPress = {
//                previewerController.enterTransform(
//                    state = previewerState,
//                    models = imageModels,
//                    index = index
//                )
//            }
//        )
//    ) {
    Box(
        modifier = modifier.clickable {
            previewerController.enterTransform(
                state = previewerState,
                models = imageModels,
                index = index
            )
        }
    ) {
        TransformItemView(
            key = model.originalUrl,
            itemState = itemState,
            transformState = previewerState,
        ) {
            MiaoAsyncImage(
                modifier = Modifier.fillMaxSize(),
                contentDescription = null,
                model = if (previewerController.isImageLoaded(model.originalUrl)) {
                    model.originalUrl
                } else { model.previewUrl },
                contentScale = contentScale,
            )
        }
    }
}

/**
 * 计算单张图片的显示尺寸。
 *
 * 以图片自身尺寸（px 按当前屏幕密度换算成 dp）为基准等比缩放：
 * - 长边超过 [maxSize] 时，等比缩小到 [maxSize] 以内；
 * - 图片自身小于 [maxSize] 时保持图片自身大小，不放大。
 */
private fun calculateSingleImageSize(
    intrinsicWidth: Float,
    intrinsicHeight: Float,
    maxSize: Dp,
    density: Density,
): DpSize {
    val limit = maxSize.value
    if (intrinsicWidth <= 0f || intrinsicHeight <= 0f || limit <= 0f) {
        return DpSize(limit.dp, limit.dp)
    }
    val naturalWidth = with(density) { intrinsicWidth.toDp().value }
    val naturalHeight = with(density) { intrinsicHeight.toDp().value }
    val scale = min(1f, min(limit / naturalWidth, limit / naturalHeight))
    return DpSize(
        (naturalWidth * scale).dp,
        (naturalHeight * scale).dp,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImagesGrid(
    imageModels: List<PreviewImageModel>,
) {
    val count = imageModels.size
    val previewerController = localImagePreviewerController()
    val previewerState = rememberPreviewerState(
        verticalDragType = VerticalDragType.Down,
        pageCount = { count },
        getKey = { imageModels[it].originalUrl },
    )
    if (count == 1) {
        val model = imageModels[0]
        BoxWithConstraints {
            val density = LocalDensity.current
            val maxSize = min(maxWidth.value, 300f).dp
            val imageSize = remember(model.width, model.height, maxSize, density) {
                calculateSingleImageSize(
                    intrinsicWidth = model.width,
                    intrinsicHeight = model.height,
                    maxSize = maxSize,
                    density = density,
                )
            }
            ImagesGridItem(
                modifier = Modifier.size(imageSize.width, imageSize.height),
                index = 0,
                imageModels = imageModels,
                previewerController = previewerController,
                previewerState = previewerState,
            )
        }
    } else if (count <= 4) {
        BoxWithConstraints {
            val width = min(maxWidth.value, 300f)
            FlowRow(
                modifier = Modifier.width(width.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (index in 0 until count) {
                    ImagesGridItem(
                        modifier = Modifier.size((width / 2 - 4).dp),
                        index = index,
                        imageModels = imageModels,
                        previewerController = previewerController,
                        previewerState = previewerState,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    } else {
        BoxWithConstraints {
            val width = min(maxWidth.value, 330f)
            FlowRow(
                modifier = Modifier.width(width.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (index in 0 until count) {
                    ImagesGridItem(
                        modifier = Modifier.size((width / 3 - 3).dp),
                        index = index,
                        imageModels = imageModels,
                        previewerController = previewerController,
                        previewerState = previewerState,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}