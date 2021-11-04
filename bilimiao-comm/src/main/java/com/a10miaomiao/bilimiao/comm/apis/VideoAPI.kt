package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class VideoAPI {

    /**
     * 视频信息
     */
    fun info(id: String, type: String = "AV",) = MiaoHttp.request {
        url = BiliApiService.biliApp("x/v2/view",
            (if (type == "AV") "aid" else "bvid") to id,
            "autoplay" to "0",
            "qn" to "32"
        )
    }


}