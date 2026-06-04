package cn.a10miaomiao.bilimiao.compose.pages.dynamic

import androidx.compose.runtime.Composable
import cn.a10miaomiao.bilimiao.compose.common.constant.PageTabIds
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.content.DynamicAllListContent
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.content.DynamicVideoListContent

sealed class DynamicPageTab(
    val id: String,
    val name: String,
) {
    @Composable
    abstract fun PageContent(
        viewModel: DynamicViewModel,
    )

    data object All : DynamicPageTab(
        id = PageTabIds.DynamicAll,
        name = "全部"
    ) {
        @Composable
        override fun PageContent(
            viewModel: DynamicViewModel,
        ) {
            DynamicAllListContent(viewModel)
        }
    }

    data object Video : DynamicPageTab(
        id = PageTabIds.DynamicVideo,
        name = "视频"
    ) {
        @Composable
        override fun PageContent(
            viewModel: DynamicViewModel,
        ) {
            DynamicVideoListContent()
        }
    }

}