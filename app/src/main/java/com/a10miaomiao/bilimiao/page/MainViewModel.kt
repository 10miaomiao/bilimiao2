package com.a10miaomiao.bilimiao.page

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.page.home.PopularFragment
import com.a10miaomiao.bilimiao.page.home.HomeFragment
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class MainViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    var position = -1
}