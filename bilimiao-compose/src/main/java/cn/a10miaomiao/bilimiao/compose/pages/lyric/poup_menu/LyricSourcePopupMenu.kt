package cn.a10miaomiao.bilimiao.compose.pages.lyric.poup_menu

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.pages.lyric.LyricPageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class LyricSourcePopupMenu(
    private val activity: Activity,
    private val viewModel: LyricPageViewModel,
): PopupMenu.OnMenuItemClickListener{

    private fun Menu.initMenu() {
        viewModel.source.value.forEachIndexed { index, it ->
            val type=when(it.type){
                LyricPageViewModel.KUGOU-> "酷狗"
                LyricPageViewModel.NETEASE -> "网易云"
                else -> "未知来源"
            }
            val name = it.name.let {
                if (it.length > 15)
                    it.take(13) + "..."
                else
                    it
            }
            add(Menu.FIRST,index,index, "${index+1}.[$type]$name")
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item?.let {
            viewModel.viewModelScope.launch{
                withContext(Dispatchers.IO) {
                    viewModel.loadLyric(it.itemId,true)
                }
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