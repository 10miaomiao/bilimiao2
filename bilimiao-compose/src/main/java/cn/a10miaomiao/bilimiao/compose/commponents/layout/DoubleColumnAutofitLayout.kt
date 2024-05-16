package cn.a10miaomiao.bilimiao.compose.commponents.layout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable.ChainScrollableLayout
import cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable.ChainScrollableLayoutState
import kotlin.math.roundToInt



@Composable
fun DoubleColumnAutofitLayout(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(),
    leftMaxHeight: Dp,
    leftMaxWidth: Dp,
    chainScrollableLayoutState: ChainScrollableLayoutState,
    leftContent: @Composable BoxScope.(orientation: Orientation) -> Unit,
    content: @Composable BoxScope.(orientation: Orientation) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        if (maxWidth > leftMaxWidth) {
            Row() {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            end = 5.dp,
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding(),
                        ),
                ) {
                    leftContent(Orientation.Horizontal)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            start = 0.dp,
                            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        )
                ) {
                    content(Orientation.Horizontal)
                }
            }
        } else {
            val density = LocalDensity.current
            val leftMaxHeightPx = remember(density) {
                density.run { leftMaxHeight.roundToPx().toFloat() }
            }
            val scrollableState = rememberScrollState()
            ChainScrollableLayout(
                modifier = modifier,
                state = chainScrollableLayoutState,
            ) { state ->
                val alpha = (leftMaxHeightPx + state.getOffsetYValue()) / leftMaxHeightPx
                Box(
                    modifier = Modifier
                        .height(leftMaxHeight)
                        .padding(
                            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                            top = innerPadding.calculateTopPadding(),
                            bottom = 0.dp,
                        )
                        .offset {
                            IntOffset(
                                0,
//                                    (state.getOffsetYValue() / 2).roundToInt()
                                state
                                    .getOffsetYValue()
                                    .roundToInt()
                            )
                        }
                        .alpha(alpha)
                        .scrollable(scrollableState, Orientation.Vertical),
                ) {
                    leftContent(Orientation.Vertical)
                }
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                0,
                                (state.maxPx + state.getOffsetYValue()).roundToInt()
                            )
                        }
                        .padding(
                            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                        )
                ) {
                    content(Orientation.Vertical)
                }
            }
        }
    }
}