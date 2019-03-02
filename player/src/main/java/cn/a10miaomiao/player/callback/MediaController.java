package cn.a10miaomiao.player.callback;

import android.view.View;

public interface MediaController {
    public void setEnabled(boolean enabled);
    public void show();
    public void show(int timeout);
    public void hide();
    public boolean isShowing();
    public void setMediaPlayer(MediaPlayerListener player);
    public void setAnchorView(View v);
    public void setTitle(String title);
}