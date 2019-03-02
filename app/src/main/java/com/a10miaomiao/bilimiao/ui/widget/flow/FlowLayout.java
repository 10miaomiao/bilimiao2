package com.a10miaomiao.bilimiao.ui.widget.flow;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import com.a10miaomiao.bilimiao.R;
import com.google.android.flexbox.AlignContent;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;


/**
 * Created by panda on 2016/10/24 上午9:55.
 */
public class FlowLayout extends FlexboxLayout implements FlowAdapter.OnDataChangedListener {

    private static final int INVALID_VAULE = -1;

    public static final int CHOICE_MODE_NONE = 0;
    public static final int CHOICE_MODE_SINGLE = 1;
    public static final int CHOICE_MODE_MULTIPLE = 2;

    private int mItemLeft;
    private int mItemTop;
    private int mItemRight;
    private int mItemBottom;

    private int mChoiceMode = CHOICE_MODE_NONE;
    private int mMaxChecked;
    private boolean mOverflow = false;

    private FlowAdapter mAdapter;

    private SparseBooleanArray mCheckStates = new SparseBooleanArray();

    private int mCheckedItemCount;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout, defStyleAttr, 0);
        if (ta != null) {
            mItemLeft = ta.getDimensionPixelSize(R.styleable.FlowLayout_itemLeft, 0);
            mItemTop = ta.getDimensionPixelSize(R.styleable.FlowLayout_itemTop, 0);
            mItemRight = ta.getDimensionPixelSize(R.styleable.FlowLayout_itemRight, 0);
            mItemBottom = ta.getDimensionPixelSize(R.styleable.FlowLayout_itemBottom, 0);
            mChoiceMode = ta.getInt(R.styleable.FlowLayout_choiceMode, CHOICE_MODE_NONE);
            mMaxChecked = ta.getInt(R.styleable.FlowLayout_maxChecked, INVALID_VAULE);
            ta.recycle();
        }

        setFlexWrap(FlexWrap.WRAP);
        setAlignContent(AlignContent.FLEX_START);
        setJustifyContent(JustifyContent.FLEX_START);
    }

    public void setAdapter(FlowAdapter adapter) {
        if (mAdapter != null) {
            throw new UnsupportedOperationException("The adpater of FlowLayout has been attached!");
        }
        mAdapter = adapter;
        mAdapter.setOnDataChangedListener(this);
        clearChoices();
        layoutViews();
    }

    private void layoutViews() {
        removeAllViews();

        for (int i = 0; i < mAdapter.getCount(); i++) {
            final View itemView = mAdapter.getView(i, this);
            itemView.setDuplicateParentStateEnabled(true);

            final FlowItemView itemViewWrapper = new FlowItemView(getContext());
            itemViewWrapper.addView(itemView);

            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(mItemLeft, mItemTop, mItemRight, mItemBottom);
            itemViewWrapper.setLayoutParams(lp);

            final int position = i;
            itemViewWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performItemClick(v, position);
                }
            });

            addView(itemViewWrapper);

            updateOnScreenCheckedViews();
        }
    }

    private void performItemClick(View view, int position) {
        switch (mChoiceMode) {
            case CHOICE_MODE_SINGLE: {
                boolean checked = !mCheckStates.get(position, false);
                if (checked) {
                    mCheckStates.clear();
                    mCheckStates.put(position, true);
                    mCheckedItemCount = 1;
                } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                    mCheckedItemCount = 0;
                }
            }
            break;
            case CHOICE_MODE_MULTIPLE: {
                if (mMaxChecked != INVALID_VAULE && mMaxChecked > 0) {
                    boolean checked = !mCheckStates.get(position, false);
                    if (checked) {
                        if (mCheckedItemCount < mMaxChecked) {
                            mCheckedItemCount++;
                            mCheckStates.put(position, true);
                        } else {
                            mOverflow = true;
                        }
                    } else {
                        mOverflow = false;
                        mCheckedItemCount--;
                        mCheckStates.put(position, false);
                    }
                } else {
                    boolean checked = !mCheckStates.get(position, false);
                    mCheckStates.put(position, checked);
                    if (checked) {
                        mCheckedItemCount++;
                    } else {
                        mCheckedItemCount--;
                    }
                }
            }
            break;
            case CHOICE_MODE_NONE:
            default:
                break;
        }

        updateOnScreenCheckedViews();

        if (mChoiceMode == CHOICE_MODE_MULTIPLE && mOverflow) {
            if (mOnOverflowItemClickListener != null) {
                mOnOverflowItemClickListener.onOverflowItemClick(view, position, this);
            }
        } else {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, position, this);
            }
        }
    }

    private void updateOnScreenCheckedViews() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child instanceof FlowItemView) {
                ((FlowItemView) child).setChecked(mCheckStates.get(i));
            } else {
                child.setActivated(mCheckStates.get(i));
            }
        }
    }

    @Override
    public void onDataChanged() {
        clearChoices();
        layoutViews();
    }

    public void setItemMargins(int left, int top, int right, int bottom) {
        this.mItemLeft = left;
        this.mItemTop = top;
        this.mItemRight = right;
        this.mItemBottom = bottom;
    }

    public void setChoiceMode(int choiceMode) {
        mChoiceMode = choiceMode;
        mMaxChecked = INVALID_VAULE;
        clearChoices();
        layoutViews();
    }

    public int getChoiceMode() {
        return mChoiceMode;
    }

    public void setMaxChecked(int maxNum) {
        mMaxChecked = maxNum;
    }

    public int getMaxChecked() {
        return mMaxChecked;
    }

    /**
     * This will only be valid if the choice mode is {@link #CHOICE_MODE_MULTIPLE}.
     *
     * @return
     */
    public boolean isOverFlow() {
        return mOverflow;
    }

    /**
     * Returns the number of items currently selected. This will only be valid
     * if the choice mode is not {@link #CHOICE_MODE_NONE} (default).
     * <p>
     * <p>To determine the specific items that are currently selected, use one of
     * the <code>getChecked*</code> methods.
     *
     * @return The number of items currently selected
     */
    public int getCheckedItemCount() {
        return mCheckedItemCount;
    }

    /**
     * Returns the checked state of the specified position. The result is only
     * valid if the choice mode has been set to {@link #CHOICE_MODE_SINGLE}
     * or {@link #CHOICE_MODE_MULTIPLE}.
     *
     * @param position The item whose checked state to return
     * @return The item's checked state or <code>false</code> if choice mode
     * is invalid
     * @see #setChoiceMode(int)
     */
    public boolean isItemChecked(int position) {
        if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null) {
            return mCheckStates.get(position);
        }
        return false;
    }

    /**
     * Returns the currently checked item. The result is only valid if the choice
     * mode has been set to {@link #CHOICE_MODE_SINGLE}.
     *
     * @return The position of the currently checked item or
     * {@link #INVALID_VAULE} if nothing is selected
     * @see #setChoiceMode(int)
     */
    public int getCheckedItemPosition() {
        if (mChoiceMode == CHOICE_MODE_SINGLE && mCheckStates != null && mCheckStates.size() == 1) {
            return mCheckStates.keyAt(0);
        }

        return INVALID_VAULE;
    }

    /**
     * Returns the set of checked items in the list. The result is only valid if
     * the choice mode has not been set to {@link #CHOICE_MODE_NONE}.
     *
     * @return A SparseBooleanArray which will return true for each call to
     * get(int position) where position is a checked position in the
     * list and false otherwise, or <code>null</code> if the choice
     * mode is set to {@link #CHOICE_MODE_NONE}.
     */
    public SparseBooleanArray getCheckedItemPositions() {
        if (mChoiceMode != CHOICE_MODE_NONE) {
            return mCheckStates;
        }
        return null;
    }

    /**
     * Clear any choices previously set
     */
    public void clearChoices() {
        if (mCheckStates != null) {
            mCheckStates.clear();
        }
        mCheckedItemCount = 0;
        updateOnScreenCheckedViews();
    }

    /**
     * Sets the checked state of the specified position. The is only valid if
     * the choice mode has been set to {@link #CHOICE_MODE_SINGLE} or
     * {@link #CHOICE_MODE_MULTIPLE}.
     *
     * @param position The item whose checked state is to be checked
     * @param value    The new checked state for the item
     */
    public void setItemChecked(int position, boolean value) {
        if (mChoiceMode == CHOICE_MODE_NONE) {
            return;
        }

        final boolean itemCheckChanged;
        if (mChoiceMode == CHOICE_MODE_MULTIPLE) {
            boolean oldValue = mCheckStates.get(position);
            mCheckStates.put(position, value);
            itemCheckChanged = oldValue != value;
            if (itemCheckChanged) {
                if (value) {
                    mCheckedItemCount++;
                } else {
                    mCheckedItemCount--;
                }
            }
            updateOnScreenCheckedViews();
        } else {
            itemCheckChanged = isItemChecked(position) != value;
            if (value || isItemChecked(position)) {
                mCheckStates.clear();
            }
            // this may end up selecting the value we just cleared but this way
            // we ensure length of mCheckStates is 1, a fact getCheckedItemPosition relies on
            if (value) {
                mCheckStates.put(position, true);
                mCheckedItemCount = 1;
            } else if (mCheckStates.size() == 0 || !mCheckStates.valueAt(0)) {
                mCheckedItemCount = 0;
            }

            if (itemCheckChanged) {
                updateOnScreenCheckedViews();
            }

        }
    }

    private OnItemClickListener mOnItemClickListener;
    private OnOverflowItemClickListener mOnOverflowItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnOverflowItemClickListener(OnOverflowItemClickListener listener) {
        mOnOverflowItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, FlowLayout parent);
    }

    public interface OnOverflowItemClickListener {
        void onOverflowItemClick(View view, int position, FlowLayout parent);
    }

}