package com.a10miaomiao.bilimiao.ui.time

import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.base.BaseFragment
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.commponents.datePickerView
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.ui.commponents.monthPickerView
import com.a10miaomiao.bilimiao.utils.*
import org.jetbrains.anko.*
import android.os.Bundle
import android.view.ViewManager
import com.a10miaomiao.bilimiao.ui.commponents.HeaderView
import com.a10miaomiao.miaoandriod.binding.MiaoBindingImpl
import com.a10miaomiao.miaoandriod.binding.bind
import org.jetbrains.anko.support.v4.nestedScrollView


class TimeSettingFragment : BaseFragment() {

    var spinnerSelected by binding.miao(0)
    var timeFrom = DateModel(binding)
    var timeTo = DateModel(binding)
    var isLink by binding.miao(false)
    var gapCount by binding.miao(0)
    var headerView: HeaderView? = null

    override fun initView() {
        isLink = SettingUtil.getBoolean(context!!, ConstantUtil.TIME_IS_LINK, false)
        spinnerSelected = SettingUtil.getInt(context!!, ConstantUtil.TIME_TYPE, 0)
        timeFrom.read(context!!, ConstantUtil.TIME_FROM)
        timeTo.read(context!!, ConstantUtil.TIME_TO)
        headerView!!.fitsSystemWindows = true
    }

    private fun View.isShow(ishow: Boolean) {
        visibility = if (ishow) View.VISIBLE else View.GONE
    }

