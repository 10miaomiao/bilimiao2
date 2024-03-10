package com.a10miaomiao.bilimiao.compose.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.a10miaomiao.bilimiao.compose.ui.home.PopularScreen
import com.a10miaomiao.bilimiao.compose.ui.home.PostScreen
import com.a10miaomiao.bilimiao.compose.ui.home.RecommendScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalFoundationApi::class)
@RootNavGraph(start = true)
@Composable
@Destination
fun HomeScreen(navigator: DestinationsNavigator){


    var topBarState by rememberSaveable { mutableIntStateOf(1) }
    val pagerState = rememberPagerState(topBarState) { 3 }
    LaunchedEffect(pagerState.targetPage){
        topBarState = pagerState.targetPage
    }
    LaunchedEffect(topBarState){
        pagerState.animateScrollToPage(topBarState)
    }
    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        TabRow(topBarState) {
            Tab(
                topBarState == 0,
                onClick = { topBarState = 0 },
                icon = { Icon(Icons.Filled.Recommend, "") },
                text = { Text(text = "Recommend") }
            )
            Tab(
                topBarState == 1,
                onClick = { topBarState = 1 },
                text = { Text("Popular") },
                icon = { Icon(Icons.Filled.Whatshot,"") }
            )
            Tab(
                topBarState == 1,
                onClick = { topBarState = 2 },
                icon = { Icon(Icons.Filled.DynamicFeed,"") },
                text = { Text(text = "Posts") }
            )
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),

        ) {index ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (index) {
                    0 -> {
                        RecommendScreen(navigator)
                    }
                    1 -> {
                        PopularScreen(navigator)
                    }
                    2 -> {
                        PostScreen(navigator)
                    }
                }
            }
        }
    }
}
