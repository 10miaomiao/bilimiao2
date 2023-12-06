package cn.a10miaomiao.bilimiao.compose.pages.setting.proxy

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.localNavController
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.setting.commponents.ProxyServerForm
import cn.a10miaomiao.bilimiao.compose.pages.setting.commponents.ProxyServerFormState
import cn.a10miaomiao.bilimiao.compose.pages.setting.commponents.rememberProxyServerFormState
import com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class AddProxyServerPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {
    private val fragment by instance<Fragment>()
    private val composeNav by instance<NavHostController>()
    fun addProxyServer(
        formState: ProxyServerFormState
    ) {
        if (formState.name.isBlank()) {
            Toast.makeText(fragment.requireActivity(), "请填写服务器名称", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (formState.host.isBlank()) {
            Toast.makeText(fragment.requireActivity(), "请填写服务器地址", Toast.LENGTH_SHORT)
                .show()
            return
        }
        ProxyHelper.saveServer(
            fragment.requireActivity(),
            ProxyServerInfo(
                name = formState.name,
                host = formState.host,
                isTrust = formState.isTrust,
                enableAdvanced = formState.enableAdvanced,
                queryArgs = formState.queryArgStates.map {
                    ProxyServerInfo.HttpQueryArg(
                        enable = true,
                        key = it.key,
                        value = it.value,
                    )
                },
                headers = formState.headerStates.map {
                    ProxyServerInfo.HttpHeader(
                        enable = true,
                        name = it.key,
                        value = it.value,
                    )
                }
            )
        )
        Toast.makeText(fragment.requireActivity(), "添加成功", Toast.LENGTH_SHORT)
            .show()
        composeNav.popBackStack()
    }
}

@Composable
fun AddProxyServerPage() {
    PageConfig(
        title = "添加代理服务器"
    )

    val viewModel: AddProxyServerPageViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val scrollState = rememberScrollState()

    val formState = rememberProxyServerFormState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(
                start = windowInsets.leftDp.dp,
                end = windowInsets.rightDp.dp,
                top = windowInsets.topDp.dp,
                bottom = windowInsets.bottomDp.dp,
            )
    ) {
        ProxyServerForm(
            state = formState,
        )
        Button(
            onClick = {
                viewModel.addProxyServer(formState)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "添加代理服务器")
        }
    }
}