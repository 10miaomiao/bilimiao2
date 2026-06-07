package cn.a10miaomiao.bilimiao.compose.common.preference

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
expect fun rememberPreferenceFlow(
    dataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
): MutableStateFlow<Preferences>
