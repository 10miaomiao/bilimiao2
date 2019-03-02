package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModel
import android.support.v4.app.Fragment

class MainViewModel : ViewModel() {
    val homeFragment = HomeFragment()
    val rankFragment = RankFragment()
    val dowmloadFragment = DowmloadFragment()
    val filterFragment = FilterFragment()

    var currentFragment: Fragment? = null
}