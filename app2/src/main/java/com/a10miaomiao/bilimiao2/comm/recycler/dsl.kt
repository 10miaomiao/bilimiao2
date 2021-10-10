package com.a10miaomiao.bilimiao2.comm.recycler

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao2.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao2.comm.MiaoUI

fun <T> RecyclerView._miaoAdapter(
    items: MutableList<T>? = null,
    adapterRef: ((adapter: MiaoBindingAdapter<T>) -> Unit)? = null,
    itemUi: MiaoBindingItemUi<T>,
) {
    val mAdapter = miaoMemo(itemUi) {
        object : MiaoBindingAdapter<T>(
            items,
            it,
        ) {}
    }
    miaoEffect(null, {
        adapterRef?.invoke(mAdapter)
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