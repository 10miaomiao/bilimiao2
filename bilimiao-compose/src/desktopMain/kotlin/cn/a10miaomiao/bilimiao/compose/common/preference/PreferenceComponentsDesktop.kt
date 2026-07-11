package cn.a10miaomiao.bilimiao.compose.common.preference

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

internal val LocalPreferenceFlow =
    compositionLocalOf<MutableStateFlow<cn.a10miaomiao.bilimiao.compose.common.preference.Preferences>?> {
        null
    }

@Composable
actual fun ProvidePreferenceLocals(
    flow: MutableStateFlow<cn.a10miaomiao.bilimiao.compose.common.preference.Preferences>?,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalPreferenceFlow provides flow) {
        content()
    }
}

@Composable
actual fun <T> rememberPreferenceState(
    key: String,
    defaultValue: T,
): MutableState<T> {
    val flow = LocalPreferenceFlow.current
    // Local mutable state that the UI reads/writes.
    val state = remember(key) { mutableStateOf(defaultValue) }
    // Sync from flow -> local state whenever the flow emits.
    val prefs = flow?.collectAsState()?.value
    LaunchedEffect(prefs) {
        state.value = prefs?.get<T>(key) ?: defaultValue
    }
    // Write back to flow whenever local state changes (debounced via distinctUntilChanged).
    LaunchedEffect(flow) {
        snapshotFlow { state.value }
            .distinctUntilChanged()
            .collect { newValue ->
                val currentFlow = flow ?: return@collect
                val current = currentFlow.value.get<T>(key)
                if (current != newValue) {
                    val mutable = currentFlow.value.toMutablePreferences()
                    mutable.set(key, newValue)
                    currentFlow.value = mutable
                }
            }
    }
    return state
}

actual enum class ListPreferenceType {
    DROPDOWN_MENU,
}

actual fun LazyListScope.preferenceCategory(
    key: String,
    title: @Composable () -> Unit,
) {
    item(key = key, contentType = "PreferenceCategory") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            title()
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

actual fun LazyListScope.switchPreference(
    key: String,
    defaultValue: Boolean,
    modifier: Modifier,
    title: @Composable () -> Unit,
    summary: @Composable ((Boolean) -> Unit)?,
    enabled: () -> Boolean,
) {
    item(key = key, contentType = "SwitchPreference") {
        val state = rememberPreferenceState(key, defaultValue)
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(enabled = enabled()) { state.value = !state.value }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                title()
                if (summary != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    summary(state.value)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = state.value, onCheckedChange = { state.value = it }, enabled = enabled())
        }
    }
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
    item(key = key, contentType = "Preference") {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                title()
                if (summary != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    summary()
                }
            }
        }
    }
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
    item(key = key, contentType = "SliderPreference") {
        val state = rememberPreferenceState(key, defaultValue)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            title()
            if (summary != null) {
                Spacer(modifier = Modifier.height(4.dp))
                summary(state.value)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = state.value,
                    onValueChange = { state.value = it },
                    valueRange = valueRange.start..valueRange.endInclusive,
                    steps = valueSteps,
                    enabled = enabled(),
                    modifier = Modifier.weight(1f),
                )
                if (valueText != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    valueText(state.value)
                }
            }
        }
    }
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
    item(key = key, contentType = "ListPreference") {
        val state = rememberPreferenceState(key, defaultValue)
        var expanded by remember { mutableStateOf(false) }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                title()
                if (summary != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    summary(state.value)
                }
            }
            Box {
                Text(
                    text = valueToText(state.value),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    values.forEach { v ->
                        DropdownMenuItem(
                            text = { Text(valueToText(v)) },
                            onClick = {
                                state.value = v
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
actual fun Preference(
    modifier: Modifier,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)?,
    icon: @Composable (() -> Unit)?,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            title()
            if (summary != null) {
                Spacer(modifier = Modifier.height(4.dp))
                summary()
            }
        }
    }
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        title()
        if (summary != null) {
            Spacer(modifier = Modifier.height(4.dp))
            summary()
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = sliderValue,
                onValueChange = onSliderValueChange,
                valueRange = valueRange.start..valueRange.endInclusive,
                steps = valueSteps,
                enabled = enabled,
                modifier = Modifier.weight(1f),
            )
            if (valueText != null) {
                Spacer(modifier = Modifier.width(8.dp))
                valueText(sliderValue)
            }
        }
    }
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        title()
        if (summary != null) {
            Spacer(modifier = Modifier.height(4.dp))
            summary()
        }
        // Simple display of selected values
        Text(
            text = value.joinToString { valueToText(it).toString() },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
