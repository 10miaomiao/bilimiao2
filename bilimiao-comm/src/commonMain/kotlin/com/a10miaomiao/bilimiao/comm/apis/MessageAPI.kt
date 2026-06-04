package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class MessageAPI {

    /**
     * 获取未读消息
     */
    fun unread() = MiaoHttp.request {
        url = BiliApiService.biliApi("x/msgfeed/unread")
    }

    /**
     * 获取点赞消息
     */
    fun like(
        id: Long,
        time: Long,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/msgfeed/like",
            "id" to id.toString(),
            "like_time" to time.toString(),
        )
    }

    /**
     * 获取@我的消息.
     */
    fun at(
        id: Long,
        time: Long,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "/x/msgfeed/at",
            "id" to id.toString(),
            "at_time" to time.toString(),
        )
    }

    /**
     * 获取回复我的消息.
     */
    fun reply(
        id: Long,
        time: Long,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "x/msgfeed/reply",
            "id" to id.toString(),
            "reply_time" to time.toString(),
        )
    }

}