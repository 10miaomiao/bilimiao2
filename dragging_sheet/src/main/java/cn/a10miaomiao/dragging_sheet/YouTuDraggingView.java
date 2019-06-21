package cn.a10miaomiao.dragging_sheet;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by moyokoo on 2018/10/11.
 * 1. Activity must set android:configChanges="orientation|screenSize"
 * 2. DispatchLayout must set background
 * 3. YouTuDraggingView must set match_parent
 */

public class YouTuDraggingView extends RelativeLayout implements View.OnClickListener {

    static final int STATUS_MAX = 1;
    static final int STATUS_MIN = 0;
    static final int STATUS_DRAG = 2;

    public interface Callback {
        void onVideoViewHide();

        void videoSize(int width, int height);

        void onIconClick(IconType iconType);

        void status(int status);
    }


    public enum IconType {
        PAUSE,
        PLAY,
        CLOSE
    }

    Callback mCallback;
    IconType statusType = IconType.PLAY;

    // 可拖动的view 和下方的详情View
    DispatchLayout mBackgroundView;
    View mDetailView;
    View mTopView;
    Activity mActivity;

    MarginViewWrapper mBackgroundViewWrapper;
    //头部的视频View
    MarginViewWrapper mTopViewWrapper;
//    MarginViewWrapper titleWrapper;
//    MarginViewWrapper pauseIvWrapper;
//    MarginViewWrapper closeIvWrapper;

    //滑动区间,取值为是topView最小化时距离屏幕顶端的高度
    float mRangeScrollY;
    float mRangeNodeScrollY;

    //当前的比例
    float nowStateScale;

    //节点最小的缩放比例
    float MIN_RATIO_HEIGHT_NODE = 0.45f;
    //最小的缩放比例
    static float MIN_RATIO_HEIGHT = 0.35f;
    float MIN_RATIO_WIDTH = 0.95f;
    //播放器比例
    static final float VIDEO_RATIO = 16f / 9f;
    int finalVideoLeftRightOffset;

    //video布局的原始宽度
    float mTopViewOriginalWidth;
    //video布局的原始高度
    float mTopOriginalHeight;
    float mBackgroundOriginalHeight;

    //底部的距离
    float bottomHeight = DensityUtil.dip2px(getContext(), 20);
    long rangeDuration = 350;
    long dismissDuration = 100;

    //View所在的activity是否全屏
    boolean activityFullscreen = false;
    OrientationEventListener mOrientationListener;
    private int isRotate;//0 代表方向锁定，1 代表没方向锁定
    private long orientationListenerDelayTime = 0;
    boolean isPortraitToLandscape = false;
    boolean isLandscapeToPortrait = false;

    public YouTuDraggingView(Context context) {
        this(context, null);
    }

    public YouTuDraggingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YouTuDraggingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        mActivity = getActivityFromView(this);
        activityFullscreen = (mActivity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;

        addView(LayoutInflater.from(getContext()).inflate(R.layout.youtu_dispatch, null), 0);
        mBackgroundView = findViewById(R.id.backgroundView);
        mTopView = findViewById(R.id.videoView);
        View videoParentLayout = findViewById(R.id.videoParentLayout);
        mDetailView = findViewById(R.id.detailView);

        mBackgroundView.setParentView(this);
        setBackgroundColor(Color.BLACK);
        mBackgroundView.setOnClickListener(this);
        videoParentLayout.setOnClickListener(this);


        //初始化包装类
        mBackgroundViewWrapper = new MarginViewWrapper(mBackgroundView);
        mTopViewWrapper = new MarginViewWrapper(mTopView);

//        titleWrapper = new MarginViewWrapper(titleLayout);
//        pauseIvWrapper = new MarginViewWrapper(pauseLayout);
//        closeIvWrapper = new MarginViewWrapper(closeLayout);

        initData();
    }


