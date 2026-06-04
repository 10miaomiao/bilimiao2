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
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.jvm.JvmInline

/**
 * The sticky item interval container. This represents a single sticky item.
 */
@Immutable
data class StickyInterval<T : Any>(
    /**
     * Key of the interval that was returned by `key` in [StickyHeadersLayout].
     */
    val key: T,
    /**
     * Start index of the interval (inclusive).
     */
    val startIndex: Int,
    /**
     * End index of the interval (exclusive).
     */
    val endIndex: Int,
)

/**
 * Contains useful information about an individual item in lazy lists like
 * [LazyColumn][androidx.compose.foundation.lazy.LazyColumn] or
 * [LazyRow][androidx.compose.foundation.lazy.LazyRow].
 */
@JvmInline
value class LazyListItem(private val value: LazyListItemInfo) {
    /**
     * The index of the item in the list.
     */
    val index: Int get() = value.index

    /**
     * The key of the item which was passed to the item() or items() function.
     */
    val key: Any get() = value.key

    /**
     * The content type of the item which was passed to the item() or items() function.
     */
    val contentType: Any? get() = value.contentType

    /**
     * The main axis offset of the item in pixels. It is relative to the start of the lazy list container.
     */
    val offset: Int get() = value.offset

    /**
     * The main axis size of the item in pixels. Note that if you emit multiple layouts in the composable
     * slot for the item then this size will be calculated as the sum of their sizes.
     */
    val size: Int get() = value.size
}

/**
 * Creates and tracks sticky items belonging to a
 * [LazyColumn][androidx.compose.foundation.lazy.LazyColumn] or a
 * [LazyRow][androidx.compose.foundation.lazy.LazyRow] with [state].
 *
 * The items are grouped by the value returned by [key]. This grouping only occurs in
 * a consecutive order, meaning that if the function returns the same value for two non-consecutive
 * items, two sticky headers will be created, thus this is generally discouraged.
 * When the [key] returns `null`, it acts as a boundary for the sticky items before /
 * after.
 *
 * @param state the [LazyListState] of the list
 * @param key key factory function for the sticky items
 * @param modifier [Modifier] applied to the container of the sticky items
 * @param stickyEdgePadding the padding applied to the sticky items on the edge where they stick to
 * @param content sticky item content
 */
@Composable
fun <T : Any> StickyHeaders(
    state: LazyListState,
    key: (item: LazyListItem) -> T?,
    // contentType: (item: LazyListItem) -> Any? = { null },
    modifier: Modifier = Modifier,
    stickyEdgePadding: Dp = 0.dp,
    content: @Composable (stickyInterval: StickyInterval<T>) -> Unit,
) {
    val keyFactory = rememberUpdatedState(key)

    val orientation by remember(state) {
        derivedStateOf {
            state.layoutInfo.orientation
        }
    }

    val reverseLayout by remember(state) {
        derivedStateOf {
            state.layoutInfo.reverseLayout
        }
    }

    val keys: List<StickyInterval<T>> by remember(state) {
        derivedStateOf {
            state.layoutInfo.visibleItemsInfo
                .takeIf { it.isNotEmpty() }
                ?.let { items ->
                    var initKeySet = false
                    var lastKey: T? = null
                    var lastIndex = items.first().index

                    buildList {
                        items.forEach { item ->
                            val currentKey = keyFactory.value(LazyListItem(item))

                            if (!initKeySet) {
                                initKeySet = true
                                lastKey = currentKey
                            }

                            if (lastKey != currentKey) {
                                lastKey?.also {
                                    add(
                                        StickyInterval(it, lastIndex, item.index),
                                    )
                                }

                                lastKey = currentKey
                                lastIndex = item.index
                            }
                        }

                        lastKey?.also {
                            add(
                                StickyInterval(it, lastIndex, items.last().index + 1),
                            )
                        }
                    }
                } ?: emptyList()
        }
    }

    StickyHeadersLayout(
        keys = keys,
        orientation = orientation,
        reverseLayout = reverseLayout,
        layoutInfoProvider = remember(state) { LazyListInfoProvider(state) },
        modifier = modifier,
        stickyEdgePadding = stickyEdgePadding,
        content = content,
    )
}

/**
 * Provides basic info about the list that will be used by [StickyHeadersLayout].
 */
class LazyListInfoProvider(private val listState: LazyListState) : StickyLayoutInfoProvider {
    override val mainAxisItemSpacing: Int
        get() = listState.layoutInfo.mainAxisItemSpacing

    override val beforeContentPadding: Int
        get() = listState.layoutInfo.beforeContentPadding

    override fun itemOffsetAt(index: Int, orientation: Orientation): Int? {
        return listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.offset
    }
}
