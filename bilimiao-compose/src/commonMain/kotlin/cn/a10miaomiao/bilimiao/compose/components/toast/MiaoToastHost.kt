package cn.a10miaomiao.bilimiao.compose.components.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.comm.toast.MiaoToast
import com.a10miaomiao.bilimiao.comm.toast.MiaoToastState
import com.a10miaomiao.bilimiao.comm.toast.ToastPosition
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt

/** 底部栈距安全区底边的距离（PopTip 的 100dp 观感，避开底部控制栏 / 手势区） */
private val ToastBottomMargin = 100.dp

/**
 * 顶部栈距安全区顶边的距离：贴近屏幕边缘，只留一点呼吸感。
 *
 * 「安全区」用的是 [WindowInsets.safeDrawing]（状态栏 ∪ 刘海/挖孔 ∪ 输入法），
 * 因此这里再往上不会被状态栏或刘海压住。
 */
private val ToastTopMargin = 12.dp

/** 气泡左右外边距 */
private val ToastHorizontalMargin = 35.dp

/** 进场动画时长（毫秒）：淡入 + 缩放，比退场稍长，出现时有「浮上来」的余量 */
private const val ToastEnterMillis = 250

/** 位移（旧条目让位 / 补位）时长：对齐 PopTip `moveBack` 的 300ms */
private const val ToastShiftMillis = 300

/** 退场动画时长（毫秒）：比进场短，消失更干脆，随后立刻由位移补位 */
private const val ToastExitMillis = 200

/**
 * 退场缩放终点：幽灵从 1 缩到该值，与淡出同时进行。
 *
 * 0.9 = 缩 10%，在 200ms 内能明显看出「收小消失」；想更含蓄可调 0.94，更夸张可到 0.85。
 * 与进场 0.92 → 1 的放大互为呼应，一胀一缩。
 */
private const val ToastExitScale = 0.9f

/**
 * 超出 [MiaoToastState] 上限被挤掉的那一条：退场比正常到期更短促、收得更狠，并且**向远离锚点
 * 的方向平移**（底部锚点向上飞、顶部锚点向下掉）——观感上是它被新来的那条挤出栈外。
 *
 * 为什么单独一套：它是在同一次更新里「旧的得走、新的要进」的竞争场景，用和普通到期一样的
 * 200ms 原地淡出会显得像被顶掉；短促 + 外移才像它自己**主动让位**。
 */
private const val ToastOverflowExitMillis = 160

/** 被挤掉那条的缩放终点（见 [ToastOverflowExitMillis]） */
private const val ToastOverflowExitScale = 0.84f

/** 被挤掉那条向栈外平移的距离（约气泡高度的 1/3，够看清方向又不至于飞出屏幕） */
private val ToastOverflowLift = 16.dp

/**
 * DialogX 堆叠上移用的插值器：Android `DecelerateInterpolator(2f)`。
 *
 * AOSP 公式 `y = 1 - (1 - t)^(2 * factor)`，factor = 2 即 `1 - (1 - t)^4`。
 * 与 Compose 的 `LinearOutSlowInEasing`（cubic-bezier(0, 0, 0.2, 1)）相比，它在开头
 * 就跟得很紧（t = 0.25 时已完成约 68%，后者约 57%；t = 0.5 时约 94%，后者约 84%），
 * 剩下的一半时间都花在最后那一小段收尾上——这正是 PopTip 上移时「先整体跟手、
 * 再轻轻落到位置」的来源。换成这条曲线，位移和淡入的节奏才和老项目一致。
 */
private val PopTipDecelerate: Easing = Easing { t -> 1f - (1f - t).pow(4) }

/**
 * DialogX 退场用的默认插值器 `AccelerateDecelerateInterpolator`：`y = cos((t + 1)π) / 2 + 0.5`。
 * 用于淡出，与进场的 Decelerate 形成「快进慢出」的对称观感。
 */
private val PopTipAccelerateDecelerate: Easing =
    Easing { t -> (cos((t + 1f) * PI) / 2.0).toFloat() + 0.5f }

/** 气泡最小高度 */
private val ToastMinHeight = 45.dp

/** 胶囊形状（percent 50：多行文本时会自动 clamp 成合理的圆角） */
private val ToastShape = RoundedCornerShape(percent = 50)

