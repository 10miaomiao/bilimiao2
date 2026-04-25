package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * AppBar 可选中菜单项
 * 选中状态会显示主题色样式
 *
 * @param data 菜单项数据
 * @param checked 是否选中
 * @param themeColor 主题色（选中时使用）
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun CheckableMenuItem(
    data: MenuItemData,
    checked: Boolean,
    themeColor: Color,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
    expandable: Boolean = false,
    expanded: Boolean = false,
    verticalLayout: Boolean = true,
    depth: Int = 0,
) {
    BaseMenuItem(
        data = data,
        checked = checked,
        themeColor = themeColor,
        onClick = onClick,
        modifier = modifier,
        expandable = expandable,
        expanded = expanded,
        verticalLayout = verticalLayout,
        depth = depth,
    )
}
