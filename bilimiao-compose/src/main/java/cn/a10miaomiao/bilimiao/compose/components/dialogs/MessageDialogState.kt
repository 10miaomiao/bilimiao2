package cn.a10miaomiao.bilimiao.compose.components.dialogs

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf


@Stable
class MessageDialogState() {

    private val _messageState = mutableStateOf<MessageState>(NoneState)
    val messageState: State<MessageState> get() = _messageState

    fun alert(
        text: String,
        title: String = "提示",
    ) {
        _messageState.value = AlertState(
            title = title,
            text = text,
        )
    }

    fun loading(
        title: String = "加载中"
    ) {
        _messageState.value = LoadingState(
            title = title,
        )
    }

    fun close() {
        _messageState.value = NoneState
    }


    sealed class MessageState

    data object NoneState: MessageState()

    data class AlertState(
        val title: String,
        val text: String,
    ): MessageState()

    data class LoadingState(
        val title: String
    ): MessageState()

}