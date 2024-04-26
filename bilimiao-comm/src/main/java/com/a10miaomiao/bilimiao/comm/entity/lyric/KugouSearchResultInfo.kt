package com.a10miaomiao.bilimiao.comm.entity.lyric

import master.flame.danmaku.danmaku.model.Duration

data class KugouSearchResultInfo(
    val data:KugouResultData,
    val errcode:Int,
    val status:Int,
    val error:String,
)


data class KugouResultData(
    val timestamp:Int,
    val info:List<KugouResultItemInfo>,
)

data class KugouResultItemInfo(
    val hash: String,
    val filename: String,
    val duration: Int,
)