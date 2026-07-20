package cn.a10miaomiao.bilimiao.compose.base

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

/**
 * 所有页面的基类接口，同时作为 Nav3 的 NavKey。
 * 子类需为 data class 或 object，并建议加 @Serializable 以支持 rememberNavBackStack。
 */
interface ComposePage : NavKey {

    @Composable
    fun Content()

}
