package cn.a10miaomiao.bilimiao.compose.common

import android.app.Activity
import android.content.Context
import android.content.Intent

interface ComposeHostBridge {
    val context: Context
    val activity: Activity

    fun finishHost()
    fun startActivity(intent: Intent)
    fun runOnUiThread(action: () -> Unit)
}
