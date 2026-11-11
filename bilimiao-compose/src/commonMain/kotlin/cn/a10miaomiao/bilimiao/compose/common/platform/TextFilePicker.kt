package cn.a10miaomiao.bilimiao.compose.common.platform

import androidx.compose.runtime.Composable

/**
 * 系统级的“另存为 / 打开文件”对话框。
 *
 * 目前只服务于屏蔽设置的纯文本备份导入导出，所以固定按 text/plain 处理，
 * 不暴露 mime 参数；如果以后要复用到别的场景再扩展。
 */
interface TextFilePicker {

    /**
     * 弹出另存为对话框。
     * 用户取消或写入失败时回调 false。
     */
    fun saveText(suggestedName: String, content: String, onResult: (Boolean) -> Unit)

    /**
     * 弹出打开文件对话框并读出文本内容。
     * 用户取消或读取失败时回调 null。
     */
    fun openText(onResult: (String?) -> Unit)
}

@Composable
expect fun rememberTextFilePicker(): TextFilePicker
