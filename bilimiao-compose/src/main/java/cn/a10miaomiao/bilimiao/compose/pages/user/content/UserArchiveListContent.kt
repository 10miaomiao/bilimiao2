package cn.a10miaomiao.bilimiao.compose.pages.user.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.video.VideoItemBox
import cn.a10miaomiao.bilimiao.compose.pages.user.UserArchiveViewModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.compose.rememberInstance

@Composable
fun UserArchiveListContent(
    viewModel: UserArchiveViewModel,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    LaunchedEffect(true) {
        viewModel.loadData("")
    }

    val listFlow = viewModel.list
    val list by listFlow.data.collectAsState()
    val listLoading by listFlow.loading.collectAsState()
    val listFinished by listFlow.finished.collectAsState()
    val listFail by listFlow.fail.collectAsState()

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(300.dp),
        contentPadding = windowInsets.toPaddingValues(
            top = 0.dp,
        )
    ) {
        items(list) {
            VideoItemBox(
                modifier = Modifier.padding(10.dp),
                title = it.title,
                pic = it.cover,
                playNum = it.play,
                damukuNum = it.danmaku,
                remark = NumberUtil.converCTime(it.ctime),
                duration = NumberUtil.converDuration(it.duration),
                onClick = {
                    viewModel.toVideoDetail(it)
                }
            )
        }
        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            ListStateBox(
                loading = listLoading,
                finished = listFinished,
                fail = listFail,
                listData = list,
            ) {
                viewModel.loadMore()
            }
        }
    }
}