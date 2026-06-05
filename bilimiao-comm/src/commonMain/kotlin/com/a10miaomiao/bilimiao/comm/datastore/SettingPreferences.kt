package com.a10miaomiao.bilimiao.comm.datastore

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

expect val appDataStore: DataStore<Preferences>

inline fun SettingPreferences.launch(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend SettingPreferences.() -> Unit
) = scope.launch(context, start) {
    block()
}

suspend fun SettingPreferences.editPreferences(
    transform: suspend SettingPreferences.(MutablePreferences) -> Unit
) {
    appDataStore.edit {
        transform(it)
    }
}

suspend fun SettingPreferences.readPreferences(
    block: suspend SettingPreferences.(Preferences) -> Unit
) {
    val preferences = appDataStore.data.first()
    block(preferences)
}

suspend fun <T> SettingPreferences.mapPreferences(
    block: suspend SettingPreferences.(Preferences) -> T
): T {
    val preferences = appDataStore.data.first()
    return block(preferences)
}

object SettingPreferences {

    /**
     * General
     */
    val IsBestRegion = booleanPreferencesKey("is_best_region")
    val IsLockScreenOrientationPortrait = booleanPreferencesKey("is_lock_screen_orientation_portrait")
    val IsAutoCheckVersion = booleanPreferencesKey("is_auto_check_version")
    val IgnoreUpdateVersionCode = longPreferencesKey("ignore_update_version_code")

    /**
     * Home
     */
    val HomeRecommendShow = booleanPreferencesKey("home_recommend_show")
    val HomePopularShow = booleanPreferencesKey("home_popular_show")
    val HomePopularCarryToken = booleanPreferencesKey("home_popular_carry_token")
    val HomeRecommendListStyle = intPreferencesKey("home_recommend_list_style")
    val HomeEntryView = intPreferencesKey("home_entry_view")

    /**
     * Theme
     */
    val ThemeColor = longPreferencesKey("theme_color")
    val ThemeType = intPreferencesKey("theme_type")
    val ThemeDarkMode = intPreferencesKey("theme_dark_mode")
    val ThemeAppBarType = intPreferencesKey("theme_app_bar_type")

    /**
     * Player
     */
    val PlayerDecoder = intPreferencesKey("player_decoder")
    val PlayerQuality = intPreferencesKey("player_quality")
    val PlayerSpeed = floatPreferencesKey("player_speed")
    val PlayerScreenType = intPreferencesKey("player_screen_type")
    val PlayerFnval = intPreferencesKey("player_fnval")
    val PlayerBackground = booleanPreferencesKey("player_background")
    val PlayerProxy = stringPreferencesKey("player_proxy")
    val PlayerOpenMode = intPreferencesKey("player_open_mode")
    val PlayerOrder = intPreferencesKey("player_order")
    val PlayerOrderRandom = booleanPreferencesKey("player_order_random")
    val PlayerNotification = booleanPreferencesKey("player_notification")
    val PlayerFullMode = intPreferencesKey("player_full_mode")
    val PlayerBottomProgressBarShow = intPreferencesKey("player_bottom_progress_bar_show")
    val PlayerSpeedValues = stringSetPreferencesKey("player_speed_values")
    val PlayerAudioFocus = booleanPreferencesKey("player_audio_focus")
    val PlayerSubtitleShow = booleanPreferencesKey("player_subtitle_show")
    val PlayerAiSubtitleShow = booleanPreferencesKey("player_ai_subtitle_show")
    val PlayerSmallShowArea = intPreferencesKey("player_small_show_area")
    val PlayerHoldShowArea = intPreferencesKey("player_hold_show_area")
    val PlayerSmallDraggable = booleanPreferencesKey("player_small_draggable")
    val PlayerAutoStopDuration = intPreferencesKey("player_auto_stop_duration")

    /**
     * Danmaku
     */
    val DanmakuEnable = booleanPreferencesKey("danmaku_enable")
    val DanmakuSysFont = booleanPreferencesKey("danmaku_sys_font")
    val DanmakuTimeSync = booleanPreferencesKey("danmaku_time_sync")
    val DanmakuDefault = Danmaku("default")
    val DanmakuSmallMode = Danmaku("small")
    val DanmakuFullMode = Danmaku("full")
    val DanmakuPipMode = Danmaku("pip")

    class Danmaku(val name: String) {
        val enable = booleanPreferencesKey("${name}_danmaku_enable")
        val show = booleanPreferencesKey("${name}_danmaku_show")
        val r2lShow = booleanPreferencesKey("${name}_danmaku_r2l_show")
        val ftShow = booleanPreferencesKey("${name}_danmaku_ft_show")
        val fbShow = booleanPreferencesKey("${name}_danmaku_fb_show")
        val specialShow = booleanPreferencesKey("${name}_danmaku_special_show")
        val fontSize = floatPreferencesKey("${name}_danmaku_fontsize")
        val opacity = floatPreferencesKey("${name}_danmaku_opacity")
        val speed = floatPreferencesKey("${name}_danmaku_speed")
        val maxLines = intPreferencesKey("${name}_danmaku_max_lines")
        val r2lMaxLine = intPreferencesKey("${name}_danmaku_r2l_max_line")
        val ftMaxLine = intPreferencesKey("${name}_danmaku_ft_max_line")
        val fbMaxLine = intPreferencesKey("${name}_danmaku_fb_max_line")
    }
}
