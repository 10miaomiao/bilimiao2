package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import kotlin.math.round

/**
 * CC 字幕样式规格（用户设置的原始值）与渲染解析结果。
 *
 * 字幕样式有**两个互相独立的单位设置**：
 * - **字号单位**（[fontSizeUnitMode]）：固定值 sp，或按画面**短边**（`min(宽, 高)`）百分比；
 * - **距底部距离单位**（[bottomMarginUnitMode]）：固定值 dp，或按画面**高度**百分比。
 *
 * 两者可以自由组合，例如「字号按画面短边百分比（大屏自动放大）+ 距底用固定值 dp」，
 * 渲染时由 [resolve] 分别按各自的单位换算，互不影响。
 *
 * 固定值不随画面尺寸变化（旧版行为）；百分比模式下画面变大时该尺寸同比放大，
 * 小窗/悬浮窗里也不会糊成一片或大到遮画面。
 *
 * 字号的百分比基准是画面**短边**而不是高度：竖屏播放（包括本身是竖屏的视频）时短边是宽度，
 * 若仍按高度换算，又高又窄的画面会把字放大到离谱、直接撑爆屏幕；按短边换算则字号随宽度缩放，
 * 横屏/竖屏的观感保持一致。距底距离是垂直方向的长度，语义上只能相对高度，因此仍按画面高度换算。
 *
 * [SubtitleStyleSpec] 只保存用户在设置页里的原始取值（未 clamp），真正绘制前由 [resolve]
 * 按当前画面宽高换算出 [ResolvedSubtitleStyle]；设置页预览则用 [fontSizeFraction] /
 * [bottomMarginFraction] 换算成比例，按预览框的短边/高度渲染。这样设置页与播放器共用同一套
 * 换算规则，做到所见即所得。
 *
 * 本文件只依赖 `Dp` / `TextUnit` / `Density` 这些 Compose 基础类型，不依赖 @Composable，
 * 便于在设置页、播放器乃至其它模块复用。
 *
 * @param fontSizeUnitMode 字号单位，取值见 [SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_FIXED] /
 * [SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT]
 * @param bottomMarginUnitMode 距底部距离单位，取值同上
 * @param fontSizeSp 字号（sp），字号单位为固定值时生效
 * @param bottomMarginDp 距画面底部距离（dp），距底单位为固定值时生效
 * @param fontSizePercent 字号占画面短边（宽高中的较小者）的比例（0.045 表示 4.5%），
 * 字号单位为百分比时生效
 * @param bottomMarginPercent 距画面底部距离占画面高度的比例（0.035 表示 3.5%），距底单位为百分比时生效
 */
data class SubtitleStyleSpec(
    val fontSizeUnitMode: Int = SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_DEFAULT,
    val bottomMarginUnitMode: Int = SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_DEFAULT,
    val fontSizeSp: Int = SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_DEFAULT,
    val bottomMarginDp: Int = SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_DEFAULT,
    val fontSizePercent: Float = SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_DEFAULT,
    val bottomMarginPercent: Float = SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_DEFAULT,
) {
    companion object {
        /** 全部取默认值（字号与距底都用固定值单位：16sp / 12dp） */
        val Default = SubtitleStyleSpec()
    }

    /**
     * 按实际画面宽高解析出字号与距底部距离，两者各自按自己的单位换算、互不影响。
     *
     * - 字号（只看 [fontSizeUnitMode]）：
     *   固定值 → [fontSizeSp] sp，clamp 到
     *   [SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MIN]..[SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MAX]；
     *   百分比 → 画面**短边** × [fontSizePercent]（比例先 clamp 到
     *   [SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MIN]..
     *   [SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MAX]），算出 dp 长度后经 [Density]
     *   的 `Dp.toSp()` 换算成 sp。注意 `toSp()` 内部会除以 `fontScale`，所以百分比模式的字号
     *   只随画面尺寸缩放，**不受系统字体缩放影响**（渲染出的物理尺寸始终等于用户设置的
     *   短边百分比）；而固定值模式直接用 sp，会随系统的字号设置一起放大，两者表现不同。
     * - 距底（只看 [bottomMarginUnitMode]）：
     *   固定值 → [bottomMarginDp] dp，clamp 到
     *   [SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MIN]..[SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MAX]；
     *   百分比 → 画面高度 × [bottomMarginPercent]（比例 clamp 到
     *   [SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MIN]..
     *   [SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MAX]）。
     *
     * 字号按短边、距底按高度，是因为二者语义不同：字号是可读性尺寸，跟随短边才能保证竖屏
     * 高窄画面里的字不会撑爆；距底是垂直方向的长度，只有相对高度才有意义。
     *
     * @param frameWidth 实际画面宽度（dp）
     * @param frameHeight 实际画面高度（dp），与 [frameWidth] 一起决定字号基准的短边
     * @param density 当前屏幕密度，用于 dp → sp 换算
     */
    fun resolve(frameWidth: Dp, frameHeight: Dp, density: Density): ResolvedSubtitleStyle {
        val fontSize = if (fontSizeUnitMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT) {
            val fontPercent = fontSizePercent.coerceIn(
                SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MIN,
                SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MAX,
            )
            // 字号按画面短边换算：竖屏时短边是宽度，字号随宽度缩放，不会撑爆画面
            val shortSide = minOf(frameWidth, frameHeight)
            with(density) { (shortSide * fontPercent).toSp() }
        } else {
            fontSizeSp.coerceIn(
                SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MIN,
                SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MAX,
            ).sp
        }
        val bottomMargin = if (bottomMarginUnitMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT) {
            val marginPercent = bottomMarginPercent.coerceIn(
                SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MIN,
                SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MAX,
            )
            frameHeight * marginPercent
        } else {
            bottomMarginDp.coerceIn(
                SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MIN,
                SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MAX,
            ).dp
        }
        return ResolvedSubtitleStyle(
            fontSize = fontSize,
            bottomMargin = bottomMargin,
        )
    }
}

