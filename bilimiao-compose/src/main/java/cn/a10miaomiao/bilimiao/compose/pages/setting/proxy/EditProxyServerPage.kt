package cn.a10miaomiao.bilimiao.compose.pages.setting.proxy

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.pages.setting.components.KeyValueInputStateCarrier
import cn.a10miaomiao.bilimiao.compose.pages.setting.components.ProxyServerForm
import cn.a10miaomiao.bilimiao.compose.pages.setting.components.ProxyServerFormState
import cn.a10miaomiao.bilimiao.compose.pages.setting.components.rememberProxyServerFormState
import com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class EditProxyServerPage(
    private val index: Int = 0,
) : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: EditProxyServerPageViewModel = diViewModel()
        EditProxyServerPageContent(index, viewModel)
    }

}
internal class EditProxyServerPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val pageNavigation by instance<PageNavigation>()
    var index = -1

    fun getProxyServer(): ProxyServerInfo? {
        val serverList = ProxyHelper.serverList(fragment.requireContext())
        if (index in 0 until serverList.size) {
            return serverList[index]
        }
        return null
    }

    fun editProxyServer(
        formState: ProxyServerFormState
    ) {
        if (formState.name.isBlank()) {
            PopTip.show("请填写服务器名称")
            return
        }
        if (formState.host.isBlank()) {
            PopTip.show("请填写服务器地址")
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
            ),
            index,
        )
        PopTip.show("修改成功")
        pageNavigation.popBackStack()
    }

    fun deleteProxyServer() {
        ProxyHelper.saveServer(
            fragment.requireActivity(),
            null,
            index,
        )
        PopTip.show("删除成功")
        pageNavigation.popBackStack()
    }
}

@Composable
internal fun EditProxyServerPageContent(
    index: Int,
    viewModel: EditProxyServerPageViewModel,
) {
    PageConfig(
        title = "编辑代理服务器"
    )

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val scrollState = rememberScrollState()

    val formState = rememberProxyServerFormState()

    LaunchedEffect(viewModel, index) {
        viewModel.index = index
        viewModel.getProxyServer()?.let { server ->
            formState.changeName(server.name)
            formState.changeHost(server.host)
            formState.changeIsTrust(server.isTrust)
            formState.changeEnableAdvanced(server.enableAdvanced ?: false)
            formState.initQueryArgStates(server.queryArgs?.map {
                KeyValueInputStateCarrier(it.key, it.value)
            } ?: listOf())
            formState.initHeaderStates(server.headers?.map {
                KeyValueInputStateCarrier(it.name, it.value)
            } ?: listOf())
        }
    }

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
            onClick = viewModel::deleteProxyServer,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
        ) {
            Text(text = "删除服务器")
        }
        Button(
            onClick = {
                viewModel.editProxyServer(formState)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "保存修改")
        }
    }
}