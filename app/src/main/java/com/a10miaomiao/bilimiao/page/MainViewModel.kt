package com.a10miaomiao.bilimiao.page

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.page.home.DynamicFragment
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

    val userStore by instance<UserStore>()

    var position = -1

    var navList = listOf<KClass<out Fragment>>()

    private var curHomeSettingVersion = -1

    fun readNavList(): List<KClass<out Fragment>> {
        if (curHomeSettingVersion == HomeSettingFragment.homeSettingVersion) {
            return navList
        }
        curHomeSettingVersion = HomeSettingFragment.homeSettingVersion
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
        if (userStore.isLogin()) {
            list.add(DynamicFragment::class)
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