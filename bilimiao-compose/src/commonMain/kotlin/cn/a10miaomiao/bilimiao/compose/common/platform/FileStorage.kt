package cn.a10miaomiao.bilimiao.compose.common.platform

/**
 * 跨平台文件存储接口
 * 抽象 Context.openFileInput/openFileOutput 操作
 */
interface FileStorage {
    fun readText(fileName: String): String?
    fun writeText(fileName: String, content: String)
    fun readBytes(fileName: String): ByteArray?
    fun writeBytes(fileName: String, content: ByteArray)
}
