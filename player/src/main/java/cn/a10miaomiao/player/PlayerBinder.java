package cn.a10miaomiao.player;

import android.os.Binder;


public class PlayerBinder extends Binder {

    private PlayerService mPlayerService;

    public PlayerBinder(PlayerService playerService) {
        mPlayerService = playerService;
    }

    public PlayerService getmPlayerService() {
        return mPlayerService;
    }
}
