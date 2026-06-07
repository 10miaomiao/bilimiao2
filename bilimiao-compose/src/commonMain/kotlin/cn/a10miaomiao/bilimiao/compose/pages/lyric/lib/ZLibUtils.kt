package cn.a10miaomiao.bilimiao.compose.pages.lyric.lib

//https://blog.csdn.net/qingzi635533/article/details/30231733
expect fun decompressZlib(data: ByteArray): ByteArray

object ZLibUtils {

    fun decompress(data: ByteArray): ByteArray {
        return try {
            decompressZlib(data)
        } catch (e: Exception) {
            e.printStackTrace()
            data
        }
    }
}
