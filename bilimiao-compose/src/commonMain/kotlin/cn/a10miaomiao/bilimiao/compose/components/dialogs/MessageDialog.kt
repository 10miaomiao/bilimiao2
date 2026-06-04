package cn.a10miaomiao.bilimiao.compose.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun MessageDialog(
    state: MessageDialogState
) {
    when(val messageState = state.messageState.value) {
        MessageDialogState.NoneState -> Unit
        is MessageDialogState.AlertState -> {
            AlertDialog(
                onDismissRequest = state::close,
                title = {
                    Text(messageState.title)
                },
                text = {
                    Text(messageState.text)
                },
                confirmButton = {
                    TextButton(
                        onClick = state::close,
                    ) {
                        Text("确定")
                    }
                }
            )
        }
        is MessageDialogState.LoadingState -> {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(messageState.title)
                },
                text = {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {  }
            )
        }
        is MessageDialogState.CustomState -> {
            AlertDialog(
                onDismissRequest = messageState.onDismissRequest,
                confirmButton = messageState.confirmButton,
                modifier = messageState.modifier,
                dismissButton = messageState.dismissButton,
                icon = messageState.icon,
                title = messageState.title,
                text = messageState.text,
            )
        }
    }
}
