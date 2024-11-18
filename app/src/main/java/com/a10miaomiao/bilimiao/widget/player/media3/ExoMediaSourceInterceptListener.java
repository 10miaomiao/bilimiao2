package com.a10miaomiao.bilimiao.widget.player.media3;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaMetadata;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.TransferListener;
import androidx.media3.exoplayer.source.MediaSource;

import java.io.File;
import java.util.Map;

/**
 * 设置 ExoPlayer 的 MediaSource 创建拦截
 */
public interface ExoMediaSourceInterceptListener {
    /**
     * @param dataSource  链接
     * @param preview     是否带上header，默认有header自动设置为true
     * @param cacheEnable 是否需要缓存
     * @param isLooping   是否循环
     * @param cacheDir    自定义缓存目录
     * @return 返回不为空时，使用返回的自定义mediaSource
     */
    MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir);

    MediaMetadata getMediaMetadata(String dataSource);

    /**
     * @return 返回不为空时，使用返回的自定义 HttpDataSource
     */
    DataSource.Factory getHttpDataSourceFactory(
            String userAgent,
            @Nullable TransferListener listener,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            Map<String, String> mapHeadData,
            boolean allowCrossProtocolRedirects);
}

