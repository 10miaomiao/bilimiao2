package com.a10miaomiao.bilimiao.page.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
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
import splitties.views.dsl.recyclerview.recyclerView

class VideoSettingFragment : Fragment(), DIAware, MyPage {

    companion object {
        const val PLAYER_BACKGROUND = "player_background"
        const val PLAYER_AUTO_STOP = "player_auto_stop"
        const val PLAYER_PLAYING_NOTIFICATION = "player_playing_notification"
        const val PLAYER_FULL_MODE = "player_full_mode"

        const val KEY_SENSOR_LANDSCAPE = "SENSOR_LANDSCAPE"
        const val KEY_LANDSCAPE = "LANDSCAPE"
        const val KEY_REVERSE_LANDSCAPE = "REVERSE_LANDSCAPE"
        const val KEY_UNSPECIFIED = "UNSPECIFIED"
    }

    override val pageConfig = myPageConfig {
        title = "视频设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()

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
                    miaoEffect(null, {
                        adapter = mAdapter
                    })
                }..lParams(matchParent, matchParent)
            }
        }
    }

    fun createRootScreen() = screen(context) {
        collapseIcon = true

        switch(PLAYER_BACKGROUND) {
            title = "后台播放"
            summary = "遇到困难时，不要停下来."
            defaultValue = true
        }
        switch(PLAYER_AUTO_STOP) {
            title = "关闭详情页时同时关闭播放"
            summary = "呐呐呐呐呐呐呐呐呐"
            defaultValue = false
        }

        switch(PLAYER_PLAYING_NOTIFICATION) {
            title = "播放时通知栏显示控制器"
            summary = "这个家里已经没有你的位置啦！"
            defaultValue = true
        }

        val fullModeSelection = listOf(
            SelectionItem(key = KEY_SENSOR_LANDSCAPE, title = "横向全屏(自动旋转)"),
            SelectionItem(key = KEY_LANDSCAPE, title = "横向全屏(固定方向1)"),
            SelectionItem(key = KEY_REVERSE_LANDSCAPE, title = "横向全屏(固定方向2)"),
            SelectionItem(key = KEY_UNSPECIFIED, title = "不指定方向"),
        )
        singleChoice(PLAYER_FULL_MODE, fullModeSelection) {
            title = "全屏播放设置"
            summary = ""
            initialSelection = "SENSOR_LANDSCAPE"
        }
    }

}