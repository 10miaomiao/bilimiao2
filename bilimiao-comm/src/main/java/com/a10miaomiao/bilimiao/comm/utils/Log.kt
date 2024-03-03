package com.a10miaomiao.bilimiao.comm.utils

typealias AndroidLog = android.util.Log
object Log {
    private val currentLevel: Int = AndroidLog.INFO
    fun info(msg: () -> String){
        // todo level check for better performance
        AndroidLog.i(msg::class.java.name.simpleName(), msg())
    }
    fun debug(msg: () -> String){
        AndroidLog.d(msg::class.java.name.simpleName(), msg())
    }
    // nested class in kotlin dont have simple name
    private fun String.simpleName() = substring(lastIndexOf('.')+1, indexOf("$"))

}