package cn.a10miaomiao.bilimiao.compose.common.proxy

import android.content.Context
import com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import kotlinx.coroutines.flow.MutableStateFlow

class ProxyRepositoryAndroid(
    private val context: Context,
) : ProxyRepository {

    override val serverListVersion = MutableStateFlow(0)

    override fun saveServer(server: ProxyServerInfo?, index: Int) {
        ProxyHelper.saveServer(context, server, index)
        serverListVersion.value++
    }

    override fun serverList(): List<ProxyServerInfo> {
        return ProxyHelper.serverList(context)
    }

    override fun saveUposName(uposName: String) {
        ProxyHelper.saveUposName(context, uposName)
    }

    override fun uposName(): String {
        return ProxyHelper.uposName(context)
    }
}
