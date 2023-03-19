package com.a10miaomiao.bilimiao.comm.utils

import android.content.Context
import android.util.Base64
import com.a10miaomiao.bilimiao.comm.R
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object AESUtil {
    private val algorithm = "RuaiqMUBBA"
    private val opmode = 64
    private val key = "+d8HjZ"
//    private val key = "X6udIYkLsCvbSuY7+VYR+A=="

    fun mykey(getS: (Int) -> String): String{
        val m = getS(R.string.me)
        val mr = m + algorithm
        val e = (opmode - 3).toChar()
        val mr6 = mr + e
        return mr6 + e
    }

    fun messageWordKey(key: String): String{
        return String(arrayOf(0, 5, 8, 11).map{ key[it] }.toCharArray())
    }

    fun getKey(key: String,context: Context): SecretKey {
        val k = mykey(context::getString)
        val m = messageWordKey(key)
        val enCodeFormat = Base64.decode(this.key + m + k, Base64.DEFAULT)
        return SecretKeySpec(enCodeFormat, "AES")
    }

    /**
     * 解密
     */
    fun decrypt(content: ByteArray, secretKey: SecretKey): ByteArray {
        // 秘钥
        val enCodeFormat = secretKey.encoded
        // 创建AES秘钥
        val key = SecretKeySpec(enCodeFormat, "AES")
        // 创建密码器
        val cipher = Cipher.getInstance("AES")
        // 初始化解密器
        cipher.init(Cipher.DECRYPT_MODE, key)
        // 解密
        return cipher.doFinal(content)
    }

    /**
     * 加密
     */
    fun encrypt(content: ByteArray, secretKey: SecretKey): ByteArray {
        // 秘钥
        val enCodeFormat = secretKey.encoded
        // 创建AES秘钥
        val key = SecretKeySpec(enCodeFormat, "AES")
        // 创建密码器
        val cipher = Cipher.getInstance("AES")
        // 初始化加密器
        cipher.init(Cipher.ENCRYPT_MODE, key)
        // 加密
        return cipher.doFinal(content)
    }
}