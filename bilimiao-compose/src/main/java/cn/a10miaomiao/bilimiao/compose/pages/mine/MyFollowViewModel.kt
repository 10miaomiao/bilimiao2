package cn.a10miaomiao.bilimiao.compose.pages.mine

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.pages.user.FollowingItemInfo
import cn.a10miaomiao.bilimiao.compose.pages.user.FollowingListAction
import cn.a10miaomiao.bilimiao.compose.pages.user.SearchFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.user.TagEditDialogState
import cn.a10miaomiao.bilimiao.compose.pages.user.TagInfo
import cn.a10miaomiao.bilimiao.compose.pages.user.UserTagSetDialogState
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

internal class MyFollowViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val pageNavigation by instance<PageNavigation>()
    private val activity by instance<Activity>()
    private val userStore by instance<UserStore>()

    val count = MutableStateFlow(1)
    val isRefreshing = MutableStateFlow(false)
    val tagList = FlowPaginationInfo<TagInfo>()

    val listActionFlow = MutableSharedFlow<FollowingListAction>()
    val tagEditDialogState = MutableStateFlow<TagEditDialogState?>(null)
    val userTagSetDialogState = MutableStateFlow<UserTagSetDialogState?>(null)

    val orderType = MutableStateFlow("attention")
    val orderTypeToNameMap = mapOf(
        "attention" to "最常访问",
        "" to "关注顺序",
    )

    init {
        loadData()
    }

    fun loadData(
        pageNum: Int = tagList.pageNum
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            tagList.loading.value = true
            val res = BiliApiService.userRelationApi
                .tags()
                .awaitCall()
                .json<ResponseData<List<TagInfo>>>()
            if (res.isSuccess) {
                tagList.pageNum = pageNum
                tagList.data.value = res.requireData()
            } else {
                tagList.fail.value = res.message
            }
        } catch (e: Exception) {
            tagList.fail.value = "无法连接到御坂网络"
        } finally {
            tagList.loading.value = false
            isRefreshing.value = false
        }
    }

    fun tryAgainLoadData() = loadData()

    suspend fun addTag(name: String): Boolean {
        try {
            val res = BiliApiService.userRelationApi
                .tagCreate(name)
                .awaitCall()
                .json<MessageInfo>()
            if (res.isSuccess) {
                PopTip.show("创建成功")
                clearTagEditDialogState()
                loadData()
                return true
            } else {
                PopTip.show(res.message)
                return false
            }
        } catch (e: Exception) {
            PopTip.show("无法连接到御坂网络")
            return false
        }
    }

    suspend fun updateTag(
        tagId: Int,
        tagName: String
    ): Boolean {
        try {
            val res = BiliApiService.userRelationApi
                .tagUpdate(tagId, tagName)
                .awaitCall()
                .json<MessageInfo>()
            if (res.isSuccess) {
                PopTip.show("修改成功")
                clearTagEditDialogState()
                loadData()
                return true
            } else {
                PopTip.show(res.message)
                return false
            }
        } catch (e: Exception) {
            PopTip.show("无法连接到御坂网络")
            return false
        }
    }

    fun deleteTag(tagId: Int) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.userRelationApi
                .tagDelete(tagId)
                .awaitCall()
                .json<MessageInfo>()
            if (res.isSuccess) {
                PopTip.show("删除成功")
                loadData()
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            PopTip.show("无法连接到御坂网络")
        }
    }

    fun addUserTags(
        user: FollowingItemInfo,
        tagIds: List<Int>,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = BiliApiService.userRelationApi
                .addUsers(
                    fids = listOf(user.mid),
                    tagIds = tagIds,
                )
                .awaitCall()
                .json<MessageInfo>()
            if (res.isSuccess) {
                PopTip.show("操作成功")
                // 刷新分组数量
                loadData()
                // 分组列表操作
                val originTagIds = user.tag ?: listOf(0)
                val updateItem = user.copy(
                    tag = tagIds
                )
                val deleteTagIds = originTagIds.filter {
                    tagIds.indexOf(it) == -1
                }
                if (deleteTagIds.isNotEmpty()) {
                    listActionFlow.emit(
                        FollowingListAction.DeleteItem(
                            tagIds = deleteTagIds,
                            item = updateItem,
                        )
                    )
                }
                val addTagIds = tagIds.filter {
                    originTagIds.indexOf(it) == -1
                }
                if (addTagIds.isNotEmpty()) {
                    listActionFlow.emit(
                        FollowingListAction.AddItem(
                            tagIds = addTagIds,
                            item = updateItem,
                        )
                    )
                }
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            PopTip.show("无法连接到御坂网络")
        }
    }

    fun refresh() {
        isRefreshing.value = true
        tagList.finished.value = false
        tagList.fail.value = ""
        loadData(1)
    }

    fun toSearchPage() {
        pageNavigation.navigate(SearchFollowPage())
    }

    fun changeOrderType(value: String) {
        if (orderType.value != value) {
            orderType.value = value
        }
    }

    fun updateTagEditDialogState(
        state: TagEditDialogState,
    ) {
        tagEditDialogState.value = state
    }

    fun clearTagEditDialogState() {
        tagEditDialogState.value = null
    }

    fun updateUserTagSetDialogState(
        state: UserTagSetDialogState,
    ) {
        userTagSetDialogState.value = state
    }

    fun clearUserTagSetDialogState() {
        userTagSetDialogState.value = null
    }
}