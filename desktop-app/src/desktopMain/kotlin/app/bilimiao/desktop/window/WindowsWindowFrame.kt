package app.bilimiao.desktop.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.FontLoadResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.zIndex
import app.bilimiao.desktop.DesktopWindowState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

val LocalTitleBarInsets = staticCompositionLocalOf { WindowInsets(0) }
val LocalCaptionButtonInsets = staticCompositionLocalOf { WindowInsets(0) }
val ZeroInsets = WindowInsets(0)

@OptIn(InternalComposeUiApi::class)
@Composable
fun FrameWindowScope.WindowsWindowFrame(
    desktopWindow: DesktopWindowState,
    windowState: WindowState,
    frameState: WindowsWindowFrameState = rememberWindowsWindowFrameState(desktopWindow),
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val windowUtils = WindowsWindowUtils.instance
    val scope = rememberCoroutineScope()

    val topBorderFixedInsets by remember(desktopWindow, windowState) {
        derivedStateOf {
            val isFloatingWindow =
                !desktopWindow.isUndecoratedFullscreen && windowState.placement == WindowPlacement.Floating
            if (isFloatingWindow) WindowInsets(top = 1) else ZeroInsets
        }
    }
    Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(topBorderFixedInsets)) {
        LaunchedEffect(desktopWindow.isUndecoratedFullscreen) {
            frameState.isTitleBarVisible = !desktopWindow.isUndecoratedFullscreen
        }

        val density = LocalDensity.current
        val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
        val titleBarHeightPx = frameState.titleBarInsets.getTop(density)
        val captionBarWidthPx = frameState.captionButtonsInsets.getRight(density, layoutDirection)
        val captionBarHeightPx = frameState.captionButtonsInsets.getTop(density)
        val platformWindowInsets = androidx.compose.ui.platform.LocalPlatformWindowInsets.current
        val desktopWindowInsets = remember(platformWindowInsets, titleBarHeightPx, captionBarWidthPx, captionBarHeightPx) {
            DesktopPlatformWindowInsets(platformWindowInsets, titleBarHeightPx, captionBarWidthPx, captionBarHeightPx)
        }
        CompositionLocalProvider(
            LocalTitleBarInsets provides frameState.titleBarInsets,
            LocalCaptionButtonInsets provides frameState.captionButtonsInsets,
            LocalTitleBarThemeController provides frameState.titleBarThemeController,
            androidx.compose.ui.platform.LocalPlatformWindowInsets provides desktopWindowInsets,
        ) {
            content()
        }

        val titleBarInteractionSource = remember(desktopWindow.isUndecoratedFullscreen) { MutableInteractionSource() }
        val titleBarHovered by titleBarInteractionSource.collectIsHoveredAsState()
        LaunchedEffect(titleBarInteractionSource, titleBarHovered, desktopWindow.isUndecoratedFullscreen) {
            if (!titleBarHovered && desktopWindow.isUndecoratedFullscreen) {
                delay(3.seconds)
                frameState.isTitleBarVisible = false
            }
        }

        ExtendToTitleBar(frameState, desktopWindow)

        AnimatedVisibility(
            visible = frameState.isTitleBarVisible,
            modifier = Modifier
                .then(
                    if (frameState.isTitleBarVisible && desktopWindow.isUndecoratedFullscreen)
                        Modifier.hoverable(titleBarInteractionSource) else Modifier
                )
                .fillMaxWidth()
                .onSizeChanged(frameState::updateTitleBarInsets)
                .wrapContentWidth(AbsoluteAlignment.Right),
        ) {
            Row(
                modifier = Modifier.onSizeChanged(frameState::updateCaptionButtonsInset),
            ) {
                CompositionLocalProvider(
                    LocalCaptionIconFamily provides rememberFontIconFamily().value,
                    LocalWindowsColorScheme provides if (frameState.titleBarThemeController.isDark) {
                        WindowsColorScheme.dark()
                    } else {
                        WindowsColorScheme.light()
                    },
                ) {
                    CaptionButtonRow(
                        frameState = frameState,
                        isMaximize = windowState.placement == WindowPlacement.Maximized,
                        onMinimizeRequest = { windowUtils.minimizeWindow(desktopWindow.windowHandle) },
                        onMaximizeRequest = { windowUtils.maximizeWindow(desktopWindow.windowHandle) },
                        onRestoreRequest = { windowUtils.restoreWindow(desktopWindow.windowHandle) },
                        onExitFullscreenRequest = {
                            scope.launch {
                                windowUtils.setUndecoratedFullscreen(desktopWindow, windowState, false)
                            }
                        },
                        onCloseRequest = onCloseRequest,
                        onMaximizeButtonRectUpdate = frameState::updateMaximizeButtonRect,
                        onMinimizeButtonRectUpdate = frameState::updateMinimizeButtonRect,
                        onCloseButtonRectUpdate = frameState::updateCloseButtonRect,
                    )
                }
            }
        }

        if (!frameState.isTitleBarVisible) {
            val awareAreaInteractionSource = remember { MutableInteractionSource() }
            val isAwareHovered by awareAreaInteractionSource.collectIsHoveredAsState()
            LaunchedEffect(isAwareHovered) {
                if (isAwareHovered) {
                    frameState.isTitleBarVisible = true
                }
            }
            Spacer(
                modifier = Modifier.hoverable(awareAreaInteractionSource)
                    .fillMaxWidth()
                    .height(16.dp),
            )
        }
    }
}

