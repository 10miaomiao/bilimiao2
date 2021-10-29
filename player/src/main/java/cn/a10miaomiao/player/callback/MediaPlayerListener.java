package cn.a10miaomiao.player.callback;

/**
 * Created by hcc on 16/8/31 21:42
 * 100332338@qq.com
 * <p/>
 * 视频控制回调接口
 */
public interface MediaPlayerListener
{

    void start();

    void pause();

    long getDuration();

    long getCurrentPosition();

    void seekTo(long pos);
//    void seekTo2(long pos);

    boolean isPlaying();

    long getBufferPercentage();

    boolean canPause();

    int getState();
}
