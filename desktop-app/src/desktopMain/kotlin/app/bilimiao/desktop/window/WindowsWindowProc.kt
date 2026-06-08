@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package app.bilimiao.desktop.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import app.bilimiao.desktop.DesktopWindowState
import com.sun.jna.CallbackReference
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HMENU
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.LRESULT
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinDef.RECT
import com.sun.jna.platform.win32.WinDef.UINT
import com.sun.jna.platform.win32.WinReg
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.platform.win32.WinUser.SWP_ASYNCWINDOWPOS
import com.sun.jna.platform.win32.WinUser.SWP_FRAMECHANGED
import com.sun.jna.platform.win32.WinUser.SWP_HIDEWINDOW
import com.sun.jna.platform.win32.WinUser.SWP_NOMOVE
import com.sun.jna.platform.win32.WinUser.SWP_NOSIZE
import com.sun.jna.platform.win32.WinUser.SWP_NOZORDER
import com.sun.jna.platform.win32.WinUser.SWP_SHOWWINDOW
import com.sun.jna.platform.win32.WinUser.WM_SIZE
import com.sun.jna.platform.win32.WinUser.WS_SYSMENU
import com.sun.jna.platform.win32.WinUser.WindowProc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.skiko.SkiaLayer
import java.awt.Window

@Composable
fun HandleWindowsWindowProc(desktopWindow: DesktopWindowState, windowScope: androidx.compose.ui.window.WindowScope?) {
    val os = System.getProperty("os.name").lowercase()
    if (!os.contains("win")) return
    DisposableEffect(desktopWindow, windowScope) {
        val handler = try {
            WindowsWindowUtils.instance.handleWindowProc(desktopWindow, windowScope)
        } catch (_: Throwable) {
            null
        }
        onDispose {
            handler?.let { WindowsWindowUtils.instance.disposeWindowProc(desktopWindow) }
        }
    }
}

open class BasicWindowProc(
    private val user32: ExtendedUser32,
    window: Window,
) : WindowProc, AutoCloseable {
    protected val windowHandle: HWND = HWND(
        (window as? ComposeWindow)
            ?.let { Pointer(it.windowHandle) }
            ?: Native.getWindowPointer(window)
    )

    private val _accentColor: MutableStateFlow<Color> = MutableStateFlow(currentAccentColor())
    val accentColor: StateFlow<Color> = _accentColor.asStateFlow()

    private val defaultWindowProc = user32.SetWindowLongPtr(
        windowHandle, WinUser.GWL_WNDPROC, CallbackReference.getFunctionPointer(this)
    )

    override fun callback(hwnd: HWND, uMsg: Int, wParam: WinDef.WPARAM, lParam: WinDef.LPARAM): LRESULT {
        if (uMsg == ExtendedUser32.WM_SETTINGCHANGE) {
            val changedKey = Pointer(lParam.toLong()).getWideString(0)
            if (changedKey == "ImmersiveColorSet") {
                _accentColor.tryEmit(currentAccentColor())
                onThemeChanged()
            }
        }
        return callDefWindowProc(hwnd, uMsg, wParam, lParam)
    }

    protected open fun onThemeChanged() {}

    private fun callDefWindowProc(hwnd: HWND, uMsg: Int, wParam: WinDef.WPARAM, lParam: WinDef.LPARAM): LRESULT {
        return user32.CallWindowProc(defaultWindowProc, hwnd, uMsg, wParam, lParam)
    }

    private fun currentAccentColor(): Color {
        val value = Advapi32Util.registryGetIntValue(
            WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Microsoft\\Windows\\DWM", "AccentColor"
        ).toLong()
        val alpha = (value and 0xFF000000)
        val green = (value and 0xFF).shl(16)
        val blue = (value and 0xFF00)
        val red = (value and 0xFF0000).shr(16)
        return Color((alpha or green or blue or red).toInt())
    }

    override fun close() {
        user32.SetWindowLongPtr(windowHandle, WinUser.GWL_WNDPROC, defaultWindowProc)
    }
}

