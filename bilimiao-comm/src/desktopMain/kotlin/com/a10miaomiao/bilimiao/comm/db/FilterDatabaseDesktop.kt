package com.a10miaomiao.bilimiao.comm.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders

actual fun createFilterDatabase(): FilterDatabase {
    val dbFile = PlatformProviders.context.filesDir.resolve("filter_db")
    return Room.databaseBuilder<FilterDatabase>(
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver()).build()
}
