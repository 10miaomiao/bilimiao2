
package master.flame.danmaku.danmaku.model.android;

import master.flame.danmaku.danmaku.model.IDrawingCache;
import master.flame.danmaku.danmaku.model.objectpool.Poolable;

public class DrawingCache implements IDrawingCache<DrawingCacheHolder>, Poolable<DrawingCache> {

    private final DrawingCacheHolder mHolder;

    private int mSize = 0;

    private DrawingCache mNextElement;

    private boolean mIsPooled;

    private int referenceCount = 0;

    public DrawingCache() {
        mHolder = new DrawingCacheHolder();
    }

    @Override
    public void build(int w, int h, int density, boolean checkSizeEquals, int bitsPerPixel) {
        final DrawingCacheHolder holder = mHolder;
        holder.buildCache(w, h, density, checkSizeEquals, bitsPerPixel);
        mSize = mHolder.bitmap.getRowBytes() * mHolder.bitmap.getHeight();
    }

    @Override
    public void erase() {
        mHolder.erase();
    }

    @Override
    public DrawingCacheHolder get() {
        final DrawingCacheHolder holder = mHolder;
        if (holder.bitmap == null) {
            return null;
        }
        return mHolder;
    }

    @Override
    public void destroy() {
        if (mHolder != null) {
            mHolder.recycle();
        }
        mSize = 0;
        referenceCount = 0;
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public void setNextPoolable(DrawingCache element) {
        mNextElement = element;
    }

    @Override
    public DrawingCache getNextPoolable() {
        return mNextElement;
    }

    @Override
    public boolean isPooled() {
        return mIsPooled;
    }

    @Override
    public void setPooled(boolean isPooled) {
        mIsPooled = isPooled;
    }

    @Override
    public synchronized boolean hasReferences() {
        return referenceCount > 0;
    }

    @Override
    public synchronized void increaseReference() {
        referenceCount++;
    }

    @Override
    public synchronized void decreaseReference() {
        referenceCount--;
    }

    @Override
    public int width() {
        return mHolder.width;
    }

    @Override
    public int height() {
        return mHolder.height;
    }

}