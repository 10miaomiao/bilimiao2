package cn.a10miaomiao.bilimiao.compose.comm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import cn.a10miaomiao.bilimiao.compose.ComposeFragment

internal val LocalFragment: ProvidableCompositionLocal<Fragment> = compositionLocalOf { ComposeFragment() }

@Composable
fun localFragment() = LocalFragment.current

internal val LocalFragmentNavController: ProvidableCompositionLocal<NavController?> = compositionLocalOf { null }

@Composable
fun localFragmentNavController() = LocalFragmentNavController.current ?: Navigation.findNavController(LocalView.current)


internal val LocalNavController: ProvidableCompositionLocal<NavHostController?> = compositionLocalOf { null }

@Composable
fun localNavController() = LocalNavController.current ?: Navigation.findNavController(LocalView.current)


