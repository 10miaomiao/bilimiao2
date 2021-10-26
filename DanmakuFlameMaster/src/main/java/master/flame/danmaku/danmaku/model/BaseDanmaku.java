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

package master.flame.danmaku.danmaku.model;

import android.util.SparseArray;

public abstract class BaseDanmaku {

    public final static String DANMAKU_BR_CHAR = "/n";

    public final static int TYPE_SCROLL_RL = 1;

    public final static int TYPE_SCROLL_LR = 6;

    public final static int TYPE_FIX_TOP = 5;

    public final static int TYPE_FIX_BOTTOM = 4;

    public final static int TYPE_SPECIAL = 7;

    public final static int TYPE_MOVEABLE_XXX = 0; // TODO: add more type

    public final static int INVISIBLE = 0;

    public final static int VISIBLE = 1;

    public final static int FLAG_REQUEST_REMEASURE = 0x1;
    public final static int FLAG_REQUEST_INVALIDATE = 0x2;

    /**
     * 显示时间(毫秒)
     */
    private long time;

    /**
     * 偏移时间
     */
    public long timeOffset;

    /**
     * 文本
     */
    public CharSequence text;

    /**
     * 多行文本: 如果有包含换行符需事先拆分到lines
     */
    public String[] lines;

    /**
     * 保存一些数据的引用(库内部使用, 外部使用请用tag)
     */
    public Object obj;

    /**
     * 可保存一些自定义数据的引用(外部使用).
     * 除非主动set null,否则不会自动释放引用.
     * 确定你会主动set null, 否则不要使用这个字段引用大内存的对象实例.
     */
    public Object tag;

    /**
     * 文本颜色
     */
    public int textColor;

    /**
     * Z轴角度
     */
    public float rotationZ;

    /**
     * Y轴角度
     */
    public float rotationY;

    /**
     * 阴影/描边颜色
     */
    public int textShadowColor;

    /**
     * 下划线颜色,0表示无下划线
     */
    public int underlineColor = 0;

    /**
     * 字体大小
     */
    public float textSize = -1;

    /**
     * 框的颜色,0表示无框
     */
    public int borderColor = 0;

    /**
     * 内边距(像素)
     */
    public int padding = 0;

    /**
     * 弹幕优先级,0为低优先级,>0为高优先级不会被过滤器过滤
     */
    public byte priority = 0;

    /**
     * 占位宽度
     */
    public float paintWidth = -1;

    /**
     * 占位高度
     */
    public float paintHeight = -1;

    /**
     * 存活时间(毫秒)
     */
    public Duration duration;

    /**
     * 索引/编号
     */
    public int index;

    /**
     * 是否可见
     */
    public int visibility;

    /**
     * 重置位 visible
     */
    private int visibleResetFlag = 0;

    /**
     * 重置位 measure
     */
    public int measureResetFlag = 0;

    /**
     * 重置位 offset time
     */
    public int syncTimeOffsetResetFlag = 0;

    /**
     * 重置位 prepare
     */
    public int prepareResetFlag = -1;

    /**
     * 绘制用缓存
     */
    public IDrawingCache<?> cache;

    /**
     * 是否是直播弹幕
     */
    public boolean isLive;

    /**
     * 临时, 是否在同线程创建缓存
     */
    public boolean forceBuildCacheInSameThread;

    /**
     * 弹幕发布者id, 0表示游客
     */
    public int userId = 0;

    /**
     * 弹幕发布者id
     */
    public String userHash;

    /**
     * 是否游客
     */
    public boolean isGuest;

    /**
     * 计时
     */
    protected DanmakuTimer mTimer;

    /**
     * 透明度
     */
    protected int alpha = AlphaValue.MAX;

    public int mFilterParam = 0;

    public int filterResetFlag = -1;

    public GlobalFlagValues flags = null;

    public int requestFlags = 0;

    /**
     * 标记是否首次显示，首次显示后将置为FIRST_SHOWN_RESET_FLAG
     */
    public int firstShownFlag = -1;

    private SparseArray<Object> mTags = new SparseArray<>();

