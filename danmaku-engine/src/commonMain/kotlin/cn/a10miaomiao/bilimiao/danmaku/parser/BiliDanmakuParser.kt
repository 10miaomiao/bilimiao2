package cn.a10miaomiao.bilimiao.danmaku.parser

import cn.a10miaomiao.bilimiao.danmaku.model.AlphaValue
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.Duration
import cn.a10miaomiao.bilimiao.danmaku.model.IDanmakus
import cn.a10miaomiao.bilimiao.danmaku.model.SpecialDanmaku
import cn.a10miaomiao.bilimiao.danmaku.collection.Danmakus
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuFactory
import cn.a10miaomiao.bilimiao.danmaku.platform.parseBiliDanmakuXml
import cn.a10miaomiao.bilimiao.danmaku.util.DanmakuUtils

/**
 * B站 XML 弹幕解析器
 *
 * 解析 B站弹幕 XML 格式，支持滚动弹幕、固定弹幕和特殊弹幕（含动画参数）。
 * 特殊弹幕的文本为 JSON 数组格式，包含位移、透明度、旋转、路径等动画参数。
 *
 * XML 格式示例:
 * ```xml
 * <i>
 *   <d p="23.826,1,25,16777215,1422201084,0,057075e9,757076900">弹幕文本</d>
 * </i>
 * ```
 *
 * p 属性格式: 时间(秒),类型,字号,颜色,时间戳,弹幕池id,用户hash,弹幕id
 */
class BiliDanmakuParser : BaseDanmakuParser() {

