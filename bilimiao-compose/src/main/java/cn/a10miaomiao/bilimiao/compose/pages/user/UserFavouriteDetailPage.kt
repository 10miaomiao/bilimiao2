package cn.a10miaomiao.bilimiao.compose.pages.user

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import bilibili.polymer.app.search.v1.Item
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.stringPageArg
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserFavouriteDetailContent

class UserFavouriteDetailPage : ComposePage() {

    val id = stringPageArg("id")
    val title = stringPageArg("title")
    val keyword = stringPageArg("keyword", "")


    override val route: String
        get() = "user/favourite/${id}?title=${title}&keyword=${keyword}"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val mediaId = navEntry.arguments?.get(id) ?: ""
        val mediaTitle = navEntry.arguments?.get(title) ?: ""
        val keyword = navEntry.arguments?.get(keyword) ?: ""
        UserFavouriteDetailPageContent(mediaId, mediaTitle, keyword)
    }
}

@Composable
private fun UserFavouriteDetailPageContent(
    mediaId: String,
    mediaTitle: String,
    keyword: String,
) {
    UserFavouriteDetailContent(
        mediaId = mediaId,
        mediaTitle = mediaTitle,
        keyword = keyword,
        showTowPane = false,
        hideFirstPane = false,
        onChangeHideFirstPane = {},
        onClose = {},
        onRefresh = {},
    )

}