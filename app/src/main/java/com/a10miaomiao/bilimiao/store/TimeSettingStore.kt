package com.a10miaomiao.bilimiao.store

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.widget.picker.DateModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import org.kodein.di.instance
import java.util.*

class TimeSettingStore(override val di: DI) :
    ViewModel(), BaseStore<TimeSettingStore.State> {

    companion object {
        const val TIME_TYPE = "timeType"
        const val TIME_FROM = "timeFrom"
        const val TIME_TO = "timeTo"
        const val TIME_IS_LINK = "timeIsLink"
        const val TIME_CHANGE = "TIME_CHANGE"
    }

    data class State (
        var timeFrom: DateModel,
        var timeTo: DateModel,
        var rankOrder: String = "click"
    )

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
        setTime(
            read(TIME_FROM),
            read(TIME_TO)
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

    fun read(type: String): DateModel {
        val dateModel = DateModel()
        val sp = activity.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
        val timeType = sp.getInt(TIME_TYPE, 0)
        if (timeType == 0) {
            val now = Date()
            dateModel.setDate(now)
            if (type == TIME_FROM) {
                return dateModel.getTimeByGapCount(-7)
            }
            return dateModel
        }
        val timeStr = sp.getString(type, "20180909")!!
        dateModel.setValue(timeStr)
        return dateModel
    }

    fun save(timeType: Int, isLink: Boolean) {
        val sp = activity.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(TIME_FROM, state.timeFrom.getValue())
        editor.putString(TIME_TO, state.timeTo.getValue())
        editor.putBoolean(TIME_IS_LINK, isLink)
        editor.putInt(TIME_TYPE, timeType)
        editor.apply()
    }

    fun setTime(
        tFrom: DateModel,
        tTo: DateModel,
    ) {
        if (
            state.timeFrom.diff(tFrom)
            || state.timeTo.diff(tTo)
        ) {
            setState {
                timeFrom = tFrom
                timeTo = tTo
            }
        }
    }
}