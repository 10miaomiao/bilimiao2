package com.a10miaomiao.bilimiao.widget.rangedate

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.a10miaomiao.bilimiao.widget.rangedate.adapter.DayAdapter
import com.a10miaomiao.bilimiao.widget.rangedate.listener.OnClickDayListener
import com.a10miaomiao.bilimiao.widget.rangedate.listener.OnFinishSelectListener
import com.a10miaomiao.bilimiao.widget.rangedate.model.*
import java.util.*

/**
 * @author DarklyCoder
 * @Description 区域日历选择
 * @date 2018/10/11
 */
class RangeDateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    //<editor-fold desc="属性变量">

    private val dayList: ArrayList<DayInfo> = arrayListOf()
    private var mAdapter: DayAdapter? = null
    private var selectInfo: SelectDateInfo? = null //选择时间
    private var selectType: SelectDateType? = null //选择类型
    private var dateRecyclerView: RecyclerView? = null
    private val mHandler = Handler()
    private var isInAnim = false
    private var mListener: OnFinishSelectListener? = null

    //</editor-fold>

    init {
        //TODO 读取自定义属性

        orientation = LinearLayout.VERTICAL

        removeAllViews()

        addHeadView()
        addCalendarView()
    }

    //<editor-fold desc="初始化布局">
    /**
     * 添加头部
     */
    private fun addHeadView() {
        val headView = LinearLayout(context)
        headView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, RangeDateUtils.dp2px(context, 50).toInt())
        headView.orientation = LinearLayout.HORIZONTAL
        headView.setBackgroundColor(Color.WHITE)

        val arr = arrayListOf("日", "一", "二", "三", "四", "五", "六")

        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT)
        lp.weight = 1f

        arr.forEach { item ->
            val textView = TextView(context)
            textView.text = item
            textView.gravity = Gravity.CENTER
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            val isSpecial = "日" == item || "六" == item
            textView.setTextColor(if (isSpecial) Color.parseColor("#F8C300") else Color.parseColor("#333333"))

            textView.layoutParams = lp
            headView.addView(textView)
        }

        addView(headView)
    }

    /**
     * 添加日历部分
     */
    private fun addCalendarView() {
        dateRecyclerView = RecyclerView(context)
        dateRecyclerView?.setBackgroundColor(Color.WHITE)
        dateRecyclerView?.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        val layoutManager = GridLayoutManager(context, 7)
        dateRecyclerView?.layoutManager = layoutManager
        dateRecyclerView?.addItemDecoration(SectionDecoration(context, object : SectionDecoration.DecorationCallback {
            override fun getGroupId(position: Int): String {
                val size = dayList.size
                return if (position >= size) "" else dayList[position].groupName
            }
        }))
        mAdapter = DayAdapter(dayList, getOnClickDayListener())
        dateRecyclerView?.adapter = mAdapter
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (dayList[position].type == DayInfo.TYPE_DAY_TITLE) layoutManager.spanCount else 1
            }
        }

        addView(dateRecyclerView)
    }

    //</editor-fold>

    //<editor-fold desc="外部调用">

    /**
     * 初始化日期
     */
    fun initDate(type: SelectDateType?, info: SelectDateInfo?) {
        if (null == type || null == info) {
            return
        }

        selectType = type
        selectInfo = info

        val monthList = getDateList(selectInfo)

        //填充日期
        dayList.clear()
        monthList.forEachIndexed { _, monthInfo ->

            val dayInfo = DayInfo(monthInfo.year, monthInfo.month, 0, DayInfo.TYPE_DAY_TITLE)
            val monthNum = if (monthInfo.month < 10) "0${monthInfo.month}" else "${monthInfo.month}"
            dayInfo.groupName = "${monthInfo.year}年${monthNum}月"
            dayList.add(dayInfo)

            dayList.addAll(monthInfo.dayList)
        }

        //滑动到当前选择的位置
        val curPosition = calculationCurPosition(monthList, info)

        refreshData()

        val mouthIndex = curPosition.first
        val mouthRow = curPosition.second

        var countRow = 0
        for (i in 0 until mouthIndex) {
            countRow += if (monthList[i].dayList.size % 7 == 0) monthList[i].dayList.size / 7 else monthList[i].dayList.size / 7 + 1
        }

        val headerHeight = if (mouthIndex > 0) mouthIndex * RangeDateUtils.dp2px(context, 55) else 0f
        val countRowHeight = countRow * RangeDateUtils.dp2px(context, 55)
        val mouthRowHeight = mouthRow * RangeDateUtils.dp2px(context, 55)

        dateRecyclerView?.post {
            dateRecyclerView?.scrollBy(0, (headerHeight + countRowHeight + mouthRowHeight).toInt())
        }
    }

    /**
     * 销毁资源
     */
    fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        mAdapter = null
        mListener = null
    }

    fun setOnFinishSelectListener(listener: OnFinishSelectListener) {
        this.mListener = listener
    }

    //</editor-fold>

    //<editor-fold desc="私有方法">

    /**
     * 获取日期列表
     */
    private fun getDateList(selectInfo: SelectDateInfo?): ArrayList<MonthInfo> {
        val monthList: ArrayList<MonthInfo> = arrayListOf()

        if (null == selectInfo) {
            return monthList
        }

        var maxCount = 12 //默认生成12个月

        val startDate = if (isInHourMode()) selectInfo.hourDate else selectInfo.endDate
        var startDay: DayInfo? = null
        var endDay: DayInfo? = null
        val isInDelayMode = isInDelayMode()

        if (isInDelayMode) {
            //处于延住模式，锁死开始时间，开始时间为当前选择的最后时间，往后最多延28天
            val cal = Calendar.getInstance()
            cal.timeInMillis = startDate
            val curSelectMonth = cal.get(Calendar.MONTH) + 1 //当前选中的月份
            startDay = DayInfo(cal.get(Calendar.YEAR), curSelectMonth, cal.get(Calendar.DAY_OF_MONTH))

            cal.add(Calendar.DAY_OF_MONTH, 28)
            val lastMonth = cal.get(Calendar.MONTH) + 1
            endDay = DayInfo(cal.get(Calendar.YEAR), lastMonth, cal.get(Calendar.DAY_OF_MONTH))

            maxCount = if (lastMonth == curSelectMonth) 1 else 2
        }

        val c = Calendar.getInstance()
        if (isInDelayMode) {
            c.timeInMillis = startDate
        }

        if (isNeedHandelMorn()) {
            //需要处理凌晨
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, -1) //前一天的

            val mornMonth = cal.get(Calendar.MONTH)
            val curMonth = c.get(Calendar.MONTH)
            if (mornMonth != curMonth) {
                //不是同一个月，需要主动添加一个月
                c.add(Calendar.MONTH, -1)
                maxCount = 13
            }
        }

        for (i in 0 until maxCount) {
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1
            monthList.add(MonthInfo(year, month))

            c.add(Calendar.MONTH, 1)
        }

        //生成日期
        val calendar = Calendar.getInstance()
        for (item in monthList) {

            calendar.set(item.year, item.month - 1, 1)  // 设置这个月的第一天
            val currYear = calendar.get(Calendar.YEAR)
            val currMonth = calendar.get(Calendar.MONTH)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 获取当前这天的星期

            // 获取第一天的前面空了几天
            val offset = dayOfWeek - 1
            // 获取当月最后一天
            calendar.add(Calendar.MONTH, 1) // 下一个月
            calendar.add(Calendar.DATE, -1) // 减一天
            val totalDays = calendar.get(Calendar.DATE)  // 获取当月的天数

            for (i1 in 0 until offset) {
                item.dayList.add(DayInfo(currYear, currMonth + 1, 0, DayInfo.TYPE_DAY_PLACEHOLDER))
            }

            for (i1 in 0 until totalDays) {
                item.dayList.add(DayInfo(currYear, currMonth + 1, i1 + 1, DayInfo.TYPE_DAY_NORMAL))
            }

            val lastDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 获取最后一天的星期
            val lastOffset = 7 - lastDayOfWeek
            //填充最后几天
            for (i1 in 0 until lastOffset) {
                item.dayList.add(DayInfo(currYear, currMonth + 1, 0, DayInfo.TYPE_DAY_PLACEHOLDER))
            }
        }

        //当前前一天
        val calendarPre = Calendar.getInstance()
        calendarPre.add(Calendar.DAY_OF_MONTH, -1)
        val yearPre = calendarPre.get(Calendar.YEAR)
        val monthPre = calendarPre.get(Calendar.MONTH) + 1
        val dayPre = calendarPre.get(Calendar.DAY_OF_MONTH)
        val isNeedHandelMorn = isNeedHandelMorn()

        monthList.forEach { month ->
            month.dayList.forEach { dayInfo ->

                val monthNum = if (dayInfo.month < 10) "0${dayInfo.month}" else "${dayInfo.month}"
                dayInfo.groupName = "${dayInfo.year}年${monthNum}月"

                if (isNormalDay(dayInfo)) {
                    dayInfo.jr = RangeDateUtils.getJR(dayInfo.month, dayInfo.day)
                    dayInfo.isToday = RangeDateUtils.isToday(dayInfo.year, dayInfo.month, dayInfo.day)
                    dayInfo.isWeekend = RangeDateUtils.isWeekend(dayInfo.year, dayInfo.month, dayInfo.day)

                    if (isInDelayMode()) {
                        dayInfo.isEnableDay = RangeDateUtils.isSameDay(dayInfo, startDay)
                                || RangeDateUtils.isSameDay(dayInfo, endDay)
                                || (RangeDateUtils.isBigThanFirstDay(startDay, dayInfo) && RangeDateUtils.isBigThanFirstDay(dayInfo, endDay))

                    } else {
                        if (isNeedHandelMorn && (dayInfo.year == yearPre && dayInfo.month == monthPre && dayInfo.day == dayPre)) {
                            dayInfo.isEnableDay = true
                            dayInfo.isEnableMorn = true

                        } else {
                            dayInfo.isEnableDay = RangeDateUtils.isValidDay(dayInfo)
                        }
                    }
                }
            }
        }

        return monthList
    }

    /**
     * 计算当前选中的位置
     */
    private fun calculationCurPosition(monthList: ArrayList<MonthInfo>, info: SelectDateInfo): Pair<Int, Int> {
        var mouthIndex = 0
        var mouthRow = 0

        val startDay = RangeDateUtils.getDayInfo(if (isInHourMode()) info.hourDate else info.startDate)
        val endDay = RangeDateUtils.getDayInfo(if (isInHourMode()) info.hourDate else info.endDate)

        //当前前一天
        val calendarPre = Calendar.getInstance()
        calendarPre.add(Calendar.DAY_OF_MONTH, -1)
        val yearPre = calendarPre.get(Calendar.YEAR)
        val monthPre = calendarPre.get(Calendar.MONTH) + 1
        val dayPre = calendarPre.get(Calendar.DAY_OF_MONTH)
        val isNeedHandelMorn = isNeedHandelMorn()

        if ((RangeDateUtils.isValidDay(startDay)
                    || (isNeedHandelMorn && RangeDateUtils.isSameDay(startDay, DayInfo(yearPre, monthPre, dayPre)))) &&
            RangeDateUtils.isValidDay(endDay) &&
            (isInHourMode() || RangeDateUtils.isBigThanFirstDay(startDay, endDay))
        ) {

            //时间区域有效
            loop@ for ((index, item) in monthList.withIndex()) {
                loop2@ for ((pos, day) in item.dayList.withIndex()) {
                    if (RangeDateUtils.isSameDay(endDay, day)) {
                        day.isSelect = true
                        day.isMiddle = false
                        day.isSelectEnd = true

                        if (isInHourMode()) {
                            day.isSelectEnd = false
                            day.isHourMode = true

                            mouthIndex = index
                            mouthRow = pos / 7
                        }

                        if (isInDelayMode()) {
                            //延住
                            day.isSelectEnd = false

                            mouthIndex = index
                            mouthRow = pos / 7
                        }

                        break@loop
                    }

                    if (isInDelayMode()) {
                        continue@loop2
                    }

                    if (RangeDateUtils.isSameDay(startDay, day)) {
                        day.isSelect = true
                        day.isMiddle = false
                        day.isSelectEnd = false

                        mouthIndex = index
                        mouthRow = pos / 7

                        if (isInHourMode()) {
                            day.isHourMode = true
                            break@loop

                        } else {
                            continue@loop2
                        }
                    }

                    if (RangeDateUtils.isBigThanFirstDay(startDay, day)) {
                        day.isSelect = true
                        day.isMiddle = true
                        day.isSelectEnd = false
                        continue@loop2
                    }
                }
            }
        }

        return Pair(mouthIndex, mouthRow)
    }

    /**
     * 是否处于选择钟点房模式
     */
    private fun isInHourMode(): Boolean {
        return selectInfo?.type == RoomType.TYPE_ROOM_HOUR.type
    }

    /**
     * 是否处于延住选择模式
     */
    private fun isInDelayMode(): Boolean {
        return selectType == SelectDateType.TYPE_DELAY
    }

    /**
     * 是否需要处理凌晨选择
     */
    private fun isNeedHandelMorn(): Boolean {
        if (isInHourMode() || isInDelayMode()) {
            return false
        }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return hour < 6
    }

    private fun isNormalDay(day: DayInfo): Boolean {
        return day.type == DayInfo.TYPE_DAY_NORMAL
    }

    private fun refreshData() {
        mAdapter?.isHourMode = isInHourMode()
        mAdapter?.notifyDataSetChanged()
    }

    private fun getOnClickDayListener(): OnClickDayListener {
        return object : OnClickDayListener {
            override fun onClickDay(view: View, day: DayInfo) {
                if (isInAnim) {
                    return
                }

                val count = getSelectDayCount()

                when (count) {
                    0    -> {
                        //当前没有选中
                        day.isSelect = true
                        day.isMiddle = false
                        day.isSelectEnd = false
                        day.isHourMode = isInHourMode()

                        if (isInHourMode()) {
                            //刷新，设为入住时间
                            refreshData()

                            finishSelect(day, day)

                        } else {
                            //设为入住时间
                            checkMaxSelectAbleDays(day)
                        }
                    }

                    1    -> {
                        val firstDay = getFirstSelectDay()
                        if (RangeDateUtils.isSameDay(firstDay, day)) {

                            if (isInHourMode()) {
                                finishSelect(day, day)
                            }

                            return
                        }

                        if (isInHourMode()) {
                            //如果小于选中时间，清除
                            clearAndSetStartDay(day, true)
                            finishSelect(day, day)

                        } else {
                            //判断当前的时间是否小于选中的时间
                            if (RangeDateUtils.isBigThanFirstDay(firstDay, day)) {
                                //大于选中时间，结束当前选择,日期却完毕
                                handleSelectDays(firstDay!!, day)

                                //返回所选日期
                                finishSelect(firstDay, day)

                            } else {
                                //如果小于选中时间，清除
                                clearAndSetStartDay(day)
                                checkMaxSelectAbleDays(day)
                            }
                        }
                    }

                    else -> {
                        if (isInDelayMode()) {
                            //不是当前选中的就结束选择
                            val firstDay = getFirstSelectDay()
                            if (RangeDateUtils.isSameDay(firstDay, day)) {
                                return
                            }

                            val lastDay = getLastSelectDay()
                            if (RangeDateUtils.isSameDay(lastDay, day)) {
                                return
                            }

                            handleSelectDays(firstDay!!, day)
                            finishSelect(firstDay, day)

                        } else {
                            // 多个选中,清除所有选中，设置当前选中时间为入店时间
                            clearAndSetStartDay(day)
                            checkMaxSelectAbleDays(day)
                        }
                    }
                }
            }
        }
    }

    /**
     * 最多可选28天
     */
    private fun checkMaxSelectAbleDays(day: DayInfo) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, day.year)
        cal.set(Calendar.MONTH, day.month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day.day)
        cal.add(Calendar.DAY_OF_MONTH, 28)

        val lastMonth = cal.get(Calendar.MONTH) + 1

        val lastDay = cal.get(Calendar.DAY_OF_MONTH)
        val lastYear = cal.get(Calendar.YEAR)

        val lastInfo = DayInfo(lastYear, lastMonth, lastDay)

        dayList.forEach { dayItem ->
            //28天之后的设置为无效数据
            if (isNormalDay(dayItem) && RangeDateUtils.isBigThanFirstDay(lastInfo, dayItem)) {
                dayItem.isEnableDay = false
            }
        }

        refreshData()
    }

    /**
     * 处理选中的时间
     */
    private fun handleSelectDays(firstDay: DayInfo, endDay: DayInfo) {
        dayList.forEach { day ->
            if (isNormalDay(day) && day.isEnableDay) {
                when {
                    RangeDateUtils.isSameDay(day, firstDay)            -> {
                        //第一天
                        day.isSelect = true
                        day.isMiddle = false
                        day.isSelectEnd = false
                    }

                    RangeDateUtils.isSameDay(day, endDay)              -> {
                        //最后一天
                        day.isSelect = true
                        day.isMiddle = false
                        day.isSelectEnd = true
                    }

                    RangeDateUtils.isInDayRange(day, firstDay, endDay) -> {
                        //处于中间的
                        day.isSelect = true
                        day.isMiddle = true
                        day.isSelectEnd = false
                    }

                    else                                               -> {
                        day.isSelect = false
                        day.isMiddle = false
                        day.isSelectEnd = false
                    }
                }
            }
        }

        refreshData()
    }

    /**
     * 清除选中，并设置入店时间
     */
    private fun clearAndSetStartDay(startDay: DayInfo, isNeedRefresh: Boolean = false) {
        dayList.forEach { day ->
            if (isNormalDay(day) && day.isEnableDay) {
                day.isSelect = false
                day.isSelectEnd = false
                day.isMiddle = false

                if (RangeDateUtils.isSameDay(day, startDay)) {
                    day.isSelect = true
                }
            }
        }

        if (isNeedRefresh) {
            refreshData()
        }
    }

    /**
     * 获取总选中天数
     */
    private fun getSelectDayCount(): Int {
        var selectNum = 0

        dayList.forEach { day ->
            if (isNormalDay(day) && day.isEnableDay && day.isSelect) {
                selectNum++
            }
        }

        return selectNum
    }

    /**
     * 获取第一个选中的
     */
    private fun getFirstSelectDay(): DayInfo? {

        dayList.forEach { day ->
            if (isNormalDay(day) && day.isEnableDay && day.isSelect) {
                return day
            }
        }

        return null
    }

    /**
     * 获取最后一个选中的
     */
    private fun getLastSelectDay(): DayInfo? {
        val size = dayList.size - 1

        for (i in size downTo 0) {
            if (!isNormalDay(dayList[i]) || !dayList[i].isEnableDay) {
                continue
            }

            if (dayList[i].isSelect) {
                return dayList[i]
            }
        }

        return null
    }

    /**
     * 完成日期选择
     */
    private fun finishSelect(firstDay: DayInfo, day: DayInfo) {
        if (isInAnim) {
            return
        }
        isInAnim = true

        val firstTime = RangeDateUtils.getTime(firstDay)
        val endTime = RangeDateUtils.getTime(day)

        mHandler.postDelayed({

            isInAnim = false

            if (isInHourMode()) {
                //钟点房
                selectInfo?.hourDate = firstTime

            } else {
                selectInfo?.startDate = firstTime
                selectInfo?.endDate = endTime
                selectInfo?.count = getSelectDayCount() - 1
            }

            mListener?.onFinishSelect(selectInfo)

        }, 500)
    }

    //</editor-fold>

}