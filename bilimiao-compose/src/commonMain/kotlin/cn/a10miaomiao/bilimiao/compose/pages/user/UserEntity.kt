package cn.a10miaomiao.bilimiao.compose.pages.user

import kotlinx.serialization.Serializable

@Serializable
internal data class FollowingItemInfo(
    val mid: String,
    val attribute: Int, // 关注属性: 0：未关注, 2：已关注, 6：已互粉
    val mtime: Long = 0L,
    val special: Int, // 特别关注标志: 0：否, 1：是
    val uname: String,
    val face: String,
    val sign: String,
    val face_nft: Int,
    val nft_icon: String,
    val tag: List<Int>?,
) {
    val isFollowing get() = attribute == 2 || attribute == 6
}

@Serializable
internal data class FollowingsInfo(
    val list: List<FollowingItemInfo>,
    val re_version: Int = 0,
    val total: Int,
)

/**
 * 分组信息
 */
@Serializable
internal data class TagInfo(
    val tagid: Int,
    val name: String,
    val count: Int,
    val tip: String,
)

@Serializable
internal data class InterrelationInfo(
    val attribute: Int = 0, // 关注属性: 0：未关注, 2：已关注, 6：已互粉
    val is_followed: Boolean = false,
    val mid: String = "",
    val mtime: Long = 0L,
    val special: Int = 0, // 特别关注标志: 0：否, 1：是
    val tag: List<Int>? = null,
)

internal sealed class FollowingListAction {
    data class UpdateList(
        val tagIds: List<Int>,
    ): FollowingListAction()

    data class AddItem(
        val tagIds: List<Int>,
        val item: FollowingItemInfo,
    ): FollowingListAction()

    data class DeleteItem(
        val tagIds: List<Int>,
        val item: FollowingItemInfo,
    ): FollowingListAction()

    data class UpdateCount(
        val tagId: Int,
        val count: Int,
    ): FollowingListAction()
}

internal sealed class TagEditDialogState {
    data object Add: TagEditDialogState()

    data class Update(
        val id: Int,
        val name: String,
    ): TagEditDialogState()
}

/**
 * 用户分组设置对话框状态
 */
internal data class UserTagSetDialogState(
    val user: FollowingItemInfo,
    val formTagId: Int,
)

enum class UserFavouriteFolderType {
    Created,
    Collected,
}