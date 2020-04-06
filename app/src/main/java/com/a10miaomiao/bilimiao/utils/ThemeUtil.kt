package com.a10miaomiao.bilimiao.utils

import android.animation.Animator
import android.app.UiModeManager
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.content.res.Configuration
import android.support.annotation.StyleRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.widget.FrameLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.MainActivity
import org.jetbrains.anko.uiModeManager

class ThemeUtil(
        val activity: AppCompatActivity
) {

    private val KEY_THEME = "theme_name"
    private val KEY_NIGHT = "night"
    val theme = MutableLiveData<String>()

    val light2DarkThemeMap = mapOf<Int, Int>(
            R.style.PinkTheme to R.style.DarkPinkTheme,
            R.style.RedTheme to R.style.DarkPinkTheme,
            R.style.YellowTheme to R.style.DarkPinkTheme,
            R.style.GreenTheme to R.style.DarkPinkTheme,
            R.style.BlueTheme to R.style.DarkPinkTheme,
            R.style.PurpleTheme to R.style.DarkPinkTheme
    )

    fun init() {
        val themeName = SettingUtil.getString(activity, KEY_THEME, "PinkTheme")
        theme.value = themeName
        activity.setTheme(getThemeResId(themeName))
    }

    fun setTheme(newTheme: String) {
        val themeId= getThemeResId(newTheme)
        activity.setTheme(themeId)
        activity.window.navigationBarColor = activity.config.themeColor
        theme.value = newTheme
        SettingUtil.putString(activity, KEY_THEME, newTheme)
    }


    fun setNight(mode: Int) {
        if (mode in 0..2 && mode != getNight()) {
            SettingUtil.putInt(activity, KEY_NIGHT, mode)
            setTheme(theme.value!!)
        }
    }

    fun getNight() = SettingUtil.getInt(activity, KEY_NIGHT, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)


    /**
     * 是否开启暗黑模式
     */
    fun isDarkModeStatus(): Boolean {
        return when(getNight()){
            0 -> {
                val configuration = activity.resources.configuration
                val mode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                mode == Configuration.UI_MODE_NIGHT_YES
            }
            2 -> true
            else -> false
        }
    }

    fun getThemeResId(themeName: String) = try{
        val name = if (isDarkModeStatus()) "Dark$themeName" else themeName
        R.style::class.java.getField(name).getInt(null)
    } catch (e: Exception){
        R.style.PinkTheme
    }


    fun observeTheme(owner: LifecycleOwner, observer: Observer<String>) = theme.observe(owner, observer)

    // 动态主题切换, 重新构建view，直接替换旧view
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

