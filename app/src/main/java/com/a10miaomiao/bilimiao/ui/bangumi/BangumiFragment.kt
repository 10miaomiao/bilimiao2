package com.a10miaomiao.bilimiao.ui.bangumi

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.SeasonEpisode
import com.a10miaomiao.bilimiao.entity.bangumi.Bangumi
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.player.PlayerActivity
import com.a10miaomiao.bilimiao.ui.region.RegionDetailsFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import com.bumptech.glide.Glide
import jp.wasabeef.glide.transformations.BlurTransformation
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.collapsingToolbarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class BangumiFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(sid: String): BangumiFragment {
            val fragment = BangumiFragment()
            val bundle = Bundle()
            bundle.putString("sid", sid);
            fragment.arguments = bundle
            return fragment
        }
    }

    val sid by lazy { arguments!!.getString("sid") }
    private val playerStore by lazy { Store.from(context!!).playerStore }
    lateinit var viewModel: BangumiViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory { BangumiViewModel(context!!, sid) })
                .get(BangumiViewModel::class.java)
        return attachToSwipeBack(createUI().view)
    }

    fun observeNotNull(observer: (value: Bangumi) -> Unit) {
        viewModel.info.observe(this, Observer { it?.let(observer) })
    }


    private fun createUI() = UI {
        coordinatorLayout {
            backgroundColor = config.windowBackgroundColor
            appBarLayout {
                collapsingToolbarLayout {
                    setContentScrimResource(attr(R.attr.colorPrimary))

                    val statusBarHeight = getStatusBarHeight()
                    val actionBarSize = dimen(attr(R.attr.actionBarSize))
                    frameLayout {
                        imageView {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            observeNotNull {
                                Glide.with(context)
                                        .loadPic(it.cover)
                                        .bitmapTransform(BlurTransformation(context, 14, 6)) // 高斯模糊
                                        .into(this@imageView)
                            }
                        }.lparams(matchParent, matchParent)
                        createHeader().lparams {
                            width = matchParent
                            height = matchParent
                            topMargin = actionBarSize + statusBarHeight
                        }
                    }.lparams {
                        collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
                        height = dip(240)
                        width = matchParent
                    }


                    createToolbar(this@appBarLayout, statusBarHeight).lparams {
                        width = matchParent
                        height = actionBarSize + statusBarHeight
                        collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
                    }

                }.lparams {
                    width = matchParent
                    height = wrapContent
//                    height = dimen(R.dimen.app_bar_height)
                    scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                }
            }

            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                viewModel.loading.observe(owner, Observer {
                    isRefreshing = it!!
                })
                setOnRefreshListener { viewModel.loadData() }

                nestedScrollView {
                    createBody()
                }

            }.lparams {
                height = matchParent
                width = matchParent
                behavior = AppBarLayout.ScrollingViewBehavior()
            }

        }
    }


    /**
     * 工具栏
     */
    private fun ViewManager.createToolbar(appBarLayout: AppBarLayout, statusBarHeight: Int) = toolbar {
        topPadding = statusBarHeight
        contentInsetStartWithNavigation = 0
        setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        setNavigationOnClickListener {
            pop()
        }

        textView {
            textSize = 16f
            textColor = Color.WHITE
            text = "ss$sid"
        }
    }


    private fun ViewManager.createHeader() = linearLayout {
        bottomPadding = dip(10)
        horizontalPadding = dip(10)

        imageView {
            observeNotNull {
                Glide.with(context)
                        .loadPic(it.cover)
                        .into(this@imageView)
            }
        }.lparams {
            width = wrapContent
            height = matchParent
            rightMargin = dip(10)
        }
        verticalLayout {
            textView {
                textSize = 20f
                textColor = Color.WHITE
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END

                observeNotNull {
                    text = it.title
                }
            }.lparams {
                height = matchParent
                weight = 1f
                width = matchParent
            }

            textView {
                textSize = 15f
                textColor = Color.WHITE
                observeNotNull {
                    text = it.newest_ep.desc
                }
            }.lparams { bottomMargin = dip(5) }

            textView {
                textSize = 15f
                textColor = Color.WHITE
                observeNotNull { text = "追番：${NumberUtil.converString(it.stat.favorites)}" }
            }.lparams { bottomMargin = dip(5) }

            textView {
                textSize = 15f
                textColor = Color.WHITE
                observeNotNull {
                    text = "播放：${NumberUtil.converString(it.stat.views)}"
                }
            }
        }.lparams {
            width = matchParent
            height = matchParent
        }
    }

    private fun ViewManager.createBody() = verticalLayout {

        verticalLayout {
            backgroundColor = config.blockBackgroundColor
            padding = dip(10)

            textView("简介：") {
                textSize = 16f
                textColor = config.foregroundColor
            }.lparams { bottomMargin = dip(5) }
            textView {
                observeNotNull {
                    text = it.evaluate
                }
            }
        }.lparams {
            width = matchParent
            topMargin = dip(10)
        }


        verticalLayout {
            backgroundColor = config.blockBackgroundColor
            padding = dip(10)

            linearLayout {
                textView("剧集列表：") {
                    textSize = 16f
                    textColor = config.foregroundColor
                }.lparams {
                    weight = 1f
                }

                textView {
                    text = "查看 >"
                    textSize = 16f
                    textColorResource = config.themeColorResource
                    setOnClickListener {
                        val fragment = EpisodesFragment.newInstance(viewModel.episodes)
                        MainActivity.of(context)
                                .showBottomSheet(fragment)
                    }
                }
            }.lparams {
                width = matchParent
                bottomMargin = dip(5)
            }

            recyclerView {
                val lm = LinearLayoutManager(context)
                lm.orientation = LinearLayoutManager.HORIZONTAL
                layoutManager = lm
                createPagesAdapter()
                isNestedScrollingEnabled = false
            }.lparams {
                width = matchParent
                height = wrapContent
                verticalMargin = dip(5)
            }
        }.lparams {
            width = matchParent
            topMargin = dip(10)
        }

        verticalLayout {
            backgroundColor = config.blockBackgroundColor
            padding = dip(10)

            textView("系列：") {
                textSize = 16f
                textColor = config.foregroundColor
            }.lparams { bottomMargin = dip(5) }

            recyclerView {
                val lm = LinearLayoutManager(context)
                lm.orientation = LinearLayoutManager.HORIZONTAL
                layoutManager = lm
                createSeasonsAdapter()
                isNestedScrollingEnabled = false
                viewModel.seasonsIndex.observe(this@BangumiFragment, Observer {
                    lm.scrollToPositionWithOffset(it!!, 0)
                })
            }.lparams {
                width = matchParent
                height = wrapContent
                verticalMargin = dip(5)
            }
        }.lparams {
            width = matchParent
            topMargin = dip(10)
        }

//        verticalLayout {
//            backgroundColor = Color.WHITE
//            padding = dip(10)
//
//            textView("标签：") {
//                textSize = 16f
//                textColor = Color.parseColor("#222222")
//            }.lparams { bottomMargin = dip(5) }
//
//
//        }.lparams {
//            width = matchParent
//            topMargin = dip(10)
//        }
//
//        verticalLayout {
//            backgroundColor = Color.WHITE
//            padding = dip(10)
//
//            textView("CV：") {
//                textSize = 16f
//                textColor = Color.parseColor("#222222")
//            }.lparams { bottomMargin = dip(5) }
//
//
//        }.lparams {
//            width = matchParent
//            topMargin = dip(10)
//        }
//
//        verticalLayout {
//            backgroundColor = Color.WHITE
//            padding = dip(10)
//
//            textView("STAFF：") {
//                textSize = 16f
//                textColor = Color.parseColor("#222222")
//            }.lparams { bottomMargin = dip(5) }
//
//
//        }.lparams {
//            width = matchParent
//            topMargin = dip(10)
//        }


    }


    /**
     * 分P列表
     */
    private fun RecyclerView.createPagesAdapter() = miao(viewModel.episodes) {
        val observePlayer = playerStore.observe()
        observePlayer {
            notifyDataSetChanged()
        }
        itemView { b ->
            verticalLayout {
                backgroundResource = R.drawable.shape_corner
                lparams(wrapContent, matchParent) {
                    rightMargin = dip(5)
                    minimumWidth = dip(60)
                }
                horizontalPadding = dip(10)
                verticalPadding = dip(10)
                gravity = Gravity.LEFT

                val indexTextView = textView {
                    textColorResource = R.color.text_black
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }.lparams {
                    bottomMargin = dip(5)
                }

                val titleTextView = textView {
                    textColorResource = R.color.text_black
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    maxWidth = dip(120)
                    minWidth = dip(60)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.LEFT
                    textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                }.lparams()

                b.bind { item ->
                    indexTextView.text = "第${item.index}集"
                    titleTextView.text = item.index_title
                    if (item.cid == playerStore.info.cid){
                        isEnabled = false
                        indexTextView.textColorResource = config.themeColorResource
                        titleTextView.textColorResource = config.themeColorResource
                    }else{
                        isEnabled = true
                        indexTextView.textColorResource = R.color.text_black
                        titleTextView.textColorResource = R.color.text_black
                    }
                }
            }
        }
        onItemClick { item, position ->
            //            PlayerActivity.playBangumi(context, item.section_id, item.ep_id, item.cid.toString(), item.index_title)
            MainActivity.of(context!!).videoPlayerDelegate.playBangumi(
                    item.section_id,
                    item.ep_id,
                    item.cid.toString(),
                    item.index_title
            )
        }
    }

    /**
     * 同系列
     */
    private fun RecyclerView.createSeasonsAdapter() = miao(viewModel.seasons) {
        itemView { b ->
            frameLayout {
                backgroundResource = R.drawable.shape_corner
                lparams(wrapContent, matchParent) {
                    rightMargin = dip(5)
                    minimumWidth = dip(60)
                }

                textView {
                    textColorResource = R.color.text_black
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    horizontalPadding = dip(10)
                    verticalPadding = dip(10)
                    maxWidth = dip(150)
                    minWidth = dip(80)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.LEFT
                    textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                    b.bindIndexed { item, index ->
                        if (item.season_id == viewModel.sid) {
                            this@frameLayout.isEnabled = false
                            textColorResource = config.themeColorResource
                        } else {
                            this@frameLayout.isEnabled = true
                            textColorResource = R.color.text_black
                        }
                        text = item.title
                    }
                }
            }
        }
        onItemClick { item, position ->
            startFragment(BangumiFragment.newInstance(item.season_id))
        }
    }

}