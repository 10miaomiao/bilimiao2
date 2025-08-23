package cn.a10miaomiao.bilimiao.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.foundation.ScaleIndication
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.pages.auth.LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.HistoryPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.WatchLaterPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.SettingPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouritePage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import cn.a10miaomiao.bilimiao.compose.pages.web.WebPage
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.compose.rememberInstance

@Composable
fun StartViewContent(
    modifier: Modifier = Modifier,
    startTopHeight: Dp = 200.dp,
    navigateTo: (ComposePage) -> Unit,
    openSearch: () -> Unit,
) {
    val windowStore by rememberInstance<WindowStore>()
    val windowState by windowStore.stateFlow.collectAsState()
    val windowInsets = windowState.windowInsets
    val userStore by rememberInstance<UserStore>()
    val userState by userStore.stateFlow.collectAsState()

    LazyColumn(
        modifier = modifier,
        contentPadding = windowInsets.addPaddingValues(
            addLeft = 10.dp,
            addRight = 10.dp,
            addTop = 10.dp,
            addBottom = 10.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .padding(top = startTopHeight)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .size(80.dp, 8.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            color = Color.White.copy(alpha = 0.8f)
                        ),
                )
            }
        }
        item {
            StartUserCard(
                userInfo = userState.info,
                onUserClick = {
                    val userInfo = userState.info
                    if (userInfo != null) {
                        navigateTo(UserSpacePage(
                            id = userInfo.mid.toString(),
                        ))
                    } else {
                        navigateTo(LoginPage())
                    }
                },
                onUserDynamicClick = {
                    val userInfo = userState.info
                    if (userInfo != null) {
                        navigateTo(UserSpacePage(
                            id = userInfo.mid.toString(),
                        ))
                    }
                },
                onUserFollowerClick = {
                    navigateTo(WebPage(
                        url = "https://space.bilibili.com/h5/follow?type=fans",
                    ))
                },
                onUserFollowingClick = {
                    navigateTo(MyFollowPage())
                },
            )
        }
        item {
            StartSearchCard(
                onClick = openSearch
            )
        }
        item {
            StartLibraryCard(
                onFavouriteClick = {
                    val userInfo = userState.info
                    if (userInfo != null) {
                        navigateTo(UserFavouritePage(userInfo.mid.toString()))
                    } else {
                        PopTip.show("请先登录喵")
                    }
                },
                onBangumiClick = {
                    val userInfo = userState.info
                    if (userInfo != null) {
                        navigateTo(MyBangumiPage())
                    } else {
                        PopTip.show("请先登录喵")
                    }
                },
                onHistoryClick = {
                    navigateTo(HistoryPage())
                },
                onWatchlaterClick = {
                    val userInfo = userState.info
                    if (userInfo != null) {
                        navigateTo(WatchLaterPage())
                    } else {
                        PopTip.show("请先登录喵")
                    }
                }
            )
        }
        item {
            StartFooterCard(
                onDownloadClick = {
                    navigateTo(DownloadListPage())
                },
                onSettingClick = {
                    navigateTo(SettingPage())
                },
            )
        }
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun StartUserCard(
    modifier: Modifier = Modifier,
    userInfo: UserInfo? = null,
    onUserClick: () -> Unit = {},
    onUserDynamicClick: () -> Unit = {},
    onUserFollowingClick: () -> Unit = {},
    onUserFollowerClick: () -> Unit = {},
) {
    val imageHeight = 100.dp
    val userRowTop = imageHeight - 40.dp
    val userRowHeight = 60.dp
    MiaoCard(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
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
                        .fillMaxWidth()
                        .clickable(
                            onClick = onUserClick,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ScaleIndication,
                        ),
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
                        Text(
                            text = userInfo?.let { "Lv${userInfo.level}  硬币${userInfo.coin}  B币${userInfo.bcoin}" } ?: "点这里可以登录喵！",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
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
private fun StartUserNumberBox(
    modifier: Modifier = Modifier,
    number: Int,
    numberName: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick),
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

@Composable
private fun StartSearchCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    MiaoCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp),
                imageVector = Icons.Filled.Search,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "请输入ID或关键字",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp),
                imageVector = Icons.Filled.CameraAlt,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun LibraryItemCard(
    cardWidth: Dp,
    cardName: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    MiaoCard(
        modifier = Modifier
            .width(cardWidth),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Image(
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 8.dp),
                painter = painterResource(iconResId),
                contentDescription = null,
            )
            Text(
                text = cardName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}
@Composable
private fun StartLibraryCard(
    onFavouriteClick: () -> Unit,
    onBangumiClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onWatchlaterClick: () -> Unit,
) {
    BoxWithConstraints {
        val columnCount = if (maxWidth < 300.dp) 1 else 2
        val cardWidth = (maxWidth - (8.dp * (columnCount - 1))) / columnCount
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LibraryItemCard(
                cardWidth,
                "收藏",
                R.drawable.ic_nav_fav,
                onClick = onFavouriteClick,
            ) {
                Text(
                    text = "默认收藏夹(5)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
//                            viewModel.toSearchPage(tag.name)
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
                Text(
                    text = "我的订阅(10)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
//                            viewModel.toSearchPage(tag.name)
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }

            LibraryItemCard(
                cardWidth,
                "追番/剧",
                R.drawable.ic_nav_bangumi,
                onClick = onBangumiClick,
            ) {
                Text(
                    text = "我的追番(5)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
//                            viewModel.toSearchPage(tag.name)
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
                Text(
                    text = "我的追剧(10)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
//                            viewModel.toSearchPage(tag.name)
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }

            LibraryItemCard(
                cardWidth,
                "历史",
                R.drawable.ic_nav_history,
                onClick = onHistoryClick,
            ) {
                Text(
                    text = "个视频",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
//                            viewModel.toSearchPage(tag.name)
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }

            LibraryItemCard(
                cardWidth,
                "稍后看",
                R.drawable.ic_nav_watchlater,
                onClick = onWatchlaterClick,
            ) {
                Text(
                    text = "请登录后使用",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
//                            viewModel.toSearchPage(tag.name)
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StartFooterCard(
    onDownloadClick: () -> Unit,
    onSettingClick: () -> Unit
) {
    MiaoCard {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onDownloadClick)
                    .padding(vertical = 10.dp),
                text = "视频下载",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            VerticalDivider(
                modifier = Modifier
                    .height(20.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onSettingClick)
                    .padding(vertical = 10.dp),
                text = "设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

