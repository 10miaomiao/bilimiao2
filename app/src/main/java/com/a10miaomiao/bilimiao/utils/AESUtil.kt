package com.a10miaomiao.bilimiao.utils

import android.content.Context
import android.util.Base64
import com.a10miaomiao.bilimiao.R
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object AESUtil {
    private val key = "X6udIYkLsCvbSuY7+VYR+A=="

    fun getKey(key: String, context: Context): SecretKey {
        val enCodeFormat = Base64.decode(this.key, Base64.DEFAULT)
        return SecretKeySpec(enCodeFormat, "AES")
    }

    /**
     * 解密
     */
    fun decrypt(content: ByteArray, secretKey: SecretKey): String {
        // 秘钥
        val enCodeFormat = secretKey.encoded
        // 创建AES秘钥
        val key = SecretKeySpec(enCodeFormat, "AES")
        // 创建密码器
        val cipher = Cipher.getInstance("AES")
        // 初始化解密器
        cipher.init(Cipher.DECRYPT_MODE, key)
        // 解密
        return String(cipher.doFinal(content))
    }

    /**
     * 加密
     */
    fun encrypt(content: String, secretKey: SecretKey): ByteArray {
        // 秘钥
        val enCodeFormat = secretKey.encoded
        // 创建AES秘钥
        val key = SecretKeySpec(enCodeFormat, "AES")
        // 创建密码器
        val cipher = Cipher.getInstance("AES")
        // 初始化加密器
        cipher.init(Cipher.ENCRYPT_MODE, key)
        // 加密
        return cipher.doFinal(content.toByteArray())
    }
}