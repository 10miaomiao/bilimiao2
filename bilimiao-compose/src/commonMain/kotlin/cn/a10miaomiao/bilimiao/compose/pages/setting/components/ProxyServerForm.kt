package cn.a10miaomiao.bilimiao.compose.pages.setting.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun KeyValueListCard(
    title: String,
    buttonContent: @Composable RowScope.() -> Unit,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    content:  @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(
                    onClick = onButtonClick,
                    content = buttonContent,
                )
            }
            content()
        }
    }
}

@Stable
class KeyValueInputState(
    initialKey: String,
    initialValue: String,
) {
    var key by mutableStateOf(initialKey)
        private set
    var value by mutableStateOf(initialValue)
        private set

    fun changeKey(str: String) {
        key = str
    }

    fun changeValue(str: String) {
        value = str
    }
}

class KeyValueInputStateCarrier(
    private val initialKey: String = "",
    private val initialValue: String = "",
) {
    var inputState: KeyValueInputState? = null
    val key: String get() = inputState?.key ?: ""
    val value: String get() = inputState?.value ?: ""

    @Composable
    fun rememberKeyValueInputState(): KeyValueInputState {
        return inputState ?: remember {
            KeyValueInputState(initialKey, initialValue).also {
                inputState = it
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KeyValueInput(
    state: KeyValueInputState,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    infixSymbol: String = ":",
    keyPlaceholder: @Composable (() -> Unit)? = null,
    valuePlaceholder: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            placeholder = keyPlaceholder,
            value = state.key,
            onValueChange = state::changeKey,
            singleLine = true,
        )
        Text(
            text = infixSymbol,
            modifier = Modifier.padding(horizontal = 5.dp)
        )
        TextField(
            modifier = Modifier.weight(2f),
            placeholder = valuePlaceholder,
            value = state.value,
            onValueChange = state::changeValue,
            singleLine = true,
        )
        IconButton(
//            modifier = Modifier.padding(start = 5.dp),
            onClick = onRemoveClick
        ) {
            Icon(
                imageVector = Icons.Filled.RemoveCircleOutline,
                contentDescription = "移除",
            )
        }
    }
}

class ProxyServerFormState() {
    var name by mutableStateOf("")
        private set
    var host by mutableStateOf("")
        private set

    var isTrust by mutableStateOf(false)
        private set

    var enableAdvanced by mutableStateOf(false)
        private set

    val queryArgStates = mutableStateListOf<KeyValueInputStateCarrier>()
    val headerStates = mutableStateListOf<KeyValueInputStateCarrier>()

    fun changeName(str: String) {
        name = str
    }

    fun changeHost(str: String) {
        host = str
    }

    fun changeIsTrust(bool: Boolean) {
        isTrust = bool
    }

    fun changeEnableAdvanced(bool: Boolean) {
        enableAdvanced = bool
    }

    fun initQueryArgStates(list: List<KeyValueInputStateCarrier>) {
        queryArgStates.clear()
        queryArgStates.addAll(list)
    }

    fun initHeaderStates(list: List<KeyValueInputStateCarrier>) {
        headerStates.clear()
        headerStates.addAll(list)
    }
}

@Composable
fun rememberProxyServerFormState(): ProxyServerFormState {
    return remember {
        ProxyServerFormState()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyServerForm(
    state: ProxyServerFormState,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            value = state.name,
            onValueChange = state::changeName,
            label = {
                Text(text = "服务器名称")
            },
            singleLine = true,
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            value = state.host,
            onValueChange = state::changeHost,
            label = {
                Text(text = "服务器地址")
            },
            singleLine = true,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = state.isTrust,
                onCheckedChange = state::changeIsTrust,
            )
            Text(
                text = "信任该服务器",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = state.enableAdvanced,
                onCheckedChange = { state.changeEnableAdvanced(it) },
            )
            Text(
                text = "高级设置",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (state.enableAdvanced) {
            KeyValueListCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                title = "请求参数",
                buttonContent = {
                    Text(text = "增加参数")
                },
                onButtonClick = {
                    state.queryArgStates.add(KeyValueInputStateCarrier())
                },
            ) {
                state.queryArgStates.forEachIndexed { index, stateCarrier ->
                    KeyValueInput(
                        state = stateCarrier.rememberKeyValueInputState(),
                        modifier = Modifier.padding(bottom = 5.dp),
                        infixSymbol = "=",
                        keyPlaceholder = { Text(text = "参数名") },
                        valuePlaceholder = { Text(text = "参数值") },
                        onRemoveClick = {
                            state.queryArgStates.removeAt(index)
                        }
                    )
                }
            }
            KeyValueListCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                title = "请求头",
                buttonContent = {
                    Text(text = "增加请求头")
                },
                onButtonClick = {
                    state.headerStates.add(KeyValueInputStateCarrier())
                },
            ) {
                state.headerStates.forEachIndexed { index, stateCarrier ->
                    KeyValueInput(
                        state = stateCarrier.rememberKeyValueInputState(),
                        modifier = Modifier.padding(bottom = 5.dp),
                        keyPlaceholder = { Text(text = "名称") },
                        valuePlaceholder = { Text(text = "值") },
                        onRemoveClick = {
                            state.headerStates.removeAt(index)
                        }
                    )
                }
            }
        }
        Text(
            text = """注意事项：
1、服务器名称任意填写，如：猫猫的服务器、鼠鼠的服务器。
2、服务器地址即服务器域名，如：10miaomiao.cn、fuck.bilibili.com。
3、勾选"信任该服务器"则会提交登录信息(token)至该服务器，请确认服务器安全后再勾选。
4、勾选"信任该服务器"后,如发现帐号有异常行为,请立即修改密码，并取消信任或删除该服务器。""",
            modifier = Modifier.padding(vertical = 5.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}