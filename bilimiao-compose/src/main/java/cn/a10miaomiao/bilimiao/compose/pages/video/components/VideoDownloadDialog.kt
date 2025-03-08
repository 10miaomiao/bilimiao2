package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import bilibili.app.archive.v1.Arc
import bilibili.app.archive.v1.Page
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AutoSheetDialog
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadBangumiCreatePageViewModel.QualityInfo
import cn.a10miaomiao.bilimiao.compose.pages.download.EpisodeItem
import cn.a10miaomiao.bilimiao.download.DownloadService
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.annotation.Inherited

@Stable
class VideoDownloadDialogState(
    val scope: CoroutineScope,
) {
    private var downloadService: DownloadService? = null
    private var videoBvid = ""

    private val _visible = mutableStateOf(false)
    val visible: Boolean get() = _visible.value

    private val _loading = mutableStateOf(false)
    val loading: Boolean get() = _loading.value

    private val _list = mutableStateOf(listOf<Page>())
    val list: List<Page> get() = _list.value

    private val _arcData = MutableStateFlow<Arc?>(null)
    val arcData get() = _arcData.value

    private val _checkedMap = mutableStateMapOf<Long,Int>() // 已选中
    val checkedMap: Map<Long,Int> get() = _checkedMap
    val checkedSize: Int get() = _checkedMap.size

    private val _downloadedSet = mutableStateOf(setOf<Long>()) // 已下载
    val downloadedSet: Set<Long> get() = _downloadedSet.value

    private val _qualityList = mutableStateOf(listOf<Pair<Int, String>>()) // Quality: Description
    val qualityList: List<Pair<Int, String>> get() = _qualityList.value

    private val _quality = mutableIntStateOf(0)
    val quality get () = _quality.intValue
    val description get() = qualityList.find { it.first == quality }?.second ?: "未选择"

    val snackbar = SnackbarHostState()

    fun show(
        service: DownloadService,
        bvid: String,
        videoArc: Arc,
        videoPages: List<Page>,
    ) {
        downloadService = service
        _visible.value = true
        _list.value = videoPages
        _arcData.value = videoArc
        _downloadedSet.value = getDownloadedList(
            service,
            videoPages.map { it.cid }.toSet()
        )
        videoBvid = bvid
        if (qualityList.isEmpty() && videoPages.isNotEmpty()) {
            val videoAid = videoArc.aid.toString()
            getAcceptQuality(videoAid, videoPages[0].cid.toString())
        }
    }

    private fun getDownloadedList(
        service: DownloadService,
        cidSet: Set<Long>,
    ): Set<Long> {
        return service
            .downloadList
            .mapNotNull { it.entry.source?.cid ?: it.entry.page_data?.cid }
            .filter { cidSet.contains(it) }
            .toSet()
    }

    private fun getAcceptQuality(
        aid: String,
        cid: String,
    ) = scope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.playerAPI.getVideoPalyUrl(
                aid, cid, 64, fnval = 4048
            )
            val acceptDescription = res.accept_description
            _qualityList.value = res.accept_quality.mapIndexed { index, q ->
                q to (acceptDescription.getOrNull(index) ?: q.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkedChange(cid: Long, index: Int) {
        if (checkedMap.contains(cid)) {
            _checkedMap.remove(cid)
        } else {
            _checkedMap[cid] = index
        }
    }

    fun setQuality(quality: Int) {
        _quality.intValue = quality
    }

    private fun showSnackbar(message: String) {
        scope.launch {
            snackbar.showSnackbar(message)
        }
    }

    fun startDownload() {
        if (quality == 0) {
            showSnackbar("请选择画质")
            return
        }
        val service = downloadService
        if (service == null) {
            showSnackbar("下载服务异常")
            return
        }
        val videoArc = arcData
        if (videoArc == null) {
            showSnackbar("缺少视频信息")
            return
        }
        checkedMap.forEach { c ->
            var page = list.getOrNull(c.value)
            if (page?.cid != c.key) {
                page = list.find { it.cid == c.key }
            }
            if (page != null) {
                downloadVideo(
                    service,
                    videoArc,
                    page,
                )
            }
        }
        PopTip.show("成功创建${checkedSize}条记录")
        dismiss()
        _checkedMap.clear()
    }

    private fun downloadVideo(
        service: DownloadService,
        videoArc: Arc,
        page: Page,
    ) {
        val pageData = BiliDownloadEntryInfo.PageInfo(
            cid = page.cid,
            page = page.page,
            from = page.from,
            part = page.part,
            vid = page.vid,
            has_alias = false,
            tid = 0,
            width = 0,
            height = 0,
            rotate = 0,
            download_title = "视频已缓存完成",
            download_subtitle = videoArc.title
        )
        val currentTime = System.currentTimeMillis()
        val biliVideoEntry = BiliDownloadEntryInfo(
            media_type = 2,
            has_dash_audio = true,
            is_completed = false,
            total_bytes = 0,
            downloaded_bytes = 0,
            title = videoArc.title,
            type_tag = quality.toString(),
            cover = videoArc.pic,
            prefered_video_quality = quality,
            quality_pithy_description = description,
            guessed_total_bytes = 0,
            total_time_milli = 0,
            danmaku_count = 1000,
            time_update_stamp = currentTime,
            time_create_stamp = currentTime,
            can_play_in_advance = true,
            interrupt_transform_temp_file = false,
            avid = videoArc.aid,
            spid = 0,
            season_id = null,
            ep = null,
            source = null,
            bvid = videoBvid,
            owner_id = videoArc.author?.mid ?: 0L,
            page_data = pageData
        )
        service.createDownload(biliVideoEntry)
    }

    fun dismiss() {
        _visible.value = false
    }
}

@Composable
private fun VideoDownloadItem(
    page: Page,
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
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = page.part,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row {
                        Text(
                            text = "P${page.page}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = NumberUtil.converDuration(page.duration),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                        )
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
fun VideoDownloadDialog(
    state: VideoDownloadDialogState,
) {
    if (state.visible) {
        var expandedQualityMenu by remember {
            mutableStateOf(false)
        }
        AutoSheetDialog(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .heightIn(max = 500.dp),
            content = {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "请选择分P下载",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            items(state.list.size, { it }) { index ->
                                val item = state.list[index]
                                val isEnabled = !state.downloadedSet.contains(item.cid)
                                val isChecked = if (isEnabled) {
                                    state.checkedMap.containsKey(item.cid)
                                } else {
                                    true
                                }
                                VideoDownloadItem(
                                    page = item,
                                    enabled = isEnabled,
                                    checked = isChecked,
                                    onCheckedChange = { state.checkedChange(item.cid, index) }
                                )
                            }
                        }
                        SnackbarHost(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            hostState = state.snackbar,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(5.dp)
                    ) {
                        Box() {
                            Button(
                                modifier = Modifier.padding(end = 5.dp),
                                onClick = { expandedQualityMenu = true },
                            ) {
                                Text(text = "画质：" + state.description)
                            }
                            DropdownMenu(
                                expanded = expandedQualityMenu,
                                onDismissRequest = { expandedQualityMenu = false },
                            ) {
                                state.qualityList.forEach {
                                    DropdownMenuItem(
                                        onClick = {
                                            expandedQualityMenu = false
                                            state.setQuality(it.first)
                                        },
                                        text = {
                                            Text(text = it.second)
                                        }
                                    )
                                }
                            }
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = state::startDownload,
                            enabled = state.checkedMap.isNotEmpty(),
                        ) {
                            Text(text = "开始下载(${state.checkedSize})")
                        }
                    }
                }
            },
            onDismiss = state::dismiss
        )
    }
}