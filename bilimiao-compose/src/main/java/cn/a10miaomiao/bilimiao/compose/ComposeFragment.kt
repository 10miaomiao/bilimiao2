package cn.a10miaomiao.bilimiao.compose

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.window.core.layout.WindowWidthSizeClass
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.LocalContainerView
import cn.a10miaomiao.bilimiao.compose.common.LocalEmitter
import cn.a10miaomiao.bilimiao.compose.common.LocalPageNavigation
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.emitter.SharedFlowEmitter
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.LocalPageConfigState
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfigState
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AutoSheetDialog
import cn.a10miaomiao.bilimiao.compose.components.dialogs.DirectionState
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialog
import cn.a10miaomiao.bilimiao.compose.components.dialogs.MessageDialogState
import cn.a10miaomiao.bilimiao.compose.components.image.MyImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerProvider
import cn.a10miaomiao.bilimiao.compose.pages.home.HomePage
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.store.AppStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.subDI
import org.kodein.di.android.x.closestDI
import org.kodein.di.bindSingleton
import org.kodein.di.compose.rememberInstance
import org.kodein.di.compose.withDI
import org.kodein.di.instance

class ComposeFragment : Fragment(), MyPage, DIAware, OnBackPressedDispatcherOwner {

    override val di: DI = subDI(closestDI()) {
        bindSingleton { this@ComposeFragment }
        bindSingleton { this@ComposeFragment.requireArguments() }
        bindSingleton { messageDialogState }
        bindSingleton { emitter }
        bindSingleton { pageNavigation }
        bindSingleton { bottomSheetState }
    }

    private val pageNavigation = PageNavigation(
        navHostController = { composeNav },
        launchUrl = ::launchWebBrowser,
    )
    private val pageConfigState = PageConfigState()
    private val emitter = SharedFlowEmitter()
    private val uriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            if (!BilibiliNavigation.navigationTo(pageNavigation, uri)) {
                BilibiliNavigation.navigationToWeb(pageNavigation, uri)
            }
        }
    }

    private val appStore by instance<AppStore>()
    private val windowStore by instance<WindowStore>()

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
    private val bottomSheetState = BottomSheetState()

    lateinit var composeNav: NavHostController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val bottomSheetView = FrameLayout(context) // 临时打补丁给windowStore使用，后期会移除windowStore
        bottomSheetView.tag = "bottomSheet"
        return ComposeView(context).apply {
            setContent {
                val connection = rememberNestedScrollInteropConnection(container ?: LocalView.current)
                composeNav = rememberNavController()
                CompositionLocalProvider(
                    LocalContainerView provides container,
                    LocalPageConfigState provides pageConfigState,
                    LocalOnBackPressedDispatcherOwner provides this@ComposeFragment,
                    LocalPageNavigation provides pageNavigation,
                    LocalEmitter provides emitter,
                    LocalUriHandler provides uriHandler
                ) {
                    withDI(di = di) {
                        val appState = appStore.stateFlow.collectAsState().value
                        val windowState = windowStore.stateFlow.collectAsState().value
                        val windowInsets = windowState.getContentInsets(localContainerView())
                        BilimiaoTheme(
                            appState = appState,
                        ) {
                            ImagePreviewerProvider(
                                contentPadding = windowInsets.addPaddingValues(
                                    addBottom = windowStore.bottomAppBarHeightDp.dp
                                ),
                                previewer = { state, innerPadding ->
                                    MyImagePreviewer(state, innerPadding)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .nestedScroll(connection)
                                        .background(MaterialTheme.colorScheme.background),
                                ) {
                                    MyNavHost(composeNav, HomePage)
                                }
                                val bottomSheetPage = bottomSheetState.page.collectAsState().value
                                if (bottomSheetPage != null) {
                                    MyBottomSheet(
                                        bottomSheetView,
                                        bottomSheetPage,
                                        onClose = {
                                            bottomSheetState.close()
                                        }
                                    )
                                }
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
                composeNav.popBackStack()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun launchWebBrowser(uri: Uri) {
        // 使用外部浏览器打开
        val activity = requireActivity()
        val typedValue = TypedValue()
        val attrId = com.google.android.material.R.attr.colorSurfaceVariant
        activity.theme.resolveAttribute(attrId, typedValue, true)
        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(ContextCompat.getColor(activity, typedValue.resourceId))
                    .build()
            )
            .build()
        intent.launchUrl(activity, uri)
    }

    fun navigateByUri(deepLink: Uri) {
        pageNavigation.navigateByUri(deepLink)
    }

    fun navigate(page: ComposePage) {
        pageNavigation.navigate(page)
    }

    fun goBackHome() {
        composeNav.popBackStack(HomePage, false)
    }

    fun openBottomSheet(page: ComposePage) {
        bottomSheetState.open(page)
    }

}

@Composable
fun MyNavHost(
    navController: NavHostController,
    startRoute: Any,
) {
    NavHost(
        navController = navController,
        startDestination = startRoute,
    ) {
        BilimiaoPageRoute(this)
            .initRoute()
    }
}

@SuppressLint("RestrictedApi")
@Composable
fun MyBottomSheet(
    container: ViewGroup?,
    page: ComposePage,
    onClose: () -> Unit,
) {
    val parentPageNavigation by rememberInstance<PageNavigation>()
    val bottomSheetNav = rememberNavController()
    val pageNavigation = remember {
        PageNavigation(
            navHostController = { bottomSheetNav },
            launchUrl = parentPageNavigation::launchWebBrowser,
            onClose = onClose,
        )
    }
    val pageConfigState = remember {
        PageConfigState()
    }
    org.kodein.di.compose.subDI(
        diBuilder = {
            bindSingleton(
                overrides = true
            ) { pageNavigation }
        }
    ) {
        CompositionLocalProvider(
            LocalContainerView provides container,
            LocalPageConfigState provides pageConfigState,
            LocalPageNavigation provides pageNavigation,
        ) {
            AutoSheetDialog(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .heightIn(max = 500.dp),
                content = {
                    MyNavHost(bottomSheetNav, page)
                    MyBottomSheetTitleBar(pageConfigState, onClose)
                },
                onDismiss = onClose,
                onPreDismiss = bottomSheetNav::popBackStack,
            )
        }
    }
}

@Composable
fun MyBottomSheetTitleBar(
    state: PageConfigState,
    onClose: () -> Unit,
) {
    val config = state.collectConfigAsState()
    Box(
        modifier = Modifier
            .height(48.dp)
            .padding(horizontal = 10.dp)
            .fillMaxWidth(),
    ) {
        IconButton(
            onClick = onClose,
            colors = IconButtonDefaults.iconButtonColors()
                .copy(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                        .copy(alpha = 0.75f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
            modifier = Modifier
                .size(30.dp)
                .align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "close"
            )
        }

        AnimatedContent(
            modifier = Modifier
                .align(Alignment.Center),
            targetState = config.value.title,
            contentKey = { it },
            label = "BottomSheetTitle",
        ) { title ->
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer
                            .copy(alpha = 0.75f)
                    )
                    .padding(vertical = 2.dp, horizontal = 10.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                text = title.replace("\n", " "),
            )
        }

    }
}
