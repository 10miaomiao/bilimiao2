package cn.a10miaomiao.bilimiao.compose.commponents.layout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable.ChainScrollableLayout
import cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable.ChainScrollableLayoutNestedScrollConnection
import cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable.ChainScrollableLayoutState
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlin.math.roundToInt

internal class DoubleColumnAutofitLayoutTopNestedScrollConnection(
    val state: ChainScrollableLayoutState,
) : NestedScrollConnection {
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (
            available.y > 0
            && state.getOffsetYValue() < 0
        ) {
            state.setOffsetY(minOf(state.getOffsetYValue() + available.y, 0f))
            return available
        }
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return super.onPostScroll(consumed, available, source)
    }
}

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
                density.run { leftMaxHeight.roundToPx().toFloat() }
            }
            val scrollableState = rememberScrollState()
            val nestedScrollState = remember(chainScrollableLayoutState) {
                DoubleColumnAutofitLayoutTopNestedScrollConnection(chainScrollableLayoutState)
            }
            ChainScrollableLayout(
                modifier = modifier,
                state = chainScrollableLayoutState,
            ) { state ->
                val alpha = (leftMaxHeightPx + state.getOffsetYValue()) / leftMaxHeightPx
                Box(
                    modifier = Modifier
                        .height(leftMaxHeight)
                        .offset {
                            IntOffset(
                                0,
                                state
                                    .getOffsetYValue()
                                    .roundToInt()
                            )
                        }
                        .alpha(alpha)
                        .nestedScroll(nestedScrollState)
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
                                (state.maxPx + state.getOffsetYValue()).roundToInt()
                            )
                        }
                ) {
                    content(
                        Orientation.Vertical,
                        PaddingValues.Absolute(
                            bottom = innerPadding.calculateBottomPadding(),
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