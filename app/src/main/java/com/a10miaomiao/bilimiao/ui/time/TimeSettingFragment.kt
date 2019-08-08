package com.a10miaomiao.bilimiao.ui.time

import android.arch.lifecycle.ViewModelProviders
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.commponents.datePickerView
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.ui.commponents.monthPickerView
import com.a10miaomiao.bilimiao.utils.*
import org.jetbrains.anko.*
import android.os.Bundle
import android.view.*
import com.a10miaomiao.bilimiao.ui.commponents.HeaderView
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.theme.ThemeViewModel
import com.a10miaomiao.miaoandriod.mergeMiaoObserver
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView


class TimeSettingFragment : SwipeBackFragment() {

    lateinit var viewModel: TimeSettingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory {
            TimeSettingViewModel(context!!)
        }).get(TimeSettingViewModel::class.java)
        return attachToSwipeBack(render().view)
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


    private fun render() = UI {
        verticalLayout {
            headerView {
                title("时间线设置")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick {
                    pop()
                }
            }
            nestedScrollView {
                verticalLayout {
                    body()
                }
            }
        }
    }

    private fun ViewManager.body() = verticalLayout {
        val observeSpinner = +viewModel.spinnerSelected
        val observeTimeFrom = +viewModel.timeFrom
        val observeTimeTo = +viewModel.timeTo
        val observeGapCount = +viewModel.gapCount
        val observeIsLink = +viewModel.isLink
        val observeAll = mergeMiaoObserver(
                observeSpinner,
                observeTimeFrom,
                observeTimeTo
        )

        linearLayout {
            applyRecursively(ViewStyle.block)
            textView("模式：")
            spinner {
                val mAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item
                        , arrayOf("当前时间线", "按月份", "自定义"))
                mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                adapter = mAdapter
                onItemChanged(viewModel::changedSpinnerItem)

                observeSpinner {
                    setSelection(it)
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
                observeSpinner { isShow(it == 0) }
                textView("活在当下，无需改变")
            }

            // 按月份
            linearLayout {
                gravity = Gravity.CENTER
                observeSpinner { isShow(it == 1) }
                monthPickerView {
                    observeAll { date = viewModel.timeFrom() }
                    onChanged = viewModel::changedMonthPicker
                }.lparams { verticalMargin = config.dividerSize }
            }

            // 自定义
            verticalLayout {
                gravity = Gravity.CENTER
                observeSpinner { isShow(it == 2) }
                datePickerView {
                    observeAll { date = viewModel.timeFrom() }
                    onChanged = viewModel::changedTimeFromPicker
                }.lparams {
                    verticalMargin = config.dividerSize
                }
                textView("至").lparams { horizontalMargin = dip(10) }
                datePickerView {
                    observeAll{date = viewModel.timeTo() }
                    onChanged = viewModel::changedTimeToPicker
                }.lparams {
                    verticalMargin = config.dividerSize
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL
                    textView {
                        observeGapCount {
                            text = "时间间隔：${it}天"
                        }
                    }.lparams { rightMargin = dip(5) }
                    textView {
                        observeIsLink { text = if (it) "已锁定" else "未锁定" }
                    }
                    switch {
                        observeIsLink { isChecked = it }
                        observeGapCount { isEnabled = !(it < 0 || it > 31) }
                        setOnCheckedChangeListener { compoundButton, b -> viewModel.isLink set b }
                        textOn = "已锁定"
                        textOff = "未锁定"
                    }
                }
                textView {
                    textColorResource = R.color.red
                    observeGapCount {
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
                textView { observeAll { text = viewModel.timeFrom().getValue("-") } }
                textView(" 至 ")
                textView { observeAll { text = viewModel.timeTo().getValue("-") } }
            }.lparams { topMargin = dip(5) }
        }.lparams(width = matchParent) { topMargin = config.dividerSize }

        textView("确定") {
            textColorResource = attr(R.attr.colorPrimary)
            selectableItemBackground()
            applyRecursively(ViewStyle.block)
            gravity = Gravity.CENTER
            setOnClickListener {
                viewModel.saveTime()
                pop()
            }
            observeGapCount {
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