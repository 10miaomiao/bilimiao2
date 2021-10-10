package com.a10miaomiao.bilimiao2.comm.recycler

import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import cn.a10miaomiao.miao.binding.MiaoBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

open class MiaoBindingAdapter<T>(
    data: MutableList<T>?,
    private val ui: MiaoBindingItemUi<T>,
) : BaseQuickAdapter<T, MiaoBindingViewHolder>(0, data) {

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): MiaoBindingViewHolder {
        val data = getItemOrNull(0)
        return if (data == null) {
            super.onCreateDefViewHolder(parent, viewType)
        } else {
            val binding = MiaoBinding()
            val view = ui.getView(binding, data)
            return MiaoBindingViewHolder(binding, view)
        }
    }

    override fun convert(holder: MiaoBindingViewHolder, item: T) {
        ui.update(holder.binding, item, holder.layoutPosition - headerLayoutCount)
    }

}