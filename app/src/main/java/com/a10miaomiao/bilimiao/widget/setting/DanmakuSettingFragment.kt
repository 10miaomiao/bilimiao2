package com.a10miaomiao.bilimiao.widget.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.store.WindowStore
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.categoryHeader
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.singleChoice
import de.Maxr1998.modernpreferences.helpers.switch
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.recyclerview.recyclerView

class DanmakuSettingFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "弹幕设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    val ui = miaoBindingUi {
        frameLayout {
            views {
                +recyclerView {
                    _miaoLayoutManage(LinearLayoutManager(requireContext()))
                    val mAdapter = miaoMemo(null) {
                        PreferencesAdapter(createRootScreen())
                    }
                    miaoEffect(null, {
                        adapter = mAdapter
                    })
                }..lParams(matchParent, matchParent)
            }
        }
    }

    fun createRootScreen() = screen(context) {
        collapseIcon = true

        categoryHeader("display") {
            title = "显示"
        }

        switch("danmaku_show") {
            title = "弹幕显示"
            defaultValue = true
        }

        switch("danmaku_r2l_show") {
            title = "滚动弹幕显示"
            defaultValue = true
        }

        switch("danmaku_ft_show") {
            title = "顶部弹幕显示"
            defaultValue = true
        }

        switch("danmaku_fb_show") {
            title = "底部弹幕显示"
            defaultValue = true
        }

        switch("danmaku_special_show") {
            title = "高级弹幕显示"
            defaultValue = true
        }

        categoryHeader("font") {
            title = "字体"
        }

        val fontsizeSelection = listOf(
            SelectionItem(key = "0.5", title = "比小更小"),
            SelectionItem(key = "0.75", title = "小"),
            SelectionItem(key = "1", title = "正常"),
            SelectionItem(key = "1.5", title = "大"),
            SelectionItem(key = "2", title = "比大更大")
        )
        singleChoice("danmaku_fontsize", fontsizeSelection) {
            title = "选择你的尺寸"
            summary = "想要多大的"

        }

        categoryHeader("speed") {
            title = "速度"
        }

        val speedSelection = listOf(
            SelectionItem(key = "2", title = "比慢更慢"),
            SelectionItem(key = "1.5", title = "慢"),
            SelectionItem(key = "1", title = "正常"),
            SelectionItem(key = "0.75", title = "快"),
            SelectionItem(key = "0.5", title = "比快更快")
        )
        singleChoice("danmaku_speed", speedSelection) {
            title = "选择你的车速"
            summary = "想要更快吗"
        }

        categoryHeader("高级") {
            title = "高级"
        }
        switch("danmaku_time_sync") {
            title = "弹幕时间校准"
            summary = "如果弹幕抖动厉害可以关掉，修改后重新播放视频生效"
            defaultValue = true
        }
    }

}