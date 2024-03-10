package com.a10miaomiao.bilimiao.compose.ui.home

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.compose.ui.destinations.VideoViewerScreenDestination
import com.a10miaomiao.bilimiao.compose.ui.tool.MutableLazyListSaver
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.parcelize.Parcelize

// data class RecommendVideoData()

@Parcelize
data class VideoInfo(
    val title: String,
    val cover: String,
    val id: String,
    val type: String,
    val upName: String
) : Parcelable

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun RecommendScreen(navigator: DestinationsNavigator) = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter
) {
    val listState = rememberLazyListState()
    val recommends = rememberSaveable(
        saver = MutableLazyListSaver(listState)
    ) {
        mutableStateListOf<VideoInfo>()
    }
    var needFetch by rememberSaveable { mutableStateOf(true) }


    LaunchedEffect(needFetch){
        if (!needFetch) return@LaunchedEffect
        repeat(2){
            val info = BiliApiService.homeApi.recommendListAwait(0).data.items.map {
                VideoInfo(
                    it.title,
                    it.cover.replace("http://","https://"),
                    it.param,
                    it.card_goto,
                    it.args.up_name ?: "UnknownUp"
                )
            }
            recommends.addAll(info)
        }
        needFetch = false
    }

    // TODO: Performance improve
    LazyColumn(state = listState) {
        itemsIndexed(
            recommends,
            contentType = { _, video -> video }
        ) {_:Int, video:VideoInfo ->
            OutlinedCard(
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .height(120.dp)
                    .background(Color.Black)
                    .clickable {
                        navigator.navigate(VideoViewerScreenDestination(video.type, video.id))
                    }
            ) {
                ListItem(
                    leadingContent = {
                        AsyncImage(
                            video.cover, null,
                            contentScale = ContentScale.Crop, // 不裁剪 不填充空白 最大可能截取原始图片
                            modifier = Modifier
                                .height(100.dp)
                                .width(160.dp)
                        )
                    },
                    headlineContent = {
                        Text(text = video.title, maxLines = 3)
                    },
                    supportingContent = {
                        Text(text = video.upName)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
