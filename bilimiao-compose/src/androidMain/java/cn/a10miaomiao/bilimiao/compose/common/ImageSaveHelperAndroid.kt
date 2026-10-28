package cn.a10miaomiao.bilimiao.compose.common

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

actual suspend fun saveImageBytes(fileName: String, bytes: ByteArray): SaveImageResult =
    withContext(Dispatchers.IO) {
        val context = PlatformProviders.context.platformContext as Context
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 起通过 MediaStore 写入，不需要任何存储权限
                SaveImageResult.Success(
                    fileName = saveImageToAlbumQ(context, fileName, bytes),
                    location = "系统相册",
                )
            } else {
                // Android 9 及以下写公共目录仍需要存储权限
                if (!hasWriteStoragePermission(context)) {
                    SaveImageResult.Failure("需要存储权限才能保存图片，请在系统设置中授予后重试")
                } else {
                    SaveImageResult.Success(
                        fileName = saveImageToPictures(context, fileName, bytes),
                        location = "系统相册",
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            SaveImageResult.Failure("保存失败：${e.message}")
        }
    }

actual fun getImageFileName(url: String): String {
    // 去掉查询串和锚点，避免把 "?x-oss-process=..." 之类拼进文件名
    val path = url.substringBefore('?').substringBefore('#')
    return path.split("/").last().takeIf { it.isNotBlank() } ?: DEFAULT_FILE_NAME
}

actual suspend fun fetchOriginalImageBytes(url: String): ByteArray? {
    val context = PlatformProviders.context.platformContext as Context
    val loader = SingletonImageLoader.get(context)
    val request = ImageRequest.Builder(context)
        .data(url)
        .build()
    // Execute to ensure image is in disk cache
    loader.execute(request)
    // Read raw bytes from disk cache
    val snapshot = loader.diskCache?.openSnapshot(url)
    return snapshot?.use { entry ->
        entry.data.toFile().readBytes()
    }
}

actual suspend fun copyImageToClipboard(fileName: String, bytes: ByteArray): Boolean =
    withContext(Dispatchers.IO) {
        val context = PlatformProviders.context.platformContext as Context
        try {
            // 剪切板只接受 URI，先把图片写到缓存目录再通过 FileProvider 暴露出去
            val dir = File(context.cacheDir, CLIPBOARD_IMAGE_DIR)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, sanitizeFileName(fileName))
            file.writeBytes(bytes)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newUri(context.contentResolver, fileName, uri))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

private const val CLIPBOARD_IMAGE_DIR = "clipboard"

/** 应用保存的图片统一放在 Pictures/Bilimiao，DCIM 是相机拍摄专用目录 */
private const val ALBUM_DIR_NAME = "Bilimiao"

private const val DEFAULT_FILE_NAME = "image"

/** 文件名主体保留的最大长度，避免标题过长撑爆文件名 */
private const val MAX_BASE_NAME_LENGTH = 64

/** 文件系统 / MediaStore 不接受的字符 */
private val ILLEGAL_FILE_NAME_CHARS = Regex("[\\\\/:*?\"<>|\\u0000-\\u001F]")

/** 已知图片格式与标准后缀的对应关系，用于纠正文件名后缀 */
private val EXTENSION_BY_MIME = mapOf(
    "image/jpeg" to "jpg",
    "image/png" to "png",
    "image/gif" to "gif",
    "image/webp" to "webp",
)

/**
 * Android 10+：通过 MediaStore 写入系统图库
 *
 * 按官方推荐的两段式写入：先以 IS_PENDING=1 插入，写完再置为 0，
 * 这样写入过程中其它应用和相册看不到半成品；写入失败则删除记录，
 * 避免图库中残留 0 字节的空图。
 *
 * @return 实际保存的文件名（MediaStore 遇到重名会自动加序号）
 */
private fun saveImageToAlbumQ(context: Context, fileName: String, bytes: ByteArray): String {
    val resolver = context.contentResolver
    val (mimeType, safeName) = resolveImageMeta(fileName, bytes)
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, safeName)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM_DIR_NAME")
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        ?: throw IOException("无法创建媒体库记录")
    try {
        val output = resolver.openOutputStream(uri) ?: throw IOException("无法打开输出流")
        output.use {
            it.write(bytes)
            it.flush()
        }
        resolver.update(
            uri,
            ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
            null,
            null,
        )
    } catch (e: Exception) {
        // 回滚已经插入的半成品记录
        runCatching { resolver.delete(uri, null, null) }
        throw e
    }
    return queryDisplayName(resolver, uri) ?: safeName
}

