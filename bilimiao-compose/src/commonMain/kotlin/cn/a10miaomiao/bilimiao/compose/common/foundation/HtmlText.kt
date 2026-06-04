package cn.a10miaomiao.bilimiao.compose.common.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger

@Stable
internal data class HtmlTextElement(
    val tag: String,
    val content: String,
)

internal class HtmlTextTagPos(
    val start: Int,
    val end: Int,
    val isClosed: Boolean,
) {
    fun getTagName(htmlText: String): String {
        val str = htmlText.substring(
            start + (if (isClosed) 2 else 1), end)
        val spacePos = str.indexOf(' ')
        if (spacePos == -1) {
            return str
        }
        return str.substring(0, spacePos)
    }
}

internal fun parseHtmlText(
    htmlText: String,
): List<HtmlTextElement> {
    val elementList = mutableListOf<HtmlTextElement>()
    var prevTag: HtmlTextTagPos? = null
    var prevPos = 0
    var i = 0
    while (i < htmlText.length) {
        val c = htmlText[i]
        if (c == '<') {
            val end = htmlText.indexOf('>', i)
            if (end != -1) {
                val isClosed = htmlText[i + 1] == '/'
                val tagPos = HtmlTextTagPos(i, end, isClosed)
                val tagName = tagPos.getTagName(htmlText)
                if (isClosed && prevTag != null
                    && tagName == prevTag.getTagName(htmlText, )) {
                    if (prevTag.start > prevPos) {
                        elementList.add(HtmlTextElement(
                            tag = "",
                            content = htmlText.substring(prevPos, prevTag.start)
                        ))
                    }
                    elementList.add(HtmlTextElement(
                        tag = tagName,
                        content = htmlText.substring(prevTag.end + 1, tagPos.start)
                    ))
                    prevPos = end + 1
                    i = end
                } else if (!isClosed) {
                    prevTag = tagPos
                    i = end
                }
            }
        }
        i++
    }
    if (i > prevPos) {
        elementList.add(HtmlTextElement(
            tag = "",
            content = htmlText.substring(prevPos, i)
        ))
    }
    return elementList
}

@Composable
fun htmlText(
    contentText: String,
) : AnnotatedString {
    val elementList = parseHtmlText(contentText)
    return buildAnnotatedString {
        elementList.forEach {
            when (it.tag) {
                "" -> {
                    append(it.content)
                }
                "em" -> {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(it.content)
                    }
                }
            }
        }
    }
}