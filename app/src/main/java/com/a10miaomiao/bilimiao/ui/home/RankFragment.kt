package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.BiliMiaoRank
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.rank.RankCategoryDetailsFragment
import com.a10miaomiao.bilimiao.ui.rank.RankCategoryFragment
import com.a10miaomiao.bilimiao.ui.region.RegionDetailsViewModel
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.ui.web.WebFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import jp.wasabeef.glide.transformations.BlurTransformation
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.support.v4.swipeRefreshLayout


class RankFragment : Fragment() {

    lateinit var viewModel: RankViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this).get(RankViewModel::class.java)
        return MainActivity.of(context!!).dynamicTheme(this) { render().view }
    }

    private fun handleItemClick(item: BiliMiaoRank, position: Int) {
        if (item.type == ConstantUtil.BANGUMI || item.type == ConstantUtil.VIDEO) {
            startFragment(RankCategoryFragment.newInstance(item))
        } else if(item.type == ConstantUtil.WEB){
            startFragment(WebFragment.newInstance(item.url))
        } else {
            alert {
                title = "请更新版本后查看"
                okButton { }
                show()
            }
        }
    }

    private val onMenuItemClick = Toolbar.OnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
            R.id.search -> {
                startFragment(SearchFragment.newInstance())
            }
        }
        true
    }

    private fun render() = UI {
        verticalLayout {
            backgroundColor = config.windowBackgroundColor
            headerView {
                title("排行榜")
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    MainActivity.of(context!!).openDrawer()
                }
                inflateMenu(R.menu.search)
                onMenuItemClick(onMenuItemClick)
            }

            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                (+viewModel.loading){ isRefreshing = it }
                setOnRefreshListener { viewModel.refreshList() }
                recyclerView {
                    createAdapter()
                }
            }
        }
    }

    private fun RecyclerView.createAdapter() = miao(viewModel.list) {
        layoutManager(LinearLayoutManager(context))
        itemView { binding ->
            frameLayout {
                lparams(matchParent, dip(120)) {
                    verticalMargin = dip(2.5f)
                }
                padding = dip(5)

                rcImageView {
                    radius = dip(5)
                    binding.bind { item ->
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        Glide.with(context)
                                .load(item.rank_pic)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .bitmapTransform(BlurTransformation(context, 14, 6)) // 高斯模糊
                                .into(this)
                    }
                }.lparams(matchParent, matchParent)

                verticalLayout {
                    lparams(matchParent, matchParent)
                    selectableItemBackground()
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
        onItemClick(::handleItemClick)
    }
}