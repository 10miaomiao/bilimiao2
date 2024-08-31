package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class ArchiveApi {

    fun relation(aid: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/web-interface/archive/relation",
            "aid" to aid
        )
    }

}