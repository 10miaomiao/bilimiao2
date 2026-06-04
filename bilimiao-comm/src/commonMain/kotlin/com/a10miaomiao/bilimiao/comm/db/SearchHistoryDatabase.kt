package com.a10miaomiao.bilimiao.comm.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.a10miaomiao.bilimiao.comm.db.dao.SearchHistoryDao
import com.a10miaomiao.bilimiao.comm.db.entity.SearchHistoryEntity

@Database(
    entities = [SearchHistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SearchHistoryDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
}

expect fun createSearchHistoryDatabase(): SearchHistoryDatabase
