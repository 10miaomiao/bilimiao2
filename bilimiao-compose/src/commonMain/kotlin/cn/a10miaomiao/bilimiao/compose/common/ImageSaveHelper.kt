package cn.a10miaomiao.bilimiao.compose.common

expect fun saveImageBytes(fileName: String, bytes: ByteArray): Boolean

expect fun getImageFileName(url: String): String

expect suspend fun fetchOriginalImageBytes(url: String): ByteArray?

/**
 * 将图片数据复制到系统剪切板
 *
 * @param fileName 图片文件名，用于推断图片格式
 * @return 是否复制成功
 */
expect suspend fun copyImageToClipboard(fileName: String, bytes: ByteArray): Boolean
