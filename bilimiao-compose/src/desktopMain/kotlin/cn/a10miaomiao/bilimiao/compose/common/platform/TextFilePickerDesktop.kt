package cn.a10miaomiao.bilimiao.compose.common.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.EventQueue
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * 桌面端没有 SAF，用 AWT 的文件对话框。
 * 对话框是模态的，必须放到 IO 线程再转到 EDT 弹出，否则会卡住 Compose 主线程。
 */
@Composable
actual fun rememberTextFilePicker(): TextFilePicker {
    val scope = rememberCoroutineScope()
    return remember(scope) {
        object : TextFilePicker {
            override fun saveText(suggestedName: String, content: String, onResult: (Boolean) -> Unit) {
                scope.launch {
                    val success = withContext(Dispatchers.IO) { saveWithDialog(suggestedName, content) }
                    onResult(success)
                }
            }

            override fun openText(onResult: (String?) -> Unit) {
                scope.launch {
                    val text = withContext(Dispatchers.IO) { openWithDialog() }
                    onResult(text)
                }
            }
        }
    }
}

private fun saveWithDialog(suggestedName: String, content: String): Boolean {
    var success = false
    EventQueue.invokeAndWait {
        val dialog = FileDialog(null as Frame?, "导出屏蔽设置", FileDialog.SAVE).apply {
            file = suggestedName
            isVisible = true
        }
        val fileName = dialog.file
        if (fileName != null) {
            success = runCatching {
                File(dialog.directory ?: "", fileName).writeText(content)
            }.isSuccess
        }
        dialog.dispose()
    }
    return success
}

private fun openWithDialog(): String? {
    var text: String? = null
    EventQueue.invokeAndWait {
        val dialog = FileDialog(null as Frame?, "导入屏蔽设置", FileDialog.LOAD).apply {
            isVisible = true
        }
        val fileName = dialog.file
        if (fileName != null) {
            text = runCatching {
                File(dialog.directory ?: "", fileName).readText()
            }.getOrNull()
        }
        dialog.dispose()
    }
    return text
}
