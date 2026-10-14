package cn.a10miaomiao.bilimiao.compose.components.dialogs

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.BackHandler
import cn.a10miaomiao.bilimiao.compose.common.isCompactWindow
import kotlinx.coroutines.launch

/**
 * 弹层最大宽度（与原 AnyPopDialog 的 600.dp 保持一致）。
 */
private val SheetMaxWidth = 600.dp

/**
 * 弹层圆角。
 */
private val SheetCornerRadius = 10.dp

/**
 * 弹层控制器（模块内部使用）：[AutoSheetDialogHost] 通过它请求「播放关闭动画」。
 *
 * 关闭是一个带退场动画的过程，所以这里只表达「请求」：[close] 之后弹层会完整播放关闭动画，
 * 动画结束后才回调 [AutoSheetDialog] 的 `onDismiss`。
 */
@Stable
internal class AutoSheetDialogState {

    internal var closeRequested by mutableStateOf(false)

    /** 请求关闭：播放关闭动画，动画结束后回调 `onDismiss` */
    fun close() {
        closeRequested = true
    }
}

@Composable
internal fun rememberAutoSheetDialogState(): AutoSheetDialogState =
    remember { AutoSheetDialogState() }

/**
 * 自适应弹层：两种状态共用同一个官方 Material3 [ModalBottomSheet]，因此滑入/滑出动画、下滑手势、
 * 遮罩点击、返回键行为完全一致，区别只在“内容显示区域”：
 * - Compact 窗口（手机竖屏等宽度 < 600dp）：弹层高度贴合内容，显示在屏幕**底部**；
 * - 非 Compact 窗口（平板、桌面端）：弹层本体撑满整屏且透明，内容以圆角卡片显示在屏幕**中间**。
 *
 * 居中状态下，卡片四周的透明区域与两侧遮罩行为一致：点击即关闭（同样会先询问 [onPreDismiss]），
 * 下滑同样可以关闭，只有卡片内部的点击不会穿透到遮罩。
 *
 * 另外，官方 [ModalBottomSheet] 的收起动画默认走 Material 的 FastEffects 曲线（stiffness 3800 的
 * 弹簧），收起时会显得过于急促；这里让收起复用打开时的弹簧曲线，做到开关对称。
 *
 * @param modifier 作用于弹层内容容器（调用方常用来设置背景色、最大高度等，会被裁剪进卡片圆角内）
 * @param state 控制器：由 [AutoSheetDialogHost] 持有，用于请求播放关闭动画
 * @param onDismiss 弹层真正关闭后回调（动画结束后）
 * @param onPreDismiss 关闭前的拦截回调，返回 true 表示已被调用方消费（例如先收起表情面板、
 *                     先回退弹层内部页面栈），此时弹层保持打开
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AutoSheetDialog(
    modifier: Modifier = Modifier,
    state: AutoSheetDialogState = rememberAutoSheetDialogState(),
    onDismiss: () -> Unit,
    onPreDismiss: (() -> Boolean)? = null,
    content: @Composable () -> Unit
) {
    val isCompact = isCompactWindow()
    val scope = rememberCoroutineScope()
    // confirmValueChange 会被 remember 在 sheetState 中，必须使用稳定的 lambda 并读取最新回调
    val currentOnPreDismiss by rememberUpdatedState(onPreDismiss)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    // 控制器请求的关闭：调用方已明确要关，直接放行，不再询问 onPreDismiss
    var forceClosing by remember { mutableStateOf(false) }

    // 只替换收起动画用的 fastEffectsSpec，其余动效沿用应用主题
    val contentMotionScheme = MaterialTheme.motionScheme
    val sheetMotionScheme = remember(contentMotionScheme) {
        object : MotionScheme {
            override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> =
                contentMotionScheme.defaultSpatialSpec()

            override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> =
                contentMotionScheme.fastSpatialSpec()

            override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> =
                contentMotionScheme.slowSpatialSpec()

            override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> =
                contentMotionScheme.defaultEffectsSpec()

            override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> =
                contentMotionScheme.defaultSpatialSpec()

            override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> =
                contentMotionScheme.slowEffectsSpec()
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        // 点击遮罩、下滑关闭都会先询问这里：返回 false 即否决关闭（调用方已消费该次返回）
        confirmValueChange = { value ->
            value != SheetValue.Hidden || forceClosing || currentOnPreDismiss?.invoke() != true
        },
    )

    // 统一的关闭入口：hide() 内部会经过 confirmValueChange（等价于点遮罩的 animateToDismiss），
    // 因此返回键、点击遮罩、点击居中状态下的透明区域行为完全一致
    val requestDismiss: () -> Unit = {
        scope.launch {
            sheetState.hide()
            if (!sheetState.isVisible) {
                currentOnDismiss()
            }
        }
    }

    // 控制器发来的关闭请求（消费掉，避免影响下一次打开）
    LaunchedEffect(state.closeRequested) {
        if (state.closeRequested) {
            state.closeRequested = false
            forceClosing = true
            requestDismiss()
        }
    }

    MaterialTheme(motionScheme = sheetMotionScheme) {
        ModalBottomSheet(
            // 关闭动画结束（弹层已移除）后由官方组件回调
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            sheetMaxWidth = SheetMaxWidth,
            // 居中状态弹层本体要撑满整屏（透明），不能带圆角；底部状态保持顶部圆角
            shape = if (isCompact) {
                RoundedCornerShape(topStart = SheetCornerRadius, topEnd = SheetCornerRadius)
            } else {
                RectangleShape
            },
            containerColor = if (isCompact) {
                MaterialTheme.colorScheme.surface
            } else {
                Color.Transparent
            },
            // 透明容器下 contentColorFor(Transparent) 会取到 Unspecified，这里显式指定
            contentColor = MaterialTheme.colorScheme.onSurface,
            // 应用自带标题栏/关闭按钮，不显示官方默认拖拽把手
            dragHandle = null,
            properties = ModalBottomSheetProperties(
                // 返回键交给我们自己的 BackHandler 处理，才能让 onPreDismiss 生效
                shouldDismissOnBackPress = false,
            ),
        ) {
            // BackHandler 必须放在弹层内容里（即 Dialog 内部），这样才会先于弹层自身的返回处理被调用
            BackHandler { requestDismiss() }

            // 弹层内容恢复应用自身的动效曲线
            MaterialTheme(motionScheme = contentMotionScheme) {
                if (isCompact) {
                    SheetContentAtBottom(modifier = modifier, content = content)
                } else {
                    SheetContentAtCenter(
                        modifier = modifier,
                        requestDismiss = requestDismiss,
                        content = content,
                    )
                }
            }
        }
    }
}

/**
 * 由「可见性状态」驱动的弹层宿主：本模块内各弹层（MyBottomSheet、各 *Dialog）统一用它，
 * 这样状态类只需要维护“是否显示”，关闭动画的协调全部收敛在这里：
 *
 * - [visible] 为 true → 挂载 [AutoSheetDialog]；
 * - [visible] 变 false（例如操作成功后关闭）→ 先请求动画关闭，动画结束后才卸载，
 *   并回调 [onDismiss]；
 * - 关闭动画期间 [visible] 又变回 true → 重新挂载一个弹层展开，避免出现「状态说打开、界面已消失」。
 *
 * 注意：这是弹层组件内部使用的适配层，不是给业务页面直接调用的公共 API。
 *
 * @param onDismiss 弹层关闭（动画结束）后回调，让状态类把 visible 置为 false；弹层自身关闭也走这里
 */
