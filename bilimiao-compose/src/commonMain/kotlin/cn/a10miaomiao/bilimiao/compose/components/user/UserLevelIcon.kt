package cn.a10miaomiao.bilimiao.compose.components.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bilimiao.bilimiao_compose.generated.resources.Res
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv9
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv8
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv7
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv6
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv5
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv4
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv3
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv2
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv1
import bilimiao.bilimiao_compose.generated.resources.ic_bili_lv0
import org.jetbrains.compose.resources.painterResource

@Composable
fun UserLevelIcon(
    modifier: Modifier = Modifier,
    level: Int
) {
    val levelImgRes = when (level) {
        0 -> Res.drawable.ic_bili_lv0
        1 -> Res.drawable.ic_bili_lv1
        2 -> Res.drawable.ic_bili_lv2
        3 -> Res.drawable.ic_bili_lv3
        4 -> Res.drawable.ic_bili_lv4
        5 -> Res.drawable.ic_bili_lv5
        6 -> Res.drawable.ic_bili_lv6
        7 -> Res.drawable.ic_bili_lv7
        8 -> Res.drawable.ic_bili_lv8
        else -> Res.drawable.ic_bili_lv9
    }
    Image(
        modifier = modifier,
        painter = painterResource(levelImgRes),
        contentDescription = "lv${level}"
    )
}