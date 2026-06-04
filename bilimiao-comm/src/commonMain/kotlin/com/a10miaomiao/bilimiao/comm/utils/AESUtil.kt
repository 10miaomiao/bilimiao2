package com.a10miaomiao.bilimiao.comm.utils

import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object AESUtil {
    private val algorithm = "RuaiqMUBBA"
    private val opmode = 64
    private val key = "+d8HjZ"

    fun mykey(): String {
        val m = "My"
        val mr = m + algorithm
        val e = (opmode - 3).toChar()
        val mr6 = mr + e
        return mr6 + e
    }

    fun messageWordKey(key: String): String {
        return String(arrayOf(0, 5, 8, 11).map{ key[it] }.toCharArray())
    }

    fun getKey(key: String): SecretKey {
        val k = mykey()
        val m = messageWordKey(key)
        val enCodeFormat = PlatformProviders.base64.decode(this.key + m + k, com.a10miaomiao.bilimiao.comm.platform.Base64Provider.DEFAULT)
        return SecretKeySpec(enCodeFormat, "AES")
    }

    fun decrypt(content: ByteArray, secretKey: SecretKey): ByteArray {
        val enCodeFormat = secretKey.encoded
        val key = SecretKeySpec(enCodeFormat, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(content)
    }

    fun encrypt(content: ByteArray, secretKey: SecretKey): ByteArray {
        val enCodeFormat = secretKey.encoded
        val key = SecretKeySpec(enCodeFormat, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(content)
    }
}
