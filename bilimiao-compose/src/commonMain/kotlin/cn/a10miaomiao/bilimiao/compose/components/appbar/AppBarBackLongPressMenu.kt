package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard
import kotlin.math.max
import kotlin.math.min

/**
 * AppBar 返回键「长按」面板中的一个动作。
 *
 * 动作为空表示长按不弹面板（转而走 [NavigationIcon] 的 `onLongClick`）。
 * 具体提供哪些动作由宿主按当前状态动态决定，例如「播放器是否正在显示」：
 * 播放中提供「返回首页 / 退出播放」，否则不提供动作、长按直接返回首页。
 *
 * @param icon 格子里的图标，由调用方传入，本组件不引入额外图标依赖
 * @param title 格子图标下方的文字（单行，超出省略）
 * @param onClick 点击格子后的动作，执行前会先关闭面板
 */
data class AppBarBackAction(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit,
)

/**
 * 弹层相对 AppBar 的弹出方向。
 *
 * - 竖屏 AppBar 是屏幕底部的横条，弹层向上弹（[AboveAppBar]）；
 * - 横屏 AppBar 是屏幕左侧的竖条，弹层向右弹（[RightOfAppBar]）。
 *
 * 一级菜单弹层（`AppBarMenuTree.kt` 的 `SharedRootMenuPopup`）与返回键长按网格面板
 * 共用这一个枚举：定位方向、最大高度策略、缩放原点三处逻辑因此全仓只有一份实现。
 *
 * 之所以不用 `DropdownMenu` / `ExposedDropdownMenuBox`：
 * Google 的弹出菜单强制保留离屏边距（`MenuVerticalMargin` 等），既不能精确贴住锚点，
 * 也无法把面板宽度控制在「列数 × 格子宽」这种确定值上，外观和一级菜单弹层对不齐。
 * 因此这里沿用本仓库一级菜单弹层已验证过的 `Popup` + `AnimatedVisibility` 方案。
 */
enum class AppBarPopupPlacement {
    AboveAppBar,
    RightOfAppBar,
}

/** 网格格子宽度：图标 + 文字统一按此宽度居中，多行多列才能对齐 */
private val BackMenuCellWidth = 76.dp

/** 格子内边距（左右各一份，格子内容宽 = 格子宽 - 2 × 此值 = 64dp） */
private val BackMenuCellPadding = 6.dp

/** 格子内图标尺寸 */
private val BackMenuIconSize = 22.dp

/** 图标与文字之间的间距 */
private val BackMenuIconTextGap = 3.dp

/** 格子文字大小 */
private val BackMenuTextSize = 11.sp

/** 格子点击水波纹的圆角（只作用于格子，卡片的圆角由 [MiaoOutlinedCard] 决定） */
private val BackMenuCellCornerRadius = 6.dp

/** 默认列数 */
private const val BackMenuDefaultColumns = 2

/** 安全区宽度足够放下 3 列时使用的列数 */
private const val BackMenuWideColumns = 3

/**
 * 返回键长按网格面板。
 *
 * 布局：每格 = 图标（22dp）+ 3dp 间距 + 文字（11sp，单行省略），整格可点击并带圆角 ripple。
 * 外观：与一级菜单弹层一致（[MiaoOutlinedCard] + 淡入 + `scaleIn` 弹簧动画），
 * 缩放原点朝向锚点（底部 AppBar 向上弹时原点在底边，左侧栏向右弹时原点在左边）。
 *
 * @param visible 是否可见（调用方控制，动作变空或图标不再是返回箭头时应为 false）
 * @param actions 面板动作列表，为空时不渲染内容
 * @param anchor 返回键在窗口中的矩形（由 [NavigationIcon] 上报）
 * @param placement 弹层弹出方向
 * @param onDismiss 请求关闭面板（点击面板外、点击动作、动作清空时调用）
 */
