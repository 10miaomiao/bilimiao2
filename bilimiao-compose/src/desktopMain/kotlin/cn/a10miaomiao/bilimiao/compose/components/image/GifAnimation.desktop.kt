package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import java.io.File
import java.security.MessageDigest
import org.jetbrains.skia.Image as SkiaImage

/** 动图解码后的最长边上限（像素），避免大尺寸 GIF 占用过多内存 */
internal const val MAX_DECODE_SIZE = 512

/** 大图预览场景的解码上限，比列表更大的尺寸保证清晰度 */
internal const val PREVIEW_MAX_DECODE_SIZE = 1280

/** 已解码帧的缓存数量 */
internal const val FRAME_CACHE_SIZE = 8

/** GIF 帧间隔的取值范围（毫秒），规避损坏文件里的 0 或超大值 */
internal const val MIN_FRAME_DELAY = 20
internal const val MAX_FRAME_DELAY = 1000

/** 原始字节内存缓存上限 */
private const val BYTES_CACHE_LIMIT = 32L * 1024 * 1024

/** 单个文件写入磁盘缓存的上限 */
private const val MAX_DISK_CACHE_FILE = 8L * 1024 * 1024

/** 磁盘缓存总大小上限 */
private const val DISK_CACHE_LIMIT = 64L * 1024 * 1024

private const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

private val httpClient by lazy { OkHttpClient() }

private val bytesCache = object : LinkedHashMap<String, ByteArray>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ByteArray>?): Boolean {
        var total = 0L
        for (value in values) {
            total += value.size
        }
        return total > BYTES_CACHE_LIMIT
    }
}

private val diskCacheDir: File by lazy {
    val baseDir = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) {
        System.getenv("LOCALAPPDATA")?.let { File(it) } ?: File(System.getProperty("user.home"))
    } else {
        File(System.getProperty("user.home"), ".cache")
    }
    File(baseDir, "bilimiao/image_cache").apply { mkdirs() }
}

internal fun String.looksLikeGif(): Boolean = contains(".gif", ignoreCase = true)

private fun cachedBytes(url: String): ByteArray? = synchronized(bytesCache) { bytesCache[url] }

private fun putBytes(url: String, bytes: ByteArray) {
    if (bytes.size > BYTES_CACHE_LIMIT) return
    synchronized(bytesCache) { bytesCache[url] = bytes }
}

private fun diskCacheFile(url: String): File {
    val digest = MessageDigest.getInstance("MD5").digest(url.toByteArray())
    val name = digest.joinToString("") { "%02x".format(it.toInt() and 0xff) }
    return File(diskCacheDir, name)
}

/**
 * 下载（或从缓存读取）动图原始字节。
 * 仅桌面端使用，带内存 + 磁盘两级缓存。
 */
internal suspend fun loadImageBytes(url: String): ByteArray? = withContext(Dispatchers.IO) {
    cachedBytes(url)?.let { return@withContext it }

    val cacheFile = diskCacheFile(url)
    if (cacheFile.isFile && cacheFile.length() > 0L) {
        val cached = runCatching { cacheFile.readBytes() }.getOrNull()
        if (cached != null && cached.isNotEmpty()) {
            putBytes(url, cached)
            return@withContext cached
        }
    }

    val request = Request.Builder()
        .url(url)
        .header("User-Agent", USER_AGENT)
        .header("Referer", "https://www.bilibili.com/")
        .build()
    val bytes = runCatching {
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@use null
            }
            response.body?.bytes()
        }
    }.getOrNull() ?: return@withContext null

    putBytes(url, bytes)
    if (bytes.size <= MAX_DISK_CACHE_FILE) {
        runCatching {
            cacheFile.writeBytes(bytes)
            trimDiskCache()
        }
    }
    bytes
}

private fun trimDiskCache() {
    val files = diskCacheDir.listFiles() ?: return
    var total = files.sumOf { it.length() }
    if (total <= DISK_CACHE_LIMIT) return
    files.sortedBy { it.lastModified() }.forEach { file ->
        if (total <= DISK_CACHE_LIMIT / 2) {
            return@forEach
        }
        total -= file.length()
        file.delete()
    }
}

/**
 * 基于 skiko Codec 的 GIF 解码器，按帧懒解码并缓存最近若干帧。
 * 仅应在单线程（播放协程）中访问。
 */
internal class GifAnimation(
    private val codec: Codec,
    val frameCount: Int,
    private val durations: IntArray,
    val width: Int,
    val height: Int,
) {
    private val frameCache = object : LinkedHashMap<Int, ImageBitmap>(FRAME_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, ImageBitmap>?): Boolean =
            size > FRAME_CACHE_SIZE
    }

    fun duration(index: Int): Int = durations[index]

    @Synchronized
    fun frame(index: Int): ImageBitmap {
        frameCache[index]?.let { return it }
        val bitmap = Bitmap()
        bitmap.allocN32Pixels(width, height)
        codec.readPixels(bitmap, index)
        val imageBitmap = SkiaImage.makeFromBitmap(bitmap).toComposeImageBitmap()
        frameCache[index] = imageBitmap
        return imageBitmap
    }

    fun close() {
        synchronized(this) {
            frameCache.clear()
            codec.close()
        }
    }

    companion object {
        fun decode(bytes: ByteArray, maxSize: Int = MAX_DECODE_SIZE): GifAnimation? {
            val codec = try {
                Codec.makeFromData(Data.makeFromBytes(bytes))
            } catch (e: Throwable) {
                return null
            }
            val frameCount = codec.frameCount
            if (frameCount <= 0) {
                codec.close()
                return null
            }
            val durations = IntArray(frameCount) { codec.getFrameInfo(it).duration }
            val imageInfo = codec.imageInfo
            var width = imageInfo.width.coerceAtLeast(1)
            var height = imageInfo.height.coerceAtLeast(1)
            // Skia 的 GIF 解码只支持整数倍缩放，按 1/2 逐级降到上限以内
            while (maxOf(width, height) > maxSize && width >= 2 && height >= 2) {
                width /= 2
                height /= 2
            }
            // 少数编码器不支持缩放解码，此时回退到原始尺寸
            if (!supportsSize(codec, width, height)) {
                width = imageInfo.width.coerceAtLeast(1)
                height = imageInfo.height.coerceAtLeast(1)
            }
            return GifAnimation(
                codec = codec,
                frameCount = frameCount,
                durations = durations,
                width = width,
                height = height,
            )
        }

        private fun supportsSize(codec: Codec, width: Int, height: Int): Boolean {
            return try {
                val bitmap = Bitmap()
                bitmap.allocN32Pixels(width, height)
                codec.readPixels(bitmap, 0)
                bitmap.close()
                true
            } catch (e: Throwable) {
                false
            }
        }
    }
}