/**
 * 气泡四周为阴影预留的透明留白（不改变视觉间距）。
 *
 * 为什么需要：阴影由 graphics layer 画在节点边界之外，而 `AnimatedVisibility` 在过渡期间
 * 会给内容加一层透明度/缩放层，最外侧 2dp 阴影会被这层图层的边界裁掉——表现就是
 * 「动画跑完才看到完整阴影」。把留白放进过渡内容的边界内，阴影在动画期间也完整。
 *
 * 注意这与之前 `LazyColumn` 时代那个留白不是一回事：那个是为了躲 `clipScrollableContainer`，
 * 现在 `Column` 没有 viewport 裁剪；这里只是让阴影落在过渡图层的边界内。
 * 配套：[ToastStackGap] 相应收窄，锚点偏移扣掉 [ToastShadowRoom]，视觉间距与位置都不变。
 */
private val ToastShadowRoom = 6.dp

/**
 * 堆叠时相邻气泡的视觉间距（对齐 PopTip 的行距观感）。
 *
 * 实际视觉间距 = 上一条的 [ToastShadowRoom] + 这里的 gap + 下一条的 [ToastShadowRoom]
 * = 6dp + 2dp + 6dp = 14dp（等于 PopTip 的 45dp × 0.3）。
 */
private val ToastStackGap = 2.dp

/**
 * 一条**还在栈里**的 toast 条目：数据（[toast]）+ 进场状态 + 最近一次测量的位置。
 *
 * 数据层的 toast 一旦被移除，条目会**立刻**从 [Column] 里摘掉（让其余条目马上开始补位），
 * 气泡本体的淡出交给 [MiaoToastGhost] 幽灵层，两者同时进行——这样不会出现
 * 「先淡出、留个空档、再收拢」的两段式观感。
 *
 * [visibleState] 以 `false → true` 起步：这样条目**首次组合时**就天然处于
 * `currentState = false, targetState = true` 的进场状态，无需额外的 `LaunchedEffect` 触发。
 */
private class MiaoToastEntry(val toast: MiaoToast) {
    /** 进场过渡状态（fade + scale） */
    val visibleState = MutableTransitionState(false).apply { targetState = true }

    /** 最近一次布局后的位置（root 坐标）：退场时用它给幽灵层定位 */
    var boundsInRoot: Rect = Rect.Zero
}

/**
 * 退场中的「幽灵」气泡：已从 [Column] 里摘掉（不再占布局、不再参与补位），
 * 但仍在原位置淡出。
 *
 * 为什么要单独一层：在 `Column` 里保留占位会让补位等到淡出结束才开始（两段式）；
 * 而如果把气泡直接删掉又没有淡出。所以把「占位」和「淡出」拆开——
 * 布局立刻收拢（其余条目由 [Modifier.animateBounds] 平滑补位），
 * 气泡本体按退场瞬间的位置绝对定位、独立淡出。DialogX 的 PopTip 是独立窗口，天然就是这种行为。
 */
private class MiaoToastGhost(
    val toast: MiaoToast,
    /** 幽灵左上角相对宿主 [Box] 的位置 */
    val offsetInHost: IntOffset,
    /** 幽灵宽度（与被摘掉的那一行完全一致，保证气泡在栈里居中） */
    val widthPx: Int,
    /** 这条的淡出时长：正常到期与「被上限挤掉」用不同节奏 */
    val exitMillis: Int = ToastExitMillis,
    /** 缩放终点（同上） */
    val exitScale: Float = ToastExitScale,
    /**
     * 退场期间向栈外平移的距离（带符号：负值向上、正值向下）。
     * 正常到期为 0（原地消散）；被上限挤掉时朝远离锚点的方向移动。
     */
    val liftDp: Dp = 0.dp,
)

/**
 * PopTip 风格 toast 的配色。
 *
 * 默认由 [miaoToastColors] 从当前 `MaterialTheme` 取色，因此会自动跟随 App 的
 * 主题色、明暗模式（含 Material You 动态取色 / AMOLED 纯黑）；
 * 需要特殊配色时可以显式传入自己的实例覆盖。
 */
data class MiaoToastColors(
    /** 气泡背景（不透明） */
    val container: Color,
    /** 正文文字 */
    val content: Color,
    /** 动作按钮文字 */
    val action: Color,
)

