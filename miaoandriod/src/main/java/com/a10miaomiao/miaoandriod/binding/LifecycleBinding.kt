package com.a10miaomiao.miaoandriod.binding

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel

class LifecycleBinding(val lifecycleOwner: () -> LifecycleOwner) : MiaoBindingImpl() {
    override fun bindData(fn: () -> Unit, key: String) {
        if (lifecycleOwner.invoke().lifecycle.currentState == Lifecycle.State.DESTROYED)
            return
        super.bindData(fn, key)
    }

    override fun updateView(key: String) {
        if (lifecycleOwner.invoke().lifecycle.currentState == Lifecycle.State.DESTROYED)
            return
        super.updateView(key)
    }
}