package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VideoUpperBox(
    author: bilibili.app.archive.v1.Author?,
    ownerExt: bilibili.app.view.v1.OnwerExt?,
    staffList: List<bilibili.app.view.v1.Staff>,
    onUserClick: (String) -> Unit,
) {
    if (staffList.isNotEmpty()) {
        LazyRow {
            items(staffList) { staff ->
                Column(
                    modifier = Modifier
                        .clickable {
                            onUserClick(staff.mid.toString())
                        }
                        .width(100.dp)
                        .padding(
                            vertical = 10.dp,
                            horizontal = 5.dp,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    GlideImage(
                        modifier = Modifier.size(40.dp)
                            .clip(CircleShape),
                        model = UrlUtil.autoHttps(staff.face) + "@200w_200h",
                        contentDescription = null,
                    )
                    Text(
                        text = staff.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = staff.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    } else if (author != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onUserClick(author.mid.toString())
                }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlideImage(
                modifier = Modifier.size(40.dp)
                    .clip(CircleShape),
                model = UrlUtil.autoHttps(author.face) + "@200w_200h",
                contentDescription = null,
            )
            Column(
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Text(
                    text = author.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (ownerExt != null) {
//                    Row {
                    Text(
                        "${NumberUtil.converString(ownerExt.fans)}粉丝",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
//                        Spacer(Modifier.width(10.dp))
//                        Text(
//                            "${NumberUtil.converString(ownerExt.arcCount)}投稿",
//                            style = MaterialTheme.typography.labelMedium,
//                        )
//                    }
                }
            }
        }
    }
}

