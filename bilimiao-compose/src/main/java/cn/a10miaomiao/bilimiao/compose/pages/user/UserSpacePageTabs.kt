package cn.a10miaomiao.bilimiao.compose.pages.user

import androidx.compose.runtime.Composable
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserArchiveListContent
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserDynamicListContent
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserSpaceIndexContent

sealed class UserSpacePageTabs(
    val id: Int,
    val name: String,
) {
    @Composable
    abstract fun PageContent()

    data class Index(
        val viewModel: UserSpaceViewModel,
    ) : UserSpacePageTabs(
        id = 0,
        name = "主页"
    ) {
        @Composable
        override fun PageContent() {
            UserSpaceIndexContent(viewModel)
        }
    }

    data class Dynamic(
        val vmid: String,
    ) : UserSpacePageTabs(
        id = 1,
        name = "动态"
    ) {
        @Composable
        override fun PageContent() {
            UserDynamicListContent(vmid)
        }
    }

    data class Archive(
        val vmid: String,
    ) : UserSpacePageTabs(
        id = 2,
        name = "投稿"
    ) {
        @Composable
        override fun PageContent() {
            UserArchiveListContent(vmid);
        }
    }

}