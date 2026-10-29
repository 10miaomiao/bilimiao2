package cn.a10miaomiao.bilimiao.compose.pages.time.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.common.foundation.horizontalWheelScroll
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingViewMode
import kotlinx.coroutines.launch

/**
 * 时间选择标签。
 *
 * 选中样式与自定义范围的日期格、月份格保持一致：实心主色底 + 反色文字；
 * 悬停显示浅色底，点击不显示涟漪（靠悬停反馈识别可点）。
 */
@Composable
private fun TimeChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit,
    minWidth: Dp = 0.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Surface(
        modifier = Modifier
            .widthIn(min = minWidth)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        color = when {
            active -> MaterialTheme.colorScheme.primary
            hovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            else -> Color.Transparent
        },
        contentColor = if (active) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        shape = RoundedCornerShape(percent = 50),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = text, fontSize = 15.sp, maxLines = 1)
        }
    }
}

/**
 * 悬浮在横向列表两端的滚动箭头。
 *
 * 桌面端鼠标滚轮不能横向滚动列表，用左右箭头补足操作方式；移动端虽然可以拖动，
 * 但箭头也一直显示，保证两端行为一致。
 */
@Composable
private fun ListScrollButton(
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (enabled) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        shadowElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * 按月份选择年份、月份。
 *
 * 年份是横向滚动列表，两端各悬浮一个滚动箭头：桌面端鼠标滚轮不能横向滚动，
 * 点击箭头即可翻动年份；移动端也保留箭头，两端操作方式一致。
 */
@Composable
internal fun MonthTime(
    viewModel: TimeSettingViewMode,
) {
    val monthTime = viewModel.monthTime.collectAsState()
    val timeFrom = monthTime.value.timeFrom
    val timeTo = monthTime.value.timeTo

    val years = remember(viewModel.minDate.year, viewModel.maxDate.year) {
        (viewModel.minDate.year..viewModel.maxDate.year).toList()
    }
    val yearListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val scrollStep = with(LocalDensity.current) { 180.dp.toPx() }

    // 首次进入时把选中的年份滚到可见位置
    LaunchedEffect(Unit) {
        val index = years.indexOf(timeFrom.year)
        if (index > 0) {
            yearListState.scrollToItem(index)
        }
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalWheelScroll(yearListState),
                state = yearListState,
                // 两侧留出悬浮箭头的位置，避免箭头压住年份标签
                contentPadding = PaddingValues(horizontal = 34.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(years.size) { index ->
                    val year = years[index]
                    TimeChip(
                        text = year.toString(),
                        active = year == timeFrom.year,
                        minWidth = 54.dp,
                        onClick = { viewModel.setMonthTime(year, timeFrom.month) },
                    )
                }
            }
            ListScrollButton(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = "上一年份",
                enabled = yearListState.canScrollBackward,
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = {
                    coroutineScope.launch { yearListState.animateScrollBy(-scrollStep) }
                },
            )
            ListScrollButton(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "下一年份",
                enabled = yearListState.canScrollForward,
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    coroutineScope.launch { yearListState.animateScrollBy(scrollStep) }
                },
            )
        }

        Text(
            text = "月份",
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (month in 1..12) {
                TimeChip(
                    text = "${month}月",
                    active = month == timeFrom.month,
                    minWidth = 44.dp,
                    onClick = { viewModel.setMonthTime(timeFrom.year, month) },
                )
            }
        }

        Text(
            text = "已选择时间线：${timeFrom.getValue("-")} 至 ${timeTo.getValue("-")}",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}
