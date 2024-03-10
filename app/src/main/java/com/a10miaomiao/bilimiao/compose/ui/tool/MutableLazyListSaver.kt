package com.a10miaomiao.bilimiao.compose.ui.tool

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshots.SnapshotStateList

class MutableLazyListSaver<T : Any?>(
    private val listState: LazyListState
) : Saver<SnapshotStateList<T>, ArrayList<T>> {
    override fun restore(value: ArrayList<T>): SnapshotStateList<T> {
        return SnapshotStateList<T>().apply { addAll(value) }
    }

    override fun SaverScope.save(value: SnapshotStateList<T>): ArrayList<T> {
        val visible = listState.layoutInfo.visibleItemsInfo
        return if (visible.isEmpty()) {
            arrayListOf()
        } else {
            val first = visible.first().index
            val last = visible.last().index + 1
            ArrayList(value.subList(first, last))
        }
    }
}