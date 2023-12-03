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

    var navList = listOf<HomeNav>()

    private var curHomeSettingVersion = -1

    fun readNavList(): List<HomeNav> {
        if (curHomeSettingVersion == HomeSettingFragment.homeSettingVersion) {
            return navList
        }
        curHomeSettingVersion = HomeSettingFragment.homeSettingVersion
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val list = mutableListOf<HomeNav>(
            HomeNav.Home
        )
        if (prefs.getBoolean("home_recommend_show", true)) {
            list.add(HomeNav.Recommend)
        }
        if (prefs.getBoolean("home_popular_show", true)) {
            list.add(HomeNav.Popular)
        }
        if (userStore.isLogin()) {
            list.add(HomeNav.Dynamic)
        }
        return list
    }

    fun equalsNavList(oldList: List<HomeNav>, newList: List<HomeNav>): Boolean {
        if (oldList.size != newList.size) {
            return false
        }
        for (i in oldList.indices) {
            if (oldList[i].id != newList[i].id) {
                return false
            }
        }
        return true
    }

    sealed class HomeNav(
        val id: Long,
        val title: String,
        val createFragment: () -> Fragment,
    ) {
        object Home : HomeNav(1L, "首页", HomeFragment::newFragmentInstance)
        object Recommend : HomeNav(2L, "推荐", RecommendFragment::newFragmentInstance)
        object Popular : HomeNav(3L, "热门", PopularFragment::newFragmentInstance)
        object Dynamic : HomeNav(4L, "动态", DynamicFragment::newFragmentInstance)
    }

}