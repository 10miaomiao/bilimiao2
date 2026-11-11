package cn.a10miaomiao.bilimiao.compose.common.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TEXT_MIME_TYPE = "text/plain"

/**
 * 用 SAF（存储访问框架）实现，不需要任何存储权限，
 * 也不需要关心用户把文件放在哪个目录。
 */
@Composable
actual fun rememberTextFilePicker(): TextFilePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 结果回调是同步返回的，待写内容和回调先暂存起来。
    // 用普通类而不是 mutableStateOf，避免暂存动作触发重组。
    val pending = remember { PendingPick() }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(TEXT_MIME_TYPE),
    ) { uri ->
        val content = pending.content
        val callback = pending.saveCallback
        pending.content = null
        pending.saveCallback = null
        if (uri == null || content == null) {
            callback?.invoke(false)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use {
                        it.write(content.toByteArray())
                    } ?: error("无法打开输出流")
                }.isSuccess
            }
            callback?.invoke(success)
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        val callback = pending.openCallback
        pending.openCallback = null
        if (uri == null) {
            callback?.invoke(null)
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
            }
            callback?.invoke(text)
        }
    }

    return remember(saveLauncher, openLauncher) {
        object : TextFilePicker {
            override fun saveText(suggestedName: String, content: String, onResult: (Boolean) -> Unit) {
                pending.content = content
                pending.saveCallback = onResult
                saveLauncher.launch(suggestedName)
            }

            override fun openText(onResult: (String?) -> Unit) {
                pending.openCallback = onResult
                // 部分文件管理器不会把 .txt 后缀映射成 text/plain，
                // 这里额外放开一种类型，避免用户自己的备份文件在列表里是灰的
                openLauncher.launch(arrayOf(TEXT_MIME_TYPE, "application/octet-stream"))
            }
        }
    }
}

private class PendingPick {
    var content: String? = null
    var saveCallback: ((Boolean) -> Unit)? = null
    var openCallback: ((String?) -> Unit)? = null
}
