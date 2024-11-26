package cn.a10miaomiao.bilimiao.compose.pages.time.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingViewMode

@Composable
internal fun CurrentTime(
    viewModel: TimeSettingViewMode
) {
    val currentTime = viewModel.currentTime.collectAsState()
    val timeFrom = currentTime.value.timeFrom
    val timeTo = currentTime.value.timeTo

    val text = when (timeTo.month) {
        1, 2, 3, -> "春天，\n就要来了，\n与你相遇林下的春天，\n就要来了。\n"
        4 -> "四月，\n还是那个四月。\n但是，这个春天，\n只剩下铛的伴奏，\n再也没有弦的悠扬。\n没有你的春天，\n已经来了。"
        5, 6, 7 -> "我们所度过的每个平凡的日常\n也许就是连续发生的奇迹。"
        8 -> "10年後の8月\nまた出会えるのを\n信じて"
        9, 10 -> "前天是小兔，\n昨天是小鹿，\n今天，\n则是你。"
        11, 12 -> "这一切都是命运石之门的选择"
        else -> "hello world"
    }

    Column() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp, top = 20.dp, bottom = 20.dp),
        ) {
            Text(
                color = MaterialTheme.colorScheme.outline,
                fontSize = 14.sp,
                text = text
            )
        }
        Text(
            text = "已选择时间线：${timeFrom.getValue("-")} 至 ${timeTo.getValue("-")}",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }

}