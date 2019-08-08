package com.a10miaomiao.miaoandriod

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.content.Context
import android.view.View
import android.widget.FrameLayout

open class MiaoView(context: Context) : FrameLayout(context), LifecycleOwner {

    val _lifecycle = LifecycleRegistry(this)

    init {
        _lifecycle.markState(Lifecycle.State.CREATED)
    }

    override fun getLifecycle() = _lifecycle

    override fun onAttachedToWindow() {
        _lifecycle.markState(Lifecycle.State.STARTED)
        super.onAttachedToWindow()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        when (visibility) {
            VISIBLE -> _lifecycle.markState(Lifecycle.State.STARTED)
            GONE -> _lifecycle.markState(Lifecycle.State.CREATED)
        }
    }

    override fun onDetachedFromWindow() {
        _lifecycle.markState(Lifecycle.State.DESTROYED)
        super.onDetachedFromWindow()
    }


}