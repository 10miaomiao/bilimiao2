package cn.a10miaomiao.bilimiao.compose.pages.message.content


import ReplyDetailListPage
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.list.SwipeToRefresh
import cn.a10miaomiao.bilimiao.compose.pages.message.components.MessageItemBox
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpacePage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.message.MessageCursorInfo
import com.a10miaomiao.bilimiao.comm.entity.message.MessageResponseInfo
import com.a10miaomiao.bilimiao.comm.entity.message.ReplyMessageInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.MessageStore
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

private class ReplyMessageContentModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()
    private val messageStore by instance<MessageStore>()

    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<ReplyMessageInfo>()
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
                .reply(id, time)
                .awaitCall()
                .json<ResultInfo<MessageResponseInfo<ReplyMessageInfo>>>()
            if (res.isSuccess) {
                messageStore.clearReplyUnread()
                _cursor = res.data.cursor
                if (id == 0L) {
                    list.data.value = res.data.items
                } else {
                    list.data.value = mutableListOf<ReplyMessageInfo>().apply {
                        addAll(list.data.value)
                        addAll(res.data.items)
                    }
                }
                list.finished.value = res.data.items.isEmpty()
            } else {
                list.fail.value = res.message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
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

    fun toUserPage(item: ReplyMessageInfo) {
        val mid = item.user.mid
        pageNavigation.navigate(UserSpacePage(mid.toString()))
    }

    fun toDetailPage(item: ReplyMessageInfo, isDetail: Boolean) {
        val type = item.item.type
        if (type == "reply") {
            // 评论
            val rootId = item.item.root_id
            val sourceId = item.item.source_id
            var enterUrl = ""
            if (item.item.business_id == 1) {
                val videoPageUrl = "bilimiao://video/${item.item.subject_id}"
                enterUrl= videoPageUrl
            }
            if (isDetail) {
                pageNavigation.navigate(ReplyDetailListPage(
                    id = rootId.toString(),
                    enterUrl = enterUrl,
                ))
            } else {
                pageNavigation.navigate(ReplyDetailListPage(
                    id = sourceId.toString(),
                    enterUrl = enterUrl,
                ))
            }
        } else if (type == "album") {
//            // 动态
        } else if (type == "danmu") {
            // 弹幕
            val aid = item.item.subject_id
            pageNavigation.navigateToVideoInfo(aid.toString())
        } else if (type == "video") {
            // 视频
            val aid = item.item.subject_id
            val sourceId = item.item.source_id
            if (isDetail) {
                pageNavigation.navigateToVideoInfo(aid.toString())
            } else {
                val videoPageUrl = "bilimiao://video/$aid"
                pageNavigation.navigate(ReplyDetailListPage(
                    id = sourceId.toString(),
                    enterUrl = videoPageUrl,
                ))
            }
        } else {
            BilibiliNavigation.navigationTo(pageNavigation, item.item.uri)
        }
    }
}

@Composable
internal fun ReplyMessageContent() {
    val viewModel: ReplyMessageContentModel = diViewModel()
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
                        HorizontalDivider()
                    }
                    MessageItemBox(
                        avatar = item.user.avatar,
                        nickname = item.user.nickname,
                        actionText = "回复了我的${item.item.business}",
                        title = item.item.title,
                        sourceContent = item.item.source_content,
                        time = item.reply_time,
                        onUserClick = {
                            viewModel.toUserPage(item)
                        },
                        onDetailClick = {
                            viewModel.toDetailPage(item, true)
                        },
                        onMessageClick = {
                            viewModel.toDetailPage(item, false)
                        }
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