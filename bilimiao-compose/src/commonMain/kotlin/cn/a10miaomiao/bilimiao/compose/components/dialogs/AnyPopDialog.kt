package cn.a10miaomiao.bilimiao.compose.components.dialogs

import cn.a10miaomiao.bilimiao.compose.common.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun AnyPopDialog(
    isActiveClose: Boolean = false,
    properties: AnyPopDialogProperties = AnyPopDialogProperties(direction = DirectionState.BOTTOM),
    onDismiss: () -> Unit,
    onPreDismiss: (() -> Boolean)? = null,
    content: @Composable () -> Unit
) {
    var isAnimateLayout by remember { mutableStateOf(false) }
    var isBackPress by remember { mutableStateOf(false) }

    LaunchedEffect(isActiveClose) {
        if (isActiveClose) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    LaunchedEffect(Unit) {
        isAnimateLayout = true
    }

    val handleBackPress = {
        if (!isBackPress && onPreDismiss?.invoke() != true) {
            isBackPress = true
            isAnimateLayout = false
        }
    }

    BackHandler(onBack = handleBackPress)

    Dialog(
        onDismissRequest = handleBackPress,
        properties = DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        LaunchedEffect(isAnimateLayout) {
            if (!isAnimateLayout) {
                delay(properties.durationMillis.toLong())
                onDismiss()
            }
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
            if (properties.backgroundDimEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (isAnimateLayout) 0.45f else 0f))
                        .then(
                            if (properties.dismissOnClickOutside) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures(onTap = { handleBackPress() })
                                }
                            } else Modifier
                        )
                )
            }
            AnimatedVisibility(
                visible = isAnimateLayout && !isBackPress,
                enter = when (properties.direction) {
                    DirectionState.TOP -> slideInVertically(initialOffsetY = { -it })
                    DirectionState.LEFT -> slideInHorizontally(initialOffsetX = { -it })
                    DirectionState.RIGHT -> slideInHorizontally(initialOffsetX = { it })
                    DirectionState.BOTTOM -> slideInVertically(initialOffsetY = { it })
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
}
