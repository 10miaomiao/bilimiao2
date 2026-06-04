package com.a10miaomiao.bilimiao.comm.platform

interface Base64Provider {
    fun encodeToString(data: ByteArray, flags: Int): String
    fun decode(str: String, flags: Int): ByteArray

    companion object {
        const val DEFAULT = 0
        const val NO_PADDING = 1
        const val NO_WRAP = 2
    }
}
