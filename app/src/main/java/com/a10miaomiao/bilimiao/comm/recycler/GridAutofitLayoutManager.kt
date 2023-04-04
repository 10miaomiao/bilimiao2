package com.a10miaomiao.bilimiao.comm.recycler

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GridAutofitLayoutManager : GridLayoutManager {

    private var mColumnWidthChanged = true;
    private var oldWidthSpec: Int = 0;
    var columnWidth: Int = 0
        set(value) {
            field = value
            mColumnWidthChanged = true
        }
    var maxLine = -1

    constructor(context: Context, columnWidth: Int, maxLine: Int = -1)
            : super(context, 1) {
        this.columnWidth = columnWidth
        this.maxLine = maxLine
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