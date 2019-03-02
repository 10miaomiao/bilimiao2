package com.a10miaomiao.bilimiao.ui.widget.flow;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * Created by panda on 2016/10/26 下午4:32.
 */
public class FlowItemView extends FrameLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};

    private boolean mChecked = false;

    public FlowItemView(Context context) {
        super(context);
    }

    public FlowItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (mChecked) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}