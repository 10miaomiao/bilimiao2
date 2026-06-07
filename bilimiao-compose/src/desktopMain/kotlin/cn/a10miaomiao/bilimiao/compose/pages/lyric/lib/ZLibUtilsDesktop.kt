package cn.a10miaomiao.bilimiao.compose.pages.lyric.lib

import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

actual fun decompressZlib(data: ByteArray): ByteArray {
    val decompresser = Inflater()
    decompresser.reset()
    decompresser.setInput(data)
    val o = ByteArrayOutputStream(data.size)
    try {
        val buf = ByteArray(1024)
        while (!decompresser.finished()) {
            val i = decompresser.inflate(buf)
            o.write(buf, 0, i)
        }
        return o.toByteArray()
    } finally {
        try {
            o.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        decompresser.end()
    }
}
