package cn.a10miaomiao.bilimiao.compose.common

import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
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
import javax.imageio.ImageIO

actual fun saveImageBytes(fileName: String, bytes: ByteArray): Boolean {
    return try {
        val userHome = System.getProperty("user.home")
        val dir = File(userHome, "Pictures/Bilimiao")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        if (file.exists()) {
            GlobalToaster.show("图片已存在")
            return true
        }
        file.writeBytes(bytes)
        GlobalToaster.show("已保存至 ${file.absolutePath}")
        true
    } catch (e: Exception) {
        e.printStackTrace()
        GlobalToaster.show("保存失败：${e.message}")
        false
    }
}

actual fun getImageFileName(url: String): String {
    return url.split("/").last().takeIf { it.isNotEmpty() } ?: "image.png"
}

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
