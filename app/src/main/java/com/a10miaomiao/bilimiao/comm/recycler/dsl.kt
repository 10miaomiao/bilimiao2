package com.a10miaomiao.bilimiao.comm.recycler

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.chad.library.adapter.base.listener.OnItemClickListener

fun <T> RecyclerView._miaoAdapter(
    items: MutableList<T>? = null,
    itemUi: MiaoBindingItemUi<T>,
    adapterRef: ((adapter: MiaoBindingAdapter<T>) -> Unit)? = null,
    onItemClick: OnItemClickListener? = null
) {
    val mAdapter = miaoMemo(itemUi) {
        object : MiaoBindingAdapter<T>(
            items,
            it,
        ) {}
    }
    miaoEffect(null, {
        adapterRef?.invoke(mAdapter)
        onItemClick?.let { mAdapter.setOnItemClickListener(it) }
        this@_miaoAdapter.adapter = mAdapter
    }) {
        mAdapter.setList(items)
    }
}

fun <T> Context.miaoBindingItemUi (block: MiaoBindingItemUi<T>.(item: T, index: Int) -> View): MiaoBindingItemUi<T> {
    return object : MiaoBindingItemUi<T>() {
        override val ctx: Context get() = this@miaoBindingItemUi
        override fun createView(item: T, index: Int) = block(item, index)
    }
}

fun <T> Fragment.miaoBindingItemUi(block: MiaoBindingItemUi<T>.(item: T, index: Int) -> View): MiaoBindingItemUi<T> {
    return object : MiaoBindingItemUi<T>() {
        override val ctx: Context get() = requireContext()
        override fun createView(item: T, index: Int) = block(item, index)
    }
}