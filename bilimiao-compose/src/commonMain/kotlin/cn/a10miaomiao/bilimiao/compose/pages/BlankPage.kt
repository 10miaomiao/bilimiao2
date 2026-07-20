package cn.a10miaomiao.bilimiao.compose.pages

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigator
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

@Serializable
class BlankPage : ComposePage {

    @Composable
    override fun Content() {
        val viewModel: BlankPageViewModel = diViewModel {
            BlankPageViewModel(it)
        }
        BlankPageContent(viewModel)
    }
}

private class BlankPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigator>()

}


@Composable
private fun BlankPageContent(
    viewModel: BlankPageViewModel
) {
    PageConfig(
        title = ""
    )
    val windowInsets = localContentInsets()
}
