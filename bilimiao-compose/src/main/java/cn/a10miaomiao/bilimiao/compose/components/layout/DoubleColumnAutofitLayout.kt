package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable.ChainScrollableLayout
import cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable.ChainScrollableLayoutState
import kotlin.math.roundToInt

@Composable
fun DoubleColumnAutofitLayout(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(),
    leftMaxHeight: Dp,
    leftMaxWidth: Dp,
    chainScrollableLayoutState: ChainScrollableLayoutState,
    leftContent: @Composable BoxScope.(Orientation, PaddingValues) -> Unit,
    content: @Composable BoxScope.(Orientation, PaddingValues) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val layoutDirection = LocalLayoutDirection.current
        if (maxWidth > leftMaxWidth) {
            Row() {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    leftContent(
                        Orientation.Horizontal,
                        PaddingValues.Absolute(
                            left = if (layoutDirection == LayoutDirection.Ltr)
                                innerPadding.calculateStartPadding(layoutDirection) else 0.dp,
                            right = if (layoutDirection == LayoutDirection.Rtl)
                                innerPadding.calculateEndPadding(layoutDirection) else 0.dp,
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding(),
                        )
                    )
                }
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    content(
                        Orientation.Horizontal,
                        PaddingValues.Absolute(
                            left = if (layoutDirection == LayoutDirection.Rtl)
                                innerPadding.calculateStartPadding(layoutDirection) else 0.dp,
                            right = if (layoutDirection == LayoutDirection.Ltr)
                                innerPadding.calculateEndPadding(layoutDirection) else 0.dp,
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding(),
                        )
                    )
                }
            }
        } else {
            val density = LocalDensity.current
            val leftMaxHeightPx = remember(density) {
                density.run {
                    leftMaxHeight.roundToPx().toFloat() - chainScrollableLayoutState.minScrollPosition.roundToPx()
                }
            }
            val scrollableState = rememberScrollState()
            val scrollOffset = chainScrollableLayoutState.getOffsetYValue()
            val alpha = ((leftMaxHeightPx + scrollOffset) / leftMaxHeightPx).coerceIn(0f, 1f)
            val offsetY = scrollOffset.roundToInt()
            ChainScrollableLayout(
                modifier = modifier,
                state = chainScrollableLayoutState,
            ) { state ->
                Box(
                    modifier = Modifier
                        .height(leftMaxHeight)
                        .offset { IntOffset(0, offsetY) }
                        .alpha(alpha)
                        .scrollable(scrollableState, Orientation.Vertical),
                ) {
                    leftContent(
                        Orientation.Vertical,
                        PaddingValues.Absolute(
                            top = innerPadding.calculateTopPadding(),
                            left = if (layoutDirection == LayoutDirection.Ltr)
                                innerPadding.calculateStartPadding(layoutDirection) else 0.dp,
                            right = if (layoutDirection == LayoutDirection.Rtl)
                                innerPadding.calculateEndPadding(layoutDirection) else 0.dp,
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                0,
                                state.maxPx.roundToInt() + offsetY
                            )
                        }
                ) {
                    content(
                        Orientation.Vertical,
                        PaddingValues.Absolute(
                            bottom = innerPadding.calculateBottomPadding() + chainScrollableLayoutState.minScrollPosition,
                            left = if (layoutDirection == LayoutDirection.Ltr)
                                innerPadding.calculateStartPadding(layoutDirection) else 0.dp,
                            right = if (layoutDirection == LayoutDirection.Rtl)
                                innerPadding.calculateEndPadding(layoutDirection) else 0.dp,
                        )
                    )
                }
            }
        }
    }
}