/**
 * 按当前 `MaterialTheme.colorScheme` 取 toast 配色。
 *
 * 映射关系（都取自 M3 语义 token，才能跟着主题/动态取色/AMOLED 一起变）：
 * - 容器 → `surfaceContainerHigh`：比 `surface` 高一档，浅色下是浅灰、深色下是深灰，
 *   AMOLED 纯黑背景上也能和背景区分开（这正是 PopTip 在 MaterialYouStyle 下的层次关系）；
 * - 正文 → `onSurface`：与容器天然成对，对比度由主题保证；
 * - 动作按钮 → `primary`：即 App 的主题色，和界面里的按钮/选中态同一套色。
 *
 * 必须在 `MaterialTheme` 之内调用（`MiaoToastHost` 挂在 `BilimiaoTheme` 内）。
 */
@Composable
fun miaoToastColors(): MiaoToastColors {
    val scheme = MaterialTheme.colorScheme
    return MiaoToastColors(
        container = scheme.surfaceContainerHigh,
        content = scheme.onSurface,
        action = scheme.primary,
    )
}

/**
 * 创建**每个宿主一份**的 toast state。
 *
 * 与 sonner 的 `rememberToasterState()` 用法一致：state 随宿主组合创建、随宿主组合销毁，
 * 因此换宿主（例如 Activity 重建）自然得到一个全新的空列表，不需要任何跨宿主的清理逻辑。
 */
@Composable
fun rememberMiaoToastState(): MiaoToastState = remember { MiaoToastState() }

/**
 * toast 宿主（对应 sonner 的 `Toaster`）。
 *
 * 逻辑上只订阅 [MiaoToastState.toasts]，把列表按「旧 → 新」画出来：数据层不管动画与计时，
 * DialogX 只贡献**样式与动画**，不参与任何布局/位置逻辑。
 *
 * 位置**由每条 toast 自己声明**（`MiaoToast.position`）：宿主无条件渲染上下两条栈——
 * 底部栈（`ToastPosition.Bottom`）与顶部栈（`ToastPosition.Top`），空的那条不绘制任何节点。
 * 两条栈共用同一套动画、间距、计时与幽灵层机制，只是镜像方向不同：
 * - 底部栈：最新一条贴底，来了新的旧条目**向上**让位，退场后上方条目**向下**补位；
 * - 顶部栈：最新一条贴顶，来了新的旧条目**向下**让位，退场后下方条目**向上**补位。
 *
 * 视觉（对齐老项目 DialogX PopTip 的 MaterialYouStyle）：胶囊形状、不透明背景、无描边、2dp 轻阴影，
 * **颜色取自当前 `MaterialTheme`**（见 [miaoToastColors]：surfaceContainerHigh / onSurface / primary），
 * 底部栈距安全区底边 [ToastBottomMargin]（PopTip 的 100dp），顶部栈只留 [ToastTopMargin]（贴近屏幕边缘），
 * 左右各留 [ToastHorizontalMargin]。
 *
 * 渲染方案是 [LookaheadScope] + [Column] + 逐条 [AnimatedVisibility] + 幽灵层：
 * - 进场由每条自己的 [AnimatedVisibility] 承担：`fadeIn + 0.92 → 1 scaleIn`（[ToastEnterMillis]）；
 * - **退场不再"原地淡出再收拢"**（那会留出一段空档、看起来是两段运动）：数据层一移除，
 *   条目**立刻**从 [Column] 摘掉，补位位移立刻由 [Modifier.animateBounds] 开始，
 *   同时气泡被转成按原位绝对定位的幽灵（[MiaoToastGhostBubble]）淡出 + 微缩（[ToastExitMillis]）。
 *   收拢与消散同一帧开始、同时结束；
 * - 旧条目被挤动 / 补位时的位移由每条根节点的 [Modifier.animateBounds] 承担：它以
 *   [LookaheadScope] 为坐标空间，把「上一帧 bounds → 目标 bounds」的差值插值成动画，
 *   因此新增一条时旧条目是**平滑上移**、移除一条时其余条目是**平滑下移**，而不是被
 *   [Column] 重新布局后瞬间跳到新位置。
 *
 * 为什么用 [Modifier.animateBounds] 而不是 `LazyColumn` + `Modifier.animateItem`：
 * `LazyColumn` 内部走 `Modifier.scrollableArea(...)` → `clipScrollableContainer(vertical)`
 * （与 `userScrollEnabled` 无关），会把最外侧气泡那 2dp 阴影裁掉。用 [Column] 就没有这层裁剪，
 * 但 [Column] 本身不会给被挤动的旧条目做位移动画，于是位移交给 [Modifier.animateBounds]。
 * 该 API 没有 `clipToBounds` 参数、也不会裁剪子节点，所以换成它**不会重新引入阴影被裁**的问题。
 *
 * 时间节奏：[Modifier.animateBounds] 的 [BoundsTransform] 用 [ToastShiftMillis] +
 * [PopTipDecelerate]（DialogX 的 `DecelerateInterpolator(2f)`，与 PopTip 的 `moveBack` 一致）；
 * 进场同曲线但更短（[ToastEnterMillis]），退场用 [PopTipAccelerateDecelerate] 且最短
 * （[ToastExitMillis]）——「进慢出快」，消失干脆、出现有余量。
 *
 * 首次出现不会多播一次位移：`animateBounds` 在节点的**第一帧**只记录 bounds、不启动动画
 * （内部 `isPending` 只有在「目标 bounds 与上一次记录不同」时才置位），所以新条目只有自己的
 * fade + scale，位移动画只作用于被挤动的旧条目；同理 [Column] 的尺寸变化也不会被动画放大。
 *
 * 通常挂在页面最外层（弹层/播放器之上），例如 `MainComposeHost` 的末尾。
 *
 * @param state 由 [rememberMiaoToastState] 创建，并通过 `GlobalToaster.init(state)` 注入全局入口
 */
