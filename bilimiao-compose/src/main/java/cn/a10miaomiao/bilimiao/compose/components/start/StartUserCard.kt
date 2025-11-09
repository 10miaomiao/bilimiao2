package cn.a10miaomiao.bilimiao.compose.components.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard
import cn.a10miaomiao.bilimiao.compose.pages.message.MessagePage
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.store.MessageStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.kodein.di.compose.rememberInstance


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun StartUserCard(
    modifier: Modifier = Modifier,
    userInfo: UserInfo? = null,
    onUserClick: () -> Unit = {},
    onUserDynamicClick: () -> Unit = {},
    onUserFollowingClick: () -> Unit = {},
    onUserFollowerClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
) {
    val imageHeight = 100.dp
    val userRowTop = imageHeight - 40.dp
    val userRowHeight = 60.dp
    MiaoOutlinedCard(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    onClick = onUserClick,
                )
                .fillMaxWidth(),
        ) {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .align(Alignment.TopCenter),
                painter = painterResource(R.drawable.top_bg1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .align(Alignment.TopCenter),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(userRowTop)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        )
                        .align(Alignment.BottomCenter),
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = userRowTop, bottom = 10.dp)
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (userInfo == null) {
                        Image(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            painter = painterResource(R.drawable.bili_akari_img),
                            contentDescription = "login"
                        )
                    } else {
                        GlideImage(
                            modifier = Modifier
                                .size(userRowHeight)
                                .clip(CircleShape),
                            model = userInfo.face,
                            contentDescription = userInfo.name,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .height(userRowHeight)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = userInfo?.name ?: "bilimiao",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (userInfo == null) {
                            Text(
                                text = "点这里可以登录喵！",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(20.dp),
                            ) {
                                Text(
                                    text = "硬币: ${userInfo.coin}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text ="B币: ${userInfo.bcoin}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                    StartUserMessageBox(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        onClick = onMessageClick,
                    )
                }
                if (userInfo != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StartUserNumberBox(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp)
                                .fillMaxHeight(),
                            number = userInfo.dynamic,
                            numberName = "动态",
                            onClick = onUserDynamicClick,
                        )
                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        StartUserNumberBox(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp)
                                .fillMaxHeight(),
                            number = userInfo.following,
                            numberName = "关注",
                            onClick = onUserFollowingClick,
                        )
                        VerticalDivider(
                            modifier = Modifier
                                .height(20.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        StartUserNumberBox(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 10.dp)
                                .fillMaxHeight(),
                            number = userInfo.follower,
                            numberName = "粉丝",
                            onClick = onUserFollowerClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartUserMessageBox(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val messageStore by rememberInstance<MessageStore>()
    val messageState by messageStore.stateFlow.collectAsState()
    Box(
        modifier = modifier,
    ) {
        val totalCount = messageState.totalCount()
        Image(
            painter = painterResource(id = R.drawable.ic_message),
            contentDescription = "message",
            modifier = Modifier
                .size(40.dp)
                .padding(end = 8.dp)
                .clickable(
                    onClick = onClick,
                )
        )
        if (totalCount > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    text = if (totalCount > 99) {
                        "99+"
                    } else {
                        totalCount.toString()
                    }
                )
            }

        }
    }
}

@Composable
private fun StartUserNumberBox(
    modifier: Modifier = Modifier,
    number: Int,
    numberName: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = NumberUtil.converString(number),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = numberName,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}