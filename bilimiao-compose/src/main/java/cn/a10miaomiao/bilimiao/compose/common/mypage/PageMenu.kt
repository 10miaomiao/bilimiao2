package cn.a10miaomiao.bilimiao.compose.common.mypage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.a10miaomiao.bilimiao.comm.mypage.MyPageMenu
import com.a10miaomiao.bilimiao.comm.mypage.myMenu


@Composable
inline fun rememberMyMenu(crossinline init: MyPageMenu.() -> Unit): MyPageMenu {
    return remember {
        myMenu(init)
    }
}

@Composable
inline fun rememberMyMenu(
    vararg keys: Any?,
    crossinline init: MyPageMenu.() -> Unit
): MyPageMenu {
    return remember(*keys) {
        myMenu(init)
    }
}
