package com.a10miaomiao.bilimiao.page

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.page.home.PopularFragment
import com.a10miaomiao.bilimiao.page.home.HomeFragment
import com.a10miaomiao.bilimiao.page.home.RecommendFragment
import com.a10miaomiao.bilimiao.page.setting.HomeSettingFragment
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import kotlin.reflect.KClass

class MainViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    var position = -1

    var navList = listOf<KClass<out Fragment>>()

    fun readNavList(): List<KClass<out Fragment>> {
        if (!HomeSettingFragment.homeSettingChange) {
            return navList
        }
        HomeSettingFragment.homeSettingChange = false
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val list = mutableListOf<KClass<out Fragment>>(
            HomeFragment::class
        )
        if (prefs.getBoolean("home_recommend_show", true)) {
            list.add(RecommendFragment::class)
        }
        if (prefs.getBoolean("home_popular_show", true)) {
            list.add(PopularFragment::class)
        }
        return list
    }

    fun equalsNavList(oldList: List<KClass<out Fragment>>, newList: List<KClass<out Fragment>>): Boolean {
        if (oldList.size != newList.size) {
            return false
        }
        for (i in oldList.indices) {
            if (oldList[i] != newList[i]) {
                return false
            }
        }
        return true
    }


}