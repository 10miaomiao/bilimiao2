package com.a10miaomiao.bilimiao.compose.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.a10miaomiao.bilimiao.compose.ui.destinations.FilterSettingScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Composable
@Destination
fun SettingsScreen(navigator: DestinationsNavigator){
    Scaffold {
        Column(Modifier.padding(it)) {

            ListItem(
                leadingContent = {
                    Icon(Icons.Filled.Block, contentDescription = "")
                },
                headlineContent = { Text("屏蔽设置") },
                supportingContent = { Text("还你一个纯净的社区") },
                trailingContent = {
                    Icon(Icons.Filled.ChevronRight,"")
                },
                modifier = Modifier.clickable {
                    navigator.navigate(FilterSettingScreenDestination)
                }

            )
            HorizontalDivider()


            ListItem(
                headlineContent = { Text("One line list item with 24x24 icon") },
                leadingContent = {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Localized description",
                    )
                }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Three line list item") },
                overlineContent = { Text("OVERLINE") },
                supportingContent = { Text("Secondary text") },
                leadingContent = {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Localized description",
                    )
                },
                trailingContent = { Text("meta") }
            )

        }
    }
}
