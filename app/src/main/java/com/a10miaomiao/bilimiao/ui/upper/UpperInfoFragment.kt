package com.a10miaomiao.bilimiao.ui.upper


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.Owner
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
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
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class UpperInfoFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(owner: Owner): UpperInfoFragment {
            val fragment = UpperInfoFragment()
            val bundle = Bundle()
            bundle.putParcelable("owner", owner)
            fragment.arguments = bundle
            return fragment
        }
    }

    val owner by lazy { arguments!!.getParcelable<Owner>("owner") }
    lateinit var viewModel: UpperInfoViewModel
    lateinit var filterStore: FilterStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        filterStore = MainActivity.of(context!!).filterStore
        viewModel = ViewModelProviders.of(this, newViewModelFactory { UpperInfoViewModel(owner) })
                .get(UpperInfoViewModel::class.java)
        return attachToSwipeBack(createUI().view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.noLike.value = !filterStore.filterUpper(owner.mid)
    }

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener {
        if (viewModel.noLike.value == true) {
            filterStore.deleteUpper(owner.mid)
            viewModel.noLike.value = false
        } else {
            filterStore.addUpper(owner.mid, owner.name)
            viewModel.noLike.value = true
        }
        true
    }

    private fun createUI() = UI {
        coordinatorLayout {
            appBarLayout {
                collapsingToolbarLayout {
                    setContentScrimResource(attr(R.attr.colorPrimary))

                    createUpperInfo().lparams {
                        collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
                        width = matchParent
                    }

                    val statusBarHeight = getStatusBarHeight()
                    createToolbar(this@appBarLayout, statusBarHeight).lparams {
                        width = matchParent
                        height = dimen(attr(R.attr.actionBarSize)) + statusBarHeight
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
                    isRefreshing = it!! != 2
                })
                setOnRefreshListener { viewModel.refreshList() }
                recyclerView {
                    layoutManager = GridLayoutManager(activity, 2)
                    createAdapter()
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
    private fun ViewManager.createToolbar(appBarLayout: AppBarLayout, statusBarHeight: Int) = include<Toolbar>(R.layout.layout_toolbar) {
        topPadding = statusBarHeight
        contentInsetStartWithNavigation = 0
        setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        setNavigationOnClickListener {
            pop()
        }

        textView {
            text = viewModel.owner.name
            textSize = 16f
            textColor = Color.WHITE

            appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                visibility = if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange > dip(-60)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            })
        }


        menu.add("屏蔽该up主").apply {
            viewModel.noLike.observe(this@UpperInfoFragment, Observer {
                title = if (it == true) {
                    "取消屏蔽该up主"
                } else {
                    "屏蔽该up主"
                }
            })
        }
        setOnMenuItemClickListener(onMenuItemClickListener)
    }


    /**
     * Up主头部信息
     */
    private fun ViewManager.createUpperInfo() = frameLayout {
        backgroundColor = config.blockBackgroundColor

        imageView {
            id = 0
            scaleType = ImageView.ScaleType.CENTER_CROP
            imageResource = R.drawable.top_bg1
            Glide.with(context)
                    .load(R.drawable.top_bg1)
                    .centerCrop()
                    .dontAnimate()
                    .into(this)
        }.lparams {
            width = matchParent
            height = dimen(R.dimen.app_bar_height)
        }

        verticalLayout {
            gravity = Gravity.CENTER

            rcImageView {
                isCircle = true
                network(viewModel.owner.face)
            }.lparams(dip(64), dip(64)) {
                bottomMargin = dip(5)
            }

            textView(viewModel.owner.name) {
                textSize = 18f
                textColor = Color.WHITE
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            }
        }.lparams {
            width = matchParent
            height = matchParent
        }
    }

    /**
     * 分区列表适配器
     */
    private fun RecyclerView.createAdapter() = miao(viewModel.list) {
        itemView { binding ->
            frameLayout {

                lparams(matchParent, dip(120)) {
                    verticalMargin = dip(2.5f)
                }
                padding = dip(5)

                selectableItemBackground()

                rcImageView {
                    radius = dip(5)
                    binding.bind { item ->
                        scaleType = ImageView.ScaleType.CENTER_CROP

                        Glide.with(context)
                                .loadPic(item.cover)
                                .bitmapTransform(BlurTransformation(context, 14, 6)) // 高斯模糊
                                .into(this)
                    }
                }

                verticalLayout {
                    lparams(matchParent, matchParent)
                    gravity = Gravity.CENTER

                    textView {
                        paint.isFakeBoldText = true
                        gravity = Gravity.CENTER
                        textSize = 16f
                        textColor = Color.WHITE
                        binding.bind { item -> text = item.name }
                    }
                    textView {
                        textColor = Color.WHITE
                        gravity = Gravity.CENTER
                        textSize = 14f
                        binding.bind { item -> text = "共${item.count}个投稿" }
                    }
                }
            }
        }
        onItemClick { item, position ->
            if (item.isAll)
                startFragment(UpperVideoListFragment.newInstance(owner))
            else
                startFragment(UpperChannelVideoListFragment.newInstance(item))
        }
    }


}