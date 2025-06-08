package cn.a10miaomiao.bilimiao.compose.pages.dynamic

import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import bilibili.app.archive.middleware.v1.PlayerArgs
import bilibili.app.dynamic.v2.Module.ModuleItem
import bilibili.app.dynamic.v2.OpusDetailReq
import bilibili.app.dynamic.v2.OpusGRPC
import bilibili.app.dynamic.v2.OpusItem
import bilibili.app.dynamic.v2.Paragraph
import bilibili.app.dynamic.v2.PicParagraph
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.community.ReplyItemBox
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicModuleBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.status.BiliFailBox
import cn.a10miaomiao.bilimiao.compose.components.status.BiliLoadingBox
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyListPageContent
import cn.a10miaomiao.bilimiao.compose.pages.community.MainReplyViewModel
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.store.WindowStore.Insets
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
data class DynamicOpusPage(
    private val id: String,
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel = diViewModel(key = "dynamic$id") {
            DynamicOpusPageViewModel(it, id)
        }
        DynamicOpusPageContent(viewModel)
    }

}

private class DynamicOpusPageViewModel(
    override val di: DI,
    val dynId: String,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    val activity: AppCompatActivity by instance()
    val userStore: UserStore by instance()

    private val _loading = MutableStateFlow(false);
    val loading: StateFlow<Boolean> get() = _loading

    private val _fail = MutableStateFlow<Any?>(null)
    val fail: StateFlow<Any?> get() = _fail

    private val _detailData = MutableStateFlow<OpusItem?>(null)
    val detailData: StateFlow<OpusItem?> get() = _detailData
    init {
        if (dynId.isNotBlank()) {
            loadData()
        }
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            _loading.value = true
            _fail.value = null
            val req = OpusDetailReq(
                oid = dynId.toLong(),
                shareId = "dt.opus-detail.0.0.pv",
                shareMode = 3,
                localTime = 8,
                playerArgs = PlayerArgs(
                    qn = 32,
                    fnval = 400,
                )
            )
            val res = BiliGRPCHttp.request {
                OpusGRPC.opusDetail(req)
            }.awaitCall()
            _detailData.value = res.opusItem
            res.opusItem?.modules?.forEach {
                miaoLogger() debug it.moduleItem!!::class.simpleName
                miaoLogger() debug it.moduleItem
            }
        } catch (e: Exception) {
            _fail.value = e
            PopTip.show("网络错误")
            e.printStackTrace()
        } finally {
            _loading.value = false
        }
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        when (item.key) {
        }
    }
}


@Composable
private fun DynamicOpusPageContent(
    viewModel: DynamicOpusPageViewModel
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val detailData = viewModel.detailData.collectAsState().value

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = detailData == null,
        label = "DynamicDetailPageContent",
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
            DynamicDetailPageLoadingContent(
                loading = viewModel.loading.collectAsState().value,
                fail = viewModel.fail.collectAsState().value,
                innerPadding = windowInsets.toPaddingValues()
            )
        } else {
            DynamicDetailPageDetailContent(
                viewModel = viewModel,
                windowInsets = windowInsets,
                detailData = detailData,
            )
        }
    }
}

@Composable
private fun DynamicDetailPageLoadingContent(
    loading: Boolean,
    fail: Any?,
    innerPadding: PaddingValues,
) {
    PageConfig(
        title = "动态详情"
    )
    if (loading) {
        BiliLoadingBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    } else if (fail != null) {
        BiliFailBox(
            e = fail,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
private fun DynamicDetailPageDetailContent(
    viewModel: DynamicOpusPageViewModel,
    windowInsets: Insets,
    detailData: OpusItem,
) {
    val oid = detailData.oid.toString()
    val replyViewModel = diViewModel(
        key = "dynamic.reply.${oid}"
    ) {
        MainReplyViewModel(it, oid, type = 11)
    }
    val replyList by replyViewModel.list.data.collectAsState()
    val replyListLoading by replyViewModel.list.loading.collectAsState()
    val replyListFinished by replyViewModel.list.finished.collectAsState()
    val replyListFail by replyViewModel.list.fail.collectAsState()
    val upMid = replyViewModel.upMid
//    val replyRefreshing by replyViewModel.isRefreshing.collectAsState()

    val origName = detailData.extend?.origName
    PageConfig(
        title =  origName?.let {
            "${it}\n的\n动态详情"
        } ?: "动态详情"
    )
    val hasTopBigImages = remember(detailData) {
        detailData.modules.slice(0..2).indexOfFirst {
            val moduleItem = it.moduleItem
            if (moduleItem is ModuleItem.ModuleParagraph) {
                val content = moduleItem.value.paragraph?.content
                return@indexOfFirst content is Paragraph.Content.Pic &&
                        content.value.style.value == PicParagraph.PicParagraphStyle.BIG_SCROLL.value
            }
            return@indexOfFirst false
        } != -1
    }
    val topModule = remember(detailData) {
        detailData.modules.firstOrNull()?.let {
            val moduleItem = it.moduleItem
            if (moduleItem is ModuleItem.ModuleTop) moduleItem.value
            else null
        }
    }
    val buttomModule = remember(detailData) {
        detailData.modules.lastOrNull()?.let {
            val moduleItem = it.moduleItem
            if (moduleItem is ModuleItem.ModuleButtom) moduleItem.value
            else null
        }
    }
    MainReplyListPageContent(
        viewModel = replyViewModel,
        pageTitle = origName?.let {
            "${it}\n的\n动态详情"
        } ?: "动态详情",
        headerContent = {
            item {
                Column(
                    modifier = Modifier
                        .run {
                            if (hasTopBigImages) this
                            else padding(top = windowInsets.topDp.dp)
                        }
                        .padding(bottom = 5.dp),
                ) {
                    for(module in detailData.modules) {
                        DynamicModuleBox(module = module)
                    }
//                topModule?.author?.let {
//                    Text(
//                        it.ptimeLocationText,
//                        style = MaterialTheme.typography.labelMedium,
//                        color = MaterialTheme.colorScheme.outline,
//                        modifier = Modifier
//                            .padding(top = 10.dp)
//                            .padding(horizontal = 10.dp)
//                    )
//                }
                }
            }
            item {
                val moduleStat = buttomModule?.moduleStat
                Text(
                    modifier = Modifier
                        .padding(
                            top = 10.dp,
                            bottom = 5.dp,
                            start = 10.dp,
                            end = 10.dp,
                        ),
                    text = if (moduleStat == null) "全部评论"
                    else "全部评论(${moduleStat.reply})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                HorizontalDivider()
            }
        }
    )
}