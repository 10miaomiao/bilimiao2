package cn.a10miaomiao.bilimiao.compose.pages.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.components.user.UserLevelIcon
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AuthorItemBox(
    modifier: Modifier = Modifier,
    name: String,
    face: String,
    sign: String,
    fans: Int,
    archives: Int,
    level: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlideImage(
            model = UrlUtil.autoHttps(face) + "@200w_200h",
            loading = placeholder(R.drawable.bili_akari_img),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 50.dp)
                .padding(start = 5.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                )
                UserLevelIcon(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .size(20.dp, 15.dp),
                    level = level,
                )
            }
            Row {
                Text(
                    text = NumberUtil.converString(fans) + "粉丝",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = NumberUtil.converString(archives) + "个视频",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Text(
                text = sign,
                maxLines = 1,
                color = MaterialTheme.colorScheme.outline,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}