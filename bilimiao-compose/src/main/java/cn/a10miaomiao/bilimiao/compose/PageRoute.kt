package cn.a10miaomiao.bilimiao.compose

import androidx.navigation.*
import cn.a10miaomiao.bilimiao.compose.comm.navigation.NavDestinationBuilder
import cn.a10miaomiao.bilimiao.compose.comm.navigation.arguments
import cn.a10miaomiao.bilimiao.compose.comm.navigation.content
import cn.a10miaomiao.bilimiao.compose.pages.BlankPage
import cn.a10miaomiao.bilimiao.compose.pages.TestPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.LoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.QrCodeLoginPage
import cn.a10miaomiao.bilimiao.compose.pages.auth.TelVerifyPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadBangumiCreatePage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadListPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.ProxySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.ProxySettingPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.AddProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.EditProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.SelectProxyServerPage
import cn.a10miaomiao.bilimiao.compose.pages.time.TimeSettingPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFollowPage
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao


object PageRoute {

    val start = "bilimiao://start" content { BlankPage() }

    val test = "bilimiao://test" content { TestPage() }

    object Auth {
        val login = "bilimiao://auth/login" content { LoginPage() }
        val qr_login = "bilimiao://auth/qr_login" content { QrCodeLoginPage() }
        val telVerify = "bilimiao://auth/tel_verify?code={code}&request_id={request_id}&source={source}" arguments listOf(
            navArgument("code") { type = NavType.StringType },
            navArgument("request_id") { type = NavType.StringType; defaultValue = "" },
            navArgument("source") { type = NavType.StringType; defaultValue = "" },
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

    object Download {
        val list = "bilimiao://download/list" content {
            DownloadListPage()
        }
        val detail = "bilimiao://download/detail?path={path}" arguments listOf(
            navArgument("path") { type = NavType.StringType },
        )  content {
            val path = it.arguments?.getString("path") ?: ""
            DownloadDetailPage(path)
        }

        val bangumiCreate = "bilimiao://download/bangumi/create?id={id}" arguments listOf(
            navArgument("id") { type = NavType.StringType },
        )  content {
            val id = it.arguments?.getString("id") ?: ""
            DownloadBangumiCreatePage(id)
        }
    }

    object Time {
        val setting = "bilimiao://time/setting" content { TimeSettingPage() }
    }

    object Setting {
        val proxySetting = "bilimiao://setting/proxy" content { ProxySettingPage() }
        val proxy_addProxyServer = "bilimiao://setting/proxy/add" content { AddProxyServerPage() }
        val proxy_editProxyServer = "bilimiao://setting/proxy/edit/{index}" arguments listOf(
            navArgument("index") { type = NavType.IntType },
        ) content {
            val index = it.arguments?.getInt("index") ?: -1
            EditProxyServerPage(index)
        }
        val proxy_selectProxyServer = "bilimiao://setting/proxy/select" content { SelectProxyServerPage() }
    }

    fun builder(builder: NavGraphBuilder) = builder.run {
        +start.build(provider)
        +test.build(provider)
        autoBuild(Auth, Auth::class.java)
        autoBuild(User, User::class.java)
        autoBuild(Download, Download::class.java)
        autoBuild(Time, Time::class.java)
        autoBuild(Setting, Setting::class.java)
    }
    
    inline fun <reified T> NavGraphBuilder.autoBuild(t: T) {
        autoBuild(t, T::class.java)
    }

    fun <T> NavGraphBuilder.autoBuild(t: T, clazz: Class<T>) {
        val typeName = NavDestinationBuilder::class.java.name
        clazz.methods.forEach {
            if (it.returnType.name == typeName) {
                (it.invoke(t) as? NavDestinationBuilder)?.let { obj ->
                    addDestination(obj.build(provider))
                }
            }
        }
    }

}
