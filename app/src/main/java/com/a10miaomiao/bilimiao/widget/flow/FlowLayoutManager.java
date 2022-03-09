package com.a10miaomiao.bilimiao.widget.flow;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.util.stream.Stream;


/**
 * Created by Box on 2017/4/2.
 * <p>
 * 流式布局
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class FlowLayoutManager extends RecyclerView.LayoutManager implements
        RecyclerView.SmoothScroller.ScrollVectorProvider {

    final static int LAYOUT_START = -1;
    final static int LAYOUT_END = 1;

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    public static final int INVALID_OFFSET = Integer.MIN_VALUE;

    private static final int DEFAULT_HORIZONTAL_SPACE = 12;
    private static final int DEFAULT_VERTICAL_SPACE = 12;

    @IntDef({HORIZONTAL, VERTICAL})
    public @interface Orientation {
    }

    private SparseArray<Rect> mScrapRects;
    private SparseIntArray mColumnCountOfRow;
    private SparseArray<LayoutParams> mScrapSites;

    private int mOffsetX;
    private int mOffsetY;
    private int mItemCount;
    private int mLeft, mTop, mRight, mBottom;
    private int mWidth, mHeight;

    private int mTotalWidth;
    private int mTotalHeight;
    private int mScrollOffsetX;
    private int mScrollOffsetY;

    private int mVerticalSpace = DEFAULT_VERTICAL_SPACE;
    private int mHorizontalSpace = DEFAULT_HORIZONTAL_SPACE;

    int mPendingScrollPositionOffset = INVALID_OFFSET;

    @Orientation
    private int mOrientation = VERTICAL;

    private RecyclerView.Recycler mRecycler;
    private RecyclerView.State mState;

    public FlowLayoutManager() {
        this(VERTICAL);
    }

    public FlowLayoutManager(@Orientation int orientation) {
        this(orientation, DEFAULT_VERTICAL_SPACE, DEFAULT_HORIZONTAL_SPACE);
    }

    public FlowLayoutManager(@Orientation int orientation, int verticalSpace, int horizontalSpace) {
        setOrientation(orientation);
        setSpace(verticalSpace, horizontalSpace);
        setAutoMeasureEnabled(true);
    }

    public FlowLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Properties properties = getProperties(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(properties.orientation);
        setReverseLayout(properties.reverseLayout);
        setStackFromEnd(properties.stackFromEnd);
        setAutoMeasureEnabled(true);
    }

    public void setSpace(int verticalSpace, int horizontalSpace) {
        if (verticalSpace == mVerticalSpace && horizontalSpace == mHorizontalSpace) {
            return;
        }
        this.mVerticalSpace = verticalSpace;
        this.mHorizontalSpace = horizontalSpace;
        requestLayout();
    }

    public void setOrientation(@Orientation int orientation) {
        if (orientation == mOrientation) {
            return;
        }
        this.mOrientation = orientation;
        requestLayout();
    }

    public void setReverseLayout(boolean reverseLayout) {
    }

    public void setStackFromEnd(boolean stackFromEnd) {
    }

    @Orientation
    public int getOrientation() {
        return mOrientation;
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    @Override
    public void scrollToPosition(int position) {
        if (position >= getItemCount()) {
            return;
        }
        View view = findViewByPosition(position);
        if (view != null) {
            if (canScrollVertically()) {
                scrollVerticallyBy((int) (view.getY() - mTop), mRecycler, mState);
            } else if (canScrollHorizontally()) {
                scrollHorizontallyBy((int) (view.getX() - mLeft), mRecycler, mState);
            }
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext());
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        final int direction = calculateScrollDirectionForPosition(targetPosition);
        PointF outVector = new PointF();
        if (direction == 0) {
            return null;
        }
        if (canScrollHorizontally()) {
            outVector.x = direction;
            outVector.y = 0;
        } else if (canScrollVertically()) {
            outVector.x = 0;
            outVector.y = direction;
        }
        return outVector;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        mPendingScrollPositionOffset = INVALID_OFFSET;
        removeAllViews();
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        // mPendingScrollPositionOffset = INVALID_OFFSET;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        mRecycler = recycler;
        mState = state;
        mItemCount = getItemCount();
        if (mItemCount == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        mScrapRects = new SparseArray<>(mItemCount);
        mScrapSites = new SparseArray<>(mItemCount);
        mColumnCountOfRow = new SparseIntArray();

        mScrollOffsetX = 0;
        mScrollOffsetY = 0;

        mWidth = getWidth();
        mHeight = getHeight();

        mLeft = getPaddingLeft();
        mTop = getPaddingTop();
        mRight = getPaddingRight();
        mBottom = getPaddingBottom();

        mOffsetX = mLeft;
        mOffsetY = mTop;

        detachAndScrapAttachedViews(recycler);
        if (canScrollVertically()) {
            mTotalHeight = calculateVerticalChildrenSites(recycler);
            scrollVerticallyBy(mPendingScrollPositionOffset, recycler, state);
        } else {
            mTotalWidth = calculateHorizontalChildrenSites(recycler);
            scrollHorizontallyBy(mPendingScrollPositionOffset, recycler, state);
        }
        // fillAndRecycleView(recycler, state);
    }

    private int calculateVerticalChildrenSites(RecyclerView.Recycler recycler) {
        final int[] maxRowHeight = {0};
        final int[] totalHeight = {0};
        final Point point = new Point();
        for(int position = 0; position < mItemCount; position++) {
            View scrap = recycler.getViewForPosition(position);

            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            final int width = getDecoratedMeasurementHorizontal(scrap);
            final int height = getDecoratedMeasurementVertical(scrap);

            if (mOffsetX + width + mHorizontalSpace > mWidth - mRight) {
                mOffsetX = mLeft;
                mOffsetY += maxRowHeight[0] + (position == 0 ? 0 : mVerticalSpace);
                maxRowHeight[0] = 0;
                point.x = 0;
                point.y++;
            }
            maxRowHeight[0] = Math.max(height, maxRowHeight[0]);

            LayoutParams lp = (LayoutParams) scrap.getLayoutParams();
            lp.column = point.x++;
            lp.row = point.y;

            if (lp.column != 0) {
                mOffsetX += mHorizontalSpace;
            }

            mScrapSites.put(position, lp);
            mColumnCountOfRow.put(lp.row, lp.column + 1);

            Rect frame = mScrapRects.get(position);
            if (frame == null) {
                frame = new Rect();
            }
            frame.set(mOffsetX, mOffsetY, mOffsetX = mOffsetX + width, mOffsetY + height);
            mScrapRects.put(position, frame);

            totalHeight[0] = Math.max(totalHeight[0], mOffsetY + height);

            layoutDecoratedWithMargins(scrap, frame.left, frame.top, frame.right, frame.bottom);
        }
        return Math.max(totalHeight[0] - mTop, getVerticalSpace());
    }

    private int calculateHorizontalChildrenSites(RecyclerView.Recycler recycler) {
        final int[] maxColumnWidth = {0};
        final int[] totalWidth = {0};
        final Point point = new Point();
        for(int position = 0; position < mItemCount; position++) {
            View scrap = recycler.getViewForPosition(position);

            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            final int width = getDecoratedMeasurementHorizontal(scrap);
            final int height = getDecoratedMeasurementVertical(scrap);

            if (mOffsetY + height + mVerticalSpace > mHeight - mBottom) {
                mOffsetY = mTop;
                mOffsetX += maxColumnWidth[0] + (position == 0 ? 0 : mHorizontalSpace);
                maxColumnWidth[0] = 0;
                point.x++;
                point.y = 0;
            }
            maxColumnWidth[0] = Math.max(width, maxColumnWidth[0]);

            LayoutParams lp = (LayoutParams) scrap.getLayoutParams();
            lp.column = point.x;
            lp.row = point.y++;

            if (lp.row != 0) {
                mOffsetY += mVerticalSpace;
            }

            mScrapSites.put(position, lp);
            mColumnCountOfRow.put(lp.row, lp.column + 1);

            Rect frame = mScrapRects.get(position);
            if (frame == null) {
                frame = new Rect();
            }
            frame.set(mOffsetX, mOffsetY, mOffsetX + width, mOffsetY = mOffsetY + height);
            mScrapRects.put(position, frame);

            totalWidth[0] = Math.max(totalWidth[0], mOffsetX + width);

            layoutDecoratedWithMargins(scrap, frame.left, frame.top, frame.right, frame.bottom);
        }
        return Math.max(totalWidth[0] - mLeft, getHorizontalSpace());
    }

    @SuppressWarnings("unused")
    private void fillAndRecycleView(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mItemCount == 0 || state.isPreLayout()) {
            return;
        }

        Rect displayFrame = canScrollVertically()
                ? new Rect(mLeft, mScrollOffsetY, mWidth, mScrollOffsetY + mHeight)
                : new Rect(mScrollOffsetX, mTop, mScrollOffsetX + mWidth, mHeight);
        for(int index = 0; index < mItemCount; index++) {
            Rect rect = mScrapRects.get(index);
            if (!Rect.intersects(displayFrame, rect)) {
                View scrap = getChildAt(index);
                if (scrap != null) {
                    removeAndRecycleView(scrap, recycler);
                }
                return;
            }

            View scrap = recycler.getViewForPosition(index);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            if (canScrollVertically()) {
                layoutDecoratedWithMargins(scrap, rect.left, rect.top - mScrollOffsetY, rect.right, rect.bottom - mScrollOffsetY);
            } else {
                layoutDecoratedWithMargins(scrap, rect.left - mScrollOffsetX, rect.top, rect.right - mScrollOffsetX, rect.bottom);
            }
        }
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dy == 0 || mItemCount == 0) {
            return 0;
        }

        int travel = dy;
        if (mScrollOffsetY + travel < 0) {
            travel = -mScrollOffsetY;
        } else if (mScrollOffsetY + travel > mTotalHeight - getVerticalSpace()) {//如果滑动到最底部
            travel = mTotalHeight - getVerticalSpace() - mScrollOffsetY;
        }

        mPendingScrollPositionOffset = (mScrollOffsetY += travel);

        // detachAndScrapAttachedViews(recycler);

        offsetChildrenVertical(-travel);
        // fillAndRecycleView(recycler, state);
        return travel;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (dx == 0 || mItemCount == 0) {
            return 0;
        }

        int travel = dx;
        if (mScrollOffsetX + travel < 0) {
            travel = -mScrollOffsetX;
        } else if (mScrollOffsetX + travel > mTotalWidth - getHorizontalSpace()) {//如果滑动到最底部
            travel = mTotalWidth - getHorizontalSpace() - mScrollOffsetX;
        }

        mPendingScrollPositionOffset = (mScrollOffsetX += travel);

        // detachAndScrapAttachedViews(recycler);

        offsetChildrenHorizontal(-travel);
        // fillAndRecycleView(recycler, state);
        return travel;
    }

    @Nullable
    public LayoutParams getLayoutParamsByPosition(int position) {
        return mScrapSites.get(position);
    }

    public int getRow(int position) {
        LayoutParams params = getLayoutParamsByPosition(position);
        return params != null ? params.row : 0;
    }

    public int getColumn(int position) {
        LayoutParams params = getLayoutParamsByPosition(position);
        return params != null ? params.column : 0;
    }

    public int getColumnCountOfRow(int row) {
        return mColumnCountOfRow.get(row, 1);
    }

    private int calculateScrollDirectionForPosition(int position) {
        if (getChildCount() == 0) {
            return LAYOUT_START;
        }
        return position < getFirstChildPosition() ? LAYOUT_START : LAYOUT_END;
    }

    int getFirstChildPosition() {
        final int childCount = getChildCount();
        return childCount == 0 ? 0 : getPosition(getChildAt(0));
    }

    private int getDecoratedMeasurementHorizontal(View view) {
        final LayoutParams params = (LayoutParams) view.getLayoutParams();
        return getDecoratedMeasuredWidth(view) + params.leftMargin + params.rightMargin;
    }

    private int getDecoratedMeasurementVertical(View view) {
        final LayoutParams params = (LayoutParams) view.getLayoutParams();
        return getDecoratedMeasuredHeight(view) + params.topMargin + params.bottomMargin;
    }

    private int getVerticalSpace() {
        return mHeight - mBottom - mTop;
    }

    private int getHorizontalSpace() {
        return mWidth - mLeft - mRight;
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {

        //Current row in the grid
        public int row;
        //Current column in the grid
        public int column;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }

        @Override
        public String toString() {
            return "LayoutParams = {"
                    + "width=" + width
                    + ",height=" + height
                    + ",row=" + row
                    + ",column=" + column + "}";
        }
    }
}