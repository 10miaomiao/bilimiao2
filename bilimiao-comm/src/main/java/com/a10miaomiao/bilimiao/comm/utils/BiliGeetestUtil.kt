package com.a10miaomiao.bilimiao.comm.utils

import org.json.JSONObject

interface BiliGeetestUtil {

    fun startCustomFlow(gtCallBack: GTCallBack)

    data class GT3ResultBean(
        val geetest_challenge: String,
        val geetest_seccode: String,
        val geetest_validate: String,
    )

    interface GTCallBack {
        suspend fun onGTDialogResult(
            result: GT3ResultBean,
        ): Boolean
        suspend fun getGTApiJson(): JSONObject?
    }
}