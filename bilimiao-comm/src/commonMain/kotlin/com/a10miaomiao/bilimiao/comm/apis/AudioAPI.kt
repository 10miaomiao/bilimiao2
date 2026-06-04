package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class AudioAPI {

    /**
     * 音频信息
     */
    fun info(id: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("audio/music-service-c/songs/playing",
            "song_id" to id,
        )
    }

}