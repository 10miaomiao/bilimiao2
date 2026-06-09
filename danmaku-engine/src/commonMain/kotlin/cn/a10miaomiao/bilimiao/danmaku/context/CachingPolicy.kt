package cn.a10miaomiao.bilimiao.danmaku.context

/**
 * 缓存策略
 * 提供缓存相关的策略设置:
 * 1. 缓存格式 ARGB_4444 / ARGB_8888
 * 2. 缓存池总容量大小百分比系数 (0.0~1.0)
 * 3. 过期缓存回收频率
 * 4. 缓存回收条件内存占比阈值
 * 5. 可复用缓存尺寸调节
 *
 * @param bitsPerPixelOfCache 缓存bitmap的格式, ARGB_4444=16 ARGB_8888=32
 * @param maxCachePoolSizeFactorPercentage 0.0~1.0, 超过0.5的话有OOM风险
 * @param periodOfRecycle 回收周期 (ms), 0=自动, -1=不回收
 * @param forceRecyleThreshold 内存占用大小超过总容量一定比例值的缓存会被主动回收
 * @param reusableOffsetPixel 可复用缓存偏移像素
 */
class CachingPolicy(
    /** 缓存bitmap的格式, ARGB_4444=16 ARGB_8888=32 */
    var bitsPerPixelOfCache: Int = BMP_BPP_ARGB_8888,
    /** 缓存池总容量大小百分比系数 (0.0~1.0), 超过0.5的话有OOM风险 */
    var maxCachePoolSizeFactorPercentage: Float = 0.3f,
    /**
     * 回收周期
     * @see CACHE_PERIOD_AUTO 0: 默认
     * @see CACHE_PERIOD_NOT_RECYCLE -1: 不回收
     */
    var periodOfRecycle: Long = CACHE_PERIOD_AUTO,
    /**
     * 内存占用大小超过总容量一定比例值(forceRecyleThreshold值)的缓存,
     * 在回收时进行主动回收,忽略CACHE_PERIOD_NOT_RECYCLE
     */
    var forceRecyleThreshold: Float = 0.01f,
    /** 可复用缓存偏移像素 */
    var reusableOffsetPixel: Int = 0
) {

    companion object {
        const val BMP_BPP_ARGB_4444 = 16
        const val BMP_BPP_ARGB_8888 = 32
        const val CACHE_PERIOD_AUTO = 0L
        const val CACHE_PERIOD_NOT_RECYCLE = -1L

        val POLICY_LAZY = CachingPolicy(
            BMP_BPP_ARGB_8888, 0.3f, CACHE_PERIOD_AUTO, 0.5f, 50
        )
        val POLICY_GREEDY = CachingPolicy(
            BMP_BPP_ARGB_8888, 0.5f, CACHE_PERIOD_NOT_RECYCLE, 0.5f, 50
        )
        val POLICY_DEFAULT = POLICY_LAZY
    }

    /** 最大严格可复用查找次数 */
    var maxTimesOfStrictReusableFinds: Int = 20

    /** 最大可复用查找次数 */
    var maxTimesOfReusableFinds: Int = 150
}
