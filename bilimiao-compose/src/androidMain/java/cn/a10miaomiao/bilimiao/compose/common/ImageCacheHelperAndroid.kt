package cn.a10miaomiao.bilimiao.compose.common

import android.content.Context
import coil3.ImageLoader
import com.a10miaomiao.bilimiao.comm.platform.PlatformProviders
import java.math.BigDecimal

actual fun getImageCacheSize(): String {
    val context = PlatformProviders.context.platformContext as Context
    val loader = ImageLoader(context)
    val diskCache = loader.diskCache ?: return "0Byte"
    return formatSize(diskCache.size)
}

actual fun clearImageCache() {
    val context = PlatformProviders.context.platformContext as Context
    val loader = ImageLoader(context)
    loader.diskCache?.clear()
    loader.memoryCache?.clear()
}

private fun formatSize(size: Long): String {
    val kiloByte = size / 1024
    if (kiloByte < 1) {
        return size.toString() + "Byte"
    }
    val megaByte = kiloByte / 1024
    if (megaByte < 1) {
        val result = BigDecimal(kiloByte.toString())
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
    }
    val gigaByte = megaByte / 1024
    if (gigaByte < 1) {
        val result = BigDecimal(megaByte.toString())
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
    }
    val teraBytes = gigaByte / 1024
    if (teraBytes < 1) {
        val result = BigDecimal(gigaByte.toString())
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
    }
    val result = BigDecimal(teraBytes)
    return result.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
}
