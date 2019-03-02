package cn.a10miaomiao.player;

import android.net.Uri;

public class VideoSource {
    Uri uri;
    long length;
    long size;

    public VideoSource() {
    }

    public VideoSource(Uri uri) {
        this(uri, 0, 0);
    }

    public VideoSource(Uri uri, long length, long size) {
        this.uri = uri;
        this.length = length;
        this.size = size;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}