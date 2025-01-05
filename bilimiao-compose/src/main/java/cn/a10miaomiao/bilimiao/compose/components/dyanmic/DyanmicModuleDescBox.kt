package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bilibili.app.dynamic.v2.DescType
import cn.a10miaomiao.bilimiao.compose.common.foundation.annotatedText
import cn.a10miaomiao.bilimiao.compose.common.foundation.AnnotatedTextNode
import cn.a10miaomiao.bilimiao.compose.common.foundation.inlineAnnotatedContent

@Composable
fun bilibili.app.dynamic.v2.ModuleDesc.toAnnotatedTextNode(): List<AnnotatedTextNode> {
    return desc.map {
        // 9: emoji
        if (it.type.value == DescType.EMOJI.value) {
            AnnotatedTextNode.Emote(it.text, it.uri)
        } else {
            AnnotatedTextNode.Text(it.text)
        }
    }
}

@Composable
fun DyanmicModuleDescBox(
    desc: bilibili.app.dynamic.v2.ModuleDesc
) {
    val nodes = desc.toAnnotatedTextNode()
    Text(
        text = annotatedText(nodes),
        inlineContent = inlineAnnotatedContent(nodes),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp))
}