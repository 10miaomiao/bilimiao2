package com.a10miaomiao.bilimiao.comm.db

import android.app.Application
import androidx.room.Room
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders

actual fun createFilterDatabase(): FilterDatabase {
    val app = PlatformProviders.context.platformContext as Application
    val dbFile = app.filesDir.resolve("filter_db")
    return Room.databaseBuilder<FilterDatabase>(
        context = app,
        name = dbFile.absolutePath,
    ).build()
}
