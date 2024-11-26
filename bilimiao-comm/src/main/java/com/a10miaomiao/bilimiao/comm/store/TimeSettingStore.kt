package com.a10miaomiao.bilimiao.comm.store

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.store.model.DateModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import org.kodein.di.instance
import java.util.*

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

    private val activity: AppCompatActivity by instance()

    override fun copyState() = state.copy()

    override fun init(context: Context) {
        super.init(context)
        initState()
    }

    fun initState () {
        val sp = activity.getSharedPreferences(BilimiaoCommApp.APP_NAME, Context.MODE_PRIVATE)
        val timeType = readTimeType(sp)
        setTime(
            timeType,
            readTime(sp, timeType, TIME_FROM),
            readTime(sp, timeType, TIME_TO)
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

    fun readTimeType(
        sp: SharedPreferences,
    ): Int {
        return sp.getInt(TIME_TYPE, TIME_TYPE_CURRENT)
    }

    fun readTime(
        sp: SharedPreferences,
        timeType: Int,
        timeName: String,
    ): DateModel {
        val dateModel = DateModel()
        if (timeType == 0) {
            val now = Date()
            dateModel.setDate(now)
            if (timeName == TIME_FROM) {
                return dateModel.getTimeByGapCount(-7)
            }
            return dateModel
        }
        val timeStr = sp.getString(timeName, "20180909")!!
        dateModel.setValue(timeStr)
        return dateModel
    }

    fun save() {
        val sp = activity.getSharedPreferences(BilimiaoCommApp.APP_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(TIME_FROM, state.timeFrom.getValue())
        editor.putString(TIME_TO, state.timeTo.getValue())
//        editor.putBoolean(TIME_IS_LINK, isLink)
        editor.putInt(TIME_TYPE, state.timeType)
        editor.apply()
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