@Composable
fun MiaoToastHost(
    state: MiaoToastState,
    modifier: Modifier = Modifier,
    colors: MiaoToastColors = miaoToastColors(),
) {
    val toasts by state.toasts.collectAsState()

    // 渲染条目列表（**旧 → 新**，最新追加在末尾）。它是两条 [Column] 真正的数据源：
    // 数据层的 toast 一被移除，条目就立刻从这里摘掉（补位马上开始）。
    val entries = remember(state) { mutableStateListOf<MiaoToastEntry>() }

    // 退场中的幽灵气泡（已不占布局，只负责在原位淡出）
    val ghosts = remember(state) { mutableStateListOf<MiaoToastGhost>() }

    // 宿主自身的 root 坐标：用来把条目测量到的 root 坐标换算成幽灵在本 Box 内的偏移
    var hostOriginInRoot by remember { mutableStateOf(Offset.Zero) }

    // 渲染条目与数据层对齐
    LaunchedEffect(toasts) {
        val aliveIds = toasts.mapTo(mutableSetOf()) { it.id }

        // 本次更新新增的 toast。上限是按位置分别计的，所以只有「同位置」有新条目进来时，
        // 同位置里消失的那条才是被上限挤掉的 → 用更短促的退场（主动让位）。
        val added = toasts.filter { toast -> entries.none { it.toast.id == toast.id } }
        val addedPositions = added.mapTo(mutableSetOf()) { it.position }

        // 数据层已移除的条目：立刻摘出渲染列表，同时把气泡转成幽灵继续淡出。
        // 注意顺序：先摘（触发 Column 收拢 + animateBounds 补位），再把幽灵加进叠层，
        // 两者从同一帧开始、同时结束，因此不会再出现「先淡出、留空档、再收拢」的两段式。
        entries.filter { it.toast.id !in aliveIds }.forEach { entry ->
            val bounds = entry.boundsInRoot
            if (bounds != Rect.Zero) {
                val overflowed = entry.toast.position in addedPositions
                ghosts.add(
                    MiaoToastGhost(
                        toast = entry.toast,
                        offsetInHost = IntOffset(
                            (bounds.left - hostOriginInRoot.x).roundToInt(),
                            (bounds.top - hostOriginInRoot.y).roundToInt(),
                        ),
                        widthPx = bounds.width.roundToInt(),
                        // 被上限挤掉的那条：短促退场 + 朝栈外平移，观感上是它自己先让位、飞出栈外
                        exitMillis = if (overflowed) ToastOverflowExitMillis else ToastExitMillis,
                        exitScale = if (overflowed) ToastOverflowExitScale else ToastExitScale,
                        liftDp = if (overflowed) {
                            // 方向按**这条 toast 自己的 position** 判定（不再看宿主级锚点）：
                            // 底部栈向上飞（负值），顶部栈向下掉（正值）
                            if (entry.toast.position == ToastPosition.Top) {
                                ToastOverflowLift
                            } else {
                                -ToastOverflowLift
                            }
                        } else {
                            0.dp
                        },
                    )
                )
            }
            entries.remove(entry)
        }

        // 新条目追加到末尾 = 最新一条落在栈尾（配合 Column 的顺序规则）
        added.forEach { entries.add(MiaoToastEntry(it)) }
    }

    // 安全区 = 状态栏 ∪ 刘海/挖孔 ∪ 输入法：顶部贴近屏幕边缘、键盘弹出时底部气泡自动抬高
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()

    // 不做「空列表提前返回」：没有条目时每条栈都不绘制任何节点，也不消费触摸。
    // LookaheadScope 不引入新的 Layout（子节点的父节点与不加它时相同），只让子树先做一次
    // lookahead 布局，从而让 animateBounds 能拿到「目标位置」，再把它插值成动画。
    LookaheadScope {
        // 提前取出 receiver：Column 的 content lambda 里 receiver 是 ColumnScope，
        // 已经不再是 LookaheadScope，只能靠这个局部变量传进去
        val lookaheadScope = this
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { hostOriginInRoot = it.positionInRoot() }
        ) {
            // 位置由每条 toast 自己声明：无条件渲染上下两条栈（空栈不绘制任何节点）
            MiaoToastStack(
                entries = entries.filter { it.toast.position == ToastPosition.Bottom },
                atTop = false,
                lookaheadScope = lookaheadScope,
                safeInsets = safeInsets,
                state = state,
                colors = colors,
            )
            MiaoToastStack(
                entries = entries.filter { it.toast.position == ToastPosition.Top },
                atTop = true,
                lookaheadScope = lookaheadScope,
                safeInsets = safeInsets,
                state = state,
                colors = colors,
            )

            // 幽灵层保持在最后：画在两条栈之上，与补位位移同时进行，让气泡「一边淡出、栈一边收拢」。
            // 幽灵本身绝对定位，因此天然覆盖上下两条栈。
            ghosts.forEach { ghost ->
                key(ghost.toast.id) {
                    MiaoToastGhostBubble(
                        ghost = ghost,
                        colors = colors,
                        onFinished = { ghosts.remove(ghost) },
                    )
                }
            }
        }
    }
}

