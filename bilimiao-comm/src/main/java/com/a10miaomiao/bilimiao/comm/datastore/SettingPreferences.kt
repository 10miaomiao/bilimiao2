package com.a10miaomiao.bilimiao.comm.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences.dataStore
import kotlinx.coroutines.flow.Flow

object SettingPreferences {

    val Context.dataStore: DataStore<Preferences>
            by preferencesDataStore(name = "settings")

    suspend fun Context.edit(
        transform: suspend SettingPreferences.(MutablePreferences) -> Unit
    ) {
        dataStore.edit {
            transform(it)
        }
    }


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

    /**
     * Player
     */
    // 解码器
    val PlayerDecoder = intPreferencesKey("player_decoder")
    // 格式
    val PlayerFnval = intPreferencesKey("player_fnval")
    // 后台播放
    val PlayerBackground = booleanPreferencesKey("player_background")
    // 代理
    val PlayerProxy = stringSetPreferencesKey("player_proxy")

//    const val PLAYER_AUTO_START = "player_auto_start"
//    const val PLAYER_AUTO_STOP = "player_auto_stop"
//    const val PLAYER_PLAYING_AUTO_REPLACE = "player_playing_auto_replace"
//    const val PLAYER_PAUSE_AUTO_REPLACE = "player_pause_auto_replace"
//    const val PLAYER_COMPLETE_AUTO_REPLACE = "player_complete_auto_replace"
//    const val PLAYER_PLAYING_NOTIFICATION = "player_playing_notification"

    // 全屏模式
    val PlayerFullMode = intPreferencesKey("player_full_mode")

    const val PLAYER_VERTICAL_DEFAULT_FULL = "player_vertical_default_full"
    const val PLAYER_HORIZONTAL_DEFAULT_FULL = "player_horizontal_default_full"

    const val PLAYER_SCREEN_TYPE = "player_screen_type"

    // 播放焦点
    val PlayerAudioFocus = booleanPreferencesKey("player_audio_focus")


    /**
     * Danmaku
     */
    const val KEY_DANMAKU_SHOW = "danmaku_show"
    const val KEY_DANMAKU_R2L_SHOW = "danmaku_r2l_show"
    const val KEY_DANMAKU_FT_SHOW = "danmaku_ft_show"
    const val KEY_DANMAKU_FB_SHOW = "danmaku_fb_show"
    const val KEY_DANMAKU_SPECIAL_SHOW = "danmaku_special_show"
    const val KEY_DANMAKU_FONTSIZE = "danmaku_fontsize"
    const val KEY_DANMAKU_TRANSPARENT = "danmaku_transparent"
    const val KEY_DANMAKU_SPEED = "danmaku_speed"
    const val KEY_DANMAKU_TIME_SYNC = "danmaku_time_sync"
    const val KEY_DANMAKU_MAX_LINES = "danmaku_max_lines"
    const val KEY_DANMAKU_R2L_MAX_LINE = "danmaku_r2l_max_line"
    const val KEY_DANMAKU_FT_MAX_LINE = "danmaku_ft_smax_line"
    const val KEY_DANMAKU_FB_MAX_LINE = "danmaku_fb_max_line"
    const val KEY_DANMAKU_SYS_FONT = "danmaku_sys_font"

}