package cn.a10miaomiao.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.a10miaomiao.player.callback.MediaPlayerListener;
import cn.a10miaomiao.player.callback.MediaController;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.pragma.DebugLog;

/**
 * Created by hcc on 16/8/31 19:50
 * 100332338@qq.com
 * <p/>
 * 自定义VideoView
 */
public class VideoPlayerView extends SurfaceView implements MediaPlayerListener {

    private static final String TAG = VideoPlayerView.class.getName();

    public static final int VIDEO_LAYOUT_ORIGIN = 0;

    public static final int VIDEO_LAYOUT_SCALE = 1;

    public static final int VIDEO_LAYOUT_STRETCH = 2;

    public static final int VIDEO_LAYOUT_ZOOM = 3;

    private static final int STATE_ERROR = -1;

    private static final int STATE_IDLE = 0;

    private static final int STATE_PREPARING = 1;

    private static final int STATE_PREPARED = 2;

    private static final int STATE_PLAYING = 3;

    private static final int STATE_PAUSED = 4;

    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private static final int STATE_SUSPEND = 6;

    private static final int STATE_RESUME = 7;

    private static final int STATE_SUSPEND_UNSUPPORTED = 8;

    private Uri mUri;

    /**
     * 多个视频段
     */
    private List<VideoSource> mSources;

    private Map<String, String> mHeaders;

    private long mDuration;

    private String mUserAgent;

    private int mCurrentState = STATE_IDLE;

    private int mTargetState = STATE_IDLE;

    private int mVideoLayout = VIDEO_LAYOUT_SCALE;

    private SurfaceHolder mSurfaceHolder = null;

    private IMediaPlayer mMediaPlayer = null;

    /**
     * 多个播放器
     */
    private List<IMediaPlayer> mediaPlayers = null;


    private int index = 0;

    private int mVideoWidth;

    private int mVideoHeight;

    private int mVideoSarNum;

    private int mVideoSarDen;

    private int mSurfaceWidth;

    private int mSurfaceHeight;

    private MediaController mMediaController;

    private View mMediaBufferingIndicator;

    private OnCompletionListener mOnCompletionListener;

    private OnPreparedListener mOnPreparedListener;

    private OnErrorListener mOnErrorListener;

    private OnSeekCompleteListener mOnSeekCompleteListener;

    private OnInfoListener mOnInfoListener;

    private OnBufferingUpdateListener mOnBufferingUpdateListener;

    private OnControllerEventsListener mOnControllerEventsListener;

    private GestureDetector gestureDetector;

    private OnGestureEventsListener onGestureEventsListener;

    private int mCurrentBufferPercentage;

    private long mSeekWhenPrepared;

    private boolean mCanPause = true;

    private boolean mCanSeekBack = true;

    private boolean mCanSeekForward = true;

    private Context mContext;

    OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {

        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height,
                                       int sarNum, int sarDen) {
            if (mp != mMediaPlayer)
                return;
            DebugLog.dfmt(TAG, "onVideoSizeChanged: (%dx%d)", width, height);
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoSarNum = sarNum;
            mVideoSarDen = sarDen;
            if (mVideoWidth != 0 && mVideoHeight != 0)
                setVideoLayout(mVideoLayout);
        }
    };

    OnPreparedListener mPreparedListener = new OnPreparedListener() {

        public void onPrepared(IMediaPlayer mp) {
            if (mp != mMediaPlayer)
                return;
            DebugLog.d(TAG, "onPrepared");
            mCurrentState = STATE_PREPARED;
            mTargetState = STATE_PLAYING;

            if (mOnPreparedListener != null)
                mOnPreparedListener.onPrepared(mMediaPlayer);
            if (mMediaController != null)
                mMediaController.setEnabled(true);
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();

            long seekToPosition = mSeekWhenPrepared;

            if (seekToPosition != 0)
                seekTo(seekToPosition);
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                setVideoLayout(mVideoLayout);
                if (mSurfaceWidth == mVideoWidth
                        && mSurfaceHeight == mVideoHeight) {
                    if (mTargetState == STATE_PLAYING) {
                        start();
                        if (mMediaController != null)
                            mMediaController.show();
                    } else if (!isPlaying()
                            && (seekToPosition != 0 || getCurrentPosition() > 0)) {
                        if (mMediaController != null)
                            mMediaController.show(0);
                    }
                }
            } else if (mTargetState == STATE_PLAYING) {
                start();
            }
        }
    };

    private OnCompletionListener mCompletionListener = new OnCompletionListener() {

        public void onCompletion(IMediaPlayer mp) {
            if (mp != mMediaPlayer)
                return;
            index++;
            Log.d("---onCompletionindex---", String.valueOf(index));
            if (index < mSources.size()) {
                switchPlayer(index);//播放下一个视频
                return;
            }
            DebugLog.d(TAG, "onCompletion");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mMediaController != null)
                mMediaController.hide();
            if (mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(mMediaPlayer);
        }
    };

    private OnErrorListener mErrorListener = new OnErrorListener() {

        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            if (mp != mMediaPlayer)
                return false;
            DebugLog.dfmt(TAG, "Error: %d, %d", framework_err, impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mMediaController != null)
                mMediaController.hide();

            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err,
                        impl_err))
                    return true;
            }

            if (getWindowToken() != null) {
                int message = framework_err == IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ?
                        R.string.video_error_text_invalid_progressive_playback : R.string.video_error_text_unknown;

                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.video_error_title)
                        .setMessage(message)
                        .setPositiveButton(
                                R.string.video_error_button,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if (mOnCompletionListener != null)
                                            mOnCompletionListener
                                                    .onCompletion(mMediaPlayer);
                                    }
                                }).setCancelable(false).show();
            }
            return true;
        }
    };

    private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {

        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            if (mp != mMediaPlayer)
                return;
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null)
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    };

    private OnInfoListener mInfoListener = new OnInfoListener() {

        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            if (mp != mMediaPlayer)
                return false;
            DebugLog.dfmt(TAG, "onInfo: (%d, %d)", what, extra);
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            } else if (mMediaPlayer != null) {
                if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    DebugLog.dfmt(TAG, "onInfo: (MEDIA_INFO_BUFFERING_START)");
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(View.VISIBLE);
                } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    DebugLog.dfmt(TAG, "onInfo: (MEDIA_INFO_BUFFERING_END)");
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(View.GONE);
                }
            }

            return true;
        }
    };

    private OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {

            DebugLog.d(TAG, "onSeekComplete");
            if (mOnSeekCompleteListener != null)
                mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            mSurfaceHolder = holder;
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(mSurfaceHolder);
            }

            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0)
                    seekTo(mSeekWhenPrepared);
                start();
                if (mMediaController != null) {
                    if (mMediaController.isShowing())
                        mMediaController.hide();
                    mMediaController.show();
                }
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND
                    && mTargetState == STATE_RESUME) {
                mMediaPlayer.setDisplay(mSurfaceHolder);
                resume();
            } else {
                openVideo();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {

            mSurfaceHolder = null;
            if (mMediaController != null)
                mMediaController.hide();
            if (mCurrentState != STATE_SUSPEND)
                release(true);
        }
    };

    private int isXScroll = -1; //-1表示方向未知，0表示水平，1表示垂直
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            isXScroll = -1;
            if (onGestureEventsListener != null) {
                onGestureEventsListener.onDown(e);
            }
            //返回false的话只能响应长摁事件
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (onGestureEventsListener != null) {
                if (onGestureEventsListener.isLocked())
                    return super.onScroll(e1, e2, distanceX, distanceY);
                if (isXScroll == -1) { // 未知
                    isXScroll = Math.abs(distanceX) > Math.abs(distanceY) ? 0 : 1;
                }
                if (isXScroll == 0) { // 水平方向
                    return onGestureEventsListener.onXScroll(e1, e2, distanceX);
                } else if (isXScroll == 1) { // 垂直方向
                    return onGestureEventsListener.onYScroll(e1, e2, distanceY);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (onGestureEventsListener != null)
                if (onGestureEventsListener.isLocked())
                    return false;
            if (isPlaying()) {
                pause();
            } else {
                start();
            }
            return false;
        }

        /**
         * 单击
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isInPlaybackState() && mMediaController != null)
                toggleMediaControlsVisiblity();
            return false;
        }
    };

    public VideoPlayerView(Context context) {

        super(context);
        initVideoView(context);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        initVideoView(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }


    public void setVideoLayout(int layout) {

        LayoutParams lp = getLayoutParams();
        Pair<Integer, Integer> res = ScreenResolution.getResolution(mContext);
        int windowWidth = res.first, windowHeight = res.second;
        float windowRatio = windowWidth / (float) windowHeight;
        int sarNum = mVideoSarNum;
        int sarDen = mVideoSarDen;
        if (mVideoHeight > 0 && mVideoWidth > 0) {
            float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
            if (sarNum > 0 && sarDen > 0)
                videoRatio = videoRatio * sarNum / sarDen;
            mSurfaceHeight = mVideoHeight;
            mSurfaceWidth = mVideoWidth;

            if (VIDEO_LAYOUT_ORIGIN == layout && mSurfaceWidth < windowWidth
                    && mSurfaceHeight < windowHeight) {
                lp.width = (int) (mSurfaceHeight * videoRatio);
                lp.height = mSurfaceHeight;
            } else if (layout == VIDEO_LAYOUT_ZOOM) {
                lp.width = windowRatio > videoRatio ? windowWidth
                        : (int) (videoRatio * windowHeight);
                lp.height = windowRatio < videoRatio ? windowHeight
                        : (int) (windowWidth / videoRatio);
            } else {
                boolean full = layout == VIDEO_LAYOUT_STRETCH;
                lp.width = (full || windowRatio < videoRatio) ? windowWidth
                        : (int) (videoRatio * windowHeight);
                lp.height = (full || windowRatio > videoRatio) ? windowHeight
                        : (int) (windowWidth / videoRatio);
            }
            setLayoutParams(lp);
            getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
            DebugLog.dfmt(
                    TAG,
                    "VIDEO: %dx%dx%f[SAR:%d:%d], Surface: %dx%d, LP: %dx%d, Window: %dx%dx%f",
                    mVideoWidth, mVideoHeight, videoRatio, mVideoSarNum,
                    mVideoSarDen, mSurfaceWidth, mSurfaceHeight, lp.width,
                    lp.height, windowWidth, windowHeight, windowRatio);
        }
        mVideoLayout = layout;
    }

    private void initVideoView(Context ctx) {

        mContext = ctx;
        mVideoWidth = 0;
        mVideoHeight = 0;
        mVideoSarNum = 0;
        mVideoSarDen = 0;
        getHolder().addCallback(mSHCallback);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        if (ctx instanceof Activity)
            ((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        gestureDetector = new GestureDetector(getContext(), simpleOnGestureListener);
    }

    public boolean isValid() {
        return (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid());
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void setVideoURI(List<VideoSource> sources, Map<String, String> headers) {
        mSources = sources;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }


    public void setUserAgent(String ua) {
        mUserAgent = ua;
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }


    private void openVideo() {
        if (mSources == null || mSurfaceHolder == null)
            return;

        Intent freshIntent = new Intent();
        freshIntent.setAction("com.android.music.musicservicecommand.pause");
        freshIntent.putExtra("command","pause");
        getContext().sendBroadcast(freshIntent);

        release(false);
        try {
            mDuration = -1;
            mCurrentBufferPercentage = 0;
            mediaPlayers = new ArrayList();
            for (VideoSource videoSource : mSources) {
                mediaPlayers.add(initPlayer(videoSource.getUri()));
            }
            mMediaPlayer = mediaPlayers.get(0);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            DebugLog.e(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer,
                    IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            DebugLog.e(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer,
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
        ijkMediaPlayer.setLogEnabled(false);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", "48");
        if (mUserAgent != null) {
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", mUserAgent);
            //ijkMediaPlayer.setAvFormatOption("user_agent", mUserAgent);
        }

        mMediaPlayer = ijkMediaPlayer;
        assert mMediaPlayer != null;
        ijkMediaPlayer.setOnPreparedListener(mPreparedListener);
        ijkMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
        ijkMediaPlayer.setOnCompletionListener(mCompletionListener);
        ijkMediaPlayer.setOnErrorListener(mErrorListener);
        ijkMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
        ijkMediaPlayer.setOnInfoListener(mInfoListener);
        ijkMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);

        if (mHeaders != null) {
            ijkMediaPlayer.setDataSource(this.getContext(), uri, mHeaders);
        } else {
            ijkMediaPlayer.setDataSource(this.getContext(), uri);
        }

        return ijkMediaPlayer;
    }

    /**
     * 切换播放器
     */
    private void switchPlayer(int index) {
        mMediaPlayer.stop();
        mMediaPlayer = mediaPlayers.get(index);
        this.index = index;
        mMediaPlayer.setDisplay(mSurfaceHolder);
        mMediaPlayer.setScreenOnWhilePlaying(true);
        mMediaPlayer.prepareAsync();
        start();
    }

    private void old_openVideo() {
        if (mUri == null || mSurfaceHolder == null)
            return;

        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        release(false);
        try {
            mDuration = -1;
            mCurrentBufferPercentage = 0;
            // mMediaPlayer = new AndroidMediaPlayer();
            IjkMediaPlayer ijkMediaPlayer = null;
            if (mUri != null) {
                ijkMediaPlayer = new IjkMediaPlayer();
                ijkMediaPlayer.setLogEnabled(false);

//                ijkMediaPlayer.setAvOption(AvFormatOption_HttpDetectRangeSupport.Disable);
//                ijkMediaPlayer.setOverlayFormat(AvFourCC.SDL_FCC_RV32);
//                ijkMediaPlayer.setMediaCodecEnabled(true);
                ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", "48");
                //ijkMediaPlayer.setAvCodecOption("skip_loop_filter", "48");
                //ijkMediaPlayer.setFrameDrop(12);
                if (mUserAgent != null) {
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", mUserAgent);
                    //ijkMediaPlayer.setAvFormatOption("user_agent", mUserAgent);
                }
            }
            mMediaPlayer = ijkMediaPlayer;
            assert mMediaPlayer != null;
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            if (mUri != null) {
                if (mHeaders != null) {
                    mMediaPlayer.setDataSource(this.getContext(), mUri, mHeaders);
                } else {
                    mMediaPlayer.setDataSource(this.getContext(), mUri);
                }
            }
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            attachMediaController();
        } catch (IOException ex) {
            DebugLog.e(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer,
                    IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            DebugLog.e(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer,
                    IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }
    }


    public void setMediaController(MediaController controller) {
        if (mMediaController != null)
            mMediaController.hide();
        mMediaController = controller;
        attachMediaController();
    }

    public void setMediaBufferingIndicator(View mediaBufferingIndicator) {
        if (mMediaBufferingIndicator != null)
            mMediaBufferingIndicator.setVisibility(View.GONE);
        mMediaBufferingIndicator = mediaBufferingIndicator;
    }

    private void attachMediaController() {

        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this
                    .getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlaybackState());
        }
    }

    public void setOnPreparedListener(OnPreparedListener l) {

        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(OnCompletionListener l) {

        mOnCompletionListener = l;
    }

    public void setOnErrorListener(OnErrorListener l) {

        mOnErrorListener = l;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {

        mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener l) {

        mOnSeekCompleteListener = l;
    }

    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
    }

    public void setOnControllerEventsListener(OnControllerEventsListener l) {
        mOnControllerEventsListener = l;
    }

    private void release(boolean cleartargetstate) {
        mCurrentState = STATE_IDLE;
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
            mTargetState = STATE_IDLE;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (onGestureEventsListener != null)
                onGestureEventsListener.onUp(ev,isXScroll == 0);
            isXScroll = -1;
        }
        return gestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {

        if (isInPlaybackState() && mMediaController != null)
            toggleMediaControlsVisiblity();
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
                && keyCode != KeyEvent.KEYCODE_VOLUME_UP
                && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
                && keyCode != KeyEvent.KEYCODE_MENU
                && keyCode != KeyEvent.KEYCODE_CALL
                && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported
                && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyCode == KeyEvent.KEYCODE_SPACE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    && mMediaPlayer.isPlaying()) {
                pause();
                mMediaController.show();
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
        if (mOnControllerEventsListener != null)
            mOnControllerEventsListener.OnVideoResume();
    }

    @Override
    public void pause() {

        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
        if (mOnControllerEventsListener != null)
            mOnControllerEventsListener.onVideoPause();
    }

    public void resume() {

        if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
            mTargetState = STATE_RESUME;
        } else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
            openVideo();
        }
    }

    @Override
    public long getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0)
                return (int) mDuration;
            /** 修改 获取全部播放源的总长度 */
            mDuration = 0;
            for (VideoSource source : mSources) {
                mDuration += source.getLength();
            }
            if(mDuration == 0){
                mDuration = mMediaPlayer.getDuration();
            }
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    @Override
    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            long position = mMediaPlayer.getCurrentPosition();
            long duration = 0;
            for (int i = 0; i < index; i++) {
                duration += mSources.get(i).getLength();
            }
            return (position + duration);
        }
        return 0;
    }

    //    @Override