    private void initOrientationListener() {
        if (mOrientationListener != null) {
            return;
        }
        mOrientationListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int orientation) {
                try {
                    //获取是否开启系统
                    isRotate = Settings.System.getInt(getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
                if (isRotate == 0) return;
                if ((orientation >= 300 || orientation <= 30) && System.currentTimeMillis() - orientationListenerDelayTime > 1000) {
                    if (isLandscapeToPortrait) {
                        isLandscapeToPortrait = false;
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                    orientationListenerDelayTime = System.currentTimeMillis();
                } else if (orientation >= 260 && orientation <= 280
                        && System.currentTimeMillis() - orientationListenerDelayTime > 1000) {
                    if (isPortraitToLandscape) {
                        isPortraitToLandscape = false;
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                    orientationListenerDelayTime = System.currentTimeMillis();
                } else if (orientation >= 70 && orientation <= 90
                        && System.currentTimeMillis() - orientationListenerDelayTime > 1000) {
                    if (isPortraitToLandscape) {
                        isPortraitToLandscape = false;
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                    orientationListenerDelayTime = System.currentTimeMillis();
                }
            }
        };
        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        } else {
            mOrientationListener.disable();
        }

    }

    private void initData() {
        initOrientationListener();
        //当前缩放比例
        nowStateScale = 1f;

        if (isLandscape()) {
            mTopViewOriginalWidth = DensityUtil.getScreenW(getContext());
            if (activityFullscreen) {
                mTopOriginalHeight = DensityUtil.getScreenH(getContext());
            } else {
                mTopOriginalHeight = DensityUtil.getScreenH(getContext()) - DensityUtil.getStatusBarH(getContext());
            }
            MIN_RATIO_HEIGHT_NODE = 0.35f;
            MIN_RATIO_HEIGHT = 0.25f;
            mDetailView.setVisibility(View.GONE);
        } else {
            mTopViewOriginalWidth = mBackgroundView.getContext().getResources().getDisplayMetrics().widthPixels;
            mTopOriginalHeight = (mTopViewOriginalWidth / VIDEO_RATIO);
            mDetailView.setVisibility(View.VISIBLE);
        }


        mTopViewWrapper.setHeight(mTopOriginalHeight);
        mTopViewWrapper.setWidth(mTopViewOriginalWidth);

        if (MIN_RATIO_HEIGHT_NODE < MIN_RATIO_HEIGHT) {
            throw new RuntimeException("MIN_RATIO_HEIGHT_NODE can't smaller than MIN_RATIO_HEIGHT_NODE");
        }
    }

    public Boolean isLandscape() {
        return getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        resetRangeAndSize();
    }


    public void resetRangeAndSize() {
        mTopViewOriginalWidth = DensityUtil.getScreenW(getContext());
        if (isLandscape()) {
            mTopOriginalHeight = activityFullscreen ? DensityUtil.getScreenH(getContext()) : DensityUtil.getScreenOriginH(getContext());
            MIN_RATIO_HEIGHT_NODE = 0.35f;
            MIN_RATIO_HEIGHT = 0.25f;
            mDetailView.setVisibility(View.GONE);
        } else {
            mTopOriginalHeight = (mTopViewOriginalWidth / VIDEO_RATIO);
            MIN_RATIO_HEIGHT_NODE = 0.45f;
            MIN_RATIO_HEIGHT = 0.35f;
        }
        float height = activityFullscreen ? DensityUtil.getScreenH(getContext()) : DensityUtil.getScreenOriginH(getContext());
        mRangeScrollY = height - MIN_RATIO_HEIGHT * mTopOriginalHeight - bottomHeight;
        mRangeNodeScrollY = height - MIN_RATIO_HEIGHT_NODE * mTopOriginalHeight - bottomHeight;
        mBackgroundOriginalHeight = height;
        finalVideoLeftRightOffset = (int) ((mTopViewOriginalWidth - mTopViewOriginalWidth * MIN_RATIO_WIDTH) / 2);
    }

    void notifyStatus() {
        if (statusType == IconType.PLAY) {
            statusType = IconType.PAUSE;
//            pauseIv.setBackgroundResource(R.drawable.play);
            mCallback.onIconClick(IconType.PAUSE);
        } else {
            statusType = IconType.PLAY;
//            pauseIv.setBackgroundResource(R.drawable.pause);
            mCallback.onIconClick(IconType.PLAY);
        }
    }

    void changeStatus(IconType iconType) {
        if (iconType == IconType.PLAY) {
            statusType = IconType.PAUSE;
//            pauseIv.setBackgroundResource(R.drawable.play);
        } else if (iconType == IconType.PAUSE) {
            statusType = IconType.PLAY;
//            pauseIv.setBackgroundResource(R.drawable.pause);
        }
    }

    void updateDismissView(int m) {
//        ViewGroup.LayoutParams params = getLayoutParams();
//        params.width = -1;
//        params.height = -1;
//        setLayoutParams(params);
        float fullY = mRangeScrollY + mTopOriginalHeight * MIN_RATIO_HEIGHT;
        float offset = mRangeScrollY;
        if (m < fullY && m > mRangeScrollY) {
            offset = mRangeScrollY + (m - mRangeScrollY);
        }
        if (m >= fullY) {
            offset = fullY;
        }
        if (m < mRangeScrollY) {
            offset = mRangeScrollY;
        }
        float alphaPercent = (m - mRangeScrollY) / (fullY - mRangeScrollY);
        mBackgroundViewWrapper.setMarginTop(Math.round(offset));
        mBackgroundViewWrapper.setMarginBottom(Math.round(bottomHeight - (m - mRangeScrollY)));
        mBackgroundView.setAlpha((1 - alphaPercent));
    }

    void updateVideoView(int m) {
        if (mBackgroundViewWrapper.getMarginTop() > 0) {
            mCallback.status(STATUS_DRAG);
        }
        //如果当前状态是最小化，先把我们的的布局宽高设置为MATCH_PARENT
//        if (nowStateScale == MIN_RATIO_HEIGHT) {
//            ViewGroup.LayoutParams params = getLayoutParams();
//            params.width = -1;
//            params.height = -1;
//            setLayoutParams(params);
//        }

        //marginTop的值最大为allScrollY，最小为0
        if (m > mRangeScrollY)
            m = (int) mRangeScrollY;
        if (m < 0)
            m = 0;

        //视频View高度的百分比100% - 0%
        float marginPercent = (mRangeScrollY - m) / mRangeScrollY;
        float nodeMarginPercent = (mRangeNodeScrollY - m) / mRangeNodeScrollY;

        float videoNodeWidthPercent = MIN_RATIO_WIDTH + (1f - MIN_RATIO_WIDTH) * nodeMarginPercent;
        float videoNodeHeightPercent = MIN_RATIO_HEIGHT_NODE + (1f - MIN_RATIO_HEIGHT_NODE) * nodeMarginPercent;
        float detailPercent = m / mRangeNodeScrollY;


        int videoLeftRightOffset = (int) ((mTopViewOriginalWidth - mTopViewOriginalWidth * videoNodeWidthPercent) / 2);
        int detailBottomOffset = Math.round(bottomHeight * detailPercent);
        //不能超过底部间距
        if (detailBottomOffset >= bottomHeight) {
            detailBottomOffset = Math.round(bottomHeight);
        }
        if (m >= mRangeNodeScrollY) {
            mDetailView.setVisibility(View.GONE);
            float alphaPercent = (m - mRangeNodeScrollY) / (mRangeScrollY - mRangeNodeScrollY);
            //背景的移动高度
            float offHeight = Math.round(mBackgroundOriginalHeight * MIN_RATIO_HEIGHT_NODE - (m - mRangeNodeScrollY));
            //视频View的移动高度
            float offHeight2 = Math.round(mTopOriginalHeight * MIN_RATIO_HEIGHT_NODE - (m - mRangeNodeScrollY));

            mBackgroundViewWrapper.setMarginTop(m);
            mBackgroundViewWrapper.setHeight(-1);
            float videoRightOffset = (m - mRangeNodeScrollY) / (mRangeScrollY - mRangeNodeScrollY) * mTopViewOriginalWidth * MIN_RATIO_WIDTH * 2 / 3;
            float topViewWidth = Math.round(mTopViewOriginalWidth * MIN_RATIO_WIDTH - videoRightOffset);

            mTopViewWrapper.setHeight(offHeight2);
            mTopViewWrapper.setWidth(topViewWidth);

            float backgroundViewHeight = (mBackgroundOriginalHeight - mRangeNodeScrollY) * alphaPercent;

            setPadding(0, Math.round(backgroundViewHeight), 0, 0);

            //头部达到最小宽度时,滑动的宽度
            float pieceWidth = mTopViewOriginalWidth * MIN_RATIO_WIDTH / 3;
            float minWidthOffset = topViewWidth - pieceWidth;
            float imageLayoutWidth = pieceWidth / 2;

            //最小布局时的控件位置
            float titleWidth = pieceWidth - minWidthOffset;
            float pauseWidth = imageLayoutWidth;
            float closeWidth = imageLayoutWidth;
            float pauseLeftOffset = pieceWidth - minWidthOffset;
            float closeLeftOffset = pieceWidth + pieceWidth / 2 - minWidthOffset;
            if (minWidthOffset >= pieceWidth) {
                titleWidth = 0;
                pauseLeftOffset = 0;
                pauseWidth = imageLayoutWidth - minWidthOffset + pieceWidth;
            }
            if (minWidthOffset >= pieceWidth + imageLayoutWidth) {
                pauseWidth = 0;
                closeLeftOffset = 0;
                closeWidth = imageLayoutWidth - minWidthOffset + pieceWidth + imageLayoutWidth;
            }
            if (minWidthOffset >= pieceWidth * 2) {
                closeWidth = 0;
            }

//            titleWrapper.setWidth(Math.round(titleWidth));
//            titleWrapper.setHeight(offHeight2);
//
//            pauseIvWrapper.setWidth(Math.round(pauseWidth));
//            pauseIvWrapper.setHeight(offHeight2);
//            pauseIvWrapper.setMarginLeft(Math.round(pauseLeftOffset));
//
//
//            closeIvWrapper.setWidth(closeWidth);
//            closeIvWrapper.setHeight(offHeight2);
//            closeIvWrapper.setMarginLeft(Math.round(closeLeftOffset));

//            pauseIv.setAlpha(alphaPercent);
//            closeIv.setAlpha(alphaPercent);
//            titleLayout.setAlpha(alphaPercent);
        } else {
            setPadding(0, 0, 0, 0);
            mDetailView.setVisibility(View.VISIBLE);
            float percent = 1 - detailPercent;
            float topViewHeight = mTopOriginalHeight * videoNodeHeightPercent;
            float backgroundViewHeight = (mBackgroundOriginalHeight - topViewHeight) * percent + topViewHeight;
            mBackgroundViewWrapper.setHeight(-1);
            mTopViewWrapper.setWidth(Math.round(mTopViewOriginalWidth * videoNodeWidthPercent));
            mTopViewWrapper.setHeight(Math.round(topViewHeight));
            mBackgroundViewWrapper.setMarginTop(m);
        }
        mBackgroundViewWrapper.setWidth(Math.round(mTopViewOriginalWidth * videoNodeWidthPercent));
        mBackgroundViewWrapper.setMarginRight(videoLeftRightOffset);
        mBackgroundViewWrapper.setMarginLeft(videoLeftRightOffset);
        mBackgroundViewWrapper.setMarginBottom(detailBottomOffset);
        mDetailView.setAlpha(marginPercent);
        this.getBackground().setAlpha((int) (marginPercent * 255 * 0.6f));
        mCallback.videoSize(mTopViewWrapper.getWidth(), mTopViewWrapper.getHeight());
        mBackgroundView.setAlpha(1);
    }

    void dismissView() {
        float fullY = mRangeScrollY + mTopOriginalHeight * MIN_RATIO_HEIGHT;
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(mBackgroundView, "alpha", 1f, 0),
                ObjectAnimator.ofInt(mBackgroundViewWrapper, "marginTop",
                        mBackgroundViewWrapper.getMarginTop(), Math.round(fullY)));
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(INVISIBLE);
                mBackgroundView.setAlpha(1f);
            }
        });
        set.setDuration((long) (dismissDuration * mBackgroundViewWrapper.getMarginTop() / fullY)).start();

        if (mCallback != null)
            mCallback.onVideoViewHide();
    }

    void confirmState(float v, int dy) {
        if (mBackgroundViewWrapper.getMarginTop() >= mRangeScrollY) {
            if (v > 15) {
                dismissView();
            } else {
                goMin(true);
            }

        } else {
            //dy用于判断是否反方向滑动了
            //如果手指抬起时宽度达到一定值 或者 速度达到一定值 则改变状态
            if (nowStateScale == 1f) {
                if (mTopViewOriginalWidth - mBackgroundView.getWidth() >= mTopViewOriginalWidth * (1 - MIN_RATIO_WIDTH) / 3 || (v > 5 && dy > 0)) {
                    goMin();
                } else {
                    goMax();
                }
            } else {
                if (mTopViewOriginalWidth - mBackgroundView.getWidth() <= 2 * mTopViewOriginalWidth * (1 - MIN_RATIO_WIDTH) / 3 || (v > 5 && dy < 0)) {
                    goMax();
                } else {
                    goMin();
                }
            }
        }
    }

    public void fullScreenGoMin() {
        if (isLandscape()) {
            nowStateScale = MIN_RATIO_HEIGHT;
            if (!activityFullscreen) {
                mActivity.getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            resetRangeAndSize();
            mBackgroundViewWrapper.setMarginTop(Math.round(mRangeScrollY));
            updateVideoView(Math.round(mRangeScrollY));
        } else {
            goMin();
        }
    }

    public void goLandscapeMin() {
        mCallback.status(STATUS_MIN);
        nowStateScale = MIN_RATIO_HEIGHT;
        resetRangeAndSize();
        mBackgroundViewWrapper.setMarginTop(Math.round(mRangeScrollY));
        updateVideoView(Math.round(mRangeScrollY));
    }

    public void goPortraitMin() {
        nowStateScale = MIN_RATIO_HEIGHT;
        mCallback.status(STATUS_MIN);
        resetRangeAndSize();
        mBackgroundViewWrapper.setMarginTop(Math.round(mRangeScrollY));
        updateVideoView(Math.round(mRangeScrollY));
    }

    public void goPortraitMax() {
        if (!activityFullscreen) {
            mActivity.getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        mCallback.status(STATUS_MAX);
        nowStateScale = 1f;
        resetRangeAndSize();
        mBackgroundViewWrapper.setMarginTop(0);
        updateVideoView(0);
    }

    public void goFullScreen() {
        if (!activityFullscreen) {
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        mCallback.status(STATUS_MAX);

        mBackgroundViewWrapper.setWidth(DensityUtil.getScreenW(getContext()));
        mBackgroundViewWrapper.setHeight(DensityUtil.getScreenH(getContext()));
        mTopViewWrapper.setWidth(DensityUtil.getScreenW(getContext()));
        mTopViewWrapper.setHeight(DensityUtil.getScreenH(getContext()));

        mCallback.videoSize(mTopViewWrapper.getWidth(), mTopViewWrapper.getHeight());
    }

    public void goMax() {
        if (nowStateScale == MIN_RATIO_HEIGHT) {
//            ViewGroup.LayoutParams params = getLayoutParams();
//            params.width = -1;
//            params.height = -1;
//            setLayoutParams(params);
        }


        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mBackgroundViewWrapper.getMarginTop(), 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                updateVideoView((int) value);
                if (value == 0) {
                    if (isLandscape()) {
                        goFullScreen();
                    }
                    mCallback.status(STATUS_MAX);
                }
            }
        });
        valueAnimator.setDuration((long) (mBackgroundViewWrapper.getMarginTop() / mRangeScrollY * rangeDuration)).start();

        nowStateScale = 1.0f;
    }


    public void goMin() {
        goMin(false);
    }

    public void goMin(final boolean isDismissToMin) {
        nowStateScale = MIN_RATIO_HEIGHT;
        final float fullTop = Math.abs(mBackgroundViewWrapper.getMarginTop() - mRangeScrollY);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mBackgroundViewWrapper.getMarginTop(), mRangeScrollY);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mBackgroundView.setAlpha(isDismissToMin ? (value / fullTop) : 1);
                updateVideoView((int) value);
                if (value == mRangeScrollY) {
//                    ViewGroup.LayoutParams p = getLayoutParams();
//                    p.width = -2;
//                    p.height = -2;
//                    setLayoutParams(p);
                    mCallback.status(STATUS_MIN);
                }
            }
        });
        valueAnimator.setDuration((long) (Math.abs((1 - mBackgroundViewWrapper.getMarginTop() / mRangeScrollY)) * rangeDuration)).start();
    }


    //获取当前状态
    public float getNowStateScale() {
        return nowStateScale;
    }

    public boolean isMin() {
        return nowStateScale == MIN_RATIO_HEIGHT;
    }

    public boolean isMax() {
        return nowStateScale == 1f;
    }

    public void show() {
        setVisibility(VISIBLE);
        statusType = IconType.PLAY;
//        pauseIv.setBackgroundResource(R.drawable.pause);
        // 默认从最底部开始变换到顶部
        mBackgroundViewWrapper.setMarginTop((int) mRangeScrollY);
        goMax();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    class MarginViewWrapper {
        private ViewGroup.MarginLayoutParams params;
        private View viewWrapper;

        MarginViewWrapper(View view) {
            this.viewWrapper = view;
            params = (ViewGroup.MarginLayoutParams) viewWrapper.getLayoutParams();
            if (params instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) params).gravity = Gravity.START;
            }
        }


        int getWidth() {
            return params.width < 0 ? (int) mTopViewOriginalWidth : params.width;
        }

        int getHeight() {
            return params.height < 0 ? (int) mTopOriginalHeight : params.height;
        }

        void setWidth(float width) {
            if (width == mTopViewOriginalWidth) {
                params.width = -1;
                params.setMargins(0, 0, 0, 0);
            } else
                params.width = (int) width;

            viewWrapper.setLayoutParams(params);
        }

        void setHeight(float height) {
            params.height = (int) height;
            viewWrapper.setLayoutParams(params);
        }

        void setMarginTop(int m) {
            params.topMargin = m;
            viewWrapper.setLayoutParams(params);
        }

        void setMarginBottom(int m) {
            params.bottomMargin = m;
            viewWrapper.setLayoutParams(params);
        }

        int getMarginTop() {
            return params.topMargin;
        }

        void setMarginRight(int mr) {
            params.rightMargin = mr;
            viewWrapper.setLayoutParams(params);
        }

        void setMarginLeft(int mr) {
            params.leftMargin = mr;
            viewWrapper.setLayoutParams(params);
        }

        int getMarginRight() {
            return params.rightMargin;
        }

        int getMarginLeft() {
            return params.leftMargin;
        }

        int getMarginBottom() {
            return params.bottomMargin;
        }
    }

    public static Activity getActivityFromView(View view) {
        if (null != view) {
            Context context = view.getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    return (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.videoParentLayout:
//                if (nowStateScale == MIN_RATIO_HEIGHT) {
//                    goMax();
//                }
//                break;
//            case R.id.downIv:
//                fullScreenGoMin();
//                break;
//            case R.id.pauseLayout:
//                if (MediaPlayerManager.instance().getPlayerState() == MediaPlayerManager.PlayerState.ERROR ||
//                        MediaPlayerManager.instance().getPlayerState() == MediaPlayerManager.PlayerState.PREPARING) {
//                    return;
//                }
//                notifyStatus();
//                break;
//            case R.id.closeLayout:
//                dismissView();
//                mCallback.onIconClick(YouTuDraggingView.IconType.CLOSE);
//                break;
//            case R.id.statusIv:
//                notifyStatus();
//                break;
//        }
    }


    void fullScreenChange() {
        if (isLandscape()) {
            isLandscapeToPortrait = true;
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            isPortraitToLandscape = true;
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    boolean handleKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getNowStateScale() == 1f) {
            if (isPortraitToLandscape) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                goPortraitMax();
            } else if (isLandscape()) {
                fullScreenGoMin();
            } else {
                goMin();
            }
            return true;
        }
        return false;
    }

    public void e(String msg) {
        Log.e("Youtu", msg);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //当前屏幕为横屏
            if (getNowStateScale() == 1f) {
                goFullScreen();
            } else {
                goLandscapeMin();
            }

        } else {
            //当前屏幕为竖屏
            if (getNowStateScale() == 1f) {
                goPortraitMax();
            } else {
                goPortraitMin();
            }
        }
    }
}