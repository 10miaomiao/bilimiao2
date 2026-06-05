package com.a10miaomiao.bilimiao.comm.delegate

interface BaseDelegate {
    fun onCreate()
    fun onResume()
    fun onPause()
    fun onStart()
    fun onStop()
    fun onDestroy()
    fun onBackPressed(): Boolean
}
