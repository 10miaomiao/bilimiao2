package cn.a10miaomiao.bilimiao.compose.pages.time.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingViewMode

@Composable
internal fun TextBox(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color = if (active) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            Color.Transparent
        },
        shape = RoundedCornerShape(5.dp)
    ) {
        Text(
            color = MaterialTheme.colorScheme.outline,
            text = text,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(vertical = 5.dp, horizontal = 8.dp)

        )
    }
}

@Composable
internal fun MonthTime(
    viewModel: TimeSettingViewMode,
) {
    val monthTime = viewModel.monthTime.collectAsState()
    val timeFrom = monthTime.value.timeFrom
    val timeTo = monthTime.value.timeTo

    val year = timeFrom.year - 2009
    val month = timeFrom.month

    val yearCount = viewModel.yearCount

    val yearListState = rememberLazyListState()

    LaunchedEffect(yearListState) {
        yearListState.scrollToItem(year)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = "年份",
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            state = yearListState,
        ) {
            items(yearCount) {
                TextBox(
                    text = (it + 2009).toString(),
                    active = year == it,
                    onClick = {
                        viewModel.setMonthTime(it + 2009, month)
                    }
                )
            }
        }

        Text(
            text = "月份",
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(vertical = 4.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            for (i in 1..6) {
                TextBox(
                    text = "${i}月",
                    active = i == month,
                    onClick = {
                        viewModel.setMonthTime(year + 2009, i)
                    },
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            for (i in 7..12) {
                TextBox(
                    text = "${i}月",
                    active = i == month,
                    onClick = {
                        viewModel.setMonthTime(year + 2009, i)
                    },
                )
            }
        }

        Text(
            text = "已选择时间线：${timeFrom.getValue("-")} 至 ${timeTo.getValue("-")}",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}
