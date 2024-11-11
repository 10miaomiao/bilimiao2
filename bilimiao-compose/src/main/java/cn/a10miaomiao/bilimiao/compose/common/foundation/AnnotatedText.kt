package cn.a10miaomiao.bilimiao.compose.common.foundation

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Stable
sealed class AnnotatedTextNode {
    @Stable
    class Text(val text: String) : AnnotatedTextNode()
    @Stable
    class Emote(
        val text: String,
        val url: String,
//        val width: Int,
//        val height: Int
    ) : AnnotatedTextNode()
    @Stable
    class Link(val text: String, val url: String) : AnnotatedTextNode()
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun inlineAnnotatedContent(
    nodes: List<AnnotatedTextNode>,
    size: TextUnit = 20.sp,
    inlineVerticalAlign: PlaceholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
):  Map<String, InlineTextContent> {
    return nodes.filterIsInstance<AnnotatedTextNode.Emote>().map { node ->
        node.text to InlineTextContent(
            placeholder = Placeholder(
                width = size,
                height = size,
                placeholderVerticalAlign = inlineVerticalAlign,
            ),
            children = {
                GlideImage(
                    model = UrlUtil.autoHttps(node.url),
                    contentDescription = null,
                )
            }
        )
    }.toMap()
}

@Composable
fun AnnotatedText(
    nodes: List<AnnotatedTextNode>
): AnnotatedString {
    return buildAnnotatedString {
        nodes.forEach {
            when (it) {
                is AnnotatedTextNode.Text -> append(it.text)
                is AnnotatedTextNode.Emote -> {
                    appendInlineContent(it.text)
                }
                is AnnotatedTextNode.Link -> {
                    append("[${it.text}]")
                    append(it.url)
                }
            }
        }
    }
}