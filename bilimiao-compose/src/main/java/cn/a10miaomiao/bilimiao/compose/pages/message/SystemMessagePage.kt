package cn.a10miaomiao.bilimiao.compose.pages.message

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

internal class SystemMessagePageModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

}


@Composable
fun SystemMessagePage() {
    val viewModel: ReplyMessagePageModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    Column {
        Text(text = "回复我的")
    }
}