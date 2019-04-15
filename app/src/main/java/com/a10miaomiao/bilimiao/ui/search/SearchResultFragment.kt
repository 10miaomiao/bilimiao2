package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.commponents.dropMenuView
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.commponents.rcLayout
import com.a10miaomiao.bilimiao.ui.region.RegionDetailsViewModel
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.binding.bind
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.linearLayoutCompat
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class SearchResultFragment : Fragment() {

    lateinit var _text: TextView
    lateinit var viewModel: SearchResultViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory { SearchResultViewModel(this) }).get(SearchResultViewModel::class.java)
        return render().view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun render() = UI {
        drawerLayout {
            // 主要
            verticalLayout {
                // 顶部横条
                linearLayout {
                    backgroundColor = Color.WHITE
                    padding = dip(5)
                    gravity = Gravity.CENTER_VERTICAL
                    textView {
                        textSize = 14f
                        setOnClickListener {
                            this@drawerLayout.openDrawer(Gravity.START)
                        }
                        viewModel.filterName.observe(this@SearchResultFragment, Observer {
                            text = it
                        })

                    }.lparams(width = matchParent, weight = 1f)
                }.lparams(width = matchParent) { bottomMargin = dip(5) }

                swipeRefreshLayout {
                    setColorSchemeResources(R.color.colorPrimary)
                    viewModel.loading.observe(owner, Observer {
                        isRefreshing = it!!
                    })
                    setOnRefreshListener { viewModel.refreshList() }
                    recyclerView {
                        backgroundColor = Color.WHITE
                        createAdapter()
                    }
                }
            }

            // 测边
            navigationView {
                backgroundColor = Color.WHITE
                verticalLayout {
                    renderLeft("排序：", viewModel.rankOrdersNameList, viewModel.rankOrdersIndex)
                    renderLeft("时长：", viewModel.durationNameList, viewModel.durationIndex)
                    renderLeft("分区：", viewModel.regionNameList, viewModel.regionIndex)
                }
            }.lparams {
                height = matchParent
                width = wrapContent
                gravity = Gravity.START
            }

            addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerStateChanged(p0: Int) {

                }

                override fun onDrawerSlide(p0: View, p1: Float) {

                }

                override fun onDrawerClosed(p0: View) {
                    viewModel.updateFilter()
                }

                override fun onDrawerOpened(p0: View) {

                }
            })
        }
    }

    // 测边筛选栏
    private fun ViewManager.renderLeft(name: String, list: ArrayList<String>, i: MutableLiveData<Int>) {
        verticalLayout {
            textView(name) {
                textSize = 16f
            }.lparams {
                margin = dip(5)
                topMargin = dip(10)
            }
            recyclerView {
                layoutManager = GridLayoutManager(context!!, 4)
                miao(list) {
                    itemView { binding ->
                        textView {
                            selectableItemBackgroundBorderless()
                            verticalPadding = dip(5)
                            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                            binding.bindIndexed { item, index ->
                                text = item
                                textColorResource = if (index == i.value) {
                                    R.color.colorAccent
                                } else {
                                    R.color.text_black
                                }
                            }
                        }
                    }
                    onItemClick { item, position ->
                        i.value = position
                    }
                    i.observe(this@SearchResultFragment, Observer {
                        notifyDataSetChanged()
                    })
                }
            }
        }
    }

    // 列表适配器
    private fun RecyclerView.createAdapter() = miao(viewModel.list) {
        layoutManager(LinearLayoutManager(context))
        itemView { binding ->
            linearLayout {
                lparams(matchParent, wrapContent)
                selectableItemBackground()
                padding = dip(5)


                rcImageView {
                    radius = dip(5)
                    binding.bind { item -> network(item.cover) }
                }.lparams(width = dip(140), height = dip(85)) {
                    rightMargin = dip(5)
                }

                verticalLayout {
                    textView {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        textColorResource = R.color.colorForeground
                        binding.bind { item -> text = item.title }
                    }.lparams(matchParent, matchParent) {
                        weight = 1f
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        imageView {
                            imageResource = R.drawable.icon_up
                        }.lparams {
                            width = dip(16)
                            rightMargin = dip(3)
                        }
                        textView {
                            textSize = 12f
                            textColorResource = R.color.black_alpha_45
                            binding.bind { item -> text = item.author }
                        }
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        imageView {
                            imageResource = R.drawable.ic_play_circle_outline_black_24dp
                        }.lparams {
                            width = dip(16)
                            rightMargin = dip(3)
                        }
                        textView {
                            textSize = 12f
                            textColorResource = R.color.black_alpha_45
                            binding.bind { item -> text = NumberUtil.converString(item.play) }
                        }
                        space().lparams(width = dip(10))
                        imageView {
                            imageResource = R.drawable.ic_subtitles_black_24dp
                        }.lparams {
                            width = dip(16)
                            rightMargin = dip(3)
                        }
                        textView {
                            textSize = 12f
                            textColorResource = R.color.black_alpha_45
                            binding.bind { item -> text = NumberUtil.converString(item.danmaku) }
                        }
                    }

                    linearLayout {

                    }
                }.lparams(width = matchParent, height = matchParent)
            }
        }
        onItemClick { item, position ->
            startFragment(VideoInfoFragment.newInstance(item.param))
//                    IntentHandlerUtil.openWithPlayer(activity!!, IntentHandlerUtil.TYPE_VIDEO, item.id)
        }
        addFootView {
            loadMoreView {
                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                viewModel.loadState.observe(this@SearchResultFragment, Observer {
                    state = it!!
                })
            }
        }
        onLoadMore {
            viewModel.pageNum++
            viewModel.loadData()
        }
    }
}