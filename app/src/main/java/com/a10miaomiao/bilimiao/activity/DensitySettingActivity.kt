package com.a10miaomiao.bilimiao.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.utils.ScreenDpiUtil
import com.a10miaomiao.bilimiao.config.config
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.DI
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
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

    private val di: DI = DI.lazy {}

    private val themeDelegate by lazy {
        ThemeDelegate(this@DensitySettingActivity, di)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeDelegate.onCreate(savedInstanceState)
        val ui = DensitySettingUi(this)
        setContentView(ui)
    }

    override fun onBackPressed() {
        if (isChanged()) {
            // 直接重启APP
            val intent = packageManager.getLaunchIntentForPackage(packageName)!!
            val componentName = intent.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        } else {
            finish()
        }
    }

    fun setCustomConfiguration(dpi: Int, fontScale: Float) {
        if (dpi <= 0 || fontScale <= 0f) {
            PopTip.show("请输入大于0的整数")
        }
        ScreenDpiUtil.saveCustomConfiguration(dpi, fontScale)
        reStartActivity()
    }

    private fun reStartActivity() {
        val intent = intent
        intent.putExtra("changed", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent)
    }

    private fun isChanged(): Boolean {
        return intent.getBooleanExtra("changed", false)
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration: Configuration = newBase.resources.configuration
        ScreenDpiUtil.readCustomConfiguration(configuration)
        val newContext = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(newContext)
    }


    @OptIn(InternalSplittiesApi::class)
    class DensitySettingUi(
        val activity: DensitySettingActivity
    ) : Ui {

        override val ctx = activity

        val toolBar = view<MaterialToolbar>(View.generateViewId()) {
            clipToPadding = true
            fitsSystemWindows = true
            setTitle(R.string.density_setting)
            setNavigationIcon(R.drawable.ic_back_24dp)
            setNavigationOnClickListener {
                activity.onBackPressed()
            }
        }

        val dipEditText = editText {
            setRawInputType(InputType.TYPE_CLASS_NUMBER)
            setText(resources.configuration.densityDpi.toString())
        }

        val fontScaleEditText = editText {
            setRawInputType(InputType.TYPE_CLASS_NUMBER)
            setText(resources.configuration.fontScale.toString())
        }

        override val root: View = verticalLayout {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            fitsSystemWindows = true

            addView(toolBar, lParams(matchParent, wrapContent))

            addView(textView {
                text = "系统默认DPI：${ScreenDpiUtil.getDefaultDpi()}"
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

            addView(textView {
                text = "系统默认字体缩放：${ScreenDpiUtil.getDefaultFontScale()}"
            }, lParams(matchParent, wrapContent) {
                topMargin = dip(10)
                horizontalMargin = dip(10)
            })
            addView(textView {
                text = "当前字体缩放修改："
            }, lParams(matchParent, wrapContent) {
                horizontalMargin = dip(10)
            })
            addView(fontScaleEditText, lParams(matchParent, wrapContent) {
                horizontalMargin = dip(10)
            })


            addView(view<MaterialButton> {
                text = "确认修改"
                backgroundColor = activity.themeDelegate.themeColor.toInt()
                textColorResource = R.color.white

                setOnClickListener {
                    try {
                        val dipStr = dipEditText.text.toString()
                        val dpi = dipStr.toInt()
                        val fontScaleStr = fontScaleEditText.text.toString()
                        val fontScale = fontScaleStr.toFloat()
                        activity.setCustomConfiguration(dpi, fontScale)
                    } catch (ex: NumberFormatException) {
                        PopTip.show("请输入整数")
                    }
                }
            }, lParams(matchParent, wrapContent) {
                horizontalMargin = dip(10)
                bottomMargin = dip(10)
            })
        }.wrapInScrollView(height = ViewGroup.LayoutParams.MATCH_PARENT) {
            backgroundColor = config.windowBackgroundColor
        }

    }

}