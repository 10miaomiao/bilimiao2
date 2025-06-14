package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.localPageNavigation
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

/**
 * 动态作者
 */
@Composable
fun DynamicModuleAuthorBox(
    author: bilibili.app.dynamic.v2.ModuleAuthor,
    isJumpToUser: Boolean = true,
) {
    val authorData = author.author ?: return
    val pageNavigation = localPageNavigation()
    fun jumpToUser() {
        if (isJumpToUser) {
            pageNavigation.navigate(
                UserSpacePage(
                    id = authorData.mid.toString(),
                )
            )
        }
    }
    DynamicModuleAuthorBox(
        name = authorData.name,
        face = authorData.face,
        labelText = author.ptimeLabelText,
        locationText = author.ptimeLocationText,
        onClick = ::jumpToUser,
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DynamicModuleAuthorBox(
    name: String,
    face: String,
    labelText: String,
    locationText: String,
    onClick: (() -> Unit)? = null,
) {

    Row(
        modifier = Modifier
            .run {
                if (onClick == null) this
                else clickable(onClick = onClick)
            }
            .fillMaxWidth()
            .padding(10.dp)

    ) {
        GlideImage(
            model = UrlUtil.autoHttps(face) + "@200w_200h",
            contentDescription = null,
            modifier = Modifier
                .size(40.dp, 40.dp)
                .clip(CircleShape),
        )
        Column(
            modifier = Modifier.padding(start = 5.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

        }
    }
}