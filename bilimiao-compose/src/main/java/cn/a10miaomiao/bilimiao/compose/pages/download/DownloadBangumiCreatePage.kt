package cn.a10miaomiao.bilimiao.compose.pages.download

import android.preference.PreferenceManager
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.download.DownloadService
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResponseResult
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.bangumi.EpisodeInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.SeasonSectionInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.SeasonV2Info
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class DownloadBangumiCreatePage(
    private val id: String,
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: DownloadBangumiCreatePageViewModel = diViewModel()
        LaunchedEffect(id) {
            viewModel.loadEpisodeList(id)
        }
        DownloadBangumiCreatePageContent(viewModel)
    }

}

internal class DownloadBangumiCreatePageViewModel(
    override val di: DI
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

    val isRefreshing = MutableStateFlow(false)
    val listState = MutableStateFlow(LazyListState(0, 0))
    val list = FlowPaginationInfo<EpisodeInfo>()

    val acceptQuality = MutableStateFlow(QualityInfo())

    val checkedSet = MutableStateFlow(setOf<String>()) // 已选中
    val downloadedSet = MutableStateFlow(setOf<String>()) // 已下载

    private var _sid = ""
    private var _seasonDetail: SeasonV2Info? = null

    /**
     * 剧集信息
     */
    fun loadEpisodeList(
        id: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        _sid = id
        val downloadService = DownloadService.getService(fragment.requireContext())
        getDowbloadedList(downloadService, id)
        loadBangumiDetail(id)
        try {
            list.loading.value = true
            val res = BiliApiService.bangumiAPI.seasonSection(id)
                .awaitCall()
                .json<ResponseResult<SeasonSectionInfo>>()
            if (res.isSuccess) {
                val result = res.requireData().main_section?.episodes ?: emptyList()
                list.data.value = result
                if (result.isNotEmpty()) {
                    loadAcceptQuality(result[0].id, result[0].cid)
                }
            } else {
                list.fail.value = res.message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = "无法连接到御坂网络"
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }

    }

    private fun loadAcceptQuality(
        epId: String,
        cid: String,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(fragment.requireContext())
            val quality = prefs.getInt("player_quality", 64)
            val res = BiliApiService.playerAPI.getBangumiUrl(
                epId, cid, quality, fnval = 4048
            )
            acceptQuality.value = QualityInfo(
                acceptQuality = res.accept_quality,
                acceptDescription = res.accept_description,
                quality = res.quality
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDowbloadedList(
        downloadService: DownloadService,
        sid: String,
    ) {
        downloadedSet.value = downloadService.downloadList.filter {
            it.entry.ep != null && it.entry.season_id == sid
        }.map { it.entry.ep!!.episode_id.toString() }.toSet()
    }

    private fun loadBangumiDetail(
        id: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.bangumiAPI.seasonInfoV2(id, "").awaitCall()
                .json<ResponseData<SeasonV2Info>>()
            if (res.code == 0) {
                _seasonDetail = res.data
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
        }
    }

    fun setQuality(
        index: Int,
    ) {
        val acceptQualityList = acceptQuality.value.acceptQuality
        if (index in acceptQualityList.indices) {
            acceptQuality.value = acceptQuality.value.copy(
                quality = acceptQualityList[index]
            )
        }
    }

    fun checkedChange(epId: String) {
        val checkedList = checkedSet.value
        if (checkedList.contains(epId)) {
            checkedSet.value = checkedList.toMutableSet().also {
                it.remove(epId)
            }
        } else {
            checkedSet.value = checkedList.toMutableSet().also {
                it.add(epId)
            }
        }
    }

    fun startDownload() {
        if (_seasonDetail == null) {
            PopTip.show("番剧信息未加载")
            return
        }
        val checkedList = checkedSet.value
        val episodeList = list.data.value
        if (checkedList.isEmpty()) {
            PopTip.show("请选择分集")
            return
        }
        if (acceptQuality.value.quality == 0) {
            PopTip.show("请选择清晰度")
            return
        }
        viewModelScope.launch {
            val downloadService = DownloadService.getService(fragment.requireContext())
            episodeList.forEachIndexed { index, item ->
                if (checkedList.contains(item.id)) {
                    createDownload(index, item, downloadService)
                }
            }
            withContext(Dispatchers.Main) {
                PopTip.show("成功创建${checkedList.size}条记录")
            }
            checkedSet.value = emptySet()
            getDowbloadedList(downloadService, _sid)
        }
    }

    private fun createDownload(
        index: Int,
        episode: EpisodeInfo,
        downloadService: DownloadService,
    ) {
        val qualityInfo = acceptQuality.value
        val currentTime = System.currentTimeMillis()
        val source = BiliDownloadEntryInfo.SourceInfo(
            av_id = episode.aid.toLong(),
            cid = episode.cid.toLong(),
        )
        val ep = BiliDownloadEntryInfo.EpInfo(
            av_id = source.av_id,
            page = index,
            danmaku = source.cid,
            cover = episode.cover,
            episode_id = episode.id.toLong(),
            index = episode.title,
            index_title = episode.long_title,
            from = "bangumi",
            season_type = 4,
            width = 0,
            height = 0,
            rotate = 0,
            link = "https:\\/\\/www.bilibili.com\\/bangumi\\/play\\/ep${episode.id}",
            bvid = "",
            sort_index = index,
        )
        val biliVideoEntry = BiliDownloadEntryInfo(
            media_type = 2,
            has_dash_audio = true,
            is_completed = false,
            total_bytes = 0,
            downloaded_bytes = 0,
            title = _seasonDetail!!.season_title,
            type_tag = qualityInfo.quality.toString(),
            cover = episode.cover,
            prefered_video_quality = qualityInfo.quality,
            quality_pithy_description = qualityInfo.description,
            guessed_total_bytes = 0,
            total_time_milli = 0,
            danmaku_count = 1000,
            time_update_stamp = currentTime,
            time_create_stamp = currentTime,
            can_play_in_advance = true,
            interrupt_transform_temp_file = false,
            spid = 0L,
            season_id = _sid,
            bvid = "",
            avid = source.av_id,
            ep = ep,
            source = source,
            owner_id = 0L,
            page_data = null,
        )
        downloadService.createDownload(biliVideoEntry)
    }

    data class QualityInfo(
        val acceptQuality: List<Int> = emptyList(),
        val acceptDescription: List<String> = emptyList(),
        val quality: Int = 0,
    ) {
        val description
            get() = acceptQuality.indexOf(quality).let {
                if (it in acceptDescription.indices) {
                    acceptDescription[it]
                } else {
                    "未选择"
                }
            }
    }
}

@Composable
internal fun EpisodeItem(
    episode: EpisodeInfo,
    enabled: Boolean,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    Box(
        modifier = Modifier.padding(
            vertical = 5.dp,
            horizontal = 10.dp,
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
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
                Checkbox(
                    enabled = enabled,
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }
        }
    }
}

@Composable
internal fun DownloadBangumiCreatePageContent(
    viewModel: DownloadBangumiCreatePageViewModel,
) {
    val userStore: UserStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val bottomAppBarHeight = windowStore.bottomAppBarHeightDp
    val windowInsets = windowState.getContentInsets(localContainerView())

    val viewModel: DownloadBangumiCreatePageViewModel = diViewModel()
    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val acceptQuality by viewModel.acceptQuality.collectAsState()
    val checkedSet by viewModel.checkedSet.collectAsState()
    val downloadedSet by viewModel.downloadedSet.collectAsState()

    var expandedQualityMenu by remember { mutableStateOf(false) }

    PageConfig(
        title = "创建下载任务"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = windowInsets.leftDp.dp,
                        end = windowInsets.rightDp.dp,
                    )
            ) {
                item("top") {
                    Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
                }
                items(list.size, { it }) {
                    val item = list[it]
                    val isEnabled = !downloadedSet.contains(item.id)
                    val isChecked = if (isEnabled) {
                        checkedSet.contains(item.id)
                    } else { true }
                    EpisodeItem(
                        episode = item,
                        enabled = isEnabled,
                        checked = isChecked,
                        onCheckedChange = { viewModel.checkedChange(item.id) }
                    )
                }
                item("bottom") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (listLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp,
                            )
                            Text(
                                "加载中",
                                modifier = Modifier.padding(start = 5.dp),
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 14.sp,
                            )
                        } else if (listFail.isNotBlank()) {
                            Text(
                                listFail,
                                modifier = Modifier.padding(start = 5.dp),
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                    bottom = windowInsets.bottomDp.dp
                )
                .padding(5.dp)
        ) {
            Box() {
                Button(
                    modifier = Modifier.padding(end = 5.dp),
                    onClick = { expandedQualityMenu = true },
                ) {
                    Text(text = "画质：" + acceptQuality.description)
                }
                DropdownMenu(
                    expanded = expandedQualityMenu,
                    onDismissRequest = { expandedQualityMenu = false },
                ) {
                    acceptQuality.acceptDescription.forEachIndexed { index, s ->
                        DropdownMenuItem(
                            onClick = {
                                expandedQualityMenu = false
                                viewModel.setQuality(index)
                            },
                            text = {
                                Text(text = s)
                            }
                        )
                    }
                }
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = viewModel::startDownload,
                enabled = checkedSet.isNotEmpty(),
            ) {
                Text(text = "开始下载(${checkedSet.size})")
            }
            Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp + 10.dp))
        }
    }

}