package com.a10miaomiao.bilimiao.comm.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_upper")
data class FilterUpperEntity(
    @PrimaryKey val mid: Long,
    val name: String,
)
