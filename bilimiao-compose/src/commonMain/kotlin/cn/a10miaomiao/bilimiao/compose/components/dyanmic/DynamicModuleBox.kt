package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import bilibili.app.dynamic.v2.Module.ModuleItem
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger

@Composable
fun DynamicModuleBox(
    module: bilibili.app.dynamic.v2.Module,
    isJumpToUser: Boolean = true,
) {
    val moduleItem = module.moduleItem ?: return
    when(moduleItem) {
        is ModuleItem.ModuleTop -> Unit
        is ModuleItem.ModuleButtom -> Unit
        is ModuleItem.ModuleAuthor -> {
            DynamicModuleAuthorBox(moduleItem.value, isJumpToUser)
        }
        is ModuleItem.ModuleOpusSummary -> {
            DynamicModuleOpusSummaryBox(moduleItem.value)
        }
        is ModuleItem.ModuleDesc -> {
            DyanmicModuleDescBox(moduleItem.value)
        }
        is ModuleItem.ModuleStat -> {
            DynamicModuleStatBox(moduleItem.value)
        }
        is ModuleItem.ModuleDynamic -> {
            DynamicModuleDynamicBox(moduleItem.value)
        }
        is ModuleItem.ModuleParagraph -> {
            DynamicModuleParagraphBox(moduleItem.value)
        }
        is ModuleItem.ModuleAuthorForward -> {
            ModuleAuthorForwardBox(moduleItem.value)
        }
        is ModuleItem.ModuleStatForward -> {
        }
        else -> {
            Box(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 5.dp
                )
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(5.dp)
            ) {
                Text(
                    "暂不支持的模块: ${moduleItem::class.simpleName}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}