package cn.a10miaomiao.bilimiao.compose.pages.community.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmoteInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePackageInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePackagesInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePanelInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun EmojiGridBox(
    modifier: Modifier = Modifier,
    onInputEmoji: (UserEmoteInfo) -> Unit,
) {
    val loading = remember {
        mutableStateOf(true)
    }
    val failMessage = remember {
        mutableStateOf("")
    }
    val packageList = remember {
        mutableStateListOf<UserEmotePackageInfo>()
    }
    val packageListState = rememberLazyListState()
    val pagerState = rememberPagerState { packageList.size }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pagerState.currentPage) {
        scope.launch {
            packageListState.animateScrollToItem(pagerState.currentPage)
        }
    }
    LaunchedEffect(Unit) {
        try {
            loading.value = true
            val res = BiliApiService.commentApi
                .emoteList()
                .awaitCall()
                .json<ResponseData<UserEmotePanelInfo>>()
            if (res.isSuccess) {
                val result = res.requireData()
                packageList.clear()
                packageList.addAll(result.packages)
            }
        } catch (e: Exception) {
            failMessage.value = e.message ?: e.toString()
        } finally {
            loading.value = false
        }
    }
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (loading.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (failMessage.value.isNotEmpty()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = failMessage.value,
                color = MaterialTheme.colorScheme.onBackground,
            )
        } else {
            Column {
                LazyRow(
                    state = packageListState,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    items(packageList.size) {
                        val item = packageList[it]
                        FilterChip(
                            selected = it == pagerState.currentPage,
                            onClick = {
                                scope.launch {
                                    pagerState.scrollToPage(it)
                                }
                            },
                            leadingIcon = {
                                GlideImage(
                                    modifier = Modifier.size(24.dp),
                                    model = UrlUtil.autoHttps(item.url),
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(item.text)
                            }
                        )
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                ) {
                    val item = packageList[it]
                    EmojiGrid(
                        item.id,
                        item.type,
                        onInputEmoji = onInputEmoji,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun EmojiGrid(
    emoteId: Int,
    emoteType: Int,
    onInputEmoji: (UserEmoteInfo) -> Unit,
) {
    val loading = remember {
        mutableStateOf(true)
    }
    val failMessage = remember {
        mutableStateOf("")
    }
    val listItems = remember {
        mutableStateListOf<UserEmoteInfo>()
    }
    LaunchedEffect(emoteId) {
        try {
            loading.value = true
            val res = BiliApiService.commentApi
                .emoteList(emoteId.toString())
                .awaitCall()
                .json<ResponseData<UserEmotePackagesInfo>>()
            if (res.isSuccess) {
                val result = res.requireData()
                val emoteList = result
                    .packages
                    .find { it.id == emoteId }
                    ?.emote
                if(emoteList != null) {
                    listItems.clear()
                    listItems.addAll(emoteList)
                }
            }
        } catch (e: Exception) {
            failMessage.value = e.message ?: e.toString()
        } finally {
            loading.value = false
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (loading.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (failMessage.value.isNotEmpty()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = failMessage.value,
                color = MaterialTheme.colorScheme.onBackground,
            )
        } else {
            if (emoteType == 4) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(100.dp),
                ) {
                    items(listItems) { item ->
                        Box(
                            modifier = Modifier
                                .clickable {
                                    onInputEmoji(item)
                                }
                                .padding(4.dp)
                                .heightIn(min = 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.text,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(60.dp),
                ) {
                    items(listItems) { item ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    onInputEmoji(item)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            GlideImage(
                                modifier = Modifier.size(48.dp),
                                model = UrlUtil.autoHttps(item.url),
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }

}
