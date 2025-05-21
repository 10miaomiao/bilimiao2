package cn.a10miaomiao.bilimiao.compose.pages.bangumi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.flow.stateMap
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.status.BiliFailBox
import cn.a10miaomiao.bilimiao.compose.components.status.BiliLoadingBox
import com.a10miaomiao.bilimiao.comm.delegate.player.BangumiPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.ResponseResult
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.bangumi.EpisodeInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.SeasonInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.SeasonSectionInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
data class BangumiEpisodesPage(
    val sid: String,
    val title: String,
): ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: BangumiEpisodesPageViewModel = diViewModel(
            key = sid,
        ) {
            BangumiEpisodesPageViewModel(it, sid, title)
        }
        BangumiEpisodesPageContent(viewModel)
    }
}

private class BangumiEpisodesPageViewModel(
    override val di: DI,
    val sid: String,
    val title: String,
) : ViewModel(), DIAware {

    @Stable
    class SectionState(
        val sectionId: String = "",
        val hasLongTitle: Boolean = true,
        val episodes: List<EpisodeInfo> = listOf()
    )

    private val pageNavigation by instance<PageNavigation>()
    private val playerStore by instance<PlayerStore>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    val loading = MutableStateFlow(false)
    val fail = MutableStateFlow<String?>(null)
    var sectionList = MutableStateFlow<List<SeasonSectionInfo.SectionInfo>>(emptyList())
    val currentSection = MutableStateFlow(SectionState())
    val currentPlay: StateFlow<PlayerStore.State> get() = playerStore.stateFlow

    init {
        loadEpisodeList()
    }


    /**
     * 剧集信息
     */
    fun loadEpisodeList() = viewModelScope.launch(Dispatchers.IO) {
        try {
            loading.value = true
            fail.value = null
            sectionList.value = emptyList()
            currentSection.value = SectionState()
            val res = BiliApiService.bangumiAPI.seasonSection(sid)
                .awaitCall()
                .json<ResponseResult<SeasonSectionInfo>>()
            if (res.code == 0) {
                val result = res.requireData()
                val list = mutableListOf<SeasonSectionInfo.SectionInfo>()
                result.main_section?.let(list::add)
                result.section?.let(list::addAll)
                sectionList.value = list.toList()
                getCurrentPlaySection(list)?.let {
                    changeSection(it)
                }
            } else {
                fail.value = res.message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fail.value = e.message ?: e.toString()
        } finally {
            loading.value = false
        }
    }

    fun getCurrentPlaySection(
        list: List<SeasonSectionInfo.SectionInfo>
    ) : SeasonSectionInfo.SectionInfo?{
        val epid = currentPlay.value.epid
        if (epid.isBlank()) {
            return list.firstOrNull()
        }
        return list.firstOrNull {
            it.episodes.indexOfFirst {
                it.id == epid
            } != -1
        }
    }

    fun changeSection(item: SeasonSectionInfo.SectionInfo) {
        val sectionId = item.id
        val episodes = item.episodes
        val hasLongTitle = episodes.indexOfFirst { it.long_title.isNotBlank() } != -1
        currentSection.value = SectionState(
            sectionId = sectionId,
            hasLongTitle = hasLongTitle,
            episodes = episodes,
        )
    }

    fun startPlayBangumi(item: EpisodeInfo) {
        val episodes = currentSection.value.episodes
        val playerSource = BangumiPlayerSource(
            sid = sid,
            epid = item.id,
            aid = item.aid,
            id = item.cid,
            title = item.long_title.ifBlank { item.title },
            coverUrl = item.cover,
            ownerId = "",
            ownerName = title,
        )
        playerSource.episodes = episodes.map {
            BangumiPlayerSource.EpisodeInfo(
                epid = it.id, aid = it.aid, cid = it.cid,
                cover = it.cover,
                index = it.title,
                index_title = it.long_title,
                badge = it.badge,
                badge_info = BangumiPlayerSource.EpisodeBadgeInfo(
                    text = it.badge_info.text,
                    bg_color = it.badge_info.bg_color,
                    bg_color_night = it.badge_info.bg_color_night,
                ),
            )
        }
        basePlayerDelegate.openPlayer(playerSource)
    }

}

@Composable
private fun ComplexEpisodeItem(
    modifier: Modifier = Modifier,
    episode: EpisodeInfo,
    currentPlayEpid: String,
    onClick: (() -> Unit),
) {
    Box(modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = if (currentPlayEpid == episode.id) BorderStroke(
                1.dp, color = MaterialTheme.colorScheme.primary
            ) else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = episode.title,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 5.dp)
                )
                if (episode.long_title.isNotBlank()) {
                    Text(
                        text = episode.long_title,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (currentPlayEpid == episode.id) {
                    Text(
                        text = "当前播放",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 5.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (episode.badge.isNotBlank()) {
                    Box(
                        modifier = Modifier.padding(horizontal = 5.dp)
                    ) {
                        Text(
                            text = episode.badge,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(
                                    color = Color(episode.badge_info.bg_color.toColorInt()),
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleEpisodeItem(
    modifier: Modifier = Modifier,
    episode: EpisodeInfo,
    currentPlayEpid: String,
    onClick: (() -> Unit),
) {
    Box(modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = if (currentPlayEpid == episode.id) BorderStroke(
                1.dp, color = MaterialTheme.colorScheme.primary
            ) else null
        ) {
            Box (
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                if (episode.badge.isNotBlank()) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = episode.badge,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(
                                    color = Color(episode.badge_info.bg_color.toColorInt()),
                                    shape = RoundedCornerShape(bottomStart = 5.dp)
                                )
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
                if (currentPlayEpid == episode.id) {
                    Text(
                        text = "当前",
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(start = 2.dp, bottom = 2.dp)
                            .align(Alignment.BottomStart),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    modifier = Modifier.padding(2.dp),
                    text = episode.title,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BangumiEpisodesPageContent(
    viewModel: BangumiEpisodesPageViewModel
) {
    PageConfig(
        title = "番剧剧集"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val loading by viewModel.loading.collectAsState()
    val failMessage by viewModel.fail.collectAsState()
    val sectionList by viewModel.sectionList.collectAsState()
    val currentSection by viewModel.currentSection.collectAsState()
    val currentPlay by viewModel.currentPlay.collectAsState()

    val listState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    fun scrollToCurrentPlay() {
        if (viewModel.sid != currentPlay.sid) return
        val index = currentSection.episodes.indexOfFirst {
            it.id == currentPlay.epid
        }
        if (index != -1) {
            scope.launch {
                listState.animateScrollToItem(index)
            }
        }
    }
    LaunchedEffect(currentSection) {
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
        LazyVerticalGrid (
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = windowInsets.toPaddingValues(),
            columns = if (currentSection.hasLongTitle)
                GridCells.Adaptive(400.dp)
            else
                GridCells.Adaptive(80.dp),
        ) {
            if (sectionList.size > 1) {
                item(
                    span = { GridItemSpan(maxLineSpan) }
                ) {
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 8.dp)
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(sectionList, { it.id }) {
                            FilterChip(
                                selected = it.id == currentSection.sectionId,
                                onClick = {
                                    viewModel.changeSection(it)
                                },
                                label = {
                                    Text(
                                        text = it.title
                                    )
                                }
                            )
                        }
                    }
                }
            }
            if (currentSection.hasLongTitle) {
                items(currentSection.episodes) {
                    ComplexEpisodeItem (
                        modifier = Modifier
                            .fillMaxSize()
                            .padding( 5.dp),
                        episode = it,
                        currentPlayEpid = currentPlay.epid,
                        onClick = {
                            viewModel.startPlayBangumi(it)
                        }
                    )
                }
            } else {
                items(currentSection.episodes) {
                    SimpleEpisodeItem(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding( 5.dp),
                        episode = it,
                        currentPlayEpid = currentPlay.epid,
                        onClick = {
                            viewModel.startPlayBangumi(it)
                        }
                    )
                }
            }

            if (currentPlay.sid == viewModel.sid) {
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

        if (currentPlay.sid == viewModel.sid) {
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