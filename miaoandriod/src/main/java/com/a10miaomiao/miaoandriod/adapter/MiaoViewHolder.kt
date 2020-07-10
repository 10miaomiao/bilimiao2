package com.a10miaomiao.miaoandriod.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import com.a10miaomiao.miaoandriod.ValueManager


/**
 * Created by 10喵喵 on 2018/2/24.
 */
class MiaoViewHolder<T>(val parentView: View, val binding: Binding<T>? = null) : RecyclerView.ViewHolder(parentView) {

    var index = 0

    fun updateView(item: T) {
        binding?.updateView(item, index)
    }


    fun <T, R> createValueManager(binding: MiaoViewHolder.Binding<R>, getValue: (R) -> T): ValueManager<T> {
        return { f -> binding.bind { item -> f(getValue(item)) } }
    }

    class Binding<T>(val mAdapter: MiaoRecyclerViewAdapter<T>) {

        var fns = ArrayList<(item: T, index: Int) -> Unit>()

        fun bindIndexed(fn: ((item: T, index: Int) -> Unit)) {
            fns.add(fn)
        }

        fun bind(fn: ((item: T) -> Unit)) {
            bindIndexed { item, index -> fn(item) }
        }

        fun <R> itemValue(getValue: T.() -> R): ValueManager<R> = { f ->
            bind { item -> f(getValue(item)) }
        }

        fun indexValue(): ValueManager<Int> = { f ->
            bindIndexed { item, index -> f(index) }
        }

        fun View.bindClick(fn: ((item: T, index: Int) -> Unit)) {
            bindIndexed { item, index ->
                this.setOnClickListener {
                    fn(item, index)
                    mAdapter.notifyDataSetChanged() //直接更新视图，省去每次都要手动更新
                }
            }
        }

        fun View.bindClick(fn: ((item: T) -> Unit)) {
            bindClick { item, index -> fn(item) }
        }

        /**
         * 更新视图
         */
        fun updateView(item: T, index: Int) {
            fns.forEach { it(item, index) }
        }
    }
}