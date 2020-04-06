package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModel
import android.support.v4.app.Fragment
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.miaoandriod.MiaoLiveData

class MainViewModel : ViewModel() {
    var checkedMenuItemId = MiaoLiveData(R.id.nav_home)

    var currentFragment: Fragment? = null
}