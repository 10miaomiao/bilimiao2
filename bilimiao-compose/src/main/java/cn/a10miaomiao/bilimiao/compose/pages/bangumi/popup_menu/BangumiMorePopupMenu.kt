package cn.a10miaomiao.bilimiao.compose.pages.bangumi.popup_menu

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.NavHostController
import cn.a10miaomiao.bilimiao.compose.comm.navigation.BottomSheetNavigation
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadBangumiCreatePage
import com.a10miaomiao.bilimiao.comm.entity.bangumi.SeasonV2Info
import com.kongzue.dialogx.dialogs.PopTip

class BangumiMorePopupMenu (
    private val activity: Activity,
    private val navController: NavHostController,
    private val detailInfo: SeasonV2Info?,
): PopupMenu.OnMenuItemClickListener {

    private fun Menu.initMenu() {
        add(Menu.FIRST, 0, 0, "用浏览器打开")
        if (detailInfo != null) {
            add(Menu.FIRST, 5, 0, "分享番剧(${detailInfo?.stat?.share ?: ""})")
        } else {
            add(Menu.FIRST, 5, 0, "分享番剧")
        }
        add(Menu.FIRST, 2, 0, "复制链接")
        add(Menu.FIRST, 4, 0, "下载番剧")
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            0 -> {
                val info = detailInfo
                if (info != null) {
                    val id = info.season_id
                    var intent = Intent(Intent.ACTION_VIEW)
                    var url = "https://www.bilibili.com/bangumi/play/ss$id"
                    intent.data = Uri.parse(url)
                    activity.startActivity(intent)
                } else {
                    PopTip.show("请等待信息加载完成")
                }
            }
            2 -> {
                val info = detailInfo
                if (info != null) {
                    val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    var label = "url"
                    var text = "https://www.bilibili.com/bangumi/play/ss${info.season_id}"
                    val clip = ClipData.newPlainText(label, text)
                    clipboard.setPrimaryClip(clip)
                    PopTip.show("已复制：$text")
                } else {
                    PopTip.show("请等待信息加载完成")
                }
            }
            4 -> {
                val info = detailInfo
                if (info != null) {
                    val url = DownloadBangumiCreatePage().apply {
                        id set info.season_id
                    }.url()
                    BottomSheetNavigation.navigate(activity, url)
                } else {
                    PopTip.show("请等待信息加载完成")
                }
            }
            5 -> {
                val info = detailInfo
                if (info != null) {
                    var shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "bilibili番剧分享")
                        putExtra(Intent.EXTRA_TEXT, "${info.season_title} https://www.bilibili.com/bangumi/play/ss${info.season_id}")
                    }
                    activity.startActivity(Intent.createChooser(shareIntent, "分享"))
                } else {
                    PopTip.show("请等待信息加载完成")
                }

            }
        }
        return false
    }

    fun show(anchor: View) {
        val popupMenu = PopupMenu(activity, anchor)
        popupMenu.menu.apply {
            initMenu()
        }
        popupMenu.setOnMenuItemClickListener(this)
        popupMenu.show()
    }


}