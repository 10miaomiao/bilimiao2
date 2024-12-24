package cn.a10miaomiao.bilimiao.compose.pages.user

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.content.UserSeasonDetailContent
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberDI
import org.kodein.di.instance

@Serializable
data class UserSeasonDetailPage(
    private val id: String,
    private val title: String,
) : ComposePage() {

    @Composable
    override fun Content() {
        UserSeasonDetailContent(
            seasonId = id,
            seasonTitle = title,
            showTowPane = false,
            hideFirstPane = false,
            onChangeHideFirstPane = {}
        )
    }
}