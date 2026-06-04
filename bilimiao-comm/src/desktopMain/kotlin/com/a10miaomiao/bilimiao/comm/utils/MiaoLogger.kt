package com.a10miaomiao.bilimiao.comm.utils

import java.util.logging.Level
import java.util.logging.Logger

actual class MiaoLogger actual constructor(
    private val tag: String,
) {
    private val logger = Logger.getLogger(tag)

    actual fun i(vararg msg: Any?): MiaoLogger {
        logger.log(Level.INFO, formatMsg(*msg))
        return this
    }

    actual fun d(vararg msg: Any?): MiaoLogger {
        logger.log(Level.FINE, formatMsg(*msg))
        return this
    }

    actual fun e(vararg msg: Any?): MiaoLogger {
        logger.log(Level.SEVERE, formatMsg(*msg))
        return this
    }

    actual infix fun info(msg: Any?): MiaoLogger {
        logger.log(Level.INFO, msg?.toString() ?: "null")
        return this
    }

    actual infix fun debug(msg: Any?): MiaoLogger {
        logger.log(Level.FINE, msg?.toString() ?: "null")
        return this
    }

    actual infix fun error(msg: Any?): MiaoLogger {
        logger.log(Level.SEVERE, msg?.toString() ?: "null")
        return this
    }

    private fun Any?.string(): String {
        return when (this) {
            null -> "null"
            is String -> this
            else -> toString()
        }
    }

    private fun formatMsg(vararg msgs: Any?): String {
        return if (msgs.size > 1) {
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
    }
}

actual fun Any.miaoLogger(): MiaoLogger {
    val tag = this::class.java.name.run {
        substring(lastIndexOf('.') + 1, length)
    }
    return MiaoLogger("Miao>$tag")
}
