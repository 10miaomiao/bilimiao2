package cn.a10miaomiao.bilimiao.compose.components.start

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 250)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest,
                    )
            )
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier.align(Alignment.TopStart),
                enter = if (searchAnimation) {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 200)
                    ) + fadeIn(animationSpec = tween(durationMillis = 200))
                } else {
                    fadeIn(animationSpec = tween(0))
                },
                exit = if (searchAnimation) {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(durationMillis = 250)
                    ) + fadeOut(animationSpec = tween(durationMillis = 250))
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
    }
}
