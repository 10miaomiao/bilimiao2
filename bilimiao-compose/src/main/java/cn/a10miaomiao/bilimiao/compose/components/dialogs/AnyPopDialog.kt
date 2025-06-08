package cn.a10miaomiao.bilimiao.compose.components.dialogs

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.view.WindowCompat
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
private fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
private fun getActivityWindow(): Window? = LocalView.current.context.getActivityWindow()

private tailrec fun Context.getActivityWindow(): Window? = when (this) {
    is Activity -> window
    is ContextWrapper -> baseContext.getActivityWindow()
    else -> null
}

private fun Context.getDisplayWidth(): Int{
    val density = resources.displayMetrics.density
    return (resources.configuration.screenWidthDp * density).roundToInt()
}

private fun Context.getDisplayHeight(): Int{
    val density = resources.displayMetrics.density
    return (resources.configuration.screenHeightDp * density).roundToInt()
}

private const val DefaultDurationMillis: Int = 250

@Composable
private fun DialogFullScreen(
    isActiveClose: Boolean,
    onDismissRequest: () -> Unit,
    onPreDismissRequest: (() -> Boolean)? = null,
    properties: AnyPopDialogProperties,
    content: @Composable () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    var isAnimateLayout by remember {
        mutableStateOf(false)
    }
    var isBackPress by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(isActiveClose) {
        if (isActiveClose) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    val handleBackPress = {
        if (!isBackPress && onPreDismissRequest?.invoke() != true) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    Dialog(
        onDismissRequest = handleBackPress,
        properties = DialogProperties(
            // 这里不使用新的测量规范，不能设置为false
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false,
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = false,
            securePolicy = properties.securePolicy
        ),
        content = {
            val animColor = remember { Animatable(Color.Transparent) }
            LaunchedEffect(isAnimateLayout) {
                if (properties.backgroundDimEnabled) {
                    animColor.animateTo(
                        if (isAnimateLayout) Color.Black.copy(alpha = 0.45F) else Color.Transparent,
                        animationSpec = tween(properties.durationMillis)
                    )
                } else {
                    delay(properties.durationMillis.toLong())
                }
                if (!isAnimateLayout) {
                    onDismissRequest.invoke()
                }
            }
            val activityWindow = getActivityWindow()
            val dialogWindow = getDialogWindow()
            val parentView = LocalView.current.parent as View
            // 处理预览模式，宽高问题
            val displayWidth = activityWindow?.decorView?.width ?: LocalContext.current.getDisplayWidth()
            val displayHeight = activityWindow?.decorView?.height ?: LocalContext.current.getDisplayHeight()
            SideEffect {
                if (
                    (isPreview && (isBackPress || isAnimateLayout)) ||
                    (!isPreview && (activityWindow == null || dialogWindow == null || isBackPress || isAnimateLayout))
                ) {
                    return@SideEffect
                }
                if(dialogWindow != null) {
                    val attributes = WindowManager.LayoutParams()
                    if (activityWindow != null) { // 判空忽略预览模式
                        attributes.copyFrom(activityWindow.attributes)
                    }
                    attributes.type = dialogWindow.attributes.type
                    dialogWindow.attributes = attributes
                    // 修复Android10 - Android11出现背景全黑的情况
                    dialogWindow.setBackgroundDrawableResource(android.R.color.transparent)
                    // 禁止Dialog跟随软键盘高度变化，用Compose提供的imePadding替代
                    dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

                    dialogWindow.setLayout(displayWidth, displayHeight)
                    dialogWindow.statusBarColor = Color.Transparent.toArgb()
                    dialogWindow.navigationBarColor = Color.Transparent.toArgb()

                    WindowCompat.getInsetsController(dialogWindow, parentView)
                        .isAppearanceLightNavigationBars = properties.isAppearanceLightNavigationBars
                }
                isAnimateLayout = true
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = when (properties.direction) {
                    DirectionState.TOP -> Alignment.TopCenter
                    DirectionState.LEFT -> Alignment.CenterStart
                    DirectionState.RIGHT -> Alignment.CenterEnd
                    DirectionState.BOTTOM -> Alignment.BottomCenter
                    else -> Alignment.Center
                }
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(animColor.value)
                        .clickOutSideModifier(
                            dismissOnClickOutside = properties.dismissOnClickOutside,
                            onTap = handleBackPress
                        )
                )
                AnimatedVisibility(
                    modifier = Modifier.pointerInput(Unit) {},
                    // 预览还想直接显示UI的情况，配置其他方向需要点击：Start interactive mode这个手势按钮
                    visible = if(isPreview && properties.direction == DirectionState.NONE) true else isAnimateLayout,
                    enter = when (properties.direction) {
                        DirectionState.TOP -> slideInVertically(initialOffsetY = { -it })
                        DirectionState.LEFT -> slideInHorizontally(initialOffsetX = { -it })
                        DirectionState.RIGHT -> slideInHorizontally(initialOffsetX = { it })
                        DirectionState.BOTTOM ->  slideInVertically(initialOffsetY = { it })
                        else -> fadeIn()
                    },
                    exit = when (properties.direction) {
                        DirectionState.TOP -> fadeOut() + slideOutVertically(targetOffsetY = { -it })
                        DirectionState.LEFT -> fadeOut() + slideOutHorizontally(targetOffsetX = { -it })
                        DirectionState.RIGHT -> fadeOut() + slideOutHorizontally(targetOffsetX = { it })
                        DirectionState.BOTTOM -> fadeOut() + slideOutVertically(targetOffsetY = { it })
                        else -> fadeOut()
                    }
                ) {
                    content()
                }
            }
        }
    )
}

/**
 * @author 被风吹过的夏天
 * @see <a href="https://github.com/TheMelody/AnyPopDialog-Compose">https://github.com/TheMelody/AnyPopDialog-Compose</a>
 * @param isActiveClose 设置为true可触发动画关闭Dialog，动画完自动触发[onDismiss]
 * @param properties Dialog相关配置
 * @param onDismiss Dialog关闭的回调
 * @param content 可组合项视图
 */
@Composable
fun AnyPopDialog(
    isActiveClose: Boolean = false,
    properties: AnyPopDialogProperties = AnyPopDialogProperties(direction = DirectionState.BOTTOM),
    onDismiss: () -> Unit,
    onPreDismiss: (() -> Boolean)? = null,
    content: @Composable () -> Unit
) {
    DialogFullScreen(
        isActiveClose = isActiveClose,
        onDismissRequest = onDismiss,
        onPreDismissRequest = onPreDismiss,
        properties = properties,
        content = content,
    )
}

/**
 * @param dismissOnBackPress 是否支持返回关闭Dialog
 * @param dismissOnClickOutside 是否支持空白区域点击关闭Dialog
 * @param isAppearanceLightNavigationBars 导航栏前景色是不是亮色
 * @param direction 当前对话框弹出的方向
 * @param backgroundDimEnabled 背景渐入渐出开关
 * @param durationMillis 弹框消失和进入的时长
 * @param securePolicy 屏幕安全策略
 */
@Immutable
class AnyPopDialogProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val isAppearanceLightNavigationBars: Boolean = true,
    val direction: DirectionState,
    val backgroundDimEnabled: Boolean = true,
    val durationMillis: Int = DefaultDurationMillis,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnyPopDialogProperties) return false

        if (dismissOnBackPress != other.dismissOnBackPress) return false
        if (isAppearanceLightNavigationBars != other.isAppearanceLightNavigationBars) return false
        if (dismissOnClickOutside != other.dismissOnClickOutside) return false
        if (direction != other.direction) return false
        if (backgroundDimEnabled != other.backgroundDimEnabled) return false
        if (durationMillis != other.durationMillis) return false
        if (securePolicy != other.securePolicy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dismissOnBackPress.hashCode()
        result = 31 * result + dismissOnClickOutside.hashCode()
        result = 31 * result + isAppearanceLightNavigationBars.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + backgroundDimEnabled.hashCode()
        result = 31 * result + durationMillis.hashCode()
        result = 31 * result + securePolicy.hashCode()
        return result
    }
}

enum class DirectionState {
    TOP,
    LEFT,
    RIGHT,
    BOTTOM,
    NONE
}

private fun Modifier.clickOutSideModifier(
    dismissOnClickOutside: Boolean,
    onTap: () -> Unit
) = this.then(
    if (dismissOnClickOutside) {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                onTap()
            })
        }
    } else Modifier
)