package com.a10miaomiao.miaoandriod.adapter

import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * Created by 10喵喵 on 2018/2/24.
 */
class MiaoViewHolder<T>(val parentView: View, val binding: Binding<T>? = null) : RecyclerView.ViewHolder(parentView) {

    var index = 0

    fun updateView(item: T) {
        binding?.updateView(item, index)
    }

    class Binding<T>(val mAdapter: MiaoRecyclerViewAdapter<T>) {

        var fns = ArrayList<(item: T, index: Int) -> Unit>()

        fun bind(fn: ((item: T, index: Int) -> Unit)) {
            fns.add(fn)
        }

        fun bind(fn: ((item: T) -> Unit)) {
            bind({ item, index -> fn(item) })
        }

        fun bindClick(view: View, fn: ((item: T, index: Int) -> Unit)) {
            bind { item, index ->
                view.setOnClickListener {
                    fn(item, index)
                    mAdapter.notifyDataSetChanged() //直接更新视图，省去每次都要手动更新
                }
            }
        }

        fun bindClick(view: View, fn: ((item: T) -> Unit)) {
            bindClick(view, { item, index -> fn(item) })
        }

        /**
         * 更新视图
         */
        fun updateView(item: T, index: Int) {
            fns.forEach { it(item, index) }
        }
    }
}