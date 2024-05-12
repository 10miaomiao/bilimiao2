package com.a10miaomiao.bilimiao.page.setting

import android.content.Intent
import android.content.SharedPreferences
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
import com.a10miaomiao.bilimiao.widget.scaffold.getScaffoldView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.categoryHeader
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.seekBar
import de.Maxr1998.modernpreferences.helpers.switch
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.recyclerview.recyclerView

class FlagsSeetingFragment : Fragment(), DIAware, MyPage
    , SharedPreferences.OnSharedPreferenceChangeListener {

    companion object : FragmentNavigatorBuilder() {
        override val name = "setting.flags"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://setting/flags")
        }

        const val FLAGS_SUB_CONTENT_SHOW = "flags_sub_content_show"
        const val FLAGS_CONTENT_DEFAULT_SPLIT = "flags_content_default_split"
        const val FLAGS_CONTENT_ANIMATION_DURATION = "flags_content_animation_duration"
    }

    override val pageConfig = myPageConfig {
        title = "实验性功能设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()

    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    private var mPreferencesAdapter: PreferencesAdapter? = null

    private val scaffoldView by lazy { requireActivity().getScaffoldView() }

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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == FLAGS_SUB_CONTENT_SHOW) {
            showRebootAppDialog("修改双屏显示，需重新打开APP后生效")
        } else if (key == FLAGS_CONTENT_DEFAULT_SPLIT) {
            scaffoldView.updateContentDefaultSplit()
        } else if (key == FLAGS_CONTENT_ANIMATION_DURATION) {
            scaffoldView.updateContentAnimationDuration()
        }
    }

    private fun showRebootAppDialog(
        message: String
    ) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("提示")
            setMessage(message)
            setNeutralButton("立即重新打开") { _, _ ->
                val ctx = requireContext()
                val packageManager = ctx.packageManager
                val intent = packageManager.getLaunchIntentForPackage(ctx.packageName)!!
                val componentName = intent.component
                val mainIntent = Intent.makeRestartActivityTask(componentName)
                ctx.startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
            setNegativeButton("稍后手动", null)
        }.show()
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

        categoryHeader("experiments") {
            title = "实验性功能"
        }

        switch(FLAGS_SUB_CONTENT_SHOW) {
            title = "横屏模式下双屏显示"
            summary = "修改后需重启APP"
            defaultValue = false
        }

        seekBar(FLAGS_CONTENT_DEFAULT_SPLIT) {
            title = "横屏模式下双屏内容分割比"
            default = 35
            max = 85
            min = 15
            formatter = {
                "${it} ：${100 - it}"
            }
        }

        seekBar(FLAGS_CONTENT_ANIMATION_DURATION) {
            title = "内容区域动画时长"
            summary = "为0时不显示动画，开启后动画可能掉帧"
            default = 0
            max = 1000
            min = 0
            formatter = {
                "${it}ms"
            }
        }

    }

}