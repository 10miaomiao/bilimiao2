package cn.a10miaomiao.bilimiao.compose.commponents.dyanmic

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bilibili.app.dynamic.v2.ModuleDynamic
import cn.a10miaomiao.bilimiao.compose.commponents.video.VideoItemBox

@Composable
private fun DynArchiveBox(
    dynPgc: bilibili.app.dynamic.v2.MdlDynArchive
) {
    VideoItemBox(
        title = dynPgc.title,
        pic = dynPgc.cover,
        duration = dynPgc.coverLeftText1,
        remark = dynPgc.coverLeftText2 + "  " + dynPgc.coverLeftText3,
        onClick = {

        }
    )
}

@Composable
fun DynamicModuleDynamicBox(
    dynamic: ModuleDynamic
) {
    val moduleItem = dynamic.moduleItem
    when (moduleItem) {
        is ModuleDynamic.ModuleItem.DynArchive -> {
            DynArchiveBox(moduleItem.value)
        }
        else -> {
            Text("模块:" + moduleItem!!::class.simpleName)
        }
    }
}