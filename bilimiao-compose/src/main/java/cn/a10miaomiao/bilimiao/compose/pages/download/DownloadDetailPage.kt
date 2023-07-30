package cn.a10miaomiao.bilimiao.compose.pages.download

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.download.commponents.DownloadDetailItem
import cn.a10miaomiao.bilimiao.compose.pages.download.commponents.DownloadListItem
import cn.a10miaomiao.bilimiao.compose.pages.setting.ProxySettingPageViewModel
import cn.a10miaomiao.bilimiao.download.DownloadService
import cn.a10miaomiao.bilimiao.download.LocalPlayerSource
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.subDI
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import java.io.File


class DownloadDetailPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val composeNav by instance<NavHostController>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    val downloadInfo = MutableStateFlow<DownloadInfo?>(null)
    val downloadItems = MutableStateFlow(emptyList<DownloadItemInfo>())
    val curDownload = MutableStateFlow<CurrentDownloadInfo?>(null)
    private var downloadService: DownloadService? = null

    fun loadDownloadDetail(
        dirPath: String,
    ) = viewModelScope.launch {
        val service = DownloadService.getService(fragment.requireContext())
        downloadService = service
        _loadDownloadDetail(service, dirPath)
        if (downloadInfo.value == null) {
            PopTip.show("缓存文件错误")
        }
        launch {
            service.downloadListVersion.collect {
                _loadDownloadDetail(service, dirPath)
            }
        }
        service.curDownload.collect {
            curDownload.value = it
        }
    }

    private fun _loadDownloadDetail(
        service: DownloadService,
        dirPath: String,
    ) {
        val list = service.readDownloadDirectory(File(dirPath))
        val items = mutableListOf<DownloadItemInfo>()
        var isCompleted = true
        list.forEach {
            val biliEntry = it.entry
            var indexTitle = ""
            var itemTitle = ""
            var id = 0L
            var cid = 0L
            var epid = 0L
            var type = DownloadType.VIDEO
            val page = biliEntry.page_data
            if (page != null) {
                id = biliEntry.avid!!
                indexTitle = page.download_title
                cid = page.cid
                type = DownloadType.VIDEO
                itemTitle = page.part
            }
            val ep = biliEntry.ep
            val source = biliEntry.source
            if (ep != null && source != null) {
                id = biliEntry.season_id!!.toLong()
                indexTitle = ep.index_title
                epid = ep.episode_id
                cid = source.cid
                type = DownloadType.BANGUMI
                itemTitle = ep.index + ep.index_title
            }
            val item = DownloadItemInfo(
                dir_path = it.entryDirPath,
                media_type = biliEntry.media_type,
                has_dash_audio = biliEntry.has_dash_audio,
                is_completed = biliEntry.is_completed,
                total_bytes = biliEntry.total_bytes,
                downloaded_bytes = biliEntry.downloaded_bytes,
                title = itemTitle,
                cover = biliEntry.cover,
                id = id,
                type = type,
                cid = cid,
                epid = epid,
                index_title = indexTitle,
            )
            items.add(item)
            if (!item.is_completed) {
                isCompleted = false
            }
        }
        if (list.isEmpty()) {
            downloadInfo.value = null
            downloadItems.value = emptyList()
        } else {
            val biliEntry = list[0].entry
            val item = items[0]
            downloadInfo.value = DownloadInfo(
                dir_path = list[0].pageDirPath,
                media_type = biliEntry.media_type,
                has_dash_audio = biliEntry.has_dash_audio,
                is_completed = isCompleted,
                total_bytes = biliEntry.total_bytes,
                downloaded_bytes = biliEntry.downloaded_bytes,
                title = biliEntry.title,
                cover = biliEntry.cover,
                cid = item.cid,
                id = item.id,
                type = item.type,
                items = items
            )
            downloadItems.value = items
        }
    }

    fun itemClick(item: DownloadItemInfo) {
        if (item.is_completed) {
            basePlayerDelegate.openPlayer(LocalPlayerSource(
                activity = fragment.requireActivity(),
                entryDirPath = item.dir_path,
                id = item.id.toString(),
                title = item.title,
                coverUrl = item.cover,
            ))
        }
    }

    fun startClick(item: DownloadItemInfo) {
        downloadService?.startDownload(item.dir_path)
    }

    fun pauseClick(item: DownloadItemInfo, taskId: Long) {
        downloadService?.cancelDownload(taskId)
    }

    fun deleteDownload(
        item: DownloadItemInfo,
        dirPath: String,
    ) {
        val info = downloadInfo?.value ?: return
        viewModelScope.launch {
            val service = DownloadService.getService(fragment.requireContext())
            service.deleteDownload(info.dir_path, item.dir_path)
            PopTip.show("已删除：" + info.title + "-"  +item.title)
            _loadDownloadDetail(service, dirPath)
            if (downloadInfo.value == null) {
                composeNav.popBackStack()
            }
        }
    }
}

@Composable
fun DownloadDetailPage(
    dirPath: String,
) {
    PageConfig(
        title = "下载详情"
    )
    val viewModel: DownloadDetailPageViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val bottomAppBarHeight = windowStore.bottomAppBarHeightDp

    LaunchedEffect(viewModel, dirPath) {
        DebugMiao.log(dirPath)
        viewModel.loadDownloadDetail(dirPath)
    }

    val downloadInfo by viewModel.downloadInfo.collectAsState()
    val downloadItems by viewModel.downloadItems.collectAsState()
    val curDownload by viewModel.curDownload.collectAsState()

    Column() {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = windowInsets.leftDp.dp, end = windowInsets.rightDp.dp)
        ) {
            item {
                Column() {
                    Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
                    downloadInfo?.let {
                        DownloadListItem(curDownload, it, onClick = {})
                    }
                }
            }
            items(
                downloadItems,
                key = { it.cid },
            ) { item ->
                DownloadDetailItem(
                    curDownload = curDownload,
                    item = item,
                    onClick = {
                        viewModel.itemClick(item)
                    },
                    onStartClick = {
                        viewModel.startClick(item)
                    },
                    onPauseClick = {
                        viewModel.pauseClick(item, it)
                    },
                    onDeleteClick = {
                        viewModel.deleteDownload(item, dirPath)
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp + bottomAppBarHeight.dp))
            }
        }
    }

}

