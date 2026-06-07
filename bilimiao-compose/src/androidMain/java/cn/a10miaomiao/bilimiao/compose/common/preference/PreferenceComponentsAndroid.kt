package cn.a10miaomiao.bilimiao.compose.common.preference

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.flow.MutableStateFlow
import me.zhanghai.compose.preference.preferenceCategory as zhPreferenceCategory
import me.zhanghai.compose.preference.switchPreference as zhSwitchPreference
import me.zhanghai.compose.preference.preference as zhPreference
import me.zhanghai.compose.preference.sliderPreference as zhSliderPreference
import me.zhanghai.compose.preference.listPreference as zhListPreference

// Bridge wrapper: adapts our common Preferences to me.zhanghai.compose.preference.Preferences
private class ZHPrefsAdapter(
    private val common: cn.a10miaomiao.bilimiao.compose.common.preference.Preferences
) : me.zhanghai.compose.preference.Preferences {
    override fun <T> get(key: String): T? = common.get(key)
    override fun asMap(): Map<String, Any> = common.asMap()
    override fun toMutablePreferences(): me.zhanghai.compose.preference.MutablePreferences {
        val commonMutable = common.toMutablePreferences()
        return ZHMutablePrefsAdapter(commonMutable)
    }
}

private class ZHMutablePrefsAdapter(
    private val common: cn.a10miaomiao.bilimiao.compose.common.preference.MutablePreferences
) : me.zhanghai.compose.preference.MutablePreferences {
    override fun <T> get(key: String): T? = common.get(key)
    override fun asMap(): Map<String, Any> = common.asMap()
    override fun toMutablePreferences(): me.zhanghai.compose.preference.MutablePreferences =
        ZHMutablePrefsAdapter(common.toMutablePreferences())
    override fun <T> set(key: String, value: T?) = common.set(key, value)
    override fun clear() = common.clear()
}

@Composable
actual fun ProvidePreferenceLocals(
    flow: MutableStateFlow<cn.a10miaomiao.bilimiao.compose.common.preference.Preferences>?,
    content: @Composable () -> Unit
) {
    if (flow != null) {
        val zhFlow = rememberZHPrefsFlow(flow)
        me.zhanghai.compose.preference.ProvidePreferenceLocals(
            flow = zhFlow,
            content = content
        )
    } else {
        me.zhanghai.compose.preference.ProvidePreferenceLocals(
            content = content
        )
    }
}

@Composable
private fun rememberZHPrefsFlow(
    commonFlow: MutableStateFlow<cn.a10miaomiao.bilimiao.compose.common.preference.Preferences>
): MutableStateFlow<me.zhanghai.compose.preference.Preferences> {
    val zhFlow = androidx.compose.runtime.remember {
        MutableStateFlow<me.zhanghai.compose.preference.Preferences>(
            ZHPrefsAdapter(commonFlow.value)
        )
    }
    androidx.compose.runtime.LaunchedEffect(commonFlow) {
        commonFlow.collect {
            zhFlow.value = ZHPrefsAdapter(it)
        }
    }
    return zhFlow
}

@Composable
actual fun <T> rememberPreferenceState(
    key: String,
    defaultValue: T,
): MutableState<T> {
    return me.zhanghai.compose.preference.rememberPreferenceState(key, defaultValue)
}

actual enum class ListPreferenceType {
    DROPDOWN_MENU,
}

actual fun LazyListScope.preferenceCategory(
    key: String,
    title: @Composable () -> Unit,
) {
    zhPreferenceCategory(key, title)
}

actual fun LazyListScope.switchPreference(
    key: String,
    defaultValue: Boolean,
    modifier: Modifier,
    title: @Composable () -> Unit,
    summary: @Composable ((Boolean) -> Unit)?,
    enabled: () -> Boolean,
) {
    zhSwitchPreference(
        key = key,
        defaultValue = defaultValue,
        modifier = modifier,
        title = { title() },
        summary = summary,
        enabled = { enabled() },
    )
}

actual fun LazyListScope.preference(
    key: String,
    modifier: Modifier,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)?,
    enabled: Boolean,
    icon: @Composable (() -> Unit)?,
    onClick: () -> Unit,
) {
    zhPreference(
        key = key,
        modifier = modifier,
        title = title,
        summary = summary,
        enabled = enabled,
        icon = icon,
        onClick = onClick,
    )
}

actual fun LazyListScope.sliderPreference(
    key: String,
    defaultValue: Float,
    modifier: Modifier,
    title: @Composable () -> Unit,
    valueRange: ClosedRange<Float>,
    valueSteps: Int,
    enabled: () -> Boolean,
    summary: @Composable ((Float) -> Unit)?,
    valueText: @Composable ((Float) -> Unit)?,
) {
    zhSliderPreference(
        key = key,
        defaultValue = defaultValue,
        modifier = modifier,
        title = { title() },
        valueRange = valueRange.start..valueRange.endInclusive,
        valueSteps = valueSteps,
        enabled = { enabled() },
        summary = summary,
        valueText = valueText,
    )
}

actual fun <T> LazyListScope.listPreference(
    key: String,
    defaultValue: T,
    modifier: Modifier,
    type: ListPreferenceType,
    title: @Composable () -> Unit,
    summary: @Composable ((T) -> Unit)?,
    values: List<T>,
    valueToText: (T) -> AnnotatedString,
) {
    zhListPreference(
        key = key,
        defaultValue = defaultValue,
        modifier = modifier,
        type = when (type) {
            ListPreferenceType.DROPDOWN_MENU -> me.zhanghai.compose.preference.ListPreferenceType.DROPDOWN_MENU
        },
        title = { title() },
        summary = summary,
        values = values,
        valueToText = valueToText,
    )
}

@Composable
actual fun Preference(
    modifier: Modifier,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)?,
    icon: @Composable (() -> Unit)?,
    onClick: () -> Unit,
) {
    me.zhanghai.compose.preference.Preference(
        modifier = modifier,
        title = title,
        summary = summary,
        icon = icon,
        onClick = onClick,
    )
}

@Composable
actual fun SliderPreference(
    value: Float,
    onValueChange: (Float) -> Unit,
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    title: @Composable () -> Unit,
    modifier: Modifier,
    valueRange: ClosedRange<Float>,
    valueSteps: Int,
    enabled: Boolean,
    icon: @Composable (() -> Unit)?,
    summary: @Composable (() -> Unit)?,
    valueText: @Composable ((Float) -> Unit)?,
) {
    me.zhanghai.compose.preference.SliderPreference(
        value = value,
        onValueChange = onValueChange,
        sliderValue = sliderValue,
        onSliderValueChange = onSliderValueChange,
        title = title,
        modifier = modifier,
        valueRange = valueRange.start..valueRange.endInclusive,
        valueSteps = valueSteps,
        enabled = enabled,
        icon = icon,
        summary = summary,
        valueText = valueText?.let { textFn -> { textFn(sliderValue) } },
    )
}

@Composable
actual fun MultiSelectListPreference(
    value: Set<Any>,
    onValueChange: (Set<Any>) -> Unit,
    values: List<Any>,
    title: @Composable () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    icon: @Composable (() -> Unit)?,
    summary: @Composable (() -> Unit)?,
    valueToText: (Any) -> AnnotatedString,
) {
    me.zhanghai.compose.preference.MultiSelectListPreference(
        value = value,
        onValueChange = onValueChange,
        values = values,
        title = title,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        summary = summary,
        valueToText = valueToText,
    )
}
