package cn.a10miaomiao.bilimiao.compose.components.start

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.a10miaomiao.bilimiao.compose.base.PageSearchMethod

@Composable
fun SearchOverlay(
    visible: Boolean,
    searchAnimation: Boolean,
    initKeyword: String,
    initMode: Int,
    pageSearchMethod: PageSearchMethod?,
    onDismissRequest: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = if (searchAnimation) {
            fadeIn(animationSpec = tween(durationMillis = 200)) +
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 200)
                    )
        } else {
            fadeIn(animationSpec = tween(0))
        },
        exit = if (searchAnimation) {
            fadeOut(animationSpec = tween(durationMillis = 250)) +
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 250)
                    )
        } else {
            fadeOut(animationSpec = tween(0))
        },
    ) {
        SearchInputInline(
            modifier = Modifier,
            initKeyword = initKeyword,
            initMode = initMode,
            pageSearchMethod = pageSearchMethod,
            onDismissRequest = onDismissRequest,
        )
    }
}
