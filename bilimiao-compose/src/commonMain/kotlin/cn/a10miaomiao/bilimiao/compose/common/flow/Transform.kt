package cn.a10miaomiao.bilimiao.compose.common.flow

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

fun <T, R> StateFlow<T>.stateMap(transform: (T) -> R): StateFlow<R> {
    return object : StateFlow<R> {

        override val replayCache: List<R>
            get() = this@stateMap.replayCache.map { transform(it) }

        override val value: R
            get() = transform(this@stateMap.value)

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            this@stateMap.map { transform(it) }.collect(collector)
            error("StateFlow collection never ends.")
        }
    }
}