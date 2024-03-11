package com.a10miaomiao.bilimiao.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.a10miaomiao.bilimiao.compose.ui.NavGraphs
import com.a10miaomiao.bilimiao.compose.ui.destinations.DirectionDestination
import com.a10miaomiao.bilimiao.compose.ui.destinations.HomeScreenDestination
import com.a10miaomiao.bilimiao.compose.ui.destinations.SelfScreenDestination
import com.a10miaomiao.bilimiao.compose.ui.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.utils.isRouteOnBackStackAsState

@Composable
fun MiaoApp(){
    val engine = rememberNavHostEngine()
    val navCtrl = engine.rememberNavController()

    // A surface container using the 'background' color from the theme
    val items = listOf(
        BottomBarDestination.Self,
        BottomBarDestination.Home,
        BottomBarDestination.Settings
    )
    Scaffold(
        bottomBar = { BottomBar(navCtrl,items) }
    ) { innerPadding ->
        DestinationsNavHost(
            engine = engine,
            navController = navCtrl,
            navGraph = NavGraphs.root,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun BottomBar(
    navCtrl: NavHostController,
    items: List<BottomBarDestination>
) {

    NavigationBar {
        items.forEach { destination ->
            val isCurrentDestOnBackStack by navCtrl.isRouteOnBackStackAsState(destination.dest)
            NavigationBarItem(
                selected = isCurrentDestOnBackStack,
                onClick = {
                    if (isCurrentDestOnBackStack) {
                        // When we click again on a bottom bar item and it was already selected
                        // we want to pop the back stack until the initial destination of this bottom bar item
                        navCtrl.popBackStack(destination.dest, false)
                        return@NavigationBarItem
                    }

                    navCtrl.navigate(destination.dest) {
                        // Pop up to the root of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(NavGraphs.root) {
                            saveState = true
                        }

                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = { if (isCurrentDestOnBackStack) {
                    Icon(destination.selectedIcon, "")
                } else {
                    Icon(destination.icon, "")
                }},
                label = { Text(stringResource(destination.label)) },
            )
        }
    }
}


sealed class BottomBarDestination(@StringRes val label: Int, val dest: DirectionDestination, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Self : BottomBarDestination(R.string.self, SelfScreenDestination, Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
    data object Home : BottomBarDestination(R.string.home, HomeScreenDestination, Icons.Outlined.Home ,Icons.Filled.Home)
    data object Settings: BottomBarDestination(R.string.settings, SettingsScreenDestination, Icons.Outlined.Settings, Icons.Filled.Settings)
}