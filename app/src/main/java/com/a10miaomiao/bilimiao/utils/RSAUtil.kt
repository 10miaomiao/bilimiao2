package com.a10miaomiao.bilimiao.utils

import android.R.attr.data
import android.util.Base64.NO_WRAP
import android.R.attr.publicKey
import android.util.Base64
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher


object RSAUtil {

    fun decryptByPublicKey(data: String, publicKey: String): String {
        val keyBytes = Base64.decode(publicKey, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")

        val pubKey = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)
        val mi = cipher.doFinal(data.toByteArray())

        return Base64.encodeToString(mi, Base64.DEFAULT)
    }

}