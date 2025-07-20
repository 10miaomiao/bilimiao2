package com.a10miaomiao.bilimiao.comm.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object SettingPreferences {

    val Context.dataStore: DataStore<Preferences>
            by preferencesDataStore(name = "settings")

    inline fun launch(
        scope: CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        crossinline block: suspend SettingPreferences.() -> Unit
    ) = scope.launch(context, start) {
        block()
    }

    suspend fun edit(
        context: Context,
        transform: suspend SettingPreferences.(MutablePreferences) -> Unit
    ) {
        context.dataStore.edit {
            transform(it)
        }
    }

    suspend fun getData(
        context: Context,
        block: suspend SettingPreferences.(Preferences) -> Unit
    ) {
        val preferences = context.dataStore.data.first()
        block(preferences)
    }

    suspend fun <T> mapData(
        context: Context,
        block: suspend SettingPreferences.(Preferences) -> T
    ): T {
        val preferences = context.dataStore.data.first()
        return block(preferences)
    }

    /**
     * General
     */
    // 使用旧版分区
    val IsBestRegion = booleanPreferencesKey("is_best_region")
    // 自动检测更新
    val IsAutoCheckVersion = booleanPreferencesKey("is_auto_check_version")
    // 忽略更新的版本
    val IgnoreUpdateVersionCode = longPreferencesKey("ignore_update_version_code")

    /**
     * Home
     */
    // 显示推荐
    val HomeRecommendShow = booleanPreferencesKey("home_recommend_show")
    // 显示热门
    val HomePopularShow = booleanPreferencesKey("home_popular_show")
    // 热门列表携带Token
    val HomePopularCarryToken = booleanPreferencesKey("home_popular_carry_token")
    // 推荐列表样式
    val HomeRecommendListStyle = intPreferencesKey("home_recommend_list_style")
    // 首页入口视图
    val HomeEntryView = intPreferencesKey("home_entry_view")

    /**
     * Theme
     */
    // 主题颜色
    val ThemeColor = longPreferencesKey("theme_color")
    // 深色模式
    val ThemeDarkMode = intPreferencesKey("theme_dark_mode")
    //
    val ThemeAppBarType = intPreferencesKey("theme_app_bar_type")

    /**
     * Player
     */
    // 解码器
    val PlayerDecoder = intPreferencesKey("player_decoder")
    // 清晰度
    val PlayerQuality = intPreferencesKey("player_quality")
    // 倍速
    val PlayerSpeed = floatPreferencesKey("player_speed")
    // 屏幕缩放类型
    val PlayerScreenType = intPreferencesKey("player_screen_type")
    // 格式
    val PlayerFnval = intPreferencesKey("player_fnval")
    // 后台播放
    val PlayerBackground = booleanPreferencesKey("player_background")
    // 代理
    val PlayerProxy = stringPreferencesKey("player_proxy")
    // 播放器打开模式
    // 0000 0000：什么都不做
    // 0000 0001：无播放时，自动播放
    // 0000 0010：自动替换播放中的视频
    // 0000 0100：自动替换暂停暂停的视频
    // 0000 1000：自动替换播放完成的视频
    // 0001 0000：自动关闭
    // 0010 0000：竖屏状态自动全屏
    // 0100 0000：横屏状态自动全屏
    val PlayerOpenMode = intPreferencesKey("player_open_mode")
    // 播放顺序
    // 0000：播放完结束
    // 0001：播放完循环
    // 0010：自动下一P
    // 0100：自动下一个视频
    // 1000：自动下一集（番剧）
    val PlayerOrder = intPreferencesKey("player_order")
    // 随机播放
    val PlayerOrderRandom = booleanPreferencesKey("player_order_random")
    // 显示通知栏控制器
    val PlayerNotification = booleanPreferencesKey("player_notification")
    // 全屏模式
    val PlayerFullMode = intPreferencesKey("player_full_mode")
    // 底部进度条显示控制
    val PlayerBottomProgressBarShow = intPreferencesKey("player_bottom_progress_bar_show")
    // 倍速菜单值
    val PlayerSpeedValues = stringSetPreferencesKey("player_speed_values")
    // 占用音频焦点
    val PlayerAudioFocus = booleanPreferencesKey("player_audio_focus")
    // 字幕显示
    val PlayerSubtitleShow = booleanPreferencesKey("player_subtitle_show")
    // AI字幕显示
    val PlayerAiSubtitleShow = booleanPreferencesKey("player_ai_subtitle_show")
    // 小屏显示面积
    val PlayerSmallShowArea = intPreferencesKey("player_small_show_area")
    // 挂起时显示面积
    val PlayerHoldShowArea = intPreferencesKey("player_hold_show_area")
    // 小屏是否可拖动
    val PlayerSmallDraggable = booleanPreferencesKey("player_small_draggable")

    /**
     * Danmaku
     */
    // 启用弹幕
    val DanmakuEnable = booleanPreferencesKey("danmaku_enable")
    // 使用系统字体
    val DanmakuSysFont = booleanPreferencesKey("danmaku_sys_font")
    // 时间同步
    val DanmakuTimeSync = booleanPreferencesKey("danmaku_time_sync")
    // 默认状态
    val DanmakuDefault = Danmaku("default")
    // 小屏模式
    val DanmakuSmallMode = Danmaku("small")
    // 全屏模式
    val DanmakuFullMode = Danmaku("full")
    // 画中画模式
    val DanmakuPipMode = Danmaku("pip")
    class Danmaku(
        val name: String,
    ) {
        // 启用设置
        val enable = booleanPreferencesKey("${name}_danmaku_enable")
        // 显示
        val show = booleanPreferencesKey("${name}_danmaku_show")
        // 滚动显示
        val r2lShow = booleanPreferencesKey("${name}_danmaku_r2l_show")
        // 顶部显示
        val ftShow = booleanPreferencesKey("${name}_danmaku_ft_show")
        // 底部显示
        val fbShow = booleanPreferencesKey("${name}_danmaku_fb_show")
        // 特殊弹幕显示
        val specialShow = booleanPreferencesKey("${name}_danmaku_special_show")
        // 字体大小
        val fontSize = floatPreferencesKey("${name}_danmaku_fontsize")
        // 不透明度
        val opacity = floatPreferencesKey("${name}_danmaku_opacity")
        // 滚动速度
        val speed = floatPreferencesKey("${name}_danmaku_speed")
        // 最大显示行数
        val maxLines = intPreferencesKey("${name}_danmaku_max_lines")
        // 滚动最大显示行数
        val r2lMaxLine = intPreferencesKey("${name}_danmaku_r2l_max_line")
        // 顶部最大显示行数
        val ftMaxLine = intPreferencesKey("${name}_danmaku_ft_max_line")
        // 底部最大显示行数
        val fbMaxLine = intPreferencesKey("${name}_danmaku_fb_max_line")
    }

    /**
     * Flag
     */
    // 副屏显示
    val FlagSubContentShow = booleanPreferencesKey("flag_sub_content_show")
    // 主副屏分割比
    val FlagContentSplit = intPreferencesKey("flag_content_split")
    // 动画时长
    val FlagContentAnimationDuration = intPreferencesKey("flag_content_animation_duration")
}