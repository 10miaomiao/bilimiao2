package com.a10miaomiao.bilimiao.comm.entity.lyric

data class NeteaseSearchResultInfo(
    val result:NeteaseSearchResult,
    val code:Int,
)

data class NeteaseSearchResult(
    val songs:List<NeteaseSong>,
    val songCount:Int,
)

data class NeteaseSong(
    val id:String,
    val name:String,
    val duration: Int,
)