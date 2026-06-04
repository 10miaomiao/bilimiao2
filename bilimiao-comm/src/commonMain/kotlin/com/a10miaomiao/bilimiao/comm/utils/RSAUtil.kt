package com.a10miaomiao.bilimiao.comm.utils

import com.a10miaomiao.bilimiao.comm.platform.Base64Provider
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher


object RSAUtil {

    fun rsaPassword(
        passport: String,
        key: String,
        rhash: String,
    ): String {
        val rsaKey = key.replace("-----BEGIN PUBLIC KEY-----\n", "")
            .replace("-----END PUBLIC KEY-----\n", "")
        val encrypt = RSAUtil.decryptByPublicKey(rhash + passport, rsaKey)
        return encrypt.replace("\n", "")
    }

    fun decryptByPublicKey(data: String, publicKey: String): String {
        val keyBytes = PlatformProviders.base64.decode(publicKey, Base64Provider.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")

        val pubKey = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        val mi = cipher.doFinal(data.toByteArray())

        return PlatformProviders.base64.encodeToString(mi, Base64Provider.DEFAULT)
    }

}
