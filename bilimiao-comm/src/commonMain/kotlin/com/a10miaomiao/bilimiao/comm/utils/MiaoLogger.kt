package com.a10miaomiao.bilimiao.comm.utils

expect class MiaoLogger(tag: String) {
    fun i(vararg msg: Any?): MiaoLogger
    fun d(vararg msg: Any?): MiaoLogger
    fun e(vararg msg: Any?): MiaoLogger
    infix fun info(msg: Any?): MiaoLogger
    infix fun debug(msg: Any?): MiaoLogger
    infix fun error(msg: Any?): MiaoLogger
}

expect fun Any.miaoLogger(): MiaoLogger
