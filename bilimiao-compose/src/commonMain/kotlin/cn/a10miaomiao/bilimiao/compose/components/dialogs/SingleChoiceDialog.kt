package cn.a10miaomiao.bilimiao.compose.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SingleChoiceItem(
    val name: String,
    val value: String,
)

@Composable
fun SingleChoiceDialog(
    state: DialogState,
    title: String,
    list: List<SingleChoiceItem>,
    selected: String,
    onChange: (value: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    if (state.openDialog) {
        AlertDialog(
            onDismissRequest = {
                state.openDialog = false
            },
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.titleSmall
                )
            },
            text = {
                Column() {
                    for (item in list) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                item.value == selected,
                                onClick = {
                                    if (selected != item.value) {
                                        onChange(item.value)
                                        scope.launch {
                                            delay(200)
                                            state.openDialog = false
                                        }
                                    }
                                }
                            )
                            Text(item.name)
                        }

                    }
                }
            },
            confirmButton = {
            }
        )
    }
}