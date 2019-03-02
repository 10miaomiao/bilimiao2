package com.a10miaomiao.bilimiao.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.widget.GridLayoutManager
import android.util.SizeF
import android.view.Gravity
import android.view.MenuItem
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.Home
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.ui.region.RegionFragment
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.MiaoFragment
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.anko.MiaoUI
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.binding.bind
import com.bumptech.glide.Glide
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.support.v4.toast
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*


class HomeFragment : MiaoFragment() {
    var title by binding.miao("时光机")
    var time by binding.miao("2018-0-0 至 2018-0-0")
    var region = MiaoList<Home.Region>()

    override fun initView() {
        randomTitle()
        updateTime()
        RxBus.getInstance().on(ConstantUtil.TIME_CHANGE) {
            updateTime()
        }
    }

    override fun loadData() {
        // 加载分区列表
        Observable.just(readAssetsJson())
                .map { Gson().fromJson(it, Home.RegionData::class.java) }
                .map { it.data }
                .doOnNext { it.forEachWithIndex(::regionIcon) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    region.addAll(it)
                }, {
                    toast("读取分区列表遇到错误")
                })
    }


    private fun updateTime() {
        val time_from = DateModel(binding)
        val time_to = DateModel(binding)
        time_from.read(context!!, ConstantUtil.TIME_FROM)
        time_to.read(context!!, ConstantUtil.TIME_TO)
        time = time_from.getValue("-") + " 至 " + time_to.getValue("-")
    }

    /**
     * 随机标题
     */
    private fun randomTitle() {
        val titles = arrayOf("时光姬", "时光基", "时光姬", "时光姬")
        val subtitles = arrayOf("ε=ε=ε=┏(゜ロ゜;)┛", "(　o=^•ェ•)o　┏━┓", "(/▽＼)", "ヽ(✿ﾟ▽ﾟ)ノ")
        val random = Random()
        title = titles[random.nextInt(titles.size)] + "  " + subtitles[random.nextInt(titles.size)]
    }

    /**
     * 分区图标
     */
    private fun regionIcon(index: Int, item: Home.Region) {
        item.icon = intArrayOf(
                R.drawable.ic_region_fj, R.drawable.ic_region_fj_domestic, R.drawable.ic_region_dh,
                R.drawable.ic_region_yy, R.drawable.ic_region_wd, R.drawable.ic_region_yx,
                R.drawable.ic_region_kj, R.drawable.ic_region_sh, R.drawable.ic_region_gc,
                R.drawable.ic_region_ss, R.drawable.ic_region_ad, R.drawable.ic_region_yl,
                R.drawable.ic_region_ys, R.drawable.ic_region_dy, R.drawable.ic_region_dsj
        )[index]
//        item.icon = intArrayOf(
//                R.drawable.ic_category_t13, R.drawable.ic_category_t167, R.drawable.ic_category_t1,
//                R.drawable.ic_category_t3, R.drawable.ic_category_t129, R.drawable.ic_category_t4,
//                R.drawable.ic_category_t36, R.drawable.ic_category_t160, R.drawable.ic_category_t119,
//                R.drawable.ic_category_t155, R.drawable.ic_category_t165, R.drawable.ic_category_t5,
//                R.drawable.ic_category_t181, R.drawable.ic_category_t23, R.drawable.ic_category_t11
//        )[index]
    }

    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.search -> {
                startFragment(SearchFragment.newInstance())
            }
        }
        return true
    }

    /**
     * 读取assets下的json数据
     */
    private fun readAssetsJson(): String? {
        val assetManager = activity!!.assets
        try {
            val inputStream = assetManager.open("region.json")
            val br = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var str: String? = br.readLine()
            while (str != null) {
                stringBuilder.append(str)
                str = br.readLine()
            }
            return stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    @SuppressLint("SetTextI18n")
    override fun render() = MiaoUI {
        verticalLayout {
            backgroundColor = config.background
            headerView {
                bind<String>(::title, this::title)
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    RxBus.getInstance().send(ConstantUtil.OPEN_DRAWER)
                }
                inflateMenu(R.menu.search)
                onMenuItemClick(this@HomeFragment::onMenuItemClick)
            }
            nestedScrollView {
                verticalLayout {
                    lparams {
                        width = matchParent
                        bottomMargin = config.dividerSize
                    }
                    // 分区列表
                    recyclerView {
                        isNestedScrollingEnabled = false
                        backgroundColor = Color.WHITE
                    }.lparams {
                        width = matchParent
                        topMargin = config.dividerSize
                    }.miao(region).itemView { miao ->
                        verticalLayout {
                            selectableItemBackground()
                            gravity = Gravity.CENTER
                            verticalPadding = dip(10)
                            imageView {
                                miao.bind { item ->
                                    Glide.with(context)
                                            .load(item.icon)
                                            .override(dip(24), dip(24))
                                            .into(this)
                                }
                            }.lparams(dip(24), dip(24))
                            textView {
                                miao.bind { item -> text = item.name }
                                gravity = Gravity.CENTER
                            }
                        }
                    }.onItemClick { item, position ->
                        startFragment(RegionFragment.newInstance(item))
                    }.layoutManager(GridLayoutManager(activity, 5))

                    // 时间线时间显示
                    linearLayout {
                        selectableItemBackground()
                        backgroundColor = Color.WHITE
                        padding = config.dividerSize
                        textView {
                            bind(::time) { text = "当前时间线：$it" }
                        }
                        setOnClickListener {
                            startFragment(TimeSettingFragment())
                        }
                    }.lparams {
                        width = matchParent
                        topMargin = config.dividerSize
                    }
                }
            }
        }
    }
}
