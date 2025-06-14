package cn.a10miaomiao.bilimiao.compose.common.foundation

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
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
    class Link(
        val text: String,
        val url: String,
        val withLineBreak: Boolean = false, // 表示前面要加换行符
    ) : AnnotatedTextNode()
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
fun annotatedText(
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
                    if (it.withLineBreak) {
                        append("\n")
                    }
                    withLink(
                        LinkAnnotation.Url(
                            it.url,
                            TextLinkStyles(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                    ) {
                        append(it.text)
                    }
                }
            }
        }
    }
}