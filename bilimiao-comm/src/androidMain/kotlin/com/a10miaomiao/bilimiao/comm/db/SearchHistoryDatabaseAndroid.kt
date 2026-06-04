package com.a10miaomiao.bilimiao.comm.db

import android.app.Application
import androidx.room.Room
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders

actual fun createSearchHistoryDatabase(): SearchHistoryDatabase {
    val app = PlatformProviders.context.platformContext as Application
    val dbFile = app.filesDir.resolve("PreventKeyWord_db2")
    return Room.databaseBuilder<SearchHistoryDatabase>(
        context = app,
        name = dbFile.absolutePath,
    ).build()
}
