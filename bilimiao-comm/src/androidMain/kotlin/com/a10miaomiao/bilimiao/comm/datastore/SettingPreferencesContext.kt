package com.a10miaomiao.bilimiao.comm.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "settings")

inline fun SettingPreferences.launch(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    crossinline block: suspend SettingPreferences.() -> Unit
) = scope.launch(context, start) {
    block()
}

suspend fun SettingPreferences.edit(
    context: Context,
    transform: suspend SettingPreferences.(MutablePreferences) -> Unit
) {
    context.dataStore.edit {
        transform(it)
    }
}

suspend fun SettingPreferences.getData(
    context: Context,
    block: suspend SettingPreferences.(Preferences) -> Unit
) {
    val preferences = context.dataStore.data.first()
    block(preferences)
}

suspend fun <T> SettingPreferences.mapData(
    context: Context,
    block: suspend SettingPreferences.(Preferences) -> T
): T {
    val preferences = context.dataStore.data.first()
    return block(preferences)
}
