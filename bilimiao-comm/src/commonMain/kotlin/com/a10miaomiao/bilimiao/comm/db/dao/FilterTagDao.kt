package com.a10miaomiao.bilimiao.comm.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.a10miaomiao.bilimiao.comm.db.entity.FilterTagEntity

@Dao
interface FilterTagDao {
    @Query("SELECT * FROM filter_tag ORDER BY id ASC")
    suspend fun queryAll(): List<FilterTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FilterTagEntity)

    @Query("UPDATE filter_tag SET name = :newName WHERE name = :oldName")
    suspend fun updateTagName(oldName: String, newName: String)

    @Query("DELETE FROM filter_tag WHERE name = :tagName")
    suspend fun deleteByTagName(tagName: String)

    @Query("DELETE FROM filter_tag WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM filter_tag")
    suspend fun deleteAll()
}
