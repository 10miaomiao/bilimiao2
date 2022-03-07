package com.a10miaomiao.bilimiao.comm.utils

import android.util.Log

/**
 * Created by 10喵喵 on 2018/2/22.
 */
object DebugMiao {

    private const val TAG = "DebugMiao"

    private inline fun Any?.getString(): String {
        return this?.toString() ?: "null"
    }

    fun log(vararg str: Any?) {
        var message = StringBuilder(str[0].getString()).apply {
            for (i in 1 until str.size) {
                append(" , " + str[i].getString())
            }
        }
        Log.d(TAG, message.toString())
    }

    fun logw(vararg str: Any?) {
        Log.w(TAG, str.toString())
    }

    fun loge(vararg str: Any?) {
        Log.e(TAG, str.toString())
    }
}