package cn.a10miaomiao.bilimiao.danmaku.parser

/**
 * 弹幕数据源接口
 *
 * @param T 数据类型
 */
interface IDataSource<T> {

    companion object {
        const val SCHEME_HTTP_TAG = "http"
        const val SCHEME_HTTPS_TAG = "https"
        const val SCHEME_FILE_TAG = "file"
    }

    /**
     * 获取数据
     */
    fun data(): T

    /**
     * 释放资源
     */
    fun release()
}
