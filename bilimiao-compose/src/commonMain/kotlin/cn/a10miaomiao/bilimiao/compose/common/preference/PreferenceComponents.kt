package cn.a10miaomiao.bilimiao.compose.common.preference

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
expect fun ProvidePreferenceLocals(
    flow: MutableStateFlow<Preferences>? = null,
    content: @Composable () -> Unit
)

@Composable
expect fun <T> rememberPreferenceState(
    key: String,
    defaultValue: T,
): MutableState<T>

expect enum class ListPreferenceType {
    DROPDOWN_MENU,
}

expect fun LazyListScope.preferenceCategory(
    key: String,
    title: @Composable () -> Unit,
)

expect fun LazyListScope.switchPreference(
    key: String,
    defaultValue: Boolean,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    summary: @Composable ((Boolean) -> Unit)? = null,
    enabled: () -> Boolean = { true },
)

expect fun LazyListScope.preference(
    key: String,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
)

expect fun LazyListScope.sliderPreference(
    key: String,
    defaultValue: Float,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    valueRange: ClosedRange<Float> = 0f..1f,
    valueSteps: Int = 0,
    enabled: () -> Boolean = { true },
    summary: @Composable ((Float) -> Unit)? = null,
    valueText: @Composable ((Float) -> Unit)? = null,
)

expect fun <T> LazyListScope.listPreference(
    key: String,
    defaultValue: T,
    modifier: Modifier = Modifier,
    type: ListPreferenceType = ListPreferenceType.DROPDOWN_MENU,
    title: @Composable () -> Unit,
    summary: @Composable ((T) -> Unit)? = null,
    values: List<T>,
    valueToText: (T) -> AnnotatedString = { AnnotatedString(it.toString()) },
)

// Composable preference components used by custom preference files
@Composable
expect fun Preference(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
)

@Composable
expect fun SliderPreference(
    value: Float,
    onValueChange: (Float) -> Unit,
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedRange<Float> = 0f..1f,
    valueSteps: Int = 0,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    valueText: @Composable ((Float) -> Unit)? = null,
)

@Composable
expect fun MultiSelectListPreference(
    value: Set<Any>,
    onValueChange: (Set<Any>) -> Unit,
    values: List<Any>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    valueToText: (Any) -> AnnotatedString = { AnnotatedString(it.toString()) },
)
