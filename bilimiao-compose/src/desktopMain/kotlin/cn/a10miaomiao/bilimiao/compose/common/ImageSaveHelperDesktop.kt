package cn.a10miaomiao.bilimiao.compose.common

import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import java.io.File

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
