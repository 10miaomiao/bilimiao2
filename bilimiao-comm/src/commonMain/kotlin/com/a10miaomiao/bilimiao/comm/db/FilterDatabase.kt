package com.a10miaomiao.bilimiao.comm.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.a10miaomiao.bilimiao.comm.db.dao.FilterTagDao
import com.a10miaomiao.bilimiao.comm.db.dao.FilterUpperDao
import com.a10miaomiao.bilimiao.comm.db.dao.FilterWordDao
import com.a10miaomiao.bilimiao.comm.db.entity.FilterTagEntity
import com.a10miaomiao.bilimiao.comm.db.entity.FilterUpperEntity
import com.a10miaomiao.bilimiao.comm.db.entity.FilterWordEntity

@Database(
    entities = [
        FilterTagEntity::class,
        FilterUpperEntity::class,
        FilterWordEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class FilterDatabase : RoomDatabase() {
    abstract fun filterTagDao(): FilterTagDao
    abstract fun filterUpperDao(): FilterUpperDao
    abstract fun filterWordDao(): FilterWordDao
}

expect fun createFilterDatabase(): FilterDatabase
