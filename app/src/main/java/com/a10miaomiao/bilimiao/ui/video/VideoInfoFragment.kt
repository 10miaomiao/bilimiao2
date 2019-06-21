package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.entity.VideoInfo
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.cover.CoverActivity
import com.a10miaomiao.bilimiao.ui.player.PlayerActivity
import com.a10miaomiao.bilimiao.ui.player.PlayerFragment
import com.a10miaomiao.bilimiao.utils.*
import kotlinx.android.synthetic.main.fragment_video_info.*
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.support.v4.dip
import kotlin.properties.Delegates


class VideoInfoFragment : SwipeBackFragment() {
    companion object {
        lateinit var instance: VideoInfoFragment
        fun newInstance(id: String): VideoInfoFragment {
            val fragment = VideoInfoFragment()
            val bundle = Bundle()
            bundle.putString(ConstantUtil.ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var playerFragment: PlayerFragment? = null
    lateinit var pagerAdapter: FragmentStatePagerAdapter
    private val fragments = arrayListOf<Fragment>()

    private lateinit var id: String
    private lateinit var viewModel: VideoInfoViewModel
    var isMiniPlayer = MutableLiveData<Boolean>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        id = arguments!!.getString(ConstantUtil.ID)
        isMiniPlayer.value = true
        return attachToSwipeBack(inflater.inflate(R.layout.fragment_video_info, container, false))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        instance = this
        initToolbar()
        initView()
        viewModel = ViewModelProviders.of(this, newViewModelFactory { VideoInfoViewModel(context!!, id) })
                .get(VideoInfoViewModel::class.java)
        viewModel.info.observe(this, updateView)
        viewModel.state.observe(this, Observer {
            if (it == null) {
                stateTv.visibility = View.GONE
            } else {
                stateTv.text = it
                stateTv.visibility = View.VISIBLE
            }
        })
        isMiniPlayer.observe(this, Observer {
            if (playerFragment == null) return@Observer
            val view = playerContainerLayout
            if (it!!) {
                fullScreenLayout.removeAllViews()
                minScreenLayout.addView(view)
                fullScreenLayout.visibility = View.GONE
                coordinatorLayout.visibility = View.VISIBLE
                MainActivity.of(context!!).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                setSwipeBackEnable(true)
            } else {
                minScreenLayout.removeAllViews()
                fullScreenLayout.addView(view)
                coordinatorLayout.visibility = View.GONE
                fullScreenLayout.visibility = View.VISIBLE
                MainActivity.of(context!!).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                setSwipeBackEnable(false)
            }
        })
    }

    private fun initView() {
        fragments.clear()
        fragments += VideoInfoDetailsFragment.newInstance(id)
        fragments +=VideoCommentFragment.newInstance(id)
        val titles = arrayListOf("简介", "评论")
        pagerAdapter = object : FragmentStatePagerAdapter(childFragmentManager) {
            override fun getItem(p0: Int) = fragments[p0]
            override fun getCount() = fragments.size
            override fun getPageTitle(position: Int) = titles[position]
            override fun getItemPosition(o: Any) = PagerAdapter.POSITION_NONE
        }
        mViewPager.adapter = pagerAdapter
        mTabLayout.setTabsFromPagerAdapter(pagerAdapter)
        mTabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        mTabLayout.setupWithViewPager(mViewPager)
        playButton.setOnClickListener(onPlay)
        mTvPlay.setOnClickListener(onPlay)
    }

    private fun initToolbar() {
        avTv.text = "av$id"
        val statusBarHeight = getStatusBarHeight()
        toolbar.layoutParams.height += statusBarHeight
        toolbar.setPadding(0, statusBarHeight, 0, 0)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            pop()
        }
        toolbar.inflateMenu(R.menu.video_info)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.open -> {
                    IntentHandlerUtil.openWithPlayer(activity!!, IntentHandlerUtil.TYPE_VIDEO, id)
                }
                R.id.watch -> {
                    CoverActivity.launch(activity!!, id, "AV")
                }
            }
            true
        }
        mAppBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (Math.abs(verticalOffset) - appBarLayout.totalScrollRange > dip(-40)) {
                // 折叠
                playButton.visibility = View.VISIBLE
                avTv.visibility = View.GONE
            } else {
                playButton.visibility = View.GONE
                avTv.visibility = View.VISIBLE
            }
        })
    }

    private val onPlay = View.OnClickListener {
        viewModel.info.value
                ?.let { info -> palyVideo(info.cid, info.title, 0) }
    }

    fun palyVideo(cid: String, title: String, index: Int) {
        viewModel.pageIndex.value = index
        palyVideo(cid, title)
    }

    fun palyVideo(cid: String, title: String) {
        val fragment = PlayerFragment.newVideoPlayerInstance(id, cid, title)
        childFragmentManager.beginTransaction()
                .replace(R.id.playerContainerLayout, fragment)
                .commit()
        playerFragment = fragment
        (mToolbarLayout.layoutParams as AppBarLayout.LayoutParams)
                .scrollFlags = 0
        mTvPlay.visibility = View.GONE
        toolbar.visibility = View.GONE
        minScreenLayout.setPadding(0, getStatusBarHeight(), 0, 0)
        minScreenLayout.setBackgroundColor(Color.BLACK)
    }


    private val updateView = Observer<VideoInfoViewModel.PageInfo> { info ->
        imageview.network(info?.pic ?: "")
    }

    private val changePlayerSize = Observer<Boolean> {

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

        } else {

        }
    }

    override fun onNewBundle(args: Bundle) {
        super.onNewBundle(args)
        val newId = args.getString(ConstantUtil.ID)
        if (newId == id) return
        id = args.getString(ConstantUtil.ID)
        viewModel.id = args.getString(ConstantUtil.ID)
        avTv.text = "av$id"
        playerFragment?.let {
            childFragmentManager.beginTransaction()
                    .remove(it)
                    .commit()
            playerFragment = null
        }
        mAppBarLayout.setExpanded(true)
        (mToolbarLayout.layoutParams as AppBarLayout.LayoutParams)
                .scrollFlags = (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
        mTvPlay.visibility = View.VISIBLE
        toolbar.visibility = View.VISIBLE
        minScreenLayout.setPadding(0, 0, 0, 0)
        minScreenLayout.setBackgroundColor(R.drawable.bili_default_image_tv)
        viewModel.clear()
        viewModel.loadData()
        fragments.clear()
        pagerAdapter.notifyDataSetChanged()
        fragments += VideoInfoDetailsFragment.newInstance(id)
        fragments += VideoCommentFragment.newInstance(id)
        pagerAdapter.notifyDataSetChanged()
    }

    override fun onBackPressedSupport(): Boolean {
        return if (isMiniPlayer.value!!) {
            super.onBackPressedSupport()
        } else {
            isMiniPlayer.value = true
            true
        }
    }

}


