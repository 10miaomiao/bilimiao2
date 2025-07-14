package cn.a10miaomiao.bilimiao.compose.pages.bangumi

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.layout.DoubleColumnAutofitLayout
import cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable.rememberChainScrollableLayoutState
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.components.BangumiEpisodeItem
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyListPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadBangumiCreatePage
import com.a10miaomiao.bilimiao.comm.delegate.player.BangumiPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResponseResult
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.bangumi.*
import com.a10miaomiao.bilimiao.comm.entity.comm.ToastInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
data class BangumiDetailPage(
    // 三选其一
    private val id: String = "",
    private val epId: String = "",
    private val mediaId: String = "",
) : ComposePage() {


    @Composable
    override fun Content() {
        val viewModel: BangumiDetailPageViewModel = diViewModel()
        BangumiDetailPageContent(
            id = id,
            epid = epId,
            mediaId = mediaId,
            viewModel = viewModel,
        )
    }

}

private class BangumiDetailPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val pageNavigation by instance<PageNavigation>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()
    private val bottomSheetState by instance<BottomSheetState>()

    var seasonId = ""
        set(value) {
            if (field != value) {
                field = value
                if (field.isNotBlank() && field != detailInfo.value?.season_id) {
                    loadData()
                    loadEpisodeList(field)
                }
            }
        }
    var epId = ""
        set(value) {
            if (field != value) {
                field = value
                if (field.isNotBlank() && seasonId.isBlank()) {
                    loadData()
                }
            }
        }

    var loading = MutableStateFlow(false)
    val detailInfo = MutableStateFlow<SeasonV2Info?>(null)
    val isFollow = MutableStateFlow(false)
    var seasons = MutableStateFlow<List<SeasonInfo>>(emptyList())

    var sectionLoading = MutableStateFlow(false)
    var sectionList = MutableStateFlow<List<SeasonSectionInfo.SectionInfo>>(emptyList())
    val sectionId = MutableStateFlow("")
    val isRefreshing = MutableStateFlow(false)
