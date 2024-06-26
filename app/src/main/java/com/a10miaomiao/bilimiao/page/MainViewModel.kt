package com.a10miaomiao.bilimiao.page

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.page.home.DynamicFragment
import com.a10miaomiao.bilimiao.page.home.PopularFragment
import com.a10miaomiao.bilimiao.page.home.HomeFragment
import com.a10miaomiao.bilimiao.page.home.RecommendFragment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class MainViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    val userStore by instance<UserStore>()

    var position = -1

    val navListFlow = SettingPreferences.run {
        context.dataStore.data.combine(
            userStore.stateFlow
        ) { preferences, userState ->
            val list = mutableListOf<HomeNav>(
                HomeNav.Home
            )
            if (preferences[HomeRecommendShow] != false) {
                list.add(HomeNav.Recommend)
            }
            if (preferences[HomePopularShow] != false) {
                list.add(HomeNav.Popular)
            }
            if (userStore.isLogin()) {
                list.add(HomeNav.Dynamic)
            }
            list.toList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = listOf()
    )
    val navList get() = navListFlow.value

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