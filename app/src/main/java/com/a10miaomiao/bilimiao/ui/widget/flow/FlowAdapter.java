package com.a10miaomiao.bilimiao.ui.widget.flow;


import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * Created by panda on 2016/10/24 上午9:59.
 */
public abstract class FlowAdapter<T> {

    private OnDataChangedListener mOnDataChangedListener;

    private List<T> mDataList;

    public FlowAdapter(List<T> dataList) {
        this.mDataList = dataList;
    }

    public FlowAdapter(T[] dataArray) {
        this.mDataList = Arrays.asList(dataArray);
    }

    public int getCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public T getItem(int position) {
        return mDataList != null ? mDataList.get(position) : null;
    }

    public abstract View getView(int position, FlowLayout parent);

    public void notifyDataSetChanged() {
        if (mOnDataChangedListener != null) {
            mOnDataChangedListener.onDataChanged();
        }
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        mOnDataChangedListener = listener;
    }

    public List<T> getDataList() {
        return mDataList;
    }

    public void setDataList(List<T> mDataList) {
        this.mDataList = mDataList;
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }

}