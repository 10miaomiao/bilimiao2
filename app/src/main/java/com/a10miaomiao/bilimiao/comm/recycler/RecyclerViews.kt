package com.a10miaomiao.bilimiao.comm.recycler

import androidx.recyclerview.widget.RecyclerView
import com.a10miaomiao.bilimiao.comm.MiaoUI

class RecyclerViews(
    private val recyclerView: RecyclerView,
    private val adapter: MiaoBindingAdapter<*>,
    private val type: Int,
    private val isRecord: Boolean,
): MiaoUI.ViewsInfo(recyclerView, isRecord) {
    override fun bindViews() {
        if (type == 0) {
            views.forEach {
                adapter.addHeaderView(it)
            }
        } else if (type == 1) {
            views.forEach {
                adapter.addFooterView(it)
            }
        }
    }
}

