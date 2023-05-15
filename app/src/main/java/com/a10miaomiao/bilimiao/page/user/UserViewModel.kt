package com.a10miaomiao.bilimiao.page.user


import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.apis.UserApi
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo
import com.a10miaomiao.bilimiao.comm.entity.user.SpaceInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UpperChannelInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class UserViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val activity: AppCompatActivity by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()
    val filterStore: FilterStore by instance()
    private val myPage: MyPage by instance()

    var id: String

    var loading = false
    var dataInfo: SpaceInfo? = null
    var channelList = listOf<UpperChannelInfo>()

    var noLike = false // 不喜欢，是否屏蔽

    val isSelf get() = userStore.isSelf(id)

    val isFiltered get() = !filterStore.filterUpper(id)

    val isFollow get() = dataInfo?.card?.relation?.is_follow == 1

    init {
        id = fragment.requireArguments().getString(MainNavArgs.id, "")
        if (id.isBlank() && userStore.isLogin()) {
            id = userStore.state.info?.mid?.toString() ?: ""
        }
        if (id.isBlank()) {
            activity.toast("请先登录")
        } else {
            loadData()
        }
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                loading = true
            }
            val res = UserApi().space(id).awaitCall().gson<ResultInfo<SpaceInfo>>()
            if (res.code == 0) {
                ui.setState {
                    dataInfo = res.data
                }
            } else {
                withContext(Dispatchers.Main) {
                    activity.toast(res.message)
                }
            }
            val res2 = UserApi().upperChanne(id).awaitCall().gson<ResultListInfo<UpperChannelInfo>>()
            if (res.code == 0) {
                ui.setState {
                    channelList = res2.data
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                activity.toast("网络错误")
            }
            e.printStackTrace()
        } finally {
            ui.setState {
                loading = false
            }
            withContext(Dispatchers.Main) {
                myPage.pageConfig.notifyConfigChanged()
            }
        }
    }

    fun filterUpperDelete () {
        filterStore.deleteUpper(id.toLong())
    }

    fun filterUpperAdd () {
        val info = dataInfo
        if (info == null) {
            activity.toast("请等待信息加载完成")
        } else {
            filterStore.addUpper(
                info.card.mid.toLong(),
                info.card.name,
            )
        }
    }

    fun logout() {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle("确定退出登录，喵？")
            setNegativeButton("确定退出") { dialog, which ->
                userStore.logout()
                val nav = activity.findNavController(R.id.nav_host_fragment)
                nav.popBackStack()
                activity.toast("已退出登录了喵")
            }
            setPositiveButton("取消", null)
        }.show()
    }

    fun attention() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val detailInfo = dataInfo ?: return@launch
            val mode = if (isFollow) { 2 } else { 1 }
            val res = BiliApiService.userApi
                .attention(id, mode)
                .awaitCall().gson<MessageInfo>()
            if (res.code == 0) {
                detailInfo.card.relation.is_follow = 2 - mode
                withContext(Dispatchers.Main) {
                    myPage.pageConfig.notifyConfigChanged()
                    activity.toast(if (mode == 1) {
                        "关注成功"
                    } else {
                        "已取消关注"
                    })
                }
            } else {
                withContext(Dispatchers.Main) {
                    activity.toast(res.message)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                activity.toast("网络错误")
            }
            e.printStackTrace()
        }
    }

}