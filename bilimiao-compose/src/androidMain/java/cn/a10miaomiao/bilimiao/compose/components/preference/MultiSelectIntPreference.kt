package cn.a10miaomiao.bilimiao.compose.components.preference

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import me.zhanghai.compose.preference.MultiSelectListPreference
import me.zhanghai.compose.preference.rememberPreferenceState

fun LazyListScope.multiSelectIntPreference(
    key: String,
    defaultValue: Int,
    values: List<Int>,
    title: @Composable (Int) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    rememberState: @Composable () -> MutableState<Int> = {
        rememberPreferenceState(key, defaultValue)
    },
    enabled: (Int) -> Boolean = { true },
    icon: @Composable ((Int) -> Unit)? = null,
    summary: @Composable ((Int) -> Unit)? = null,
    valueToText: (Int) -> AnnotatedString = { AnnotatedString(it.toString()) },
) {
    item(key = key, contentType = "MultiSelectListPreference") {
        val state = rememberState()
        val value = state.value
        val intSet = remember(value, values) {
            values.filter {
                value and it == it
            }.toSet()
        }
        MultiSelectListPreference(
            value = intSet,
            onValueChange = {
                state.value = it.fold(0) { acc, i ->
                    acc or i
                }
            },
            values = values,
            title = { title(state.value) },
            modifier = modifier,
            enabled = enabled(state.value),
            icon = icon?.let { { it(state.value) } },
            summary = summary?.let { { it(state.value) } },
            valueToText = valueToText,
        )
    }
}