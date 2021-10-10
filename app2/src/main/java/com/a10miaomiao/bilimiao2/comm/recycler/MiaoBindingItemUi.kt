package com.a10miaomiao.bilimiao2.comm.recycler

import android.view.View
import cn.a10miaomiao.miao.binding.MiaoBinding
import cn.a10miaomiao.miao.binding.MiaoTarget
import com.a10miaomiao.bilimiao2.comm.MiaoUI
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import splitties.views.dsl.core.Ui

abstract class MiaoBindingItemUi<T> : MiaoUI() {

    override val root: View
        get() = throw NullPointerException()

    fun getView (binding: MiaoBinding, item: T): View {
        return binding.start {
            miao { createView(item, 0) }
        }
    }

    abstract fun createView (item: T, index: Int): View

    fun update(binding: MiaoBinding, item: T, index: Int) {
        binding.start {
            createView(item, 0)
        }
    }

}