//    val isFollow get() = detailInfo.value?.user_status?.follow == 1

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            loading.value = true
            detailInfo.value = null

            val res = BiliApiService.bangumiAPI.seasonInfoV2(
                seasonId, epId
            ).awaitCall()
                .json<ResponseData<SeasonV2Info>>()
            if (res.code == 0) {
                val result = res.requireData()
                detailInfo.value = result
                val seasonModule = result.modules.find {
                    it.style == "season"
                }
                seasons.value = seasonModule?.data?.seasons ?: emptyList()
                isFollow.value = detailInfo.value?.user_status?.follow == 1
                if (seasonId != result.season_id) {
                    seasonId = result.season_id
                    loadEpisodeList(result.season_id)
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show("无法连接到御坂网络")
            }
        } finally {
            loading.value = false
        }
    }

    /**
     * 剧集信息
     */
    fun loadEpisodeList(
        id: String,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            sectionLoading.value = true
            sectionList.value = emptyList()
            sectionId.value = ""

            val res = BiliApiService.bangumiAPI.seasonSection(id)
                .awaitCall()
                .json<ResponseResult<SeasonSectionInfo>>()
            if (res.code == 0) {
                val result = res.requireData()
                val list = mutableListOf<SeasonSectionInfo.SectionInfo>()
                result.main_section?.let(list::add)
                result.section?.let(list::addAll)
                sectionList.value = list.toList()
                list.firstOrNull()?.let {
                    sectionId.value = it.id
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show("无法连接到御坂网络")
            }
        } finally {
            sectionLoading.value = false
            isRefreshing.value = false
        }
    }

    fun followSeason() = viewModelScope.launch(Dispatchers.IO) {
        val detail = detailInfo.value ?: return@launch
        try {
            val mode = if (isFollow.value) {
                2
            } else {
                1
            }
            val res = (if (mode == 2) {
                BiliApiService.bangumiAPI.cancelFollow(detail.season_id)
            } else {
                BiliApiService.bangumiAPI.followSeason(detail.season_id)
            }).awaitCall().json<ResponseResult<ToastInfo>>()
            if (res.isSuccess) {
                isFollow.value = mode == 1
                withContext(Dispatchers.Main) {
                    PopTip.show(
                        if (mode == 1) {
                            res.result?.toast ?: "追番成功"
                        } else {
                            res.result?.toast ?: "已取消追番"
                        }
                    )
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                PopTip.show("网络错误")
            }
            e.printStackTrace()
        }
    }

    fun refresh() {
        if (seasonId.isBlank()) {
            isRefreshing.value = true
            loadData()
            loadEpisodeList(seasonId)
        }
    }

    fun changeSection(item: SeasonSectionInfo.SectionInfo) {
        sectionId.value = item.id
    }

    fun toCommentListPage(item: EpisodeInfo) {
        pageNavigation.navigate(MainReplyListPage(
            oid = item.aid,
            type = 1,
        ))
    }

    fun shareEpisode(item: EpisodeInfo) {
        val title = item.title + if (item.long_title.isBlank()) "" else "_" + item.long_title
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "bilibili番剧分享")
            putExtra(Intent.EXTRA_TEXT, "$title https://www.bilibili.com/bangumi/play/ep${item.id}")
        }
        fragment.requireActivity().startActivity(Intent.createChooser(shareIntent, "分享"))
    }

    fun startPlayBangumi(episodes: List<EpisodeInfo>, item: EpisodeInfo) {
        val seasonDetail = detailInfo.value ?: return
        val playerSource = BangumiPlayerSource(
            sid = seasonDetail.season_id,
            epid = item.id,
            aid = item.aid,
            id = item.cid,
            title = item.long_title.ifBlank { item.title },
            coverUrl = item.cover,
            ownerId = "",
            ownerName = seasonDetail.season_title
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
        playerSource.defaultPlayerSource.run {
            val progress = detailInfo.value?.user_status?.progress
            if (progress != null && item.id == progress.last_ep_id ) {
                lastPlayCid = item.cid
                lastPlayTime = progress.last_time * 1000L
            }
            val dimension = item.dimension
            if (dimension != null) {
                width = dimension.width
                height = dimension.height
            }
        }
        basePlayerDelegate.openPlayer(playerSource)
    }

    fun menuItemClick(view: View, menuItem: MenuItemPropInfo) {
        when (menuItem.key) {
            1 -> {
                // 用浏览器打开
                val info = detailInfo.value
                if (info != null) {
                    val id = info.season_id
                    var url = "https://www.bilibili.com/bangumi/play/ss$id"
                    BiliUrlMatcher.toUrlLink(fragment.requireContext(), url)
                } else {
                    PopTip.show("请等待信息加载完成")
                }
            }
            2 -> {
                // 分享番剧
                val info = detailInfo.value
                if (info != null) {
                    val activity = fragment.requireActivity()
                    var shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "bilibili番剧分享")
                        putExtra(Intent.EXTRA_TEXT, "${info.season_title} https://www.bilibili.com/bangumi/play/ss${info.season_id}")
                    }
                    activity.startActivity(Intent.createChooser(shareIntent, "分享"))
                } else {
                    PopTip.show("请等待信息加载完成")
                }

            }
            3 -> {
                // 复制链接
                val info = detailInfo.value
                if (info != null) {
                    val activity = fragment.requireActivity()
                    val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    var label = "url"
                    var text = "https://www.bilibili.com/bangumi/play/ss${info.season_id}"
                    val clip = ClipData.newPlainText(label, text)
                    clipboard.setPrimaryClip(clip)
                    PopTip.show("已复制：$text")
                } else {
                    PopTip.show("请等待信息加载完成")
                }
            }
            4 -> {
                // 下载番剧
                val info = detailInfo.value
                if (info != null) {
                    bottomSheetState.open(DownloadBangumiCreatePage(info.season_id))
                } else {
                    PopTip.show("请等待信息加载完成")
                }
            }

            MenuKeys.follow -> {
                // 追番
                followSeason()
            }
        }
    }

    fun findSectionEpisodeIndex(id: String): Pair<SeasonSectionInfo.SectionInfo?, Int> {
        val sections = sectionList.value
        var index: Int = -1
        return sections.find {
            index = it.episodes.indexOfFirst { ep ->
                ep.id == id
            }
            index != -1
        } to index
    }
}


