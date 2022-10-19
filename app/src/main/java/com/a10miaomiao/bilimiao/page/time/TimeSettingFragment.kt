package com.a10miaomiao.bilimiao.page.time

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.datePickerView
import com.a10miaomiao.bilimiao.widget.monthPickerView
import com.a10miaomiao.bilimiao.widget.rangeDateView
import com.a10miaomiao.bilimiao.widget.rangedate.RangeDateUtils
import com.a10miaomiao.bilimiao.widget.rangedate.model.SelectDateInfo
import com.a10miaomiao.bilimiao.widget.rangedate.model.SelectDateType
import kotlinx.coroutines.launch
import org.kodein.di.*
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.appcompat.switch
import splitties.views.dsl.core.*

class TimeSettingFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "时光姬-时间线设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<TimeSettingViewModel>(di)

    private val windowStore by instance<WindowStore>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
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

    val handleOkClick = View.OnClickListener {
        viewModel.saveTime()
        findNavController().popBackStack()
    }

    val handleCheckedChange = CompoundButton.OnCheckedChangeListener { _, b ->
        ui.setState {
            viewModel.isLink = b
        }
    }

    fun MiaoUI.currentView(): View {
        return horizontalLayout {
            padding = dip(20)
            gravity = Gravity.CENTER
            views {
                +textView {
                    text = "活在当下，无需改变"
                }
            }
        }
    }

    fun MiaoUI.monthView(): View {
        return horizontalLayout {
            gravity = Gravity.CENTER

            views {
                +monthPickerView {
                    miaoEffect(null) {
                        date = viewModel.timeFrom
                    }
                    onChanged = viewModel::changedMonthPicker
                }..lParams {
                    verticalMargin = config.dividerSize
                }
            }

        }
    }

    private var mSelectDateType: SelectDateType = SelectDateType.TYPE_NORMAL
    private var mSelectDateInfo: SelectDateInfo = RangeDateUtils.getDefaultSelectDate()

    fun MiaoUI.customView(): View {
        return verticalLayout {
            gravity = Gravity.CENTER

            views {
                +rangeDateView {
                    initDate(mSelectDateType, mSelectDateInfo)
                }..lParams(matchParent, wrapContent)
            }

        }

    }

    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)
        verticalLayout {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right

            views {
                +horizontalLayout {
                    setBackgroundResource(config.blockBackgroundResource)
                    backgroundColor = config.blockBackgroundColor
                    horizontalPadding = config.pagePadding
                    bottomPadding = config.dividerSize
                    _topPadding = contentInsets.top + config.dividerSize

                    views {
                        +textView {
                            text = "模式："
                        }
                        +spinner {
                            val mAdapter = ArrayAdapter<String>(
                                context,
                                android.R.layout.simple_spinner_item,
                                arrayOf("当前时间线", "按月份", "自定义")
                            )
                            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            adapter = mAdapter
                            miaoEffect(viewModel.spinnerSelected) {
                                setSelection(it)
                            }
                            onItemChanged(viewModel::changedSpinnerItem)
                        }..lParams(width = wrapContent)
                    }

                }..lParams(width = matchParent)

                +verticalLayout {
                    padding = config.pagePadding
                    apply(ViewStyle.roundRect(dip(10)))
                    backgroundColor = config.blockBackgroundColor

                    views {
                        +textView {
                            text = "调整时间线："
                        }
                        // 当前时间线
                        +currentView().apply {
                            _show = viewModel.spinnerSelected == 0
                        }
                        // 按月份
                        +monthView().apply {
                            _show = viewModel.spinnerSelected == 1
                        }
                        // 自定义
                        +customView().apply {
                            _show = viewModel.spinnerSelected == 2
                        }

                        +horizontalLayout {

                            views {
                                +textView{
                                    text = "已选择时间线："
                                }
                                +textView {
                                    _text = viewModel.timeFrom.getValue("-")
                                }
                                +textView{
                                    text = " 至 "
                                }
                                +textView {
                                    _text = viewModel.timeTo.getValue("-")
                                }
                            }
                        }..lParams {
                            topMargin = dip(5)
                        }
                    }
                }.wrapInNestedScrollView {
                    padding = config.pagePadding
                }..lParams(width = matchParent) {
                    weight = 1f
                }

                +frameLayout {
                    _bottomPadding = contentInsets.bottom + config.dividerSize
                    topPadding = config.dividerSize
                    horizontalPadding = config.pagePadding

                    setBackgroundColor(config.blockBackgroundColor)

                    views {
                        +frameLayout {
                            setBackgroundColor(config.windowBackgroundColor)
                            apply(ViewStyle.roundRect(dip(24)))
                            setOnClickListener(handleOkClick)

                            views {
                                +textView{
                                    setBackgroundResource(config.selectableItemBackground)
                                    gravity = Gravity.CENTER
                                    text = "确定"
                                    setTextColor(config.foregroundAlpha45Color)
                                    gravity = Gravity.CENTER
                                }
                            }

                        }..lParams {
                            width = matchParent
                            height = dip(48)
                        }
                    }
                }
            }
        }
    }

}