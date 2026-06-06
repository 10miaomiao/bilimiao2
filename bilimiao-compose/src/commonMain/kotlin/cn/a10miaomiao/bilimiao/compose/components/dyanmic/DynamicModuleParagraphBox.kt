package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bilibili.app.dynamic.v2.ModuleParagraph
import bilibili.app.dynamic.v2.Paragraph
import bilibili.app.dynamic.v2.PicParagraph
import cn.a10miaomiao.bilimiao.compose.common.foundation.annotatedText
import cn.a10miaomiao.bilimiao.compose.common.foundation.inlineAnnotatedContent
import cn.a10miaomiao.bilimiao.compose.common.foundation.toAnnotatedTextNode
import cn.a10miaomiao.bilimiao.compose.components.image.ImagesGrid
import cn.a10miaomiao.bilimiao.compose.components.image.ImagesScroll
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import kotlin.math.min

@Composable
fun DynamicModuleParagraphBox(
    paragraph: ModuleParagraph
) {
    when(val content = paragraph.paragraph?.content) {
        is Paragraph.Content.Text -> {
            val nodes = content.value.nodes.toAnnotatedTextNode()
            val emoteMap = inlineAnnotatedContent(nodes)
            Text(
                annotatedText(nodes),
                modifier = Modifier.padding(horizontal = 10.dp),
                inlineContent = emoteMap,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        is Paragraph.Content.Pic -> {
            val items = content.value.pics?.items ?: listOf()
            val imageModels = remember(items) {
                items.map {
                    val w = min(600, it.width)
                    val h = w * it.width / it.height
                    val url = UrlUtil.autoHttps(it.src)
                    PreviewImageModel(
                        previewUrl = url + "@${w}w_${h}h",
                        originalUrl = url,
                        height = it.height.toFloat(),
                        width = it.width.toFloat(),
                    )

                }
            }
            val style = content.value.style.value
            if (style == PicParagraph.PicParagraphStyle.BIG_SCROLL.value) {
                ImagesScroll(imageModels)
            } else {
                Box(modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                )) {
                    ImagesGrid(imageModels)
                }
            }
        }
        is Paragraph.Content.Line -> {

        }
        is Paragraph.Content.LinkCard -> {

        }
        null -> Unit
    }
}