package com.a10miaomiao.bilimiao.page.rank

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.store.RegionStore
import com.a10miaomiao.bilimiao.store.TimeSettingStore
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
    var fragments: Array<Fragment?>

//    val type = fragment.requireArguments().getString(MainNavGraph.args.type)


    init {
        val regions = regionStore.state.regions
        tids.add(0)
        titles.add("全站")
        regions.forEach {
            tids.add(it.tid)
            titles.add(it.name)
        }
        fragments = Array(tids.size) { null }
    }

}