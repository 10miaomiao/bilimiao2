@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeMute
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.SubtitlesOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.Container
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.keepLayoutWhenHidden
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.PlayerControllerState
import cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.PlaybackSpeedControllerState
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.SubtitleSourceInfo
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
import kotlin.math.roundToInt

const val TAG_SELECT_EPISODE_ICON_BUTTON = "SelectEpisodeIconButton"
const val TAG_SPEED_SWITCHER_TEXT_BUTTON = "SpeedSwitcherTextButton"
const val TAG_SPEED_SWITCHER_DROPDOWN_MENU = "SpeedSwitcherDropdownMenu"
const val TAG_SPEED_SWITCHER_SLIDER = "SpeedSwitcherSlider"
const val TAG_SPEED_SWITCHER_VALUE_INDICATOR = "SpeedSwitcherValueIndicator"
const val TAG_DANMAKU_ICON_BUTTON = "DanmakuIconButton"
const val TAG_VIDEO_ASPECT_RATIO_SELECTOR_TEXT_BUTTON = "VideoAspectRatioTextButton"
const val TAG_VIDEO_ASPECT_RATIO_SELECTOR_DROPDOWN_MENU = "VideoAspectRatioDropdownMenu"
const val TAG_QUALITY_SELECTOR_TEXT_BUTTON = "QualitySelectorTextButton"
const val TAG_QUALITY_SELECTOR_DROPDOWN_MENU = "QualitySelectorDropdownMenu"
const val TAG_SUBTITLE_SWITCHER_TEXT_BUTTON = "SubtitleSwitcherTextButton"
const val TAG_SUBTITLE_SWITCHER_DROPDOWN_MENU = "SubtitleSwitcherDropdownMenu"

/**
 * 视频宽高比控制器的简化状态.
 *
 * 迁移自 animeko 的 VideoAspectRatioControllerState, 移除了对 mediamp 的依赖.
 *
 * @param currentMode 当前宽高比模式 (0=适应, 1=拉伸, 2=裁剪)
 * @param onModeChange 模式变更回调
 */
@Stable
class VideoAspectRatioControllerState(
    val currentMode: Int = 0,
    val onModeChange: (Int) -> Unit = {},
) {
    fun setMode(mode: Int) = onModeChange(mode)

    companion object {
        val Entries: List<Int> = listOf(0, 1, 2)
    }
}

/**
 * 将宽高比模式渲染为中文字符串.
 * 替换自 animeko 的 renderAspectRatioMode.
 */
fun renderAspectRatioMode(mode: Int): String = when (mode) {
    0 -> "适应"
    1 -> "拉伸"
    2 -> "裁剪"
    else -> "适应"
}

/**
 * 格式化倍速值为字符串.
 * 替换自 animeko 的 formatSpeedValue, 简化为 "${speed}x".
 */
fun formatSpeedValue(speed: Float): String = "${speed}x"

