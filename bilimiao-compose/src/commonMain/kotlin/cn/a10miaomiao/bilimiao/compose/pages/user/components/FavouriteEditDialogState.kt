package cn.a10miaomiao.bilimiao.compose.pages.user.components

internal sealed class FavouriteEditDialogState {
    data object Add : FavouriteEditDialogState()

    data class Update(
        val id: String,
        val cover: String,
        val title: String,
        val intro: String,
        val privacy: Int,
    ) : FavouriteEditDialogState()

    data class Delete(
        val id: String,
        val title: String,
    ) : FavouriteEditDialogState()
}
