package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class LiveApi {

    /**
     * 直播間信息
     */
    fun info(roomId: String) = MiaoHttp.request {
        url = "https://api.live.bilibili.com/room/v1/Room/get_info?room_id=${roomId}"
    }

}