package cn.a10miaomiao.bilimiao.compose.pages.time.commponents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingPageViewMode
import com.a10miaomiao.bilimiao.comm.store.model.DateModel

@Composable
fun CurrentTime(
    viewModel: TimeSettingPageViewMode
) {
    val currentTime = viewModel.currentTime.collectAsState()
    val timeFrom = currentTime.value.timeFrom
    val timeTo = currentTime.value.timeTo

    Column() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                color = MaterialTheme.colorScheme.outline,
                fontSize = 14.sp,
                text = "春天，马上就要来了。\n让我与你相遇的春天，就要来了。\n再也没有你的春天，就要来了。"
            )
        }
        Text(
            text = "已选择时间线：${timeFrom.getValue("-")} 至 ${timeTo.getValue("-")}",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }

}