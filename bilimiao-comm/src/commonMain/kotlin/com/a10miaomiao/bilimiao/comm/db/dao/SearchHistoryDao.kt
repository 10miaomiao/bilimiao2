package com.a10miaomiao.bilimiao.comm.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.a10miaomiao.bilimiao.comm.db.entity.SearchHistoryEntity

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM PreventKeyWord2 ORDER BY id DESC")
    suspend fun queryAllHistory(): List<SearchHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: SearchHistoryEntity)

    @Query("DELETE FROM PreventKeyWord2 WHERE keyword = :keyword")
    suspend fun deleteHistory(keyword: String)

    @Query("DELETE FROM PreventKeyWord2 WHERE id = :id")
    suspend fun deleteHistory(id: Int)

    @Query("DELETE FROM PreventKeyWord2")
    suspend fun deleteAllHistory()
}
