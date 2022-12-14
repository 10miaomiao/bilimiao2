package com.a10miaomiao.bilimiao.comm.apis

import android.os.Build
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import java.util.*

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


    /**
     * web端视频推荐
     */
    fun webRecommendList(
        idx: Long,
        flush: String = "5",
        column: String = "4",
        device: String = "pad",
        deviceName: String = Build.DEVICE,
    ) = MiaoHttp.request {
        // ?y_num=5&fresh_type=4&feed_version=V4&fresh_idx_1h=10&fetch_row=31&fresh_idx=10&brush=10&homepage_ver=1&ps=15&outside_trigger=
        // ?y_num=4&fresh_type=4&feed_version=V4&fresh_idx_1h=11&fetch_row=34&fresh_idx=11&brush=11&homepage_ver=1&ps=15&outside_trigger=
        // ?y_num=4&fresh_type=4&feed_version=V4&fresh_idx_1h=12&fetch_row=37&fresh_idx=12&brush=12&homepage_ver=1&ps=15&outside_trigger=
        // ?y_num=4&fresh_type=4&feed_version=V4&fresh_idx_1h=13&fetch_row=40&fresh_idx=13&brush=13&homepage_ver=1&ps=15&outside_trigger=
        url = BiliApiService.biliApi("x/web-interface/index/top/feed/rcmd",
           "y_num" to column,
            "fresh_type" to "4",
            "fresh_idx_1h" to idx.toString(),
            "feed_version" to "V4",
            "fetch_row" to "20",
            "fresh_idx" to idx.toString(),
            "brush" to "10",
            "homepage_ver" to "1",
            "ps" to "15",
            "outside_trigger" to "",
        )
    }

}