/**
 * 一条栈：把同一位置（[atTop] 为 true 是顶部栈，否则底部栈）的条目画成一列。
 *
 * 两条栈的差异只有三处，其余（间距、留白、动画）完全共用：
 * - 锚点：顶部贴 [Alignment.TopCenter]、底部贴 [Alignment.BottomCenter]；
 * - 安全区偏移：顶部栈扣顶部安全区、底部栈扣底部安全区（都减掉 [ToastShadowRoom]）；
 * - 条目顺序：顶部栈用 [List.asReversed]，让最新一条贴顶。
 *
 * @param entries 属于这条栈的条目，**顺序为「旧 → 新」**
 * @param atTop 是否顶部栈
 * @param lookaheadScope 宿主的 lookahead 空间，供 [Modifier.animateBounds] 插值位移
 * @param safeInsets 安全区（状态栏/导航栏 ∪ 输入法）
 */
@Composable
private fun BoxScope.MiaoToastStack(
    entries: List<MiaoToastEntry>,
    atTop: Boolean,
    lookaheadScope: LookaheadScope,
    safeInsets: PaddingValues,
    state: MiaoToastState,
    colors: MiaoToastColors,
) {
    // 空栈什么都不画：没有条目时 Column 天然不产生节点，也不消费触摸
    if (entries.isEmpty()) return

    Column(
        modifier = Modifier
            .align(if (atTop) Alignment.TopCenter else Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(
                start = ToastHorizontalMargin,
                end = ToastHorizontalMargin,
                // 条目自带 ToastShadowRoom 的留白，扣掉后气泡的视觉边缘仍恰好贴着设定的边距
                top = if (atTop) {
                    safeInsets.calculateTopPadding() + ToastTopMargin - ToastShadowRoom
                } else {
                    0.dp
                },
                bottom = if (atTop) {
                    0.dp
                } else {
                    safeInsets.calculateBottomPadding() + ToastBottomMargin - ToastShadowRoom
                },
            ),
        verticalArrangement = Arrangement.spacedBy(ToastStackGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 条目顺序为「旧 → 新」：底部栈最旧的在最上面、最新的贴底；
        // 顶部栈反过来（最新的一条贴安全区顶部，旧的往下排）
        val orderedEntries = if (atTop) entries.asReversed() else entries
        orderedEntries.forEach { entry ->
            // key：同一条 toast 在栈里的位置变化时复用同一棵子树，
            // 否则 animateBounds 的节点会被重建、上一帧 bounds 丢失，动画就没了
            key(entry.toast.id) {
                Box(
                    modifier = Modifier
                        // 被挤动的旧条目「滑」到新位置，而不是被 Column 布局后瞬移。
                        // 统一 300ms、PopTipDecelerate，与 PopTip 的 moveBack 同一节奏
                        .animateBounds(
                            lookaheadScope = lookaheadScope,
                            boundsTransform = BoundsTransform { _, _ ->
                                tween<Rect>(
                                    durationMillis = ToastShiftMillis,
                                    // 与 PopTip 的 moveBack 一致：DecelerateInterpolator(2f)
                                    easing = PopTipDecelerate,
                                )
                            },
                        )
                        // 记录当前位置：退场时幽灵层要按这个位置继续淡出
                        .onGloballyPositioned { entry.boundsInRoot = it.boundsInRoot() }
                ) {
                    MiaoToastItem(
                        entry = entry,
                        state = state,
                        colors = colors,
                    )
                }
            }
        }
    }
}

/**
 * 栈内单条 toast：只负责**进场动画**与独立计时。
 *
 * 退场不在这里做：数据层一移除，条目就从 [Column] 里摘掉（见宿主 `LaunchedEffect(toasts)`），
 * 补位位移由栈加在根节点上的 [Modifier.animateBounds] 完成，淡出交给 [MiaoToastGhostBubble]。
 */
@Composable
private fun MiaoToastItem(
    entry: MiaoToastEntry,
    state: MiaoToastState,
    colors: MiaoToastColors,
) {
    val toast = entry.toast

    // 独立计时：到点只移除自己这一条，其余条目不受影响。
    // 放在条目内部（而不是宿主级的 Job 表）：Column 不会回收条目，一条 toast 一个
    // `LaunchedEffect(toast.id)` 最直观，key 稳定也不会被重组打断。
    LaunchedEffect(toast.id) {
        delay(toast.duration)
        state.dismiss(toast)
    }

    AnimatedVisibility(
        visibleState = entry.visibleState,
        // 进场：淡入 + 轻微放大（0.92 → 1）。0.92 比 PopTip 原始的 0.9 更收敛，
        // 在胶囊这种小尺寸上不会显得「弹一下」；原点居中，缩放不偏向某一侧。
        enter = fadeIn(tween(ToastEnterMillis, easing = PopTipDecelerate)) +
            scaleIn(
                animationSpec = tween(ToastEnterMillis, easing = PopTipDecelerate),
                initialScale = 0.92f,
                transformOrigin = TransformOrigin.Center,
            ),
        // 条目被移除时是直接摘掉的（淡出由幽灵层承担），这里的 exit 永远不会被触发
        exit = fadeOut(tween(ToastExitMillis, easing = PopTipAccelerateDecelerate)),
    ) {
        Box(
            // 留白让阴影落在过渡图层边界内（见 ToastShadowRoom）
            modifier = Modifier
                .fillMaxWidth()
                .padding(ToastShadowRoom),
            contentAlignment = Alignment.Center,
        ) {
            MiaoToastBubble(
                toast = toast,
                colors = colors,
                onActionClick = {
                    try {
                        toast.action?.onClick?.invoke()
                    } catch (e: Exception) {
                        // 动作回调来自业务方，失败不能让 UI 崩溃（带上堆栈便于定位）
                        MiaoLogger("MiaoToast").e(
                            "toast 动作回调执行失败",
                            e.stackTraceToString(),
                        )
                    }
                    // 先执行回调，再移除这一条（只移除自己，其余继续计时）
                    state.dismiss(toast)
                },
            )
        }
    }
}

/**
 * 退场幽灵：按摘出瞬间的位置绝对定位，淡出 + 缩放 +（可选）朝栈外平移，结束后通知宿主销毁自己。
 *
 * 因为不参与布局，补位位移（[Modifier.animateBounds]）可以与它同时进行——
 * 这就是「气泡一边消散、栈一边收拢」的实现方式，不再是先淡出再留空档再收拢。
 *
 * 三件事共用同一个 [Animatable] 进度（同一条曲线、同一时长）：
 * - 正常到期：淡出 + 缩到 [ToastExitScale]，原地消散；
 * - 被上限挤掉：更短（[ToastOverflowExitMillis]）、更狠（[ToastOverflowExitScale]），
 *   并沿 [MiaoToastGhost.liftDp] 朝远离锚点的方向平移 [ToastOverflowLift]，像被挤出栈外。
 *
 * 幽灵不可交互：动作按钮不挂 clickable，避免退场期间被再点一次。
 */
@Composable
private fun MiaoToastGhostBubble(
    ghost: MiaoToastGhost,
    colors: MiaoToastColors,
    onFinished: () -> Unit,
) {
    val progress = remember(ghost) { Animatable(0f) }
    LaunchedEffect(ghost) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(ghost.exitMillis, easing = PopTipAccelerateDecelerate),
        )
        onFinished()
    }
    val widthDp = with(LocalDensity.current) { ghost.widthPx.toDp() }
    val liftPx = with(LocalDensity.current) { ghost.liftDp.toPx() }
    Box(
        modifier = Modifier
            .offset { ghost.offsetInHost }
            .width(widthDp)
            .graphicsLayer {
                alpha = 1f - progress.value
                // 淡出 + 收小：与进场 0.92 → 1 的放大互相呼应，消失时是「收掉」而不是「被切掉」
                val scale = 1f - (1f - ghost.exitScale) * progress.value
                scaleX = scale
                scaleY = scale
                // 与淡出/缩放同步的外移：被挤掉时朝栈外飞出去，正常到期为 0（原地消散）
                translationY = liftPx * progress.value
                transformOrigin = TransformOrigin.Center
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ToastShadowRoom),
            contentAlignment = Alignment.Center,
        ) {
            MiaoToastBubble(
                toast = ghost.toast,
                colors = colors,
                interactive = false,
                onActionClick = {},
            )
        }
    }
}

