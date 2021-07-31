package com.a10miaomiao.bilimiao.ui.video

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.bottomSheetHeaderView
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI

class CoinFragment : BottomSheetDialogFragment() {

    val TAG = "CoinFragment"

    companion object {
        fun newInstance(viewModel: VideoInfoViewModel): CoinFragment {
            val fragment = CoinFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            fragment.viewModel = viewModel
            return fragment
        }
    }

    lateinit var viewModel: VideoInfoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return createUI().view
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }

    private fun createUI() = UI {
        verticalLayout {
            backgroundColor = config.blockBackgroundColor
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
            setOnClickListener {  }
            val observeCoinNum = viewModel.coinNum.observe()

            textView {
                observeCoinNum{
                    text = "给UP主投上${it}枚硬币"
                }
                textSize = 18f
                textColor = config.foregroundColor
            }.lparams(wrapContent, wrapContent) {
                topMargin = dip(20)
            }

            linearLayout {

                verticalLayout {
                    gravity = Gravity.CENTER
                    imageView {
                        observeCoinNum {
                            imageResource = if (it == 1) {
                                R.drawable.bili_22
                            } else {
                                R.drawable.bili_22_gray
                            }
                            post {
                                if (drawable is AnimationDrawable) {
                                    (drawable as AnimationDrawable).start()
                                }
                            }
                        }
                    }.lparams(dip(100), wrapContent)
                    setOnClickListener {
                        viewModel.coinNum set 1
                    }
                }.lparams(matchParent, matchParent) {
                    weight = 1f
                }
                verticalLayout {
                    gravity = Gravity.CENTER
                    (viewModel.info.observe()) {
                        visibility = if (it?.copyright == 2) {
                            View.GONE
                        } else {
                            View.VISIBLE
                        }
                    }
                    imageView {
                        observeCoinNum {
                            imageResource = if (it == 2) {
                                R.drawable.bili_33
                            } else {
                                R.drawable.bili_33_gray
                            }
                            post {
                                if (drawable is AnimationDrawable) {
                                    (drawable as AnimationDrawable).start()
                                }
                            }
                        }
                    }.lparams(dip(100), wrapContent)
                    setOnClickListener {
                        viewModel.coinNum set 2
                    }
                }.lparams(matchParent, matchParent) {
                    weight = 1f
                }
            }.lparams(matchParent, wrapContent)

            frameLayout {
                backgroundColor = config.windowBackgroundColor
                verticalLayout {
                    selectableItemBackground()
                    gravity = Gravity.CENTER

                    textView("确认投币"){
                        textColor = config.foregroundAlpha45Color
                        gravity = Gravity.CENTER
                    }

                    setOnClickListener {
                        dismiss()
                        viewModel.confirmCoin()
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                }
            }.applyRecursively(ViewStyle.roundRect(dip(24))).lparams {
                width = matchParent
                height = dip(48)
                bottomMargin = dip(20)
                horizontalMargin = dip(20)
            }

        }
    }
}