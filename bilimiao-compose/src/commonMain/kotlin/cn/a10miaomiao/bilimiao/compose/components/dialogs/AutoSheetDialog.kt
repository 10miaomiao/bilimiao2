package cn.a10miaomiao.bilimiao.compose.components.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.isCompactWindow

@Composable
fun AutoSheetDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onPreDismiss: (() -> Boolean)? = null,
    content: @Composable () -> Unit
) {
    val direction = if (isCompactWindow()) {
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
                    .safeDrawingPadding()
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
        properties = AnyPopDialogProperties(
            direction = direction,
        ),
    )
}
