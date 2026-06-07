package cn.a10miaomiao.bilimiao.compose.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

expect fun saveImageBytes(fileName: String, bytes: ByteArray): Boolean

expect fun getImageFileName(url: String): String

suspend fun fetchImageBytes(url: String): ByteArray? {
    return withContext(Dispatchers.Default) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            response.body?.bytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
