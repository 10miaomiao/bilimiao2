package cn.a10miaomiao.bilimiao.compose.components.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bilimiao.bilimiao_compose.generated.resources.Res
import bilimiao.bilimiao_compose.generated.resources.bili_loading_img2
import bilimiao.bilimiao_compose.generated.resources.bili_loading_img1
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.delay

@Composable
fun BiliLoadingBox(modifier: Modifier) {
    var i by remember { mutableIntStateOf(0) }
    LaunchedEffect(i) {
        delay(100)
        i = 1 - i
    }
    val img1 = painterResource(Res.drawable.bili_loading_img1)
    val img2 = painterResource(Res.drawable.bili_loading_img2)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier
                .widthIn(max = 150.dp)
                .aspectRatio(1f),
            painter = if (i == 0) img1 else img2,
            contentDescription = "loading",
        )
    }
}