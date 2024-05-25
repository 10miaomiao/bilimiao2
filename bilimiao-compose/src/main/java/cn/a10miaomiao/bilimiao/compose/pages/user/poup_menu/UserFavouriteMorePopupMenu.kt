package cn.a10miaomiao.bilimiao.compose.pages.user.poup_menu

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import cn.a10miaomiao.bilimiao.compose.pages.user.MyFollowViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.TagEditDialogState
import cn.a10miaomiao.bilimiao.compose.pages.user.TagInfo
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteViewModel
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.kongzue.dialogx.dialogs.MessageDialog
import kotlinx.coroutines.flow.firstOrNull

internal class UserFavouriteMorePopupMenu(
    private val activity: Activity,
    private val viewModel: UserFavouriteViewModel,
    private val userStore: UserStore,
    private val mediasInfo: MediaListInfo,
): PopupMenu.OnMenuItemClickListener{

    private fun Menu.initMenu() {
        if (userStore.isSelf(mediasInfo.mid.toString())) {
            add(Menu.FIRST, 0, 0, "新增收藏夹")
            add(Menu.FIRST, 1, 0, "编辑收藏夹")
            add(Menu.FIRST, 2, 0, "删除收藏夹")
        } else if (mediasInfo.fav_state == 1){
            add(Menu.FIRST, 3, 0, "取消订阅")
        } else {
            add(Menu.FIRST, 4, 0, "订阅收藏")
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            0 -> {
                //  新增收藏夹
                viewModel.showAddDialog()
            }
            1 -> {
                // 编辑收藏夹
                viewModel.showEditDialog(mediasInfo)
            }
            2 -> {
                // 删除收藏夹
                viewModel.showDeleteDialog(mediasInfo)
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