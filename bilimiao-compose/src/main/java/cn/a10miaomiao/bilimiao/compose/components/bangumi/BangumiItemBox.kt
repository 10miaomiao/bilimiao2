package cn.a10miaomiao.bilimiao.compose.components.bangumi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.foundation.htmlText
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BangumiItemBox(
    modifier: Modifier = Modifier,
    title: String,
    cover: String,
    statusText: String? = null,
    desc: String? = null,
    coverBadge1: @Composable () -> Unit = {},
    coverBadge2: @Composable () -> Unit = {},
    isHtml: Boolean = false,
    moreMenu: List<Pair<Int, String>?> = listOf(),
    onMenuItemClick: ((Pair<Int, String>) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .run {
                if (onClick == null) this
                else clickable(onClick = onClick)
            }
            .semantics(mergeDescendants = true) {
                contentDescription = with(StringBuilder()) {
                    append(title)
                    if (!statusText.isNullOrBlank()) {
                        append(",")
                        append(statusText)
                    }
                    if (!desc.isNullOrBlank()) {
                        append(",")
                        append(desc)
                    }
                }.toString()
            }
            .then(modifier)
    ) {
        Box(
            modifier = Modifier
                .size(width = 90.dp, height = 125.dp)
                .clip(RoundedCornerShape(5.dp)),
        ) {
            GlideImage(
                model = UrlUtil.autoHttps(cover) + "@560w_746h",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                coverBadge1()
            }
            Box(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                coverBadge2()
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .height(130.dp)
                .padding(start = 5.dp),
        ) {
            if (isHtml) {
                Text(
                    text = htmlText(title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
            }
            if (statusText != null) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 5.dp),
                )
            }
            if (desc != null) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    modifier = Modifier.padding(),
                )
            }
            if (moreMenu.isNotEmpty()) {
                val expandedMenu = remember {
                    mutableStateOf(false)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        IconButton(
                            onClick = {
                                expandedMenu.value = true
                            }
                        ) {
                            Icon(
                                tint = MaterialTheme.colorScheme.onBackground,
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = null,
                            )
                        }
                        DropdownMenu(
                            expanded = expandedMenu.value,
                            onDismissRequest = { expandedMenu.value = false },
                        ) {
                            moreMenu.filterNotNull().forEach { menu ->
                                DropdownMenuItem(
                                    onClick = {
                                        expandedMenu.value = false
                                        onMenuItemClick?.invoke(menu)
                                    },
                                    text = {
                                        Text(text = menu.second)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
