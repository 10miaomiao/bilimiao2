package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
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
    // 双栏展开断点：容器宽度大于该值时使用双栏布局
    twoColumnMinWidth: Dp,
    // 单栏布局时上方内容区域的高度
    topContentHeight: Dp,
    // 双栏布局时右栏的宽度上限，为 null 时右栏与左栏平分宽度
    rightColumnMaxWidth: Dp? = null,
    chainScrollableLayoutState: ChainScrollableLayoutState,
    leftContent: @Composable BoxScope.(Orientation, PaddingValues) -> Unit,
    content: @Composable BoxScope.(Orientation, PaddingValues) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val layoutDirection = LocalLayoutDirection.current
        if (maxWidth > twoColumnMinWidth) {
            val containerWidth = maxWidth
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
                    modifier = if (rightColumnMaxWidth != null) {
                        // 右栏宽度不超过上限，剩余宽度由左栏填充
                        Modifier.width(minOf(containerWidth / 2, rightColumnMaxWidth))
                    } else {
                        Modifier.weight(1f)
                    }
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
            val topContentHeightPx = remember(density) {
                density.run {
                    topContentHeight.roundToPx().toFloat() - chainScrollableLayoutState.minScrollPosition.roundToPx()
                }
            }
            val scrollableState = rememberScrollableState { 0f }
            val scrollOffset = chainScrollableLayoutState.getHeightOffset()
            val alpha = (1f - scrollOffset / topContentHeightPx).coerceIn(0f, 1f)
            val offsetY = (-scrollOffset).roundToInt()
            ChainScrollableLayout(
                modifier = modifier,
                state = chainScrollableLayoutState,
            ) { state ->
                Box(
                    modifier = Modifier
                        .height(topContentHeight)
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
