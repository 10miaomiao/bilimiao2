package com.a10miaomiao.bilimiao.comm.datastore

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

// 向后兼容的重载函数，context 参数不再使用（appDataStore 已全局可用）

suspend fun SettingPreferences.edit(
    context: Context,
    transform: suspend SettingPreferences.(MutablePreferences) -> Unit
) {
    editPreferences(transform)
}

suspend fun SettingPreferences.getData(
    context: Context,
    block: suspend SettingPreferences.(Preferences) -> Unit
) {
    readPreferences(block)
}

suspend fun <T> SettingPreferences.mapData(
    context: Context,
    block: suspend SettingPreferences.(Preferences) -> T
): T {
    return mapPreferences(block)
}
