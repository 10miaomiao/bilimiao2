package com.a10miaomiao.bilimiao.comm.utils

import kotlin.experimental.xor

/**
 * 简单加解密
 * 原理：a=a^b^b
 */
class MiaoEncryptDecrypt(
    val key: ByteArray,
) {

    /**
     * 加密
     */
    fun encrypt(original: ByteArray): ByteArray {
        val encryptByte = ByteArray(original.size)
        original.forEachIndexed { i, b ->
            encryptByte[i] = b xor key[i % key.size]
        }
        return encryptByte
    }

    /**
     * 解密
     */
    fun decrypt(original: ByteArray): ByteArray {
        return encrypt(original)
    }
}