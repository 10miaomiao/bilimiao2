package com.a10miaomiao.miaoandriod.anko

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.support.v4.app.Fragment
import com.a10miaomiao.miaoandriod.binding.MiaoBinding
import com.a10miaomiao.miaoandriod.binding.MiaoBindingImpl
import org.jetbrains.anko.AnkoContextImpl

open class LiveAnkoContext(
        override val ctx: Context,
        override val owner: LifecycleOwner,
        private val setContentView: Boolean
) : AnkoContextImpl<LifecycleOwner>(ctx, owner, setContentView) {
    fun <T> observe(liveData: LiveData<T>, observer: (value: T?) -> Unit) {
        liveData.observe(owner, Observer { observer.invoke(it) })
    }

    fun <T> observeNotNull(liveData: LiveData<T>, observer: (value: T) -> Unit) {
        liveData.observe(owner, Observer { it?.let(observer) })
    }

    fun <T, R> T.observe(liveData: LiveData<R>, observer: T.(value: R?) -> Unit): T {
        liveData.observe(owner, Observer { this.observer(it) })
        return this
    }

    fun <T, R> T.observeNotNull(liveData: LiveData<R>, observer: T.(value: R) -> Unit): T {
        liveData.observe(owner, Observer { if (it != null) this.observer(it) })
        return this
    }

    fun ((CharSequence?) -> Unit).observeText(liveData: LiveData<String>) {
        observe(liveData) { this.invoke(it) }
    }

    fun <T> ((T?) -> Unit).observe(liveData: LiveData<T>) {
        observe(liveData, this)
    }
}

