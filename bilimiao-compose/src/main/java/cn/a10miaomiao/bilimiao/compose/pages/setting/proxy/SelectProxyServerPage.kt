package cn.a10miaomiao.bilimiao.compose.pages.setting.proxy

import android.app.Activity
import androidx.compose.animation.AnimatedContentScope
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
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.pages.setting.components.ProxyServerCard
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.proxy.BiliUposInfo
import com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class SelectProxyServerPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: SelectProxyServerPageViewModel = diViewModel()
        SelectProxyServerPageContent(viewModel)
    }

}

internal class SelectProxyServerPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val activity by instance<Activity>()
    private val pageNavigation by instance<PageNavigation>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    val serverList = MutableStateFlow(emptyList<ProxyServerInfo>())
    var selectedServerIndex = MutableStateFlow(-1)

//    bos（百度）upos-sz-mirrorbos.bilivideo.com
//    cos（騰訊） upos-sz-mirrorcos.bilivideo.com
//    cosb（騰訊）upos-sz-mirrorcosb.bilivideo.com
//    coso1（騰訊） upos-sz-mirrorcoso1.bilivideo.com
//    hw（華為） upos-sz-mirrorhw.bilivideo.com
//    hwb（華為） upos-sz-mirrorhwb.bilivideo.com
//    hwo1（華為） upos-sz-mirrorhwo1.bilivideo.com
//    08c（華為）upos-sz-mirror08c.bilivideo.com
//    08h（華為） upos-sz-mirror08h.bilivideo.com
//    08ct（華為） upos-sz-mirror08ct.bilivideo.com
//    ali（阿里） upos-sz-mirrorali.bilivideo.com
//    alib（阿里） upos-sz-mirroralib.bilivideo.com
//    alio1（阿里） upos-sz-mirroralio1.bilivideo.com
//    Akamai海外 upos-hz-mirrorakam.akamaized.net
//    aliov（阿里海外） upos-sz-mirroraliov.bilivideo.com
//    hwov（華為海外） upos-sz-mirrorhwov.bilivideo.com
//    cosov（騰訊海外） upos-sz-mirrorcosov.bilivideo.com
//    hk_bcache（Bilibili海外） cn-hk-eq-bcache-01.bilivideo.com
//    tf_hw（華為） upos-tf-all-hw.bilivideo.com
//    tf_tx（騰訊） upos-tf-all-tx.bilivideo.com
    val uposList = listOf(
        BiliUposInfo("none", "不替换", ""),
//        BiliUposInfo("ks3", "ks3（金山云）", "upos-sz-mirrorali.bilivideo.com"),
//        BiliUposInfo("kodo", "kodo（七牛云）", "upos-sz-mirrorkodo.bilivideo.com"),
        BiliUposInfo("ali", "ali（阿里云）", "upos-sz-mirrorali.bilivideo.com"),
        BiliUposInfo("cos", "cos（腾讯云）", "upos-sz-mirrorcos.bilivideo.com"),
//        BiliUposInfo("bos", "bos（百度云）", "upos-sz-mirrorbos.bilivideo.com"),
//        BiliUposInfo("wcs", "wcs（网宿云）", "upos-sz-mirrorwcs.bilivideo.com"),
        BiliUposInfo("hw", "hw（华为云）", "upos-sz-mirrorhw.bilivideo.com"),
        BiliUposInfo("akamai", "akamai（Akamai海外）", "upos-hz-mirrorakam.akamaized.net"),
        BiliUposInfo("aliov", "aliov（阿里海外）", "upos-sz-mirroraliov.bilivideo.com"),
//        BiliUposInfo("aliov", "hwov（华为海外）", "upos-sz-mirrorhwov.bilivideo.com"),
        BiliUposInfo("aliov", "cosov（腾讯海外）", "upos-sz-mirrorcosov.bilivideo.com"),
        BiliUposInfo("tf_hw", "tf_hw（华为）", "upos-tf-all-hw.bilivideo.com"),
        BiliUposInfo("tf_tx", "tf_tx（腾讯）", "upos-tf-all-tx.bilivideo.com"),
    )

    val selectedUpos = MutableStateFlow(uposList[0])

    fun readServerList() {
        serverList.value = ProxyHelper.serverList(activity)
        val uposName = ProxyHelper.uposName(activity)
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
        ProxyHelper.saveUposName(activity, selectedUpos.value.name)
        val uposHost = selectedUpos.value.host
        val proxyServer = serverList.value[selectedServerIndex.value]
        basePlayerDelegate.setProxy(proxyServer, uposHost)
        pageNavigation.popBackStack()
    }

    fun toAddPage() {
        pageNavigation.navigate(AddProxyServerPage())
    }

    fun toEditPage(
        index: Int
    ) {
        pageNavigation.navigate(EditProxyServerPage(
            index = index
        ))
    }
}

@Composable
internal fun SelectProxyServerPageContent(
    viewModel: SelectProxyServerPageViewModel
) {
    PageConfig(
        title = "区域限制-选择代理"
    )
    val basePlayerDelegate: BasePlayerDelegate by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val selectedUpos by viewModel.selectedUpos.collectAsState()
    var uposMenuExpanded by remember { mutableStateOf(false) }

    val serverList by viewModel.serverList.collectAsState()
    val selectedServerIndex by viewModel.selectedServerIndex.collectAsState()

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
            Button(onClick = viewModel::toAddPage) {
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
                            onClick = {
                                viewModel.toEditPage(selectedServerIndex)
                            },
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