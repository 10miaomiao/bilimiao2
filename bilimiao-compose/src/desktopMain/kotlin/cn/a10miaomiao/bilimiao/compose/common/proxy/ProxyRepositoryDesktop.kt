package cn.a10miaomiao.bilimiao.compose.common.proxy

import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import kotlinx.coroutines.flow.MutableStateFlow

class ProxyRepositoryDesktop : ProxyRepository {

    override val serverListVersion = MutableStateFlow(0)

    override fun saveServer(server: ProxyServerInfo?, index: Int) {
        println("ProxyRepository.saveServer is not supported on Desktop")
    }

    override fun serverList(): List<ProxyServerInfo> {
        return emptyList()
    }

    override fun saveUposName(uposName: String) {
        println("ProxyRepository.saveUposName is not supported on Desktop")
    }

    override fun uposName(): String {
        return "none"
    }
}