private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? = runCatching {
    resolver.query(uri, arrayOf(MediaStore.Images.Media.DISPLAY_NAME), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
}.getOrNull()

/**
 * Android 9 及以下：直接写入公共 Pictures 目录，再通知媒体扫描入库
 *
 * @return 实际保存的文件名（重名时自动加序号）
 */
@Suppress("DEPRECATION")
private fun saveImageToPictures(context: Context, fileName: String, bytes: ByteArray): String {
    if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
        throw IOException("外部存储不可用")
    }
    val dir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        ALBUM_DIR_NAME,
    )
    if (!dir.exists() && !dir.mkdirs()) throw IOException("无法创建目录：${dir.absolutePath}")
    val (_, safeName) = resolveImageMeta(fileName, bytes)
    val file = uniqueFile(dir, safeName)
    FileOutputStream(file).use { it.write(bytes) }
    MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
    return file.name
}

/** 重名时在文件名后追加序号，与 MediaStore 的命名策略保持一致 */
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

/**
 * 解析图片真实格式并生成合法文件名
 *
 * 网络图片的 URL 常带 "@960w_540h.webp"、"xxx.jpg?x-oss-process=..." 之类后缀，
 * 直接拿来当文件名会出现「.jpg 里装着 webp」的情况，部分相册会无法显示。
 *
 * @return MIME 类型与最终文件名
 */
private fun resolveImageMeta(fileName: String, bytes: ByteArray): Pair<String, String> {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    val mimeType = sniffMimeType(bytes) ?: getMimeType(extension)
    val baseName = sanitizeFileName(fileName.substringBeforeLast('.', fileName))
        .take(MAX_BASE_NAME_LENGTH)
    // 让后缀与真实格式保持一致
    val targetExtension = EXTENSION_BY_MIME[mimeType] ?: extension
    val name = if (targetExtension.isEmpty()) baseName else "$baseName.$targetExtension"
    return mimeType to name
}

/** 按文件头识别图片格式，识别不出来时才回退到后缀判断 */
private fun sniffMimeType(bytes: ByteArray): String? {
    fun byteAt(index: Int) = bytes[index].toInt() and 0xFF
    fun charAt(index: Int) = bytes[index].toInt()
    return when {
        bytes.size >= 3 && byteAt(0) == 0xFF && byteAt(1) == 0xD8 -> "image/jpeg"
        bytes.size >= 8 && byteAt(0) == 0x89 && charAt(1) == 'P'.code -> "image/png"
        bytes.size >= 6 && charAt(0) == 'G'.code && charAt(1) == 'I'.code &&
            charAt(2) == 'F'.code -> "image/gif"
        bytes.size >= 12 && charAt(0) == 'R'.code && charAt(1) == 'I'.code &&
            charAt(2) == 'F'.code && charAt(3) == 'F'.code &&
            charAt(8) == 'W'.code && charAt(9) == 'E'.code &&
            charAt(10) == 'B'.code && charAt(11) == 'P'.code -> "image/webp"
        else -> null
    }
}

private fun getMimeType(extension: String): String {
    if (extension.isNotEmpty()) {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.let { return it }
    }
    return "image/jpeg"
}

private fun sanitizeFileName(fileName: String): String =
    ILLEGAL_FILE_NAME_CHARS.replace(fileName, "_").trim().ifBlank { DEFAULT_FILE_NAME }

private fun hasWriteStoragePermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    ) == PackageManager.PERMISSION_GRANTED
