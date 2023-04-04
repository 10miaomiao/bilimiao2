package com.a10miaomiao.bilimiao.comm.dsl

import com.google.android.material.tabs.TabLayout

class OnTabLayoutDoubleClickListener : TabLayout.OnTabSelectedListener {
    var lastPressTime = 0L
    override fun onTabSelected(tab: TabLayout.Tab) {
    }
    override fun onTabUnselected(tab: TabLayout.Tab) {
    }
    override fun onTabReselected(tab: TabLayout.Tab) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPressTime < 2000) {
            lastPressTime = 0
        } else {
            lastPressTime = currentTime
        }
    }
}

fun TabLayout.addOnDoubleClickTabListener(
    onDoubleClick: (tab: TabLayout.Tab) -> Unit,
) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        var lastPressTime = 0L
        override fun onTabSelected(tab: TabLayout.Tab) {
        }
        override fun onTabUnselected(tab: TabLayout.Tab) {
        }
        override fun onTabReselected(tab: TabLayout.Tab) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastPressTime < 2000) {
                lastPressTime = 0
                onDoubleClick(tab)
            } else {
                lastPressTime = currentTime
            }
        }
    })
}