package cn.a10miaomiao.bilimiao.compose.pages.lyric

import android.app.Activity
import android.util.Base64
import android.view.View
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.pages.lyric.lib.KrcText
import cn.a10miaomiao.bilimiao.compose.pages.lyric.poup_menu.LyricOffsetPopupMenu
import cn.a10miaomiao.bilimiao.compose.pages.lyric.poup_menu.LyricSourcePopupMenu
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance
import kotlin.coroutines.coroutineContext

@Serializable
class LyricPage :ComposePage(){

    @Composable
    override fun Content() {
        val viewModel: LyricPageViewModel = diViewModel()
        LyricPageContent(viewModel)
    }
}

internal class LyricPageViewModel(
    override val di: DI,
): ViewModel(), DIAware{

    companion object{
        const val KUGOU = "ku"
        const val NETEASE = "net"
    }

    private val activity by instance<Activity>()
    private val playerDelegate by instance<BasePlayerDelegate>()

    var loadingSource = MutableStateFlow(false)
    var loadingLyric = MutableStateFlow(false)


    var lyricTitle= MutableStateFlow("")
    var author= MutableStateFlow("")
    var by= MutableStateFlow("")
    var lyric= MutableStateFlow(mutableStateListOf<LyricLine>())
    var offset= MutableStateFlow(0)

    var loadedSourceTitle=MutableStateFlow("\n")
    var source=MutableStateFlow(mutableStateListOf<LyricSource>())


    private val sourceMutex =Mutex()
    private val lyricMutex =Mutex()

    val playProgress = flow<Long> {
        while (coroutineContext.isActive) {
            emit(playerDelegate.currentPosition())
            delay(200)
        }
    }


    //加载过程中，一些提示信息放在歌词位置第一行
    fun setMessage(message:String){
        setLyric(mutableStateListOf(LyricLine(0,message)),"","","")
    }
    @Synchronized
    fun setLyric(list:MutableList<LyricLine>,title:String,author:String,by:String){
        this.lyricTitle.value=title
        this.author.value=author
        this.by.value=by
        this.lyric.value.clear()
        this.lyric.value.addAll(list)
    }
    @Synchronized
    fun addSource(list:MutableList<LyricSource>){
        source.value.addAll(list)
    }
    fun loadSource(videoTitle:String) = viewModelScope.launch(Dispatchers.IO){
        sourceMutex.withLock {
            source.value.clear()
            loadedSourceTitle.value=videoTitle
            if(videoTitle==""){
                setMessage("当前无视频播放")
            } else {
                loadingSource.value = true
                setMessage("正在加载歌词源...")
                val kugouRes=async{ loadSourceFromKugou(videoTitle) }
                val neteaseRes=async{ loadSourceFromNetease(videoTitle) }
                //优先读取酷狗歌词，因为有双语
                kugouRes.await()
                loadFirstLyricOf(KUGOU)
                neteaseRes.await()
                loadFirstLyricOf(NETEASE)

                loadingSource.value = false
            }
        }
    }
    suspend fun loadFirstLyricOf(type: String){
        source.value.forEachIndexed { index, lyricSource ->
            if(lyricSource.type == type){
                loadLyric(index,false)
                return
            }
        }
    }
    private suspend fun loadSourceFromNetease(videoTitle: String){
        try {
            val res = MiaoHttp.request {
                url ="https://music.163.com/api/search/get/web?csrf_token=hlpretag=&hlposttag=&s=$videoTitle&type=1&offset=0&total=true&limit=12"
            }.awaitCall().json<NeteaseSearchResultInfo>()
            if (res.code==200) {
                val addList= mutableListOf<LyricSource>()
                res.result.songs.forEach {
                    addList.add(LyricSource(it.name,it.id, NETEASE,it.duration))
                }
                addSource(addList)
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show("网易云歌词列表加载失败"+res.code.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show("网易云歌词列表加载失败")
            }
        }
    }

    private suspend fun loadSourceFromKugou(videoTitle: String){
        try {
            val res = MiaoHttp.request {
                url ="https://mobileservice.kugou.com/api/v3/lyric/search?version=9108&highlight=1&keyword=$videoTitle&plat=0&pagesize=12&area_code=1&page=1&with_res_tag=1"
            }.awaitCall().let{
                val jsonStr = it.body!!.string().replace("<!--.*?-->".toRegex(),"")
                MiaoJson.fromJson<KugouSearchResultInfo>(jsonStr)
            }
            if (res.errcode == 0) {
                val addList= mutableListOf<LyricSource>()
                res.data.info.forEach {
                    addList.add(LyricSource(it.filename,it.hash, KUGOU,it.duration*100))
                }
                addSource(addList)
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show("酷狗歌词列表加载失败"+res.error)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show("酷狗歌词列表加载失败")
            }
        }
    }
    private fun String.base64Decode():ByteArray{
        return Base64.decode(this,Base64.DEFAULT)
    }
    //酷狗歌词解密
    private fun String.decodeKrc():String{
        val base64DecodedBytes= base64Decode()
        val byteArray = base64DecodedBytes.copyOfRange(4,base64DecodedBytes.size)
        return KrcText().getKrcText(byteArray)
    }
    //krc双语部分解密
    private fun String.decodeKrcLanguage():String{
        val base64DecodedBytes= base64Decode()
        return String(base64DecodedBytes).decodeUnicode()
    }
    //酷狗歌词的双语部分，字符串格式需要unicode转义
    private fun String.decodeUnicode(): String {
        var str = ""
        this.split("\\u").forEach {
            if(it.length == 4){
                str += it.toIntOrNull(16)?.toChar() ?: it
            } else {
                val first4 = it.substring(0..3)
                val char = first4.toIntOrNull(16)?.toChar()
                if(char == null){
                    str += it
                } else {
                    str += (char + it.substring(4))
                }
            }
        }
        return str
    }

    suspend fun loadLyric(index:Int, force:Boolean = false){
        lyricMutex.withLock {
            loadingLyric.value = true
            if(source.value.isEmpty()){
                setMessage("当前无歌词源")
                loadingLyric.value = false
                return
            }
            if(!force && lyricTitle.value!=""){
                //已有歌词时默认不覆盖
                loadingLyric.value = false
                return
            }
            if(index !in source.value.indices){
                loadingLyric.value = false
                return
            }
            setMessage("正在加载歌词...")
            try {
                val src =source.value[index]
                var title=""
                var author=""
                var by=""
                val list= mutableListOf<LyricLine>()
                when (src.type) {
                    KUGOU -> {
                        val res1 = MiaoHttp.request {
                            url ="https://krcs.kugou.com/search?ver=1&man=yes&client=mobi&keyword=&duration=&hash=${src.code}&album_audio_id="
                        }.awaitCall().json<KugouAccessKeyItem>()
                        if(res1.errcode==200){
                            val res2 = MiaoHttp.request {
                                val can = res1.candidates[0]
                                url ="https://lyrics.kugou.com/download?ver=1&client=pc&id=${can.id}&accesskey=${can.accesskey}&fmt=krc&charset=utf8"
                            }.awaitCall().json<KugouLyricItem>()
                            if(res2.error_code==0){
                                val decoded = res2.content.decodeKrc()
                                var language:KugouLyricLanguage? = null
                                decoded.split('\n').forEach {
                                    val full=it.substringBefore(']').substringAfter('[')
                                    val body=it.substringAfter(']')
                                    if(full.contains(':')){
                                        val left=full.substringBefore(':')
                                        val right=full.substringAfter(':')
                                        if(left=="ti"){
                                            title=right
                                        } else if(left=="by"){
                                            by=right
                                        } else if(left=="ar"){
                                            author=right
                                        } else if(left=="language"){
                                            val k = right.decodeKrcLanguage()
                                            try {
                                                language = MiaoJson.fromJson<KugouLyricLanguage>(k)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        } else if(left=="offset"){
                                            val time = right.toIntOrNull()
                                            if(time!=null&&time!=0){
                                                //offset为0时不覆盖原有的
                                                offset.value=time
                                            }
                                        }
                                    } else if(full.contains(',')){
                                        val time=full.substringBefore(',').toIntOrNull()
                                        if(time!=null){
                                            val regex="<.*?>".toRegex()
                                            val text=body.replace(regex,"")
                                            //将双语部分拼接进去。正常此时已读取到双语部分。
                                            val subText = language?.let { lang ->
                                                var textString = ""
                                                //多种候选语言，last更容易是中文
                                                lang.content.last().lyricContent[list.size].forEach { str ->
                                                    //酷狗的逐字歌词，直接拼接起来
                                                    textString += str
                                                }
                                                textString.ifEmpty { null }
                                            }
                                            list.add(LyricLine(time.toLong(),text,subText))
                                        }
                                    }
                                }

                            } else {
                                PopTip.show(res2.info)
                                list.add(LyricLine(0,"歌词详情获取失败"))
                            }
                        } else {
                            PopTip.show(res1.errmsg)
                            list.add(LyricLine(0,"歌词详情获取失败"))
                        }
                    }
                    NETEASE -> {
                        val res = MiaoHttp.request {
                            url ="https://music.163.com/api/song/media?id=${src.code}"
                        }.awaitCall().json<NeteaseLyricItem>()
                        if (res.code==200) {
                            res.lyric.split('\n').forEach {
                                val left=it.substringBefore(':').substringAfter('[')
                                var right=it.substringBefore(']').substringAfter(':')
                                val body=it.substringAfter(']')
                                if(left=="title"){
                                    title=right
                                } else if(left=="by"){
                                    by=right
                                } else if(left=="author"){
                                    author=right
                                } else if(left=="offset"){
                                    val time = right.toIntOrNull()
                                    if(time!=null&&time!=0){
                                        //offset为0时不覆盖原有的
                                        offset.value=time
                                    }
                                } else {
                                    if(right.contains(':')){
                                        right = right.replace(':','.')
                                    }
                                    val minute=left.toIntOrNull()
                                    val second=right.toFloatOrNull()
                                    if(minute!=null&&second!=null){
                                        val time=minute*60000+(second*1000).toLong()
                                        list.add(LyricLine(time,body))
                                    }
                                }
                            }
                        } else {
                            PopTip.show("网易云歌词获取失败：${res.code}")
                            list.add(LyricLine(0,"歌词详情获取失败"))
                        }
                    }
                    else -> {
                        list.add(LyricLine(0,"歌词未知类型：${src.type}"))
                    }
                }
                if(list.isEmpty()){
                    list.add(LyricLine(0,"歌词内容为空"))
                }
                if(title==""){
                    //没读取到标题的，自动填充
                    title=src.name
                }
                setLyric(list, title, author, by)
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    PopTip.show("歌词详情获取失败")
                    setMessage("歌词详情获取失败")
                }
            } finally {
                loadingLyric.value = false
            }
        }
    }

    fun menuItemClick(view: View, menuItem: MenuItemPropInfo){
        when (menuItem.key) {
            1 -> {
                if(source.value.isEmpty()){
                    PopTip.show("无歌词源")
                } else {
                    LyricSourcePopupMenu(activity,this).show(view)
                }
            }
            2 -> {
                LyricOffsetPopupMenu(activity,this).show(view)
            }
        }
    }
}
@Preview
@Composable
fun Preview(){
    val test1= LyricLine(1000,"第一行","first line")
    val test2= LyricLine(2000,"第二行","second line")
    val test3= LyricLine(3000,"第3行")
    val lyric = MutableStateFlow<List<LyricLine>>(listOf(test1,test2,test3)).collectAsState().value
    LazyColumn(){
        items(lyric){
            LyricLineItem(Color(0xffffffff),alpha = 1f, line = it)
        }
    }
}





@Composable
internal fun LyricPageContent(viewModel: LyricPageViewModel){

    val playerStore: PlayerStore by rememberInstance()
    val windowStore: WindowStore by rememberInstance()
    val playerState = playerStore.stateFlow.collectAsState().value
    val windowState = windowStore.stateFlow.collectAsState().value
    val lyric = viewModel.lyric.collectAsState().value
    val offset = viewModel.offset.collectAsState().value
    val title = viewModel.lyricTitle.collectAsState().value
    val playProgress = viewModel.playProgress.collectAsState(initial = 0).value
    val focusOn = remember (playProgress,lyric,offset){
        derivedStateOf{
            var focused = -100
            for (item in lyric) {
                if (item.startTime + offset > playProgress) {
                    focused = lyric.indexOf(item) - 1
                    break
                }
            }
            if(focused == -100){
                focused = lyric.size - 1
            }
            focused
        }.value
    }
    val scrollState= rememberLazyListState()
    val conf = LocalConfiguration.current
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    val spacerHeight = remember(canvasHeight,windowState.contentInsets.bottom){
        derivedStateOf {
            ((canvasHeight-windowState.contentInsets.bottom)/2*160/conf.densityDpi).toInt()-50
        }
    }
    LaunchedEffect(playerState.mainTitle){
        val videoTitle=playerState.mainTitle
            .replace("【.*?】".toRegex(),"")
            .replace("\\(.*?\\)".toRegex(),"") //过滤掉视频名中的括号，搜索更精准
            .substringBefore("feat.")
            .substringBeforeLast('/') //过滤掉作者信息
        if(videoTitle!=viewModel.loadedSourceTitle.value){
            viewModel.loadSource(videoTitle)
        }
    }
    LaunchedEffect(key1 = focusOn,spacerHeight){
        scrollState.animateScrollToItem(focusOn+1,-spacerHeight.value*conf.densityDpi/160)
    }
    Box{
        Canvas(modifier = Modifier.fillMaxSize()){
            canvasHeight = size.height
        }
        LazyColumn(state = scrollState) {
            item {
                Spacer(modifier = Modifier.height(spacerHeight.value.dp))
            }
            items(lyric) {
                if (focusOn == lyric.indexOf(it)) {
                    //当前播放
                    LyricLineItem(color = MaterialTheme.colorScheme.onSurface, alpha = 1f, line = it)
                } else {
                    //其他歌词
                    LyricLineItem(color = MaterialTheme.colorScheme.onSurface, alpha = 0.5f, line = it)
                }
            }
            item {
                Spacer(modifier = Modifier.height(spacerHeight.value.dp + windowState.contentInsets.bottomDp.dp))
            }
        }
    }
    val pageConfigId = PageConfig(
        title = "歌词-"+title.let {
            if(it==""){
                "暂无歌词"
            } else {
                if(it.length>15) {
                    it.take(13) + "..."
                }
                else {
                    it
                }
            }
        },
        menu = remember(offset) {
            myMenu {
                myItem {
                    key = 1
                    this.title = "歌词源"
                    iconFileName = "ic_more_vert_grey_24dp"
                }
                myItem {
                    key = 2
                    this.title = if(offset==0) {
                        "延迟"
                    }else if (offset>0) {
                        '+' + String.format("%.1f",offset/1000f) +'s'
                    }else {
                        String.format("%.1f",offset/1000f) + 's'
                    }
                    iconFileName = "ic_history_gray_24dp"
                }
            }
        }
    )
    PageListener(
        pageConfigId,
        onMenuItemClick = viewModel::menuItemClick,
    )
}

@Composable
internal fun LyricLineItem(color: Color, alpha: Float, line: LyricLine){
    Box(modifier = Modifier
        .height(100.dp)
        .wrapContentHeight(Alignment.CenterVertically)
    ) {
        if (line.subText == null) {
            MainTextItem(color = color, alpha = alpha, text = line.mainText)
        } else {
            Column {
                MainTextItem(color = color, alpha = alpha, text = line.mainText)
                SubTextItem(color = color, alpha = alpha, text = line.subText)
            }
        }
    }
}

@Composable fun MainTextItem(color: Color, alpha: Float, text:String){
    Text(
        text = text,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
    )
}
@Composable fun SubTextItem(color: Color, alpha: Float, text:String){
    Text(
        text = text,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        fontSize = 16.sp,
        textAlign = TextAlign.Center,
    )
}
internal class LyricLine(
    val startTime:Long,
    val mainText:String,
    val subText:String?=null,
)

internal class LyricSource(
    val name:String,
    val code:String,
    val type:String,
    val duration: Int,
)