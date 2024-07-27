package com.a10miaomiao.bilimiao.comm.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.decoder.av1.Libgav1VideoRenderer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.video.VideoRendererEventListener
import com.a10miaomiao.bilimiao.widget.player.media3.ExoMediaPlayer
import com.a10miaomiao.bilimiao.widget.player.media3.Media3ExoPlayerManager

@UnstableApi
class Libgav1Media3ExoPlayerManager : Media3ExoPlayerManager() {
    private var renderersFactory: DefaultRenderersFactory? = null

    override fun buildMediaPlayer(context: Context): ExoMediaPlayer {
        if (renderersFactory == null) {
            renderersFactory = Libgav1VideoRendererFactory(context)
        }
        val exoMediaPlayer = ExoMediaPlayer(context)
        exoMediaPlayer.rendererFactory = renderersFactory
        return exoMediaPlayer
    }

    internal inner class Libgav1VideoRendererFactory(context: Context)
        : DefaultRenderersFactory(context) {
        init {
            setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON)
        }

        override fun buildVideoRenderers(
            context: android.content.Context,
            extensionRendererMode: Int,
            mediaCodecSelector: MediaCodecSelector,
            enableDecoderFallback: Boolean,
            eventHandler: android.os.Handler,
            eventListener: VideoRendererEventListener,
            allowedVideoJoiningTimeMs: Long,
            out: java.util.ArrayList<androidx.media3.exoplayer.Renderer>
        ) {
            val renderer = Libgav1VideoRenderer(
                allowedVideoJoiningTimeMs,
                eventHandler,
                eventListener,
                MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY
            )
            out.add(renderer)
            super.buildVideoRenderers(
                context,
                extensionRendererMode,
                mediaCodecSelector,
                enableDecoderFallback,
                eventHandler,
                eventListener,
                allowedVideoJoiningTimeMs,
                out
            )
        }
    }
}