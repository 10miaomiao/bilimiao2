package com.a10miaomiao.bilimiao.comm.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import okio.Path.Companion.toOkioPath

actual val appDataStore: DataStore<Preferences> by lazy {
    val dir = PlatformProviders.context.filesDir
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { dir.resolve("settings.preferences_pb").toOkioPath() }
    )
}
