package cn.a10miaomiao.bilimiao.danmaku.context

import cn.a10miaomiao.bilimiao.danmaku.filter.DanmakuFilters
import cn.a10miaomiao.bilimiao.danmaku.filter.DanmakuFilters.IDanmakuFilter
import cn.a10miaomiao.bilimiao.danmaku.model.AbsDanmakuSync
import cn.a10miaomiao.bilimiao.danmaku.model.AlphaValue
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.GlobalFlagValues
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.cache.BaseCacheStuffer
import cn.a10miaomiao.bilimiao.danmaku.cache.CacheStufferProxy
import cn.a10miaomiao.bilimiao.danmaku.platform.DanmakuTypeface
import java.lang.ref.WeakReference

/**
 * 弹幕上下文，持有弹幕显示和过滤的所有配置
 */
class DanmakuContext {

    companion object {
        fun create(): DanmakuContext = DanmakuContext()
    }

    /**
     * 弹幕配置标签枚举
     */
    enum class DanmakuConfigTag {
        FT_DANMAKU_VISIBILITY,
        FB_DANMAKU_VISIBILITY,
        L2R_DANMAKU_VISIBILITY,
        R2L_DANMAKU_VISIBILIY,
        SPECIAL_DANMAKU_VISIBILITY,
        TYPEFACE,
        TRANSPARENCY,
        SCALE_TEXTSIZE,
        MAXIMUM_NUMS_IN_SCREEN,
        DANMAKU_STYLE,
        DANMAKU_BOLD,
        COLOR_VALUE_WHITE_LIST,
        USER_ID_BLACK_LIST,
        USER_HASH_BLACK_LIST,
        SCROLL_SPEED_FACTOR,
        BLOCK_GUEST_DANMAKU,
        DUPLICATE_MERGING_ENABLED,
        MAXIMUN_LINES,
        OVERLAPPING_ENABLE,
        ALIGN_BOTTOM,
        DANMAKU_MARGIN,
        DANMAKU_SYNC;

        fun isVisibilityRelatedTag(): Boolean {
            return this == FT_DANMAKU_VISIBILITY || this == FB_DANMAKU_VISIBILITY
                    || this == L2R_DANMAKU_VISIBILITY || this == R2L_DANMAKU_VISIBILIY
                    || this == SPECIAL_DANMAKU_VISIBILITY || this == COLOR_VALUE_WHITE_LIST
                    || this == USER_ID_BLACK_LIST
        }
    }

    /**
     * 配置变更回调接口
     */
    interface ConfigChangedCallback {
        fun onDanmakuConfigChanged(config: DanmakuContext, tag: DanmakuConfigTag,
                                   vararg value: Any?): Boolean
    }

    /**
     * 默认字体
     */
    var mFont: DanmakuTypeface? = null

    /**
     * paint alpha: 0-255
     */
    var transparency: Int = AlphaValue.MAX

    var scaleTextSize: Float = 1.0f

    var margin: Int = 0

    /**
     * 弹幕显示隐藏设置
     */
    var FTDanmakuVisibility: Boolean = true
    var FBDanmakuVisibility: Boolean = true
    var L2RDanmakuVisibility: Boolean = true
    var R2LDanmakuVisibility: Boolean = true
    var SpecialDanmakuVisibility: Boolean = true

    private val mFilterTypes: MutableList<Int> = mutableListOf()

    /**
     * 同屏弹幕数量 -1 按绘制效率自动调整 0 无限制 n 同屏最大显示n个弹幕
     */
    var maximumNumsInScreen: Int = -1

    /**
     * 默认滚动速度系数
     */
    var scrollSpeedFactor: Float = 1.0f

    var danmakuSync: AbsDanmakuSync? = null

    private val mColorValueWhiteList: MutableList<Int> = mutableListOf()
    private val mUserIdBlackList: MutableList<Int> = mutableListOf()
    private val mUserHashBlackList: MutableList<String> = mutableListOf()

    private var mCallbackList: MutableList<WeakReference<ConfigChangedCallback>>? = null

