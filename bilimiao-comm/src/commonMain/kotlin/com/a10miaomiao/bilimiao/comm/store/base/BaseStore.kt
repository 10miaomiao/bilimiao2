package com.a10miaomiao.bilimiao.comm.store.base

import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DIAware

interface BaseStore<T> : DIAware {
    val stateFlow: MutableStateFlow<T>

    val state: T get() = stateFlow.value

    fun copyState(): T

    fun init() {
    }

    fun setState (block: T.() -> Unit) {
        stateFlow.value = copyState().apply(block)
    }
}