@Composable
internal fun AutoSheetDialogHost(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onPreDismiss: (() -> Boolean)? = null,
    content: @Composable () -> Unit
) {
    val sheetState = rememberAutoSheetDialogState()
    var shown by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }
    var generation by remember { mutableIntStateOf(0) }

    LaunchedEffect(visible) {
        if (visible) {
            if (closing) {
                // 关闭动画还没播完就又被打开了：换一个新弹层重新展开
                closing = false
                generation++
            }
            shown = true
        } else if (shown) {
            closing = true
            sheetState.close()
        }
    }

    if (shown) {
        key(generation) {
            AutoSheetDialog(
                modifier = modifier,
                state = sheetState,
                onDismiss = {
                    shown = false
                    closing = false
                    onDismiss()
                },
                onPreDismiss = onPreDismiss,
                content = content,
            )
        }
    }
}

/**
 * 贴底：弹层高度由内容决定。
 */
@Composable
private fun SheetContentAtBottom(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
    ) {
        content()
    }
}

/**
 * 居中：弹层撑满整屏（透明），内容作为圆角卡片居中；卡片以外的透明区域等同遮罩，点击即关闭。
 */
@Composable
private fun SheetContentAtCenter(
    modifier: Modifier,
    requestDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    // 卡片在窗口坐标系中的范围，用来判断点击是否落在卡片之外
    var cardBounds by remember { mutableStateOf(Rect.Zero) }
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { rootCoordinates = it }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val windowOffset = rootCoordinates?.localToWindow(offset)
                    // 卡片以外的透明区域：与两侧遮罩一致，点击即关闭
                    if (windowOffset == null || !cardBounds.contains(windowOffset)) {
                        requestDismiss()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = SheetMaxWidth)
                .padding(horizontal = 10.dp)
                .onGloballyPositioned { cardBounds = it.boundsInWindow() },
            shape = RoundedCornerShape(SheetCornerRadius),
            color = MaterialTheme.colorScheme.surface,
        ) {
            // 调用方的 modifier（背景色等）放在卡片裁剪之内，保证四个角都是圆角
            Box(modifier = modifier) {
                content()
            }
        }
    }
}
