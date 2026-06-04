package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import bilibili.app.dynamic.v2.ModuleDynamic
import cn.a10miaomiao.bilimiao.compose.common.localPageNavigation
import cn.a10miaomiao.bilimiao.compose.components.image.ImagesGrid
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import kotlin.math.min

@Composable
private fun DynArchiveBox(
    dynPgc: bilibili.app.dynamic.v2.MdlDynArchive
) {
    val pageNavigation = localPageNavigation()

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
            pageNavigation.navigate(VideoDetailPage(
                id = dynPgc.bvid,
            ))
        }
    )
}

@Composable
fun DynDrawBox(
    dynDraw: bilibili.app.dynamic.v2.MdlDynDraw
) {
    val items = dynDraw.items
    val imageModels = remember(items) {
        items.map {
            val w = min(600, it.width)
            val h = w * it.width / it.height
            val url = UrlUtil.autoHttps(it.src)
            PreviewImageModel(
                previewUrl = url + "@${w}w_${h}h",
                originalUrl = url,
                height = it.height.toFloat(),
                width = it.width.toFloat(),
            )

        }
    }
    Box(modifier = Modifier.padding(
        horizontal = 10.dp,
        vertical = 5.dp
    )) {
        ImagesGrid(imageModels)
    }
}

@Composable
fun DynForwardBox(
    dynForward: bilibili.app.dynamic.v2.MdlDynForward
) {
    val modules = dynForward.item?.modules ?: return
    val uriHandler = LocalUriHandler.current
    Column (
        modifier = Modifier
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
            .clip(RoundedCornerShape(5.dp))
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
            )
            .clickable {
                dynForward.item?.extend?.let {
                    uriHandler.openUri(it.cardUrl)
                }
            }
    ) {
        modules.forEach { moduleItem ->
            DynamicModuleBox(moduleItem)
        }
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
        is ModuleDynamic.ModuleItem.DynForward -> {
            DynForwardBox(moduleItem.value)
        }
        else -> {
            Box(
                modifier = Modifier
                    .padding(
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