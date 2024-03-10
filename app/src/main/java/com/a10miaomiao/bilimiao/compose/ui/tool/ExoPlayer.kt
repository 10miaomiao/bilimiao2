package com.a10miaomiao.bilimiao.compose.ui.tool

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.a10miaomiao.bilimiao.comm.network.ApiHelper

@OptIn(UnstableApi::class)
fun customCaMediaSourceFactory(context: Context): DefaultMediaSourceFactory {
    ApiHelper
    return DefaultMediaSourceFactory(context)
        .setDataSourceFactory(
            DefaultDataSource.Factory(
                context,
                DefaultHttpDataSource.Factory()
                    .setUserAgent("Bilibili Freedoooooom/MarkII") // What fuck is this
                    .setAllowCrossProtocolRedirects(true)
                    .setDefaultRequestProperties(HashMap<String, String>().apply {
                        put("Referer", "https://www.bilibili.com/")
                        put("User-Agent", "Bilibili Freedoooooom/MarkII")
                    })
            )
        )
}