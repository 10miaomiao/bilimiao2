package com.a10miaomiao.bilimiao.page.region

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class RegionViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment: Fragment by instance()
    private val myPage: MyPage by instance()
    private val timeSettingStore: TimeSettingStore by instance()

    val region = fragment.requireArguments().getParcelable<RegionInfo>(MainNavArgs.region)!!

    init {
        viewModelScope.launch {
            timeSettingStore.stateFlow.collect {
                myPage.pageConfig.notifyConfigChanged()
            }
        }
    }

    fun getTimeText(): String {
        val timeState = timeSettingStore.state
        return "${timeState.timeFrom.getValue("-")}\n至\n${timeState.timeTo.getValue("-")}"
    }

}