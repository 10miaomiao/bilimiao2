package cn.a10miaomiao.bilimiao.compose.pages.dynamic

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navOptions
import bilibili.app.dynamic.v2.UpListItem
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePage
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class DynamicViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val pageNavigation by instance<PageNavigation>()

    val tabs = listOf<DynamicPageTab>(
        DynamicPageTab.All,
        DynamicPageTab.Video,
    )

    private val _upList = MutableStateFlow(listOf<UpListItem>())
    val upList: StateFlow<List<UpListItem>> get() = _upList
    private val _selectedUpper = MutableStateFlow<UpListItem?>(null)
    val selectedUpper: StateFlow<UpListItem?> get() = _selectedUpper

    fun toHomePage() {
        val nav = pageNavigation.hostController
        nav.navigate(HomePage, navOptions {
            popUpTo(nav.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        })
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        when (item.key) {
            MenuKeys.home -> {
                toHomePage()
            }
        }
    }

    fun setUpList(list: List<UpListItem>) {
        _upList.value = list
        if (list.isNotEmpty()) {
            selectUpper(list[0])
        }
    }

    fun selectUpper(item: UpListItem) {
        _selectedUpper.value = item
    }

}