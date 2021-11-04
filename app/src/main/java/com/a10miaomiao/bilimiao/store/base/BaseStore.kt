package com.a10miaomiao.bilimiao.store.base

import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DIAware

@OptIn(InternalCoroutinesApi::class)
interface BaseStore<T> : DIAware {
    val stateFlow: MutableStateFlow<T>

    val state: T get() = stateFlow.value

    fun copyState(): T

    suspend fun connectUi (ui: MiaoBindingUi) {
        stateFlow.collect(object : FlowCollector<T> {
            override suspend fun emit(value: T) {
                ui.setState {  }
            }
        })
    }

    fun setState (block: T.() -> Unit) {
        stateFlow.value = copyState().apply(block)
    }

}
