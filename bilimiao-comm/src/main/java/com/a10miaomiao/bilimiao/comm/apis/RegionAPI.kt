package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class RegionAPI {

    fun regions() = MiaoHttp.request {
        url = BiliApiService.biliApp(
            "x/v2/region/index",
        )
    }

}