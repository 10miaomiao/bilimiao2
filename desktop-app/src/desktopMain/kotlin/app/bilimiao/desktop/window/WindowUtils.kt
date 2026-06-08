package app.bilimiao.desktop.window

import app.bilimiao.desktop.DesktopWindowState
import androidx.compose.ui.window.WindowState

interface WindowUtils {
    suspend fun setUndecoratedFullscreen(
        windowState: DesktopWindowState,
        windowState_: WindowState,
        undecorated: Boolean
    ) {}

    companion object {
        val instance: WindowUtils by lazy {
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("win") -> WindowsWindowUtils.instance
                else -> object : WindowUtils {
                    override suspend fun setUndecoratedFullscreen(
                        windowState: DesktopWindowState,
                        windowState_: WindowState,
                        undecorated: Boolean
                    ) {
                        windowState_.placement = if (undecorated) {
                            androidx.compose.ui.window.WindowPlacement.Fullscreen
                        } else {
                            androidx.compose.ui.window.WindowPlacement.Floating
                        }
                    }
                }
            }
        }
    }
}