/**
 * 解析后的字幕样式：可直接用于绘制。
 *
 * @param fontSize 字号（已按字号单位换算）
 * @param bottomMargin 控制器隐藏时距画面底部的距离（已按距底单位换算）
 */
data class ResolvedSubtitleStyle(
    val fontSize: TextUnit,
    val bottomMargin: Dp,
)

/**
 * 设置页预览用的参考画面尺寸（dp）。
 *
 * 预览框固定为 16:9，此时画面**短边就是高度**，所以字号（相对短边）与距底（相对高度）可以
 * 共用同一个参考值 360dp。固定值单位下 sp/dp 与画面尺寸无关，因此约定这个参考值把固定值
 * 换算成比例，让预览框可以用与播放器完全相同的「比例缩放」方式渲染（见 [fontSizeFraction] /
 * [bottomMarginFraction]）。
 */
val SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT = 360.dp

/**
 * 字号换算成「占画面**短边**的比例」，供设置页预览按比例渲染。
 *
 * 与 [bottomMarginFraction] 的基准不同：字号相对短边、距底相对高度（16:9 预览框里两者数值恰好
 * 相同，所以预览折算共用 [SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT]）。
 *
 * 只看字号单位 [SubtitleStyleSpec.fontSizeUnitMode]：百分比单位直接返回比例；固定值单位按
 * [SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT] 折算（sp 与 dp 在预览比例换算里按 1:1 近似，
 * 等同旧版 16sp 约等于 4.4%）。
 */
fun SubtitleStyleSpec.fontSizeFraction(): Float =
    if (fontSizeUnitMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT) {
        fontSizePercent.coerceIn(
            SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MIN,
            SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MAX,
        )
    } else {
        fontSizeSp.coerceIn(
            SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MIN,
            SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MAX,
        ) / SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT.value
    }

/**
 * 距底部距离换算成「占画面高度的比例」，供设置页预览按比例渲染。
 *
 * 与 [fontSizeFraction] 的基准不同：距底是垂直方向的长度，因此**仍以画面高度为基准**，
 * 不改成短边（16:9 预览框里短边与高度相同，所以折算用的参考值一致）。
 *
 * 只看距底单位 [SubtitleStyleSpec.bottomMarginUnitMode]：百分比单位直接返回比例；固定值单位按
 * [SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT] 折算（12dp / 360dp ≈ 3.3%）。
 */
fun SubtitleStyleSpec.bottomMarginFraction(): Float =
    if (bottomMarginUnitMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT) {
        bottomMarginPercent.coerceIn(
            SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MIN,
            SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MAX,
        )
    } else {
        bottomMarginDp.coerceIn(
            SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MIN,
            SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MAX,
        ) / SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT.value
    }

/**
 * 字号的人读描述，只看字号单位 [SubtitleStyleSpec.fontSizeUnitMode]：
 * 固定值 `"16sp"`、百分比 `"4.5%"`。
 *
 * @param frameHeight 参考画面高度，保留以统一设置页与播放器的调用签名（描述文本本身
 * 只反映用户设置的原始值，与画面高度无关）
 */
fun SubtitleStyleSpec.fontSizeDescription(
    @Suppress("UNUSED_PARAMETER") frameHeight: Dp = SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT,
): String = if (fontSizeUnitMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT) {
    "${formatSubtitlePercent(fontSizePercent.coerceIn(
        SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MIN,
        SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MAX,
    ))}%"
} else {
    "${fontSizeSp.coerceIn(
        SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MIN,
        SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MAX,
    )}sp"
}

/**
 * 距底部距离的人读描述，只看距底单位 [SubtitleStyleSpec.bottomMarginUnitMode]：
 * 固定值 `"12dp"`、百分比 `"3.5%"`。
 *
 * @param frameHeight 参考画面高度，保留以统一设置页与播放器的调用签名（描述文本本身
 * 只反映用户设置的原始值，与画面高度无关）
 */
fun SubtitleStyleSpec.bottomMarginDescription(
    @Suppress("UNUSED_PARAMETER") frameHeight: Dp = SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT,
): String = if (bottomMarginUnitMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT) {
    "${formatSubtitlePercent(bottomMarginPercent.coerceIn(
        SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MIN,
        SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MAX,
    ))}%"
} else {
    "${bottomMarginDp.coerceIn(
        SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MIN,
        SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MAX,
    )}dp"
}

/**
 * 比例 → 百分数文本，保留一位小数且整数时省略小数位（0.045 → "4.5"，0.05 → "5"，0 → "0"）。
 *
 * 不用 `String.format`（common 源集中不可用），手工按十分位取整拼接。
 */
private fun formatSubtitlePercent(percent: Float): String {
    val tenths = round(percent * 1000f).toInt()
    val integerPart = tenths / 10
    val decimalPart = tenths % 10
    return if (decimalPart == 0) "$integerPart" else "$integerPart.$decimalPart"
}
