package cn.a10miaomiao.bilimiao.compose.common.back

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 跨平台返回事件栈
 *
 * 「返回」在不同平台上入口不同：Android 有系统返回键/返回手势，桌面端没有，
 * 只能靠 app bar 返回按钮或键盘快捷键。为了让所有平台共用同一套返回语义
 * （app bar 返回 == 系统返回），这里把返回抽象成应用级事件：
 *
 * 页面与组件通过 [cn.a10miaomiao.bilimiao.compose.common.BackHandler] 注册到同一个返回栈，
 * 由 [AppBackHandler] 统一按优先级分发，各平台只负责把「原生返回入口」接到这个栈上：
 * - Android：系统返回（含返回手势）→ [AppBackHandler.handleBack]
 * - 桌面端：Esc 快捷键 → [AppBackHandler.handleBack]
 * - 所有平台：app bar 返回按钮 → [AppBackHandler.handleBack]
 *
 * 分发顺序为「后注册优先」（LIFO），与 Android 系统返回的语义一致。
 */
class BackDispatcher {

    private class Entry(
        val isEnabled: () -> Boolean,
        val onBack: () -> Unit,
    )

    private val entries = mutableListOf<Entry>()

    /**
     * 注册返回处理器，返回取消注册的函数
     *
     * @param isEnabled 通过函数而不是值传入，处理器的启用状态变化时无需重新注册
     */
    fun register(isEnabled: () -> Boolean, onBack: () -> Unit): () -> Unit {
        val entry = Entry(isEnabled, onBack)
        entries.add(entry)
        return { entries.remove(entry) }
    }

    /**
     * 分发返回事件
     *
     * @return 是否被处理器消费
     */
    fun dispatch(): Boolean {
        // 复制一份再遍历：处理器内部可能触发重组，从而在本轮分发中注册/注销其它处理器
        val snapshot = entries.toList()
        for (i in snapshot.indices.reversed()) {
            val entry = snapshot[i]
            if (entry.isEnabled()) {
                entry.onBack()
                return true
            }
        }
        return false
    }
}

/**
 * 应用内统一返回栈，由 MainComposeHost 提供
 *
 * 为 null 表示当前组合不在应用主界面内（例如独立 Activity 的弹层），
 * 此时 [cn.a10miaomiao.bilimiao.compose.common.BackHandler] 退化为平台自身的返回机制。
 */
val LocalBackDispatcher = staticCompositionLocalOf<BackDispatcher?> { null }
