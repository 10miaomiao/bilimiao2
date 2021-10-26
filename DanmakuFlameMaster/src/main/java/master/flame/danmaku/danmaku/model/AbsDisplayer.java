package master.flame.danmaku.danmaku.model;

import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;

public abstract class AbsDisplayer<T, F> implements IDisplayer {
    
    public abstract T getExtraData();
    
    public abstract void setExtraData(T data);

    @Override
    public boolean isHardwareAccelerated() {
        return false;
    }

    public abstract void drawDanmaku(BaseDanmaku danmaku, T canvas, float left, float top, boolean fromWorkerThread);

    public abstract void clearTextHeightCache();

    public abstract void setTypeFace(F font);

    public abstract void setFakeBoldText(boolean bold);

    public abstract void setTransparency(int newTransparency);

    public abstract void setScaleTextSizeFactor(float factor);

    public abstract void setCacheStuffer(BaseCacheStuffer cacheStuffer);

    public abstract BaseCacheStuffer getCacheStuffer();
}
