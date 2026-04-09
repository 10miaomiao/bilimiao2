package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * AppBar 尺寸配置
 * 对应原有 ViewConfig 中的 appBar 相关常量
 */
object AppBarConfig {
    /** AppBar总高度 */
    val Height: Dp = 70.dp

    /** 标题区域高度 */
    val TitleHeight: Dp = 20.dp

    /** 菜单区域高度 */
    val MenuHeight: Dp = 50.dp

    /** 横屏时菜单宽度 */
    val MenuWidth: Dp = 120.dp

    /** 菜单项最小宽度 */
    val MenuItemMinWidth: Dp = 60.dp

    /** 菜单项图标大小 */
    val MenuItemIconSize: Dp = 20.dp

    /** 菜单项图标水平间距 */
    val MenuItemIconMarginHorizontal: Dp = 5.dp

    /** 标题文字大小 */
    val TitleTextSize: Dp = 12.dp

    /** 副标题文字大小 */
    val SubTitleTextSize: Dp = 10.dp

    /** 副标题上边距 */
    val SubTitleMarginTop: Dp = 2.dp

    /** 导航图标内边距 */
    val NavigationIconPadding: Dp = 10.dp

    /** 导航图标大小 */
    val NavigationIconSize: Dp = 24.dp

    /** 分割线高度 */
    val DividerHeight: Dp = 1.dp
}
