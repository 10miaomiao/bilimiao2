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

package master.flame.danmaku.danmaku.renderer;


import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.ICacheManager;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.Danmakus;

public interface IRenderer {
    
    int NOTHING_RENDERING = 0;
    int CACHE_RENDERING = 1;
    int TEXT_RENDERING = 2;

    interface OnDanmakuShownListener {
        void onDanmakuShown(BaseDanmaku danmaku);
    }

    class Area {

        public final float[] mRefreshRect = new float[4];
        private int mMaxHeight;
        private int mMaxWidth;

        public void setEdge(int maxWidth, int maxHeight) {
            mMaxWidth = maxWidth;
            mMaxHeight = maxHeight;
        }

        public void reset() {
            set(mMaxWidth, mMaxHeight, 0, 0);
        }

        public void resizeToMax() {
            set(0, 0, mMaxWidth, mMaxHeight);
        }

        public void set(float left, float top, float right, float bottom) {
            mRefreshRect[0] = left;
            mRefreshRect[1] = top;
            mRefreshRect[2] = right;
            mRefreshRect[3] = bottom;
        }

    }

    public class RenderingState {
        public final static int UNKNOWN_TIME = -1;

        public boolean isRunningDanmakus;
        public DanmakuTimer timer = new DanmakuTimer();
        public int indexInScreen;
        public int totalSizeInScreen;
        public BaseDanmaku lastDanmaku;

        public int r2lDanmakuCount;
        public int l2rDanmakuCount;
        public int ftDanmakuCount;
        public int fbDanmakuCount;
        public int specialDanmakuCount;
        public int totalDanmakuCount;
        public int lastTotalDanmakuCount;
        public long consumingTime;
        public long beginTime;
        public long endTime;
        public boolean nothingRendered;
        public long sysTime;
        public long cacheHitCount;
        public long cacheMissCount;

        private IDanmakus runningDanmakus = new Danmakus(Danmakus.ST_BY_LIST);
        private boolean mIsObtaining;

        public int addTotalCount(int count) {
            totalDanmakuCount += count;
            return totalDanmakuCount;
        }

        public int addCount(int type, int count) {
            switch (type) {
                case BaseDanmaku.TYPE_SCROLL_RL:
                    r2lDanmakuCount += count;
                    return r2lDanmakuCount;
                case BaseDanmaku.TYPE_SCROLL_LR:
                    l2rDanmakuCount += count;
                    return l2rDanmakuCount;
                case BaseDanmaku.TYPE_FIX_TOP:
                    ftDanmakuCount += count;
                    return ftDanmakuCount;
                case BaseDanmaku.TYPE_FIX_BOTTOM:
                    fbDanmakuCount += count;
                    return fbDanmakuCount;
                case BaseDanmaku.TYPE_SPECIAL:
                    specialDanmakuCount += count;
                    return specialDanmakuCount;
            }
            return 0;
        }

        public void reset() {
            lastTotalDanmakuCount = totalDanmakuCount;
            r2lDanmakuCount = l2rDanmakuCount = ftDanmakuCount = fbDanmakuCount = specialDanmakuCount = totalDanmakuCount = 0;
            sysTime = beginTime = endTime = consumingTime = 0;
            nothingRendered = false;
            synchronized (this) {
                runningDanmakus.clear();
            }
        }

        public void set(RenderingState other) {
            if(other == null)
                return;
            lastTotalDanmakuCount = other.lastTotalDanmakuCount;
            r2lDanmakuCount = other.r2lDanmakuCount;
            l2rDanmakuCount = other.l2rDanmakuCount;
            ftDanmakuCount = other.ftDanmakuCount;
            fbDanmakuCount = other.fbDanmakuCount;
            specialDanmakuCount = other.specialDanmakuCount;
            totalDanmakuCount = other.totalDanmakuCount;
            consumingTime = other.consumingTime;
            beginTime = other.beginTime;
            endTime = other.endTime;
            nothingRendered = other.nothingRendered;
            sysTime = other.sysTime;
            cacheHitCount = other.cacheHitCount;
            cacheMissCount = other.cacheMissCount;
        }

        public void appendToRunningDanmakus(BaseDanmaku danmaku) {
            if (!mIsObtaining) {
                runningDanmakus.addItem(danmaku);
            }
        }

        public IDanmakus obtainRunningDanmakus() {
            mIsObtaining = true;
            IDanmakus danmakus;
            synchronized (this) {
                danmakus = runningDanmakus;
                runningDanmakus = new Danmakus(Danmakus.ST_BY_LIST);
            }
            mIsObtaining = false;
            return danmakus;
        }

    }

    void draw(IDisplayer disp, IDanmakus danmakus, long startRenderTime, RenderingState renderingState);

    void clear();

    void clearRetainer();

    void release();

    void setVerifierEnabled(boolean enabled);

    void setCacheManager(ICacheManager cacheManager);

    void setOnDanmakuShownListener(OnDanmakuShownListener onDanmakuShownListener);

    void removeOnDanmakuShownListener();

    void alignBottom(boolean enable);

}
