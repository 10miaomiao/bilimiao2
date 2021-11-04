package com.a10miaomiao.bilimiao.comm.recycler

import android.content.Context
import android.util.TypedValue
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao

class GridAutofitLayoutManager : GridLayoutManager {

    private var mColumnWidthChanged = true;
    private var oldWidthSpec: Int = 0;
    var columnWidth: Int = 0
        set(value) {
            field = value
            mColumnWidthChanged = true
        }

    constructor(context: Context, columnWidth: Int)
            : super(context, 1) {
        this.columnWidth = columnWidth
    }

    constructor(context: Context, columnWidth: Int, orientation: Int, reverseLayout: Boolean)
            : super(context, 1, orientation, reverseLayout) {
        this.columnWidth = columnWidth
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        if (mColumnWidthChanged && columnWidth > 0) {
            val totalSpace = if (orientation == VERTICAL) {
                width - paddingRight - paddingLeft;
            } else {
                height - paddingTop - paddingBottom;
            }
            val spanCount = Math.max(1, totalSpace / columnWidth);
            setSpanCount(spanCount);
            mColumnWidthChanged = false;
        }
        super.onLayoutChildren(recycler, state)
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        super.onMeasure(recycler, state, widthSpec, heightSpec)
        if (oldWidthSpec != widthSpec) {
            mColumnWidthChanged = true
            oldWidthSpec = widthSpec
        }
    }

}