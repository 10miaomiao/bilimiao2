package cn.a10miaomiao.bilimiao.compose.pages.setting.proxy

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.localNavController
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.setting.commponents.ProxyServerForm
import com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class EditProxyServerPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {
    private val fragment by instance<Fragment>()

    var index = -1
        set(value) {
            field = value
            val serverList = ProxyHelper.serverList(fragment.requireContext())
            if (index in 0 until serverList.size) {
                val item = serverList[index]
                setName(item.name)
                setHost(item.host)
                setIsTrust(item.isTrust)
            }
        }

    val name = MutableStateFlow("")
    val host = MutableStateFlow("")
    val isTrust = MutableStateFlow(false)

    fun setName(value: String) { name.value = value }
    fun setHost(value: String) { host.value = value }
    fun setIsTrust(value: Boolean) { isTrust.value = value }

    fun editProxyServer(): Boolean {
        if (name.value.isBlank()) {
            Toast.makeText(fragment.requireActivity(), "请填写服务器名称", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (host.value.isBlank()) {
            Toast.makeText(fragment.requireActivity(), "请填写服务器地址", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        ProxyHelper.saveServer(
            fragment.requireActivity(),
            ProxyServerInfo(
                name = name.value,
                host = host.value,
                isTrust = isTrust.value,
            ),
            index,
        )
        Toast.makeText(fragment.requireActivity(), "修改成功", Toast.LENGTH_SHORT)
            .show()
        return true
    }

    fun deleteProxyServer(): Boolean {
        ProxyHelper.saveServer(
            fragment.requireActivity(),
            null,
            index,
        )
        Toast.makeText(fragment.requireActivity(), "删除成功", Toast.LENGTH_SHORT)
            .show()
        return true
    }
}

@Composable
fun EditProxyServerPage(
    index: Int,
) {
    PageConfig(
        title = "编辑代理服务器"
    )

    val viewModel: EditProxyServerPageViewModel = diViewModel()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val scrollState = rememberScrollState()

    val name by viewModel.name.collectAsState()
    val host by viewModel.host.collectAsState()
    val isTrust by viewModel.isTrust.collectAsState()

    val nav = localNavController()

    val addClick = remember(viewModel) {
        {
            if (viewModel.editProxyServer()) {
                nav.popBackStack()
            }
            Unit
        }
    }

    val delClick = remember(viewModel) {
        {
            if (viewModel.deleteProxyServer()) {
                nav.popBackStack()
            }
            Unit
        }
    }

    LaunchedEffect(viewModel, index) {
        viewModel.index = index
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
            name = name,
            onNameChange = viewModel::setName,
            host = host,
            onHostChange = viewModel::setHost,
            isTrust = isTrust,
            onIsTrustChange = viewModel::setIsTrust,
        )
        Button(
            onClick = delClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
        ) {
            Text(text = "删除服务器")
        }
        Button(
            onClick = addClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "保存修改")
        }
    }
}