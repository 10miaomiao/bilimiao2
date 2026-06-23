package com.a10miaomiao.bilimiao.comm.platform

import java.util.Base64

class JvmBase64Provider : Base64Provider {
    override fun encodeToString(data: ByteArray, flags: Int): String {
        val encoder = if (flags and Base64Provider.NO_WRAP != 0) {
            Base64.getEncoder().withoutPadding()
        } else {
            Base64.getEncoder()
        }
        return encoder.encodeToString(data)
    }

    override fun decode(str: String, flags: Int): ByteArray {
        return if (flags and Base64Provider.NO_WRAP != 0) {
            Base64.getMimeDecoder().decode(str)
        } else {
            Base64.getDecoder().decode(str)
        }
    }
}
