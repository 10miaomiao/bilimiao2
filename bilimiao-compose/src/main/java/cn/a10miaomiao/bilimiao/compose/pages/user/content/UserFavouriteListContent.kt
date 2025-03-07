package cn.a10miaomiao.bilimiao.compose.pages.user.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteFolderType
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteViewModel
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import org.kodein.di.compose.rememberInstance

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
internal fun UserFavouriteListContent(
    viewModel: UserFavouriteViewModel,
    showTowPane: Boolean,
    folderType: UserFavouriteFolderType,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val (listFlow, isRefreshingFlow) = remember(viewModel, folderType) {
        viewModel.getListAndIsRefreshingFlow(folderType)
    }
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()
    val isRefreshing by isRefreshingFlow.collectAsState()

    val openedMedia by viewModel.openedMedia.collectAsState()

    SwipeToRefresh(
        modifier = Modifier.fillMaxSize(),
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh(folderType) },
    ) {
        LazyColumn {
            val selectedMedia = openedMedia ?: list.firstOrNull()?.takeIf {
                folderType == UserFavouriteFolderType.Created
            }
            items(list, { it.id }) {
                val isSelected = if (showTowPane) {
                    selectedMedia?.id == it.id
                } else false
                MiaoCard(
                    modifier = Modifier.padding(5.dp),
                    onClick = {
                        viewModel.openMediaDetail(it)
                    },
                    enabled = !isSelected,
                ) {
                    Row(
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    viewModel.openMediaDetail(it)
                                },
                                enabled = !isSelected,
                            )
                            .padding(10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlideImage(
                            model = UrlUtil.autoHttps(it.cover) + "@672w_378h_1c_",
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = Modifier
                                .size(width = 120.dp, height = 80.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            loading = placeholder(R.drawable.bili_default_placeholder_img_tv),
                            failure = placeholder(R.drawable.bili_fail_placeholder_img_tv),
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .padding(horizontal = 10.dp),
                        ) {
                            Text(
                                text = it.title,
                                maxLines = 2,
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(
                                text = if (folderType == UserFavouriteFolderType.Created) {
                                    "${it.media_count}个视频 · ${if (it.privacy == 1) "私密" else "公开"}"
                                } else {
                                    "${it.media_count}个视频"
                                },
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.outline,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            item {
                ListStateBox(
                    modifier = Modifier.padding(
                        bottom = windowInsets.bottomDp.dp
                    ),
                    loading = listLoading,
                    finished = listFinished,
                    fail = listFail,
                    listData = list,
                ) {
                    viewModel.loadMore(folderType)
                }
            }
        }
    }
}