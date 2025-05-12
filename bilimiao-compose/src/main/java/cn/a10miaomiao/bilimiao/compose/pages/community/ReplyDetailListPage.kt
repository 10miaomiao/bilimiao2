
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import bilibili.app.view.v1.ViewReply
import bilibili.main.community.reply.v1.ReplyGRPC
import bilibili.main.community.reply.v1.ReplyInfo
import bilibili.main.community.reply.v1.ReplyInfoReq
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.localPageNavigation
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.status.BiliFailBox
import cn.a10miaomiao.bilimiao.compose.components.status.BiliLoadingBox
import cn.a10miaomiao.bilimiao.compose.pages.community.content.ReplyDetailContent
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class ReplyDetailListPage(
    val id: String,
    val enterUrl: String = "", // 评论来源,
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: ReplyDetailListPageViewModel = diViewModel(
            key = id,
        ) {
            ReplyDetailListPageViewModel(it, id.toLong())
        }
        ReplyDetailListPageContent(viewModel, enterUrl)
    }
}

private class ReplyDetailListPageViewModel(
    override val di: DI,
    val rootId: Long
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading
    private val _fail = MutableStateFlow<Any?>(null)
    val fail: StateFlow<Any?> get() = _fail
    private val _detailData = MutableStateFlow<ReplyInfo?>(null)
    val detailData: StateFlow<ReplyInfo?> get() = _detailData

    init {
        loadData()
    }

    private fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            _loading.value = true
            _fail.value = null
            val req = ReplyInfoReq(
                rpid = rootId,
                scene = 1,
            )
            val res = BiliGRPCHttp.request {
                ReplyGRPC.replyInfo(req)
            }.awaitCall()
            _detailData.value = res.reply
        } catch (e: Exception) {
            e.printStackTrace()
            _fail.value = e
        } finally {
            _loading.value = false
        }
    }

    fun likeReply() {
        val item = detailData.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val isLike = item.replyControl?.action == 1L
                val newAction = if (isLike) 0 else 1
                val res = BiliApiService.commentApi
                    .action(1, item.oid.toString(), item.id.toString(), newAction)
                    .awaitCall()
                    .json<MessageInfo>()
                if (res.isSuccess) {
                    val likeNum = if (isLike) item.like - 1 else item.like + 1
                    val newItem = item.copy(
                        replyControl = item.replyControl?.copy(
                            action = newAction.toLong(),
                        ),
                        like = likeNum,
                    )
                    _detailData.value = newItem
                } else {
                    PopTip.show(res.message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                PopTip.show("喵喵被搞坏了:" + e.message ?: e.toString())
            }
        }
    }

    fun popBackStack() {
        pageNavigation.popBackStack()
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ReplyDetailListPageContent(
    viewModel: ReplyDetailListPageViewModel,
    enterUrl: String = "",
) {
    val pageNavigation: PageNavigation by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val detailData = viewModel.detailData.collectAsState().value
    val loading = viewModel.loading.collectAsState().value
    val fail = viewModel.fail.collectAsState().value

    val parentId = detailData?.parent ?: 0L

    val configId = PageConfig(
        title = "评论回复详情",
        menu = rememberMyMenu(enterUrl, detailData?.parent) {
            if (enterUrl.isNotEmpty()) {
                myItem {
                    key = MenuKeys.url
                    iconFileName = "ic_link_black_24dp"
                    title = "评论来源"
                }
            }
            if (parentId != 0L) {
                myItem {
                    key = MenuKeys.parent
                    iconFileName = "ic_link_black_24dp"
                    title = "上级评论"
                }
            }
        }
    )
    PageListener(
        configId = configId,
        onMenuItemClick = { _, menuItem ->
            when (menuItem.key) {
                MenuKeys.parent -> {
                    pageNavigation.navigate(ReplyDetailListPage(
                        id = parentId.toString(),
                        enterUrl = enterUrl
                    ))
                }
                MenuKeys.url -> {
                    pageNavigation.navigateByUri(Uri.parse(enterUrl))
                }
            }

        }
    )

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = detailData == null,
        label = "VideoDetailPage",
        transitionSpec = {
            // Follow M3 Clean fades
            val fadeIn = fadeIn(
                tween(),
            )
            val fadeOut = fadeOut()
            fadeIn.togetherWith(fadeOut)
        }
    ) {
        if (it || detailData == null) {
            if (loading) {
                BiliLoadingBox(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(windowInsets.toPaddingValues())
                )
            } else if (fail != null) {
                BiliFailBox(
                    e = fail,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(windowInsets.toPaddingValues())
                )
            }
        } else {
            ReplyDetailContent(
                reply = detailData,
                innerPadding = windowInsets.toPaddingValues(),
                onCloseClick = {
                    viewModel.popBackStack()
                },
                onLikeReply = {
                    viewModel.likeReply()
                },
                onDeletedReply = {
                    viewModel.popBackStack()
                }
            )
        }
    }


}