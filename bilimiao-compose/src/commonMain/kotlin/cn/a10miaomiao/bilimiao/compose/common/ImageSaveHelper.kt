package cn.a10miaomiao.bilimiao.compose.common

expect fun saveImageBytes(fileName: String, bytes: ByteArray): Boolean

expect fun getImageFileName(url: String): String

expect suspend fun fetchOriginalImageBytes(url: String): ByteArray?
