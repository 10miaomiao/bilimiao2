package com.a10miaomiao.bilimiao.comm.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_world")
data class FilterWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val keyword: String,
)
