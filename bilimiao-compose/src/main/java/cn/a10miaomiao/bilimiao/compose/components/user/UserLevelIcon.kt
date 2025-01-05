package cn.a10miaomiao.bilimiao.compose.components.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R

@Composable
fun UserLevelIcon(
    modifier: Modifier = Modifier,
    level: Int
) {
    val levelImgRes = when (level) {
        0 -> R.drawable.ic_bili_lv0
        1 -> R.drawable.ic_bili_lv1
        2 -> R.drawable.ic_bili_lv2
        3 -> R.drawable.ic_bili_lv3
        4 -> R.drawable.ic_bili_lv4
        5 -> R.drawable.ic_bili_lv5
        6 -> R.drawable.ic_bili_lv6
        7 -> R.drawable.ic_bili_lv7
        8 -> R.drawable.ic_bili_lv8
        else -> R.drawable.ic_bili_lv9
    }
    Image(
        modifier = modifier,
        painter = painterResource(levelImgRes),
        contentDescription = "lv${level}"
    )
}