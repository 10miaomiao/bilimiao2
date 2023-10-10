package com.a10miaomiao.bilimiao

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.loader.glide.GlideImageLoader
import net.mikaelzero.mojito.view.sketch.SketchImageLoadFactory


class Bilimiao: Application() {

    companion object {
        const val APP_NAME = "bilimiao"
        lateinit var app: Bilimiao
        lateinit var commApp: BilimiaoCommApp
    }

    private lateinit var mNoncompatDensityAndScaledDensity: Pair<Float, Float>
    val noncompatDensity get() = mNoncompatDensityAndScaledDensity.first
    val noncompatScaledDensity get() = mNoncompatDensityAndScaledDensity.second
    val noncompatDpi get() = (160 * noncompatDensity).toInt()

    init {
        app = this
        commApp = BilimiaoCommApp(this)
    }

    override fun onCreate() {
        super.onCreate()
        ThemeDelegate.setNightMode(this)
        Mojito.initialize(
            GlideImageLoader.with(this),
            SketchImageLoadFactory()
        )
        commApp.onCreate()

        mNoncompatDensityAndScaledDensity = Pair(
            resources.displayMetrics.density,
            resources.displayMetrics.scaledDensity,
        )
        registerComponentCallbacks(object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                if (newConfig.fontScale > 0) {
                    mNoncompatDensityAndScaledDensity = mNoncompatDensityAndScaledDensity.copy(
                        second = resources.displayMetrics.scaledDensity
                    )
                }
            }
            override fun onLowMemory() {}
        })
    }


    fun setCustomDensityDpi(activity: Activity, dpi: Int) {
        val appDisplayMetrics = resources.displayMetrics
//        val targetDensity = (appDisplayMetrics.widthPixels / 360).toFloat()
        val targetDensity = dpi.toFloat() / 160
        val targetScaledDensity = targetDensity * (noncompatScaledDensity / noncompatDensity)
        val targetDensityDpi = (160 * targetDensity).toInt()
        appDisplayMetrics.density = targetDensity
        appDisplayMetrics.scaledDensity = targetScaledDensity
        appDisplayMetrics.densityDpi = targetDensityDpi
        val activityDisplayMetrics: DisplayMetrics = activity.getResources().getDisplayMetrics()
        activityDisplayMetrics.density = targetDensity
        activityDisplayMetrics.scaledDensity = targetScaledDensity
        activityDisplayMetrics.densityDpi = targetDensityDpi
    }
}