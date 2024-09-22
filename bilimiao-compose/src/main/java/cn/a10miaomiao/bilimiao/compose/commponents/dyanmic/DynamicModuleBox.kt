package cn.a10miaomiao.bilimiao.compose.commponents.dyanmic

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import bilibili.app.dynamic.v2.Module.ModuleItem

@Composable
fun DynamicModuleBox(
    module: bilibili.app.dynamic.v2.Module,
) {
    when(val moduleItem = module.moduleItem) {
        is ModuleItem.ModuleAuthor -> {
            DynamicModuleAuthorBox(moduleItem.value)
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
        else -> {
            if (moduleItem != null) {
                Text(text = "模块:" + moduleItem::class.simpleName)
            }
        }
    }
}