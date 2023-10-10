package com.a10miaomiao.bilimiao.page.setting

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.toast.toast
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.editText
import splitties.views.dsl.core.horizontalMargin
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.setContentView
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.core.wrapInScrollView
import splitties.views.textColorResource

class DensitySettingActivity : AppCompatActivity() {

    private var initialAppDpi = 0
    private lateinit var ui: DensitySettingUi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = DensitySettingUi(this)
        setContentView(ui)
        initialAppDpi = getAppApi()
    }

    fun getAppApi(): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getInt("app_dpi", 0)
    }

    fun setCustomDensityDpi(dpi: Int) {
        Bilimiao.app.setCustomDensityDpi(this, dpi)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit()
            .putInt("app_dpi", dpi)
            .apply()
        ui = DensitySettingUi(this)
        setContentView(ui)
    }

    override fun onBackPressed() {
        if (initialAppDpi == getAppApi()) {
            super.onBackPressed()
        } else {
            // 直接重启APP
            val intent = packageManager.getLaunchIntentForPackage(packageName)!!
            val componentName = intent.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }
    }

    @OptIn(InternalSplittiesApi::class)
    class DensitySettingUi(
        val activity: DensitySettingActivity
    ) : Ui {

        override val ctx = activity

        val toolBar = view<MaterialToolbar>(View.generateViewId()) {
            setTitle(R.string.density_setting)
            setNavigationIcon(R.drawable.ic_back_24dp)
            setNavigationOnClickListener {
                activity.onBackPressed()
            }
        }

        val dipEditText = editText {
            setRawInputType(InputType.TYPE_CLASS_NUMBER)
            setText(resources.displayMetrics.densityDpi.toString())
        }

        override val root: View = verticalLayout {
            addView(toolBar, lParams(matchParent, wrapContent))

            addView(textView {
                text = "系统默认DPI：${Bilimiao.app.noncompatDpi}"
            }, lParams(matchParent, wrapContent) {
                topMargin = dip(10)
                horizontalMargin = dip(10)
            })

            addView(textView {
                text = "当前应用内DPI修改："
            }, lParams(matchParent, wrapContent) {
                horizontalMargin = dip(10)
            })
            addView(dipEditText, lParams(matchParent, wrapContent) {
                horizontalMargin = dip(10)
            })
            addView(view<MaterialButton> {
                text = "确认修改"
                textColorResource = R.color.white

                setOnClickListener {
                    try {
                        val dipStr = dipEditText.text.toString()
                        val dpi = dipStr.toInt()
                        if (dpi <= 0) {
                            toast("请输入大于0的整数")
                        } else {
                            activity.setCustomDensityDpi(dpi)
                        }
                    } catch (ex: NumberFormatException) {
                        toast("请输入整数")
                    }
                }
            }, lParams(matchParent, wrapContent) {
                horizontalMargin = dip(10)
                bottomMargin = dip(10)
            })
        }.wrapInScrollView(height = ViewGroup.LayoutParams.MATCH_PARENT)

    }

}