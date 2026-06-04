package com.a10miaomiao.bilimiao.comm.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PreventKeyWord2")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyword: String,
    val type: String = "video",
)
