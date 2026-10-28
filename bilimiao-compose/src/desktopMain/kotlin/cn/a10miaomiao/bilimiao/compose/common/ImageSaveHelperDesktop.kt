package cn.a10miaomiao.bilimiao.compose.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

actual suspend fun saveImageBytes(fileName: String, bytes: ByteArray): SaveImageResult =
    withContext(Dispatchers.IO) {
        try {
            val userHome = System.getProperty("user.home")
            val dir = File(userHome, "Pictures/Bilimiao")
            if (!dir.exists() && !dir.mkdirs()) throw IOException("无法创建目录：${dir.absolutePath}")
            val file = uniqueFile(dir, sanitizeFileName(fileName))
            file.writeBytes(bytes)
            SaveImageResult.Success(
                fileName = file.name,
                location = file.parent.orEmpty(),
            )
        } catch (e: Exception) {
            e.printStackTrace()
            SaveImageResult.Failure("保存失败：${e.message}")
        }
    }

actual fun getImageFileName(url: String): String {
    // 去掉查询串和锚点，避免把 "?x-oss-process=..." 之类拼进文件名
    val path = url.substringBefore('?').substringBefore('#')
    return path.split("/").last().takeIf { it.isNotBlank() } ?: "image"
}

/** 重名时在文件名后追加序号，避免同名图片互相覆盖 */
private fun uniqueFile(dir: File, fileName: String): File {
    val baseName = fileName.substringBeforeLast('.', fileName)
    val extension = fileName.substringAfterLast('.', "")
    var file = File(dir, fileName)
    var index = 1
    while (file.exists()) {
        file = File(
            dir,
            if (extension.isEmpty()) "$baseName ($index)" else "$baseName ($index).$extension",
        )
        index++
    }
    return file
}

/** 去掉 Windows 文件名里不允许的字符（桌面端需兼容各平台文件系统） */
private fun sanitizeFileName(fileName: String): String =
    fileName.replace(ILLEGAL_FILE_NAME_CHARS, "_").trim().ifBlank { "image" }

private val ILLEGAL_FILE_NAME_CHARS = Regex("[\\\\/:*?\"<>|\\u0000-\\u001F]")

actual suspend fun fetchOriginalImageBytes(url: String): ByteArray? {
    return withContext(Dispatchers.IO) {
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

actual suspend fun copyImageToClipboard(fileName: String, bytes: ByteArray): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val image = ImageIO.read(ByteArrayInputStream(bytes)) ?: return@withContext false
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(ImageSelection(image), null)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * 剪切板中的图片传输对象
 */
private class ImageSelection(private val image: Image) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
        flavor == DataFlavor.imageFlavor

    override fun getTransferData(flavor: DataFlavor): Any {
        if (flavor != DataFlavor.imageFlavor) {
            throw UnsupportedFlavorException(flavor)
        }
        return image
    }
}
