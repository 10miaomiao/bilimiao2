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
                // show_text 按 WordNode 正确解析后，words 即链接显示文字。
                // （旧 proto 把 show_text 错配成 string，拿到的是嵌套消息的二进制乱码，
                // 需要手动剥离控制字符；修正后这里不再有乱码，直接取 words）
                val link = node.value
                AnnotatedTextNode.Link(link.showText?.words ?: "", link.link, false)
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
