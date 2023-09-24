package cn.a10miaomiao.bilimiao.compose.pages.download

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import cn.a10miaomiao.bilimiao.compose.PageRoute
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.download.commponents.DownloadListItem
import cn.a10miaomiao.bilimiao.download.DownloadService
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryAndPathInfo
import cn.a10miaomiao.bilimiao.download.entry.CurrentDownloadInfo
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class DownloadListPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val composeNav by instance<NavHostController>()

    var downloadListVersion = 0
    val downloadList = MutableStateFlow(emptyList<BiliDownloadEntryAndPathInfo>())
    val curDownload = MutableStateFlow<CurrentDownloadInfo?>(null)

    init {
        loadDownloadList()
    }

    private fun loadDownloadList() = viewModelScope.launch {
        val service = DownloadService.getService(fragment.requireContext())
        _loadDownloadList(service)
        launch {
            service.downloadListVersion.collect {
                if (it != downloadListVersion) {
                    downloadListVersion = it
                    _loadDownloadList(service)
                }
            }
        }
        launch {
            service.curDownload.collect(curDownload::value::set)
        }
    }

    private fun _loadDownloadList(
        service: DownloadService,
    ) {
        downloadList.value = service.downloadList
    }

    fun filterDownloadList(
        list: List<BiliDownloadEntryAndPathInfo>,
        status: Int,
    ): List<DownloadInfo> {
        val result = mutableListOf<DownloadInfo>()
        list.filter {
            if (status == 1) {
                !it.entry.is_completed
            } else if (status == 2) {
                it.entry.is_completed
            } else {
                true
            }
        }.forEach {
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
                itemTitle = if (ep.index_title.isNotBlank()) {
                    ep.index_title
                } else {
                    ep.index
                }
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
            val last = result.lastOrNull()
            if (last != null
                && last.type == item.type
                && last.id == item.id
            ) {
                if (last.is_completed && !item.is_completed) {
                    last.is_completed = false
                }
                last.items.add(item)
            } else {
                result.add(
                    DownloadInfo(
                        dir_path = it.pageDirPath,
                        media_type = biliEntry.media_type,
                        has_dash_audio = biliEntry.has_dash_audio,
                        is_completed = biliEntry.is_completed,
                        total_bytes = biliEntry.total_bytes,
                        downloaded_bytes = biliEntry.downloaded_bytes,
                        title = biliEntry.title,
                        cover = biliEntry.cover,
                        cid = cid,
                        id = id,
                        type = type,
                        items = mutableListOf(item)
                    )
                )
            }
        }
        return result
    }

    fun toDetailPage(item: DownloadInfo) {
        composeNav.navigate(
            PageRoute.Download.detail.url(
                mapOf(
                    "path" to item.dir_path
                )
            )
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadListPage() {
    PageConfig(
        title = "下载列表"
    )
    val viewModel: DownloadListPageViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val bottomAppBarHeight = windowStore.bottomAppBarHeightDp

    var status by remember { mutableStateOf(0) }
    val downloadList by viewModel.downloadList.collectAsState()
    val curDownload by viewModel.curDownload.collectAsState()
    val list = remember(downloadList, status) {
        viewModel.filterDownloadList(downloadList, status)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = windowInsets.leftDp.dp, end = windowInsets.rightDp.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(windowInsets.topDp.dp))
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                FilterChip(
                    selected = status == 0,
                    onClick = {
                        status = 0
                    },
                    label = {
                        Text(text = "全部")
                    }
                )
                FilterChip(
                    selected = status == 1,
                    onClick = {
                        status = 1
                    },
                    label = {
                        Text(text = "下载中")
                    }
                )
                FilterChip(
                    selected = status == 2,
                    onClick = {
                        status = 2
                    },
                    label = {
                        Text(text = "下载完成")
                    }
                )
            }
        }
        items(
            list,
            key = { it.cid },
        ) {
            DownloadListItem(
                curDownload = curDownload,
                item = it,
                onClick = {
                    viewModel.toDetailPage(it)
                }
            )
        }
        item {
            Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp + bottomAppBarHeight.dp))
        }
    }

}

