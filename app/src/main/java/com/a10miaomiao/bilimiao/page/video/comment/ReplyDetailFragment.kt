package com.a10miaomiao.bilimiao.page.video.comment

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.padding
import splitties.views.verticalPadding

class ReplyDetailFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "video.comment.reply"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.reply) {
                type = NavType.ParcelableType(ReplyDetailParam::class.java)
                nullable = false
            }
        }

        fun createArguments(
            reply: ReplyDetailParam
        ): Bundle {
            return bundleOf(
                MainNavArgs.reply to reply,
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = "评论内容详情"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<ReplyDetailViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        verticalLayout {
            gravity = Gravity.CENTER_HORIZONTAL
            _leftPadding = contentInsets.left + config.pagePadding
            _rightPadding = contentInsets.right + config.pagePadding
            _topPadding = contentInsets.top + config.pagePadding
            _bottomPadding = contentInsets.bottom + config.pagePadding

            views {
                +textView {
                    miaoEffect(viewModel.reply.content.message) {
                        text = it
                        verticalPadding = if (it.length > 100) dip(20) else dip(40)
                        gravity = if (it.indexOf('\n') == -1 && it.length <= 100) {
                            Gravity.CENTER
                        } else {
                            Gravity.START
                        }
                    }
                    setTextColor(config.foregroundColor)
                    textSize = 20f
                    setTextIsSelectable(true)
                }..lParams(matchParent, wrapContent)

                +rcImageView {
                    isCircle = true
                    _network(viewModel.reply.avatar, "@200w_200h")
                }..lParams {
                    height = dip(50)
                    width = dip(50)
                    topMargin = dip(20)
                }
                +textView {
                    _text = viewModel.reply.uname
                    setTextColor(config.foregroundColor)
                    setTextIsSelectable(true)
                    textSize = 16f
                }..lParams {
                    width = wrapContent
                    height = wrapContent
                    topMargin = dip(5)
                }

                +textView {
                    _text = "发表于" + NumberUtil.converCTime(viewModel.reply.ctime)
                    setTextIsSelectable(true)
                    setTextColor(config.foregroundAlpha45Color)
                }..lParams {
                    width = wrapContent
                    height = wrapContent
                    topMargin = dip(10)
                }

                +textView {
                    verticalPadding = dip(50)
                    setTextColor(config.foregroundAlpha45Color)
                    setTextIsSelectable(true)
                    gravity = Gravity.CENTER
                    _text = "长按文字可选择复制"
                }..lParams(wrapContent, wrapContent)
            }
        }.wrapInNestedScrollView()
    }

}