@Composable
internal fun AppBarBackLongPressPopup(
    visible: Boolean,
    actions: List<AppBarBackAction>,
    anchor: MenuAnchor?,
    placement: AppBarPopupPlacement,
    onDismiss: () -> Unit,
) {
    val visibleState = remember { MutableTransitionState(false) }
    // 退出动画期间仍要渲染内容，因此缓存最后一次非空的动作与锚点。
    // 注意：这里每次重组都会把参数赋给快照状态，是否引发额外重组取决于参数是否「结构相等」——
    // [AppBarBackAction] 是含 onClick lambda 的 data class，equals 会比到 lambda 引用，
    // 所以前提是调用方用 remember 复用同一个 list 实例；若调用方每次重组都内联构造新 lambda，
    // 这里的赋值会持续把快照状态写成「不等」而触发重组
    var renderedActions by remember { mutableStateOf(actions) }
    var renderedAnchor by remember { mutableStateOf(anchor) }
    if (actions.isNotEmpty()) {
        renderedActions = actions
    }
    if (anchor != null) {
        renderedAnchor = anchor
    }

    val canShow = visible && actions.isNotEmpty() && anchor != null
    visibleState.targetState = canShow
    if (!visibleState.currentState && !visibleState.targetState) {
        return
    }
    val popupActions = renderedActions
    val popupAnchor = renderedAnchor ?: return

    val density = LocalDensity.current
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val safeTopPx = with(density) { safePadding.calculateTopPadding().roundToPx() }
    val safeBottomPx = with(density) { safePadding.calculateBottomPadding().roundToPx() }
    val safeLeftPx = with(density) { safePadding.calculateLeftPadding(LayoutDirection.Ltr).roundToPx() }
    val safeRightPx = with(density) { safePadding.calculateRightPadding(LayoutDirection.Ltr).roundToPx() }

    // 安全区内的可用宽度；窗口宽度尚未测出（桌面端首帧）时为 null，表示「未知」
    val windowWidthPx = LocalWindowInfo.current.containerSize.width
    val availableWidthDp = windowWidthPx
        .takeIf { it > 0 }
        ?.let { widthPx ->
            with(density) {
                (widthPx.toDp() - safeLeftPx.toDp() - safeRightPx.toDp()).coerceAtLeast(0.dp)
            }
        }
        ?.takeIf { it > 0.dp }

    // 列数自适应：默认 2 列，安全区内放得下 3 列时用 3 列；
    // 动作数少于列数时按动作数收缩，避免出现空格子把面板撑宽。
    // 阈值 = 3 列的内容宽（3 × 格子宽）：面板没有额外的左右内边距（视觉留白由格子
    // 自身的 BackMenuCellPadding 承担），所以卡片总宽就等于内容宽，判定通过即一定放得下
    val wideColumns = if (
        availableWidthDp != null &&
        availableWidthDp >= BackMenuCellWidth * BackMenuWideColumns
    ) {
        BackMenuWideColumns
    } else {
        BackMenuDefaultColumns
    }
    val columns = min(wideColumns, popupActions.size.coerceAtLeast(1))
    // 面板宽度只由「列数 × 格子宽」得出（确定的小数值）：卡片本身不加内边距，
    // 格子的 [BackMenuCellPadding] 已经提供了图标/文字四周的视觉留白，
    // 因此这里绝不能再额外叠加内边距，否则卡片会比内容宽出一段不对称的空白。
    // 可用宽度已知时再兜底 clamp 一次
    val popupWidthDp = (BackMenuCellWidth * columns)
        .let { width -> availableWidthDp?.let { width.coerceAtMost(it) } ?: width }

    // Popup 定位回调实测到的「弹层所在窗口」高度（px）。
    // 0 表示尚未测得（首帧），必须当成「未知」显式处理，绝不能当哨兵参与换算。
    var measuredWindowHeightPx by remember(placement) { mutableIntStateOf(0) }

    val popupPositionProvider = remember(
        placement,
        popupAnchor,
        safeTopPx,
        safeBottomPx,
        safeLeftPx,
        safeRightPx,
    ) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                // 记录窗口真实高度，供后续帧计算最大高度（只在变化时写入，避免多余重组）
                if (measuredWindowHeightPx != windowSize.height) {
                    measuredWindowHeightPx = windowSize.height
                }
                val popupWidth = popupContentSize.width
                val popupHeight = popupContentSize.height
                return when (placement) {
                    // 竖屏：底栏面板向上弹，底边贴锚点顶边，水平以锚点为中心并 clamp 在安全区
                    AppBarPopupPlacement.AboveAppBar -> {
                        val anchorCenterX = popupAnchor.x + popupAnchor.width / 2
                        val preferredLeft = anchorCenterX - popupWidth / 2
                        val maxLeft = max(windowSize.width - safeRightPx - popupWidth, safeLeftPx)
                        IntOffset(
                            x = preferredLeft.coerceIn(safeLeftPx, maxLeft),
                            y = max(popupAnchor.y - popupHeight, safeTopPx),
                        )
                    }
                    // 横屏：侧栏面板向右弹，左边贴锚点右边，垂直以锚点为中心并 clamp 在安全区
                    AppBarPopupPlacement.RightOfAppBar -> {
                        val anchorCenterY = popupAnchor.y + popupAnchor.height / 2
                        val preferredTop = anchorCenterY - popupHeight / 2
                        val maxTop = max(windowSize.height - safeBottomPx - popupHeight, safeTopPx)
                        IntOffset(
                            x = popupAnchor.x + popupAnchor.width,
                            y = preferredTop.coerceIn(safeTopPx, maxTop),
                        )
                    }
                }
            }
        }
    }

    // 面板最大高度只能来自真实可用空间：
    // ① 优先用 Popup 定位回调实测到的窗口高度（与 Popup 坐标系一致，最准确）；
    // ② 首帧还没测得时回退到 LocalWindowInfo 的窗口高度；
    // ③ 两者都拿不到时交由 resolveRootPopupMaxHeight 返回 null（= 不做高度限制，
    //    这里用 Dp.Unspecified 表示），由布局自身按窗口约束测量。
    // 旧实现曾用 Int.MAX_VALUE 当「窗口未知」的哨兵，dp↔px 往返后得到 ≈2^31 的像素值，
    // 远超 Constraints 能打包的尺寸上限，触发线上崩溃
    // "Can't represent a width of 0 and height of 2147483520 in Constraints"。
    val windowHeightPx = measuredWindowHeightPx.takeIf { it > 0 }
        ?: LocalWindowInfo.current.containerSize.height.takeIf { it > 0 }
    val popupMaxHeightDp = resolveRootPopupMaxHeight(
        placement = placement,
        anchorTopPx = popupAnchor.y,
        windowHeightPx = windowHeightPx,
        safeTopPx = safeTopPx,
        safeBottomPx = safeBottomPx,
        density = density,
    )

    val popupTransformOrigin = remember(placement) {
        when (placement) {
            AppBarPopupPlacement.AboveAppBar ->
                TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 1f)
            AppBarPopupPlacement.RightOfAppBar ->
                TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0.5f)
        }
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn() + scaleIn(
                initialScale = 0.82f,
                transformOrigin = popupTransformOrigin,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 520f,
                ),
            ),
            exit = fadeOut() + scaleOut(
                targetScale = 0.9f,
                transformOrigin = popupTransformOrigin,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = 700f,
                ),
            ),
        ) {
            MiaoOutlinedCard {
                Column(
                    modifier = Modifier
                        .width(popupWidthDp)
                        // Dp.Unspecified 表示窗口高度未知，此时不给高度限制
                        .heightIn(max = popupMaxHeightDp ?: Dp.Unspecified)
                        .verticalScroll(rememberScrollState()),
                ) {
                    popupActions.chunked(columns).forEach { rowActions ->
                        // 居中对齐：最后一行可能不满列数，左对齐会让它看起来偏左
                        Row(horizontalArrangement = Arrangement.Center) {
                            rowActions.forEach { action ->
                                BackMenuCell(
                                    action = action,
                                    onClick = {
                                        // 先关面板再执行动作：动作里通常会整页跳转，
                                        // 若先执行动作，面板可能跟着旧页面多留一帧
                                        onDismiss()
                                        action.onClick()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 网格中的单个格子：图标在上、文字在下，整格宽度固定为 [BackMenuCellWidth]，可点击。
 */
@Composable
private fun BackMenuCell(
    action: AppBarBackAction,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .width(BackMenuCellWidth)
            .clip(RoundedCornerShape(BackMenuCellCornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(BackMenuCellPadding),
        // Column 高度由内容撑开（没有固定高度），因此不设 verticalArrangement：
        // 无固定高度时 Center 不会产生任何效果
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.title,
            modifier = Modifier.size(BackMenuIconSize),
        )
        Spacer(modifier = Modifier.height(BackMenuIconTextGap))
        Text(
            text = action.title,
            fontSize = BackMenuTextSize,
            lineHeight = BackMenuTextSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
