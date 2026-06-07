package cn.a10miaomiao.bilimiao.compose.common.platform

import android.content.Context
import java.io.IOException

class FileStorageAndroid(
    private val context: Context,
) : FileStorage {

    override fun readText(fileName: String): String? {
        return try {
            context.openFileInput(fileName).use { it.reader().readText() }
        } catch (e: IOException) {
            null
        }
    }

    override fun writeText(fileName: String, content: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(content.toByteArray())
        }
    }

    override fun readBytes(fileName: String): ByteArray? {
        return try {
            context.openFileInput(fileName).use { it.readBytes() }
        } catch (e: IOException) {
            null
        }
    }

    override fun writeBytes(fileName: String, content: ByteArray) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(content)
        }
    }
}
