package cn.a10miaomiao.dragging_sheet;


import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.LinearLayout;

public class DispatchLayout extends LinearLayout {
    private int mLastY;
    private int mClickY;
    private int dy;
    int firstClickY = 0;
    int touchSlop = 8;
    int scrollY;
    VelocityTracker mVelocityTracker;
    YouTuDraggingView parentView;

    public DispatchLayout(Context context) {
        this(context, null);
    }

    public DispatchLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DispatchLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setParentView(YouTuDraggingView parentView) {
        this.parentView = parentView;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (parentView.getNowStateScale() == 1f && parentView.isLandscape()) {
            return super.dispatchTouchEvent(ev);
        }

        int y = (int) ev.getRawY();
        if (firstClickY == 0) {
            firstClickY = y;
        }
        if (firstClickY >= parentView.mTopOriginalHeight && firstClickY <= this.getMeasuredHeight()) {
            firstClickY = 0;
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mClickY = y;
                mVelocityTracker = VelocityTracker.obtain();
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(ev);
                dy = y - mLastY;
                scrollY = Math.abs(scrollY) + Math.abs(dy);
                //drag
                if (Math.abs(y - mClickY) > touchSlop) {
                    int newMarY = parentView.mBackgroundViewWrapper.getMarginTop() + dy;
                    if (newMarY > parentView.mRangeScrollY && parentView.nowStateScale == parentView.MIN_RATIO_HEIGHT &&
                            parentView.mBackgroundViewWrapper.getMarginTop() >= (int) parentView.mRangeScrollY) {
                        //如果是在最小高度  下滑
                        parentView.updateDismissView(newMarY);
                    } else {
                        parentView.updateVideoView(newMarY);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                firstClickY = 0;
                if (Math.abs(scrollY) > touchSlop) {
                    scrollY = 0;
                    mVelocityTracker.computeCurrentVelocity(100);
                    float yVelocity = Math.abs(mVelocityTracker.getYVelocity());
                    mVelocityTracker.clear();
                    mVelocityTracker.recycle();
                    parentView.confirmState(yVelocity, dy);
                    //直接拦截  不分发事件 不让点击
                    return true;
                }
                scrollY = 0;

        }
        mLastY = y;
        return super.dispatchTouchEvent(ev);
    }


}
