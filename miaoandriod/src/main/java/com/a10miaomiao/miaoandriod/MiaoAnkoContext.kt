package com.a10miaomiao.miaoandriod

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.support.v4.app.Fragment
import android.system.Os.bind
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.AnkoContextImpl
import org.jetbrains.anko.internals.AnkoInternals.createAnkoContext
import kotlin.reflect.KProperty0

open class MiaoAnkoContext<T>(
        override val ctx: Context,
        override val owner: T,
        private val setContentView: Boolean
) : AnkoContextImpl<T>(ctx, owner, setContentView), MiaoBinding {
    var binding = MiaoBindingImpl()

    override fun bindData(fn: () -> Unit, key: String) = binding.bindData(fn, key)

    override fun updateView(key: String) = binding.updateView(key)
}

inline fun <T> T.createMiaoAnkoContext(
        ctx: Context,
        init: MiaoAnkoContext<T>.() -> Unit,
        setContentView: Boolean = false
): MiaoAnkoContext<T> {
    val dsl = MiaoAnkoContext(ctx, this, setContentView)
    dsl.init()
    return dsl
}

inline fun Context.MiaoUI(setContentView: Boolean, init: MiaoAnkoContext<Context>.() -> Unit): MiaoAnkoContext<Context> =
        createMiaoAnkoContext(this, init, setContentView)

inline fun Context.MiaoUI(init: MiaoAnkoContext<Context>.() -> Unit): MiaoAnkoContext<Context> =
        createMiaoAnkoContext(this, init)

inline fun Fragment.MiaoUI(init: MiaoAnkoContext<Fragment>.() -> Unit): MiaoAnkoContext<Fragment> =
        createMiaoAnkoContext(context!!, init)