@Stable
object PlayerControllerDefaults {
    /**
     * 播放 / 暂停按钮
     */
    @Composable
    fun PlaybackIcon(
        isPlaying: () -> Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier,
        ) {
            if (isPlaying()) {
                Icon(Icons.Rounded.Pause, contentDescription = "暂停", Modifier.size(36.dp))
            } else {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "播放", Modifier.size(36.dp))
            }
        }
    }

    /**
     * 弹幕开关按钮
     */
    @Composable
    fun DanmakuIcon(
        danmakuEnabled: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier.testTag(TAG_DANMAKU_ICON_BUTTON),
        ) {
            if (danmakuEnabled) {
                Icon(Icons.Rounded.Subtitles, contentDescription = "关闭弹幕")
            } else {
                Icon(Icons.Rounded.SubtitlesOff, contentDescription = "开启弹幕")
            }
        }
    }

    /**
     * 音量按钮, 悬停时显示垂直音量滑块.
     */
    @Composable
    fun AudioIcon(
        volume: Float,
        isMute: Boolean,
        maxValue: Float,
        onClick: () -> Unit,
        onchange: (Float) -> Unit,
        controllerState: PlayerControllerState,
        modifier: Modifier = Modifier,
    ) {
        val hoverInteraction = remember { MutableInteractionSource() }
        val isHovered by hoverInteraction.collectIsHoveredAsState()
        val audioIconRequester = remember { Any() }

        LaunchedEffect(true) {
            snapshotFlow { isHovered }.collect {
                controllerState.setRequestAlwaysOn(audioIconRequester, isHovered)
            }
        }
        Box(
            modifier = modifier.hoverable(hoverInteraction),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val iconButton = @Composable {
                IconButton(
                    onClick = onClick,
                ) {
                    when {
                        isMute -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeOff,
                                contentDescription = "静音",
                            )
                        }

                        volume < 0.33f -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeMute,
                                contentDescription = "音量",
                            )
                        }

                        volume < 0.66f -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeDown,
                                contentDescription = "音量",
                            )
                        }

                        else -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "音量",
                            )
                        }
                    }
                }
            }

            iconButton()

            Popup(
                alignment = Alignment.BottomCenter,
            ) {
                Surface(
                    modifier = Modifier
                        .hoverable(hoverInteraction)
                        .clip(shape = CircleShape),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedVisibility(
                            visible = isHovered && !isMute,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = volume.times(100).roundToInt().toString(),
                                    modifier = Modifier.padding(8.dp),
                                )
                                val colors = SliderDefaults.colors(
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface,
                                )
                                VerticalSlider(
                                    value = volume,
                                    onValueChange = onchange,
                                    modifier = Modifier.width(96.dp),
                                    thumb = {},
                                    colors = colors,
                                    track = { sliderState ->
                                        SliderDefaults.Track(
                                            colors = colors,
                                            enabled = true,
                                            sliderState = sliderState,
                                            thumbTrackGapSize = 0.dp,
                                        )
                                    },
                                    valueRange = 0f..maxValue,
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isHovered && !isMute,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            iconButton()
                        }
                    }
                }
            }
        }
    }

    /**
     * 下一集按钮
     */
    @Composable
    fun NextEpisodeIcon(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick,
            modifier,
        ) {
            Icon(Icons.Rounded.SkipNext, "下一集", Modifier.size(36.dp))
        }
    }

    /**
     * 选集按钮
     */
    @Composable
    fun SelectEpisodeIcon(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        TextButton(
            onClick,
            modifier.testTag(TAG_SELECT_EPISODE_ICON_BUTTON),
            colors = ButtonDefaults.textButtonColors(
                contentColor = LocalContentColor.current,
            ),
        ) {
            Text("选集")
        }
    }

    /**
     * 弹幕输入框占位文本列表.
     */
    private val danmakuPlaceholders: List<String> = listOf(
        "弹幕护体",
        "前方高能",
        "哈哈哈哈",
        "泪目了",
        "好家伙",
        "名场面",
        "草(中日双语)",
        "快进到...",
        "此处应有弹幕",
        "悠悠球",
        "awsl",
        "爷青回",
        "好活当赏",
        "妙啊",
        "太强了",
        "学到了",
        "下次一定",
        "催更催更",
        "沙发",
        "第一",
    )

    fun randomDanmakuPlaceholder(placeholders: List<String> = danmakuPlaceholders): String = placeholders.random()

    @Composable
    fun rememberRandomDanmakuPlaceholder(): String {
        return remember { randomDanmakuPlaceholder() }
    }

    /**
     * 发送弹幕按钮
     */
    @Composable
    fun DanmakuSendButton(
        onClick: () -> Unit,
        enabled: Boolean = true,
        modifier: Modifier = Modifier,
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = modifier) {
            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "发送")
        }
    }

    /**
     * 弹幕发送入口（样式参考 animeko 的 DummyDanmakuEditor）
     *
     * 圆角描边胶囊样式，点击打开发送弹幕页面。
     * 使用白色系配色，适配黑色视频背景。
     */
    @Composable
    fun DanmakuSendEntry(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val shape = MaterialTheme.shapes.medium
        Row(
            Modifier
                .widthIn(max = 160.dp)
                .fillMaxWidth()
                .height(36.dp)
                .clip(shape)
                .clickable(onClick = onClick)
                .border(1.dp, Color.White.copy(alpha = 0.5f), shape)
                .padding(horizontal = 12.dp)
                .then(modifier),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides Color.White.copy(alpha = 0.85f),
            ) {
                Text(
                    "发送弹幕",
                    style = MaterialTheme.typography.labelLarge,
                )
                Icon(Icons.AutoMirrored.Rounded.Send, null)
            }
        }
    }

    /**
     * 视频内弹幕输入框的颜色配置.
     * slightlyWeaken -> alpha 0.7f, stronglyWeaken -> alpha 0.3f
     */
    @Composable
    fun inVideoDanmakuTextFieldColors(): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
        )
    }

    @Composable
    fun inTabDanmakuTextFieldColors(): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
        )
    }

    /**
     * 弹幕编辑输入框, 通过 [trailingIcon] 发送.
     */
    @Composable
    fun DanmakuTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        onSend: () -> Unit = {},
        isSending: () -> Boolean = { false },
        interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
        placeholder: @Composable () -> Unit = {
            Text(
                rememberRandomDanmakuPlaceholder(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = {
            if (isSending()) {
                CircularProgressIndicator(
                    Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                DanmakuSendButton(
                    onClick = { onSend() },
                    enabled = value.isNotBlank(),
                )
            }
        },
        enabled: Boolean = true,
        singleLine: Boolean = true,
        isError: Boolean = false,
        shape: Shape = MaterialTheme.shapes.medium,
        style: TextStyle = MaterialTheme.typography.bodyMedium,
        colors: TextFieldColors = inVideoDanmakuTextFieldColors()
    ) {
        BasicTextField(
            value,
            onValueChange,
            // 替换自 animeko 的 onKey(Key.Enter), 使用标准 onPreviewKeyEvent
            modifier.onPreviewKeyEvent {
                if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                    onSend()
                    true
                } else {
                    false
                }
            }.height(38.dp),
            textStyle = style.copy(color = colors.unfocusedTextColor),
            cursorBrush = SolidColor(rememberUpdatedState(if (isError) colors.errorCursorColor else colors.cursorColor).value),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value,
                    innerTextField,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(vertical = 7.dp, horizontal = 16.dp),
                    colors = colors,
                    placeholder = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.weight(1f)) {
                                placeholder()
                            }
                        }
                    },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    container = {
                        Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = shape,
                        )
                    },
                )
            },
        )
    }

    /**
     * 全屏切换按钮
     */
    @Composable
    fun FullscreenIcon(
        isFullscreen: Boolean,
        onClickFullscreen: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        // needWorkaroundForFocusManager 简化为 false, 移除焦点 workaround
        IconButton(
            onClick = onClickFullscreen,
            modifier,
        ) {
            if (isFullscreen) {
                Icon(Icons.Rounded.FullscreenExit, contentDescription = "退出全屏", Modifier.size(32.dp))
            } else {
                Icon(Icons.Rounded.Fullscreen, contentDescription = "全屏", Modifier.size(32.dp))
            }
        }
    }

    /**
     * 当前倍速入口与 Slider 弹层.
     *
     * 入口始终显示规范化后的当前值; 弹层只有一条水平 Slider,
     * 拖动期间实时预览, 松手后提交最终值.
     */
    @Composable
    fun SpeedSwitcher(
        state: PlaybackSpeedControllerState,
        modifier: Modifier = Modifier,
        onExpandedChanged: (expanded: Boolean) -> Unit = {},
    ) {
        SpeedSwitcher(
            currentSpeed = state.currentSpeed,
            speedRange = state.speedRange,
            onPreviewSpeed = state::previewSpeed,
            onCommitSpeed = state::commitSpeed,
            modifier = modifier,
            onExpandedChanged = onExpandedChanged,
        )
    }

    @Composable
    fun SpeedSwitcher(
        currentSpeed: Float,
        speedRange: ClosedFloatingPointRange<Float>,
        onPreviewSpeed: (Float) -> Unit,
        onCommitSpeed: (Float) -> Unit,
        modifier: Modifier = Modifier,
        onExpandedChanged: (expanded: Boolean) -> Unit = {},
    ) {
        var expanded by rememberSaveable { mutableStateOf(false) }
        fun setExpanded(value: Boolean) {
            expanded = value
            onExpandedChanged(value)
        }

        Box(modifier, contentAlignment = Alignment.Center) {
            SpeedSwitcherButton(
                speed = currentSpeed,
                onClick = { setExpanded(true) },
            )

            if (expanded) {
                SpeedSliderPopup(
                    currentSpeed,
                    speedRange,
                    onPreviewSpeed,
                    onCommitSpeed,
                    onDismissRequest = { setExpanded(false) },
                )
            }
        }
    }

    @Composable
    private fun SpeedSwitcherButton(
        speed: Float,
        onClick: () -> Unit,
    ) {
        TextButton(
            onClick,
            colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current),
            modifier = Modifier.testTag(TAG_SPEED_SWITCHER_TEXT_BUTTON),
        ) {
            // formatSpeedValue 简化为 "${speed}x"
            Text(remember(speed) { if (speed == 1.0f) "倍速" else formatSpeedValue(speed) })
        }
    }

    @Composable
    private fun SpeedSliderPopup(
        currentSpeed: Float,
        speedRange: ClosedFloatingPointRange<Float>,
        onPreviewSpeed: (Float) -> Unit,
        onCommitSpeed: (Float) -> Unit,
        onDismissRequest: () -> Unit,
    ) {
        // 居中显示的 Popup, 替代 animeko 的 TooltipDefaults 定位
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = onDismissRequest,
            // PlatformPopupProperties -> PopupProperties
            properties = PopupProperties(focusable = true, clippingEnabled = false),
        ) {
            // AniTheme(darkModeOverride = DarkMode.DARK) -> MaterialTheme
            Surface(
                modifier = Modifier
                    .testTag(TAG_SPEED_SWITCHER_DROPDOWN_MENU)
                    .width(280.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 8.dp,
            ) {
                // SteppedSlider -> 普通 Slider
                var sliderValue by remember { mutableStateOf(currentSpeed) }
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        formatSpeedValue(sliderValue),
                        Modifier.testTag(TAG_SPEED_SWITCHER_VALUE_INDICATOR),
                        maxLines = 1,
                        softWrap = false,
                    )
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            onPreviewSpeed(it)
                        },
                        onValueChangeFinished = {
                            onCommitSpeed(sliderValue)
                        },
                        valueRange = speedRange,
                        modifier = Modifier.testTag(TAG_SPEED_SWITCHER_SLIDER),
                    )
                }
            }
        }
    }

    /**
     * 倍速菜单（对照原安卓版 SpeedPopupMenu）
     *
     * 入口按钮显示当前倍速，点击弹出预设倍速值菜单（设置中的 PlayerSpeedValues）。
     */
    @Composable
    fun SpeedSelector(
        currentSpeed: Float,
        options: List<Float>,
        onValueChange: (Float) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        if (options.isEmpty()) return
        OptionsSwitcher(
            value = currentSpeed,
            onValueChange = onValueChange,
            optionsProvider = { options },
            renderValue = { Text(formatSpeedValue(it)) },
            renderValueExposed = {
                Text(
                    if (currentSpeed == 1.0f) "倍速" else formatSpeedValue(currentSpeed),
                    maxLines = 1,
                )
            },
            modifier,
        )
    }

    /**
     * CC 字幕选择（参照 animeko 的 SubtitleSwitcher）
     *
     * 菜单第一项为"关闭"，后续为可用字幕列表；无可用字幕时不显示。
     */
    @Composable
    fun SubtitleSwitcher(
        currentSubtitle: SubtitleSourceInfo?,
        options: List<SubtitleSourceInfo?>,
        onValueChange: (SubtitleSourceInfo?) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        if (options.size <= 1) return // 只有 null（关闭）一项时无需显示
        // 自实现（与 QualitySwitcher 同构），避免 OptionsSwitcher 的 Popup 行为差异
        var expanded by rememberSaveable { mutableStateOf(false) }
        Box(modifier, contentAlignment = Alignment.Center) {
            TextButton(
                onClick = { expanded = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = LocalContentColor.current,
                ),
                modifier = Modifier.testTag(TAG_SUBTITLE_SWITCHER_TEXT_BUTTON),
            ) {
                val label = currentSubtitle?.lan_doc ?: "字幕"
                Text(
                    label,
                    Modifier.widthIn(max = 64.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.testTag(TAG_SUBTITLE_SWITCHER_DROPDOWN_MENU),
            ) {
                options.forEach { option ->
                    val selected = option == currentSubtitle
                    DropdownMenuItem(
                        text = {
                            val color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                LocalContentColor.current
                            }
                            CompositionLocalProvider(LocalContentColor provides color) {
                                if (option == null) {
                                    Text("关闭")
                                } else {
                                    Text(
                                        option.lan_doc,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        },
                        onClick = {
                            expanded = false
                            onValueChange(option)
                        },
                    )
                }
            }
        }
    }

    /**
     * 清晰度切换（对照原安卓版 QualityPopupMenu）
     *
     * 点击弹出清晰度菜单，当前清晰度带选中标记；
     * 需要登录/大会员的清晰度置灰禁用（由 [isOptionEnabled] 控制）。
     */
    @Composable
    fun QualitySwitcher(
        currentQuality: Int,
        options: List<PlayerSourceInfo.AcceptInfo>,
        onValueChange: (Int) -> Unit,
        isOptionEnabled: (PlayerSourceInfo.AcceptInfo) -> Boolean = { true },
        modifier: Modifier = Modifier,
    ) {
        if (options.isEmpty()) return
        var expanded by rememberSaveable { mutableStateOf(false) }
        Box(modifier, contentAlignment = Alignment.Center) {
            val currentDescription = options.find { it.quality == currentQuality }?.description
                ?: options.lastOrNull()?.description
                ?: ""
            TextButton(
                onClick = { expanded = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = LocalContentColor.current,
                ),
                modifier = Modifier.testTag(TAG_QUALITY_SELECTOR_TEXT_BUTTON),
            ) {
                Text(
                    currentDescription,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.testTag(TAG_QUALITY_SELECTOR_DROPDOWN_MENU),
            ) {
                options.forEach { option ->
                    val selected = option.quality == currentQuality
                    val enabled = isOptionEnabled(option)
                    DropdownMenuItem(
                        text = {
                            val color = when {
                                selected -> MaterialTheme.colorScheme.primary
                                !enabled -> LocalContentColor.current.copy(alpha = 0.38f)
                                else -> LocalContentColor.current
                            }
                            CompositionLocalProvider(LocalContentColor provides color) {
                                Text(option.description)
                            }
                        },
                        leadingIcon = when {
                            selected -> {
                                {
                                    Icon(
                                        Icons.Rounded.Check,
                                        "已选择",
                                        Modifier.size(18.dp),
                                    )
                                }
                            }

                            !enabled -> {
                                {
                                    Icon(
                                        Icons.Rounded.Lock,
                                        "不可用",
                                        Modifier.size(18.dp),
                                    )
                                }
                            }

                            else -> null
                        },
                        onClick = {
                            if (enabled) {
                                expanded = false
                                onValueChange(option.quality)
                            }
                        },
                        enabled = enabled,
                    )
                }
            }
        }
    }

    /**
     * 视频宽高比选择器
     */
    @Composable
    fun VideoAspectRatioSelector(
        videoAspectRatioControllerState: VideoAspectRatioControllerState,
        modifier: Modifier = Modifier,
        onExpandedChanged: (expanded: Boolean) -> Unit = {},
    ) {
        return OptionsSwitcher(
            value = videoAspectRatioControllerState.currentMode,
            onValueChange = { videoAspectRatioControllerState.setMode(it) },
            optionsProvider = { VideoAspectRatioControllerState.Entries },
            renderValue = { Text(renderAspectRatioMode(it)) },
            renderValueExposed = { Text(renderAspectRatioMode(it)) },
            modifier,
            // PlatformPopupProperties -> PopupProperties
            properties = PopupProperties(
                focusable = true,
                clippingEnabled = false,
            ),
            textButtonTestTag = TAG_VIDEO_ASPECT_RATIO_SELECTOR_TEXT_BUTTON,
            dropdownMenuTestTag = TAG_VIDEO_ASPECT_RATIO_SELECTOR_DROPDOWN_MENU,
            onExpandedChanged = onExpandedChanged,
        )
    }

    /**
     * @param optionsProvider 可选项列表. 注意值变化时不会反映到 UI.
     */
    @Composable
    fun <T> OptionsSwitcher(
        value: T,
        onValueChange: (T) -> Unit,
        optionsProvider: () -> List<T>,
        renderValue: @Composable (T) -> Unit,
        renderValueExposed: @Composable (T) -> Unit = renderValue,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        // 与 material3 DropdownMenu 默认一致（focusable = true），避免覆盖后菜单无法弹出
        properties: PopupProperties = PopupProperties(focusable = true),
        textButtonTestTag: String = "textButton",
        dropdownMenuTestTag: String = "dropDownMenu",
        onExpandedChanged: (expanded: Boolean) -> Unit = {},
    ) {
        Box(modifier, contentAlignment = Alignment.Center) {
            var expanded by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(true) {
                snapshotFlow { expanded }.collect {
                    onExpandedChanged(expanded)
                }
            }
            TextButton(
                { expanded = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = LocalContentColor.current,
                ),
                enabled = enabled,
                modifier = Modifier.testTag(textButtonTestTag),
            ) {
                renderValueExposed(value)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = properties,
                modifier = Modifier.testTag(dropdownMenuTestTag),
            ) {
                val options = remember(optionsProvider) { optionsProvider() }
                for (option in options) {
                    DropdownMenuItem(
                        text = {
                            val color = if (value == option) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                LocalContentColor.current
                            }
                            CompositionLocalProvider(LocalContentColor provides color) {
                                renderValue(option)
                            }
                        },
                        onClick = {
                            expanded = false
                            onValueChange(option)
                        },
                    )
                }
            }
        }
    }

    /**
     * 媒体进度滑块.
     *
     * 迁移说明: animeko 版本引用了 PlayerProgressSliderState, MediaCacheProgressInfo,
     * MediaProgressFramePreviewState, TouchSeekState 等类型.
     * 这些类型尚未迁移, 此处保留参数签名, 实际渲染由集成层通过 progressSlider lambda 填充.
     *
     * @param progressSliderState 占位, 由集成层提供
     * @param enabled 是否启用
     */
    @Composable
    fun MediaProgressSlider(
        progressSliderState: PlayerProgressSliderState,
        cacheProgressInfoFlow: Flow<MediaCacheProgressInfo?> = flowOf(null),
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        showPreviewTimeTextOnThumb: Boolean = true,
        framePreview: MediaProgressFramePreviewState? = null,
        showFramePreviewInPopup: Boolean = true,
        touchSeekState: TouchSeekState? = null,
    ) {
        val cacheProgressInfo by cacheProgressInfoFlow.collectAsState(initial = null)
        cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress.MediaProgressSlider(
            state = progressSliderState,
            cacheProgressInfoFlow = { cacheProgressInfo },
            enabled = enabled,
            showPreviewTimeTextOnThumb = showPreviewTimeTextOnThumb,
            framePreview = framePreview,
            showFramePreviewInPopup = showFramePreviewInPopup,
            touchSeekState = touchSeekState,
            modifier = modifier,
        )
    }

    /**
     * 左下角提示 (跳过 OP/ED).
     */
    @Composable
    fun LeftBottomTips(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        // AniTheme(darkModeOverride = DarkMode.DARK) -> MaterialTheme
        Surface(
            modifier = modifier,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("跳过 OP/ED")
                    TextButton(onClick = onClick) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

/**
 * 视频播放器底部控制栏.
 *
 * 参见 [PlayerControllerDefaults] 中的组件.
 *
 * @param startActions 左侧操作区 (如 PlaybackIcon, DanmakuIcon)
 * @param progressIndicator 进度指示文本
 * @param progressSlider 进度滑块
 * @param danmakuEditor 弹幕编辑器
 * @param endActions 右侧操作区 (如 FullscreenIcon)
 * @param expanded 控制栏是否展开.
 * 为 `true` 时, [progressIndicator] 和 [progressSlider] 显示在上方独立行, 底部行包含 [danmakuEditor].
 * 为 `false` 时, 整个控制栏只有一行, [danmakuEditor] 被忽略.
 * @param sliderOnly 是否仅保留 [progressSlider] 可见而不替换其组合.
 */
@Composable
fun PlayerControllerBar(
    startActions: @Composable RowScope.() -> Unit,
    progressIndicator: @Composable RowScope.() -> Unit,
    progressSlider: @Composable RowScope.() -> Unit,
    danmakuEditor: @Composable RowScope.() -> Unit,
    endActions: @Composable RowScope.() -> Unit,
    expanded: Boolean,
    sliderOnly: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clickable(remember { MutableInteractionSource() }, null, onClick = {}) // 消费触摸事件
            .padding(
                horizontal = if (expanded) 8.dp else 4.dp,
                vertical = if (expanded) 4.dp else 2.dp,
            ),
    ) {
        Column {
            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                Row(
                    Modifier
                        .keepLayoutWhenHidden(sliderOnly)
                        .padding(start = if (expanded) 8.dp else 4.dp)
                        .padding(vertical = if (expanded) 4.dp else 2.dp),
                ) {
                    progressIndicator()
                }
                if (expanded) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        progressSlider()
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (expanded) 8.dp else 4.dp),
        ) {
            // 播放 / 暂停按钮
            Row(
                Modifier.keepLayoutWhenHidden(sliderOnly),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                startActions()
            }

            Row(
                Modifier.weight(1f).keepLayoutWhenHidden(sliderOnly && expanded),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (expanded) {
                    ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                        danmakuEditor()
                    }
                } else {
                    progressSlider()
                }
            }

            Row(
                Modifier.keepLayoutWhenHidden(sliderOnly),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                endActions()
            }
        }
    }
}
