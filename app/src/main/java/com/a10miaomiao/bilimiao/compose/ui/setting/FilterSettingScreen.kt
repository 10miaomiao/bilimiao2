package com.a10miaomiao.bilimiao.compose.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.GroupOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.a10miaomiao.bilimiao.compose.ui.destinations.FilterWordSettingScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
@Destination
fun FilterSettingScreen(navigator: DestinationsNavigator){
    Scaffold(
        topBar = {
            Text(text = "屏蔽设置")
        }
    ) {
        Column(Modifier.padding(it)) {
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.Block,
                        contentDescription = "",
                    )
                },
                headlineContent = { Text("屏蔽标题") },
                modifier = Modifier.clickable {
                    navigator.navigate(FilterWordSettingScreenDestination)
                }
            )
            HorizontalDivider()
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.GroupOff,
                        contentDescription = "",
                    )
                },
                headlineContent = { Text("屏蔽UP主") }
            )
            HorizontalDivider()
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.Block,
                        contentDescription = "",
                    )
                },
                headlineContent = { Text("屏蔽标签") }
            )
            HorizontalDivider()
            // TODO junction to help message
        }
    }
}
