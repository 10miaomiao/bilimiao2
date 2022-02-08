package com.a10miaomiao.bilimiao

import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.page.MainFragment
import com.a10miaomiao.bilimiao.page.auth.H5LoginFragment
import com.a10miaomiao.bilimiao.page.region.RegionFragment
import com.a10miaomiao.bilimiao.page.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.template.SettingFragment
import com.a10miaomiao.bilimiao.template.TemplateFragment
import com.a10miaomiao.bilimiao.page.setting.AboutFragment
import com.a10miaomiao.bilimiao.page.setting.DanmakuSettingFragment
import com.a10miaomiao.bilimiao.page.setting.VideoSettingFragment
import kotlin.reflect.KClass


object MainNavGraph {
    // Counter for id's. First ID will be 1.
    private var id_counter = 1
    private val idToFragment = hashMapOf<Int, FragmentDest>()
    private var id = id_counter++

    object dest {
        val id = id_counter++
        val home = f<MainFragment>()
        val template = f<TemplateFragment>()
        val timeSetting = f<TimeSettingFragment>() {
            deepLink("bilimiao://time/setting")
        }
        val setting = f<SettingFragment>() {
            deepLink("bilimiao://setting")
        }
        val danmakuSetting = f<DanmakuSettingFragment>() {
            deepLink("bilimiao://setting/danmaku")
        }
        val videoSetting = f<VideoSettingFragment>() {
            deepLink("bilimiao://setting/video")
        }
        val about = f<AboutFragment>() {
            deepLink("bilimiao://about")
        }
        val region = f<RegionFragment> {
            argument(args.region) {
                type = NavType.ParcelableType(RegionInfo::class.java)
            }
        }
        val videoInfo = f<VideoInfoFragment> {
            argument(args.type) {
                type = NavType.StringType
                defaultValue = "AV"
            }
            argument(args.id) {
                type = NavType.StringType
                nullable = false
            }
        }
        val h5Login = f<H5LoginFragment> {
        }
    }

    object action {
        val id = id_counter++
        val home_to_region = dest.home to dest.region
        val home_to_setting = dest.home to dest.setting
        val home_to_h5Login = dest.home to dest.h5Login

        val setting_to_videoSetting = dest.setting to dest.videoSetting
        val setting_to_danmakuSetting = dest.setting to dest.danmakuSetting
        val setting_to_about = dest.setting to dest.about

        val region_to_videoInfo = dest.region to dest.videoInfo

        val videoInfo_to_videoInfo = dest.videoInfo to dest.videoInfo
    }

    object args {
        const val type = "type"
        const val id = "id"
        const val region = "region"
    }

    private val defaultNavOptionsBuilder: NavOptionsBuilder.() -> Unit = {
        anim {
            enter = R.anim.h_fragment_enter
            exit = R.anim.h_fragment_exit
            popEnter = R.anim.h_fragment_pop_enter
            popExit = R.anim.h_fragment_pop_exit
        }
    }

    fun createGraph(navController: NavController, startDestination: Int) {
        navController.graph = navController.createGraph(id, startDestination) {
            val id = dest.id + action.id
            idToFragment.values.forEach { fd ->
                destination(
                    FragmentNavigatorDestinationBuilder(
                        provider[FragmentNavigator::class],
                        fd.id,
                        fd.fragmentClass
                    ).apply {
                        fd.builder(this)
                        fd.actionList.forEach { fa ->
                            this.action(fa.id) {
                                destinationId = fa.destinationId
                                navOptions(fa.optionsBuilder)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun f(
        fragmentClass: KClass<out Fragment>,
        builder: (FragmentNavigatorDestinationBuilder.() -> Unit) = {},
    ): Int {
        id_counter++
        val id = id_counter
        val fd = FragmentDest(
            id, fragmentClass, builder
        )
        idToFragment[id] = fd
        return id
    }

    private inline fun <reified T : Fragment> f(
        noinline builder: (FragmentNavigatorDestinationBuilder.() -> Unit) = {},
    ): Int {
        return f(T::class, builder)
    }

    private fun a(
        fragmentId: Int,
        destinationId: Int,
        optionsBuilder: (NavOptionsBuilder.() -> Unit) = defaultNavOptionsBuilder
    ): Int {
        id_counter++
        val id = id_counter
        val fa = FragmentAction(
            id, fragmentId, destinationId, optionsBuilder
        )
        idToFragment[fragmentId]!!.actionList!!.add(fa)
        return id
    }

    private infix fun Int.to(that: Int): Int = a(this, that)


    private class FragmentDest(
        val id: Int,
        val fragmentClass: KClass<out Fragment>,
        val builder: FragmentNavigatorDestinationBuilder.() -> Unit,
        val actionList: ArrayList<FragmentAction> = arrayListOf(),
    )

    private class FragmentAction(
        val id: Int,
        val fragmentId: Int,
        val destinationId: Int,
        val optionsBuilder: NavOptionsBuilder.() -> Unit,

        )

    private class FragmentArgument(
        name: String,
        navType: NavArgument
    )

}