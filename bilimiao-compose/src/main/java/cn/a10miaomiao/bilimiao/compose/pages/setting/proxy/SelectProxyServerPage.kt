package cn.a10miaomiao.bilimiao.compose.pages.setting.proxy

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.localNavController
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.pages.setting.commponents.ProxyServerCard
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.proxy.BiliUposInfo
import com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.forEach
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


class SelectProxyServerPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    val serverList = MutableStateFlow(emptyList<ProxyServerInfo>())
    var selectedServerIndex = MutableStateFlow(-1)

    val uposList = listOf(
        BiliUposInfo("none", "不替换", ""),
        BiliUposInfo("ks3", "ks3（金山云）", "upos-sz-mirrorks3.bilivideo.com"),
        BiliUposInfo("kodo", "kodo（七牛云）", "upos-sz-mirrorkodo.bilivideo.com"),
        BiliUposInfo("cos", "cos（腾讯云）", "upos-sz-mirrorcos.bilivideo.com"),
        BiliUposInfo("bos", "bos（百度云）", "upos-sz-mirrorbos.bilivideo.com"),
        BiliUposInfo("wcs", "wcs（网宿云）", "upos-sz-mirrorwcs.bilivideo.com"),
        BiliUposInfo("hw", "hw（华为云）", "upos-sz-mirrorhw.bilivideo.com"),
    )
    val selectedUpos = MutableStateFlow(uposList[0])

    fun readServerList() {
        serverList.value = ProxyHelper.serverList(fragment.requireContext())
        val uposName = ProxyHelper.uposName(fragment.requireContext())
        uposList.find { it.name == uposName }?.let {
            selectedUpos.value = it
        }
    }

    fun selectedServer(index: Int) {
        selectedServerIndex.value = index
    }

    fun clearServer() {
        selectedServerIndex.value = -1
    }

    fun changeUpos(value: BiliUposInfo) {
        selectedUpos.value = value
    }

    fun applyServer() {
        ProxyHelper.saveUposName(fragment.requireContext(), selectedUpos.value.name)
        val uposHost = selectedUpos.value.host
        val proxyServer = serverList.value[selectedServerIndex.value]
        basePlayerDelegate.setProxy(proxyServer, uposHost)
        fragment.findNavController().popBackStack()
    }
}

@Composable
fun SelectProxyServerPage() {
    PageConfig(
        title = "区域限制-选择代理"
    )
    val viewModel: SelectProxyServerPageViewModel = diViewModel()
    val basePlayerDelegate: BasePlayerDelegate by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val selectedUpos by viewModel.selectedUpos.collectAsState()
    var uposMenuExpanded by remember { mutableStateOf(false) }

    val serverList by viewModel.serverList.collectAsState()
    val selectedServerIndex by viewModel.selectedServerIndex.collectAsState()

    val nav = localNavController()
    val addClick = remember(nav) {
        {
            nav.navigate("bilimiao://setting/proxy/add")
            Unit
        }
    }
    val editClick = remember(viewModel, nav) {
        {
            val index = selectedServerIndex
            viewModel.selectedServer(-1)
            nav.navigate("bilimiao://setting/proxy/edit/$index")
            Unit
        }
    }

    LaunchedEffect(viewModel, ProxyHelper.version) {
        viewModel.readServerList()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(top = windowInsets.topDp.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "代理服务器列表",
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Button(onClick = addClick) {
                Text(text = "添加服务器")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                serverList.size,
                key = { it },
            ) { i ->
                val item = serverList[i]
                ProxyServerCard(
                    name = item.name,
                    host = item.host,
                    isTrust = item.isTrust,
                    onClick = { viewModel.selectedServer(i) }
                )
            }

            item {
                if (serverList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                            .padding(bottom = windowInsets.bottomDp.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "你还没有添加服务器",
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .padding(bottom = windowInsets.bottomDp.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "下面没有了",
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
            }
        } // LazyColumn
        if (selectedServerIndex >= 0) {
            val selectedServer = serverList[selectedServerIndex]
            AlertDialog(
                onDismissRequest = viewModel::clearServer,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedServer.name,
                            fontWeight = FontWeight.W700,
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(
                            onClick = editClick,
                        ) {
                            Text("编辑服务器")
                        }
                    }
                },
                text = {
                    Column() {
                        Text(
                            text = "服务器：${selectedServer.host}",
                            fontSize = 16.sp,
                        )
                        if (selectedServer.isTrust) {
                            Text(
                                text = "已信任该服务器",
                                color = Color.Red,
                                fontSize = 16.sp,
                            )
                        } else {
                            Text(
                                text = "未信任该服务器，不会提交登录信息",
                                color = Color.Gray,
                                fontSize = 16.sp,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "替换upos视频服务器："
                            )
                            Box(
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .clickable { uposMenuExpanded = !uposMenuExpanded },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = selectedUpos.label,
                                        textAlign = TextAlign.Center,
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "展开upos视频服务器菜单"
                                    )
                                }

                                DropdownMenu(
                                    expanded = uposMenuExpanded,
                                    onDismissRequest = { uposMenuExpanded = false },
                                ) {
                                    viewModel.uposList.forEach {
                                        DropdownMenuItem(
                                            text = {
                                                Text(text = it.label)
                                            },
                                            onClick = {
                                                viewModel.changeUpos(it)
                                                uposMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = viewModel::applyServer,
                    ) {
                        Text(
                            "使用此代理",
                            fontWeight = FontWeight.W700,
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = viewModel::clearServer
                    ) {
                        Text(
                            "取消",
                            fontWeight = FontWeight.W700,
                        )
                    }
                }
            ) // AlertDialog
        }  // if
    }
}