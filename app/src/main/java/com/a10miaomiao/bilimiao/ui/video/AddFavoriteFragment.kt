package com.a10miaomiao.bilimiao.ui.video

import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
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
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI

class AddFavoriteFragment : BottomSheetDialogFragment() {

    val TAG = "AddFavoriteFragment"

    companion object {
        fun newInstance(viewModel: VideoInfoViewModel): AddFavoriteFragment {
            val fragment = AddFavoriteFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            fragment.viewModel = viewModel
            return fragment
        }
    }

    lateinit var viewModel: VideoInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return createUI().view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.viewModel.favoriteSelectedMap.clear()
        this.viewModel.loadFavoriteCreatedList()
    }

    fun show(manager: FragmentManager) {
        show(manager, TAG)
    }

    private fun createUI() = UI {
        verticalLayout {
            backgroundColor = config.windowBackgroundColor
            bottomSheetHeaderView("选择收藏夹", View.OnClickListener {
                MainActivity.of(context)
                    .hideBottomSheet()
            })
            frameLayout {
                recyclerView {
                    layoutManager = LinearLayoutManager(context)
                    miao(viewModel.favoriteCreatedList) {
                        itemView { b ->
                            linearLayout {
                                padding = config.dividerSize
                                gravity = Gravity.CENTER_VERTICAL
                                selectableItemBackground()

                                verticalLayout {
                                    textView {
                                        textSize = 18f
                                        textColor = config.foregroundColor
                                        b.bind { text = it.title }
                                    }
                                    textView {
                                        textColor = config.foregroundAlpha45Color
                                        b.bind { text = "${it.media_count}个内容" }
                                    }
                                }.lparams { weight = 1f }

                                checkBox {
                                    rightPadding = config.dividerSize
                                    b.bindIndexed { item, index ->
                                        isChecked = item.fav_state == 1
                                        tag = index
                                    }
                                    setOnCheckedChangeListener { buttonView, isChecked ->
                                        if (buttonView.tag is Int) {
                                            val index = buttonView.tag as Int
                                            val item = viewModel.favoriteCreatedList[index]
                                            viewModel.favoriteSelectedMap[item.id] = isChecked
                                        }
                                    }
                                }

                            }.lparams(matchParent, wrapContent)
                        }
                        onItemClick { item, position ->

                        }
//                    observePlayer {
//                        notifyDataSetChanged()
//                    }
                    }
                }

                progressBar {
                    indeterminateTintList = ColorStateList.valueOf(config.themeColor)
                    (viewModel.favoriteLoading.observe()) {
                        visibility = if (it) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }.lparams {
                    width = dip(48)
                    height = dip(48)
                    gravity = Gravity.CENTER
                }
            }.lparams(matchParent, matchParent) {
                weight = 1f
            }

            linearLayout {
                backgroundColor = config.blockBackgroundColor
                gravity = Gravity.CENTER_VERTICAL

                textView {
                    gravity = Gravity.CENTER
                    selectableItemBackground()

                    setOnClickListener {
                        if (
                            viewModel.favoriteCreatedList.find {
                                if (it.fav_state == 1) {
                                    !(viewModel.favoriteSelectedMap[it.id] ?: true)
                                } else {
                                    viewModel.favoriteSelectedMap[it.id] ?: false
                                }
                            } != null
                        ) {
                            viewModel.addFavorite()
                        } else {
                            toast("请选择收藏夹")
                        }
                    }
                    text = "完成"
                }.lparams {
                    height = matchParent
                    weight = 1f
                }
            }.lparams(matchParent, dip(48))
        }
    }
}