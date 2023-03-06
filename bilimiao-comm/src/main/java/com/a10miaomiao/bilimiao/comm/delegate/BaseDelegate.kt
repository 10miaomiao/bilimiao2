package com.a10miaomiao.bilimiao.comm.delegate

import android.os.Bundle

interface BaseDelegate {
    fun onCreate(savedInstanceState: Bundle?)
    fun onResume()
    fun onPause()
    fun onStart()
    fun onStop()
    fun onDestroy()
    fun onBackPressed(): Boolean
}