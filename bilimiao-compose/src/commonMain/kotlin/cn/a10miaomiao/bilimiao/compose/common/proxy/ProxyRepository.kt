package cn.a10miaomiao.bilimiao.compose.common.proxy

import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 代理服务器数据仓库接口
 * 抽象 ProxyHelper 的平台相关操作（文件 I/O、SharedPreferences）
 */
interface ProxyRepository {

    val serverListVersion: MutableStateFlow<Int>

    fun saveServer(server: ProxyServerInfo?, index: Int = -1)

    fun serverList(): List<ProxyServerInfo>

    fun saveUposName(uposName: String)

    fun uposName(): String
}
