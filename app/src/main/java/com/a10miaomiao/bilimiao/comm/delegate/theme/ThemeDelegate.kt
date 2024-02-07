package com.a10miaomiao.bilimiao.comm.delegate.theme

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.google.android.material.color.DynamicColors
import com.kongzue.dialogx.DialogX
import org.kodein.di.DI
import org.kodein.di.DIAware


class ThemeDelegate(
    private var activity: AppCompatActivity,
    override val di: DI,
) : DIAware {

    companion object {
        private const val KEY_THEME = "theme_name"
        private const val KEY_NIGHT = "night"
        private const val KEY_APP_BAR_TYPE = "app_bar_type"
        private const val NAME_MATERIAL_YOU_THEME = "MaterialYouTheme"

        private const val APP_BAR_TYPE_COLORFUL = 0 // 主题彩色
        private const val APP_BAR_TYPE_SOLID = 1 // 纯色

        fun getNightMode (context: Context): Int {
            val sp = context.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
            return sp.getInt(KEY_NIGHT, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        fun getThemeName(context: Context): String {
            val sp = context.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
            return sp.getString(KEY_THEME, "PinkTheme")!!
        }

        fun setNightMode (context: Context, mode: Int = getNightMode(context)) {
            if (mode in 0..2) {
                context.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(KEY_NIGHT, mode)
                    .apply()
                if (mode == 0) {
                    DialogX.globalTheme = DialogX.THEME.AUTO
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else if (mode == 1) {
                    DialogX.globalTheme = DialogX.THEME.LIGHT
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else if (mode == 2) {
                    DialogX.globalTheme = DialogX.THEME.DARK
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
    }

    private val theme = MutableLiveData<String>()

    private val handler = Handler(Looper.getMainLooper())

    val themeList = arrayListOf(
        ThemeInfo("哔哩粉", activity.resources.getColor(R.color.pink), "PinkTheme"),
        ThemeInfo("姨妈红", activity.resources.getColor(R.color.red), "RedTheme"),
        ThemeInfo("咸蛋黄", activity.resources.getColor(R.color.yellow), "YellowTheme"),
        ThemeInfo("早苗绿", activity.resources.getColor(R.color.green), "GreenTheme"),
        ThemeInfo("胖次蓝", activity.resources.getColor(R.color.blue), "BlueTheme"),
        ThemeInfo("基佬紫", activity.resources.getColor(R.color.purple), "PurpleTheme")
    )

    fun onCreate(savedInstanceState: Bundle?) {
        setTheme()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            themeList.add(
                ThemeInfo(
                    "Material You",
                    DynamicColors.wrapContextIfAvailable(activity).config.themeColor,
                    NAME_MATERIAL_YOU_THEME,
                )
            )
        }
    }

    fun getThemeResId(themeName: String = getTheme()) = try {
        R.style::class.java.getField(themeName).getInt(null)
    } catch (e: Exception) {
        R.style.PinkTheme
    }

    fun getTheme(): String {
        return theme.value!!
    }

    fun setTheme() {
        val themeName = getThemeName(activity)
        activity.setTheme(getThemeResId(themeName))
        if (themeName == NAME_MATERIAL_YOU_THEME) {
            DynamicColors.applyIfAvailable(activity)
        }
        theme.value = themeName
    }

    fun setTheme(newTheme: String) {
        activity.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, newTheme)
            .apply()
        setTheme()
    }

    fun getAppBarType(): Int {
        return activity.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_APP_BAR_TYPE, 0)
    }

    fun setAppBarType(type: Int) {
        activity.getSharedPreferences(Bilimiao.APP_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_APP_BAR_TYPE, type)
            .apply()
        setTheme()
    }

    fun getAppBarBgColor(): Int {
        val bgColor = if (getAppBarType() == 0) {
            activity.config.colorSurfaceVariant
        } else {
            activity.config.blockBackgroundColor
        }
        return (bgColor and 0x00FFFFFF) or (0xF8000000).toInt()
    }

    fun observeTheme(owner: LifecycleOwner, observer: Observer<String>) = theme.observe(owner, observer)

    class ThemeInfo(
        var name: String,
        var color: Int,
        var theme: String
    )
}