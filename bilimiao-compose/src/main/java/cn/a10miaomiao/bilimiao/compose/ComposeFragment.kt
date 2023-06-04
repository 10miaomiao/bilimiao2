package cn.a10miaomiao.bilimiao.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.comm.LocalContainerView
import cn.a10miaomiao.bilimiao.compose.comm.LocalFragment
import cn.a10miaomiao.bilimiao.compose.comm.LocalFragmentNavController
import cn.a10miaomiao.bilimiao.compose.comm.LocalNavController
import cn.a10miaomiao.bilimiao.compose.comm.mypage.LocalPageConfigInfo
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfigInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.subDI
import org.kodein.di.android.x.closestDI
import org.kodein.di.bindSingleton
import org.kodein.di.compose.withDI

class ComposeFragment : Fragment(), MyPage, DIAware {

    override val di: DI = subDI(closestDI()) {
        bindSingleton { this@ComposeFragment }
        bindSingleton { this@ComposeFragment.requireArguments() }
    }

    private val pageConfigInfo = PageConfigInfo(this)

    override val pageConfig = myPageConfig {
        title = pageConfigInfo.title
        menus = pageConfigInfo.menus
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        pageConfigInfo.onMenuItemClick?.invoke(menuItem)
    }

    private val url by lazy {
        requireArguments().getString("url", "")
    }

    private lateinit var fragmentNav: NavController
    private lateinit var composeNav: NavHostController


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentNav = findNavController()
        return ComposeView(requireContext()).apply {
            setContent {
                // TODO: 嵌套滚动
                rememberNestedScrollInteropConnection(container ?: LocalView.current)
                composeNav = rememberNavController()
                CompositionLocalProvider(
                    LocalContainerView provides container,
                    LocalFragment provides this@ComposeFragment,
                    LocalFragmentNavController provides fragmentNav,
                    LocalNavController provides composeNav,
                    LocalPageConfigInfo provides pageConfigInfo,
                ) {
                    withDI(di = di) {
                        BilimiaoTheme {
                            MyNavHost(composeNav, url)
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        showPrivacyDialog()
//        requireActivity().onBackPressedDispatcher
//            .addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    composeNav.
//                }
//            })
    }

    fun onBackPressed(): Boolean {
        if (!composeNav.popBackStack()) {
            findNavController().popBackStack()
        }
        return true
    }


}

@Composable
fun MyNavHost(
    navController: NavHostController,
    startRoute: String,
) {
    NavHost(
        navController = navController,
        startDestination = PageRoute.start.url(),
        builder = PageRoute::builder
    )
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
