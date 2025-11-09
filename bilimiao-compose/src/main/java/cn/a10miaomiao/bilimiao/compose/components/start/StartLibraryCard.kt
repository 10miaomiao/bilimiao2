package cn.a10miaomiao.bilimiao.compose.components.start

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard
import cn.a10miaomiao.bilimiao.compose.pages.mine.HistoryPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyBangumiPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.WatchLaterPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouritePage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import com.a10miaomiao.bilimiao.comm.store.UserLibraryStore
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.compose.rememberInstance


@Composable
fun StartLibraryCard(
    userId: Long?,
    navigateTo: (ComposePage) -> Unit,
) {
    val userLibraryStore by rememberInstance<UserLibraryStore>()
    val userLibraryState by userLibraryStore.stateFlow.collectAsState()
    val isLogin = userId != null
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
                onClick = {
                    if (userId != null) {
                        navigateTo(UserFavouritePage(userId.toString()))
                    } else {
                        PopTip.show("请先登录喵")
                    }
                },
            ) {
                if (isLogin) {
                    val favourite = userLibraryState.favourite
                    Text(
                        text = favourite.defaultFavTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (favourite.defaultFavId.isNotEmpty()) {
                                    navigateTo(
                                        UserFavouriteDetailPage(
                                            id = favourite.defaultFavId,
                                            title = favourite.defaultFavTitle,
                                        )
                                    )
                                } else {
                                    PopTip.show("无默认收藏夹信息")
                                }
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                    Text(
                        text = "我的订阅",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                if (userId != null) {
                                    navigateTo(UserFavouritePage(
                                        mid = userId.toString(),
                                        type = "collected"
                                    ))
                                } else {
                                    PopTip.show("请先登录喵")
                                }
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                } else {
                    Text(
                        text = "未登录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            LibraryItemCard(
                cardWidth,
                "追番/剧",
                R.drawable.ic_nav_bangumi,
                onClick = {
                    if (userId != null) {
                        navigateTo(MyBangumiPage())
                    } else {
                        PopTip.show("请先登录喵")
                    }
                },
            ) {
                if (isLogin) {
                    Text(
                        text = "我的追番",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                navigateTo(MyBangumiPage(
                                   type = "bangumi",
                                ))
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                    Text(
                        text = "我的追剧",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                navigateTo(MyBangumiPage(
                                    type = "cinema",
                                ))
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                } else {
                    Text(
                        text = "未登录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            LibraryItemCard(
                cardWidth,
                "历史",
                R.drawable.ic_nav_history,
                onClick = {
                    navigateTo(HistoryPage())
                },
            ) {
                userLibraryState.history.forEach {
                    Text(
                        text = it.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                navigateTo(VideoDetailPage(
                                    id = it.aid.toString(),
                                ))
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
                if (userLibraryState.history.isEmpty()) {
                    Text(
                        text = "无历史记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    // 占位
                    Text(
                        modifier = Modifier
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        text = "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            LibraryItemCard(
                cardWidth,
                "稍后看",
                R.drawable.ic_nav_watchlater,
                onClick = {
                    if (userId != null) {
                        navigateTo(WatchLaterPage())
                    } else {
                        PopTip.show("请先登录喵")
                    }
                },
            ) {
                if (isLogin) {
                    userLibraryState.watchLater.forEach {
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    navigateTo(VideoDetailPage(
                                        id = it.aid.toString(),
                                    ))
                                }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                    if (userLibraryState.watchLater.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            text = "无稍后再看",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        // 占位
                        Text(
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            text = "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                } else {
                    Text(
                        text = "未登录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
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
    MiaoOutlinedCard(
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

