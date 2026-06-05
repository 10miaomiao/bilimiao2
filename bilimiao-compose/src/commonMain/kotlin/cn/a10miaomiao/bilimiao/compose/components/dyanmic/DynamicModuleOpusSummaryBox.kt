package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bilibili.app.dynamic.v2.TextNode
import bilibili.app.dynamic.v2.Paragraph;
import cn.a10miaomiao.bilimiao.compose.common.foundation.annotatedText
import cn.a10miaomiao.bilimiao.compose.common.foundation.AnnotatedTextNode
import cn.a10miaomiao.bilimiao.compose.common.foundation.inlineAnnotatedContent
import cn.a10miaomiao.bilimiao.compose.common.foundation.toAnnotatedTextNode

@Composable
private fun DynamicParagraphRender(p: Paragraph) {
    val nodes = p.text?.nodes?.toAnnotatedTextNode() ?: listOf()
    val emoteMap = inlineAnnotatedContent(nodes)
    Text(
        annotatedText(nodes),
        inlineContent = emoteMap,
        color = MaterialTheme.colorScheme.onSurface,
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