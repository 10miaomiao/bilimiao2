package com.a10miaomiao.bilimiao.comm.apis

import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp

class UserBangumiAPI {

    fun followList(
        status: Int, // 1:想看 2:在看 3:已看
        pageNum: Int,
        pageSize: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi(
            "pgc/app/follow/v2/bangumi",
            "status" to status.toString(),
            "pn" to pageNum.toString(),
            "ps" to pageSize.toString()
        )
    }

    fun followAdd(
        seasonId: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/app/follow/add")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "season_id" to seasonId,
        )
    }

    fun followUpdate(
        seasonId: String,
        status: Int,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/app/follow/status/update")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "season_id" to seasonId,
            "status" to status.toString(),
        )
    }

    fun followDel(
        seasonId: String,
    ) = MiaoHttp.request {
        url = BiliApiService.biliApi("pgc/app/follow/del")
        method = MiaoHttp.POST
        formBody = ApiHelper.createParams(
            "season_id" to seasonId,
        )
    }

}