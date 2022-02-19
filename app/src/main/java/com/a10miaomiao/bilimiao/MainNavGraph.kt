package com.a10miaomiao.bilimiao

import androidx.fragment.app.Fragment
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoPageInfo
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
import com.a10miaomiao.bilimiao.page.user.UserArchiveListFragment
import com.a10miaomiao.bilimiao.page.user.UserFragment
import com.a10miaomiao.bilimiao.page.user.favourite.UserFavouriteDetailFragment
import com.a10miaomiao.bilimiao.page.user.favourite.UserFavouriteListFragment
import com.a10miaomiao.bilimiao.page.video.VideoAddFavoriteFragment
import com.a10miaomiao.bilimiao.page.video.VideoCoinFragment
import com.a10miaomiao.bilimiao.page.video.VideoPagesFragment
import com.a10miaomiao.bilimiao.page.video.comment.VideoCommentDetailFragment
import com.a10miaomiao.bilimiao.page.video.comment.VideoCommentListFragment
import kotlin.reflect.KClass


object MainNavGraph {
    // Counter for id's. First ID will be 1.
    private var id_counter = 1
    private val globalActionList = arrayListOf<FragmentAction>()
    private val idToFragment = hashMapOf<Int, FragmentDest>()
    private var id = id_counter++

    object dest {
        val id = id_counter++
        val global = id_counter++
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
        val videoPages = f<VideoPagesFragment> {
            deepLink("bilimiao://video/pages")
            argument(args.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(args.pages) {
                type = NavType.ParcelableType(VideoPageInfo::class.java)
                nullable = false
            }
            argument(args.index) {
                type = NavType.IntType
                nullable = false
            }
        }
        val videoCoin = f<VideoCoinFragment> {
            argument(args.num) {
                type = NavType.IntType
                defaultValue = 1
            }
        }
        val videoAddFavorite = f<VideoAddFavoriteFragment> {

        }

        val videoCommentList = f<VideoCommentListFragment> {
            argument(args.id) {
                type = NavType.StringType
                nullable = false
            }
        }
        val videoCommentDetail = f<VideoCommentDetailFragment> {
            argument(args.reply) {
                type = NavType.ParcelableType(VideoCommentReplyInfo::class.java)
                nullable = false
            }
        }

        val h5Login = f<H5LoginFragment> {
        }

        val user = f<UserFragment> {
            argument(args.type) {
                type = NavType.StringType
                defaultValue = "AV"
            }
        }
        val userArchiveList = f<UserArchiveListFragment> {
            argument(args.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(args.name) {
                type = NavType.StringType
                nullable = false
            }
        }
        val userFavouriteList = f<UserFavouriteListFragment> {
            argument(args.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(args.name) {
                type = NavType.StringType
                nullable = false
            }
        }
        val userFavouriteDetail = f<UserFavouriteDetailFragment> {
            argument(args.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(args.name) {
                type = NavType.StringType
                nullable = false
            }
        }
    }

    object action {
        val id = id_counter++
        val global_to_videoPages = dest.global to dest.videoPages
        val global_to_videoCoin = dest.global to dest.videoCoin
        val global_to_videoAddFavorite = dest.global to dest.videoAddFavorite

        val home_to_region = dest.home to dest.region
        val home_to_setting = dest.home to dest.setting
        val home_to_h5Login = dest.home to dest.h5Login
        val home_to_user = dest.home to dest.user

        val setting_to_videoSetting = dest.setting to dest.videoSetting
        val setting_to_danmakuSetting = dest.setting to dest.danmakuSetting
        val setting_to_about = dest.setting to dest.about

        val region_to_videoInfo = dest.region to dest.videoInfo

        val videoInfo_to_videoInfo = dest.videoInfo to dest.videoInfo
        val videoInfo_to_user = dest.videoInfo to dest.user
        val videoInfo_to_videoCommentList = dest.videoInfo to dest.videoCommentList

        val videoCommentList_to_user = dest.videoCommentList to dest.user
        val videoCommentList_to_videoInfo = dest.videoCommentList to dest.videoInfo
        val videoCommentList_to_videoCommentDetail = dest.videoCommentList to dest.videoCommentDetail

        val videoCommentDetail_to_user = dest.videoCommentDetail to dest.user
        val videoCommentDetail_to_videoInfo = dest.videoCommentDetail to dest.videoInfo

        val user_to_videoInfo = dest.user to dest.videoInfo
        val user_to_userArchiveList = dest.user to dest.userArchiveList
        val user_to_userFavouriteList = dest.user to dest.userFavouriteList
        val user_to_userFavouriteDetail = dest.user to dest.userFavouriteDetail

        val userArchiveList_to_videoInfo = dest.userArchiveList to dest.videoInfo

        val userFavouriteList_to_userFavouriteDetail = dest.userFavouriteList to dest.userFavouriteDetail

        val userFavouriteDetail_to_videoInfo = dest.userFavouriteDetail to dest.videoInfo
    }

    object args {
        const val type = "type"
        const val id = "id"
        const val name = "name"
        const val num = "num"
        const val pages = "pages"
        const val index = "index"
        const val region = "region"
        const val reply = "reply"
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
            globalActionList.forEach { fa ->
                action(fa.id) {
                    destinationId = fa.destinationId
                    navOptions(fa.optionsBuilder)
                }
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
        val actionList = idToFragment[fragmentId]?.actionList ?: globalActionList
        actionList.add(fa)
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