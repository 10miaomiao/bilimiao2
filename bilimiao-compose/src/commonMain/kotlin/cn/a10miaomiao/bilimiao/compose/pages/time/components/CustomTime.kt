package cn.a10miaomiao.bilimiao.compose.pages.time.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.outlined.KeyboardDoubleArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingViewMode
import com.a10miaomiao.bilimiao.comm.store.model.DateModel
import kotlin.math.abs

/** 头部胶囊内侧的纵向留白 */
private val CapsulePadding = 5.dp

/** 胶囊里选择器行的高度 */
private val SelectorRowHeight = 38.dp

/** 胶囊里星期/提示行的高度 */
private val SubRowHeight = 26.dp

/** 日期格行高（紧凑） */
private val CalendarRowHeight = 36.dp

/** 固定 6 行：不同月份长度、日期/月份视图之间高度完全一致，切换时不跳动 */
private const val CalendarRowCount = 6

/** 头部胶囊与网格之间的间距 */
private val CalendarGap = 4.dp

/** 整个日历区域高度：头部胶囊 + 间距 + 6 行网格，日期/月份视图共用 */
private val CalendarAreaHeight =
    CapsulePadding * 2 + SelectorRowHeight + SubRowHeight + CalendarGap +
        CalendarRowHeight * CalendarRowCount

private val WeekTitles = listOf("一", "二", "三", "四", "五", "六", "日")

internal enum class TextBoxStatus {
    Enable,
    Start,
    Middle,
    End,
    Disable,
}

/**
 * 胶囊条里的星期行。
 *
 * 圆角和底色由外层胶囊统一提供，这里只负责文字：小号中等字重，
 * 周末（六、日）用正文色并加粗，和工作日区分。
 */
@Composable
private fun WeekdayRow(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(SubRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeekTitles.forEachIndexed { index, title ->
            val weekend = index >= 5
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (weekend) FontWeight.SemiBold else FontWeight.Medium,
                color = if (weekend) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

/**
 * 胶囊条里的副标题行：月份视图下顶替星期行的位置，保证胶囊高度不变。
 */
@Composable
private fun MonthHintRow(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SubRowHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "选择月份",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 日期格在选中区间中的位置，用于绘制区间色带 */
private enum class RangeSide {
    None,
    Start,
    End,
    Full,
}

/** 日期选中圆的直径 */
private val DayCircleSize = 32.dp

/**
 * 日期格。
 *
 * Material 3 Expressive 风格：选中态是实心主色圆点；区间中段在圆点下方铺一条
 * 色带（起点向右半格、终点向左半格，与相邻格连成一条），悬停时显示浅色圆点。
 */
@Composable
private fun DayCell(
    text: String,
    status: TextBoxStatus,
    textColor: Color,
    rangeSide: RangeSide,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val selected = status == TextBoxStatus.Start || status == TextBoxStatus.End
    val disabled = status == TextBoxStatus.Disable
    Box(
        modifier = modifier
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (rangeSide) {
            RangeSide.Start -> Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxWidth(0.5f)
                    .height(DayCircleSize)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )

            RangeSide.End -> Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.5f)
                    .height(DayCircleSize)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )

            RangeSide.Full -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DayCircleSize)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )

            RangeSide.None -> Unit
        }
        Box(
            modifier = Modifier
                .size(DayCircleSize)
                .clip(CircleShape)
                .background(
                    when {
                        selected -> MaterialTheme.colorScheme.primary
                        hovered && !disabled ->
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = when {
                    selected -> MaterialTheme.colorScheme.onPrimary
                    status == TextBoxStatus.Middle -> MaterialTheme.colorScheme.onSurface
                    else -> textColor
                },
                fontSize = 14.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

/**
 * 月份格。
 *
 * Material 3 Expressive 风格：未选中为透明圆，选中为实心主色圆，悬停显示浅色圆。
 */
@Composable
private fun MonthCell(
    text: String,
    active: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = modifier
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    when {
                        active -> MaterialTheme.colorScheme.primary
                        hovered && enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    active -> MaterialTheme.colorScheme.onPrimary
                    enabled -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.outlineVariant
                },
            )
        }
    }
}

/**
 * 月份网格：4 行 × 3 列，与日期网格共用同一高度，切换视图时高度不变。
 */
@Composable
private fun MonthGrid(
    month: Int,
    minMonth: Int,
    maxMonth: Int,
    onClick: (month: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = 4
    val columns = 3
    Column(modifier = modifier.fillMaxSize()) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                for (column in 0 until columns) {
                    val value = row * columns + column + 1
                    MonthCell(
                        text = "${value}月",
                        active = value == month,
                        enabled = value in minMonth..maxMonth,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = { onClick(value) },
                    )
                }
            }
        }
    }
}

