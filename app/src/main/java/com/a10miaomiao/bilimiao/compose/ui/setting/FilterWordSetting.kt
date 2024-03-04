package com.a10miaomiao.bilimiao.compose.ui.setting

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
@Destination
fun FilterWordSettingScreen(navigator: DestinationsNavigator){
    Text(text = "Filter word setting")
}