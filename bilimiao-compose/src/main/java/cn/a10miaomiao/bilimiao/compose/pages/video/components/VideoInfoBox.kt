package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Danmukunum
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Playnum
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailViewModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil

@Composable
private fun String.toLinkUrl(): LinkAnnotation.Url {
    val url = if (startsWith("http")) {
        this
    } else if (startsWith("av") || startsWith("AV")){
        "bilimiao://video/${substring(2, length)}"
    } else if (startsWith("BV")){
        "bilimiao://video/$this"
    } else if (startsWith("sm") || startsWith("SM")){
        "https://www.nicovideo.jp/watch/$this"
    } else if (startsWith("ac") || startsWith("AC")){
        "https://www.acfun.cn/v/${this}"
    } else if (startsWith("cv") || startsWith("CV")){
        "https://www.bilibili.com/read/${this}"
    } else {
        "http://$this"
    }
    return LinkAnnotation.Url(
        url,
        TextLinkStyles(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary
            )
        )
    )
}

@Composable
private fun parseText(
    text: String
): AnnotatedString {
    val regex = Regex(
        """(?i)""" +  // 忽略大小写
                """(\b(https?://|www\.)[\w-]+(\.[\w-]+)+([/\S]*)*\b)|""" +  // URL（优先匹配）
                """(\b(av\d{1,15})\b)|""" +     // B站av号（1-15位数字）
                """(\b(BV[\dA-Za-z]{10})\b)|""" + // B站BV号（固定10位）
                """(\b(ac\d{1,10})\b)|""" +     // A站ac号（1-10位数字）
                """(\b(sm\d{1,10})\b)|""" +     // Niconico sm号（1-10位数字）
                """(\b(cv\d{1,8})\b)"""         // B站专栏cv号（1-8位数字）
    )
    return buildAnnotatedString {
        append(text) // 添加原始文本
        // 为每个匹配的URL添加样式
        regex.findAll(text).forEach { result ->
            addLink(
                result.value.toLinkUrl(),
                start = result.range.first,
                end = result.range.last + 1,
            )
        }
    }
}

@Composable
fun VideoInfoBox(
    viewModel: VideoDetailViewModel,
    arc: bilibili.app.archive.v1.Arc,
    stat: bilibili.app.archive.v1.Stat?,
    pages: List< bilibili.app.archive.v1.Page>,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ){
        SelectionContainer {
            Text(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .animateContentSize(),
                text = arc.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Row(
            modifier = Modifier.padding(
                vertical = 5.dp,
                horizontal = 10.dp,
            ),
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onBackground,
                imageVector = BilimiaoIcons.Common.Playnum,
                contentDescription = "播放量"
            )
            Text(
                modifier = Modifier.padding(start = 2.dp),
                text = NumberUtil.converString(stat?.view ?: 0),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onBackground,
                imageVector = BilimiaoIcons.Common.Danmukunum,
                contentDescription = "弹幕数"
            )
            Text(
                modifier = Modifier.padding(start = 2.dp),
                text =  NumberUtil.converString(stat?.danmaku ?: 0),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = NumberUtil.converCTime(arc.pubdate),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (pages.size > 1) {
            VideoPagesBox(
                pages = pages,
                onPageClick = viewModel::playVideo,
                onMoreClick = viewModel::openVideoPages,
            )
            HorizontalDivider(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp,
                ),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        if (arc.desc.isNotBlank()) {
            SelectionContainer {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .animateContentSize(),
                    text = parseText(arc.desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { layoutResult ->
                        // 检测文本是否溢出
                        hasOverflow = layoutResult.didOverflowHeight
                    }
                )
            }
        }
        if (hasOverflow || isExpanded) {
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { isExpanded = !isExpanded },
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Text(
                    text = if (isExpanded) "收起" else "展开",
                )
            }
        }
    }
}