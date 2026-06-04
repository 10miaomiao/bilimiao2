package com.a10miaomiao.bilimiao.comm.platform

import android.util.Base64

class AndroidBase64Provider : Base64Provider {
    override fun encodeToString(data: ByteArray, flags: Int): String {
        return Base64.encodeToString(data, flags)
    }

    override fun decode(str: String, flags: Int): ByteArray {
        return Base64.decode(str, flags)
    }
}
