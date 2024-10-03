package cn.a10miaomiao.bilimiao.compose.pages.user

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.stringPageArg
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserSeasonDetailContent
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberDI
import org.kodein.di.instance


class UserSeasonDetailPage : ComposePage() {

    val id = stringPageArg("id")

    val title = stringPageArg("title")

    override val route: String
        get() = "user/season/${id}?title=${title}"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val seasonId = navEntry.arguments?.get(id) ?: ""
        val seasonTitle = navEntry.arguments?.get(title) ?: ""
        UserSeasonDetailContent(
            seasonId = seasonId,
            seasonTitle = seasonTitle,
            showTowPane = false,
            hideFirstPane = false,
            onChangeHideFirstPane = {}
        )
    }
}