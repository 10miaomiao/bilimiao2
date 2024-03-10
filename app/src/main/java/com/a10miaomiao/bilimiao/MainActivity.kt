package com.a10miaomiao.bilimiao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.media3.common.util.UnstableApi
import com.a10miaomiao.bilimiao.compose.BilimiaoTheme
import com.a10miaomiao.bilimiao.compose.MiaoApp
import com.a10miaomiao.bilimiao.compose.state.windowSize

class MainActivity : ComponentActivity() {
    @androidx.annotation.OptIn(UnstableApi::class)
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            BilimiaoTheme {
                windowSize = calculateWindowSizeClass(this)
                // val displayFeatures = calculateDisplayFeatures(this)
                MiaoApp()
            }
        }
    }
}
