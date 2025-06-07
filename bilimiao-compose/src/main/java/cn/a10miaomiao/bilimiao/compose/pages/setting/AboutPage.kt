package cn.a10miaomiao.bilimiao.compose.pages.setting

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.layout.DoubleColumnAutofitLayout
import cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable.rememberChainScrollableLayoutState
import cn.a10miaomiao.bilimiao.compose.pages.TestPage
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoAdInfo
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.store.WindowStore
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance


@Serializable
class AboutPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: AboutPageViewModel = diViewModel()
        AboutPageContent(viewModel)
    }
}

private sealed class AppVersionState {

    data object None: AppVersionState()

    data object Checking: AppVersionState()

    data class Fail(
        val message: String,
    ): AppVersionState()

    data class HasUpdate(
        val version: String,
        val url: String,
    ): AppVersionState()

    data object NotUpdate: AppVersionState()
}

private class AboutPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val pageNavigation by instance<PageNavigation>()

    val applicationIcon: Drawable = with(fragment.requireActivity()) {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        applicationInfo.loadIcon(packageManager)
    }

    val versionName: String = with(fragment.requireActivity()) {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName
    }

    val versionCode: Long = with(fragment.requireActivity()) {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
    }

    val contributorsList = MutableStateFlow(listOf<ContributorInfo>())

    val versionState = MutableStateFlow<AppVersionState>(AppVersionState.None)

    init {
        viewModelScope.launch {
            runCatching {
                getGithubContributors()
//                getGiteeContributors()
                checkUpdate()
            }.onFailure {

            }
        }
    }

    suspend fun getGithubContributors() {
        try {
            val data = MiaoHttp.request {
                url = "https://api.github.com/repos/10miaomiao/bilimiao2/contributors"
            }.awaitCall().json<List<GithubContributorInfo>>()
            contributorsList.value = data.map {
                ContributorInfo(
                    email = it.html_url,
                    name = it.login,
                    contributions = it.contributions,
                    avatar_url = it.avatar_url,
                )
            }
        } catch (e: Exception) {
            // 如果请求失败且无缓存，则改用gitee
            getGiteeContributors()
        }
    }

    suspend fun getGiteeContributors() {
        val data = MiaoHttp.request {
            url = "https://gitee.com/api/v5/repos/10miaomiao/bilimiao2/contributors"
        }.awaitCall().json<List<ContributorInfo>>()
        val list = mutableListOf<ContributorInfo>()
        // 合并email或name相同的贡献者
        data.forEach {
            val name = it.name
            val email = it.email
            val contributions = it.contributions
            val i = list.indexOfFirst {
                name == it.name || email == it.email
            }
            if (i == -1) {
                list.add(
                    ContributorInfo(
                        name = name,
                        email = email,
                        contributions = contributions,
                        avatar_url = null,
                    )
                )
            } else {
                val item = list[i]
                list[i] = item.copy(
                    contributions = contributions + item.contributions
                )
            }
        }
        contributorsList.value = list
    }

    /**
     * 检测更新
     */
     fun checkUpdate() = viewModelScope.launch(Dispatchers.IO) {
        try {
            versionState.value = AppVersionState.Checking
            val url = "https://bilimiao.10miaomiao.cn/miao/init?v=$versionCode"
            val res = MiaoHttp.request(url).call().json<MiaoAdInfo>()
            if (res.code == 0) {
                val versionInfo = res.data.version
                versionState.value = if (versionInfo.versionCode > versionCode) {
                    AppVersionState.HasUpdate(
                        version = versionInfo.versionName,
                        url = versionInfo.url,
                    )
                } else {
                    AppVersionState.NotUpdate
                }
            } else {
                PopTip.show(res.msg)
                versionState.value = AppVersionState.Fail(
                    message = res.msg
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show("网络异常，检测失败")
            versionState.value = AppVersionState.Fail(
                message = e.message ?: e.toString()
            )
        }
    }

    fun openUrl(url: String) {
        val urlRegex = """^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$""".toRegex()
        if (urlRegex.matches(url)) {
            BiliUrlMatcher.toUrlLink(
                fragment.requireActivity(),
                url,
            )
            return
        }
        runCatching {
            val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()
            val intent = Intent(Intent.ACTION_VIEW)
            val activity = fragment.requireActivity()
            if (emailRegex.matches(url)) {
                intent.data = Uri.parse("mailto:$url")
            } else {
                intent.data = Uri.parse(url)
            }
            activity.startActivity(intent)
        }.onFailure {
            PopTip.show("打开失败")
        }
    }

    fun openUpdateUrl() {
        val version = versionState.value
        if (version is AppVersionState.HasUpdate) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(version.url)
                fragment.requireActivity().startActivity(intent)
            } catch (e: Exception) {
                PopTip.show("打开失败:" + version.url)
            }
        } else {
            PopTip.show("没有更新")
        }
    }

    fun toTestPage() {
        pageNavigation.navigate(TestPage())
    }

    @Serializable
    data class ContributorInfo(
        val email: String,
        val name: String,
        val contributions: Int,
        val avatar_url: String?,
    )

    @Serializable
    data class GithubContributorInfo(
        val login: String,
        val html_url: String,
        val contributions: Int,
        val avatar_url: String,
    )
}

