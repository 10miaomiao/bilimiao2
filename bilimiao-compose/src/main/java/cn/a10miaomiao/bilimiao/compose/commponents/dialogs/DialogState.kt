package cn.a10miaomiao.bilimiao.compose.commponents.dialogs

import androidx.compose.runtime.*

@Composable
fun rememberDialogState(): DialogState {
    return remember {
        DialogState()
    }
}

@Stable
class DialogState() {
    var openDialog: Boolean by mutableStateOf(false)

}