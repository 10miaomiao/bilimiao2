package com.a10miaomiao.bilimiao.page.filter

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.padding
import splitties.views.textColorResource

class FilterAddWordFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "filter.add.word"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://filter/word/add")
        }
    }

    override val pageConfig = myPageConfig {
        title = "添加屏蔽关键字"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val windowStore by instance<WindowStore>()
    private val filterStore by instance<FilterStore>()
    private val supportHelper by instance<SupportHelper>()

    private val ID_editText = 100

    private lateinit var mEditText: EditText

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
        mEditText = view.findViewById(ID_editText)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    override fun onResume() {
        super.onResume()
        supportHelper.showSoftInput(mEditText)
    }

    override fun onPause() {
        super.onPause()
        supportHelper.hideSoftInput(mEditText)
    }

    val handleEditorAction = TextView.OnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
            && event.action == KeyEvent.ACTION_DOWN) {
            handleOkClick.onClick(v)
            return@OnEditorActionListener true
        }
        return@OnEditorActionListener false
    }
    val handleOkClick = View.OnClickListener {
        val keyword = mEditText.text.toString()
        if (keyword.isEmpty()) {
            toast("内容不能为空")
        } else {
            filterStore.addWord(keyword)
            findNavController().popBackStack()
        }
    }

    val ui = miaoBindingUi {
        val insets = windowStore.getContentInsets(parentView)
        verticalLayout {
            _leftPadding = insets.left
            _topPadding = insets.top
            _rightPadding = insets.right
            _bottomPadding = insets.bottom

            views {
                +verticalLayout {
                    apply(ViewStyle.block)

                    views {
                        +textView {
                            text = "输入关键字："
                        }
                        +editText(ID_editText) {
                            setOnEditorActionListener(handleEditorAction)
                        }
                        +textView {
                            text = "注：支持正则表达式（语法：/正则表达式主体/）"
                        }
                    }

                }..lParams(width = matchParent) {
                    topMargin = config.dividerSize
                }

                +frameLayout {
                    backgroundColor = config.blockBackgroundColor

                    views {
                        +textView {
                            text = "确定"
                            padding = config.dividerSize
                            textColorResource = config.themeColorResource
                            setBackgroundResource(config.selectableItemBackground)
                            gravity = Gravity.CENTER
                            setOnClickListener(handleOkClick)
                        }
                    }
                }..lParams(width = matchParent) {
                    topMargin = config.dividerSize
                }
            }

        }.wrapInNestedScrollView()
    }

}