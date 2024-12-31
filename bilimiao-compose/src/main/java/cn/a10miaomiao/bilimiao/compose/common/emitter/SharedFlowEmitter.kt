package cn.a10miaomiao.bilimiao.compose.common.emitter

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Stable
class SharedFlowEmitter {

    private val sharedFlow = MutableSharedFlow<EmitterAction>()
    val flow: SharedFlow<EmitterAction> get () = sharedFlow

    suspend fun emit(action: EmitterAction) {
        sharedFlow.emit(action)
    }

    suspend fun collect(collector: FlowCollector<EmitterAction>) {
        sharedFlow.collect(collector)
    }

    suspend inline fun <reified T: EmitterAction> collectAction(collector: FlowCollector<T>) {
        collect {
            if (it is T) collector.emit(it)
        }
    }

}