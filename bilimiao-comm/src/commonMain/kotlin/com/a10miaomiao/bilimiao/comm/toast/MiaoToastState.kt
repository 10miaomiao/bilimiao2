package com.a10miaomiao.bilimiao.comm.toast

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * toast 上的动作按钮（例如「撤销」「恢复」）。
 *
 * @param text 按钮文案
 * @param onClick 点击回调，先执行回调再移除该条 toast
 */
data class MiaoToastAction(
    val text: String,
    val onClick: () -> Unit,
)

/** toast 显示位置：贴安全区底部或顶部 */
enum class ToastPosition { Bottom, Top }

/**
 * 一条 toast 数据。
 *
 * @param id 单调递增的唯一标识：作为列表 key、独立计时协程的 key 与 [MiaoToastState.dismiss] 的匹配条件
 * @param duration 这条 toast 的显示时长（到点自动消失）
 * @param position 这条 toast 的显示位置（贴安全区底部或顶部）
 * @param action 可选的动作按钮
 */
data class MiaoToast(
    val id: Long,
    val message: String,
    val duration: Duration = MiaoToastDefaults.DurationShort,
    val position: ToastPosition = ToastPosition.Bottom,
    val action: MiaoToastAction? = null,
)

/**
 * 默认时长，对齐老项目 DialogX PopTip 的 `showShort` / `showLong`。
 */
object MiaoToastDefaults {
    /** 短提示时长（对应 PopTip.showShort） */
    val DurationShort = 2.seconds

    /** 长提示时长（对应 PopTip.showLong） */
    val DurationLong = 3500.milliseconds
}

/**
 * toast 状态持有者（自研实现，对应 sonner 的 `ToasterState`）。
 *
 * 只依赖 kotlinx-coroutines 与 kotlin.time，**不依赖 Compose**，因此可以放在 `bilimiao-comm` 的 commonMain，
 * 由任意平台的界面层（`MiaoToastHost`）订阅 [toasts] 渲染。
 *
 * 与 sonner 一致的地方：
 * - 列表顺序约定为「旧 → 新」，新 toast 追加到**末尾**；
 * - [show] 返回创建出来的 [MiaoToast]，调用方可在需要时提前 [dismiss] 它；
 * - 每条 toast 自带 [MiaoToast.duration]，由界面层为每条单独计时，互不干扰；
 * - [dismiss] 按 id 精确移除，不涉及「哪一条是最新的」之类的位置推断。
 *
 * 相对 sonner 有意简化的地方：
 * - 不做 toast 类型（success/error/loading）与富文本，只保留文案 + 可选动作按钮；
 * - 不做 `duration = Long.MAX_VALUE` 的常驻语义、不做 `promise` 之类的高级 API；
 * - 不持有宿主相关信息（宿主 token、创建时刻、剩余时长等），state 与宿主同生命周期，
 *   换宿主即换 state，天然不存在「旧宿主的提示残留」。
 *
 * @param maxVisibleToasts **每个位置**各自保留的最大条数（底部、顶部分开计数，互不挤占），
 *   超出时丢弃该位置里**最旧**的一条。这是界面层为了不糊满屏幕而设的**视觉上限**，与 DialogX 无关。
 */
class MiaoToastState(
    private val maxVisibleToasts: Int = 3,
) {
    /** 递增 id 生成器。用 StateFlow 的原子更新保证线程安全：调用可能来自任意线程 */
    private val idGen = MutableStateFlow(0L)

    private val _toasts = MutableStateFlow<List<MiaoToast>>(emptyList())

    /**
     * 当前需要显示的 toast 列表，**顺序为「旧 → 新」**：下标 0 最旧，末尾最新。
     *
     * 空列表表示当前无提示。
     */
    val toasts: StateFlow<List<MiaoToast>> = _toasts.asStateFlow()

    /**
     * 追加一条 toast 到列表末尾，并返回它。
     *
     * 达到 [maxVisibleToasts] 上限时，**最旧的那一条会从列表里被移出**（保留最新的 N 条）。
     * 界面层按「列表差集」渲染，因此这条被挤掉的 toast 会像正常到期一样播完退场动画再消失，
     * 不会静默消失。
     *
     * @param message 文案
     * @param action 可选的动作按钮
     * @param duration 显示时长
     * @param position 显示位置：位置由每条 toast 自己声明，界面层据此分栈渲染
     */
    fun show(
        message: String,
        action: MiaoToastAction? = null,
        duration: Duration = MiaoToastDefaults.DurationShort,
        position: ToastPosition = ToastPosition.Bottom,
    ): MiaoToast {
        val toast = MiaoToast(
            id = idGen.updateAndGet { it + 1 },
            message = message,
            duration = duration,
            position = position,
            action = action,
        )
        val limit = maxVisibleToasts.coerceAtLeast(1)
        _toasts.update { list ->
            val appended = list + toast
            // 按位置分别保留最近 limit 条：底部栈与顶部栈各自独立，互不挤占。
            // 用 id 集合过滤而不是分组重建，保证结果仍是「旧 → 新」的全局顺序。
            val keepIds = appended
                .groupBy { it.position }
                .values
                .flatMap { group -> group.takeLast(limit) }
                .mapTo(mutableSetOf()) { it.id }
            appended.filter { it.id in keepIds }
        }
        return toast
    }

    /**
     * 移除指定 toast（按 id 匹配）。
     *
     * id 不在列表中时**原样返回同一个列表实例**：避免发出内容相同的新状态引起无意义重组
     * （例如计时协程到点 dismiss 与动作按钮点击 dismiss 撞在一起时）。
     */
    fun dismiss(toast: MiaoToast) {
        _toasts.update { list ->
            if (list.none { it.id == toast.id }) list else list.filterNot { it.id == toast.id }
        }
    }

    /** 清空全部 toast */
    fun dismissAll() {
        _toasts.value = emptyList()
    }
}
