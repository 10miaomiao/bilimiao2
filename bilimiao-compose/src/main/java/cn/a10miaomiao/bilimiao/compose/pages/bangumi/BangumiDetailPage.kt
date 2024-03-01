package cn.a10miaomiao.bilimiao.compose.pages.bangumi

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.stringPageArg
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageMenuItemClick
import cn.a10miaomiao.bilimiao.compose.comm.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.commponents.layout.DoubleColumnAutofitLayout
import cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable.rememberChainScrollableLayoutState
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.commponents.BangumiEpisodeItem
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.popup_menu.BangumiMorePopupMenu
import com.a10miaomiao.bilimiao.comm.delegate.player.BangumiPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.bangumi.*
import com.a10miaomiao.bilimiao.comm.entity.comm.ToastInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.gson.JsonObject
import com.kongzue.dialogx.dialogs.PopTip
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class BangumiDetailPage : ComposePage() {

    // 三选其一
    val id = stringPageArg("id", "")
    val epId = stringPageArg("epid", "")
    val mediaId = stringPageArg("mediaid", "")

    override val route: String
        get() = "bangumi/detail?id=${id}&epid=${epId}%mediaid=${mediaId}"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel: BangumiDetailPageViewModel = diViewModel()
        BangumiDetailPageContent(
            id = navEntry.arguments?.get(id) ?: "",
            epid = navEntry.arguments?.get(epId) ?: "",
            mediaId = navEntry.arguments?.get(mediaId) ?: "",
            viewModel = viewModel,
        )
    }

}

internal class BangumiDetailPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()
    private val navController by instance<NavHostController>()

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

//    val isFollow get() = detailInfo.value?.user_status?.follow == 1

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            loading.value = true
            detailInfo.value = null

            val res = BiliApiService.bangumiAPI.seasonInfoV2(
                seasonId, epId
            ).awaitCall()
                .gson<ResultInfo<SeasonV2Info>>()
            if (res.code == 0) {
                val result = res.data
                detailInfo.value = result
                val seasonModule = result.modules.find {
                    it.style == "season"
                }
                seasons.value = seasonModule?.data?.seasons ?: emptyList()
                isFollow.value = detailInfo.value?.user_status?.follow == 1
                if (seasonId != result.season_id) {
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
//            withContext(Dispatchers.Main) {
//                myPage.pageConfig.notifyConfigChanged()
//            }
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
                .gson<ResultInfo2<SeasonSectionInfo>>()
            if (res.code == 0) {
                val result = res.result
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
                BiliApiService.bangumiAPI.cancelFollowSeason(detail.season_id)
            } else {
                BiliApiService.bangumiAPI.followSeason(detail.season_id)
            }).awaitCall().gson<ResultInfo2<ToastInfo>>()
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

    fun refreshData() {
//        ui.setState {
//            list = PaginationInfo()
//            triggered = true
//        loadData()
//        loadEpisodeList()
//        }
    }

    fun changeSection(item: SeasonSectionInfo.SectionInfo) {
        sectionId.value = item.id
    }

    fun toCommentListPage(item: EpisodeInfo) {
        val id = item.aid
        val title = Uri.encode(item.title + if (item.long_title.isBlank()) "" else "_" + item.long_title)
        val cover = Uri.encode(item.cover)
        val name = Uri.encode(detailInfo.value?.season_title ?: "")
        val uri = Uri.parse("bilimiao://video/comment/$id?title=$title&cover=$cover&name=$name")
        fragment.findNavController().navigate(uri)
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
        basePlayerDelegate.openPlayer(playerSource)
    }

    fun menuItemClick(view: View, menuItem: MenuItemPropInfo) {
        when (menuItem.key) {
            MenuKeys.more -> {
                // 更多
                val pm = BangumiMorePopupMenu(
                    activity = fragment.requireActivity(),
                    navController = navController,
                    detailInfo = detailInfo.value,
                )
                pm.show(view)
            }
            MenuKeys.follow -> {
                // 追番
                followSeason()
            }
        }
    }
}


@Composable
internal fun BangumiDetailPageContent(
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
                    MiaoHttp.request {
                        url = "https://api.bilibili.com/pgc/review/user?media_id=${mediaId}"
                    }.awaitCall().gson<ResultInfo2<JsonObject>>()
                }
                if (res.isSuccess) {
                    if (res.result.has("media")
                        && res.result["media"].isJsonObject
                    ) {
                        val media = res.result["media"].asJsonObject
                        if (media.has("season_id")) {
                            seasonId.value = media["season_id"].asString
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

    PageConfig(
        title = detailInfo?.season_title ?: "番剧详情",
        menus = listOf(
            myMenuItem {
                key = MenuKeys.more
                iconFileName = "ic_more_vert_grey_24dp"
//                iconResource = R.drawable.ic_more_vert_grey_24dp
                title = "更多"
            },
            myMenuItem {
                key = MenuKeys.follow
                if (isFollow) {
                    iconFileName = "ic_baseline_favorite_24"
//                    iconResource = R.drawable.ic_baseline_favorite_24
                    title = "已追番"
                } else {
                    iconFileName = "ic_outline_favorite_border_24"
//                    iconResource = R.drawable.ic_outline_favorite_border_24
                    title = "追番"
                }
            },
        )

    )
    PageMenuItemClick(viewModel::menuItemClick)

    DoubleColumnAutofitLayout(
        modifier = Modifier.fillMaxSize(),
        innerPadding = windowInsets.toPaddingValues(),
        chainScrollableLayoutState = chainScrollableLayoutState,
        leftMaxWidth = 600.dp,
        leftMaxHeight = 340.dp,
        leftContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    if (detailInfo != null) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            GlideImage(
                                imageModel = UrlUtil.autoHttps(detailInfo.cover) + "@560w_746h",
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
        }
    ) {
        LazyColumn(
            state = episodesListState,
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 5.dp,
                end = 5.dp,
                top = if (it == Orientation.Vertical) {
                    5.dp
                } else {
                    windowInsets.topDp.dp
                },
                bottom = windowInsets.bottomDp.dp + windowStore.bottomAppBarHeightDp.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item("evaluate") {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
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

            items(episodes, { it.id }) { item ->
                BangumiEpisodeItem(
                    item = item,
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

}