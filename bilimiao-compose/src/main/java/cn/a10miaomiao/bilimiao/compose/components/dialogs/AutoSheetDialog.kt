package cn.a10miaomiao.bilimiao.compose.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass

@Composable
fun AutoSheetDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onPreDismiss: (() -> Boolean)? = null,
    content: @Composable () -> Unit
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val direction = if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
        DirectionState.BOTTOM
    } else {
        DirectionState.NONE
    }
    AnyPopDialog(
        onDismiss = onDismiss,
        onPreDismiss = onPreDismiss,
        content = {
            Box(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .safeContentPadding()
                    .let { modifier ->
                        if (direction == DirectionState.NONE) {
                            modifier
                                .padding(horizontal = 10.dp)
                                .clip(RoundedCornerShape(10.dp))
                        } else {
                            modifier
                                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        }
                    }
                    .then(modifier),
            ) {
                content()
            }
        },
        isActiveClose = false,
        // 根据你自己的功能，调整进入方向即可，支持:TOP/LEFT/RIGHT/BOTTOM/NONE
        properties = AnyPopDialogProperties(
            direction = direction,
        ),
    )
}