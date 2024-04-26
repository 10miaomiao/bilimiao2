package com.a10miaomiao.bilimiao.comm.entity.lyric

data class KugouAccessKeyItem(
    val errcode:Int,
    val errmsg:String,
    val candidates:List<KugouCandidates>
)

data class KugouCandidates(
    val id:Int,
    val accesskey:String,
)