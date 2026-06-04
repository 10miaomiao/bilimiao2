package cn.a10miaomiao.bilimiao.compose.animation


import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Returns the provided [Dp] as an [Int] value by the [LocalDensity].
 *
 * @param slideDistance Value to the slide distance dimension, 30dp by default.
 */
@Composable
fun rememberSlideDistance(
    slideDistance: Dp = DefaultSlideDistance,
): Int {
    val density = LocalDensity.current
    return remember(density, slideDistance) {
        with(density) { slideDistance.roundToPx() }
    }
}

private const val ProgressThreshold = 0.35f

private val Int.ForOutgoing: Int
    get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
    get() = this - this.ForOutgoing

/**
 * [materialSharedAxisX] allows to switch a layout with shared X-axis transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param slideDistance the slide distance of transition.
 * @param durationMillis the duration of transition.
 */
fun materialSharedAxisX(
    forward: Boolean,
    slideDistance: Int,
    durationMillis: Int = DefaultMotionDuration,
): ContentTransform = materialSharedAxisXIn(
    forward = forward,
    slideDistance = slideDistance,
    durationMillis = durationMillis,
) togetherWith materialSharedAxisXOut(
    forward = forward,
    slideDistance = slideDistance,
    durationMillis = durationMillis,
)

/**
 * [materialSharedAxisXIn] allows to switch a layout with shared X-axis enter transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param slideDistance the slide distance of the enter transition.
 * @param durationMillis the duration of the enter transition.
 */
fun materialSharedAxisXIn(
    forward: Boolean,
    slideDistance: Int,
    durationMillis: Int = DefaultMotionDuration,
): EnterTransition = slideInHorizontally(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing,
    ),
    initialOffsetX = {
        if (forward) slideDistance else -slideDistance
    },
) + fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis.ForIncoming,
        delayMillis = durationMillis.ForOutgoing,
        easing = LinearOutSlowInEasing,
    ),
)

/**
 * [materialSharedAxisXOut] allows to switch a layout with shared X-axis exit transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param slideDistance the slide distance of the exit transition.
 * @param durationMillis the duration of the exit transition.
 */
fun materialSharedAxisXOut(
    forward: Boolean,
    slideDistance: Int,
    durationMillis: Int = DefaultMotionDuration,
): ExitTransition = slideOutHorizontally(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing,
    ),
    targetOffsetX = {
        if (forward) -slideDistance else slideDistance
    },
) + fadeOut(
    animationSpec = tween(
        durationMillis = durationMillis.ForOutgoing,
        delayMillis = 0,
        easing = FastOutLinearInEasing,
    ),
)

/**
 * [materialSharedAxisY] allows to switch a layout with shared Y-axis transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param slideDistance the slide distance of transition.
 * @param durationMillis the duration of transition.
 */
fun materialSharedAxisY(
    forward: Boolean,
    slideDistance: Int,
    durationMillis: Int = DefaultMotionDuration,
): ContentTransform = materialSharedAxisYIn(
    forward = forward,
    slideDistance = slideDistance,
    durationMillis = durationMillis,
) togetherWith materialSharedAxisYOut(
    forward = forward,
    slideDistance = slideDistance,
    durationMillis = durationMillis,
)

/**
 * [materialSharedAxisYIn] allows to switch a layout with shared Y-axis enter transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param slideDistance the slide distance of the enter transition.
 * @param durationMillis the duration of the enter transition.
 */
fun materialSharedAxisYIn(
    forward: Boolean,
    slideDistance: Int,
    durationMillis: Int = DefaultMotionDuration,
): EnterTransition = slideInVertically(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing,
    ),
    initialOffsetY = {
        if (forward) slideDistance else -slideDistance
    },
) + fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis.ForIncoming,
        delayMillis = durationMillis.ForOutgoing,
        easing = LinearOutSlowInEasing,
    ),
)

/**
 * [materialSharedAxisYOut] allows to switch a layout with shared Y-axis exit transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param slideDistance the slide distance of the exit transition.
 * @param durationMillis the duration of the exit transition.
 */
fun materialSharedAxisYOut(
    forward: Boolean,
    slideDistance: Int,
    durationMillis: Int = DefaultMotionDuration,
): ExitTransition = slideOutVertically(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing,
    ),
    targetOffsetY = {
        if (forward) -slideDistance else slideDistance
    },
) + fadeOut(
    animationSpec = tween(
        durationMillis = durationMillis.ForOutgoing,
        delayMillis = 0,
        easing = FastOutLinearInEasing,
    ),
)

/**
 * [materialSharedAxisZ] allows to switch a layout with shared Z-axis transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param durationMillis the duration of transition.
 */
fun materialSharedAxisZ(
    forward: Boolean,
    durationMillis: Int = DefaultMotionDuration,
): ContentTransform = materialSharedAxisZIn(
    forward = forward,
    durationMillis = durationMillis,
) togetherWith materialSharedAxisZOut(
    forward = forward,
    durationMillis = durationMillis,
)

/**
 * [materialSharedAxisZIn] allows to switch a layout with shared Z-axis enter transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param durationMillis the duration of the enter transition.
 */
fun materialSharedAxisZIn(
    forward: Boolean,
    durationMillis: Int = DefaultMotionDuration,
): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis.ForIncoming,
        delayMillis = durationMillis.ForOutgoing,
        easing = LinearOutSlowInEasing,
    ),
) + scaleIn(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing,
    ),
    initialScale = if (forward) 0.8f else 1.1f,
)

/**
 * [materialSharedAxisZOut] allows to switch a layout with shared Z-axis exit transition.
 *
 * @param forward whether the direction of the animation is forward.
 * @param durationMillis the duration of the exit transition.
 */
fun materialSharedAxisZOut(
    forward: Boolean,
    durationMillis: Int = DefaultMotionDuration,
): ExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = durationMillis.ForOutgoing,
        delayMillis = 0,
        easing = FastOutLinearInEasing,
    ),
) + scaleOut(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = FastOutSlowInEasing,
    ),
    targetScale = if (forward) 1.1f else 0.8f,
)