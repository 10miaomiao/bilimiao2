package com.a10miaomiao.bilimiao.comm.delegate.helper

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

class SupportHelper(
    val activity: AppCompatActivity
) {

    private val SHOW_SPACE = 500L

    /**
     * 显示软键盘
     */
    fun showSoftInput(view: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.postDelayed(
            {
                view.requestFocus()
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
            },
            SHOW_SPACE
        )
    }

    /**
     * 隐藏软键盘
     */
    fun hideSoftInput(view: View) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}