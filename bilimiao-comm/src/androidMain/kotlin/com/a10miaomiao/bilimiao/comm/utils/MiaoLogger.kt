package com.a10miaomiao.bilimiao.comm.utils

import android.util.Log

actual class MiaoLogger actual constructor(
    private val tag: String,
) {
    private val maxLength = 2000 - tag.length
    private var level = Log.DEBUG

    actual fun i(vararg msg: Any?): MiaoLogger{
        level = Log.INFO
        println(*msg)
        return this
    }

    actual fun d(vararg msg: Any?): MiaoLogger{
        level = Log.DEBUG
        println(*msg)
        return this
    }

    actual fun e(vararg msg: Any?): MiaoLogger{
        level = Log.ERROR
        println(*msg)
        return this
    }

    actual infix fun info(msg: Any?): MiaoLogger{
        level = Log.INFO
        println(msg)
        return this
    }

    actual infix fun debug(msg: Any?): MiaoLogger{
        level = Log.DEBUG
        println(msg)
        return this
    }

    actual infix fun error(msg: Any?): MiaoLogger{
        level = Log.ERROR
        println(msg)
        return this
    }

    private fun Any?.string(): String {
        return when (this) {
            null -> "null"
            is String -> this
            else -> toString()
        }
    }

    private fun println(vararg msgs: Any?) {
        var msgStr = if (msgs.size > 1) {
            StringBuilder().apply {
                msgs.forEachIndexed { index, msg ->
                    if (msg is Pair<*, *>) {
                        append("[${msg.first.string()}]:")
                        append(msg.second.string())
                    } else {
                        append("[$index]")
                        append(msg.string())
                    }
                    append("\n")
                }
            }.toString()
        } else {
            msgs[0].string()
        }
        while (msgStr.length > maxLength) {
            Log.println(level, tag, msgStr.substring(0, maxLength))
            msgStr = msgStr.substring(maxLength)
        }
        Log.println(level, tag, msgStr)
    }
}

actual fun Any.miaoLogger(): MiaoLogger {
    val tag = this::class.java.name.run {
        substring(lastIndexOf('.') + 1, length)
    }
    return MiaoLogger("Miao>$tag")
}
