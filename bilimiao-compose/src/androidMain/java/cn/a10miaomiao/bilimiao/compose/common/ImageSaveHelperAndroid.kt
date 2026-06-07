package cn.a10miaomiao.bilimiao.compose.common

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import java.io.File
import java.io.FileOutputStream

actual fun saveImageBytes(fileName: String, bytes: ByteArray): Boolean {
    val context = PlatformProviders.context.platformContext as Context
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToAlbumQ(context, fileName, bytes)
        } else {
            saveImageToFile(context, fileName, bytes)
        }
        GlobalToaster.show("已保存，文件名: $fileName")
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

private fun saveImageToAlbumQ(context: Context, fileName: String, bytes: ByteArray) {
    val mimeType = getMimeType(fileName)
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/Bilimiao")
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
    }
    val uri = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
    )
    if (uri != null) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(bytes)
            output.flush()
        }
    }
}

private fun saveImageToFile(context: Context, fileName: String, bytes: ByteArray) {
    val path = Environment.getExternalStorageDirectory().path + "/BiliMiao/bili图片/"
    File(path).let { if (!it.exists()) it.mkdirs() }
    val outFile = File(path + fileName)
    if (outFile.exists()) {
        GlobalToaster.show("图片已存在")
        return
    }
    FileOutputStream(outFile).use { it.write(bytes) }
    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
        data = Uri.fromFile(outFile)
    }
    context.sendBroadcast(intent)
}

private fun getMimeType(fileName: String): String {
    return when {
        fileName.uppercase().endsWith(".PNG") -> "image/png"
        fileName.uppercase().endsWith(".GIF") -> "image/gif"
        fileName.uppercase().endsWith(".WEBP") -> "image/webp"
        else -> "image/jpeg"
    }
}
