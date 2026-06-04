package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class ArticleAPI {

    /**
     * 专栏信息
     */
    fun info(id: String) = MiaoHttp.request {
        url = BiliApiService.biliApi("x/article/viewinfo",
            "id" to id,
        )
    }

}