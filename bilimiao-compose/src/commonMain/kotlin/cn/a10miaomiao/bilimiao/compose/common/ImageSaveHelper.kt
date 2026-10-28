package cn.a10miaomiao.bilimiao.compose.common

/**
 * 保存图片的对外结果
 *
 * 保存实现不直接弹提示：同一个函数既可能被主界面调用，也可能被独立页面（如封面页
 * CoverActivity）调用，而各页面持有的提示组件不同，所以由调用方按自己的方式展示。
 */
sealed interface SaveImageResult {
    /**
     * 保存成功
     *
     * @param fileName 实际保存的文件名（可能被系统重命名）
     * @param location 保存位置的展示文案，如「系统相册」或目录绝对路径
     */
    data class Success(val fileName: String, val location: String) : SaveImageResult

    /** 保存失败，[message] 为可直接展示给用户的提示 */
    data class Failure(val message: String) : SaveImageResult
}

/**
 * 保存图片到系统相册（Android）或图片目录（桌面端）
 *
 * 文件名会被清洗成合法文件名，并按图片真实格式修正后缀。
 *
 * @param fileName 期望的文件名
 * @param bytes 图片数据
 * @return 保存结果，由调用方负责把结果提示给用户
 */
expect suspend fun saveImageBytes(fileName: String, bytes: ByteArray): SaveImageResult

expect fun getImageFileName(url: String): String

expect suspend fun fetchOriginalImageBytes(url: String): ByteArray?

/**
 * 将图片数据复制到系统剪切板
 *
 * @param fileName 图片文件名，用于推断图片格式
 * @return 是否复制成功
 */
expect suspend fun copyImageToClipboard(fileName: String, bytes: ByteArray): Boolean