    override fun parse(): IDanmakus? {
        val source = mDataSource ?: return null
        val data = source.data()
        val bytes = when (data) {
            is ByteArray -> data
            else -> return null
        }

        val result = Danmakus(IDanmakus.ST_BY_TIME, false, mContext?.getBaseComparator())
        var currentItem: BaseDanmaku? = null
        var index = 0
        val textBuilder = StringBuilder()

        try {
            parseBiliDanmakuXml(
                input = bytes,
                onElementStart = { _, localName, _, attributes ->
                    val tagName = localName.lowercase().trim()
                    if (tagName == "d") {
                        // <d p="23.826000213623,1,25,16777215,1422201084,0,057075e9,757076900">文本</d>
                        // 0:时间(弹幕出现时间, 秒)
                        // 1:类型(1从右至左|6从左至右|5顶端固定|4底端固定|7特殊弹幕)
                        // 2:字号
                        // 3:颜色
                        // 4:时间戳
                        // 5:弹幕池id
                        // 6:用户hash
                        // 7:弹幕id
                        val pValue = attributes["p"] ?: return@parseBiliDanmakuXml
                        val values = pValue.split(",")
                        if (values.isNotEmpty()) {
                            val time = (parseFloat(values[0]) * 1000).toLong() // 秒转毫秒
                            val type = parseInteger(values[1]) // 弹幕类型
                            val textSize = parseFloat(values[2]) // 字体大小
                            val color = (0x00000000ff000000L or parseLong(values[3])).toInt() // 颜色
                            val userHash = if (values.size > 6) values[6] else null

                            val danmaku = mContext?.mDanmakuFactory?.createDanmaku(type, mContext!!)
                            if (danmaku != null) {
                                danmaku.setTime(time)
                                danmaku.textSize = textSize * (mDispDensity - 0.6f)
                                danmaku.textColor = color
                                // 根据颜色明度选择阴影色：深色文字用白色阴影，浅色文字用黑色阴影
                                danmaku.textShadowColor = if (isColorDark(color)) {
                                    COLOR_WHITE
                                } else {
                                    COLOR_BLACK
                                }
                                danmaku.userHash = userHash
                                danmaku.index = index++
                                currentItem = danmaku
                                textBuilder.clear()
                            }
                        }
                    }
                },
                onElementEnd = { _, localName, _ ->
                    val item = currentItem
                    if (item != null) {
                        val tagName = localName.lowercase().trim()
                        if (tagName == "d") {
                            // 处理累积的文本内容
                            val rawText = textBuilder.toString()
                            if (rawText.isNotEmpty()) {
                                val decoded = decodeXmlString(rawText)
                                DanmakuUtils.fillText(item, decoded)

                                // 解析特殊弹幕的 JSON 参数
                                val trimmedText = item.text?.toString()?.trim() ?: ""
                                if (item.getType() == BaseDanmaku.TYPE_SPECIAL
                                    && trimmedText.startsWith("[") && trimmedText.endsWith("]")
                                ) {
                                    parseSpecialDanmaku(item, trimmedText)
                                }
                            }

                            // 弹幕有效（有文本且有时长）则添加到结果集
                            if (item.text != null && item.duration != null) {
                                item.setTimer(mTimer)
                                item.flags = mContext?.mGlobalFlagValues
                                val lock = result.obtainSynchronizer()
                                synchronized(lock) {
                                    result.addItem(item)
                                }
                                mListener?.onDanmakuAdd(item)
                            }
                        }
                    }
                    currentItem = null
                },
                onCharacters = { text ->
                    // 累积文本内容，XML 解析器可能分多次回调
                    if (currentItem != null) {
                        textBuilder.append(text)
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 更新工厂视口状态
        if (mDisp != null && mContext != null) {
            mContext!!.mDanmakuFactory.updateViewportState(
                mDisp!!.width.toFloat(),
                mDisp!!.height.toFloat(),
                getViewportSizeFactor()
            )
        }

        mListener?.onDanmakuDataChanged()
        return result
    }

    /**
     * 解析特殊弹幕的 JSON 数组参数
     *
     * 格式: [beginX, beginY, alphaRange, duration, text, rotateZ, rotateY,
     *        endX, endY, translationDuration, delay, noStroke, easing, pathData]
     *
     * - beginX/beginY: 起始坐标（0.0~1.0 为百分比，>1 为像素）
     * - alphaRange: 透明度范围，如 "0-1" 表示从完全透明到完全不透明
     * - duration: 动画持续时间（秒）
     * - text: 显示文本
     * - rotateZ/rotateY: 旋转角度
     * - endX/endY: 结束坐标
     * - translationDuration: 位移动画时长（毫秒）
     * - delay: 位移开始延迟（毫秒）
     * - noStroke: "true" 表示无描边
     * - easing: "0" 为 Quadratic.easeOut，其他为 Linear.easeIn
     * - pathData: SVG 路径数据，如 "M0,0L100,100L200,0"
     */
    private fun parseSpecialDanmaku(danmaku: BaseDanmaku, text: String) {
        val textArr = parseJsonArray(text)
        if (textArr == null || textArr.size < 5 || textArr[4].isEmpty()) {
            return
        }

        // 设置显示文本
        DanmakuUtils.fillText(danmaku, textArr[4])

        var beginX = parseFloat(textArr[0])
        var beginY = parseFloat(textArr[1])
        var endX = beginX
        var endY = beginY

        // 解析透明度范围: "0.5-1.0" 或 "1.0"
        val alphaArr = textArr[2].split("-")
        val beginAlpha = (AlphaValue.MAX * parseFloat(alphaArr[0])).toInt()
        var endAlpha = beginAlpha
        if (alphaArr.size > 1) {
            endAlpha = (AlphaValue.MAX * parseFloat(alphaArr[1])).toInt()
        }

        val alphaDuration = (parseFloat(textArr[3]) * 1000).toLong()
        var translationDuration = alphaDuration
        var translationStartDelay = 0L
        var rotateY = 0f
        var rotateZ = 0f

        if (textArr.size >= 7) {
            rotateZ = parseFloat(textArr[5])
            rotateY = parseFloat(textArr[6])
        }

        if (textArr.size >= 11) {
            endX = parseFloat(textArr[7])
            endY = parseFloat(textArr[8])
            if (textArr[9].isNotEmpty()) {
                translationDuration = parseInteger(textArr[9]).toLong()
            }
            if (textArr[10].isNotEmpty()) {
                translationStartDelay = parseFloat(textArr[10]).toLong()
            }
        }

        // 百分比坐标转换为 B站播放器实际像素坐标
        if (isPercentageNumber(textArr[0])) {
            beginX *= DanmakuFactory.BILI_PLAYER_WIDTH
        }
        if (isPercentageNumber(textArr[1])) {
            beginY *= DanmakuFactory.BILI_PLAYER_HEIGHT
        }
        if (textArr.size >= 8 && isPercentageNumber(textArr[7])) {
            endX *= DanmakuFactory.BILI_PLAYER_WIDTH
        }
        if (textArr.size >= 9 && isPercentageNumber(textArr[8])) {
            endY *= DanmakuFactory.BILI_PLAYER_HEIGHT
        }

        danmaku.duration = Duration(alphaDuration)
        danmaku.rotationZ = rotateZ
        danmaku.rotationY = rotateY

        val ctx = mContext
        if (ctx != null) {
            ctx.mDanmakuFactory.fillTranslationData(
                danmaku, beginX, beginY, endX, endY,
                translationDuration, translationStartDelay
            )
            ctx.mDanmakuFactory.fillAlphaData(danmaku, beginAlpha, endAlpha, alphaDuration)
        }

        // 是否有描边（去除阴影）
        if (textArr.size >= 12) {
            if (textArr[11].isNotEmpty() && textArr[11].equals("true", ignoreCase = true)) {
                danmaku.textShadowColor = COLOR_TRANSPARENT
            }
        }

        // 字体（index 12，暂不处理）
        // if (textArr.size >= 13) { /* TODO: 字体 textArr[12] */ }

        // 缓动函数: "0" = Quadratic.easeOut, 其他 = Linear.easeIn
        if (textArr.size >= 14) {
            (danmaku as? SpecialDanmaku)?.isQuadraticEaseOut = (textArr[13] == "0")
        }

        // 路径数据: SVG 格式，如 "M0,0L100,100L200,0"
        if (textArr.size >= 15 && textArr[14].isNotEmpty()) {
            val motionPathString = textArr[14]
            if (motionPathString.isNotEmpty()) {
                // 移除开头的 "M" 命令符
                val innerPath = if (motionPathString.startsWith("M")) {
                    motionPathString.substring(1)
                } else {
                    motionPathString
                }
                val pointStrArray = innerPath.split("L")
                if (pointStrArray.isNotEmpty()) {
                    val points = Array(pointStrArray.size) { FloatArray(2) }
                    for (i in pointStrArray.indices) {
                        val pointArray = pointStrArray[i].split(",")
                        if (pointArray.size >= 2) {
                            points[i][0] = parseFloat(pointArray[0])
                            points[i][1] = parseFloat(pointArray[1])
                        }
                    }
                    ctx?.mDanmakuFactory?.fillLinePathData(danmaku, points)
                }
            }
        }
    }

    companion object {
        private const val COLOR_TRANSPARENT = 0x00000000
        private const val COLOR_WHITE = 0xFFFFFFFF.toInt()
        private const val COLOR_BLACK = 0xFF000000.toInt()

        /**
         * 判断颜色是否为深色（HSV 明度 < 0.1）
         *
         * 用于决定弹幕文字的阴影/描边颜色：
         * 深色文字使用白色阴影，浅色文字使用黑色阴影。
         */
        private fun isColorDark(color: Int): Boolean {
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val max = maxOf(r, g, b)
            return max / 255.0 < 0.1
        }

        /**
         * 解码 XML 实体字符
         */
        private fun decodeXmlString(title: String): String {
            var result = title
            if (result.contains("&amp;")) result = result.replace("&amp;", "&")
            if (result.contains("&quot;")) result = result.replace("&quot;", "\"")
            if (result.contains("&gt;")) result = result.replace("&gt;", ">")
            if (result.contains("&lt;")) result = result.replace("&lt;", "<")
            return result
        }

        /**
         * 判断是否为百分比数字
         *
         * B站特殊弹幕中，包含小数点的数字视为百分比（0.0~1.0），
         * 需要乘以播放器宽高转换为实际像素坐标。
         */
        private fun isPercentageNumber(number: String): Boolean {
            return number.contains(".")
        }

        /**
         * 安全解析浮点数
         */
        private fun parseFloat(floatStr: String): Float {
            return floatStr.trim().toFloatOrNull() ?: 0.0f
        }

        /**
         * 安全解析整数
         */
        private fun parseInteger(intStr: String): Int {
            return intStr.trim().toIntOrNull() ?: 0
        }

        /**
         * 安全长整型解析
         */
        private fun parseLong(longStr: String): Long {
            return longStr.trim().toLongOrNull() ?: 0L
        }

        /**
         * 简易 JSON 数组解析器
         *
         * 解析类似 [value1,value2,"string",value4] 格式的 JSON 数组。
         * 不依赖 kotlinx.serialization，手动处理字符串引号和逗号分隔。
         *
         * 支持的元素类型:
         * - 数字: 0.0, 6, -1.5
         * - 带引号字符串: "text content"
         * - 空字符串: ""
         *
         * @return 解析后的字符串列表，解析失败返回 null
         */
        private fun parseJsonArray(text: String): List<String>? {
            val trimmed = text.trim()
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return null
            val inner = trimmed.substring(1, trimmed.length - 1)
            val result = mutableListOf<String>()
            val current = StringBuilder()
            var inString = false
            var i = 0
            while (i < inner.length) {
                val c = inner[i]
                when {
                    c == '"' && !inString -> {
                        inString = true
                    }
                    c == '"' && inString -> {
                        // 检查是否为转义引号（JSON 中 \" 表示字面引号）
                        if (i + 1 < inner.length && inner[i + 1] == '"') {
                            current.append('"')
                            i++ // 跳过下一个引号
                        } else {
                            inString = false
                        }
                    }
                    c == ',' && !inString -> {
                        result.add(current.toString().trim())
                        current.clear()
                    }
                    else -> {
                        current.append(c)
                    }
                }
                i++
            }
            result.add(current.toString().trim())
            return result
        }
    }
}
