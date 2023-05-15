package com.a10miaomiao.bilimiao.page.video

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.NavHostFragment
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._imageResource
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.dsl.core.*

class VideoCoinFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "video.coin"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.num) {
                type = NavType.IntType
                defaultValue = 1
            }
        }

        fun createArguments(
            num: Int,
        ): Bundle {
            return bundleOf(
                MainNavArgs.num to num,
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = "投币"
    }

    override val di: DI by lazyUiDi(ui = { ui })

//    private val viewModel by diViewModel<VideoCoinViewModel>(di)
    val maxNum by lazy { requireArguments().getInt(MainNavArgs.num, 1) }

    var coinNum = 1

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

    val handleConfirmClick = View.OnClickListener {
        val navHostFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val fragment = navHostFragment.childFragmentManager.primaryNavigationFragment
        if (fragment is VideoInfoFragment) {
            fragment.confirmCoin(coinNum)
        }
        Navigation.findNavController(it).popBackStack()
    }

    private val handleCoinNumClick = View.OnClickListener {
        val num = (it.tag ?: 1) as Int
        ui.setState {
            coinNum = num
        }
    }

    fun MiaoUI.coinView(): View {
        return horizontalLayout {
            views {
                +verticalLayout {
                    gravity = Gravity.CENTER
                    tag = 1
                    setOnClickListener(handleCoinNumClick)

                    views {
                        +imageView {
                            _imageResource = if (coinNum == 1) {
                                R.drawable.bili_22
                            } else {
                                R.drawable.bili_22_gray
                            }
                            miaoEffect(coinNum) {
                                if (drawable is AnimationDrawable) {
                                    (drawable as AnimationDrawable).start()
                                }
                            }
                        }..lParams(dip(100), wrapContent)
                    }

                }..lParams(matchParent, matchParent) {
                    weight = 1f
                }

                +verticalLayout {
                    gravity = Gravity.CENTER
                    tag = 2
                    _show = maxNum == 2
                    setOnClickListener(handleCoinNumClick)

                    views {
                        +imageView {
                            _imageResource = if (coinNum == 2) {
                                R.drawable.bili_33
                            } else {
                                R.drawable.bili_33_gray
                            }
                            miaoEffect(coinNum) {
                                if (drawable is AnimationDrawable) {
                                    (drawable as AnimationDrawable).start()
                                }
                            }
                        }..lParams(dip(100), wrapContent)
                    }

                }..lParams(matchParent, matchParent) {
                    weight = 1f
                }
            }
        }
    }

    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)

        verticalLayout {
            setBackgroundColor(config.blockBackgroundColor)
            _topPadding = contentInsets.top
            _bottomPadding = contentInsets.bottom
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right
            gravity = Gravity.CENTER_HORIZONTAL

            views {
                +coinView()..lParams(matchParent, wrapContent) {
                    weight = 1f
                }
                +textView {
                    _text = "给UP主投上${coinNum}枚硬币"
                    textSize = 18f
                    setTextColor(config.foregroundColor)
                }..lParams(wrapContent, wrapContent) {
                    margin = dip(20)
                }
                +frameLayout {
                    setBackgroundColor(config.windowBackgroundColor)
                    apply(ViewStyle.roundRect(dip(24)))
                    setOnClickListener(handleConfirmClick)

                    views {
                        +textView{
                            setBackgroundResource(config.selectableItemBackground)
                            gravity = Gravity.CENTER
                            text = "确认投币"
                            setTextColor(config.foregroundAlpha45Color)
                            gravity = Gravity.CENTER
                        }
                    }

                }..lParams {
                    width = matchParent
                    height = dip(48)
                    bottomMargin = dip(20)
                    horizontalMargin = dip(20)
                }
            }
        }
    }

}