package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.store.model.DateModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.kodein.di.DI
import org.kodein.di.instance

class TimeSettingStore(override val di: DI) :
    ViewModel(), BaseStore<TimeSettingStore.State> {

    companion object {
        const val TIME_TYPE = "timeType"
        const val TIME_TYPE_CURRENT = 0
        const val TIME_TYPE_MONTH = 1
        const val TIME_TYPE_CUSTOM = 2
        const val TIME_FROM = "timeFrom"
        const val TIME_TO = "timeTo"
        const val TIME_IS_LINK = "timeIsLink"
        const val TIME_CHANGE = "TIME_CHANGE"
    }

    data class State (
        var timeFrom: DateModel,
        var timeTo: DateModel,
        var timeType: Int = TIME_TYPE_CURRENT,
        var rankOrder: String = "click"
    ) {
        fun getRankOrderKey(): Int {
            return when (rankOrder) {
                "click" -> 0
                "scores" -> 1
                "stow" -> 2
                "coin" ->3
                "dm" -> 4
                else -> 0
            }
        }
    }

    override val stateFlow = MutableStateFlow(State(
        timeFrom = DateModel(),
        timeTo = DateModel(),
    ))

    private val settings: SettingsProvider by instance()

    override fun copyState() = state.copy()

    override fun init() {
        super.init()
        initState()
    }

    fun initState () {
        val timeType = readTimeType()
        setTime(
            timeType,
            readTime(timeType, TIME_FROM),
            readTime(timeType, TIME_TO)
        )
    }

    fun getRankOrderText(): String {
        return when (state.rankOrder) {
            "click" -> "播放数"
            "scores" -> "评论数"
            "stow" -> "收藏数"
            "coin" -> "硬币数"
            "dm" -> "弹幕数"
            else -> "播放数"
        }
    }

    fun setRankOrder(key: Int) {
        setState {
            rankOrder = arrayOf("click", "scores", "stow", "coin", "dm")[key]
        }
    }

    private fun readTimeType(): Int {
        return settings.getInt(TIME_TYPE, TIME_TYPE_CURRENT)
    }

    private fun readTime(
        timeType: Int,
        timeName: String,
    ): DateModel {
        val dateModel = DateModel()
        if (timeType == 0) {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            dateModel.setDate(today)
            if (timeName == TIME_FROM) {
                return dateModel.getTimeByGapCount(-7)
            }
            return dateModel
        }
        val timeStr = settings.getString(timeName, "20180909")
        dateModel.setValue(timeStr)
        return dateModel
    }

    fun save() {
        settings.edit()
            .putString(TIME_FROM, state.timeFrom.getValue())
            .putString(TIME_TO, state.timeTo.getValue())
            .putInt(TIME_TYPE, state.timeType)
            .apply()
    }

    fun setTime(
        tType: Int,
        tFrom: DateModel,
        tTo: DateModel,
    ) {
        if (
            tType != state.timeType
            || state.timeFrom != tFrom
            || state.timeTo != tTo
        ) {
            setState {
                timeType= tType
                timeFrom = tFrom
                timeTo = tTo
            }
        }
    }
}
