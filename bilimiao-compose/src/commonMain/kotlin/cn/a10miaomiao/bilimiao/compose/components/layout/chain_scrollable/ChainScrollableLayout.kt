package cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll


@Composable
fun ChainScrollableLayout(
    modifier: Modifier = Modifier,
    state: ChainScrollableLayoutState,
    content: @Composable BoxScope.(state: ChainScrollableLayoutState) -> Unit,
) {
    Box(
        modifier.nestedScroll(state.nestedScroll),
    ) {
        content(state)
    }
}
