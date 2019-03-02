package com.a10miaomiao.miaoandriod.anko

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Context
import org.jetbrains.anko.AnkoContextImpl

open class LiveAnkoContext1<T>(
        override val ctx: Context,
        override val owner: LifecycleOwner,
        private val setContentView: Boolean,
        private val liveData: LiveData<T>
) : LiveAnkoContext(ctx, owner, setContentView) {

    fun observe(observer: (value: T?) -> Unit) {
        liveData.observe(owner, Observer { observer.invoke(it) })
    }

    fun observeNotNull(observer: (value: T) -> Unit) {
        liveData.observe(owner, Observer { it?.let(observer) })
    }

    fun <R> R.observe(observer: R.(value: T?) -> Unit): R {
        liveData.observe(owner, Observer { this.observer(it) })
        return this
    }

    fun <R> R.observeNotNull(observer: R.(value: T) -> Unit): R {
        liveData.observe(owner, Observer { if (it != null) this.observer(it) })
        return this
    }

    inline fun <R> ((R) -> Unit).bind(crossinline fn: (data: T) -> R) {
        observeNotNull { this(fn(it)) }
    }

    inline fun ((CharSequence?) -> Unit).bindText(crossinline fn: (data: T) -> String) {
        observeNotNull { this(fn(it)) }
    }
}