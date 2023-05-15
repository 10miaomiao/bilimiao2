package com.a10miaomiao.bilimiao.page.setting

import android.content.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao.comm.connectUi
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.store.WindowStore
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.categoryHeader
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.switch
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.recyclerview.recyclerView

class HomeSettingFragment : Fragment(), DIAware, MyPage
    , SharedPreferences.OnSharedPreferenceChangeListener {

    companion object : FragmentNavigatorBuilder() {
        var homeSettingVersion = 0
        override val name = "setting.home"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://setting/home")
        }
    }

    override val pageConfig = myPageConfig {
        title = "首页设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()

    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    private var mPreferencesAdapter: PreferencesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
        mPreferencesAdapter?.let { mAdapter ->
            mAdapter.onScreenChangeListener
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if ("home" in key) {
            homeSettingVersion++
        }
    }

    val ui = miaoBindingUi {
        val insets = windowStore.getContentInsets(parentView)
        frameLayout {
            _leftPadding = insets.left
            _topPadding = insets.top
            _rightPadding = insets.right
            _bottomPadding = insets.bottom
            views {
                +recyclerView {
                    _miaoLayoutManage(LinearLayoutManager(requireContext()))
                    val mAdapter = miaoMemo(null) {
                        PreferencesAdapter(createRootScreen())
                    }
                    miaoEffect(null) {
                        adapter = mAdapter
                    }
                    mPreferencesAdapter = mAdapter
                }..lParams(matchParent, matchParent)
            }
        }
    }

    fun createRootScreen() = screen(context) {
        collapseIcon = true

        categoryHeader("top_nav") {
            title = "首页顶部设置"
        }

        switch("home_recommend_show") {
            title = "显示推荐"
            defaultValue = true
        }

        switch("home_popular_show") {
            title = "显示热门"
            defaultValue = true
        }

        categoryHeader("popular") {
            title = "热门设置"
        }

        switch("home_popular_carry_token") {
            title = "个性化热门列表"
            summary = "修改后需手动刷新列表"
            defaultValue = true
        }

    }

}