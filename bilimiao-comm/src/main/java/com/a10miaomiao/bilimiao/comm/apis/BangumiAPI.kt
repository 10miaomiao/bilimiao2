package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class BangumiAPI {

    /**
     * 番剧信息
     */
    fun seasonInfo(seasonId: String) = MiaoHttp.request {
        url = BiliApiService.biliBangumi("view/api/season",
            "season_id" to seasonId
        )
    }

    /**
     * 剧集信息
     */
    fun episodeInfo(epId: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/view/app/season",
            "ep_id" to epId)
    }


}