    public long getDuration() {
        return duration.value;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public int draw(IDisplayer displayer) {
        return displayer.draw(this);
    }

    public boolean isMeasured() {
        return paintWidth > -1 && paintHeight > -1
                && measureResetFlag == flags.MEASURE_RESET_FLAG;
    }

    public void measure(IDisplayer displayer, boolean fromWorkerThread) {
        displayer.measure(this, fromWorkerThread);
        this.measureResetFlag = flags.MEASURE_RESET_FLAG;
    }

    public boolean isPrepared() {
        return this.prepareResetFlag == flags.PREPARE_RESET_FLAG;
    }

    public void prepare(IDisplayer displayer, boolean fromWorkerThread) {
        displayer.prepare(this, fromWorkerThread);
        this.prepareResetFlag = flags.PREPARE_RESET_FLAG;
    }

    public IDrawingCache<?> getDrawingCache() {
        return cache;
    }

    public boolean isShown() {
        return this.visibility == VISIBLE
                && visibleResetFlag == flags.VISIBLE_RESET_FLAG;
    }

    public boolean isTimeOut() {
        return mTimer == null || isTimeOut(mTimer.currMillisecond());
    }

    public boolean isTimeOut(long ctime) {
        return ctime - getActualTime() >= duration.value;
    }

    public boolean isOutside() {
        return mTimer == null || isOutside(mTimer.currMillisecond());
    }

    public boolean isOutside(long ctime) {
        long dtime = ctime - getActualTime();
        return dtime <= 0 || dtime >= duration.value;
    }

    public boolean isLate() {
        return mTimer == null || mTimer.currMillisecond() < getActualTime();
    }

    public boolean hasPassedFilter() {
        if (filterResetFlag != flags.FILTER_RESET_FLAG) {
            mFilterParam = 0;
            return false;
        }
        return true;
    }

    public boolean isFiltered() {
        return filterResetFlag == flags.FILTER_RESET_FLAG && mFilterParam != 0;
    }

    public boolean isFilteredBy(int flag) {
        return filterResetFlag == flags.FILTER_RESET_FLAG && (mFilterParam & flag) == flag;
    }

    public void setVisibility(boolean b) {
        if (b) {
            this.visibleResetFlag = flags.VISIBLE_RESET_FLAG;
            this.visibility = VISIBLE;
        } else
            this.visibility = INVISIBLE;
    }

    public abstract void layout(IDisplayer displayer, float x, float y);

    public abstract float[] getRectAtTime(IDisplayer displayer, long currTime);

    public abstract float getLeft();

    public abstract float getTop();

    public abstract float getRight();

    public abstract float getBottom();

    /**
     * return the type of Danmaku
     *
     * @return TYPE_SCROLL_RL = 0 TYPE_SCROLL_RL = 1 TYPE_SCROLL_LR = 2
     * TYPE_FIX_TOP = 3; TYPE_FIX_BOTTOM = 4;
     */
    public abstract int getType();

    public DanmakuTimer getTimer() {
        return mTimer;
    }

    public void setTimer(DanmakuTimer timer) {
        mTimer = timer;
    }

    public int getAlpha() {
        return alpha;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public void setTag(int key, Object tag) {
        this.mTags.put(key, tag);
    }

    public Object getTag(int key) {
        if (mTags == null) {
            return null;
        }
        return mTags.get(key);
    }

    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
        this.syncTimeOffsetResetFlag = flags.SYNC_TIME_OFFSET_RESET_FLAG;
    }

    public void setTime(long time) {
        this.time = time;
        this.timeOffset = 0;
    }

    public long getTime() {
        return time;
    }

    public long getActualTime() {
        if (flags == null || flags.SYNC_TIME_OFFSET_RESET_FLAG != this.syncTimeOffsetResetFlag) {
            this.timeOffset = 0;
            return time;
        }
        return time + timeOffset;
    }

    public boolean isOffset() {
        if (flags == null || flags.SYNC_TIME_OFFSET_RESET_FLAG != this.syncTimeOffsetResetFlag) {
            this.timeOffset = 0;
            return false;
        }
        return timeOffset != 0;
    }
}
