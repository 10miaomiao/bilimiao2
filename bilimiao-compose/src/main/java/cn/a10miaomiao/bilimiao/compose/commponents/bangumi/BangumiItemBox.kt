package cn.a10miaomiao.bilimiao.compose.commponents.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.skydoves.landscapist.glide.GlideImage

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BangumiItemBox(
    modifier: Modifier = Modifier,
    title: String? = null,
    cover: String? = null,
    statusText: String? = null,
    desc: String? = null,
    isChinaMade: Boolean = false,
    badgeText: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    moreMenu: List<Pair<Int, String>?> = listOf(),
    onMenuItemClick: ((Pair<Int, String>) -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(10.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = with(StringBuilder()) {
                    if (!title.isNullOrBlank()) {
                        append(title)
                    }
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
    ) {
        if (cover != null) {
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 125.dp)
                    .clip(RoundedCornerShape(5.dp)),
            ) {
                GlideImage(
                    imageModel = UrlUtil.autoHttps(cover) + "@560w_746h",
                    modifier = Modifier.fillMaxSize(),
                )
                if (!badgeText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .align(Alignment.TopEnd)
                            .padding(5.dp)
                            .background(
                                color = badgeColor,
                                shape = RoundedCornerShape(5.dp)
                            )
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                if (isChinaMade) {
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .align(Alignment.BottomStart)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(
                                    topEnd = 10.dp,
                                )
                            )
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                    ) {
                        Text(
                            text = "国产动漫",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .height(130.dp)
                .padding(start = 5.dp),
        ) {
            if (title != null) {
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
