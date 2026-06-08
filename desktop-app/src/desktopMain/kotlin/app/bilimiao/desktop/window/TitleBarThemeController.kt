package app.bilimiao.desktop.window

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf

val LocalTitleBarThemeController = compositionLocalOf<TitleBarThemeController?> { null }

class TitleBarThemeController {
    val isDark by derivedStateOf { requesterStack.lastOrNull()?.second == true }

    private val requesterStack = mutableStateListOf<Pair<Any, Boolean>>()

    fun requestTheme(owner: Any, isDark: Boolean) {
        val index = requesterStack.indexOfLast { it.first == owner }
        if (index >= 0) {
            requesterStack[index] = owner to isDark
        } else {
            requesterStack.add(owner to isDark)
        }
    }

    fun removeTheme(owner: Any) {
        requesterStack.removeIf { it.first == owner }
    }
}
