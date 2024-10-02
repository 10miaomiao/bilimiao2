package cn.a10miaomiao.bilimiao.compose.components.status

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import kotlinx.coroutines.delay

@Composable
fun BiliLoadingBox(modifier: Modifier) {
    var i by remember { mutableIntStateOf(0) }
    LaunchedEffect(i) {
        delay(100)
        i = 1 - i
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier
                .widthIn(max = 150.dp)
                .aspectRatio(1f),
            painter = if (i == 0)
                painterResource(id = R.drawable.bili_loading_img1)
            else
                painterResource(id = R.drawable.bili_loading_img2),
            contentDescription = "loading",
        )
    }
}