private const val WARN_TEXT = """1、本程序为哔哩哔哩动画的第三方APP，资源均来自哔哩哔哩动画(bilibili.com)
2、如果侵犯您的合法权益，请及时联系本人以第一时间删除"""
private const val MY_WEBSITE_URL = "https://10miaomiao.cn"
private const val GITHUB_PROJECT_URL = "https://github.com/10miaomiao/bilimiao2"
private const val GITEE_PROJECT_URL = "https://gitee.com/10miaomiao/bilimiao2"
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun AboutPageContent(
    viewModel: AboutPageViewModel
) {
    PageConfig(
        title = "关于bilimiao"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val chainScrollableLayoutState = rememberChainScrollableLayoutState(
        maxScrollPosition = 340.dp,
    )
    val listState = rememberLazyListState()
    val contributionsList = viewModel.contributorsList.collectAsState()

    DoubleColumnAutofitLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        innerPadding = windowInsets.addPaddingValues(
            addBottom = windowStore.bottomAppBarHeightDp.dp,
        ),
        chainScrollableLayoutState = chainScrollableLayoutState,
        leftMaxWidth = 600.dp,
        leftMaxHeight = 340.dp,
        leftContent = { _, innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = rememberDrawablePainter(viewModel.applicationIcon),
                    contentDescription = "app icon",
                    modifier = Modifier
                        .size(100.dp, 100.dp)
                        .padding(8.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    viewModel.toTestPage()
                                }
                            )
                        }
                )
                Text(
                    text = "bilimiao",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "哔哩喵~",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(24.dp))
                val versionState = viewModel.versionState.collectAsState().value
                when (versionState) {
                    AppVersionState.None -> {
                        TextButton(
                            onClick = viewModel::checkUpdate,
                        ) {
                            Text(text = "检测更新")
                        }
                    }
                    AppVersionState.Checking -> {
                        TextButton(
                            onClick = {},
                            enabled = false
                        ) {
                            Text(text = "检测中")
                        }
                    }
                    AppVersionState.NotUpdate -> {
                        TextButton(
                            onClick = viewModel::checkUpdate,
                        ) {
                            Text(text = "已是最新版本")
                        }
                    }
                    is AppVersionState.HasUpdate -> {
                        TextButton(
                            onClick = viewModel::openUpdateUrl,
                        ) {
                            Text(text = "有新版本：" + versionState.version)
                        }
                    }
                    is AppVersionState.Fail -> {
                        TextButton(
                            onClick = viewModel::checkUpdate,
                        ) {
                            Text(text = "检测更新失败")
                        }
                    }
                }
                Text(
                    text = "当前版本：" + viewModel.versionName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    ) { _, innerPadding ->
        ProvidePreferenceLocals {
            LazyColumn(
                contentPadding = innerPadding,
                state = listState,
            ) {
                preferenceCategory(
                    key = "me",
                    title = {
                        Text("基本信息")
                    }
                )
                preference(
                    key = "author",
                    modifier = Modifier.itemStyle(),
                    title = {
                        Text("作者")
                    },
                    summary = {
                        Text("10喵喵")
                    },
                    onClick = {
                        viewModel.openUrl(MY_WEBSITE_URL)
                    }
                )
                preference(
                    key = "warn",
                    modifier = Modifier.itemStyle(),
                    title = {
                        Text("使用声明")
                    },
                    summary = {
                        Text(WARN_TEXT)
                    }
                )
                preferenceCategory(
                    key = "url",
                    title = {
                        Text("开源链接")
                    }
                )
                preference(
                    key = "github",
                    modifier = Modifier.itemStyle(),
                    title = {
                        Text("Github")
                    },
                    summary = {
                        Text("github.com/10miaomiao/bilimiao2")
                    },
                    onClick = {
                        viewModel.openUrl(GITHUB_PROJECT_URL)
                    }
                )
                preference(
                    key = "gitee",
                    modifier = Modifier.itemStyle(),
                    title = {
                        Text("Gitee")
                    },
                    summary = {
                        Text("gitee.com/10miaomiao/bilimiao2")
                    },
                    onClick = {
                        viewModel.openUrl(GITEE_PROJECT_URL)
                    }
                )
                preferenceCategory(
                    key = "contributors",
                    title = {
                        Text("贡献者")
                    }
                )

                contributionsList.value.forEach {
                    if (it.name == "10miaomiao") {
                        return@forEach
                    }
                    preference(
                        key = "contributors.${it.name}",
                        modifier = Modifier.itemStyle(),
                        icon = it.avatar_url?.let { avatarUrl ->
                            {
                                GlideImage(
                                    model = avatarUrl,
                                    contentDescription = null,
                                    loading = placeholder(R.drawable.bili_akari_img),
                                    failure = placeholder(R.drawable.bili_akari_img),
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                        },
                        title = {
                            Text(it.name)
                        },
                        summary = {
                            Text(it.email)
                        },
                        onClick = {
                            viewModel.openUrl(it.email)
                        }
                    )
                }

            }
        }
    }
}


private fun Modifier.itemStyle() = composed {
    this
        .fillMaxSize()
        .padding(
            vertical = 4.dp,
            horizontal = 8.dp,
        )
        .background(
            MaterialTheme.colorScheme.surfaceContainer,
            RoundedCornerShape(10.dp)
        )
        .clip(
            RoundedCornerShape(10.dp)
        )
}
