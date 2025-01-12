/*
 * Copyright 2024 Gergely Kőrössy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.a10miaomiao.bilimiao.compose.components.layout.sticky

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Provides basic info about the layout that will be used by [StickyHeadersLayout] when placing the
 * sticky items.
 */
interface StickyLayoutInfoProvider {
    /**
     * The spacing between items in the direction of scrolling.
     */
    val mainAxisItemSpacing: Int

    /**
     * The content padding in pixels applied before the first row/ column in the direction of
     * scrolling.
     */
    val beforeContentPadding: Int

    /**
     * The offset of the visible item that the sticky item is placed against in the direction of
     * scrolling. For vertical layouts it is from the top and for horizontal layouts it is from the
     * start / end (depending on the [LayoutDirection]).
     *
     * @param index the index in the lazy layout's _**visible**_ items list
     * @param orientation the orientation of the lazy layout
     * @return the offset of the visible item. When [orientation] is
     * [Vertical][Orientation.Vertical], it is calculated from the top;
     * otherwise it is from the start / end (depending on the [LayoutDirection]).
     */
    fun itemOffsetAt(index: Int, orientation: Orientation): Int?
}

/**
 * A low-level layout for creating and tracking sticky items.
 * Generally it's better to use any of the [StickyHeaders] implementations.
 *
 * @param keys the list of pre-calculated key intervals. See [StickyHeaders] for a sample
 * implementation.
 * @param orientation orientation of the layout
 * @param reverseLayout reverse the direction of scrolling and layout. When `true`, items are
 * laid out in the reverse order.
 * @param layoutInfoProvider basic info provider about the visible items and their container
 * @param modifier [Modifier] applied to the container of the sticky items
 * @param stickyEdgePadding the padding applied to the sticky items on the edge where they stick to
 * @param content sticky item content
 */
@Composable
fun <T : Any> StickyHeadersLayout(
    keys: List<StickyInterval<T>>,
    orientation: Orientation,
    reverseLayout: Boolean,
    layoutInfoProvider: StickyLayoutInfoProvider,
    modifier: Modifier = Modifier,
    stickyEdgePadding: Dp = 0.dp,
    content: @Composable (stickyInterval: StickyInterval<T>) -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Box(
        modifier = modifier.clipToBounds(),
    ) {
        keys.forEach { interval ->
            key(interval.key) { // TODO ReusableContentHost { }, see LazyLayoutItemContentFactory
                Box(
                    modifier = Modifier
                        .run {
                            if (orientation == Orientation.Horizontal && reverseLayout) {
                                align(Alignment.CenterEnd)
                            } else if (orientation == Orientation.Vertical && reverseLayout) {
                                align(Alignment.BottomCenter)
                            } else {
                                this
                            }
                        }
                        .graphicsLayer {
                            val spacing = layoutInfoProvider.mainAxisItemSpacing

                            val nextOffset =
                                layoutInfoProvider.itemOffsetAt(interval.endIndex, orientation)
                                    ?: Int.MAX_VALUE
                            val itemOffset =
                                layoutInfoProvider.itemOffsetAt(interval.startIndex, orientation)

                            val beforePadding = layoutInfoProvider.beforeContentPadding

                            val switchDirection =
                                reverseLayout.xor(isRtl && orientation == Orientation.Horizontal)

                            val direction = if (switchDirection) -1f else 1f

                            val stickyPadding = stickyEdgePadding.roundToPx()

                            if (itemOffset == null) {
                                // don't show the item if it's not visible anymore
                                alpha = 0f
                            } else {
                                val diff = nextOffset - spacing + beforePadding - stickyPadding

                                if (orientation == Orientation.Vertical) {
                                    val y = (diff - size.height).coerceAtMost(0f)
                                    val offset =
                                        (itemOffset + beforePadding).coerceAtLeast(0)

                                    val inset = (stickyPadding - offset).coerceAtLeast(0)

                                    translationY = (offset + y + inset) * direction
                                } else {
                                    val x = (diff - size.width).coerceAtMost(0f)
                                    val offset =
                                        (itemOffset + beforePadding).coerceAtLeast(0)

                                    val inset = (stickyPadding - offset).coerceAtLeast(0)

                                    translationX = (offset + x + inset) * direction
                                }
                            }
                        },
                ) {
                    content(interval)
                }
            }
        }
    }
}

/* TODO
    - scroll on the box? overscroll effect?
 */