//    public void seekTo2(long msec){
//        if (mSources == null)
//            return;
//
//    }
    boolean seekToFlag = false;

    @Override
    public void seekTo(long msec) {
        if (mSources == null || seekToFlag)
            return;
        seekToFlag = true;
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
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
        seekToFlag = false;
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    @Override
    public long getBufferPercentage() {
        if (mMediaPlayer != null) {
            long duration = 0;
            for (int i = 0; i < index; i++) {
                duration += mSources.get(i).getLength();
            }
            return (mCurrentBufferPercentage + duration);
        }
        return 0;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    protected boolean isInPlaybackState() {

        return (mMediaPlayer != null && mCurrentState != STATE_ERROR
                && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    public boolean canPause() {

        return mCanPause;
    }

    public boolean canSeekBackward() {

        return mCanSeekBack;
    }

    public boolean canSeekForward() {

        return mCanSeekForward;
    }

    public void setOnGestureEventsListener(OnGestureEventsListener onGestureEventsListener) {
        this.onGestureEventsListener = onGestureEventsListener;
    }

    public interface OnControllerEventsListener {

        void onVideoPause();

        void OnVideoResume();
    }

    public interface OnGestureEventsListener {
        boolean isLocked();

        boolean onXScroll(MotionEvent e1, MotionEvent e2, float distanceX);

        boolean onYScroll(MotionEvent e1, MotionEvent e2, float distanceY);

        void onDown(MotionEvent e);

        void onUp(MotionEvent e, boolean isXScroll);
    }
}
