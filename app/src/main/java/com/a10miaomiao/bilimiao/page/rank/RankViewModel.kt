package com.a10miaomiao.bilimiao.page.rank

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.store.RegionStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class RankViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment: Fragment by instance()
    private val myPage: MyPage by instance()
    private val regionStore: RegionStore by instance()

    var position = 0
    val tids = mutableListOf<Int>()
    val titles = mutableListOf<String>()

//    val type = fragment.requireArguments().getString(MainNavArgs.type)


    init {
        val regions = regionStore.state.regions
        tids.add(0)
        titles.add("全站")
        regions.forEach {
            tids.add(it.tid)
            titles.add(it.name)
        }
    }

}