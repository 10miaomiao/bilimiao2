package com.a10miaomiao.bilimiao.comm.apis

import android.os.Build
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.home.HomeRecommendInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeApi {

    /**
     * 视频推荐
     */

    fun recommendList(
        idx: Long,
        flush: String = "5",
        column: String = "4",
        device: String = "pad",
        deviceName: String = Build.DEVICE,
    ) = MiaoHttp.request {
        val isPull = idx == 0L
        url = BiliApiService.biliApp("x/v2/feed/index",
            "idx" to idx.toString(),
            "flush" to flush,
            "column" to column,
            "device" to device,
            "device_name" to deviceName,
            "device_type" to "0",
            "pull" to isPull.toString().lowercase(Locale.getDefault()),
        )
    }
    suspend fun recommendListAwait(
        idx: Long,
        flush: String = "5",
        column: String = "4",
        device: String = "pad",
        deviceName: String = Build.DEVICE,
    ): ResultInfo<HomeRecommendInfo> = withContext(Dispatchers.IO) {
        return@withContext recommendList(idx, flush, column, device, deviceName).awaitCall().gson<ResultInfo<HomeRecommendInfo>>()
    }

}