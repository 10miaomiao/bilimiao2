package cn.a10miaomiao.bilimiao.compose.common.preference

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.flow.MutableStateFlow
import me.zhanghai.compose.preference.preferenceCategory as zhPreferenceCategory
import me.zhanghai.compose.preference.switchPreference as zhSwitchPreference
import me.zhanghai.compose.preference.preference as zhPreference
import me.zhanghai.compose.preference.sliderPreference as zhSliderPreference
import me.zhanghai.compose.preference.listPreference as zhListPreference

// 桥接适配器：将本项目的 Preferences 适配为 me.zhanghai.compose.preference.Preferences
private class ZHPrefsAdapter(
    private val common: Preferences,
) : me.zhanghai.compose.preference.Preferences {
    override fun <T> get(key: String): T? = common.get(key)
    override fun asMap(): Map<String, Any> = common.asMap()
    override fun toMutablePreferences(): me.zhanghai.compose.preference.MutablePreferences {
        val commonMutable = common.toMutablePreferences()
        return ZHMutablePrefsAdapter(commonMutable)
    }
}

private class ZHMutablePrefsAdapter(
    private val common: MutablePreferences,
) : me.zhanghai.compose.preference.MutablePreferences {
    override fun <T> get(key: String): T? = common.get(key)
    override fun asMap(): Map<String, Any> = common.asMap()
    override fun toMutablePreferences(): me.zhanghai.compose.preference.MutablePreferences =
        ZHMutablePrefsAdapter(common.toMutablePreferences())
    override fun <T> set(key: String, value: T?) = common.set(key, value)
    override fun clear() = common.clear()
}

/**
 * 将本项目的 [MutableStateFlow]<[Preferences]> 转换为库的
 * [MutableStateFlow]<[me.zhanghai.compose.preference.Preferences]>。
 */
@Composable
private fun rememberZHPrefsFlow(
    commonFlow: MutableStateFlow<Preferences>,
): MutableStateFlow<me.zhanghai.compose.preference.Preferences> {
    val zhFlow = remember {
        MutableStateFlow<me.zhanghai.compose.preference.Preferences>(
            ZHPrefsAdapter(commonFlow.value)
        )
    }
    LaunchedEffect(commonFlow) {
        commonFlow.collect {
            zhFlow.value = ZHPrefsAdapter(it)
        }
    }
    return zhFlow
}

@Composable
fun ProvidePreferenceLocals(
    flow: MutableStateFlow<Preferences>? = null,
    content: @Composable () -> Unit,
) {
    if (flow != null) {
        val zhFlow = rememberZHPrefsFlow(flow)
        me.zhanghai.compose.preference.ProvidePreferenceLocals(
            flow = zhFlow,
            content = content,
        )
    } else {
        me.zhanghai.compose.preference.ProvidePreferenceLocals(
            content = content,
        )
    }
}

@Composable
fun <T> rememberPreferenceState(
    key: String,
    defaultValue: T,
): MutableState<T> {
    return me.zhanghai.compose.preference.rememberPreferenceState(key, defaultValue)
}

enum class ListPreferenceType {
    ALERT_DIALOG,
    DROPDOWN_MENU,
}

fun LazyListScope.preferenceCategory(
    key: String,
    title: @Composable () -> Unit,
) {
    zhPreferenceCategory(key, title)
}

fun LazyListScope.switchPreference(
    key: String,
    defaultValue: Boolean,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    summary: @Composable ((Boolean) -> Unit)? = null,
    enabled: () -> Boolean = { true },
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

fun LazyListScope.preference(
    key: String,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
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

fun LazyListScope.sliderPreference(
    key: String,
    defaultValue: Float,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    valueRange: ClosedRange<Float> = 0f..1f,
    valueSteps: Int = 0,
    enabled: () -> Boolean = { true },
    summary: @Composable ((Float) -> Unit)? = null,
    valueText: @Composable ((Float) -> Unit)? = null,
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

fun <T> LazyListScope.listPreference(
    key: String,
    defaultValue: T,
    modifier: Modifier = Modifier,
    type: ListPreferenceType = ListPreferenceType.DROPDOWN_MENU,
    title: @Composable () -> Unit,
    summary: @Composable ((T) -> Unit)? = null,
    values: List<T>,
    valueToText: (T) -> AnnotatedString = { AnnotatedString(it.toString()) },
) {
    zhListPreference(
        key = key,
        defaultValue = defaultValue,
        modifier = modifier,
        type = when (type) {
            ListPreferenceType.ALERT_DIALOG -> me.zhanghai.compose.preference.ListPreferenceType.ALERT_DIALOG
            ListPreferenceType.DROPDOWN_MENU -> me.zhanghai.compose.preference.ListPreferenceType.DROPDOWN_MENU
        },
        title = { title() },
        summary = summary,
        values = values,
        valueToText = { valueToText(it) },
    )
}

// Composable preference components used by custom preference files
@Composable
fun Preference(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
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
fun SliderPreference(
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
fun MultiSelectListPreference(
    value: Set<Any>,
    onValueChange: (Set<Any>) -> Unit,
    values: List<Any>,
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    summary: @Composable (() -> Unit)? = null,
    valueToText: (Any) -> AnnotatedString = { AnnotatedString(it.toString()) },
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
        valueToText = { valueToText(it) },
    )
}
