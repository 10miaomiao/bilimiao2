package com.a10miaomiao.bilimiao.ui.setting

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.MainActivity

/**
 * Created by 10喵喵 on 2017/10/28.
 */
class SettingPreferenceFragment : PreferenceFragment() {

    val about by lazy {
        findPreference("about") as Preference
    }
    val donate by lazy {
        findPreference("donate") as Preference
    }
    val help by lazy {
        findPreference("help") as Preference
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference_setting)
        showVersion()
    }

    /**
     * 显示应用版本号
     */
    private fun showVersion() {
        val version = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0).versionName
        about.summary = "版本：$version"
        donate.summary = "开发者想买女朋友o((>ω< ))o"
        help.summary = "有啥不懂的吗(￣▽￣)"
    }


    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: android.preference.Preference?): Boolean {
        when (preference?.key) {
            "player" -> {
//                selectPalyer()
                return true
            }
            "about" -> {
                (activity as MainActivity).start(AboutFragment())
                return true
            }
            "donate" -> {
                val intent = Intent(Intent.ACTION_VIEW)
                //HTTPS://QR.ALIPAY.COM/FKX07587MLQPOBBKACENE1
                try {
                    intent.data = Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/FKX07587MLQPOBBKACENE1")
                    startActivity(intent)
                } catch (e: Exception) {
                    intent.data = Uri.parse("https://qr.alipay.com/FKX07587MLQPOBBKACENE1")
                    startActivity(intent)
                }
                return true
            }
            "theme" -> {
//                ThemePickerActivity.launch(activity)
            }
            "help" -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://10miaomiao.cn/bilimiao/help.html")
                startActivity(intent)
            }
        }
        return false
    }
}