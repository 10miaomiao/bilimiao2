package cn.a10miaomiao.bilimiao.compose.pages.user

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.stringPageArg
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserFavouriteDetailContent

class UserFavouriteDetailPage : ComposePage() {

    val id = stringPageArg("id")
    val title = stringPageArg("title")

    override val route: String
        get() = "user/favourite/${id}?title=${title}"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val mediaId = navEntry.arguments?.get(id) ?: ""
        val mediaTitle = navEntry.arguments?.get(title) ?: ""
        UserFavouriteDetailPageContent(mediaId, mediaTitle)
    }
}

@Composable
private fun UserFavouriteDetailPageContent(
    mediaId: String,
    mediaTitle: String,
) {

    UserFavouriteDetailContent(
        mediaId = mediaId,
        mediaTitle = mediaTitle,
        showTowPane = false,
        hideFirstPane = false,
        onChangeHideFirstPane = {},
        onClose = {},
        onRefresh = {},
    )

}