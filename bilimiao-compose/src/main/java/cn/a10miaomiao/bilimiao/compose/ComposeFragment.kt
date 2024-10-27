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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.common.LocalContainerView
import cn.a10miaomiao.bilimiao.compose.common.LocalFragment
import cn.a10miaomiao.bilimiao.compose.common.LocalNavController
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.LocalPageConfigInfo
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfigInfo
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.components.image.MyImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerProvider
import cn.a10miaomiao.bilimiao.compose.pages.BlankPage
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicPageContent
import cn.a10miaomiao.bilimiao.compose.pages.dynamic.DynamicPageViewModel
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.store.WindowStore
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
    }

    override val di: DI = subDI(closestDI()) {
        bindSingleton { this@ComposeFragment }
        bindSingleton { this@ComposeFragment.requireArguments() }
    }

    private val pageConfigInfo = PageConfigInfo(this)

    override val pageConfig = myPageConfig {
        val config = pageConfigInfo.lastConfig()
        title = config?.title ?: ""
        menu = config?.menu
        search = config?.search
    }

    private val viewModel by ViewModelLazy(
        DynamicPageViewModel::class,
        { this.viewModelStore },
        ::newViewModelFactory
    )

    fun newViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <R : ViewModel> create(modelClass: Class<R>): R {
                return DynamicPageViewModel(di) as R
            }
        }
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        pageConfigInfo.onMenuItemClick(view, menuItem)
    }

    override fun onSearchSelfPage(context: Context, keyword: String) {
        super.onSearchSelfPage(context, keyword)
        pageConfigInfo.onSearchSelfPage(context, keyword)
    }

    private val url by lazy {
        requireArguments().getString("url", "")
    }

    lateinit var composeNav: NavHostController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val connection = rememberNestedScrollInteropConnection(container ?: LocalView.current)
                composeNav = rememberNavController()
                CompositionLocalProvider(
                    LocalContainerView provides container,
                    LocalFragment provides this@ComposeFragment,
                    LocalNavController provides composeNav,
                    LocalPageConfigInfo provides pageConfigInfo,
                    LocalOnBackPressedDispatcherOwner provides this@ComposeFragment,
                ) {
                    withDI(di = di) {
                        val windowStore: WindowStore by rememberInstance()
                        val windowState = windowStore.stateFlow.collectAsState().value
                        val windowInsets = windowState.getContentInsets(localContainerView())
                        BilimiaoTheme {
                            ImagePreviewerProvider(
                                previewer = { state, models ->
                                    MyImagePreviewer(
                                        previewerState = state,
                                        imageModels = models,
                                        contentPadding = windowInsets.addPaddingValues(
                                            addBottom = windowStore.bottomAppBarHeightDp.dp
                                        )
                                    )
                                }
                            ) {
                                MyNavHost(composeNav, connection, url)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun onBackPressed(): Boolean {
        onBackPressedDispatcher.onBackPressed()
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
    startRoute: String,
) {
    Box(
        modifier = Modifier.nestedScroll(connection),
    ) {
        NavHost(
            navController = navController,
            startDestination = BlankPage().url(),
        ) {
            PageRouteBuilder(this)
                .initRoute()
        }
    }
    LaunchedEffect(startRoute) {
        navController.navigate(startRoute) {
            popUpTo(0) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}
