package com.a10miaomiao.bilimiao.comm.recycler

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

open class RecyclerViewFragment: Fragment() {

    var mLayoutManager: LinearLayoutManager? = null

    fun toListTop() {
        mLayoutManager?.let { layoutManage ->
            if (layoutManage.isSmoothScrolling) {
                return@let
            }
            if (layoutManage.findFirstVisibleItemPosition() == 0) {
                refreshList()
            } else {
                val smoothScroller = TopSnappedSmoothScroller(requireContext())
                smoothScroller.targetPosition = 0
                layoutManage.startSmoothScroll(smoothScroller)
            }
        }
    }

    open fun refreshList() {}

}