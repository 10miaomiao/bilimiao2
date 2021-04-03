package com.a10miaomiao.bilimiao.ui.filter

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.EditText
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.bottomSheetHeaderView
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.utils.attr
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.nestedScrollView
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.inputmethod.EditorInfo
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.utils.DebugMiao


class AddWorldFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(): AddWorldFragment {
            val fragment = AddWorldFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    lateinit var editText: EditText

    lateinit var filterStore: FilterStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        filterStore = Store.from(context!!).filterStore
        return attachToSwipeBack(render().view)
    }

    override fun onResume() {
        super.onResume()
        showSoftInput(editText)
    }

    override fun onPause() {
        super.onPause()
        hideSoftInput()
    }

    private fun addKeyword(){
        val keyword = editText.text.toString()
        if (keyword.isEmpty()) {
            alert("内容不能为空")
            return
        }
        filterStore.addWord(keyword)
        pop()
    }

    private fun render() = UI {
        verticalLayout {
            backgroundColor = config.windowBackgroundColor

            headerView {
                title("添加屏蔽关键字")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick {
                    pop()
                }
            }

            nestedScrollView {
                verticalLayout {
                    verticalLayout {
                        applyRecursively(ViewStyle.block)
                        textView("输入关键字：")
                        editText {
                            editText = this
                            setOnEditorActionListener { v, actionId, event ->
                                DebugMiao.log(actionId)
                                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                                        && event.action == KeyEvent.ACTION_DOWN) {
                                    addKeyword()
                                    return@setOnEditorActionListener true
                                }
                                return@setOnEditorActionListener false
                            }
                        }
                        textView("注：支持正则表达式（语法：/正则表达式主体/）")
                    }.lparams(width = matchParent) {
                        topMargin = config.dividerSize
                    }

                    frameLayout {
                        backgroundColor = config.blockBackgroundColor
                        textView("确定") {
                            padding = config.dividerSize
                            textColorResource = attr(R.attr.colorPrimary)
                            selectableItemBackground()
                            gravity = Gravity.CENTER
                            setOnClickListener{ addKeyword() }
                        }
                    }.lparams(width = matchParent) {
                        topMargin = config.dividerSize
                    }


                }.lparams {
                    width = matchParent
                    bottomMargin = config.dividerSize
                }
            }
        }
    }

}