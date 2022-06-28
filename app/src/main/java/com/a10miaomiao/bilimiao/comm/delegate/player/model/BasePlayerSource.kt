package com.a10miaomiao.bilimiao.comm.delegate.player.model

import com.a10miaomiao.bilimiao.comm.apis.PlayerAPI

interface BasePlayerSource {
    val title: String
    suspend fun getPlayerUrl(quality: Int): String

}