package cn.a10miaomiao.bilimiao.compose.pages.message

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.commponents.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.commponents.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.message.commponents.MessageItemBox
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.message.AtMessageInfo
import com.a10miaomiao.bilimiao.comm.entity.message.LikeMessageInfo
import com.a10miaomiao.bilimiao.comm.entity.message.LikeMessageResponseInfo
import com.a10miaomiao.bilimiao.comm.entity.message.MessageCursorInfo
import com.a10miaomiao.bilimiao.comm.entity.message.MessageResponseInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

internal class LikeMessagePageModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<LikeMessageInfo>()
    var _cursor: MessageCursorInfo? = null

    init {
        loadData()
    }

    fun loadData(
        id: Long = 0L,
        time: Long = 0L,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            list.loading.value = true
            val res = BiliApiService.messageApi
                .like(id, time)
                .awaitCall()
                .gson<ResultInfo<LikeMessageResponseInfo>>()
            if (res.isSuccess) {
                val total = res.data.total
                _cursor = total.cursor
                if (id == 0L) {
                    list.data.value = total.items
                } else {
                    list.data.value = mutableListOf<LikeMessageInfo>().apply {
                        addAll(list.data.value)
                        addAll(total.items)
                    }
                }
                list.finished.value = total.items.isEmpty()
            } else {
                list.fail.value = res.message
            }
        } catch (e: Exception) {
            list.fail.value = "无法连接到御坂网络"
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    fun loadMore() {
        if (
            !list.finished.value &&
            !list.loading.value
        ) {
            _cursor?.let {
                loadData(it.id, it.time)
            }
        }
    }

    fun refresh() {
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
        _cursor = null
        loadData()
    }
}


@Composable
fun LikeMessagePage() {
    val viewModel: LikeMessagePageModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    SwipeToRefresh(
        modifier = Modifier.padding(
            start = windowInsets.leftDp.dp,
            end = windowInsets.rightDp.dp,
        ),
        refreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
    ) {
        LazyColumn() {
            items(list.size, { list[it].id }) {
                val item = list[it]
                Column() {
                    if (it != 0) {
                        Divider()
                    }
                    val business = item.item.business
                    val (nickname, actionText) = when(item.users.size) {
                        0 -> Pair("", "零人赞了我的${business}")
                        1 ->  Pair(
                            item.users[0].nickname,
                            "赞了我的${business}"
                        )
                        2 ->  Pair(
                            "${item.users[0].nickname}、${item.users[1].nickname}",
                            "赞了我的${business}"
                        )
                        else -> Pair(
                            "${item.users[0].nickname}、${item.users[1].nickname}",
                            "等总计${item.counts}人赞了我的${business}"
                        )
                    }
                    MessageItemBox(
                        avatar = item.users[0]?.avatar ?: "",
                        nickname = nickname,
                        actionText = actionText,
                        title = item.item.title,
                        sourceContent = "",
                        time = item.like_time,
                    )
                }
            }
            item() {
                ListStateBox(
                    modifier = Modifier.padding(
                        bottom = windowInsets.bottomDp.dp
                    ),
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
}