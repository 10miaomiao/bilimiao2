package com.a10miaomiao.bilimiao.comm.apis

import android.os.Build
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import java.util.*

class HomeApi {

    /**
     * 视频信息
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
            "pull" to isPull.toString().lowercase(Locale.getDefault()),
        )
//        var request = await _httpProvider.GetRequestMessageAsync(
//                HttpMethod.Get,
//        Home.Recommend,
//        queryParameters,
//        Models.Enums.RequestClientType.IOS);
//        var response = await _httpProvider.SendAsync(request);
//        var data = await _httpProvider.ParseAsync<ServerResponse<HomeRecommendInfo>>(response);
//        return data.Data.Items.Where(p => !string.IsNullOrEmpty(p.Goto)).ToList();
//        )
    }

}