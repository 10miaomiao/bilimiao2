package com.a10miaomiao.bilimiao.comm.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders

actual fun createSearchHistoryDatabase(): SearchHistoryDatabase {
    val dbFile = PlatformProviders.context.filesDir.resolve("PreventKeyWord_db2")
    return Room.databaseBuilder<SearchHistoryDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver()).build()
}
