package com.a10miaomiao.bilimiao.comm.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.a10miaomiao.bilimiao.comm.db.entity.FilterUpperEntity

@Dao
interface FilterUpperDao {
    @Query("SELECT * FROM filter_upper")
    suspend fun queryAll(): List<FilterUpperEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FilterUpperEntity)

    @Query("DELETE FROM filter_upper WHERE mid = :mid")
    suspend fun deleteByMid(mid: Long)
}
