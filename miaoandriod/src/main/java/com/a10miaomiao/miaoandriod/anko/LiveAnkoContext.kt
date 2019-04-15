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

    fun <T> LiveData<T>.observe(observer: (value: T?) -> Unit) {
        this.observe(owner, Observer { observer.invoke(it) })
    }

    fun <T> LiveData<T>.observeNotNull(observer: (value: T) -> Unit) {
        this.observe(owner, Observer { it?.let(observer) })
    }

    fun ((CharSequence?) -> Unit).observeText(liveData: LiveData<String>) {
        liveData.observe { this.invoke(it) }
    }

    fun <T> ((T?) -> Unit).observe(liveData: LiveData<T>) {
        liveData.observe(this)
    }
}

