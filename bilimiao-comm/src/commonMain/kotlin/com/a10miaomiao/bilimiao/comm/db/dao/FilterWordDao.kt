package com.a10miaomiao.bilimiao.comm.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.a10miaomiao.bilimiao.comm.db.entity.FilterWordEntity

@Dao
interface FilterWordDao {
    @Query("SELECT * FROM filter_world ORDER BY id ASC")
    suspend fun queryAll(): List<FilterWordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FilterWordEntity)

    @Query("UPDATE filter_world SET keyword = :newKeyword WHERE keyword = :oldKeyword")
    suspend fun updateKeyword(oldKeyword: String, newKeyword: String)

    @Query("DELETE FROM filter_world WHERE keyword = :keyword")
    suspend fun deleteByKeyword(keyword: String)

    @Query("DELETE FROM filter_world WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM filter_world")
    suspend fun deleteAll()
}
