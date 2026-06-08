package app.bilimiao.desktop.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowState
import app.bilimiao.desktop.DesktopWindowState

@Composable
fun FrameWindowScope.WindowFrame(
    desktopWindow: DesktopWindowState,
    windowState: WindowState,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val os = System.getProperty("os.name").lowercase()
    when {
        os.contains("win") -> {
            WindowsWindowFrame(
                desktopWindow = desktopWindow,
                windowState = windowState,
                onCloseRequest = onCloseRequest,
                content = content,
            )
        }
        else -> {
            content()
        }
    }
}
