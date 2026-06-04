package cn.a10miaomiao.bilimiao.compose.components.image.previewer


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import cn.a10miaomiao.bilimiao.compose.components.image.viewer.AnyComposable
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformItemView
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformPreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberTransformItemState

/**
 * 支持图片变换效果的ImageView，简单理解为：小图转换为大图动效中的小图
 * 此接口的目的是方便开发者直接调用，如需更深入的定制化，推荐直接使用TransformItemView
 *
 * @param modifier 图层修饰
 * @param imageLoader 图片加载器，Triple<Any, Size, Any>，
 *                      其中三个参数分别为：Key、图片数据、图片大小、
 *                      图片数据支持Painter、ImageBitmap、ImageVector、AnyComposable
 *                      Key必须要与transformState中输入的key一致
 * @param imageItemContent 用于解析图像数据的方法，可以自定义
 * @param transformState TransformPreviewerState或其实现类
 */
@Composable
fun TransformImageView(
    modifier: Modifier = Modifier,
    imageLoader: @Composable () -> Triple<Any, Any, Size>,
    imageItemContent: ImageItemContent = defaultImageItemContent,
    transformState: TransformPreviewerState,
) {
    val (key, model, size) = imageLoader.invoke()
    val itemState =
        rememberTransformItemState(intrinsicSize = size)
    TransformItemView(
        modifier = modifier,
        key = key,
        itemState = itemState,
        transformState = transformState,
    ) {
        imageItemContent.invoke(model)
    }
}

/**
 * 用于解析图像数据给TransformItemView显示的方法
 */
typealias ImageItemContent = @Composable (Any) -> Unit

/**
 * 默认处理，当前model仅支持Painter、ImageBitmap、ImageVector、AnyComposable
 */
val defaultImageItemContent: ImageItemContent = { model ->
    when (model) {
        is Painter -> {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = model,
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }

        is ImageBitmap -> {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = model,
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }

        is ImageVector -> {
            Image(
                modifier = Modifier.fillMaxSize(),
                imageVector = model,
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }

        is AnyComposable -> {
            model.composable.invoke()
        }

    }
}