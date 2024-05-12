package com.a10miaomiao.bilimiao.comm.recycler

import android.view.ViewGroup
import android.widget.FrameLayout
import cn.a10miaomiao.miao.binding.MiaoBinding
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.module.LoadMoreModule

open class MiaoBindingMultiAdapter<T>(
    data: MutableList<T>?,
    private val ui: MiaoBindingItemUi<T>,
) : BaseDelegateMultiAdapter<T, MiaoBindingViewHolder>(data), LoadMoreModule {

    private val typeNameSet = mutableSetOf<String>()

    init {
        loadMoreModule.loadMoreView = CustomLoadMoreView()
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): MiaoBindingViewHolder {
        val binding = MiaoBinding()
        val view = FrameLayout(parent.context)
        return MiaoBindingViewHolder(binding, view)
    }

    override fun convert(holder: MiaoBindingViewHolder, item: T) {
        val position = holder.layoutPosition - headerLayoutCount
        val view = holder.view as? FrameLayout ?: return
        val binding = holder.binding
        if (view.childCount > 0) {
            ui.update(binding, item, position)
        } else {
            view.addView(
                ui.getView(binding, item, position)
            )
        }
    }

    override fun getDefItemViewType(position: Int): Int {
        val data = getItem(position) ?: return -1
        val typeName = data::class.qualifiedName ?: return -1
        val index = typeNameSet.indexOf(typeName)
        if (index == -1) {
            typeNameSet.add(typeName)
            return typeNameSet.size - 1
        }
        return index
    }

}