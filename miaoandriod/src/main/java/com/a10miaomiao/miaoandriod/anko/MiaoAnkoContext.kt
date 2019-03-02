package com.a10miaomiao.miaoandriod.anko

import android.content.Context
import com.a10miaomiao.miaoandriod.binding.MiaoBinding
import com.a10miaomiao.miaoandriod.binding.MiaoBindingImpl
import org.jetbrains.anko.AnkoContextImpl

open class MiaoAnkoContext<T>(
        override val ctx: Context,
        override val owner: T,
        private val setContentView: Boolean
) : AnkoContextImpl<T>(ctx, owner, setContentView), MiaoBinding {
    var binding = MiaoBindingImpl()

    override fun bindData(fn: () -> Unit, key: String) = binding.bindData(fn, key)

    override fun updateView(key: String) = binding.updateView(key)
}


