package com.a10miaomiao.bilimiao.compose.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.a10miaomiao.bilimiao.compose.ui.NavGraphs
import com.a10miaomiao.bilimiao.compose.ui.destinations.PopularScreenDestination
import com.a10miaomiao.bilimiao.compose.ui.destinations.PostScreenDestination
import com.a10miaomiao.bilimiao.compose.ui.destinations.RecommendScreenDestination
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.rememberNavHostEngine

@RootNavGraph(start = true)
@Composable
@Destination
fun HomeScreen(){
    val engine = rememberNavHostEngine()
    val homeNavCtrl = engine.rememberNavController()
    Scaffold(
        topBar = {
            var state by remember { mutableIntStateOf(1) }

            TabRow(selectedTabIndex = state) {
                Tab(state == 0, onClick = {
                    state = 0
                    homeNavCtrl.navigate(RecommendScreenDestination)
                }){
                    Text(text = "Recommend")
                }
                Tab(state == 1, onClick = {
                    state = 1
                    homeNavCtrl.navigate(PopularScreenDestination)
                }){
                    Text(text = "Popular")
                }
                Tab(state == 2, onClick = {
                    state = 2
                    homeNavCtrl.navigate(PostScreenDestination)
                }){
                    Text(text = "Posts")
                }
            }
        }
    ) { innerPadding ->
        DestinationsNavHost(
            engine = engine,
            navController = homeNavCtrl,
            navGraph = NavGraphs.root,
            startRoute = PopularScreenDestination,
            modifier = Modifier.padding(innerPadding)
        )
    }

}
