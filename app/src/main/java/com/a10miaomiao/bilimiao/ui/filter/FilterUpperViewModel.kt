package com.a10miaomiao.bilimiao.ui.filter

import android.arch.lifecycle.ViewModel
import android.content.Context

class FilterUpperViewModel(
        val context: Context
) : ViewModel(){
    val selectedList = HashSet<Int>()

    fun selectedChange(index: Int, isChecked: Boolean) {
        if (isChecked) {
            selectedList.add(index)
        } else {
            selectedList.remove(index)
        }
    }

    fun selectAll(count: Int) {
        selectedList.addAll(0 until count)
    }

    fun unSelectAll() {
        selectedList.clear()
    }
}