class ExtendedTitleBarWindowProc(
    window: Window,
    private val user32: ExtendedUser32,
    dwmapi: Dwmapi
) : BasicWindowProc(user32, window) {

    private var childHitTestOwner: WindowsWindowHitTestOwner? = null

    private val _windowIsActive: MutableStateFlow<Boolean> = MutableStateFlow(user32.GetActiveWindow() == windowHandle)
    val windowIsActive: StateFlow<Boolean> = _windowIsActive.asStateFlow()

    private val _frameIsColorful: MutableStateFlow<Boolean> = MutableStateFlow(isAccentColorWindowFrame())
    val frameIsColorful: StateFlow<Boolean> = _frameIsColorful.asStateFlow()

    private var hitTestResult = WindowsWindowHitResult.CLIENT

    private val skiaLayerWindowProc: SkiaLayerHitTestWindowProc? =
        window.findSkiaLayer()?.let { SkiaLayerHitTestWindowProc(it, user32, ::hitTest) }

    private var isMaximized: Boolean = user32.isWindowInMaximized(windowHandle)
    private var dpi: UINT = UINT(0)
    private var width: Int = 0
    private var height: Int = 0
    private var frameX: Int = 0
    private var frameY: Int = 0
    private var edgeX: Int = 0
    private var edgeY: Int = 0
    private var padding: Int = 0

    init {
        dwmapi.DwmExtendFrameIntoClientArea(windowHandle, Dwmapi.WindowMargins(-1, -1, -1, -1))
        windowHandle.updateWindowStyle { it and WS_SYSMENU.inv() }
        eraseWindowBackground()
    }

    private fun hitTestWindowResizerBorder(x: Int, y: Int): WindowsWindowHitResult {
        updateWindowInfo()
        val currentStyle = User32.INSTANCE.GetWindowLong(windowHandle, WinUser.GWL_STYLE)
        if (currentStyle and WinUser.WS_CAPTION == 0) {
            return WindowsWindowHitResult.NOWHERE
        }
        val horizontalPadding = frameX
        val verticalPadding = frameY
        return when {
            x <= horizontalPadding && y > verticalPadding && y < height - verticalPadding ->
                WindowsWindowHitResult.BORDER_LEFT
            x <= horizontalPadding && y <= verticalPadding ->
                WindowsWindowHitResult.BORDER_TOP_LEFT
            x <= horizontalPadding ->
                WindowsWindowHitResult.BORDER_BOTTOM_LEFT
            y <= verticalPadding && x > horizontalPadding && x < width - horizontalPadding ->
                WindowsWindowHitResult.BORDER_TOP
            y <= verticalPadding ->
                WindowsWindowHitResult.BORDER_TOP_RIGHT
            x >= width - horizontalPadding && y > verticalPadding && y < height - verticalPadding ->
                WindowsWindowHitResult.BORDER_RIGHT
            x >= width - horizontalPadding ->
                WindowsWindowHitResult.BORDER_BOTTOM_RIGHT
            y >= height - verticalPadding && x > horizontalPadding && x < width - horizontalPadding ->
                WindowsWindowHitResult.BORDER_BOTTOM
            y >= height - verticalPadding ->
                WindowsWindowHitResult.BORDER_BOTTOM_RIGHT
            else -> WindowsWindowHitResult.NOWHERE
        }
    }

    private fun hitTest(lParam: WinDef.LPARAM): WindowsWindowHitResult {
        return lParam.usePoint { x, y ->
            if (!isMaximized) {
                hitTestResult = hitTestWindowResizerBorder(x, y)
                if (isHitWindowResizer(hitTestResult)) {
                    return@usePoint hitTestResult
                }
            }
            hitTestResult = childHitTestOwner?.hitTest(x.toFloat(), y.toFloat())
                ?: WindowsWindowHitResult.CLIENT
            hitTestResult
        }
    }

    override fun callback(hwnd: HWND, uMsg: Int, wParam: WinDef.WPARAM, lParam: WinDef.LPARAM): LRESULT {
        return when (uMsg) {
            ExtendedUser32.WM_NCCALCSIZE -> {
                if (wParam.toInt() == 0) {
                    super.callback(hwnd, uMsg, wParam, lParam)
                } else {
                    val style = user32.GetWindowLong(hwnd, WinUser.GWL_STYLE)
                    if (style and (WinUser.WS_CAPTION or WinUser.WS_THICKFRAME) == 0) {
                        frameX = 0; frameY = 0; edgeX = 0; edgeY = 0; padding = 0
                        isMaximized = user32.isWindowInMaximized(hwnd)
                        return LRESULT(0)
                    }
                    dpi = user32.GetDpiForWindow(hwnd)
                    frameX = user32.GetSystemMetricsForDpi(WinUser.SM_CXFRAME, dpi)
                    frameY = user32.GetSystemMetricsForDpi(WinUser.SM_CYFRAME, dpi)
                    edgeX = user32.GetSystemMetricsForDpi(WinUser.SM_CXEDGE, dpi)
                    edgeY = user32.GetSystemMetricsForDpi(WinUser.SM_CYEDGE, dpi)
                    padding = user32.GetSystemMetricsForDpi(WinUser.SM_CXPADDEDBORDER, dpi)
                    isMaximized = user32.isWindowInMaximized(hwnd)
                    val params = Structure.newInstance(NCCalcSizeParams::class.java, Pointer(lParam.toLong()))
                    params.read()
                    params.rgrc[0]?.apply {
                        left += if (isMaximized) frameX + padding else edgeX
                        right -= if (isMaximized) frameX + padding else edgeX
                        bottom -= if (isMaximized) padding + frameX else edgeY
                        top += if (isMaximized) padding + frameX else 0
                    }
                    params.write()
                    LRESULT(0)
                }
            }

            ExtendedUser32.WM_NCHITTEST -> {
                if (!isMaximized) {
                    val callResult = lParam.usePoint(::hitTestWindowResizerBorder)
                    if (isHitWindowResizer(callResult)) {
                        hitTestResult = callResult
                    }
                }
                LRESULT(hitTestResult.value.toLong())
            }

            ExtendedUser32.WM_NCRBUTTONUP -> {
                if (wParam.toInt() == WindowsWindowHitResult.CAPTION.value) {
                    val oldStyle = user32.GetWindowLong(hwnd, WinUser.GWL_STYLE)
                    user32.SetWindowLong(hwnd, WinUser.GWL_STYLE, oldStyle or WS_SYSMENU)
                    val menu = user32.GetSystemMenu(hwnd, false)
                    user32.SetWindowLong(hwnd, WinUser.GWL_STYLE, oldStyle)
                    isMaximized = user32.isWindowInMaximized(hwnd)
                    if (menu != null) {
                        val menuItemInfo = ExtendedUser32.MENUITEMINFO().apply {
                            cbSize = this.size()
                            fMask = ExtendedUser32.MIIM_STATE
                            fType = ExtendedUser32.MFT_STRING
                        }
                        updateMenuItemInfo(menu, menuItemInfo, ExtendedUser32.SC_RESTORE, isMaximized)
                        updateMenuItemInfo(menu, menuItemInfo, ExtendedUser32.SC_MOVE, !isMaximized)
                        updateMenuItemInfo(menu, menuItemInfo, ExtendedUser32.SC_SIZE, !isMaximized)
                        updateMenuItemInfo(menu, menuItemInfo, WinUser.SC_MINIMIZE, true)
                        updateMenuItemInfo(menu, menuItemInfo, WinUser.SC_MAXIMIZE, !isMaximized)
                        updateMenuItemInfo(menu, menuItemInfo, ExtendedUser32.SC_CLOSE, true)
                        user32.SetMenuDefaultItem(menu, ExtendedUser32.WINT_MAX, false)
                        val lParamValue = lParam.toInt()
                        val x = lowWord(lParamValue)
                        val y = highWord(lParamValue)
                        val ret = user32.TrackPopupMenu(menu, ExtendedUser32.TPM_RETURNCMD, x, y, 0, hwnd, null)
                        menuItemInfo.clear()
                        if (ret != 0) {
                            user32.PostMessage(hwnd, WinUser.WM_SYSCOMMAND, WinDef.WPARAM(ret.toLong()), WinDef.LPARAM(0))
                        }
                    }
                }
                super.callback(hwnd, uMsg, wParam, lParam)
            }

            WM_SIZE -> {
                val lParamValue = lParam.toInt()
                width = lowWord(lParamValue)
                height = highWord(lParamValue)
                super.callback(hwnd, uMsg, wParam, lParam)
            }

            else -> {
                if (uMsg == ExtendedUser32.WM_ACTIVATE) {
                    _windowIsActive.tryEmit(wParam.toInt() != ExtendedUser32.WA_INACTIVE)
                }
                if (uMsg == ExtendedUser32.WM_NCMOUSEMOVE) {
                    skiaLayerWindowProc?.let {
                        user32.PostMessage(it.contentHandle, uMsg, wParam, lParam)
                    }
                }
                if (uMsg == ExtendedUser32.WM_SETTINGCHANGE) {
                    val changedKey = Pointer(lParam.toLong()).getWideString(0)
                    if (changedKey == "ImmersiveColorSet") {
                        _frameIsColorful.tryEmit(isAccentColorWindowFrame())
                    }
                }
                super.callback(hwnd, uMsg, wParam, lParam)
            }
        }
    }

    internal fun updateChildHitTestProvider(owner: WindowsWindowHitTestOwner) {
        childHitTestOwner = owner
    }

    override fun onThemeChanged() {
        _frameIsColorful.tryEmit(isAccentColorWindowFrame())
    }

    private fun updateMenuItemInfo(menu: HMENU, menuItemInfo: ExtendedUser32.MENUITEMINFO, item: Int, enabled: Boolean) {
        menuItemInfo.fState = if (enabled) ExtendedUser32.MFS_ENABLED else ExtendedUser32.MFS_DISABLED
        user32.SetMenuItemInfo(menu, item, false, menuItemInfo)
    }

    private fun eraseWindowBackground() {
        val buildNumber = WindowsWindowUtils.instance.windowsBuildNumber() ?: return
        if (buildNumber < 22000) {
            val flag = SWP_NOZORDER or ExtendedUser32.SWP_NOACTIVATE or SWP_FRAMECHANGED or SWP_NOMOVE or SWP_NOSIZE or SWP_ASYNCWINDOWPOS
            user32.SetWindowPos(windowHandle, null, 0, 0, 0, 0, flag or SWP_HIDEWINDOW)
            user32.SetWindowPos(windowHandle, null, 0, 0, 0, 0, flag or SWP_SHOWWINDOW)
        }
    }

    private fun highWord(value: Int): Int = (value shr 16) and 0xFFFF
    private fun lowWord(value: Int): Int = value and 0xFFFF

    private fun isHitWindowResizer(hitResult: WindowsWindowHitResult): Boolean = when (hitResult) {
        WindowsWindowHitResult.BORDER_TOP, WindowsWindowHitResult.BORDER_LEFT,
        WindowsWindowHitResult.BORDER_RIGHT, WindowsWindowHitResult.BORDER_BOTTOM,
        WindowsWindowHitResult.BORDER_TOP_LEFT, WindowsWindowHitResult.BORDER_TOP_RIGHT,
        WindowsWindowHitResult.BORDER_BOTTOM_LEFT, WindowsWindowHitResult.BORDER_BOTTOM_RIGHT -> true
        else -> false
    }

    private fun updateWindowInfo() {
        dpi = user32.GetDpiForWindow(windowHandle)
        frameX = user32.GetSystemMetricsForDpi(WinUser.SM_CXFRAME, dpi)
        frameY = user32.GetSystemMetricsForDpi(WinUser.SM_CYFRAME, dpi)
        val rect = RECT()
        if (user32.GetWindowRect(windowHandle, rect)) {
            rect.read()
            width = rect.right - rect.left
            height = rect.bottom - rect.top
        }
        rect.clear()
    }

    private inline fun <T> WinDef.LPARAM.usePoint(crossinline block: (x: Int, y: Int) -> T): T {
        val intValue = toInt()
        val x = lowWord(intValue).toShort().toInt()
        val y = highWord(intValue).toShort().toInt()
        val point = POINT(x, y)
        user32.ScreenToClient(windowHandle, point)
        point.read()
        val result = block(point.x, point.y)
        point.clear()
        return result
    }

    private inline fun HWND.updateWindowStyle(block: (old: Int) -> Int) {
        val oldStyle = user32.GetWindowLong(this, WinUser.GWL_STYLE)
        user32.SetWindowLong(this, WinUser.GWL_STYLE, block(oldStyle))
    }

    private fun isAccentColorWindowFrame(): Boolean {
        return Advapi32Util.registryGetIntValue(
            WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Microsoft\\Windows\\DWM", "ColorPrevalence"
        ) != 0
    }

    @Structure.FieldOrder("rgrc", "lppos")
    @Suppress("SpellCheckingInspection", "unused")
    class NCCalcSizeParams(
        @JvmField var rgrc: Array<RECT?> = Array(3) { null },
        @JvmField var lppos: WindowPos? = null
    ) : Structure(), Structure.ByReference

    @Structure.FieldOrder("hwnd", "hwndInsertAfter", "x", "y", "cx", "cy", "flags")
    @Suppress("SpellCheckingInspection", "unused")
    class WindowPos(
        @JvmField var hwnd: HWND? = null,
        @JvmField var hwndInsertAfter: HWND? = null,
        @JvmField var x: Int = 0,
        @JvmField var y: Int = 0,
        @JvmField var cx: Int = 0,
        @JvmField var cy: Int = 0,
        @JvmField var flags: UINT = UINT()
    ) : Structure(), Structure.ByReference

    override fun close() {
        skiaLayerWindowProc?.close()
        windowHandle.updateWindowStyle { it or WS_SYSMENU }
        super.close()
    }
}