/**
 * 日期网格：固定 [CalendarRowCount] 行，跨月空白格用占位，保证行数恒定。
 */
@Composable
private fun DateGrid(
    year: Int,
    month: Int,
    maxDate: DateModel,
    monthStartWeek: Int,
    monthDayNum: Int,
    startTime: DateModel?,
    endTime: DateModel?,
    onClick: (day: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMaxMonth = year == maxDate.year && month == maxDate.month
    Column(modifier = modifier.fillMaxSize()) {
        repeat(CalendarRowCount) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                for (column in 0..6) {
                    val day = row * 7 + column - monthStartWeek + 1
                    if (day in 1..monthDayNum && !(isMaxMonth && day > maxDate.date)) {
                        val curTime = DateModel().also {
                            it.year = year
                            it.month = month
                            it.date = day
                        }
                        val _startTime = startTime
                        val _endTime = endTime
                        val status = if (_startTime == null) {
                            TextBoxStatus.Enable
                        } else if (curTime == _startTime) {
                            TextBoxStatus.Start
                        } else if (_endTime == null) {
                            if (abs(_startTime.getGapCount(curTime)) > 30) {
                                TextBoxStatus.Disable
                            } else {
                                TextBoxStatus.Enable
                            }
                        } else if (curTime == _endTime) {
                            TextBoxStatus.End
                        } else {
                            if (_startTime.getGapCount(curTime) > 0
                                && _endTime.getGapCount(curTime) < 0
                            ) {
                                TextBoxStatus.Middle
                            } else {
                                TextBoxStatus.Enable
                            }
                        }
                        DayCell(
                            text = day.toString(),
                            status = status,
                            rangeSide = when (status) {
                                TextBoxStatus.Start ->
                                    if (_endTime != null) RangeSide.Start else RangeSide.None
                                TextBoxStatus.End ->
                                    if (_startTime != null) RangeSide.End else RangeSide.None
                                TextBoxStatus.Middle -> RangeSide.Full
                                else -> RangeSide.None
                            },
                            textColor = when {
                                status == TextBoxStatus.Disable ->
                                    MaterialTheme.colorScheme.outlineVariant
                                // 周六、日
                                column >= 5 -> MaterialTheme.colorScheme.onBackground
                                else -> MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = { onClick(day) },
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 标题栏里的可点区域。
 *
 * 年份和月份合并成一个按钮；悬停时给一层底色，方便桌面端鼠标识别可点。
 */
@Composable
private fun TitlePart(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (hovered) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                } else {
                    Color.Transparent
                }
            )
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content,
    )
}

/**
 * 日历头部。
 *
 * 日期视图：单箭头切月、双层箭头切年，点击中间标题进入月份视图。
 * 月份视图：单箭头切年、双层箭头跨十年，中间标题变成年份选择器（下拉选年份）。
 */
@Composable
private fun Header(
    year: MutableState<Int>,
    month: MutableState<Int>,
    maxDate: DateModel,
    minDate: DateModel,
    monthMode: Boolean,
    onMonthModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    // 年份变化后把月份夹到边界月份，避免落到最大/最小日期之外
    fun clampMonth() {
        if (year.value <= minDate.year) {
            month.value = month.value.coerceAtLeast(minDate.month)
        }
        if (year.value >= maxDate.year) {
            month.value = month.value.coerceAtMost(maxDate.month)
        }
    }

    fun prevMonth() {
        if (month.value == 1) {
            month.value = 12
            year.value--
        } else {
            month.value--
        }
    }

    fun nextMonth() {
        if (month.value == 12) {
            month.value = 1
            year.value++
        } else {
            month.value++
        }
    }

    fun prevYear() {
        if (year.value > minDate.year) {
            year.value--
            clampMonth()
        }
    }

    fun nextYear() {
        if (year.value < maxDate.year) {
            year.value++
            clampMonth()
        }
    }

    fun jumpYears(step: Int) {
        year.value = (year.value + step).coerceIn(minDate.year, maxDate.year)
        clampMonth()
    }

    val atMinMonth = year.value == minDate.year && month.value == minDate.month
    val atMaxMonth = year.value == maxDate.year && month.value == maxDate.month
    val atMinYear = year.value <= minDate.year
    val atMaxYear = year.value >= maxDate.year

    Column() {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(SelectorRowHeight),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 外层：日期视图切一年、月份视图跨十年
            ArrowButton(
                imageVector = Icons.Outlined.KeyboardDoubleArrowLeft,
                contentDescription = if (monthMode) "上十年" else "上一年",
                enabled = !atMinYear,
                onClick = {
                    if (monthMode) {
                        jumpYears(-10)
                    } else {
                        prevYear()
                    }
                },
            )
            // 内层：日期视图切一月、月份视图切一年
            ArrowButton(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = if (monthMode) "上一年" else "上一月",
                enabled = if (monthMode) !atMinYear else !atMinMonth,
                onClick = {
                    if (monthMode) {
                        prevYear()
                    } else {
                        prevMonth()
                    }
                },
            )
            Box {
                // 年份、月份合并成一个按钮：日期视图点开月份网格，月份视图点开年份下拉
                TitlePart(
                    modifier = Modifier.widthIn(min = 130.dp),
                    onClick = {
                        if (monthMode) {
                            expanded = true
                        } else {
                            onMonthModeChange(true)
                        }
                    },
                ) {
                    Text(
                        text = if (monthMode) {
                            "${year.value}年"
                        } else {
                            "${year.value}年 ${month.value}月"
                        },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    for (y in minDate.year..maxDate.year) {
                        DropdownMenuItem(
                            text = {
                                Text(text = "${y}年")
                            },
                            onClick = {
                                year.value = y
                                clampMonth()
                                expanded = false
                            }
                        )
                    }
                }
            }
            // 内层：日期视图切一月、月份视图切一年
            ArrowButton(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = if (monthMode) "下一年" else "下一月",
                enabled = if (monthMode) !atMaxYear else !atMaxMonth,
                onClick = {
                    if (monthMode) {
                        nextYear()
                    } else {
                        nextMonth()
                    }
                },
            )
            // 外层：日期视图切一年、月份视图跨十年
            ArrowButton(
                imageVector = Icons.Outlined.KeyboardDoubleArrowRight,
                contentDescription = if (monthMode) "下十年" else "下一年",
                enabled = !atMaxYear,
                onClick = {
                    if (monthMode) {
                        jumpYears(10)
                    } else {
                        nextYear()
                    }
                },
            )
        }
    }
}

/**
 * 合并胶囊条：选择器行（箭头 + 年月）和星期行放进同一个大圆角容器。
 *
 * 月份视图下星期行换成「选择月份」提示行，两行高度一致，保证切换视图时
 * 胶囊和整个日历区域的高度都不变。
 */
@Composable
private fun CalendarHeader(
    year: MutableState<Int>,
    month: MutableState<Int>,
    maxDate: DateModel,
    minDate: DateModel,
    monthMode: Boolean,
    onMonthModeChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(vertical = CapsulePadding),
        ) {
            Header(
                modifier = Modifier.padding(horizontal = 6.dp),
                year = year,
                month = month,
                maxDate = maxDate,
                minDate = minDate,
                monthMode = monthMode,
                onMonthModeChange = onMonthModeChange,
            )
            if (monthMode) {
                MonthHintRow()
            } else {
                WeekdayRow()
            }
        }
    }
}

@Composable
internal fun CustomTime(
    viewModel: TimeSettingViewMode
) {
    val customTime = viewModel.customTime.collectAsState()
    val timeFrom = customTime.value.timeFrom
    val timeTo = customTime.value.timeTo
    val maxDate = viewModel.maxDate
    val minDate = viewModel.minDate

    var year = remember {
        mutableStateOf(2009)
    }
    var month = remember {
        mutableStateOf(9)
    }
    // 月份视图：由日历头部标题切换，选完月份自动回到日期视图
    var monthMode by remember {
        mutableStateOf(false)
    }

    val monthStartWeek = remember(year.value, month.value) {
        getMonthStartOffset(year.value, month.value)
    }
    val monthDayNum = remember(year.value, month.value) {
        getMonthDayNum(year.value, month.value)
    }

    var startTime by remember {
        mutableStateOf<DateModel?>(null)
    }
    var endTime by remember {
        mutableStateOf<DateModel?>(null)
    }

    LaunchedEffect(Unit) {
        if (timeFrom.year != -1) {
            startTime = timeFrom.copy()
            endTime = timeTo.copy()
            year.value = timeFrom.year
            month.value = timeFrom.month
        }
    }

    val itemClick = remember(viewModel) {
        { i: Int ->
            val _startTime = startTime
            if (_startTime == null) {
                startTime = DateModel().also {
                    it.year = year.value
                    it.month = month.value
                    it.date = i
                }
            } else if (endTime == null) {
                val dateModel = DateModel().also {
                    it.year = year.value
                    it.month = month.value
                    it.date = i
                }
                if (abs(_startTime.getGapCount(dateModel)) > 30) {
                    // TODO: toast
                } else if (dateModel.toLocalDate() > _startTime.toLocalDate()) {
                    endTime = dateModel
                } else {
                    endTime = _startTime
                    startTime = dateModel
                }
                viewModel.setCustomTime(startTime, endTime)
            } else {
                startTime = null
                endTime = null
                viewModel.setCustomTime(null, null)
            }
            Unit
        }
    }

    Column() {

        // 头部胶囊 + 网格：整体固定高度，日期/月份视图切换时卡片高度不变
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(CalendarAreaHeight)
        ) {
            CalendarHeader(
                year = year,
                month = month,
                maxDate = maxDate,
                minDate = minDate,
                monthMode = monthMode,
                onMonthModeChange = { monthMode = it },
            )
            Spacer(modifier = Modifier.height(CalendarGap))
            Crossfade(
                targetState = monthMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { showMonths ->
                if (showMonths) {
                    MonthGrid(
                        month = month.value,
                        minMonth = if (year.value == minDate.year) minDate.month else 1,
                        maxMonth = if (year.value == maxDate.year) maxDate.month else 12,
                        modifier = Modifier.fillMaxSize(),
                        onClick = {
                            month.value = it
                            monthMode = false
                        },
                    )
                } else {
                    DateGrid(
                        year = year.value,
                        month = month.value,
                        maxDate = maxDate,
                        monthStartWeek = monthStartWeek,
                        monthDayNum = monthDayNum,
                        startTime = startTime,
                        endTime = endTime,
                        modifier = Modifier.fillMaxSize(),
                        onClick = itemClick,
                    )
                }
            }
        }

        Text(
            text = "已选择时间线：${startTime?.getValue("-") ?: "未选择"} 至 ${endTime?.getValue("-") ?: "未选择"}",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}
