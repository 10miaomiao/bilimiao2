package com.a10miaomiao.miaoandriod.binding

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class LiveDataBinding(val lifecycleOwne: () -> LifecycleOwner) : ViewModel(), MiaoBinding {

    var liveDatas = mutableMapOf<String, MutableLiveData<*>>()
    var allFns = arrayListOf<List<() -> Unit>>()

    override fun bindData(fn: () -> Unit, key: String) {
        liveDatas[key]?.let{
//            it.observe() { fn.invoke() }
            it.observeForever { fn.invoke() }
        }
    }

    override fun updateView(key: String) {
        liveDatas[key]?.let{
            it.value = it.value
        }
    }

    fun updateView() {
        liveDatas.forEach { it.value.value = it.value.value }
        allFns.forEach { it.forEach { it() }}
    }

}