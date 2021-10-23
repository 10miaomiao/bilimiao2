package cn.a10miaomiao.player;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.pragma.DebugLog;

public class PlayerService extends Service {

    public static final String ACTION_CREATED = "cn.a10miaomiao.player.action.CREATED";
    private static PlayerService mPlayerService;

    public static PlayerService getInstance() {
        return mPlayerService;
    }


    private static final String TAG = PlayerService.class.getSimpleName();

    private PlayerBinder playerBinder;
    private IMediaPlayer mMediaPlayer = null;
    private VideoPlayerView videoPlayerView;

    List<VideoSource> mSources;
    Map<String, String> mHeaders;
    String mUserAgent;

    /**
     * 多段视频
     */
    private List<IMediaPlayer> mediaPlayers = null;
    int index = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayerService = this;
        playerBinder = new PlayerBinder(this);

        Intent intent = new Intent(ACTION_CREATED);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

    public void setVideoPlayerView(VideoPlayerView videoPlayerView) {
        this.videoPlayerView = videoPlayerView;
        if (videoPlayerView != null){
            videoPlayerView.setPlayerService(this);
        }
    }

//    public void setVideoPath(String path) {
//        setVideoURI(Uri.parse(path));
//    }
//
//    public void setVideoURI(Uri uri) {
//        setVideoURI(uri, null);
//    }

//    public void setVideoURI(Uri uri, Map<String, String> headers) {
//        mUri = uri;
//        mHeaders = headers;
//        mSeekWhenPrepared = 0;
//        openVideo();
//        requestLayout();
//        invalidate();
//    }

    public void setVideoURI(List<VideoSource> sources, Map<String, String> headers) {
        mSources = sources;
        mHeaders = headers;
        if (videoPlayerView != null) videoPlayerView.mSeekWhenPrepared = 0;
        openVideo();
        if (videoPlayerView != null)
            videoPlayerView.requestLayout();
            videoPlayerView.invalidate();
    }

    public void setUserAgent(String ua) {
        mUserAgent = ua;
    }

    void openVideo() {
        if (mSources == null || videoPlayerView == null || videoPlayerView.mSurfaceHolder == null)
            return;

        release(false);
        try {
            videoPlayerView.mDuration = -1;
            videoPlayerView.mCurrentBufferPercentage = 0;
            mediaPlayers = new ArrayList();
            for (VideoSource videoSource : mSources) {
                mediaPlayers.add(initPlayer(videoSource.getUri()));
            }
            mMediaPlayer = mediaPlayers.get(0);
            mMediaPlayer.setDisplay(videoPlayerView.mSurfaceHolder);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            videoPlayerView.setMediaPlayer(mMediaPlayer);
            videoPlayerView.mCurrentState = VideoPlayerView.STATE_PREPARING;
            videoPlayerView.attachMediaController();
        } catch (IOException ex) {
            DebugLog.e(TAG, "Unable to open content: ", ex);
            videoPlayerView.mCurrentState = VideoPlayerView.STATE_ERROR;
            videoPlayerView.mTargetState = VideoPlayerView.STATE_ERROR;
            videoPlayerView.mErrorListener.onError(mMediaPlayer,
                    IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            DebugLog.e(TAG, "Unable to open content: ", ex);
            videoPlayerView.mCurrentState = VideoPlayerView.STATE_ERROR;
            videoPlayerView.mTargetState = VideoPlayerView.STATE_ERROR;
            videoPlayerView.mErrorListener.onError(mMediaPlayer,
                    IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }

    /**
     * 初始化一个新的播放器
     *
     * @param uri
     * @return
     * @throws IOException
     */
    private IMediaPlayer initPlayer(@NonNull Uri uri) throws IOException {
        IjkMediaPlayer ijkMediaPlayer = null;

        ijkMediaPlayer = new IjkMediaPlayer();
//        ijkMediaPlayer.setLogEnabled(false);
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", "48");
        if (mUserAgent != null) {
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", mUserAgent);
            //ijkMediaPlayer.setAvFormatOption("user_agent", mUserAgent);
        }

        mMediaPlayer = ijkMediaPlayer;
        assert mMediaPlayer != null;
        if (videoPlayerView != null) {
            ijkMediaPlayer.setOnPreparedListener(videoPlayerView.mPreparedListener);
            ijkMediaPlayer.setOnVideoSizeChangedListener(videoPlayerView.mSizeChangedListener);
            ijkMediaPlayer.setOnCompletionListener(videoPlayerView.mCompletionListener);
            ijkMediaPlayer.setOnErrorListener(videoPlayerView.mErrorListener);
            ijkMediaPlayer.setOnBufferingUpdateListener(videoPlayerView.mBufferingUpdateListener);
            ijkMediaPlayer.setOnInfoListener(videoPlayerView.mInfoListener);
            ijkMediaPlayer.setOnSeekCompleteListener(videoPlayerView.mSeekCompleteListener);
        }
        if (mHeaders != null) {
            ijkMediaPlayer.setDataSource(this, uri, mHeaders);
        } else {
            ijkMediaPlayer.setDataSource(this, uri);
        }

        return ijkMediaPlayer;
    }


    public void release(boolean cleartargetstate) {
        videoPlayerView.mCurrentState = VideoPlayerView.STATE_IDLE;
        if (mediaPlayers == null) {
            return;
        }
        for (IMediaPlayer im : mediaPlayers) {
            if (im != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
            }
        }
        if (cleartargetstate)
            videoPlayerView.mTargetState = VideoPlayerView.STATE_IDLE;
        index = 0;
    }

    /**
     * 切换播放器
     */
    void switchPlayer(int index) {
        mMediaPlayer.stop();
        mMediaPlayer = mediaPlayers.get(index);
        this.index = index;
        mMediaPlayer.setDisplay(videoPlayerView.mSurfaceHolder);
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.prepareAsync();
        videoPlayerView.setMediaPlayer(mMediaPlayer);
        videoPlayerView.start();
    }

    public void seekTo(long msec) {
        if (mSources == null || videoPlayerView.seekToFlag)
            return;
        videoPlayerView.seekToFlag = true;
        int i = 0;
        long length = mSources.get(i).getLength(); //视频长度
        long beforeLength = 0;
        while (msec > length) {
            i++;
            beforeLength = length;
            length += mSources.get(i).getLength();
        }
        msec -= beforeLength;
        if (i != index) {
            switchPlayer(i);
        }
        if (videoPlayerView.isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            videoPlayerView.mSeekWhenPrepared = 0;
        } else {
            videoPlayerView.mSeekWhenPrepared = msec;
        }
        videoPlayerView.seekToFlag = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayerService = null;
    }
}
