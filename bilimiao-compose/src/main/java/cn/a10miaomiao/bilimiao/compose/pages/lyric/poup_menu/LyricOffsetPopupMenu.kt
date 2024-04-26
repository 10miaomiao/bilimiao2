package cn.a10miaomiao.bilimiao.compose.pages.lyric.poup_menu

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import cn.a10miaomiao.bilimiao.compose.pages.lyric.LyricPageViewModel

internal class LyricOffsetPopupMenu(
    private val activity: Activity,
    private val viewModel: LyricPageViewModel,
): PopupMenu.OnMenuItemClickListener{

    private fun Menu.initMenu() {
        add(Menu.FIRST,-1000,1,"-1000" )
        add(Menu.FIRST,-100,2,"-100" )
        add(Menu.FIRST,-10,3,"-10" )
        add(Menu.FIRST,0,4,"->0" )
        add(Menu.FIRST,10,5,"+10" )
        add(Menu.FIRST,100,6,"+100" )
        add(Menu.FIRST,1000,7,"+1000" )
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.let {
            if(item.itemId==0){
                viewModel.offset.value=0
            } else {
                viewModel.offset.value+=it.itemId
            }
        }
        return false
    }

    fun show(anchor: View) {
        val popupMenu = PopupMenu(activity, anchor)
        popupMenu.menu.initMenu()
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }
}