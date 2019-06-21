package com.a10miaomiao.bilimiao.utils

import android.animation.Animator
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.MainActivity

class ThemeUtil(
        val activity: AppCompatActivity
) {

    private val KEY_THEME = "theme"
    val theme = MutableLiveData<Int>()

    fun init() {
        theme.value = SettingUtil.getInt(activity, KEY_THEME, R.style.PinkTheme)
        activity.setTheme(theme.value!!)
    }

    fun setTheme(newTheme: Int) {
        activity.setTheme(newTheme)
        activity.window.navigationBarColor = activity.config.themeColor
        theme.value = newTheme
        SettingUtil.putInt(activity, KEY_THEME, newTheme)
    }

    fun observeTheme(owner: LifecycleOwner, observer: Observer<Int>)
            = theme.observe(owner, observer)

    // 动态主题切换, 重新构建view，直接替换view
    fun dynamicTheme(owner: LifecycleOwner, builder: () -> View, animate: Boolean = false): View {
        val layout = FrameLayout(activity)
        layout.addView(builder.invoke())
        theme.observe(owner, Observer {
            if (animate) {
                val newView = builder.invoke()
                newView.alpha = 0f
                layout.addView(newView)
                animate(newView) {
                    if (layout.childCount > 1)
                        layout.removeViewAt(0)
                }
            } else {
                layout.removeAllViews()
                layout.addView(builder.invoke())
            }
        })
        return layout
    }

    private fun animate(view: View, onAnimationEnd: () -> Unit) {
        val animatorListener = object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                onAnimationEnd.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {
                onAnimationEnd.invoke()
            }

            override fun onAnimationStart(animation: Animator?) {

            }
        }
        view.animate().alpha(1f)
                .setDuration(300)
                .setListener(animatorListener)
                .start()
    }

}

