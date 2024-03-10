package com.a10miaomiao.bilimiao.compose.ui.home

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.a10miaomiao.bilimiao.compose.ui.destinations.FilterSettingScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
fun PostScreen(navigator: DestinationsNavigator) {

    Button(onClick = {
        navigator.navigate(FilterSettingScreenDestination)
    }) {
        Text(text = "Block Settings")
    }
}