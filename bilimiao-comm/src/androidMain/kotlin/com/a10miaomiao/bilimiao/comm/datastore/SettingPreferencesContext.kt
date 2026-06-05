package com.a10miaomiao.bilimiao.comm.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders

internal val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "settings")

actual val appDataStore: DataStore<Preferences>
    get() {
        val app = PlatformProviders.context.platformContext as Context
        return app.dataStore
    }