    private var mBlockGuestDanmaku: Boolean = false
    private var mDuplicateMergingEnable: Boolean = false
    private var mIsAlignBottom: Boolean = false

    var mCacheStuffer: BaseCacheStuffer? = null
        private set

    private var mIsMaxLinesLimited: Boolean = false
    private var mIsPreventOverlappingEnabled: Boolean = false

    /**
     * 弹幕显示器（通过依赖注入提供，不自行创建）
     */
    lateinit var mDisplayer: IDisplayer

    var mGlobalFlagValues: GlobalFlagValues = GlobalFlagValues()

    var mDanmakuFilters: DanmakuFilters = DanmakuFilters()

    var mDanmakuFactory: DanmakuFactory = DanmakuFactory.create()

    var cachingPolicy: CachingPolicy = CachingPolicy.POLICY_DEFAULT

    private var mBaseComparator: IDanmakus.BaseComparator? = null

    /**
     * 0 默认 Choreographer驱动DrawHandler线程刷新
     * 1 "DFM Update"单独线程刷新
     * 2 DrawHandler线程自驱动刷新
     */
    var updateMethod: Byte = 0

    fun getBaseComparator(): IDanmakus.BaseComparator? = mBaseComparator

    fun setBaseComparator(baseComparator: IDanmakus.BaseComparator?) {
        mBaseComparator = baseComparator
    }

    fun getDisplayer(): IDisplayer = mDisplayer

    /**
     * 设置字体
     */
    fun setTypeface(font: DanmakuTypeface?): DanmakuContext {
        if (mFont != font) {
            mFont = font
            mDisplayer.clearTextHeightCache()
            mDisplayer.setTypeFace(font)
            notifyConfigureChanged(DanmakuConfigTag.TYPEFACE)
        }
        return this
    }

    /**
     * 设置弹幕透明度
     * @param p 透明度比例 (0.0~1.0)
     */
    fun setDanmakuTransparency(p: Float): DanmakuContext {
        val newTransparency = (p * AlphaValue.MAX).toInt()
        if (newTransparency != transparency) {
            transparency = newTransparency
            mDisplayer.setTransparency(newTransparency)
            notifyConfigureChanged(DanmakuConfigTag.TRANSPARENCY, p)
        }
        return this
    }

    /**
     * 设置弹幕文字缩放
     * @param p 缩放比例
     */
    fun setScaleTextSize(p: Float): DanmakuContext {
        if (scaleTextSize != p) {
            scaleTextSize = p
            mDisplayer.clearTextHeightCache()
            mDisplayer.setScaleTextSizeFactor(p)
            mGlobalFlagValues.updateMeasureFlag()
            mGlobalFlagValues.updateVisibleFlag()
            notifyConfigureChanged(DanmakuConfigTag.SCALE_TEXTSIZE, p)
        }
        return this
    }

    /**
     * 设置弹幕间距
     * @param m 间距像素
     */
    fun setDanmakuMargin(m: Int): DanmakuContext {
        if (margin != m) {
            margin = m
            mDisplayer.setMargin(m)
            mGlobalFlagValues.updateFilterFlag()
            mGlobalFlagValues.updateVisibleFlag()
            notifyConfigureChanged(DanmakuConfigTag.DANMAKU_MARGIN, m)
        }
        return this
    }

    /**
     * 设置顶部间距
     * @param m 间距像素
     */
    fun setMarginTop(m: Int): DanmakuContext {
        mDisplayer.setAllMarginTop(m)
        return this
    }

