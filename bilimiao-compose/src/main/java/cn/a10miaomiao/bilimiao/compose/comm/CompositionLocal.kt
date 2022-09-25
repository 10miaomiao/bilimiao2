package cn.a10miaomiao.bilimiao.compose.comm

import android.app.Activity
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import org.kodein.di.DI

internal val LocalFragment: ProvidableCompositionLocal<Fragment> = compositionLocalOf { ComposeFragment() }

@Composable
fun localFragment() = LocalFragment.current

internal val LocalNavController: ProvidableCompositionLocal<NavController?> = compositionLocalOf { null }

@Composable
fun localNavController() = LocalNavController.current ?: Navigation.findNavController(LocalView.current)




