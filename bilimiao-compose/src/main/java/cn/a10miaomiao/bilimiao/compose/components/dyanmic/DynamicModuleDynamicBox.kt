package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bilibili.app.dynamic.v2.ModuleDynamic
import cn.a10miaomiao.bilimiao.compose.components.image.ImagesGrid
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import kotlin.math.min

@Composable
private fun DynArchiveBox(
    dynPgc: bilibili.app.dynamic.v2.MdlDynArchive
) {
    VideoItemBox(
        modifier = Modifier.padding(
            horizontal = 10.dp,
            vertical = 5.dp
        ),
        title = dynPgc.title,
        pic = dynPgc.cover,
        duration = dynPgc.coverLeftText1,
        remark = dynPgc.coverLeftText2 + "  " + dynPgc.coverLeftText3,
        onClick = {

        }
    )
}

@Composable
fun DynDrawBox(
    dynDraw: bilibili.app.dynamic.v2.MdlDynDraw
) {
    Box(modifier = Modifier.padding(
        horizontal = 10.dp,
        vertical = 5.dp
    )) {
        ImagesGrid(dynDraw.items.map {
            val w = min(600, it.width)
            val h = w * it.width / it.height
            "${it.src}@${w}w_${h}h"
        })
    }
}

@Composable
fun DynamicModuleDynamicBox(
    dynamic: ModuleDynamic
) {
    val moduleItem = dynamic.moduleItem ?: return
    when (moduleItem) {
        is ModuleDynamic.ModuleItem.DynArchive -> {
            DynArchiveBox(moduleItem.value)
        }
        is ModuleDynamic.ModuleItem.DynDraw -> {
            DynDrawBox(moduleItem.value)
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