@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
private fun BangumiDetailPageContent(
    id: String,
    epid: String,
    mediaId: String,
    viewModel: BangumiDetailPageViewModel,
) {
    val playerStore: PlayerStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val playerState = playerStore.stateFlow.collectAsState().value
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val detailInfo = viewModel.detailInfo.collectAsState().value
    val isFollow = viewModel.isFollow.collectAsState().value
    val seasons = viewModel.seasons.collectAsState().value
    val loading = viewModel.loading.collectAsState().value

    val sectionList = viewModel.sectionList.collectAsState().value
    val sectionId = viewModel.sectionId.collectAsState().value
    val sectionLoading = viewModel.sectionLoading.collectAsState().value
    val episodes = remember(sectionId, sectionList) {
        sectionList.find {
            it.id == sectionId
        }?.episodes ?: emptyList()
    }

    val scope = rememberCoroutineScope()
    val chainScrollableLayoutState = rememberChainScrollableLayoutState(
        maxScrollPosition = 340.dp,
    )
    val seasonsListState = rememberLazyListState()
    val episodesListState = rememberLazyListState()

    var seasonId = rememberSaveable() {
        mutableStateOf(id)
    }
    var seasonEpId = rememberSaveable() {
        mutableStateOf(epid)
    }

    LaunchedEffect(seasonId.value) {
        viewModel.seasonId = seasonId.value
    }
    LaunchedEffect(seasonEpId.value) {
        viewModel.epId = seasonEpId.value
    }
    LaunchedEffect(detailInfo) {
        detailInfo?.let {
            if (it.season_id != seasonId.value) {
                seasonId.value = it.season_id
            }
        }
    }
    LaunchedEffect(seasons, seasonId.value) {
        val index = seasons.indexOfFirst {
            it.season_id == seasonId.value
        }
        if (index > 0) {
            seasonsListState.scrollToItem(index)
        }
    }

    LaunchedEffect(mediaId) {
        // 先通过mediaId拿到seasonId
        if (mediaId.isNotBlank() && seasonId.value.isBlank()) {
            try {
                val res = withContext(Dispatchers.IO) {
                    MiaoHttp
                        .request {
                            url = "https://api.bilibili.com/pgc/review/user?media_id=${mediaId}"
                        }
                        .awaitCall()
                        .json<ResponseResult<Map<String, JsonElement>>>()
                }
                if (res.isSuccess) {
                    val resultData = res.requireData()
                    if (resultData.containsKey("media")) {
                        val media = resultData["media"]!!
                        val jsonObject = media.jsonObject
                        if (jsonObject.containsKey("season_id")) {
                            seasonId.value = jsonObject["season_id"]!!.jsonPrimitive.content
                        }
                    }
                } else {
                    PopTip.show(res.message)
                }
            } catch (e: Exception) {
                PopTip.show("网络错误")
            }
        }
    }

    val pageConfigId = PageConfig(
        title = detailInfo?.season_title ?: "番剧详情",
        menu = remember(isFollow) {
            myMenu {
                myItem {
                    key = MenuKeys.more
                    iconFileName = "ic_more_vert_grey_24dp"
                    title = "更多"

                    childMenu = myMenu {
                        myItem {
                            key = 1
                            title = "用浏览器打开"
                        }
                        myItem {
                            key = 2
                            title = if (detailInfo != null) {
                                "分享番剧(${detailInfo.stat.share})"
                            } else {
                                "分享番剧"
                            }
                        }
                        myItem {
                            key = 3
                            title = "复制链接"
                        }
                        myItem {
                            key = 4
                            title = "下载番剧"
                        }
                    }
                }
                myItem {
                    key = MenuKeys.follow
                    if (isFollow) {
                        iconFileName = "ic_baseline_favorite_24"
                        title = "已追番"
                    } else {
                        iconFileName = "ic_outline_favorite_border_24"
                        title = "追番"
                    }
                }
            }
        }

    )
    PageListener(
        pageConfigId,
        onMenuItemClick = viewModel::menuItemClick
    )

    val isRefreshing by viewModel.isRefreshing.collectAsState()

    SwipeToRefresh(
        modifier = Modifier
            .fillMaxSize(),
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        DoubleColumnAutofitLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            innerPadding = windowInsets.toPaddingValues(),
            chainScrollableLayoutState = chainScrollableLayoutState,
            leftMaxWidth = 600.dp,
            leftMaxHeight = 340.dp,
            leftContent = { _, innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    if (detailInfo != null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            GlideImage(
                                model = UrlUtil.autoHttps(detailInfo.cover) + "@560w_746h",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp, 166.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                            )
                            Text(
                                modifier = Modifier.padding(vertical = 10.dp),
                                text = detailInfo.season_title,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                modifier = Modifier.padding(bottom = 5.dp),
                                text = detailInfo.new_ep.desc,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row() {
                                Text(
                                    text = detailInfo.stat.followers,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = detailInfo.stat.play,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else if (loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp,
                            )
                        }

                    }
                }
            }
        ) { _, innerPadding ->
            LazyColumn(
                state = episodesListState,
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = innerPadding,
            ) {
                item("evaluate") {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        ) {
                            if (seasons.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "选择系列：",
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    LazyRow(
                                        state = seasonsListState,
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        items(seasons, { it.season_id }) {
                                            FilterChip(
                                                selected = it.season_id == seasonId.value,
                                                onClick = {
                                                    seasonId.value = it.season_id
                                                },
                                                label = {
                                                    Text(text = it.season_title)
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                            Text(
                                text = detailInfo?.evaluate ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                if (sectionList.size > 1) {
                    item("sectionList") {
                        LazyRow(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            items(sectionList, { it.id }) {
                                FilterChip(
                                    selected = it.id == sectionId,
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
                val userProgress = detailInfo?.user_status?.progress
                items(episodes, { it.id }) { item ->
                    BangumiEpisodeItem(
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp,
                        ),
                        item = item,
                        desc = if (item.id == userProgress?.last_ep_id) {
                            val time = NumberUtil.converDuration(userProgress.last_time)
                            "上次看到 $time"
                        } else null,
                        playerState = playerState,
                        onClick = {
                            viewModel.startPlayBangumi(episodes, item)
                        },
                        onCommentClick = {
                            viewModel.toCommentListPage(item)
                        },
                        onShareClick = {
                            viewModel.shareEpisode(item)
                        }
                    )
                }
            }
        }

        detailInfo?.user_status?.progress?.let {
            if (playerState.sid == detailInfo.season_id) {
                return@let
            }
            val lastEpIndex = it.last_ep_index.ifBlank {
                episodes.firstOrNull { episode ->
                    it.last_ep_id == episode.id
                }?.index ?: return@let
            }
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .padding(
                        bottom = windowStore.bottomAppBarHeightDp.dp
                                + windowInsets.bottomDp.dp
                    ),
                onClick = {
                    scope.launch {
                        chainScrollableLayoutState.scrollToMax()
                        val (section, index) = viewModel.findSectionEpisodeIndex(
                            it.last_ep_id
                        )
                        if (section != null && index != -1) {
                            val offset = if (sectionList.size > 1) {
                                2
                            } else {
                                1
                            }
                            viewModel.changeSection(section)
                            episodesListState.scrollToItem(
                                index = index + offset,
                                scrollOffset = -windowInsets.top
                            )
                        }
                    }
                }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(text = "上次看到")
                    Text(
                        text = "${if (NumberUtil.isNumber(lastEpIndex)) {
                            "第${lastEpIndex}话"
                        } else {
                            lastEpIndex
                        }} ${NumberUtil.converDuration(it.last_time)}"
                    )

                }
            }
        }

    }

}