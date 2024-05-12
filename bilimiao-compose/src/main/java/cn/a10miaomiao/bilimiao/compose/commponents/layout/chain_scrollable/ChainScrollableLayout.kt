package cn.a10miaomiao.bilimiao.compose.commponents.layout.chain_scrollable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity


internal class ChainScrollableLayoutNestedScrollConnection(
    val state: ChainScrollableLayoutState,
) : NestedScrollConnection {
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (
            available.y < 0
            && state.getOffsetYValue() > -state.maxPx
        ) {
            state.setOffsetY(maxOf(state.getOffsetYValue() + available.y, -state.maxPx))
            return available
        }

        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
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
        return super.onPostScroll(consumed, available, source)
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
//        state.callOnScrollStop()
        return super.onPostFling(consumed, available)
    }
}

@Composable
fun ChainScrollableLayout(
    modifier: Modifier = Modifier,
    state: ChainScrollableLayoutState,
    content: @Composable BoxScope.(state: ChainScrollableLayoutState) -> Unit,
) {
    val nestedScrollState = remember(state) {
        ChainScrollableLayoutNestedScrollConnection(state)
    }
    Box(
        modifier
            .nestedScroll(nestedScrollState),
    ) {
//        Box(
//            modifier = Modifier.offset {
//                IntOffset(
//                    0,
//                    state.getOffsetYValue().roundToInt()
//                )
//            }
//        ) {
//            chainContent(state)
//        }
//        Box(
//            modifier = Modifier.offset {
//                IntOffset(
//                    0,
//                    (maxPx + state.getOffsetYValue()).roundToInt()
//                )
//            }
//        ) {
//            content(state)
//        }
        content(state)
    }
}