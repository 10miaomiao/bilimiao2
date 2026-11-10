package com.a10miaomiao.bilimiao.comm.db

import android.app.Application
import androidx.room.Room
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders

actual fun createFilterDatabase(): FilterDatabase {
    val app = PlatformProviders.context.platformContext as Application
    val dbFile = app.filesDir.resolve("filter_db")
    val db = Room.databaseBuilder<FilterDatabase>(
        context = app,
        name = dbFile.absolutePath,
    ).build()
    // 把旧版 SQLiteOpenHelper 时代的 databases/filter_db 合并进来，成功后旧库会被改名
    LegacyFilterMigrator.migrate(app, db)
    return db
}
