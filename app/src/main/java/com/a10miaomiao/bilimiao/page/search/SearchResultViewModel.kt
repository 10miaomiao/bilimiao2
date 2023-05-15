package com.a10miaomiao.bilimiao.page.search

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.page.search.result.BangumiResultFragment
import com.a10miaomiao.bilimiao.page.search.result.BaseResultFragment
import com.a10miaomiao.bilimiao.page.search.result.UpperResultFragment
import com.a10miaomiao.bilimiao.page.search.result.VideoResultFragment
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class SearchResultViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    val keyword by lazy { fragment.requireArguments().getString(MainNavArgs.text) }

    var position = -1
    val fragments by lazy {
        listOf<BaseResultFragment>(
            VideoResultFragment.newInstance(keyword),
            BangumiResultFragment.newInstance(keyword),
            UpperResultFragment.newInstance(keyword),
        )
    }

    val curFragment get() = if (position == -1) {
        null
    } else {
        fragments[position]
    }

}