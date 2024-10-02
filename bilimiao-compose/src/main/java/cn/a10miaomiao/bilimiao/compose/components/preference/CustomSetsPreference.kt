package cn.a10miaomiao.bilimiao.compose.components.preference

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kongzue.dialogx.dialogs.PopTip
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.rememberPreferenceState

inline fun LazyListScope.customSetsPreference(
    key: String,
    defaultValue: Set<String>,
    crossinline valueText: @Composable (String) -> Unit,
    crossinline title: @Composable () -> Unit,
    crossinline valueCanEdit: (String) -> Boolean = { true },
    crossinline canAdd: ((Set<String>) -> Boolean) = { true },
    modifier: Modifier = Modifier.fillMaxWidth(),
    crossinline rememberState: @Composable () -> MutableState<Set<String>> = {
        rememberPreferenceState(key, defaultValue)
    },
) {
    item(key = key, contentType = "listStylePreference") {
        val state = rememberState()
        CustomSetsPreference(
            state = state,
            title = { title() },
            modifier = modifier,
            valueText = { valueText(it) },
            valueCanEdit = { valueCanEdit(it) },
            canAdd = { canAdd(it) }
        )
    }
}

@Composable
fun CustomSetsPreference(
    state: MutableState<Set<String>>,
    title: @Composable () -> Unit,
    valueText: @Composable (String) -> Unit,
    valueCanEdit: ((String) -> Boolean) = { true },
    canAdd: ((Set<String>) -> Boolean) = { true },
    modifier: Modifier = Modifier,
) {
    var value by state
    CustomSetsPreference(
        value = value,
        onValueChange = { value = it },
        title = title,
        valueText = valueText,
        valueCanEdit = valueCanEdit,
        canAdd = canAdd,
        modifier = modifier,
    )
}

private sealed class EditDialogState {
    data object Closed : EditDialogState()
    data object Add : EditDialogState()
    data class Update(val value: String) : EditDialogState()
}

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
fun CustomSetsPreference(
    value: Set<String>,
    onValueChange: (Set<String>) -> Unit,
    title: @Composable () -> Unit,
    valueText: @Composable (String) -> Unit,
    valueCanEdit: ((String) -> Boolean) = { true },
    canAdd: ((Set<String>) -> Boolean) = { true },
    modifier: Modifier = Modifier,
) {
    val editDialogState = remember {
        mutableStateOf<EditDialogState>(EditDialogState.Closed)
    }
    Preference(
        modifier = modifier,
        title = title,
        summary = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                value.forEach { text ->
                    Chip(
                        onClick = {
                            if (valueCanEdit(text)) {
                                editDialogState.value = EditDialogState.Update(text)
                            } else {
                                PopTip.show("禁止编辑")
                            }
                        },
                        content = {
                            valueText(text)
                        },
                    )
                }
                if (canAdd(value)) {
                    Chip(onClick = {
                        editDialogState.value = EditDialogState.Add
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "add"
                        )
                    }
                }
            }
        }
    )
    val editDialogStateValue = editDialogState.value
    if (editDialogStateValue is EditDialogState.Add) {
        val text = remember { mutableStateOf("") }
        val errorMessage = remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {
                editDialogState.value = EditDialogState.Closed
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val v = text.value.toDoubleOrNull()
                        if (v == null || v <= 0) {
                            errorMessage.value = "请输入有效值"
                        } else if(v > 10) {
                            errorMessage.value = "最高10倍速"
                        } else if(v in value.map { it.toDouble() }) {
                            errorMessage.value = "已存在$v"
                        } else {
                            onValueChange(setOf(
                                *value.toTypedArray(),
                                v.toString(),
                            ))
                            editDialogState.value = EditDialogState.Closed
                            PopTip.show("添加成功")
                        }
                    }
                ) {
                    Text(text = "确认添加")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editDialogState.value = EditDialogState.Closed
                    }
                ) {
                    Text(text = "取消")
                }
            },
            title = {
                Text(text = "添加")
            },
            text = {
                TextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    placeholder = {
                        Text("输入值")
                    },
                    supportingText = {
                        if (errorMessage.value.isNotEmpty()) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = errorMessage.value,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                )
            }
        )
    } else if (editDialogStateValue is EditDialogState.Update) {
        AlertDialog(
            onDismissRequest = {
                editDialogState.value = EditDialogState.Closed
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(
                            value.filter {
                                it != editDialogStateValue.value
                            }.toSet()
                        )
                        editDialogState.value = EditDialogState.Closed
                        PopTip.show("移除成功")
                    }
                ) {
                    Text(
                        text = "移除",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editDialogState.value = EditDialogState.Closed
                    }
                ) {
                    Text(text = "取消")
                }
            },
            title = {
                Text(text = "编辑")
            },
            text = {
                TextField(
                    value = editDialogStateValue.value,
                    onValueChange = {  },
                    enabled = false,
                )
            }
        )
    }
}