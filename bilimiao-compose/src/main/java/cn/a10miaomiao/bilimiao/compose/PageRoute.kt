package cn.a10miaomiao.bilimiao.compose

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import cn.a10miaomiao.bilimiao.compose.comm.navigation.arguments
import cn.a10miaomiao.bilimiao.compose.comm.navigation.content
import cn.a10miaomiao.bilimiao.compose.pages.BlankPage
import cn.a10miaomiao.bilimiao.compose.pages.TestPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFollowPage


object PageRoute {

    val start = "bilimiao://start" content { BlankPage() }

    val test = "bilimiao://test" content { BlankPage() }

    object User {
        val follow = "bilimiao://user/{id}/follow" arguments listOf(
            navArgument("id") { type = NavType.StringType },
        ) content {
            val id = it.arguments?.getString("id") ?: ""
            UserFollowPage(id)
        }
    }

    fun builder(builder: NavGraphBuilder) = builder.run {
        +start.build(provider)
        +test.build(provider)
        +User.follow.build(provider)
    }

}
