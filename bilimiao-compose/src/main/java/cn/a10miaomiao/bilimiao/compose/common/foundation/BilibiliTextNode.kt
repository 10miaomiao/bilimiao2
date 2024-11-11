package cn.a10miaomiao.bilimiao.compose.common.foundation

import androidx.compose.runtime.Composable
import bilibili.app.dynamic.v2.TextNode

@Composable
fun List<TextNode>.toAnnotatedTextNode(): List<AnnotatedTextNode> {
    return mapNotNull {
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
    }
}
