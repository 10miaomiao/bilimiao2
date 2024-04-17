package cn.a10miaomiao.bilimiao.compose.pages.user.poup_menu

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import cn.a10miaomiao.bilimiao.compose.pages.user.MyFollowViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.TagEditDialogState
import cn.a10miaomiao.bilimiao.compose.pages.user.TagInfo
import com.kongzue.dialogx.dialogs.MessageDialog

internal class MyFollowMorePopupMenu(
    private val activity: Activity,
    private val tagInfo: TagInfo,
    private val viewModel: MyFollowViewModel,
): PopupMenu.OnMenuItemClickListener{

    private fun Menu.initMenu() {
        add(Menu.FIRST, 0, 0, "修改分组")
        add(Menu.FIRST, 1, 0, "删除分组")
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            0 -> {
                viewModel.updateTagEditDialogState(
                    TagEditDialogState.Update(
                        tagInfo.tagid,
                        tagInfo.name,
                    )
                )
            }
            1 -> {
                if (tagInfo.count > 0) {
                    MessageDialog.build()
                        .setTitle("提示")
                        .setMessage("该分组下还有关注的人\n删除后将会放到默认分组")
                        .setOkButton("确定") { dialog, view ->
                            viewModel.deleteTag(tagInfo.tagid)
                            false
                        }
                        .setCancelButton("取消")
                        .show()
                } else {
                    viewModel.deleteTag(tagInfo.tagid)
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