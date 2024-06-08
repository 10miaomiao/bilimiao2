package com.a10miaomiao.bilimiao.page.setting

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.bilimiao.compose.pages.setting.ProxySettingPage
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
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.player.media3.Libgav1Media3ExoPlayerManager
import com.a10miaomiao.bilimiao.widget.player.media3.Media3ExoPlayerManager
import com.a10miaomiao.bilimiao.widget.scaffold.getScaffoldView
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.categoryHeader
import de.Maxr1998.modernpreferences.helpers.onClick
import de.Maxr1998.modernpreferences.helpers.pref
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.seekBar
import de.Maxr1998.modernpreferences.helpers.singleChoice
import de.Maxr1998.modernpreferences.helpers.switch
import de.Maxr1998.modernpreferences.preferences.SwitchPreference
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.recyclerview.recyclerView

class VideoSettingFragment : Fragment(), DIAware, MyPage
    , SharedPreferences.OnSharedPreferenceChangeListener {

    companion object : FragmentNavigatorBuilder() {
        override val name = "setting.video"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://setting/video")
        }

        const val PLAYER_DECODER = "player_decoder"
        const val PLAYER_FNVAL = "player_fnval"
        const val PLAYER_BACKGROUND = "player_background"
        const val PLAYER_PROXY = "player_proxy"
        const val PLAYER_AUTO_START = "player_auto_start"
        const val PLAYER_AUTO_STOP = "player_auto_stop"
        const val PLAYER_PLAYING_AUTO_REPLACE = "player_playing_auto_replace"
        const val PLAYER_PAUSE_AUTO_REPLACE = "player_pause_auto_replace"
        const val PLAYER_COMPLETE_AUTO_REPLACE = "player_complete_auto_replace"
        const val PLAYER_PLAYING_NOTIFICATION = "player_playing_notification"
        const val PLAYER_FULL_MODE = "player_full_mode"
        const val PLAYER_VERTICAL_DEFAULT_FULL = "player_vertical_default_full"
        const val PLAYER_HORIZONTAL_DEFAULT_FULL = "player_horizontal_default_full"
        const val PLAYER_SCREEN_TYPE = "player_screen_type"
        const val PLAYER_AUDIO_FOCUS = "player_audio_focus"

        const val PLAYER_FULL_SHOW_BOTTOM_PROGRESS_BAR = "player_full_show_bottom_progress_bar"
        const val PLAYER_SMALL_SHOW_BOTTOM_PROGRESS_BAR = "player_small_show_bottom_progress_bar"

        const val PLAYER_AUTO_NEXT_VIDEO = "player_auto_next_video"
        const val PLAYER_AUTO_NEXT_BANGUMI = "player_auto_next_bangumi"
        const val PLAYLIST_RANDOM_NEXT = "playlist_random_next"
        const val PLAYLIST_AUTO_REPLAY = "playlist_auto_replay"

        const val PLAYER_AUTO_STOP_DURATION = "player_auto_stop_duration"

        const val PLAYER_SUBTITLE_SHOW = "player_subtitle_show"
        const val PLAYER_AI_SUBTITLE_SHOW = "player_ai_subtitle_show"

        const val DECODER_DEFAULT = "default"
        const val DECODER_AV1 = "AV1"

        const val FNVAL_FLV = "2"
        const val FNVAL_MP4 = "2"
        const val FNVAL_DASH = "4048"

        const val KEY_AUTO = "AUTO"
        const val KEY_SENSOR_LANDSCAPE = "SENSOR_LANDSCAPE"
        const val KEY_LANDSCAPE = "LANDSCAPE"
        const val KEY_REVERSE_LANDSCAPE = "REVERSE_LANDSCAPE"
        const val KEY_UNSPECIFIED = "UNSPECIFIED"

        const val PLAYER_SMALL_SHOW_AREA = "player_small_show_area"
        const val PLAYER_HOLD_SHOW_AREA = "player_hold_show_area"
        const val FULL_SCREEN_DRAGGABLE = "full_screen_draggable"
    }

    override val pageConfig = myPageConfig {
        title = "视频设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()

    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    private var mAdapter: PreferencesAdapter? = null

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
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(this)
        onSharedPreferenceChanged(prefs, PLAYER_SUBTITLE_SHOW)
        onSharedPreferenceChanged(prefs, PLAYER_AI_SUBTITLE_SHOW)
    }

    override fun onDestroy() {
        super.onDestroy()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        mAdapter?.run {
            val aiSubtitlePreference = currentScreen[PLAYER_AI_SUBTITLE_SHOW] as SwitchPreference
            if (key == PLAYER_SUBTITLE_SHOW) {
                val subtitlePreference = currentScreen[PLAYER_SUBTITLE_SHOW] as SwitchPreference
                if (sharedPreferences.getBoolean(PLAYER_SUBTITLE_SHOW, true)) {
                    subtitlePreference.summary = "字幕功能已启用"
                    aiSubtitlePreference.enabled = true
                } else {
                    subtitlePreference.summary = "字幕功能已关闭"
                    aiSubtitlePreference.enabled = false
                }
                notifyDataSetChanged()
            } else if (key == PLAYER_AI_SUBTITLE_SHOW) {
                if (sharedPreferences.getBoolean(PLAYER_AI_SUBTITLE_SHOW, false)) {
                    aiSubtitlePreference.summary = "AI字幕功能已启用"
                } else {
                    aiSubtitlePreference.summary = "AI字幕功能已关闭"
                }
                notifyDataSetChanged()
            } else if (key == PLAYER_DECODER) {
                if (sharedPreferences.getString(PLAYER_DECODER, DECODER_DEFAULT) == DECODER_AV1) {
                    // AV1
                    PlayerFactory.setPlayManager(Libgav1Media3ExoPlayerManager::class.java)
                } else {
                    // 默认
                    PlayerFactory.setPlayManager(Media3ExoPlayerManager::class.java)
                }
            }
        }
        // 同步播放器底部进度条显示控制
        // 及音频焦点
        if (key == PLAYER_SMALL_SHOW_BOTTOM_PROGRESS_BAR ||
            key == PLAYER_FULL_SHOW_BOTTOM_PROGRESS_BAR ||
            key == PLAYER_AUDIO_FOCUS) {
            basePlayerDelegate.updateVideoSetting()
        }
        // 同步横屏时小屏播放面积
        if (key == PLAYER_SMALL_SHOW_AREA) {
            scaffoldView.updatePlayerSmallShowArea()
        }
        if (key == PLAYER_HOLD_SHOW_AREA) {
            scaffoldView.updatePlayerHoldShowArea()
        }
        if (key == FULL_SCREEN_DRAGGABLE) {
            scaffoldView.updateFullScreenDraggable()
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
                    mAdapter = miaoMemo(null) {
                        PreferencesAdapter(createRootScreen())
                    }
                    miaoEffect(null) {
                        adapter = mAdapter
                    }
                }..lParams(matchParent, matchParent)
            }
        }
    }

    fun createRootScreen() = screen(context) {
        collapseIcon = true

        categoryHeader("0") {
            title = "播放器设置"
        }
        val engineSelection = listOf(
            SelectionItem(key = DECODER_DEFAULT, title = "默认"),
            SelectionItem(key = DECODER_AV1, title = "AV1"),
        )
        singleChoice(PLAYER_DECODER, engineSelection) {
            title = "解码器设置"
            summary = "修改后重启播放器生效"
            initialSelection = DECODER_DEFAULT
        }

        switch(PLAYER_BACKGROUND) {
            title = "后台播放"
            summary = "遇到困难时，不要停下来."
            defaultValue = true
        }

        switch(PLAYER_AUDIO_FOCUS) {
            title = "占用音频焦点"
            summary = "关闭后可以与其它APP同时播放"
            defaultValue = true
        }

        categoryHeader("1") {
            title = "视频源设置"
        }

        val fnvalSelection = listOf(
            SelectionItem(key = FNVAL_DASH, title = "dash(支持4K)"),
            SelectionItem(key = FNVAL_MP4, title = "mp4(不支持2K及以上)"),
//            SelectionItem(key = FNVAL_FLV, title = "flv(不支持2K及以上)"),
        )
        singleChoice(PLAYER_FNVAL, fnvalSelection) {
            title = "视频获取方式选择(视频格式)"
            summary = "不能播放时，换个格式试试吧"
            initialSelection = FNVAL_DASH
        }

        pref(PLAYER_PROXY) {
            title = "区域限制设置"
            summary = "滴，出差卡"

            onClick {
                val nav = findNavController()
                nav.navigateToCompose(ProxySettingPage())
                true
            }
        }

        categoryHeader("2") {
            title = "播放控制设置"
        }

        switch(PLAYER_AUTO_START) {
            title = "打开详情页时自动打开播放器"
            summary = "无正在播放视频，自动播放"
            defaultValue = false
        }

        switch(PLAYER_PLAYING_AUTO_REPLACE) {
            title = "自动替换播放中的视频"
            summary = "正在播放视频时，打开新详情页自动开始播放新视频"
            defaultValue = false
        }

        switch(PLAYER_PAUSE_AUTO_REPLACE) {
            title = "自动替换暂停中的视频"
            summary = "暂停播放视频时，打开新详情页自动开始播放新视频"
            defaultValue = false
        }

        switch(PLAYER_COMPLETE_AUTO_REPLACE) {
            title = "自动替换播放完成的视频"
            summary = "完成视频播放时，打开新详情页自动开始播放新视频"
            defaultValue = false
        }


        switch(PLAYER_AUTO_STOP) {
            title = "关闭详情页时自动关闭播放器"
            summary = "呐呐呐呐呐呐呐呐呐"
            defaultValue = false
        }

        switch(PLAYER_PLAYING_NOTIFICATION) {
            title = "播放时通知栏显示控制器"
            summary = "这个家里已经没有你的位置啦！"
            defaultValue = true
        }

        val fullModeSelection = listOf(
            SelectionItem(key = KEY_AUTO, title = "跟随视频"),
            SelectionItem(key = KEY_UNSPECIFIED, title = "跟随系统"),
            SelectionItem(key = KEY_SENSOR_LANDSCAPE, title = "横向全屏(自动旋转)"),
            SelectionItem(key = KEY_LANDSCAPE, title = "横向全屏(固定方向1)"),
            SelectionItem(key = KEY_REVERSE_LANDSCAPE, title = "横向全屏(固定方向2)"),
        )
        singleChoice(PLAYER_FULL_MODE, fullModeSelection) {
            title = "全屏播放设置"
            summary = ""
            initialSelection = KEY_AUTO
        }

        switch(PLAYER_VERTICAL_DEFAULT_FULL) {
            title = "竖屏时播放器默认全屏播放"
            summary = ""
            defaultValue = false
        }

        switch(PLAYER_HORIZONTAL_DEFAULT_FULL) {
            title = "横屏时播放器默认全屏播放"
            summary = ""
            defaultValue = false
        }

        switch(PLAYER_AUTO_NEXT_VIDEO) {
            title = "视频自动播放下一P"
            summary = ""
            defaultValue = true
        }

        seekBar(PLAYER_AUTO_STOP_DURATION) {
            title = "播放器定时关闭"
            summary = "视频播放的时长，而不是实际经过的时间"
            max = 3600000
            min = 0
            default = 0
            formatter = {
                val second = value/1000
                val minute = second/60
                if(second == 0){
                    "${value}ms"
                } else if(minute == 0){
                    "${second}s"
                } else {
                    "${minute}min${second-minute*60}s"
                }
            }
        }

        switch(PLAYER_AUTO_NEXT_BANGUMI) {
            title = "番剧自动播放下一集"
            summary = ""
            defaultValue = true
        }

//        switch(PLAYLIST_RANDOM_NEXT) {
//            title = "收藏夹列表随机播放"
//            summary = ""
//            defaultValue = false
//        }
//
//        switch(PLAYLIST_AUTO_REPLAY) {
//            title = "收藏夹列表单集循环"
//            summary = "优先级高于随机播放"
//            defaultValue = false
//        }

        switch(PLAYER_FULL_SHOW_BOTTOM_PROGRESS_BAR) {
            title = "全屏时显示底部进度条"
            summary = ""
            defaultValue = true
        }

        switch(PLAYER_SMALL_SHOW_BOTTOM_PROGRESS_BAR) {
            title = "小屏时显示底部进度条"
            summary = ""
            defaultValue = true
        }

        seekBar(PLAYER_SMALL_SHOW_AREA) {
            title = "横屏时小屏播放面积"
            default = 480
            max = 600
            min = 150
            formatter = { it.toString() }
        }

        seekBar(PLAYER_HOLD_SHOW_AREA) {
            title = "小屏挂起后播放面积"
            default = 130
            max = 300
            min = 100
            formatter = { it.toString() }
        }

        switch(FULL_SCREEN_DRAGGABLE) {
            title = "小屏时整个屏幕可拖拽"
            summary = ""
            defaultValue = false
        }

        categoryHeader("3") {
            title = "字幕显示设置"
        }

        switch(PLAYER_SUBTITLE_SHOW) {
            title = "字幕显示"
            summary = "字幕功能已打开"
            summaryDisabled = "字幕功能已关闭"
            defaultValue = true
        }

        switch(PLAYER_AI_SUBTITLE_SHOW) {
            title = "AI字幕显示"
            summary = "无论什么字幕都显示"
            summaryDisabled = "字幕功能已关闭"
            defaultValue = false
        }

        categoryHeader("其它") {
            title = "其它"
        }
        switch(DanmakuSettingFragment.KEY_DANMAKU_TIME_SYNC) {
            title = "弹幕时间校准"
            summary = "如果弹幕抖动厉害可以关掉，修改后重新播放视频生效"
            defaultValue = true
        }
        switch(DanmakuSettingFragment.KEY_DANMAKU_SYS_FONT) {
            title = "弹幕使用系统字体"
            summary = "修改后需重启APP生效"
            defaultValue = false
        }



    }

}