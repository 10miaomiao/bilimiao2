package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.support.v4.app.Fragment
import com.a10miaomiao.bilimiao.entity.Archive

class SearchResultViewModel(val fragment: Fragment) : ViewModel() {

    val list = ArrayList<Archive>()

    init {
        SearchFragment.keyword.observe(fragment, Observer {
            loadData()
        })
    }

    fun loadData() {

    }
}