@Composable
fun rememberWindowsWindowFrameState(desktopWindow: DesktopWindowState): WindowsWindowFrameState {
    val layoutHitTestOwner = desktopWindow.layoutHitTestOwner
    return remember(desktopWindow, layoutHitTestOwner) { WindowsWindowFrameState(desktopWindow, layoutHitTestOwner) }
}

@OptIn(ExperimentalLayoutApi::class)
class WindowsWindowFrameState(
    internal val desktopWindow: DesktopWindowState,
    private val layoutHitTestOwner: LayoutHitTestOwner?,
) {
    val titleBarThemeController = TitleBarThemeController()

    var isTitleBarVisible by mutableStateOf(true)

    private val captionButtonsRect = Array(3) { Rect.Zero }

    private val _titleBarInsets = MutableWindowInsets()
    private val _captionButtonsInsets = MutableWindowInsets()

    val titleBarInsets: WindowInsets
        get() = if (isTitleBarVisible) _titleBarInsets else ZeroInsets

    val captionButtonsInsets: WindowInsets
        get() = if (isTitleBarVisible) _captionButtonsInsets else ZeroInsets

    fun updateMinimizeButtonRect(rect: Rect) { captionButtonsRect[0] = rect }
    fun updateMaximizeButtonRect(rect: Rect) { captionButtonsRect[1] = rect }
    fun updateCloseButtonRect(rect: Rect) { captionButtonsRect[2] = rect }

    fun updateCaptionButtonsInset(size: IntSize) {
        _captionButtonsInsets.insets = WindowInsets(right = size.width, top = size.height)
    }

    fun updateTitleBarInsets(size: IntSize) {
        _titleBarInsets.insets = WindowInsets(top = size.height)
    }

    fun hitTest(x: Float, y: Float, density: Density) = when {
        captionButtonsRect[0].contains(x, y) -> WindowsWindowHitResult.CAPTION_MIN
        captionButtonsRect[1].contains(x, y) -> WindowsWindowHitResult.CAPTION_MAX
        captionButtonsRect[2].contains(x, y) -> WindowsWindowHitResult.CAPTION_CLOSE
        y <= titleBarInsets.getTop(density) && layoutHitTestOwner?.hitTest(x, y) != true -> WindowsWindowHitResult.CAPTION
        else -> WindowsWindowHitResult.CLIENT
    }

    @Composable
    fun collectWindowIsActive(): androidx.compose.runtime.State<Boolean> {
        return remember(desktopWindow) {
            WindowsWindowUtils.instance.windowIsActive(desktopWindow).map { it != false }
        }.collectAsState(false)
    }
}

