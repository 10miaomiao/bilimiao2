package cn.a10miaomiao.bilimiao.compose.commponents.dyanmic

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import bilibili.app.dynamic.v2.TextNode
import bilibili.app.dynamic.v2.Paragraph;
import cn.a10miaomiao.bilimiao.compose.comm.foundation.AnnotatedText
import cn.a10miaomiao.bilimiao.compose.comm.foundation.AnnotatedTextNode
import cn.a10miaomiao.bilimiao.compose.comm.foundation.inlineAnnotatedContent

@Composable
fun Paragraph.toAnnotatedTextNode(): List<AnnotatedTextNode> {
    return text?.nodes?.map {
        val node = it.text
        when (node) {
            is TextNode.Text.Word -> {
                AnnotatedTextNode.Text(node.value.words)
            }

            is TextNode.Text.Link -> {
                AnnotatedTextNode.Link(node.value.showText, node.value.link)
            }

            is TextNode.Text.Emote -> {
                val emote = node.value
                val emoteId = emote.rawText?.words ?: emote.emoteUrl
                AnnotatedTextNode.Emote(
                    text = emoteId,
                    url = emote.emoteUrl,
//                        width = emote.emoteWidth?.width?.toInt() ?: 20,
//                        height = (emote.emoteWidth?.emojiSize ?: 2) * 16,
                )
            }

            null -> null
        }
    }?.filterNotNull() ?: listOf()
}

@Composable
private fun DynamicParagraphRender(p: Paragraph) {
    val nodes = p.toAnnotatedTextNode()
    val emoteMap = inlineAnnotatedContent(nodes)
    Text(
        AnnotatedText(nodes),
        inlineContent = emoteMap,
    )
}

/**
 * 动态摘要
 */
@Composable
internal fun DynamicModuleOpusSummaryBox(
    opusSummary: bilibili.app.dynamic.v2.ModuleOpusSummary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        opusSummary.title?.let {
            DynamicParagraphRender(it)
        }
        opusSummary.summary?.let {
            DynamicParagraphRender(it)
        }
    }
}