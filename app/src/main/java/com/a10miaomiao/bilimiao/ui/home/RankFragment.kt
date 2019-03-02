package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.rcLayout
import com.a10miaomiao.bilimiao.ui.rank.RankCategoryDetailsFragment
import com.a10miaomiao.bilimiao.ui.rank.RankCategoryFragment
import com.a10miaomiao.bilimiao.ui.region.RegionDetailsViewModel
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.binding.bind
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.BlurTransformation
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.support.v4.swipeRefreshLayout


class RankFragment : Fragment() {

    lateinit var viewModel: RankViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(RankViewModel::class.java)
        return render().view
    }

    private fun render() = UI {
        verticalLayout {
            backgroundColor = config.background
            headerView {
                title("排行榜")
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    RxBus.getInstance().send(ConstantUtil.OPEN_DRAWER)
                }
            }

            swipeRefreshLayout {
                setColorSchemeResources(R.color.colorPrimary)
                viewModel.bind(viewModel::loading) { isRefreshing = it }
                setOnRefreshListener { viewModel.refreshList() }
                recyclerView {
//                    backgroundColor = Color.WHITE
                    createAdapter()
                }
            }
        }
    }

    private fun RecyclerView.createAdapter() = miao(viewModel.list) {
        layoutManager(LinearLayoutManager(context))
        itemView { binding ->
            rcLayout {
                lparams(matchParent, dip(120)) {
                    verticalMargin = dip(2.5f)
                }
                roundCorner = dip(5)
                padding = dip(5)

                frameLayout {
                    lparams(matchParent, matchParent)
                    selectableItemBackground()
                    imageView {
                        // scaleType = ImageView.ScaleType.CENTER
                        binding.bind { item ->
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            Glide.with(context)
                                    .load(item.rank_pic)
                                    .bitmapTransform(BlurTransformation(context, 14, 6)) // 高斯模糊
                                    .into(this)
                        }
                    }.lparams(matchParent, matchParent)
                    verticalLayout {
                        lparams(matchParent, matchParent)
                        gravity = Gravity.CENTER

                        textView {
                            paint.isFakeBoldText = true
                            gravity = Gravity.CENTER
                            textSize = 16f
                            textColor = Color.WHITE
                            binding.bind { item -> text = item.rank_name }
                        }
                        textView {
                            textColor = Color.WHITE
                            gravity = Gravity.CENTER
                            textSize = 14f
                            binding.bind { item -> text = item.rank_info }
                        }
                    }
                }

            }
        }
        onItemClick { item, position ->
            startFragment(RankCategoryFragment.newInstance(item))
        }
    }
}