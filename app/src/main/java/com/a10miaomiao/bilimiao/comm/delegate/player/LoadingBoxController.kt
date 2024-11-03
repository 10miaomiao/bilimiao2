package com.a10miaomiao.bilimiao.comm.delegate.player

import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.network
import splitties.views.imageResource

class LoadingBoxController(
    private val activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
) {

    private val loadingLayout = activity.findViewById<FrameLayout>(R.id.player_loading)
    private val loadingTopLauout = activity.findViewById<LinearLayout>(R.id.loading_top)
    private val loadingBottomLayout = activity.findViewById<LinearLayout>(R.id.loading_bottom);
    private val loadingCloseBtn = activity.findViewById<ImageView>(R.id.loading_close)
    private val loadingMoreBtn = activity.findViewById<ImageView>(R.id.loading_more)
    private val loadingFullscreenBtn = activity.findViewById<ImageView>(R.id.loading_fullscreen)
    private val loadingTitle = activity.findViewById<TextView>(R.id.loading_title)
    private val loadingCover = activity.findViewById<ImageView>(R.id.loading_cover)
    private val loadingAnimTV = activity.findViewById<ImageView>(R.id.loading_anim_tv)
    private val loadingText = activity.findViewById<TextView>(R.id.loading_text)

    init {
        initLoadingBox()
    }

    private fun initLoadingBox() {
        loadingCloseBtn.setOnClickListener {
            delegate.controller.onBackClick()
        }
        loadingMoreBtn.setOnClickListener(delegate.controller::showMoreMenu)
        loadingFullscreenBtn.setOnClickListener(delegate.controller::changeFullscreen)
        loadingFullscreenBtn.setOnLongClickListener {
            delegate.controller.showFullModeMenu(it)
            true
        }
    }

    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
        loadingTopLauout.setPadding(left, top, right, 0);
        loadingBottomLayout.setPadding(left, 0, right, bottom)
    }

    fun showLoading(title: String, cover: String) {
        loadingText.text = ""
        loadingTitle.text = title
        loadingCover.network(cover)
        loadingLayout.visibility = View.VISIBLE
        (loadingAnimTV.drawable as? AnimationDrawable)?.start()
    }

    fun hideLoading() {
        (loadingAnimTV.drawable as? AnimationDrawable)?.stop()
        loadingLayout.visibility = View.GONE
        loadingCover.imageResource = 0
        loadingText.text = ""
    }

    fun print(text: String) {
        loadingText.text = loadingText.text.toString() + text
    }

    fun println(text: String) {
        loadingText.text = loadingText.text.toString() + text + "\n"
    }
}