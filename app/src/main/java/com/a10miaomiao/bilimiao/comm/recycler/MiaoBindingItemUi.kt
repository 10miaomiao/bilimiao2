package com.a10miaomiao.bilimiao.comm.recycler

import android.view.View
import cn.a10miaomiao.miao.binding.MiaoBinding
import com.a10miaomiao.bilimiao.comm.MiaoUI
import java.lang.Exception

abstract class MiaoBindingItemUi<T> : MiaoUI() {

    override val root: View
        get() = throw NullPointerException()

    fun getView (binding: MiaoBinding, item: T, index: Int = 0): View {
        return binding.start(MiaoBinding.INIT) {
            miao { createView(item, index) }
        }
    }

    abstract fun createView (item: T, index: Int): View

    fun update(binding: MiaoBinding, item: T, index: Int) {
        try {
            binding.start(MiaoBinding.UPDATE) {
                createView(item, index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}