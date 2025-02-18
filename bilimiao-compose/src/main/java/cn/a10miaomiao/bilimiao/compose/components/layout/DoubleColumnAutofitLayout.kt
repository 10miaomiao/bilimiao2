package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
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
        if (maxWidth > leftMaxWidth) {
            Row() {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    leftContent(
                        Orientation.Horizontal,
                        PaddingValues.Absolute(
                            left = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
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
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding(),
                            right = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
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
            ChainScrollableLayout(
                modifier = modifier,
                state = chainScrollableLayoutState,
            ) { state ->
                val alpha = (leftMaxHeightPx + state.getOffsetYValue()) / leftMaxHeightPx
                val offsetY by animateIntAsState(
                    targetValue = state.getOffsetYValue().roundToInt(), label = "",
                )
                Box(
                    modifier = Modifier
                        .animateContentSize()
                        .height(leftMaxHeight)
                        .offset {
                            IntOffset(0, offsetY)
                        }
                        .alpha(alpha)
                        .nestedScroll(state.nestedScroll)
                        .scrollable(scrollableState, Orientation.Vertical),
                ) {
                    leftContent(
                        Orientation.Vertical,
                        PaddingValues.Absolute(
                            top = innerPadding.calculateTopPadding(),
                            left = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            right = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
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
                            left = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            right = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
//                            top = density.run { (state.maxPx + state.getOffsetYValue()).toDp() }
                        )
                    )
                }
            }
        }
    }
}