    /**
     * 设置是否显示顶部弹幕
     */
    fun setFTDanmakuVisibility(visible: Boolean): DanmakuContext {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_FIX_TOP)
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes)
        mGlobalFlagValues.updateFilterFlag()
        if (FTDanmakuVisibility != visible) {
            FTDanmakuVisibility = visible
            notifyConfigureChanged(DanmakuConfigTag.FT_DANMAKU_VISIBILITY, visible)
        }
        return this
    }

    private fun <T> setFilterData(tag: String, data: T) {
        setFilterData(tag, data, true)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> setFilterData(tag: String, data: T, primary: Boolean) {
        val filter = mDanmakuFilters.get(tag, primary) as IDanmakuFilter<T>
        filter.setData(data)
    }

    private fun setDanmakuVisible(visible: Boolean, type: Int) {
        if (visible) {
            mFilterTypes.remove(type)
        } else if (!mFilterTypes.contains(type)) {
            mFilterTypes.add(type)
        }
    }

    /**
     * 设置是否显示底部弹幕
     */
    fun setFBDanmakuVisibility(visible: Boolean): DanmakuContext {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_FIX_BOTTOM)
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes)
        mGlobalFlagValues.updateFilterFlag()
        if (FBDanmakuVisibility != visible) {
            FBDanmakuVisibility = visible
            notifyConfigureChanged(DanmakuConfigTag.FB_DANMAKU_VISIBILITY, visible)
        }
        return this
    }

    /**
     * 设置是否显示左右滚动弹幕
     */
    fun setL2RDanmakuVisibility(visible: Boolean): DanmakuContext {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_SCROLL_LR)
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes)
        mGlobalFlagValues.updateFilterFlag()
        if (L2RDanmakuVisibility != visible) {
            L2RDanmakuVisibility = visible
            notifyConfigureChanged(DanmakuConfigTag.L2R_DANMAKU_VISIBILITY, visible)
        }
        return this
    }

    /**
     * 设置是否显示右左滚动弹幕
     */
    fun setR2LDanmakuVisibility(visible: Boolean): DanmakuContext {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_SCROLL_RL)
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes)
        mGlobalFlagValues.updateFilterFlag()
        if (R2LDanmakuVisibility != visible) {
            R2LDanmakuVisibility = visible
            notifyConfigureChanged(DanmakuConfigTag.R2L_DANMAKU_VISIBILIY, visible)
        }
        return this
    }

    /**
     * 设置是否显示特殊弹幕
     */
    fun setSpecialDanmakuVisibility(visible: Boolean): DanmakuContext {
        setDanmakuVisible(visible, BaseDanmaku.TYPE_SPECIAL)
        setFilterData(DanmakuFilters.TAG_TYPE_DANMAKU_FILTER, mFilterTypes)
        mGlobalFlagValues.updateFilterFlag()
        if (SpecialDanmakuVisibility != visible) {
            SpecialDanmakuVisibility = visible
            notifyConfigureChanged(DanmakuConfigTag.SPECIAL_DANMAKU_VISIBILITY, visible)
        }
        return this
    }

    /**
     * 设置同屏弹幕密度 -1自动 0无限制
     * @param maxSize 最大数量
     */
    fun setMaximumVisibleSizeInScreen(maxSize: Int): DanmakuContext {
        maximumNumsInScreen = maxSize
        // 无限制
        if (maxSize == 0) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_QUANTITY_DANMAKU_FILTER)
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_ELAPSED_TIME_FILTER)
            notifyConfigureChanged(DanmakuConfigTag.MAXIMUM_NUMS_IN_SCREEN, maxSize)
            return this
        }
        // 自动调整
        if (maxSize == -1) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_QUANTITY_DANMAKU_FILTER)
            mDanmakuFilters.registerFilter(DanmakuFilters.TAG_ELAPSED_TIME_FILTER)
            notifyConfigureChanged(DanmakuConfigTag.MAXIMUM_NUMS_IN_SCREEN, maxSize)
            return this
        }
        setFilterData(DanmakuFilters.TAG_QUANTITY_DANMAKU_FILTER, maxSize)
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.MAXIMUM_NUMS_IN_SCREEN, maxSize)
        return this
    }

    /**
     * 设置描边样式
     *
     * @param style DANMAKU_STYLE_NONE DANMAKU_STYLE_SHADOW 或
     *              DANMAKU_STYLE_STROKEN 或 DANMAKU_STYLE_PROJECTION
     * @param values
     *        DANMAKU_STYLE_SHADOW 阴影模式下，values传入阴影半径
     *        DANMAKU_STYLE_STROKEN 描边模式下，values传入描边宽度
     *        DANMAKU_STYLE_PROJECTION 投影模式下，values传入offsetX, offsetY, alpha
     */
    fun setDanmakuStyle(style: Int, vararg values: Float): DanmakuContext {
        mDisplayer.setDanmakuStyle(style, values)
        notifyConfigureChanged(DanmakuConfigTag.DANMAKU_STYLE, style, values)
        return this
    }

    /**
     * 设置是否粗体显示,对某些字体无效
     * @param bold 是否粗体
     */
    fun setDanmakuBold(bold: Boolean): DanmakuContext {
        mDisplayer.setFakeBoldText(bold)
        notifyConfigureChanged(DanmakuConfigTag.DANMAKU_BOLD, bold)
        return this
    }

    /**
     * 设置色彩过滤弹幕白名单
     * @param colors 颜色值数组
     */
    fun setColorValueWhiteList(vararg colors: Int?): DanmakuContext {
        mColorValueWhiteList.clear()
        if (colors.isEmpty()) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_TEXT_COLOR_DANMAKU_FILTER)
        } else {
            colors.filterNotNullTo(mColorValueWhiteList)
            setFilterData(DanmakuFilters.TAG_TEXT_COLOR_DANMAKU_FILTER, mColorValueWhiteList)
        }
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.COLOR_VALUE_WHITE_LIST, mColorValueWhiteList)
        return this
    }

    fun getColorValueWhiteList(): List<Int> = mColorValueWhiteList

    /**
     * 设置屏蔽弹幕用户hash
     * @param hashes 用户hash数组
     */
    fun setUserHashBlackList(vararg hashes: String?): DanmakuContext {
        mUserHashBlackList.clear()
        if (hashes.isEmpty()) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_USER_HASH_FILTER)
        } else {
            hashes.filterNotNullTo(mUserHashBlackList)
            setFilterData(DanmakuFilters.TAG_USER_HASH_FILTER, mUserHashBlackList)
        }
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.USER_HASH_BLACK_LIST, mUserHashBlackList)
        return this
    }

    fun removeUserHashBlackList(vararg hashes: String?): DanmakuContext {
        if (hashes.isEmpty()) return this
        for (hash in hashes) {
            if (hash != null) {
                mUserHashBlackList.remove(hash)
            }
        }
        setFilterData(DanmakuFilters.TAG_USER_HASH_FILTER, mUserHashBlackList)
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.USER_HASH_BLACK_LIST, mUserHashBlackList)
        return this
    }

    /**
     * 添加屏蔽用户hash
     * @param hashes 用户hash数组
     */
    fun addUserHashBlackList(vararg hashes: String?): DanmakuContext {
        if (hashes.isEmpty()) return this
        hashes.filterNotNullTo(mUserHashBlackList)
        setFilterData(DanmakuFilters.TAG_USER_HASH_FILTER, mUserHashBlackList)
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.USER_HASH_BLACK_LIST, mUserHashBlackList)
        return this
    }

    fun getUserHashBlackList(): List<String> = mUserHashBlackList

    /**
     * 设置屏蔽弹幕用户id, 0 表示游客弹幕
     * @param ids 用户id数组
     */
    fun setUserIdBlackList(vararg ids: Int?): DanmakuContext {
        mUserIdBlackList.clear()
        if (ids.isEmpty()) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_USER_ID_FILTER)
        } else {
            ids.filterNotNullTo(mUserIdBlackList)
            setFilterData(DanmakuFilters.TAG_USER_ID_FILTER, mUserIdBlackList)
        }
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.USER_ID_BLACK_LIST, mUserIdBlackList)
        return this
    }

    fun removeUserIdBlackList(vararg ids: Int?): DanmakuContext {
        if (ids.isEmpty()) return this
        for (id in ids) {
            if (id != null) {
                mUserIdBlackList.remove(id)
            }
        }
        setFilterData(DanmakuFilters.TAG_USER_ID_FILTER, mUserIdBlackList)
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.USER_ID_BLACK_LIST, mUserIdBlackList)
        return this
    }

    /**
     * 添加屏蔽用户
     * @param ids 用户id数组
     */
    fun addUserIdBlackList(vararg ids: Int?): DanmakuContext {
        if (ids.isEmpty()) return this
        ids.filterNotNullTo(mUserIdBlackList)
        setFilterData(DanmakuFilters.TAG_USER_ID_FILTER, mUserIdBlackList)
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.USER_ID_BLACK_LIST, mUserIdBlackList)
        return this
    }

    fun getUserIdBlackList(): List<Int> = mUserIdBlackList

    /**
     * 设置是否屏蔽游客弹幕
     * @param block true屏蔽，false不屏蔽
     */
    fun blockGuestDanmaku(block: Boolean): DanmakuContext {
        if (mBlockGuestDanmaku != block) {
            mBlockGuestDanmaku = block
            if (block) {
                setFilterData(DanmakuFilters.TAG_GUEST_FILTER, block)
            } else {
                mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_GUEST_FILTER)
            }
            mGlobalFlagValues.updateFilterFlag()
            notifyConfigureChanged(DanmakuConfigTag.BLOCK_GUEST_DANMAKU, block)
        }
        return this
    }

    /**
     * 设置弹幕滚动速度系数,只对滚动弹幕有效
     * @param p 速度系数
     */
    fun setScrollSpeedFactor(p: Float): DanmakuContext {
        if (scrollSpeedFactor != p) {
            scrollSpeedFactor = p
            mDanmakuFactory.updateDurationFactor(p)
            mGlobalFlagValues.updateMeasureFlag()
            mGlobalFlagValues.updateVisibleFlag()
            notifyConfigureChanged(DanmakuConfigTag.SCROLL_SPEED_FACTOR, p)
        }
        return this
    }

    /**
     * 设置是否启用合并重复弹幕
     * @param enable 是否启用
     */
    fun setDuplicateMergingEnabled(enable: Boolean): DanmakuContext {
        if (mDuplicateMergingEnable != enable) {
            mDuplicateMergingEnable = enable
            mGlobalFlagValues.updateFilterFlag()
            notifyConfigureChanged(DanmakuConfigTag.DUPLICATE_MERGING_ENABLED, enable)
        }
        return this
    }

    fun isDuplicateMergingEnabled(): Boolean = mDuplicateMergingEnable

    /**
     * 设置弹幕底部对齐
     * @param enable 是否启用
     */
    fun alignBottom(enable: Boolean): DanmakuContext {
        if (mIsAlignBottom != enable) {
            mIsAlignBottom = enable
            notifyConfigureChanged(DanmakuConfigTag.ALIGN_BOTTOM, enable)
            mGlobalFlagValues.updateVisibleFlag()
        }
        return this
    }

    fun isAlignBottom(): Boolean = mIsAlignBottom

    /**
     * 设置最大显示行数
     * @param pairs map<K,V> 设置null取消行数限制
     *        K = (BaseDanmaku.TYPE_SCROLL_RL|BaseDanmaku.TYPE_SCROLL_LR|BaseDanmaku.TYPE_FIX_TOP|BaseDanmaku.TYPE_FIX_BOTTOM)
     *        V = 最大行数
     */
    fun setMaximumLines(pairs: Map<Int, Int>?): DanmakuContext {
        mIsMaxLinesLimited = (pairs != null)
        if (pairs == null) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_MAXIMUN_LINES_FILTER, false)
        } else {
            setFilterData(DanmakuFilters.TAG_MAXIMUN_LINES_FILTER, pairs, false)
        }
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.MAXIMUN_LINES, pairs)
        return this
    }

    /**
     * 设置防弹幕重叠
     * @param pairs map<K,V> 设置null恢复默认设置,默认为允许重叠
     *        K = (BaseDanmaku.TYPE_SCROLL_RL|BaseDanmaku.TYPE_SCROLL_LR|BaseDanmaku.TYPE_FIX_TOP|BaseDanmaku.TYPE_FIX_BOTTOM)
     *        V = true|false 是否重叠
     */
    fun preventOverlapping(pairs: Map<Int, Boolean>?): DanmakuContext {
        mIsPreventOverlappingEnabled = (pairs != null)
        if (pairs == null) {
            mDanmakuFilters.unregisterFilter(DanmakuFilters.TAG_OVERLAPPING_FILTER, false)
        } else {
            setFilterData(DanmakuFilters.TAG_OVERLAPPING_FILTER, pairs, false)
        }
        mGlobalFlagValues.updateFilterFlag()
        notifyConfigureChanged(DanmakuConfigTag.OVERLAPPING_ENABLE, pairs)
        return this
    }

    @Deprecated("使用 preventOverlapping 替代", ReplaceWith("preventOverlapping(pairs)"))
    fun setOverlapping(pairs: Map<Int, Boolean>?): DanmakuContext {
        return preventOverlapping(pairs)
    }

    fun isMaxLinesLimited(): Boolean = mIsMaxLinesLimited

    fun isPreventOverlappingEnabled(): Boolean = mIsPreventOverlappingEnabled

    /**
     * 设置缓存绘制填充器
     * @param cacheStuffer 填充器
     * @param cacheStufferAdapter 填充器代理
     */
    fun setCacheStuffer(cacheStuffer: BaseCacheStuffer?, cacheStufferAdapter: CacheStufferProxy?): DanmakuContext {
        this.mCacheStuffer = cacheStuffer
        if (this.mCacheStuffer != null) {
            this.mCacheStuffer!!.setProxy(cacheStufferAdapter)
        }
        return this
    }

    /**
     * 设置弹幕同步器
     * @param danmakuSync 同步器
     */
    fun setDanmakuSync(danmakuSync: AbsDanmakuSync?): DanmakuContext {
        this.danmakuSync = danmakuSync
        return this
    }

    /**
     * 设置缓存策略
     * @param cachingPolicy 缓存策略
     */
    fun setCachingPolicy(cachingPolicy: CachingPolicy): DanmakuContext {
        this.cachingPolicy = cachingPolicy
        return this
    }

    fun registerConfigChangedCallback(listener: ConfigChangedCallback?) {
        if (listener == null || mCallbackList == null) {
            mCallbackList = java.util.Collections.synchronizedList(mutableListOf<WeakReference<ConfigChangedCallback>>())
        }
        mCallbackList?.let { list ->
            for (configReferer in list) {
                if (listener == configReferer.get()) {
                    return
                }
            }
            list.add(WeakReference(listener!!))
        }
    }

    fun unregisterConfigChangedCallback(listener: ConfigChangedCallback?) {
        if (listener == null || mCallbackList == null) return
        mCallbackList?.let { list ->
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val ref = iterator.next()
                if (listener == ref.get()) {
                    iterator.remove()
                    return
                }
            }
        }
    }

    fun unregisterAllConfigChangedCallbacks() {
        mCallbackList?.clear()
        mCallbackList = null
    }

    private fun notifyConfigureChanged(tag: DanmakuConfigTag, vararg values: Any?) {
        mCallbackList?.let { list ->
            for (configReferer in list) {
                val cb = configReferer.get()
                cb?.onDanmakuConfigChanged(this, tag, *values)
            }
        }
    }

    fun registerFilter(filter: DanmakuFilters.BaseDanmakuFilter<*>): DanmakuContext {
        mDanmakuFilters.registerFilter(filter)
        mGlobalFlagValues.updateFilterFlag()
        return this
    }

    fun unregisterFilter(filter: DanmakuFilters.BaseDanmakuFilter<*>): DanmakuContext {
        mDanmakuFilters.unregisterFilter(filter)
        mGlobalFlagValues.updateFilterFlag()
        return this
    }

    fun resetContext(): DanmakuContext {
        mGlobalFlagValues = GlobalFlagValues()
        mDanmakuFilters.clear()
        mDanmakuFactory = DanmakuFactory.create()
        return this
    }
}
