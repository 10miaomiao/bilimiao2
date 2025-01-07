package cn.a10miaomiao.bilimiao.compose

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.common.LocalContainerView
import cn.a10miaomiao.bilimiao.compose.common.LocalEmitter
import cn.a10miaomiao.bilimiao.compose.common.LocalPageNavigation
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.LocalPageConfigState
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfigState
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialog
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.components.image.MyImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerProvider
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.subDI
import org.kodein.di.android.x.closestDI
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.withDI

class ComposeFragment : Fragment(), MyPage, DIAware, OnBackPressedDispatcherOwner {

    companion object {
        const val KEY_URL = "url"
        const val KEY_ENTRY = "entry"
        const val KEY_PARAM = "param"

        var id: Int = 0
            private set
        var actionId: Int = 0
            private set

        fun initFragmentNavigatorDestinationBuilder(
            builder: FragmentNavigatorDestinationBuilder,
            destinationId: Int,
            actionId: Int
        ) {
            this.id = destinationId
            this.actionId = actionId
            builder.run {
                argument(KEY_URL) {
                    type = NavType.StringType
                    nullable = true
                }
                argument(KEY_ENTRY) {
                    type = NavType.IntType
                    defaultValue = 0
                }
                argument(KEY_PARAM) {
                    type = NavType.StringType
                    nullable = true
                }
                deepLink("bilimiao://compose?url={url}")
            }
        }

        fun createArguments(
            url: String
        ): Bundle {
            return bundleOf(
                KEY_URL to url
            )
        }

        fun createArguments(
            entry: BilimiaoPageRoute.Entry,
            param: String,
        ): Bundle {
            return bundleOf(
                KEY_ENTRY to entry.ordinal,
                KEY_PARAM to param,
            )
        }
    }

    override val di: DI = subDI(closestDI()) {
        bindSingleton { this@ComposeFragment }
        bindSingleton { this@ComposeFragment.requireArguments() }
        bindSingleton { messageDialogState }
        bindSingleton { emitter }
        bindSingleton { pageNavigation }
    }

    private val pageNavigation = PageNavigation(
        this,  { composeNav }
    )
    private val pageConfigState = PageConfigState()
    private val emitter = SharedFlowEmitter()

    private var _pageConfig = PageConfigState.Cofing(-1)
    override val pageConfig = myPageConfig {
        val config = _pageConfig
        title = config.title
        menu = config.menu
        search = config.search
    }


    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        pageConfigState.onMenuItemClick(view, menuItem)
    }

    override fun onSearchSelfPage(context: Context, keyword: String) {
        super.onSearchSelfPage(context, keyword)
        pageConfigState.onSearchSelfPage(context, keyword)
    }

    private val messageDialogState = MessageDialogState()

    lateinit var composeNav: NavHostController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val arguments = requireArguments()
        val entry = arguments.getInt(KEY_ENTRY, 0)
        val param = arguments.getString(KEY_PARAM, "")
        val startRoute = BilimiaoPageRoute.getEntryRoute(
            BilimiaoPageRoute.Entry.entries[entry],
            param
        )
        return ComposeView(requireContext()).apply {
            setContent {
                val connection = rememberNestedScrollInteropConnection(container ?: LocalView.current)
                composeNav = rememberNavController()
                CompositionLocalProvider(
                    LocalContainerView provides container,
                    LocalPageConfigState provides pageConfigState,
                    LocalOnBackPressedDispatcherOwner provides this@ComposeFragment,
                    LocalPageNavigation provides pageNavigation,
                    LocalEmitter provides emitter,
                ) {
                    withDI(di = di) {
                        val windowStore: WindowStore by rememberInstance()
                        val windowState = windowStore.stateFlow.collectAsState().value
                        val windowInsets = windowState.getContentInsets(localContainerView())
                        BilimiaoTheme {
                            ImagePreviewerProvider(
                                contentPadding = windowInsets.addPaddingValues(
                                    addBottom = windowStore.bottomAppBarHeightDp.dp
                                ),
                                previewer = { MyImagePreviewer(it) }
                            ) {
                                MyNavHost(composeNav, connection, startRoute)
                            }
                            MessageDialog(messageDialogState)
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            pageConfigState.collectConfig {
                _pageConfig = it
                pageConfig.notifyConfigChanged()
            }
        }
    }

    fun onBackPressed(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        miaoLogger() debug "onBackPressed"
        return true
    }

    override val onBackPressedDispatcher: OnBackPressedDispatcher by lazy {
        OnBackPressedDispatcher {
            try {
                if (!composeNav.popBackStack()) {
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

@Composable
fun MyNavHost(
    navController: NavHostController,
    connection: NestedScrollConnection,
    startRoute: Any,
) {
//    val startDestination = remember {
//        if (startRoute == null) BlankPage()
//        else HomePage()
//    }
    Box(
        modifier = Modifier.nestedScroll(connection),
    ) {
        NavHost(
            navController = navController,
            startDestination = startRoute,
        ) {
            BilimiaoPageRoute(this)
                .initRoute()
        }
    }
//    startRoute?.let {
//        LaunchedEffect(it) {
//            navController.navigate(it) {
//                popUpTo(0) {
//                    saveState = true
//                }
//                launchSingleTop = true
//                restoreState = true
//            }
//        }
//    }
}
