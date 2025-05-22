package cn.a10miaomiao.bilimiao.compose.pages.video

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.archive.v1.Page
import bilibili.app.view.v1.ViewGRPC
import bilibili.app.view.v1.ViewPage
import bilibili.app.view.v1.ViewReq
import bilibili.polymer.app.search.v1.Item.CardItem
import bilibili.polymer.app.search.v1.SearchGRPC
import bilibili.polymer.app.search.v1.Space
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.flow.stateMap
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.status.BiliFailBox
import cn.a10miaomiao.bilimiao.compose.components.status.BiliLoadingBox
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.bangumi.EpisodeInfo
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class VideoPagesPage(
    val aid: String,
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: VideoPagesPageViewModel = diViewModel(
            key = aid,
        ) {
            VideoPagesPageViewModel(it, aid)
        }
        VideoPagesPageContent(viewModel)
    }
}

private class VideoPagesPageViewModel(
    override val di: DI,
    val aid: String,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()
    private val playerStore by instance<PlayerStore>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    val loading = MutableStateFlow(false)
    val fail = MutableStateFlow<String?>(null)
    val pages = MutableStateFlow(listOf<Page>())

    var arcInfo: bilibili.app.archive.v1.Arc? = null
    val currentPlay: StateFlow<PlayerStore.State> get() = playerStore.stateFlow

    init {
        loadPages()
    }

    fun loadPages() = viewModelScope.launch(Dispatchers.IO) {
        try {
            loading.value = true
            fail.value = null
            val req = ViewReq(
                aid = aid.toLong()
            )
            val result = BiliGRPCHttp.request {
                ViewGRPC.view(req)
            }.awaitCall()
            arcInfo = result.arc
            pages.value = result.pages.map {
                it.page
            }.filterNotNull()
        } catch (e: Exception) {
            e.printStackTrace()
            fail.value = e.message ?: e.toString()
        } finally {
            loading.value = false
        }
    }

    fun startPlayVideo(page: Page) {
        val arc = arcInfo ?: return
        val playerSource = VideoPlayerSource(
            aid = aid,
            id = page.cid.toString(),
            coverUrl = arc.pic,
            mainTitle = arc.title,
            title = page.part,
            ownerId = arc.author?.mid.toString(),
            ownerName = arc.author?.name.toString(),
        )
        playerSource.pages = pages.value.map {
            VideoPlayerSource.PageInfo(
                cid = it.cid.toString(),
                title = it.part,
            )
        }
        basePlayerDelegate.openPlayer(playerSource)
    }
}

@Composable
private fun VideoPagesPageContent(
    viewModel: VideoPagesPageViewModel
) {
    PageConfig(
        title = "视频分P"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val pages by viewModel.pages.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val failMessage by viewModel.fail.collectAsState()
    val currentPlay by viewModel.currentPlay.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun scrollToCurrentPlay() {
        if (viewModel.aid != currentPlay.aid) return
        val index = pages.indexOfFirst {
            it.cid.toString() == currentPlay.cid
        }
        if (index != -1) {
            scope.launch {
                listState.animateScrollToItem(index)
            }
        }
    }

    LaunchedEffect(pages) {
        scrollToCurrentPlay()
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (loading) {
            BiliLoadingBox(
                modifier = Modifier
                    .fillMaxSize()
            )
        } else if (failMessage != null) {
            BiliFailBox(
                e = failMessage!!,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        LazyColumn(
            state = listState,
            contentPadding = windowInsets.toPaddingValues()
        ) {
            items(pages.size, { pages[it].cid }) { index ->
                val page = pages[index]
                val isCurrentPlay = currentPlay.cid == page.cid.toString()
                Box(Modifier.padding(vertical = 5.dp, horizontal = 10.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(min = 50.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = if (isCurrentPlay) BorderStroke(
                            1.dp, color = MaterialTheme.colorScheme.primary
                        ) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = {
                                    viewModel.startPlayVideo(page)
                                })
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "P${index + 1} " + page.part,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isCurrentPlay) {
                                Text(
                                    text = "正在播放",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(end = 5.dp),
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            } else {
                                Text(
                                    text = NumberUtil.converDuration(page.duration),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(end = 5.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
            if (currentPlay.aid == viewModel.aid) {
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            Color.Transparent,
                        )
                    )
                )
        )
        if (currentPlay.aid == viewModel.aid) {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .padding(
                        bottom = windowInsets.bottomDp.dp
                    ),
                onClick = ::scrollToCurrentPlay,
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(text = "当前播放：")
                    Text(
                        text = currentPlay.title
                    )
                }
            }
        }

    }

}