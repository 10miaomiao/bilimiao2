package cn.a10miaomiao.bilimiao.compose.pages.user.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.common.emitter.EmitterAction
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localEmitter
import cn.a10miaomiao.bilimiao.compose.components.bangumi.MiniBangumiItemBox
import cn.a10miaomiao.bilimiao.compose.components.favourite.MiniFavouriteItemBox
import cn.a10miaomiao.bilimiao.compose.components.video.MiniVideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpaceViewModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Composable
private fun IndexTitle(
    title: String,
    num: String = "",
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (num.isNotBlank()) {
            Text(
                text = num,
                modifier = Modifier.padding(start = 5.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (onClick != null) {
            TextButton(
                onClick = onClick,
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                    vertical = 0.dp,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "查看更多")
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(start = 2.dp),
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}


@Composable
fun UserSpaceIndexContent(
    viewModel: UserSpaceViewModel
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val detailData = viewModel.detailData.collectAsState().value ?: return Box {}

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val emitter = localEmitter()
    LaunchedEffect(Unit) {
        emitter.collectAction<EmitterAction.DoubleClickTab> {
            if (it.tab == PageTabIds.UserIndex) {
                if (listState.firstVisibleItemIndex == 0) {
//                    viewModel.refresh()
                } else {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = windowInsets.addPaddingValues(
            addTop = -windowInsets.topDp.dp,
            addBottom = 40.dp,
        )
    ) {
        val archiveData = detailData.archive
        item {
            IndexTitle(
                title = "视频",
                num = archiveData.count.toString(),
                onClick = {
                    scope.launch {
                        viewModel.changeTab(2)
                    }
                }
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(archiveData.item, { it.param }) {
                    Box(
                        modifier = Modifier.width(180.dp)
                    ) {
                        MiniVideoItemBox(
                            title = it.title,
                            pic = it.cover,
                            playNum = it.play,
                            damukuNum = it.danmaku,
                            duration = NumberUtil.converDuration(it.duration),
                            remark = NumberUtil.converCTime(it.ctime),
                            onClick = {
                                viewModel.toVideoDetail(it)
                            }
                        )
                    }
                }
            }
        }

        val favouriteData = detailData.favourite2
        if (favouriteData.count != 0) {
            item {
                IndexTitle(
                    title = "收藏",
                    num = favouriteData.count.toString(),
                    onClick = viewModel::toFavouriteList,
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favouriteData.item, { it.id }) {
                        MiniFavouriteItemBox(
                            modifier = Modifier.width(120.dp),
                            title = it.title,
                            cover = it.cover,
                            count = it.count.toString(),
                            isPublic = it.is_public == 0,
                            onClick = {
                                viewModel.toFavouriteDetail(it)
                            }
                        )
                    }
                    if (favouriteData.item.size < favouriteData.count) {
                        item {
                            Column(
                                modifier = Modifier
                                    .size(120.dp, 120.dp)
                                    .scale(0.85f)
                                    .background(
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(5.dp)
                                    )
                                    .clickable(onClick = viewModel::toFavouriteList),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = "查看更多",
                                    color = Color.White,
                                )
                                Icon(
                                    modifier = Modifier.padding(top = 10.dp)
                                        .size(24.dp),
                                    imageVector = Icons.Default.ArrowForward,
                                    tint = Color.White,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }


        val seasonData = detailData.season
        if (seasonData.count != 0) {
            item {
                IndexTitle(
                    title = "追番",
                    num = seasonData.count.toString(),
                    onClick = {
                        viewModel.toBangumiFollow()
                    }
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(seasonData.item, { it.param }) {
                        MiniBangumiItemBox(
                            modifier = Modifier.width(120.dp),
                            title = it.title,
                            cover = it.cover,
                            desc = if (it.finish == 1) {
                                "全${it.total_count}话"
                            } else if (it.newest_ep_index == "-1"){
                                "即将开播"
                            } else {
                                "更新至${it.newest_ep_index}话"
                            },
                            onClick = {
                                viewModel.toBangumiDetail(it)
                            }
                        )
                    }
                }
            }
        }


        val coinArchiveData = detailData.coin_archive
        if (coinArchiveData.count != 0) {
            item {
                IndexTitle(
                    title = "最近投币",
                    num = coinArchiveData.count.toString(),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(coinArchiveData.item, { it.param }) {
                        Box(
                            modifier = Modifier.width(180.dp)
                        ) {
                            MiniVideoItemBox(
                                title = it.title,
                                pic = it.cover,
                                playNum = it.play,
                                damukuNum = it.danmaku,
                                duration = NumberUtil.converDuration(it.duration),
                                upperName = it.author,
                                onClick = {
                                    viewModel.toVideoDetail(it)
                                }
                            )
                        }
                    }
                }
            }
        }

        val likeArchiveData = detailData.like_archive
        if (likeArchiveData.count != 0) {
            item {
                IndexTitle(
                    title = "最近点赞",
                    num = likeArchiveData.count.toString(),
                    onClick = {
                        viewModel.toLikeArchive()
                    }
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(likeArchiveData.item, { it.param }) {
                        Box(
                            modifier = Modifier.width(180.dp)
                        ) {
                            MiniVideoItemBox(
                                title = it.title,
                                pic = it.cover,
                                playNum = it.play,
                                damukuNum = it.danmaku,
                                duration = NumberUtil.converDuration(it.duration),
                                upperName = it.author,
                                onClick = {
                                    viewModel.toVideoDetail(it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

}