@Composable
private fun ExtendToTitleBar(frameState: WindowsWindowFrameState, desktopWindow: DesktopWindowState) {
    val density = LocalDensity.current
    LaunchedEffect(desktopWindow, density, frameState) {
        WindowsWindowUtils.instance.collectWindowProcHitTestProvider(desktopWindow) { x, y ->
            frameState.hitTest(x, y, density)
        }
    }
}

@Composable
private fun WindowsWindowFrameState.collectCaptionButtonColors(): CaptionButtonColors {
    val isAccentColorFrameEnabled = remember(desktopWindow) {
        WindowsWindowUtils.instance.frameIsColorful(desktopWindow)
    }.collectAsState(false)
    return if (isAccentColorFrameEnabled.value) {
        CaptionButtonDefaults.accentColors(seedColor = Color(0xFF0078D4))
    } else {
        CaptionButtonDefaults.defaultColors()
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun rememberFontIconFamily(): androidx.compose.runtime.State<FontFamily?> {
    val fontIconFamily = remember { mutableStateOf<FontFamily?>(null) }
    val fontFamilyResolver = LocalDensity.current.let { LocalFontFamilyResolver.current }
    LaunchedEffect(fontFamilyResolver) {
        fontIconFamily.value = sequenceOf("Segoe Fluent Icons", "Segoe MDL2 Assets")
            .mapNotNull {
                val fontFamily = FontFamily(it)
                runCatching {
                    val result = fontFamilyResolver.resolve(fontFamily).value as FontLoadResult
                    if (result.typeface == null || result.typeface?.familyName != it) null
                    else fontFamily
                }.getOrNull()
            }.firstOrNull()
    }
    return fontIconFamily
}

@Composable
private fun CaptionButtonRow(
    frameState: WindowsWindowFrameState,
    isMaximize: Boolean,
    onMinimizeRequest: () -> Unit,
    onMaximizeRequest: () -> Unit,
    onRestoreRequest: () -> Unit,
    onExitFullscreenRequest: () -> Unit,
    onCloseRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onMinimizeButtonRectUpdate: (Rect) -> Unit,
    onMaximizeButtonRectUpdate: (Rect) -> Unit,
    onCloseButtonRectUpdate: (Rect) -> Unit,
) {
    val captionButtonColors = frameState.collectCaptionButtonColors()
    val isActive by frameState.collectWindowIsActive()
    Row(
        horizontalArrangement = Arrangement.aligned(AbsoluteAlignment.Right),
        modifier = modifier.zIndex(1f),
    ) {
        CaptionButton(
            onClick = onMinimizeRequest,
            icon = CaptionButtonIcon.Minimize,
            isActive = isActive,
            colors = captionButtonColors,
            modifier = Modifier.onGloballyPositioned { onMinimizeButtonRectUpdate(it.boundsInWindow()) },
        )
        val isFullscreen = frameState.desktopWindow.isUndecoratedFullscreen
        CaptionButton(
            onClick = when {
                isFullscreen -> onExitFullscreenRequest
                isMaximize -> onRestoreRequest
                else -> onMaximizeRequest
            },
            icon = when {
                isFullscreen -> CaptionButtonIcon.BackToWindow
                isMaximize -> CaptionButtonIcon.Restore
                else -> CaptionButtonIcon.Maximize
            },
            isActive = isActive,
            colors = captionButtonColors,
            modifier = Modifier.onGloballyPositioned { onMaximizeButtonRectUpdate(it.boundsInWindow()) },
        )
        CaptionButton(
            icon = CaptionButtonIcon.Close,
            onClick = onCloseRequest,
            isActive = isActive,
            colors = CaptionButtonDefaults.closeColors(),
            modifier = Modifier.onGloballyPositioned { onCloseButtonRectUpdate(it.boundsInWindow()) },
        )
    }
}

@Composable
private fun CaptionButton(
    onClick: () -> Unit,
    icon: CaptionButtonIcon,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    colors: CaptionButtonColors = CaptionButtonDefaults.defaultColors(),
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isHovered by interaction.collectIsHoveredAsState()
    val isPressed by interaction.collectIsPressedAsState()

    val color = when {
        isPressed -> colors.pressed
        isHovered -> colors.hovered
        else -> colors.default
    }
    Surface(
        color = if (isActive) color.background else color.inactiveBackground,
        contentColor = if (isActive) color.foreground else color.inactiveForeground,
        modifier = modifier
            .size(46.dp, 32.dp)
            .clickable(onClick = onClick, interactionSource = interaction, indication = null),
        shape = RectangleShape,
    ) {
        val fontFamily = LocalCaptionIconFamily.current
        if (fontFamily != null) {
            Text(
                text = icon.glyph.toString(),
                fontFamily = fontFamily,
                textAlign = TextAlign.Center,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
            )
        } else {
            Text(
                text = icon.fallbackText,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
            )
        }
    }
}

private object CaptionButtonDefaults {
    @Composable
    @Stable
    fun defaultColors(
        default: CaptionButtonColor = CaptionButtonColor(
            background = Color.Transparent,
            foreground = LocalWindowsColorScheme.current.textPrimaryColor,
            inactiveBackground = Color.Transparent,
            inactiveForeground = LocalWindowsColorScheme.current.textDisabledColor,
        ),
        hovered: CaptionButtonColor = default.copy(
            background = LocalWindowsColorScheme.current.fillSubtleSecondaryColor,
            inactiveBackground = LocalWindowsColorScheme.current.fillSubtleSecondaryColor,
            inactiveForeground = LocalWindowsColorScheme.current.textPrimaryColor,
        ),
        pressed: CaptionButtonColor = default.copy(
            background = LocalWindowsColorScheme.current.fillSubtleTertiaryColor,
            foreground = LocalWindowsColorScheme.current.textSecondaryColor,
            inactiveBackground = LocalWindowsColorScheme.current.fillSubtleTertiaryColor,
            inactiveForeground = LocalWindowsColorScheme.current.textTertiaryColor,
        ),
        disabled: CaptionButtonColor = default.copy(
            foreground = LocalWindowsColorScheme.current.textDisabledColor,
        ),
    ) = CaptionButtonColors(default = default, hovered = hovered, pressed = pressed, disabled = disabled)

    @Composable
    @Stable
    fun closeColors() = accentColors(seedColor = LocalWindowsColorScheme.current.shellCloseColor)

    @Composable
    @Stable
    fun accentColors(
        seedColor: Color,
        default: CaptionButtonColor = CaptionButtonColor(
            background = LocalWindowsColorScheme.current.fillSubtleTransparentColor,
            foreground = LocalWindowsColorScheme.current.textPrimaryColor,
            inactiveBackground = LocalWindowsColorScheme.current.fillSubtleTransparentColor,
            inactiveForeground = LocalWindowsColorScheme.current.textDisabledColor,
        ),
        hovered: CaptionButtonColor = default.copy(
            background = seedColor, foreground = Color.White,
            inactiveBackground = seedColor, inactiveForeground = Color.White,
        ),
        pressed: CaptionButtonColor = default.copy(
            background = seedColor.copy(0.9f), foreground = Color.White.copy(0.7f),
            inactiveBackground = seedColor.copy(0.9f), inactiveForeground = Color.White.copy(0.7f),
        ),
        disabled: CaptionButtonColor = default.copy(
            foreground = LocalWindowsColorScheme.current.textDisabledColor,
        ),
    ) = CaptionButtonColors(default = default, hovered = hovered, pressed = pressed, disabled = disabled)
}

private data class WindowsColorScheme(
    val textPrimaryColor: Color,
    val textSecondaryColor: Color,
    val textTertiaryColor: Color,
    val textDisabledColor: Color,
    val fillSubtleTransparentColor: Color,
    val fillSubtleSecondaryColor: Color,
    val fillSubtleTertiaryColor: Color,
    val fillSubtleDisabledColor: Color,
    val shellCloseColor: Color = Color(0xFFC42B1C),
) {
    companion object {
        fun light() = WindowsColorScheme(
            textPrimaryColor = Color(0xE4000000),
            textSecondaryColor = Color(0x9B000000),
            textTertiaryColor = Color(0x72000000),
            textDisabledColor = Color(0x5C000000),
            fillSubtleTransparentColor = Color.Transparent,
            fillSubtleSecondaryColor = Color(0x09000000),
            fillSubtleTertiaryColor = Color(0x06000000),
            fillSubtleDisabledColor = Color.Transparent,
        )
        fun dark() = WindowsColorScheme(
            textPrimaryColor = Color(0xFFFFFFFF),
            textSecondaryColor = Color(0xC5FFFFFF),
            textTertiaryColor = Color(0x87FFFFFF),
            textDisabledColor = Color(0x5DFFFFFF),
            fillSubtleTransparentColor = Color.Transparent,
            fillSubtleSecondaryColor = Color(0x0FFFFFFF),
            fillSubtleTertiaryColor = Color(0x0AFFFFFF),
            fillSubtleDisabledColor = Color.Transparent,
        )
    }
}

private val LocalWindowsColorScheme = staticCompositionLocalOf { WindowsColorScheme.light() }
private val LocalCaptionIconFamily = staticCompositionLocalOf<FontFamily?> { null }

@Stable
private data class CaptionButtonColors(
    val default: CaptionButtonColor,
    val hovered: CaptionButtonColor,
    val pressed: CaptionButtonColor,
    val disabled: CaptionButtonColor,
)

@Stable
private data class CaptionButtonColor(
    val background: Color,
    val foreground: Color,
    val inactiveBackground: Color,
    val inactiveForeground: Color,
)

private enum class CaptionButtonIcon(
    val glyph: Char,
    val fallbackText: String,
) {
    Minimize(glyph = '', fallbackText = "–"),
    Maximize(glyph = '', fallbackText = "□"),
    Restore(glyph = '', fallbackText = "▣"),
    BackToWindow(glyph = '', fallbackText = "▣"),
    Close(glyph = '', fallbackText = "✕"),
}

private fun Rect.contains(x: Float, y: Float): Boolean = x >= left && x < right && y >= top && y < bottom

@OptIn(InternalComposeUiApi::class)
private class DesktopPlatformWindowInsets(
    private val delegate: androidx.compose.ui.platform.PlatformWindowInsets,
    private val titleBarHeightPx: Int,
    private val captionBarWidthPx: Int,
    private val captionBarHeightPx: Int,
) : androidx.compose.ui.platform.PlatformWindowInsets by delegate {

    private fun androidx.compose.ui.platform.PlatformInsets.withTitleBar()
        = androidx.compose.ui.platform.PlatformInsets(
            left = left,
            top = top + titleBarHeightPx,
            right = right,
            bottom = bottom,
        )

    override val statusBars: androidx.compose.ui.platform.PlatformInsets
        get() = delegate.statusBars.withTitleBar()
    override val systemBars: androidx.compose.ui.platform.PlatformInsets
        get() = delegate.systemBars.withTitleBar()
    override val captionBar: androidx.compose.ui.platform.PlatformInsets
        get() = androidx.compose.ui.platform.PlatformInsets(
            left = 0,
            top = captionBarHeightPx,
            right = captionBarWidthPx,
            bottom = 0,
        )
}
