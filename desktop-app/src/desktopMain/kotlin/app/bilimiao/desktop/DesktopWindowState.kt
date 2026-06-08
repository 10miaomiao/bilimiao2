package app.bilimiao.desktop

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.WindowState
import app.bilimiao.desktop.window.LayoutHitTestOwner
import app.bilimiao.desktop.window.WindowsWindowUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

val LocalDesktopWindow = staticCompositionLocalOf<DesktopWindowState> {
    error("No DesktopWindowState provided")
}

class DesktopWindowState(
    val windowHandle: Long,
    val windowState: WindowState,
) {
    internal var savedWindowsWindowState: SavedWindowsWindowState? = null

    internal val windowsWindowProc = MutableStateFlow<app.bilimiao.desktop.window.BasicWindowProc?>(null)

    var layoutHitTestOwner: LayoutHitTestOwner? = null

    private var _isUndecoratedFullscreen by mutableStateOf(false)

    val isUndecoratedFullscreen: Boolean
        get() = _isUndecoratedFullscreen

    val isExactlyMaximized: Boolean
        get() = windowState.placement == androidx.compose.ui.window.WindowPlacement.Maximized

    internal fun onUndecoratedFullscreenStateChange(newState: Boolean) {
        _isUndecoratedFullscreen = newState
    }
}

class SavedWindowsWindowState(
    val style: Int,
    val exStyle: Int,
    val rect: androidx.compose.ui.geometry.Rect,
    val maximized: Boolean,
)
