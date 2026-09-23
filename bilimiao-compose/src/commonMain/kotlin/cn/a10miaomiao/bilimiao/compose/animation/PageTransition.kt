package cn.a10miaomiao.bilimiao.compose.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith

/**
 * 页面切换动画
 *
 * - 前进：新页面由 0.85 放大到全屏，旧页面向外扩散到 1.15 并淡出
 * - 返回：上一页由 1.1 缩回全屏，当前页向内缩小到 0.9 并淡出
 */
private const val PageScaleDurationMillis = 300

private const val PageAlphaDurationMillis = 150
private const val PageOpenAlphaDelayMillis = 100
private const val PageCloseAlphaDelayMillis = 50

private val DecelerateQuadEasing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)

/** 打开新页面（A→B）：B 由里扩到整个屏幕，A 向外扩散消失 */
fun pageOpenTransition(): ContentTransform = pageTransition(
    enterScale = 0.8f,
    exitScale = 1.1f,
    alphaDelayMillis = PageOpenAlphaDelayMillis,
)

/** 返回上一页（B→A）：A 由外缩回整个屏幕，B 向内缩小消失 */
fun pageCloseTransition(): ContentTransform = pageTransition(
    enterScale = 1.1f,
    exitScale = 0.8f,
    alphaDelayMillis = PageCloseAlphaDelayMillis,
)

private fun pageTransition(
    enterScale: Float,
    exitScale: Float,
    alphaDelayMillis: Int,
): ContentTransform {
    val scaleSpec = tween<Float>(
        durationMillis = PageScaleDurationMillis,
        easing = DecelerateQuadEasing,
    )
    val alphaSpec = tween<Float>(
        durationMillis = PageAlphaDurationMillis,
        delayMillis = alphaDelayMillis,
        easing = LinearEasing,
    )
    val enter = scaleIn(
        animationSpec = scaleSpec,
        initialScale = enterScale,
    ) + fadeIn(animationSpec = alphaSpec)
    val exit = scaleOut(
        animationSpec = scaleSpec,
        targetScale = exitScale,
    ) + fadeOut(animationSpec = alphaSpec)
    return enter togetherWith exit
}
