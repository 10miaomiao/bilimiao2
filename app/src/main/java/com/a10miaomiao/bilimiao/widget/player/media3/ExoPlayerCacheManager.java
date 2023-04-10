package com.a10miaomiao.bilimiao.widget.player.media3;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import com.shuyu.gsyvideoplayer.cache.ICacheManager;
import java.io.File;
import java.util.Map;
import tv.danmaku.ijk.media.player.IMediaPlayer;

@OptIn(markerClass = UnstableApi.class)
public class ExoPlayerCacheManager implements ICacheManager {

    protected ExoSourceManager mExoSourceManager;

    @Override
    public void doCacheLogic(Context context, IMediaPlayer mediaPlayer, String url, Map<String, String> header, File cachePath) {
        if (!(mediaPlayer instanceof ExoMediaPlayer)) {
            throw new UnsupportedOperationException("ExoPlayerCacheManager only support IjkExo2MediaPlayer");
        }
        ExoMediaPlayer exoPlayer = ((ExoMediaPlayer) mediaPlayer);
        mExoSourceManager = exoPlayer.getExoHelper();
        //通过自己的内部缓存机制
        exoPlayer.setCache(true);
        exoPlayer.setCacheDir(cachePath);
        exoPlayer.setDataSource(context, Uri.parse(url), header);
    }

    @Override
    public void clearCache(Context context, File cachePath, String url) {
        ExoSourceManager.clearCache(context, cachePath, url);
    }

    @Override
    public void release() {
        mExoSourceManager = null;
    }

    @Override
    public boolean hadCached() {
        return mExoSourceManager != null && mExoSourceManager.hadCached();
    }

    @Override
    public boolean cachePreview(Context context, File cacheDir, String url) {
        return ExoSourceManager.cachePreView(context, cacheDir, url);
    }

    @Override
    public void setCacheAvailableListener(ICacheAvailableListener cacheAvailableListener) {

    }
}