    private fun Spinner.onItemChanged(onChanged: (position: Int) -> Unit) {
        this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                onChanged(p2)
            }
        }
    }

    override fun onReadInstanceState(state: Bundle) {
        spinnerSelected = state.getInt("spinnerSelected")
        isLink = state.getBoolean("isLink")
        timeFrom.year = state.getInt("timeFrom.year")
        timeFrom.month = state.getInt("timeFrom.month ")
        timeFrom.date = state.getInt("timeFrom.date")
        timeTo.year = state.getInt("timeTo.year")
        timeTo.month = state.getInt("timeTo.month")
        timeTo.date = state.getInt("timeTo.date")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isLink", isLink)
        outState.putInt("spinnerSelected", spinnerSelected)
        outState.putInt("timeFrom.year", timeFrom.year)
        outState.putInt("timeFrom.month", timeFrom.month)
        outState.putInt("timeFrom.date", timeFrom.date)
        outState.putInt("timeTo.year", timeFrom.year)
        outState.putInt("timeTo.month", timeFrom.month)
        outState.putInt("timeTo.date", timeFrom.date)

    }

    private fun saveTime() {
        SettingUtil.putBoolean(context!!, ConstantUtil.TIME_IS_LINK, isLink)
        SettingUtil.putInt(context!!, ConstantUtil.TIME_TYPE, spinnerSelected)
        timeFrom.save(context!!, ConstantUtil.TIME_FROM)
        timeTo.save(context!!, ConstantUtil.TIME_TO)
    }

    override fun render() = MiaoUI {
        verticalLayout {
            headerView {
                title("时间线设置")
                let { headerView = it }
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick {
                    pop()
                }
            }
            nestedScrollView {
                verticalLayout {
                    body(this, this@MiaoUI.binding)
                }
            }
        }
    }

    private fun body(viewManager: ViewManager, binding: MiaoBindingImpl) = viewManager.verticalLayout {
        linearLayout {
            applyRecursively(ViewStyle.block)
            textView("模式：")
            spinner {
                val mAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item
                        , arrayOf("当前时间线", "按月份", "自定义"))
                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                adapter = mAdapter
                binding.bind(::spinnerSelected) { setSelection(it) }
                onItemChanged {
                    spinnerSelected = it
                    when (it) {
                        0 -> {
                            val time = DateModel(binding)
                            time.now()
                            timeFrom.setValue(time.getTimeByGapCount(-7)) //最近7天
                            timeTo.setValue(time)
                        }
                        1 -> {
                            timeFrom.date = 1
                            timeTo.setValue(timeFrom.getTimeToByMonth())
                            binding.updateView()
                        }
                        2 -> {
                            binding.updateView()
                            gapCount = timeFrom.getGapCount(timeTo)
                        }
                    }
                }
            }.lparams(width = wrapContent)
        }.lparams(width = matchParent) { topMargin = config.dividerSize }

        verticalLayout {
            applyRecursively(ViewStyle.block)
            textView("调整时间线：")

            // 当前时间线
            linearLayout {
                padding = dip(20)
                gravity = Gravity.CENTER
                binding.bind(::spinnerSelected) { isShow(it == 0) }
                textView("活在当下，无需改变")
            }

            // 按月份
            linearLayout {
                gravity = Gravity.CENTER
                binding.bind(::spinnerSelected) { isShow(it == 1) }
                monthPickerView {
                    binding.bind { date.setValue(timeFrom) }
                }.onChanged {
                    timeFrom.setValue(it.getTimeFromByMonth())
                    timeTo.setValue(it.getTimeToByMonth())
                }.lparams { verticalMargin = config.dividerSize }
            }

            // 自定义
            verticalLayout {
                gravity = Gravity.CENTER
                binding.bind(::spinnerSelected) { isShow(it == 2) }
                datePickerView {
                    binding.bind { date.setValue(timeFrom) }
                    onChanged {
                        timeFrom.setValue(it)
                        if (isLink) {
                            timeTo.setValue(timeFrom.getTimeByGapCount(gapCount))
                        } else {
                            gapCount = timeFrom.getGapCount(timeTo)
                        }
                    }
                }.lparams {
                    verticalMargin = config.dividerSize
                }
                textView("至").lparams { horizontalMargin = dip(10) }
                datePickerView {
                    binding.bind { date.setValue(timeTo) }
                    onChanged {
                        timeTo.setValue(it)
                        if (isLink) {
                            timeFrom.setValue(timeTo.getTimeByGapCount(-gapCount))
                        } else {
                            gapCount = timeFrom.getGapCount(timeTo)
                        }
                    }
                }.lparams {
                    verticalMargin = config.dividerSize
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL
                    textView {
                        binding.bind(::gapCount) {
                            text = "时间间隔：${gapCount}天"
                        }
                    }.lparams { rightMargin = dip(5) }
                    textView {
                        binding.bind(::isLink) { text = if (it) "已锁定" else "未锁定" }
                    }
                    switch {
                        binding.bind(::isLink) { isChecked = it }
                        binding.bind(::gapCount) { isEnabled = !(it < 0 || it > 31) }
                        setOnCheckedChangeListener { compoundButton, b -> isLink = b }
                        textOn = "已锁定"
                        textOff = "未锁定"
                    }
                }
                textView {
                    textColorResource = R.color.red
                    binding.bind(::gapCount) {
                        if (it < 0) {
                            visibility = View.VISIBLE
                            text = "起始时间不能大于结束时间"
                        } else if (it > 30) {
                            visibility = View.VISIBLE
                            text = "时间跨度不能超过30天"
                        } else {
                            visibility = View.GONE
                        }
                    }
                }

            }

            linearLayout {
                textView("已选择时间线：")
                textView { binding.bind { text = timeFrom.getValue("-") } }
                textView(" 至 ")
                textView { binding.bind { text = timeTo.getValue("-") } }
            }.lparams { topMargin = dip(5) }
        }.lparams(width = matchParent) { topMargin = config.dividerSize }

        textView("确定") {
            textColorResource = attr(R.attr.colorPrimary)
            selectableItemBackground()
            applyRecursively(ViewStyle.block)
            gravity = Gravity.CENTER
            setOnClickListener {
                saveTime()
                RxBus.getInstance().send(ConstantUtil.TIME_CHANGE)
                pop()
            }
            binding.bind(::gapCount) {
                if (it < 0 || it > 30) {
                    isEnabled = false
                    textColorResource = R.color.gray
                } else {
                    isEnabled = true
                    textColorResource = attr(R.attr.colorPrimary)
                }
            }
        }.lparams(width = matchParent) { verticalMargin = config.dividerSize }
    }
}