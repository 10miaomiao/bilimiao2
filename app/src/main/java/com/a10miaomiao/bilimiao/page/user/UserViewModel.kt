package com.a10miaomiao.bilimiao.page.user

import android.content.Context
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.apis.UserApi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo
import com.a10miaomiao.bilimiao.comm.entity.user.SpaceInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UpperChannelInfo
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.store.UserStore
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

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavGraph.args.id, "") }

    var scrollY = 0
    var loading = false
    var dataInfo: SpaceInfo? = null
    var channelList = listOf<UpperChannelInfo>()

    var noLike = false // 不喜欢，是否屏蔽

    val isSelf get() = userStore.isSelf(id)

    init {
        loadData()
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                loading = true
            }
            val res = UserApi().space(id).call().gson<ResultInfo<SpaceInfo>>()
            if (res.code == 0) {
                ui.setState {
                    dataInfo = res.data
                }
            } else {
                withContext(Dispatchers.Main) {
                    context.toast(res.message)
                }
            }
            val res2 = UserApi().upperChanne(id).call().gson<ResultListInfo<UpperChannelInfo>>()
            if (res.code == 0) {
                ui.setState {
                    channelList = res2.data
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                context.toast("网络错误")
            }
            e.printStackTrace()
        } finally {
            ui.setState {
                loading = false
            }
        }
    }

    val handleScrollChange = NestedScrollView.OnScrollChangeListener { _, x, y, ox, oy ->
        scrollY = y
    }

}