class SkiaLayerHitTestWindowProc(
    skiaLayer: SkiaLayer,
    private val user32: ExtendedUser32,
    private val hitTest: (lParam: WinDef.LPARAM) -> WindowsWindowHitResult,
) : WindowProc, AutoCloseable {
    private val windowHandle = HWND(Pointer(skiaLayer.windowHandle))
    internal val contentHandle = HWND(skiaLayer.canvas.let(Native::getComponentPointer))

    private val defaultWindowProc =
        user32.SetWindowLongPtr(contentHandle, WinUser.GWL_WNDPROC, CallbackReference.getFunctionPointer(this))

    private var hitResult = WindowsWindowHitResult.CLIENT

    init {
        val buildNumber = WindowsWindowUtils.instance.windowsBuildNumber() ?: 22000
        skiaLayer.transparency = buildNumber < 22000
    }

    override fun callback(hwnd: HWND, uMsg: Int, wParam: WinDef.WPARAM, lParam: WinDef.LPARAM): LRESULT {
        return when (uMsg) {
            ExtendedUser32.WM_NCHITTEST -> {
                hitResult = hitTest(lParam)
                when (hitResult) {
                    WindowsWindowHitResult.CLIENT,
                    WindowsWindowHitResult.CAPTION_MAX,
                    WindowsWindowHitResult.CAPTION_MIN,
                    WindowsWindowHitResult.CAPTION_CLOSE -> hitResult
                    else -> WindowsWindowHitResult.TRANSPARENT
                }.let { LRESULT(it.value.toLong()) }
            }
            ExtendedUser32.WM_NCMOUSEMOVE -> {
                user32.SendMessage(contentHandle, ExtendedUser32.WM_MOUSEMOVE, wParam, lParam)
                LRESULT(0)
            }
            ExtendedUser32.WM_NCLBUTTONDOWN -> {
                user32.SendMessage(contentHandle, ExtendedUser32.WM_LBUTTONDOWN, wParam, lParam)
                LRESULT(0)
            }
            ExtendedUser32.WM_NCLBUTTONUP -> {
                user32.SendMessage(contentHandle, ExtendedUser32.WM_LBUTTONUP, wParam, lParam)
                LRESULT(0)
            }
            ExtendedUser32.WM_NCRBUTTONUP -> {
                user32.SendMessage(windowHandle, uMsg, wParam, lParam)
                LRESULT(0)
            }
            else -> {
                user32.CallWindowProc(defaultWindowProc, hwnd, uMsg, wParam, lParam) ?: LRESULT(0)
            }
        }
    }

    override fun close() {
        user32.SetWindowLongPtr(contentHandle, WinUser.GWL_WNDPROC, defaultWindowProc)
    }
}

private fun User32.isWindowInMaximized(hWnd: HWND): Boolean {
    val placement = WinUser.WINDOWPLACEMENT()
    val result = GetWindowPlacement(hWnd, placement).booleanValue() &&
            placement.showCmd == WinUser.SW_SHOWMAXIMIZED
    placement.clear()
    return result
}

private fun Window.findSkiaLayer(): SkiaLayer? {
    return try {
        findComponent(this, SkiaLayer::class.java)
    } catch (_: Throwable) {
        null
    }
}

private fun <T> findComponent(container: java.awt.Container, klass: Class<T>): T? {
    val componentSequence = container.components.asSequence()
    return componentSequence
        .filter { klass.isInstance(it) }
        .ifEmpty {
            componentSequence
                .filterIsInstance<java.awt.Container>()
                .mapNotNull { findComponent(it, klass) }
        }.map { klass.cast(it) }
        .firstOrNull()
}
