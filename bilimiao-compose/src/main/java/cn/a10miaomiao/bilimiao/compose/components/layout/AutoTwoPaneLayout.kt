package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

class AutoTwoPaneLayoutState(
    val visible: Boolean,
    val showTowPane: Boolean,
)

private val defaultFirstEnter = fadeIn(
    animationSpec = tween(durationMillis = 200)
) + slideInHorizontally(
    initialOffsetX = { -it / 4 },
    animationSpec = tween(durationMillis = 200)
)
private val defaultFirstExit = fadeOut(
    animationSpec = tween(durationMillis = 200)
) + slideOutHorizontally(
    targetOffsetX = { -it / 4 },
    animationSpec = tween(durationMillis = 200)
)
private val defaultSecondEnter = fadeIn(
    animationSpec = tween(durationMillis = 200)
) + slideInHorizontally(
    initialOffsetX = { it / 4 },
    animationSpec = tween(durationMillis = 200)
)
private val defaultSecondExit = fadeOut(
    animationSpec = tween(durationMillis = 200)
) + slideOutHorizontally(
    targetOffsetX = { it / 4 },
    animationSpec = tween(durationMillis = 200)
)

@Composable
fun AutoTwoPaneLayout(
    modifier: Modifier = Modifier,
    first: @Composable (state: AutoTwoPaneLayoutState) -> Unit,
    second: @Composable (state: AutoTwoPaneLayoutState) -> Unit,
    firstEnter: EnterTransition = defaultFirstEnter,
    firstExit: ExitTransition = defaultFirstExit,
    secondEnter: EnterTransition = defaultSecondEnter,
    secondExit: ExitTransition = defaultSecondExit,
    twoPaneMinWidth: Dp,
    firstPaneMaxWidth: Dp = 0.dp,
    secondPaneMaxWidth: Dp = 0.dp,
    hideFirstPane: Boolean = false,
    openedSecond: Boolean = false,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val density = LocalDensity.current
        val splitPane = maxWidth > twoPaneMinWidth
        val showFirst = !hideFirstPane && (splitPane || !openedSecond)
        val showSecond = splitPane || openedSecond
        Layout(
            modifier = Modifier.wrapContentSize(),
            content = {
                Box(Modifier.layoutId(0)) {
                    AnimatedVisibility(
                        visible = showFirst,
                        enter = firstEnter,
                        exit = firstExit,
                    ) {
                        first(
                            AutoTwoPaneLayoutState(
                                visible = showFirst,
                                showTowPane = splitPane,
                            )
                        )
                    }
                }
                Box(Modifier.layoutId(1)) {
                    AnimatedVisibility(
                        visible = showSecond,
                        enter = secondEnter,
                        exit = secondExit,
                    ) {
                        second(
                            AutoTwoPaneLayoutState(
                                visible = showSecond,
                                showTowPane = splitPane,
                            )
                        )
                    }
                }
            }
        ) { measurable, constraints ->
            val firstMeasurable = measurable.find { it.layoutId == 0 }!!
            val secondMeasurable = measurable.find { it.layoutId == 1 }!!
            layout(constraints.maxWidth, constraints.maxHeight) {
                if (splitPane) {
                    val splitWidth = if (firstPaneMaxWidth > 0.dp) {
                        val firstPaneMaxWidthPx = with(density) {
                            firstPaneMaxWidth.toPx()
                        }.toInt()
                        min(constraints.maxWidth / 2, firstPaneMaxWidthPx)
                    } else if (secondPaneMaxWidth > 0.dp) {
                        val secondPaneMaxWidthPx = with(density) {
                            secondPaneMaxWidth.toPx()
                        }.toInt()
                        max(constraints.maxWidth / 2, constraints.maxWidth - secondPaneMaxWidthPx)
                    } else {
                        constraints.maxWidth / 2
                    }
                    val firstConstraints =
                        constraints.copy(minWidth = splitWidth, maxWidth = splitWidth)
                    val secondConstraints = if (hideFirstPane) {
                        constraints.copy(
                            minWidth = constraints.maxWidth,
                            maxWidth = constraints.maxWidth
                        )
                    } else {
                        constraints.copy(
                            minWidth = constraints.maxWidth - splitWidth,
                            maxWidth = constraints.maxWidth - splitWidth
                        )
                    }
                    val firstPlaceable =
                        firstMeasurable.measure(constraints.constrain(firstConstraints))
                    val secondPlaceable =
                        secondMeasurable.measure(constraints.constrain(secondConstraints))
                    firstPlaceable.placeRelative(0, 0)
                    val detailOffsetX = constraints.maxWidth - secondPlaceable.width
                    secondPlaceable.placeRelative(detailOffsetX, 0)
                } else {
                    val firstConstraints = constraints.copy()
                    val secondConstraints = constraints.copy()
                    val firstPlaceable =
                        firstMeasurable.measure(constraints.constrain(firstConstraints))
                    val secondPlaceable =
                        secondMeasurable.measure(constraints.constrain(secondConstraints))
                    firstPlaceable.placeRelative(0, 0)
                    secondPlaceable.placeRelative(0, 0)
                }
            }
        }
    }
}