package com.a10miaomiao.bilimiao.comm.delegate.player

import android.app.Activity
import android.view.Menu
import android.view.View
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.store.UserStore

class QualityPopupMenu(
    private val activity: Activity,
    private val anchor: View,
    private val userStore: UserStore,
    private val list: List<PlayerSourceInfo.AcceptInfo>,
    private val value: Int,
) {
    private val popupMenu = PopupMenu(activity, anchor)
    val MAX_QUALITY_NOT_LOGIN = 48 // 48[480P 清晰]
    val MAX_QUALITY_NOT_VIP = 80 // 80[1080P 高清]

    init {
        popupMenu.menu.apply {
            initMenu()
        }

        //使用反射，强制显示菜单图标
        try {
            val field = popupMenu.javaClass.getDeclaredField("mPopup")
            field.isAccessible = true
            val mPopup = field.get(popupMenu) as MenuPopupHelper
            mPopup.setForceShowIcon(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Menu.initMenu() {
        list.forEachIndexed { index, item ->
            add(Menu.FIRST, index, 0, item.description).apply {
                if(item.quality > MAX_QUALITY_NOT_VIP) {
                    setIcon(R.drawable.ic_big_vip)
                    isEnabled = userStore.isVip()
                } else if (item.quality > MAX_QUALITY_NOT_LOGIN && !userStore.isLogin()) {
                    setIcon(R.drawable.ic_login)
                    isEnabled = false
                }
                isChecked = value == item.quality
            }
        }
        setGroupCheckable(Menu.FIRST, true, true)
    }

    fun setOnChangedQualityListener(changedQuality: (Int) -> Unit) {
        popupMenu.setOnMenuItemClickListener {
            val position = it.itemId
            val item = list[position]
            changedQuality(item.quality)
            false
        }
    }

    fun show() {
        popupMenu.show()
    }
}