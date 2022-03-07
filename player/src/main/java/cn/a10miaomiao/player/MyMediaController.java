package cn.a10miaomiao.player;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;

import java.util.Locale;

import cn.a10miaomiao.player.callback.MediaController;
import cn.a10miaomiao.player.callback.MediaPlayerListener;

public class MyMediaController extends FrameLayout implements MediaController
        , View.OnClickListener, View.OnTouchListener {

    private MediaPlayerListener mPlayer;
    private boolean mDragging;
    private long mDuration = 0L;
    private boolean mDanmakuShow = true;
    private Fun2<Boolean> danmakuSwitchEvent;
    private Fun videoBackEvent;
    private View.OnClickListener qualityEvent;
    private boolean isLocked = false;

    private LinearLayout mHeaderLayout;
    private LinearLayout mMediaMontrollerControls;
    private ImageView mBackIV;
    private TextView mTitleTV;
    private ImageView mMoreIv;
    private ImageView mTvPlay;
    private ImageView mPauseButton;
    private ImageView mOpenLockLeftIV;
    private ImageView mOpenLockRightIV;
    private SeekBar mProgress;
    private TextView mCurrentTime;
    private TextView mEndTime;
    private LinearLayout mDanmakuSwitchLayout;
    private TextView mDanmakuSwitchTV;
    private ImageView mDanmakuSwitchIV;
    private LinearLayout mLockLayout;
    private LinearLayout mQualityLayout;

    private PopupMenu mMorePopupMenu;

    private Fun2<Long> restartPlayEvent;
    private Fun2<Boolean> visibilityChangedEvent;

    public MyMediaController(@NonNull Context context) {
        super(context);
        initView();
    }

    public MyMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MyMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void bindView() {
        mHeaderLayout = findViewById(R.id.mHeaderLayout);
        mMediaMontrollerControls = findViewById(R.id.mMediaMontrollerControls);
        mBackIV = findViewById(R.id.mBackIV);
        mTitleTV = findViewById(R.id.mTitleTV);
        mMoreIv = findViewById(R.id.mMoreIv);
        mTvPlay = findViewById(R.id.mTvPlay);
        mPauseButton = findViewById(R.id.mPauseButton);
        mOpenLockLeftIV = findViewById(R.id.mOpenLockLeftIV);
        mOpenLockRightIV = findViewById(R.id.mOpenLockRightIV);
        mProgress = findViewById(R.id.mProgress);
        mCurrentTime = findViewById(R.id.mCurrentTime);
        mEndTime = findViewById(R.id.mEndTime);
        mDanmakuSwitchLayout = findViewById(R.id.mDanmakuSwitchLayout);
        mDanmakuSwitchTV = findViewById(R.id.mDanmakuSwitchTV);
        mDanmakuSwitchIV = findViewById(R.id.mDanmakuSwitchIV);
        mLockLayout = findViewById(R.id.mLockLayout);
        mQualityLayout = findViewById(R.id.mQualityLayout);
    }

    private void initView() {
        View.inflate(getContext(), R.layout.layout_media_controller, this);
        bindView();
        mBackIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoBackEvent != null)
                    videoBackEvent.accept();
            }
        });
        mMorePopupMenu = new PopupMenu(
                getContext(),
                mMoreIv
        );
        mMoreIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mMorePopupMenu.show();
            }
        });
        mDanmakuSwitchLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDanmakuShow = !mDanmakuShow;
                setDanmakuShow(mDanmakuShow);
                if (danmakuSwitchEvent != null)
                    danmakuSwitchEvent.accept(mDanmakuShow);
            }
        });
        mQualityLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qualityEvent != null){
                    qualityEvent.onClick(v);
                }
            }
        });
        mLockLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMediaController.this.lock();
            }
        });
        View.OnClickListener openLock = new OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMediaController.this.unlock();
            }
        };
        mOpenLockLeftIV.setOnClickListener(openLock);
        mOpenLockRightIV.setOnClickListener(openLock);
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDragging = false;
                try {
                    if (mPlayer != null) {
                        int state = mPlayer.getState();
                        if (state == VideoPlayerView.STATE_PLAYBACK_COMPLETED) {
                            if (restartPlayEvent != null) {
                                restartPlayEvent.accept(Long.valueOf(seekBar.getProgress()));
                            }
                        } else {
                            mPlayer.seekTo(mDuration * seekBar.getProgress() / 1000L);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mPauseButton.setOnClickListener(this);
        mTvPlay.setOnClickListener(this);
        updatePausePlay();
        mHeaderLayout.setOnTouchListener(this);
        mMediaMontrollerControls.setOnTouchListener(this);
    }

    public void inflateMore (@MenuRes int menuRes) {
        mMorePopupMenu.inflate(menuRes);
    }

    public void setOnMoreMenuItemClickListener(@Nullable PopupMenu.OnMenuItemClickListener listener) {
        mMorePopupMenu.setOnMenuItemClickListener(listener);
    }

    public void lock() {
        isLocked = true;
        mHeaderLayout.setVisibility(View.GONE);
        mMediaMontrollerControls.setVisibility(View.GONE);
        mTvPlay.setVisibility(View.GONE);
        mOpenLockLeftIV.setVisibility(View.VISIBLE);
        mOpenLockRightIV.setVisibility(View.VISIBLE);
    }

    public void unlock() {
        isLocked = false;
        mHeaderLayout.setVisibility(View.VISIBLE);
        mMediaMontrollerControls.setVisibility(View.VISIBLE);
        mTvPlay.setVisibility(View.VISIBLE);
        mOpenLockLeftIV.setVisibility(View.GONE);
        mOpenLockRightIV.setVisibility(View.GONE);
    }

    @Override
    public void show() {
        this.setVisibility(View.VISIBLE);
        if (visibilityChangedEvent != null)
            visibilityChangedEvent.accept(true);
    }

    @Override
    public void show(int timeout) {
        this.setVisibility(View.VISIBLE);
        if (visibilityChangedEvent != null)
            visibilityChangedEvent.accept(true);
    }

    @Override
    public void hide() {
        this.setVisibility(View.GONE);
        if (visibilityChangedEvent != null)
            visibilityChangedEvent.accept(false);
    }

    @Override
    public boolean isShowing() {
        return this.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setMediaPlayer(MediaPlayerListener player) {
        mPlayer = player;
    }

    @Override
    public void setAnchorView(View v) {

    }

    @Override
    public void setTitle(String title) {
        mTitleTV.setText(title);
    }

    public void setDanmakuSwitchEvent(Fun2<Boolean> danmakuSwitchEvent) {
        this.danmakuSwitchEvent = danmakuSwitchEvent;
    }

    public void setVideoBackEvent(Fun videoBackEvent) {
        this.videoBackEvent = videoBackEvent;
    }

    public void setQualityEvent(View.OnClickListener qualityEvent) {
        this.qualityEvent = qualityEvent;
    }

    public void setVisibilityChangedEvent(Fun2<Boolean> visibilityChangedEvent) {
        this.visibilityChangedEvent = visibilityChangedEvent;
    }

    public void setDanmakuShow(boolean show) {
        if (show) {
            mDanmakuSwitchIV.setImageResource(R.drawable.bili_player_danmaku_is_open);
            mDanmakuSwitchTV.setText("弹幕开");
        } else {
            mDanmakuSwitchIV.setImageResource(R.drawable.bili_player_danmaku_is_closed);
            mDanmakuSwitchTV.setText("弹幕关");
        }
        mDanmakuShow = show;
    }

    @Override
    public void onClick(View view) {
        if (mPlayer == null)
            return;
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mPauseButton.setImageResource(R.drawable.bili_player_play_can_play);
            mTvPlay.setImageResource(R.drawable.ic_tv_play);
        } else {
            if (mPlayer.getState() == VideoPlayerView.STATE_PLAYBACK_COMPLETED) {
                if (restartPlayEvent != null) {
                    restartPlayEvent.accept(0L);
                }
            } else {
                mPlayer.start();
                mPauseButton.setImageResource(R.drawable.bili_player_play_can_pause);
                mTvPlay.setImageResource(R.drawable.ic_tv_stop);
            }
        }
    }

    /**
     * 设置播放进度
     */
    public long setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            long percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress((int) (percent * 10));
        }
        mDuration = duration;
        mEndTime.setText(generateTime(mDuration));
        mCurrentTime.setText(generateTime(position));
        return position;
    }

    public void setProgress(long position) {
        long duration = mPlayer.getDuration();
        long pos = 1000L * position / duration;
        mProgress.setProgress((int) pos);
        mCurrentTime.setText(generateTime(position));
    }

    public void updatePausePlay() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPauseButton.setImageResource(R.drawable.bili_player_play_can_pause);
                mTvPlay.setImageResource(R.drawable.ic_tv_stop);
            } else {
                mPauseButton.setImageResource(R.drawable.bili_player_play_can_play);
                mTvPlay.setImageResource(R.drawable.ic_tv_play);
            }
        }
    }

    public void setDragging(boolean dragging) {
        this.mDragging = dragging;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public static String generateTime(long position) {
        int totalSeconds = (int) (position / 1000.0 + 0.5);
        int seconds = totalSeconds % 60;
        int minutes = totalSeconds / 60 % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ?
                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds) :
                String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("onTouch", "onTouch");
        return true;
    }

    public void setRestartPlayEvent(Fun2<Long> restartPlayEvent) {
        this.restartPlayEvent = restartPlayEvent;
    }

    @FunctionalInterface
    public interface Fun {
        void accept();
    }

    @FunctionalInterface
    public interface Fun2<T> {
        void accept(T b);
    }

    public void setHeaderLayoutPadding(int left, int top, int right, int bottom) {
        mHeaderLayout.setPadding(left, top, right, bottom);
    }

    public void updateColor(int color){
        Drawable draw = getContext().getDrawable(R.drawable.layer_progress);
        Rect bounds = mProgress.getProgressDrawable().getBounds();
        mProgress.setProgressDrawable(draw);
        mProgress.getProgressDrawable().setBounds(bounds);
        mProgress.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void setDisplayCutout(DisplayCutout displayCutout) {
        if (displayCutout == null || displayCutout.getBoundingRects() == null) {
            return;
        }
        int left = displayCutout.getSafeInsetLeft();
        int right = displayCutout.getSafeInsetRight();
        int bottom = displayCutout.getSafeInsetBottom();
        mHeaderLayout.setPadding(
                left,
                mHeaderLayout.getPaddingTop(),
                right,
                0
        );
        mMediaMontrollerControls.setPadding(
                left,
                mMediaMontrollerControls.getPaddingTop(),
                right,
                bottom
        );
        RelativeLayout.LayoutParams openLockLeftIVLayoutParams = ((RelativeLayout.LayoutParams)mOpenLockLeftIV.getLayoutParams());
        openLockLeftIVLayoutParams.setMargins(
                left,
                openLockLeftIVLayoutParams.topMargin,
                openLockLeftIVLayoutParams.rightMargin,
                openLockLeftIVLayoutParams.bottomMargin
        );
        RelativeLayout.LayoutParams openLockRightIVParams = ((RelativeLayout.LayoutParams)mOpenLockRightIV.getLayoutParams());
        openLockRightIVParams.setMargins(
                left,
                openLockRightIVParams.topMargin,
                openLockRightIVParams.rightMargin,
                openLockRightIVParams.bottomMargin
        );
    }

}
