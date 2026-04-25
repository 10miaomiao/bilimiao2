package cn.a10miaomiao.bilimiao.compose.components.appbar

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPageMenu

/**
 * 菜单项数据
 * 用于在 Compose 侧构建 AppBar 菜单
 */
data class MenuItemData(
    /** 菜单项唯一标识 */
    val key: Int,
    /** 显示标题 */
    val title: String,
    /** 副标题（可选） */
    val subTitle: String? = null,
    /** 图标 - ImageVector 优先 */
    val iconVector: ImageVector? = null,
    /** 图标 - DrawableRes */
    @DrawableRes val iconResource: Int? = null,
    /** 子菜单 */
    val childMenu: List<MenuItemData>? = null,
    /** 子菜单是否为可选中菜单 */
    val checkable: Boolean = false,
    /** 子菜单当前选中的 key */
    val checkedKey: Int? = null,
    /** 内容描述（无障碍） */
    val contentDescription: String? = null,
) {
    companion object {
        /**
         * 从 MenuItemPropInfo 转换
         */
        fun fromPropInfo(propInfo: MenuItemPropInfo, context: Context): MenuItemData {
            var iconResource = propInfo.iconResource
            propInfo.iconFileName?.let {
                iconResource = context.resources.getIdentifier(it, "drawable", context.packageName)
            }
            return MenuItemData(
                key = propInfo.key ?: 0,
                title = propInfo.title ?: "",
                subTitle = propInfo.subTitle,
                iconResource = iconResource,
                childMenu = propInfo.childMenu?.items?.let { items ->
                    items.map { fromPropInfo(it, context) }
                },
                checkable = propInfo.childMenu?.checkable == true,
                checkedKey = propInfo.childMenu?.takeIf { it.checkable }?.checkedKey,
                contentDescription = propInfo.contentDescription,
            )
        }
    }

    /**
     * 转换为 MenuItemPropInfo（用于与旧代码兼容）
     */
    fun toPropInfo(): MenuItemPropInfo {
        return MenuItemPropInfo(
            key = key,
            title = title,
            subTitle = subTitle,
            iconResource = iconResource,
            childMenu = childMenu?.toMyPageMenu(
                checkable = checkable,
                checkedKey = checkedKey,
            ),
            contentDescription = contentDescription,
        )
    }

    private fun List<MenuItemData>.toMyPageMenu(
        checkable: Boolean,
        checkedKey: Int?,
    ): MyPageMenu {
        return MyPageMenu().apply {
            this.checkable = checkable
            this.checkedKey = checkedKey ?: 0
            forEach { child ->
                myItem {
                    key = child.key
                    title = child.title
                    subTitle = child.subTitle
                    iconResource = child.iconResource
                    childMenu = child.childMenu?.toMyPageMenu(
                        checkable = child.checkable,
                        checkedKey = child.checkedKey,
                    )
                    contentDescription = child.contentDescription
                }
            }
        }
    }
}

/**
 * AppBar 导航图标类型
 */
enum class AppBarNavigationIcon {
    /** 返回箭头 */
    Back,
    /** 菜单图标 */
    Menu,
    /** 自定义图标 */
    Custom,
}

/**
 * AppBar 布局方向
 */
enum class AppBarOrientation {
    /** 竖屏 - 标题在上，菜单水平排列 */
    Vertical,
    /** 横屏 - 导航在左，菜单垂直排列 */
    Horizontal,
}
