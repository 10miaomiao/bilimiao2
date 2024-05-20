package cn.a10miaomiao.bilimiao.compose.pages.user

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.base.navigate
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.navigation.findComposeNavController
import cn.a10miaomiao.bilimiao.compose.pages.user.poup_menu.MyFollowMorePopupMenu
import cn.a10miaomiao.bilimiao.compose.pages.user.poup_menu.UserFollowOrderPopupMenu
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.navigation.currentOrSelf
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
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
                .gson<ResultInfo<List<TagInfo>>>()
            if (res.isSuccess) {
                tagList.pageNum = pageNum
                tagList.data.value = res.data
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
                .gson<MessageInfo>()
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
                .gson<MessageInfo>()
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
                .gson<MessageInfo>()
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
                .gson<MessageInfo>()
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
        fragment.findComposeNavController().currentOrSelf()
            .navigate(SearchFollowPage())
    }

    fun showOrderPopupMenu(view: View) {
        val pm = UserFollowOrderPopupMenu(
            activity,
            view,
            checkedValue = orderType.value
        )
        pm.setOnMenuItemClickListener {
            it.isChecked = true
            val value = arrayOf("attention", "")[it.itemId]
            if (value != orderType.value) {
                orderType.value = value
            }
            false
        }
        pm.show()
    }

    fun showMorePopupMenu(view: View, currentPage: Int) {
        val tagListData = tagList.data.value
        if (currentPage in tagListData.indices) {
            val tagInfo = tagListData[currentPage]
            val pm = MyFollowMorePopupMenu(activity, tagInfo, this)
            pm.show(view)
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