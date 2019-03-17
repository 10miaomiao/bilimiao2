package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.entity.VideoInfo
import com.a10miaomiao.bilimiao.ui.cover.CoverActivity
import com.a10miaomiao.bilimiao.ui.player.PlayerActivity
import com.a10miaomiao.bilimiao.utils.*
import kotlinx.android.synthetic.main.fragment_video_info.*
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.support.v4.dip


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

    lateinit var videoInfoDetailsFragment: VideoInfoDetailsFragment
    lateinit var videoCommentFragment: VideoCommentFragment

    val id by lazy { arguments!!.getString(ConstantUtil.ID) }

    private lateinit var viewModel: VideoInfoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(inflater.inflate(R.layout.fragment_video_info, container, false))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        instance = this
        initToolbar()
        videoInfoDetailsFragment = VideoInfoDetailsFragment.newInstance(id)
        videoCommentFragment = VideoCommentFragment.newInstance(id)
        initView()
        viewModel = ViewModelProviders.of(this, newViewModelFactory { VideoInfoViewModel(id) })
                .get(VideoInfoViewModel::class.java)
        viewModel.info.observe(this, Observer {
            it?.let(::updateView)
        })
        viewModel.state.observe(this, Observer {
            if (it == null) {
                stateTv.visibility = View.GONE
            } else {
                stateTv.text = it
                stateTv.visibility = View.VISIBLE
            }
        })
    }

    private fun initView() {
        val fragments = arrayListOf(
                videoInfoDetailsFragment,
                videoCommentFragment
        )
        val titles = arrayListOf("简介", "评论")
        val mAdapter = object : FragmentStatePagerAdapter(childFragmentManager) {
            override fun getItem(p0: Int) = fragments[p0]
            override fun getCount() = fragments.size
            override fun getPageTitle(position: Int) = titles[position]
        }
        mViewPager.adapter = mAdapter
        mTabLayout.setTabsFromPagerAdapter(mAdapter)
        mTabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        mTabLayout.setupWithViewPager(mViewPager)

        val onPlay = View.OnClickListener {
            viewModel.info.value?.let { info ->
                PlayerActivity.play(activity!!, id, info.cid.toString(), info.title)
            }
//            viewModel.playVideo()
        }
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

    private fun updateView(info: VideoInfo) {
        imageview.network(info.pic)
    }


}


