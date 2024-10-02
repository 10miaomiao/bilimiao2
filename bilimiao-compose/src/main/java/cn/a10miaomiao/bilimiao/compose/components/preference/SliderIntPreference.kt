package cn.a10miaomiao.bilimiao.compose.components.preference

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.zhanghai.compose.preference.SliderPreference
import me.zhanghai.compose.preference.rememberPreferenceState

inline fun LazyListScope.sliderIntPreference(
    key: String,
    defaultValue: Int,
    crossinline title: @Composable (Int) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    crossinline rememberState: @Composable () -> MutableState<Int> = {
        rememberPreferenceState(key, defaultValue)
    },
    valueRange: ClosedRange<Int> = 0..100,
    valueSteps: Int = 0,
    crossinline rememberSliderState: @Composable (Int) -> MutableState<Int> = {
        remember { mutableIntStateOf(it) }
    },
    crossinline enabled: (Int) -> Boolean = { true },
    noinline icon: @Composable ((Int) -> Unit)? = null,
    noinline summary: @Composable ((Int) -> Unit)? = null,
    noinline valueText: @Composable ((Int) -> Unit)? = null
) {
    item(key = key, contentType = "SliderIntPreference") {
        val state = rememberState()
        val value by state
        val sliderState = rememberSliderState(value)
        val sliderValue by sliderState
        SliderIntPreference(
            state = state,
            title = { title(sliderValue) },
            modifier = modifier,
            valueRange = valueRange,
            valueSteps = valueSteps,
            sliderState = sliderState,
            enabled = enabled(value),
            icon = icon?.let { { it(sliderValue) } },
            summary = summary?.let { { it(sliderValue) } },
            valueText = valueText?.let { { it(sliderValue) } }
        )
    }
}

@Composable
fun SliderIntPreference(
    state: MutableState<Int>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedRange<Int> = 0..100,
    valueSteps: Int = 0,
    sliderState: MutableState<Int> = remember { mutableIntStateOf(state.value) },
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    valueText: @Composable (() -> Unit)? = null
) {
    var value by state
    var sliderValue by sliderState
    SliderPreference(
        value = value.toFloat(),
        onValueChange = { value = it.toInt() },
        sliderValue = sliderValue.toFloat(),
        onSliderValueChange = { sliderValue = it.toInt() },
        title = title,
        modifier = modifier,
        valueRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat(),
        valueSteps = valueSteps,
        enabled = enabled,
        icon = icon,
        summary = summary,
        valueText = valueText
    )
}