package cn.a10miaomiao.bilimiao.compose.pages.lyric.lib

//https://blog.csdn.net/qingzi635533/article/details/30231733  此处源码已修改
class KrcText {
    private val miarry = charArrayOf(
        '@', 'G', 'a', 'w', '^', '2', 't',
        'G', 'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i'
    )

    /**
     * @param zip_byte 去掉前四字节"krc1"后的部分
     * @return krc文件处理后的文本
     */
    fun getKrcText(zipByte: ByteArray): String {
        val j = zipByte.size
        for (k in 0 until j) {
            val l = k % 16
            zipByte[k] = (zipByte[k].toInt() xor miarry[l].code.toByte().toInt()).toByte()
        }
        return ZLibUtils.decompress(zipByte).decodeToString()
    }
}
