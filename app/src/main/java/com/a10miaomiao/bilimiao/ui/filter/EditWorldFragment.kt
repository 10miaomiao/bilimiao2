package com.a10miaomiao.bilimiao.ui.filter

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.attr
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.nestedScrollView

class EditWorldFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(keyword: String): EditWorldFragment {
            val fragment = EditWorldFragment()
            val bundle = Bundle()
            bundle.putString("keyword", keyword)
            fragment.arguments = bundle
            return fragment
        }
    }

    lateinit var editText: EditText
    lateinit var filterStore: FilterStore

    private val _keyword by lazy { arguments!!.getString("keyword") }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        filterStore = MainActivity.of(context!!).filterStore
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

    private fun setKeyword(){
        val keyword = editText.text.toString()
        if (keyword.isEmpty()) {
            alert("内容不能为空")
            return
        }
        filterStore.setWord(_keyword, keyword)
        pop()
    }

    private fun render() = UI {
        verticalLayout {
            headerView {
                title("编辑屏蔽关键字")
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
                            setText(_keyword)
                            editText = this
                            setOnEditorActionListener { v, actionId, event ->
                                DebugMiao.log(actionId)
                                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                                        && event.action == KeyEvent.ACTION_DOWN) {
                                    setKeyword()
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
                        backgroundColor = Color.WHITE
                        textView("确定") {
                            padding = config.dividerSize
                            textColorResource = attr(R.attr.colorPrimary)
                            selectableItemBackground()
                            gravity = Gravity.CENTER
                            setOnClickListener{ setKeyword() }
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