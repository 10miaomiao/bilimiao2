package cn.a10miaomiao.bilimiao.compose.comm.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.debounce
import me.zhanghai.compose.preference.Preferences
import me.zhanghai.compose.preference.defaultPreferenceFlow
import java.time.Duration
import java.time.temporal.ChronoUnit

private typealias PreferencesDataStore = DataStore<androidx.datastore.preferences.core.Preferences>

@Composable
fun rememberPreferenceFlow(dataStore: PreferencesDataStore): MutableStateFlow<Preferences> {
    val scope = rememberCoroutineScope()
    return remember(dataStore) {
        val initialPreferences = DataStorePreferences(null)
        MutableStateFlow<Preferences>(initialPreferences).also { flow ->
            scope.launch {
                dataStore.data.collect {
                    flow.value = DataStorePreferences(it)
                }
            }
            scope.launch {
                flow.mapNotNull {
                    (it as? MutableDataStorePreferences)?.preferences
                }.collect { p ->
                    dataStore.updateData { p }
                }
            }
        }
    }
}