package com.a10miaomiao.bilimiao.comm.utils

import android.content.pm.ApplicationInfo
import android.util.Log
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp

/**
 * Created by 10喵喵 on 2018/2/22.
 */
object DebugMiao {

    private const val TAG = "DebugMiao"

    private var isDebug: Boolean? = null

    private inline fun Any?.getString(): String {
        return this?.toString() ?: "null"
    }

    fun log(vararg str: Any?) {
        if (isDebug == null) {
            isDebug = BilimiaoCommApp.commApp.app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE !== 0
        } else if (isDebug == false){
            return
        }
        var message = StringBuilder(str[0].getString()).apply {
            for (i in 1 until str.size) {
                append(" , " + str[i].getString())
            }
        }.toString()
        val maxLength = 2001 - TAG.length
        //大于4000时
        while (message.length > maxLength) {
            Log.d(TAG, message.substring(0, maxLength))
            message = message.substring(maxLength)
        }
        Log.d(TAG, message)
    }

    fun i(tag: String, msg: String) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        var msg = msg

    }

    fun logw(vararg str: Any?) {
        Log.w(TAG, str.toString())
    }

    fun loge(vararg str: Any?) {
        Log.e(TAG, str.toString())
    }
}