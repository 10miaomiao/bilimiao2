package cn.a10miaomiao.bilimiao.compose.common.mypage

import androidx.compose.runtime.*
import com.a10miaomiao.bilimiao.comm.mypage.MenuActions
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPageMenu
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce

private var _configId = 0
interface OnMyPageListener {
    fun onMenuItemClick(menuItem: MenuItemPropInfo)
    fun onSearchSelfPage(keyword: String)
}

class PageConfigState {
    private val configFlow = MutableStateFlow(Cofing(-1))
    private val configList = mutableListOf<Cofing>()
    private val listenerMap = mutableMapOf<Int, OnMyPageListener>()

    var openSearch: (() -> Unit)? = null

    val currentConfig: Cofing get() = configFlow.value

    fun addConfig(id: Int, configBuilder: (Cofing) -> Unit) {
        configList.add(Cofing(id).apply(configBuilder))
        notifyConfigChanged()
    }

    fun removeConfig(id: Int) {
        val i = configList.indexOfFirst { it.id == id }
        if (i != -1) {
            configList.removeAt(i)
            notifyConfigChanged()
        }
    }

    private fun notifyConfigChanged() {
        configFlow.value = configList.lastOrNull() ?: Cofing(-1)
    }

    @OptIn(FlowPreview::class)
    suspend fun collectConfig(collector: FlowCollector<Cofing>) {
        configFlow.debounce(200).collect(collector)
    }

    @Composable
    fun collectConfigAsState(): State<Cofing> {
        return configFlow.collectAsState()
    }

    fun putMyPageListener(id: Int, listener: OnMyPageListener) {
        listenerMap[id] = listener
    }

    fun removeMyPageListener(id: Int) {
        listenerMap.remove(id)
    }

    fun onMenuItemClick(menuItem: MenuItemPropInfo) {
        if (menuItem.action == MenuActions.search) {
            openSearch?.invoke()
            return
        }
        val id = configFlow.value.id
        val listener = listenerMap[id] ?: return
        listener.onMenuItemClick(menuItem)
    }

    fun onSearchSelfPage(keyword: String) {
        val id = configFlow.value.id
        val listener = listenerMap[id] ?: return
        listener.onSearchSelfPage(keyword)
    }

    class Cofing(
        val id: Int,
    ) {
        var title: String = ""
        var menu: MyPageMenu? = null
        var search: SearchConfigInfo? = null
    }
}

internal val LocalPageConfigState: ProvidableCompositionLocal<PageConfigState?> =
    compositionLocalOf { null }

@Composable
fun PageConfig(
    title: String = "",
    menu: MyPageMenu? = null,
    search: SearchConfigInfo? = null
): Int {
    val pageConfigInfo = LocalPageConfigState.current ?: return -1
    val configId = remember {
        _configId++
    }
    DisposableEffect(
        title, menu, search
    ) {
        pageConfigInfo.addConfig(configId) {
            it.title = title
            it.menu = menu
            it.search = search
        }
        onDispose {
            pageConfigInfo.removeConfig(configId)
        }
    }
    return configId
}

@Composable
fun PageListener(
    configId: Int,
    onSearchSelfPage: ((keyword: String) -> Unit)? = null,
    onMenuItemClick: ((menuItem: MenuItemPropInfo) -> Unit)? = null
) {
    val pageConfigInfo = LocalPageConfigState.current
    if (configId == -1 || pageConfigInfo == null) {
        return
    }
    DisposableEffect(configId, onSearchSelfPage, onMenuItemClick) {
        val listener = object : OnMyPageListener {
            override fun onMenuItemClick(menuItem: MenuItemPropInfo) {
                onMenuItemClick?.invoke(menuItem)
            }

            override fun onSearchSelfPage(keyword: String) {
                onSearchSelfPage?.invoke(keyword)
            }
        }
        pageConfigInfo.putMyPageListener(configId, listener)
        onDispose {
            pageConfigInfo.removeMyPageListener(configId)
        }
    }
}
