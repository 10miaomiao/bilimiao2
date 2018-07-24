package com.a10miaomiao.bilimiao.utils

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

/**
 * Created by 10喵喵 on 2018/2/22.
 */
object DebugMiao : AnkoLogger {
    fun log(str: Any?) {
        info(str)
    }

    fun logw(str: Any?) {
        warn(str)
    }

    fun loge(str: Any?) {
        error(str)
    }
}