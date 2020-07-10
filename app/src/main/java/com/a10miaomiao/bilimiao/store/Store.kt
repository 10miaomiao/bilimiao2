package com.a10miaomiao.bilimiao.store

import android.content.Context
import android.support.v4.app.FragmentActivity
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.utils.getViewModel

class Store(
        val activity: FragmentActivity
){

    val timeSettingStore by lazy {
        activity.getViewModel { TimeSettingStore(activity) }
    }
    val filterStore by lazy {
        activity.getViewModel { FilterStore(activity) }
    }
    val userStore by lazy {
        activity.getViewModel { UserStore(activity) }
    }
    val playerStore by lazy {
        activity.getViewModel { PlayerStore(activity) }
    }

    companion object{
        fun from(context: Context)
                = MainActivity.of(context).store
    }

}