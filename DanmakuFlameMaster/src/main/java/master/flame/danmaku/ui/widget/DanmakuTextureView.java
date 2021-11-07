/*
 * Copyright (C) 2013 Chen Hui <calmer91@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package master.flame.danmaku.ui.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import java.util.LinkedList;
import java.util.Locale;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.DrawHandler.Callback;
import master.flame.danmaku.controller.DrawHelper;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.controller.IDanmakuViewController;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.renderer.IRenderer.RenderingState;
import master.flame.danmaku.danmaku.util.SystemClock;

/**
 * DanmakuTextureView  目前使用lockCanvas, 没有使用opengl硬件加速
 * @author ch
 *
 */
@SuppressLint("NewApi")
public class DanmakuTextureView extends TextureView implements IDanmakuView, IDanmakuViewController,
        TextureView.SurfaceTextureListener {

    public static final String TAG = "DanmakuTextureView";

    private Callback mCallback;

    private HandlerThread mHandlerThread;

    private DrawHandler handler;

    private boolean isSurfaceCreated;

    private boolean mEnableDanmakuDrwaingCache = true;

	private OnDanmakuClickListener mOnDanmakuClickListener;

    private float mXOff;

    private float mYOff;

    private DanmakuTouchHelper mTouchHelper;

    private boolean mShowFps;

    private boolean mDanmakuVisible = true;
    
    protected int mDrawingThreadType = THREAD_TYPE_NORMAL_PRIORITY;

    public DanmakuTextureView(Context context) {
        super(context);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void init() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setOpaque(false);
        setWillNotCacheDrawing(true);
        setDrawingCacheEnabled(false);
        setWillNotDraw(true);
        setSurfaceTextureListener(this);
        DrawHelper.useDrawColorToClearCanvas(true, true);
		mTouchHelper = DanmakuTouchHelper.instance(this);    
	}

    public DanmakuTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DanmakuTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void addDanmaku(BaseDanmaku item) {
        if (handler != null) {
            handler.addDanmaku(item);
        }
    }

    @Override
    public void invalidateDanmaku(BaseDanmaku item, boolean remeasure) {
        if (handler != null) {
            handler.invalidateDanmaku(item, remeasure);
        }
    }

    @Override
    public void removeAllDanmakus(boolean isClearDanmakusOnScreen) {
        if (handler != null) {
            handler.removeAllDanmakus(isClearDanmakusOnScreen);
        }
    }
    
    @Override
    public void removeAllLiveDanmakus() {
        if (handler != null) {
            handler.removeAllLiveDanmakus();
        }
    }

    @Override
    public IDanmakus getCurrentVisibleDanmakus() {
        if (handler != null) {
            return handler.getCurrentVisibleDanmakus();
        }

        return null;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
        if (handler != null) {
            handler.setCallback(callback);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        isSurfaceCreated = true;
    }

    @Override
    public synchronized boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        isSurfaceCreated = false;
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (handler != null) {
            handler.notifyDispSizeChanged(width, height);
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void release() {
        stop();
        if(mDrawTimes!= null) mDrawTimes.clear();
    }

    @Override
    public void stop() {
        stopDraw();
    }

    private synchronized void stopDraw() {
        if (handler != null) {
            handler.quit();
            handler = null;
        }
        HandlerThread handlerThread = this.mHandlerThread;
        mHandlerThread = null;
        if (handlerThread != null) {
            try {
                handlerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            handlerThread.quit();
        }
    }
    
    protected synchronized Looper getLooper(int type){
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        
        int priority;
        switch (type) {
            case THREAD_TYPE_MAIN_THREAD:
                return Looper.getMainLooper();
            case THREAD_TYPE_HIGH_PRIORITY:
                priority = android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY;
                break;
            case THREAD_TYPE_LOW_PRIORITY:
                priority = android.os.Process.THREAD_PRIORITY_LOWEST;
                break;
            case THREAD_TYPE_NORMAL_PRIORITY:
            default:
                priority = android.os.Process.THREAD_PRIORITY_DEFAULT;
                break;
        }
        String threadName = "DFM Handler Thread #"+priority;
        mHandlerThread = new HandlerThread(threadName, priority);
        mHandlerThread.start();
        return mHandlerThread.getLooper();
    }

    private void prepare() {
        if (handler == null)
            handler = new DrawHandler(getLooper(mDrawingThreadType), this, mDanmakuVisible);
    }

    @Override
    public void prepare(BaseDanmakuParser parser, DanmakuContext config) {
        prepare();
        handler.setConfig(config);
        handler.setParser(parser);
        handler.setCallback(mCallback);
        handler.prepare();
    }

    @Override
    public boolean isPrepared() {
        return handler != null && handler.isPrepared();
    }

    @Override
    public DanmakuContext getConfig() {
        if (handler == null) {
            return null;
        }
        return handler.getConfig();
    }

    @Override
    public void showFPS(boolean show) {
        mShowFps = show;
    }
    private static final int MAX_RECORD_SIZE = 50;
    private static final int ONE_SECOND = 1000;
    private LinkedList<Long> mDrawTimes;
    private float fps() {
        long lastTime = SystemClock.uptimeMillis();
        mDrawTimes.addLast(lastTime);
        Long first = mDrawTimes.peekFirst();
        if (first == null) {
            return 0.0f;
        }
        float dtime = lastTime - first;
        int frames = mDrawTimes.size();
        if (frames > MAX_RECORD_SIZE) {
            mDrawTimes.removeFirst();
        }
        return dtime > 0 ? mDrawTimes.size() * ONE_SECOND / dtime : 0.0f;
    }
    
    @Override
    public synchronized long drawDanmakus() {
        if (!isSurfaceCreated)
            return 0;
        long stime = SystemClock.uptimeMillis();
        if (!isShown())
            return -1;
        long dtime = 0;
        Canvas canvas = lockCanvas();
        if (canvas != null) {
            if (handler != null) {
                RenderingState rs = handler.draw(canvas);
                if (mShowFps) {
                    if (mDrawTimes == null)
                        mDrawTimes = new LinkedList<Long>();
                    dtime = SystemClock.uptimeMillis() - stime;
                    String fps = String.format(Locale.getDefault(),
                            "fps %.2f,time:%d s,cache:%d,miss:%d", fps(), getCurrentTime() / 1000,
                            rs.cacheHitCount, rs.cacheMissCount);
                    DrawHelper.drawFPS(canvas, fps);
                }
            }
            if (isSurfaceCreated)
                unlockCanvasAndPost(canvas);
        }
        dtime = SystemClock.uptimeMillis() - stime;
        return dtime;
    }

    public void toggle() {
        if (isSurfaceCreated) {
            if (handler == null)
                start();
            else if (handler.isStop()) {
                resume();
            } else
                pause();
        }
    }

    @Override
    public void pause() {
        if (handler != null)
            handler.pause();
    }

    @Override
    public void resume() {
        if (handler != null && handler.isPrepared())
            handler.resume();
        else if (handler == null) {
            restart();
        }
    }
    
    @Override
    public boolean isPaused() {
        if(handler != null) {
            return handler.isStop();
        }
        return false;
    }

    public void restart() {
        stop();
        start();
    }

    @Override
    public void start() {
        start(0);
    }

    @Override
    public void start(long postion) {
        if (handler == null) {
            prepare();
        } else {
            handler.removeCallbacksAndMessages(null);
        }
        handler.obtainMessage(DrawHandler.START, postion).sendToTarget();
    }

	@Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isEventConsumed = mTouchHelper.onTouchEvent(event);
        if (!isEventConsumed) {
            return super.onTouchEvent(event);
        }
        return isEventConsumed;
    }

    public void seekTo(Long ms) {
        if (handler != null) {
            handler.seekTo(ms);
        }
    }

    public void enableDanmakuDrawingCache(boolean enable) {
        mEnableDanmakuDrwaingCache = enable;
    }

    @Override
    public boolean isDanmakuDrawingCacheEnabled() {
        return mEnableDanmakuDrwaingCache;
    }

    @Override
    public boolean isViewReady() {
        return isSurfaceCreated;
    }

    @Override
    public int getViewWidth() {
        return super.getWidth();
    }

    @Override
    public int getViewHeight() {
        return super.getHeight();
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void show() {
        showAndResumeDrawTask(null);
    }
    
    @Override
    public void showAndResumeDrawTask(Long position) {
        mDanmakuVisible = true;
        if (handler == null) {
            return;
        }
        handler.showDanmakus(position);
    }

    @Override
    public void hide() {
        mDanmakuVisible = false;
        if (handler == null) {
            return;
        }
        handler.hideDanmakus(false);
    }
    
    @Override
    public long hideAndPauseDrawTask() {
        mDanmakuVisible = false;
        if (handler == null) {
            return 0;
        }
        return handler.hideDanmakus(true);
    }

    @Override
    public void setOnDanmakuClickListener(OnDanmakuClickListener listener) {
        mOnDanmakuClickListener = listener;
    }

    @Override
    public void setOnDanmakuClickListener(OnDanmakuClickListener listener, float xOff, float yOff) {
        mOnDanmakuClickListener = listener;
        mXOff = xOff;
        mYOff = yOff;
    }

    @Override
    public OnDanmakuClickListener getOnDanmakuClickListener() {
        return mOnDanmakuClickListener;
    }

    @Override
    public float getXOff() {
        return mXOff;
    }

    @Override
    public float getYOff() {
        return mYOff;
    }

    @Override
    public void forceRender() {

    }

    @Override
    public synchronized void clear() {
        if (!isViewReady()) {
            return;
        }        
        Canvas canvas = lockCanvas();
        if (canvas != null) {
            DrawHelper.clearCanvas(canvas);
            unlockCanvasAndPost(canvas);
        }

    }

    @Override
    public boolean isShown() {
        return mDanmakuVisible && super.isShown();
    }

    @Override
    public void setDrawingThreadType(int type) {
        mDrawingThreadType = type;
    }

    @Override
    public long getCurrentTime() {
        if (handler != null) {
            return handler.getCurrentTime();
        }
        return 0;
    }

    @Override
    public boolean isHardwareAccelerated() {
        return false;
    }

    @Override
    public void clearDanmakusOnScreen() {
        if (handler != null) {
            handler.clearDanmakusOnScreen();
        }
    }

}
