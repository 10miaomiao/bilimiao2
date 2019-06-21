package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModel
import android.support.v4.app.Fragment

class MainViewModel : ViewModel() {
    var checkedMenuItemId: Int? = null
    var currentFragment: Fragment? = null
}