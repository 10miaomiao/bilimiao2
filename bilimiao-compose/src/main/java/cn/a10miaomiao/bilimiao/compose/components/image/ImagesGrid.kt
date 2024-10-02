package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlin.math.min

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImagesGrid(
    urls: List<String>,
) {
    val count = urls.size
    if (count == 1) {
        GlideImage(
            imageModel = UrlUtil.autoHttps(urls[0]),
            modifier = Modifier.widthIn(max = 300.dp),
            imageOptions = ImageOptions(
                contentScale = ContentScale.FillWidth
            )
        )
    } else if (count <= 4) {
        BoxWithConstraints {
            val width = min(maxWidth.value, 300f)
            FlowRow(
                modifier = Modifier.width(width.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                urls.forEach {
                    GlideImage(
                        imageModel = UrlUtil.autoHttps(it),
                        modifier = Modifier.size((width / 2 - 2).dp),
                        previewPlaceholder = R.drawable.bili_default_img_tv,
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
                urls.forEach {
                    GlideImage(
                        imageModel = UrlUtil.autoHttps(it),
                        modifier = Modifier.size((width / 3 - 3).dp),
                    )
                }
            }
        }
    }
}