/**
 * 单个胶囊气泡。
 *
 * 除动作按钮外不添加任何 pointer 输入修饰符，保证气泡不消费触摸（点击穿透）。
 *
 * @param interactive 是否允许点击动作按钮；退场幽灵传 false（淡出期间不应再触发业务动作）
 */
@Composable
private fun MiaoToastBubble(
    toast: MiaoToast,
    colors: MiaoToastColors,
    interactive: Boolean = true,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            // 显式写 clip = false：让「裁剪」职责只由下面的 .clip() 承担，修饰符链语义单一。
            // 注：Modifier.shadow 的 clip 参数只决定「内容是否被 shape 裁剪」，阴影本身由 graphics layer
            // 绘制在节点边界之外，所以本来就不会被后续的 .clip() 裁掉——这里不是在修「阴影被裁掉」的 bug。
            .shadow(elevation = 2.dp, shape = ToastShape, clip = false)
            .clip(ToastShape)
            .background(colors.container)
            .heightIn(min = ToastMinHeight)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = toast.message,
            // fill = false：气泡仍按内容宽度包裹，同时保证长文本换行后动作按钮不会被挤掉
            modifier = Modifier.weight(1f, fill = false),
            color = colors.content,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
        )
        val action = toast.action
        if (action != null) {
            // 一次性守卫：退场的 200ms 内气泡仍可见，避免快速双击重复触发业务动作
            var fired by remember(toast.id) { mutableStateOf(false) }
            Text(
                text = action.text,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .clip(ToastShape)
                    // 幽灵（退场中的气泡）不可点击，避免淡出期间被再触发一次
                    .then(
                        if (interactive) {
                            Modifier.clickable {
                                if (fired) return@clickable
                                fired = true
                                onActionClick()
                            }
                        } else {
                            Modifier
                        }
                    )
                    // 比正文更紧凑：13sp + 6dp/8dp 内边距，视觉上像气泡右侧的一个文字按键，
                    // 而不是把整条气泡撑高、撑宽的独立按钮
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                color = colors.action,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
