package cn.a10miaomiao.bilimiao.compose.comm.foundation

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.skydoves.landscapist.glide.GlideImage

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

@Composable
fun inlineAnnotatedContent(
    nodes: List<AnnotatedTextNode>,
    size: TextUnit = 20.sp
):  Map<String, InlineTextContent> {
    return nodes.filterIsInstance<AnnotatedTextNode.Emote>().map { node ->
        node.text to InlineTextContent(
            placeholder = Placeholder(
                width = size,
                height = size,
                placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
            ),
            children = {
                GlideImage(
                    imageModel = UrlUtil.autoHttps(node.url),
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