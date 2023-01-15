package cn.a10miaomiao.bilimiao.compose

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import cn.a10miaomiao.bilimiao.compose.comm.navigation.arguments
import cn.a10miaomiao.bilimiao.compose.comm.navigation.content
import cn.a10miaomiao.bilimiao.compose.pages.BlankPage
import cn.a10miaomiao.bilimiao.compose.pages.TestPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.TelVerifyPage
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFollowPage


object PageRoute {

    val start = "bilimiao://start" content { BlankPage() }

    val test = "bilimiao://test" content { TestPage() }

    object Auth {
        val login = "bilimiao://auth/login" content { LoginPage() }
        val telVerify = "bilimiao://auth/tel_verify?code={code}&request_id={request_id}&source={source}" arguments listOf(
            navArgument("code") { type = NavType.StringType },
            navArgument("request_id") { type = NavType.StringType },
            navArgument("source") { type = NavType.StringType },
        ) content {
            val code = it.arguments?.getString("code") ?: ""
            val requestId = it.arguments?.getString("request_id") ?: ""
            val source = it.arguments?.getString("source") ?: ""
            TelVerifyPage(code, requestId, source)
        }
    }

    object User {
        val follow = "bilimiao://user/{id}/follow" arguments listOf(
            navArgument("id") { type = NavType.StringType },
        ) content {
            val id = it.arguments?.getString("id") ?: ""
            UserFollowPage(id)
        }
    }

    object Time {
        val setting = "bilimiao://time/setting" content { TimeSettingPage() }
    }

    fun builder(builder: NavGraphBuilder) = builder.run {
        +start.build(provider)
        +test.build(provider)
        +Auth.login.build(provider)
        +Auth.telVerify.build(provider)
        +User.follow.build(provider)
        +Time.setting.build(provider)
    }

}
