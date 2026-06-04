package cn.a10miaomiao.bilimiao.compose.components.dialogs

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier


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

    fun open(
        title: String,
        text: String,
        confirmButton: @Composable () -> Unit,
        closeText: String = "关闭",
        showClose: Boolean = true,
    ) {
        _messageState.value = CustomState(
            title = { Text(title) },
            text = { Text(text) },
            dismissButton =  if (showClose) {
                {
                    TextButton(
                        onClick = ::close
                    ) {
                        Text(closeText)
                    }
                }
            } else null,
            confirmButton = confirmButton,
            onDismissRequest = ::close,
            modifier = Modifier,
            icon = null
        )
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

    class CustomState(
        val onDismissRequest: () -> Unit,
        val confirmButton: @Composable () -> Unit,
        val modifier: Modifier,
        val dismissButton: @Composable (() -> Unit)?,
        val icon: @Composable (() -> Unit)?,
        val title: @Composable (() -> Unit)?,
        val text: @Composable (() -> Unit)?,
    ): MessageState()

}