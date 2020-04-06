package com.a10miaomiao.bilimiao.ui.shortcut

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import org.jetbrains.anko.startActivity

class SearchShortcutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity<MainActivity>(
                ConstantUtil.FROM_SHORTCUT to true,
                ConstantUtil.SHORTCUT_NAME to ConstantUtil.SHORTCUT_SEARCH
        )
        finish()
    }
}