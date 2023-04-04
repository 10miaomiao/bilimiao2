package com.a10miaomiao.bilimiao.comm.recycler

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller

class TopSnappedSmoothScroller(
    context: Context
) : LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
    }
}