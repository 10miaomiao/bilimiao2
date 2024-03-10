package com.a10miaomiao.bilimiao.compose.ui.self


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.compose.state.UserState
import com.a10miaomiao.bilimiao.compose.ui.destinations.QrCodeLoginScreenDestination
import com.a10miaomiao.bilimiao.compose.ui.destinations.WebViewScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
@Destination
fun LoginScreen (navigator: DestinationsNavigator) = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
){

    var user by remember { UserState }

    if (user != null){
        Column {
            Button(onClick = {
                BilimiaoCommApp.commApp.deleteAuth()
                user = null
            }) {
                Text(text = "Logout")
            }
        }
    } else {
        Column {
            Button(onClick = {
                navigator.navigate(WebViewScreenDestination)
            }) {
                Text(text = "WebLogin - WIP")
            }
            Button(onClick = {
                navigator.navigate(QrCodeLoginScreenDestination)
            }) {
                Text(text = "QrCodeLogin")
            }
        }
    }

}


