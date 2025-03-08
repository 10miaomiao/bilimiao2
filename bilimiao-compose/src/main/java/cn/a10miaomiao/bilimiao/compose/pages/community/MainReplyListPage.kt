package cn.a10miaomiao.bilimiao.compose.pages.community

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.community.ReplyItemBox
import cn.a10miaomiao.bilimiao.compose.components.dyanmic.DynamicModuleBox
import cn.a10miaomiao.bilimiao.compose.components.layout.DataDrivenNavigator
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.pages.community.content.ReplyDetailContent
import cn.a10miaomiao.bilimiao.compose.pages.community.content.ReplyListContent
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.compose.rememberInstance

@Serializable
class MainReplyListPage(
    val oid: String,
    val type: Int,
    val enterUrl: String = "",
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: MainReplyViewModel = diViewModel(
            key = oid,
        ) {
            MainReplyViewModel(it, oid, type)
        }
        MainReplyListPageContent(
            headerContent = {

            },
            viewModel = viewModel,
            pageTitle = "评论列表",
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainReplyListPageContent(
    headerContent: LazyListScope.() -> Unit,
    pageTitle: String,
    viewModel: MainReplyViewModel,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val currentReply by viewModel.currentReply.collectAsState()

    BackHandler(
        enabled = currentReply != null
    ) {
        viewModel.clearCurrentReply()
    }

    DataDrivenNavigator(
        data = currentReply,
        dataKey = { it.id },
        dataContent = { data ->
            ReplyDetailContent(
                reply = data,
                innerPadding = windowInsets.toPaddingValues(),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onCloseClick = {
                    viewModel.clearCurrentReply()
                },
                onLikeReply = viewModel::likeReply,
                onDeletedReply = viewModel::removeReplyItem,
                usePageConfig = true,
            )
        },
    ) {
        ReplyListContent(
            headerContent = headerContent,
            viewModel = viewModel,
            innerPadding = windowInsets.toPaddingValues(),
            sharedTransitionScope = sharedTransitionScope,
            usePageConfig = true,
            pageTitle = pageTitle,
            animatedVisibilityScope = animatedVisibilityScope,
        )
    }
}