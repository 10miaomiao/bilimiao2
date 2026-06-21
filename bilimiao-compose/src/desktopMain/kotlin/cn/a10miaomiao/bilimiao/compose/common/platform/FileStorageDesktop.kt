package cn.a10miaomiao.bilimiao.compose.common.platform

import java.io.File
import java.io.IOException

class FileStorageDesktop(private val filesDir: File) : FileStorage {

    init {
        filesDir.mkdirs()
    }

    override fun readText(fileName: String): String? {
        return try {
            val file = File(filesDir, fileName)
            if (file.exists()) file.readText() else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun writeText(fileName: String, content: String) {
        try {
            File(filesDir, fileName).writeText(content)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun readBytes(fileName: String): ByteArray? {
        return try {
            val file = File(filesDir, fileName)
            if (file.exists()) file.readBytes() else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun writeBytes(fileName: String, content: ByteArray) {
        try {
            File(filesDir, fileName).writeBytes(content)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
