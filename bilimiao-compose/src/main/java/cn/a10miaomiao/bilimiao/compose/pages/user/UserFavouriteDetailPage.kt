package cn.a10miaomiao.bilimiao.compose.pages.user

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import bilibili.polymer.app.search.v1.Item
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserFavouriteDetailContent
import kotlinx.serialization.Serializable

@Serializable
data class UserFavouriteDetailPage(
    private val id: String,
    private val title : String,
    private val keyword: String = "",
) : ComposePage() {

    @Composable
    override fun Content() {
        UserFavouriteDetailPageContent(id, title, keyword)
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