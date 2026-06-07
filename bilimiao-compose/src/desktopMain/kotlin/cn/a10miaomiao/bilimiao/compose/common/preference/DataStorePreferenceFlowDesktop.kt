package cn.a10miaomiao.bilimiao.compose.common.preference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

private typealias PreferencesDataStore = DataStore<androidx.datastore.preferences.core.Preferences>

@Composable
actual fun rememberPreferenceFlow(dataStore: PreferencesDataStore): MutableStateFlow<cn.a10miaomiao.bilimiao.compose.common.preference.Preferences> {
    val scope = rememberCoroutineScope()
    return remember(dataStore) {
        val initialPreferences = DataStorePreferences(null)
        MutableStateFlow<cn.a10miaomiao.bilimiao.compose.common.preference.Preferences>(initialPreferences).also { flow ->
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
