package com.a10miaomiao.bilimiao.comm.delegate.player.source

import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.editPreferences
import com.a10miaomiao.bilimiao.comm.datastore.mapPreferences
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.proxy.BiliUposInfo
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 一个 DASH 流（视频或音频）在 B 站 playurl 里下发的地址集合
 */
class SourceUrlSet(
    val baseUrl: String,
    val backupUrls: List<String> = emptyList(),
)

/**
 * 一次播放实际使用的流地址
 *
 * [audioUrl] 为 null 表示没有独立音频（或音频线路无从判断），由调用方沿用原始地址。
 */
class ResolvedSourceUrls(
    val videoUrl: String,
    val audioUrl: String?,
)

/**
 * 智能换源结果
 */
class SmartSourceResult(
    /** 选中的视频地址（带正确的签名参数，可直接播放） */
    val videoUrl: String,
    /** 同一条线路上的音频地址；音频流没有这条线路的地址时为 null */
    val audioUrl: String?,
    /** 选中的线路 host */
    val host: String,
)

/**
 * 智能换源：在 B 站下发的若干条视频线路里，实测挑一条较快的
 *
 * ## 依据
 *
 * 同一个分片文件（如 `42231991605-1-30102.m4s`）在所有 CDN 线路上的字节完全相同，
 * 只有签名参数不同，所以拿每条线路各下前几百 KB 比一比耗时，就能判断哪条线路更快。
 * 这正是 bilibili-accelerator 那个浏览器脚本的核心思路（`src/core/routing.js`）。
 *
 * ## 与浏览器脚本的差别
 *
 * 浏览器脚本挂在 MSE 上，能一边播一边看实时下载速度，卡了随时换线路；原生播放器
 * （安卓 ExoPlayer / 桌面 mpv）没有这个钩子，播放中换线路必须重载媒体源，会黑屏重连。
 * 因此这里只在**播放前**选一次线路，不做播放中的动态切换：
 *
 * - [SettingConstants.PLAYER_SMART_SOURCE_BEFORE_PLAY]：起播前把所有候选测一遍，
 *   直接用当场实测最快的那条；
 * - [SettingConstants.PLAYER_SMART_SOURCE_BACKGROUND]（默认）：起播不等待，用历史
 *   实测结果选线路，同时后台重测刷新结果供下次使用。没测过任何线路时保持原样，
 *   符合「播放正常时不介入」的取向。
 *
 * 用户手动指定了 upos 服务器（「区域限制」设置）时完全不介入：那是明确的人工选择。
 */
object SmartSourceSelector {

    /**
     * 每条线路每次测速下载的字节数
     *
     * 与插件一致：大到足以让中转边缘节点露出真实速度（1080P 也就 0.2~1 MB/s，
     * 即不到一秒的播放量），小到整个测速能在 1 秒内跑完。
     */
    private const val PROBE_BYTES = 768 * 1024

    /**
     * 单条线路的测速超时
     *
     * 起播前测速是要让用户等的，所以比插件的 4 秒取得更紧：这点数据 2.5 秒还下不完，
     * 这条线路本来也不该被选中。
     */
    private const val PROBE_TIMEOUT_MS = 2500L

    /**
     * 单轮同时测速的线路数上限
     *
     * 并发越多，各条线路互相抢带宽越厉害，测出来的相对快慢反而失真。4 条足以覆盖
     * 「B 站下发了 2~4 条线路」这一最常见的情况。
     */
    private const val MAX_PROBES = 4

    /** 后台模式下同一条线路的最短重测间隔：结论要留到下次播放才生效，不必每次起播都重测 */
    private const val PROBE_INTERVAL_MS = 10L * 60 * 1000

    /** 历史分的有效期（3 天）。线路快慢随地区和运营商变，太旧的结果不作数 */
    private const val HISTORY_MAX_AGE_MS = 3L * 24 * 60 * 60 * 1000

    /** 计入历史分的最小样本量：几个字节就断掉的请求算不出速度，记进去只会污染分数 */
    private const val MIN_SAMPLE_BYTES = 16 * 1024

    /** UPOS 镜像的 host 形如 `upos-sz-mirrorali.bilivideo.com`，彼此之间签名通用 */
    private val uposHostRegex = Regex("^upos-[a-z0-9-]+\\.bilivideo\\.com$")

    /** 后台测速用的进程级作用域：结论只写给下一次播放，不阻塞当前起播 */
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 历史分的串行化锁（起播协程与后台测速协程都会读写） */
    private val scoresLock = Mutex()

    /** 历史分的内存副本，首次使用时从 DataStore 载入 */
    @Volatile
    private var scores: MutableMap<String, Double>? = null

    /** 各线路上次实测时间，用于后台模式的节流 */
    private val lastProbeAt = ConcurrentHashMap<String, Long>()

    /**
     * 本模块日志
     *
     * 换源的决策过程（有哪些候选、各测多久、最后选了谁）一律用 info：一是每次起播
     * 只打一次、频率低；二是桌面端 [MiaoLogger] 的 d() 走 java.util.logging 的 FINE，
     * 默认配置下根本不输出，用 d() 就没法在桌面端验证换源到底有没有生效。
     */
    private val logger = miaoLogger()

    /**
     * 测速专用客户端
     *
     * 不复用 [com.a10miaomiao.bilimiao.comm.network.MiaoHttp]：它每发一个请求就新建一个
     * OkHttpClient（连接池、线程池全新建），而测速恰恰是并发短请求，复用连接池能明显
     * 降低测量噪声；同时避免 MiaoHttp 默认附带的 Cookie / buvid 头干扰 CDN 对签名的校验。
     */
    private val speedClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(PROBE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(PROBE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .callTimeout(PROBE_TIMEOUT_MS * 2, TimeUnit.MILLISECONDS)
            .build()
    }

    /**
     * 选出本次播放要用的线路，没有更好的选择时返回 null（调用方沿用原始地址）
     *
     * @param video 视频流的候选地址
     * @param audio 音频流的候选地址，无独立音频时传 null
     * @param headers 播放用的请求头（Referer / User-Agent），CDN 靠它校验
     */
    suspend fun select(
        video: SourceUrlSet,
        audio: SourceUrlSet?,
        headers: Map<String, String>,
    ): SmartSourceResult? {
        val mode = currentMode()
        if (mode == SettingConstants.PLAYER_SMART_SOURCE_OFF) return null

        val candidates = buildCandidates(video)
        // 只有一条线路就无从选择
        if (candidates.size < 2) {
            logger.i(
                "智能换源跳过：没有其他可选线路",
                "baseHost" to hostOf(video.baseUrl),
                "candidates" to candidates.size,
            )
            return null
        }
        logger.i(
            "智能换源开始",
            "mode" to modeName(mode),
            "baseHost" to hostOf(video.baseUrl),
            "candidates" to candidates.joinToString { it.host + if (it.issued) "" else "(镜像)" },
        )

        val history = loadScores()
        val chosen: Pair<String, String> =
            if (mode == SettingConstants.PLAYER_SMART_SOURCE_BEFORE_PLAY) {
                val probes = probeAll(pickProbes(candidates, history), headers)
                if (probes.isEmpty()) {
                    logger.i("智能换源跳过：所有线路测速都失败")
                    return null
                }
                record(probes)
                val fastest = probes.minByOrNull { it.ms } ?: return null
                fastest.host to fastest.url
            } else {
                // 没有历史分的线路不参与选择：不做没有依据的猜测。
                // 但历史分正是后台测速攒出来的，所以无论选不选得出来都得先把它排上，
                // 否则第一条视频永远没有历史分，这个模式就永远不会生效。
                probeInBackground(candidates, headers)
                val best = candidates
                    .filter { history.containsKey(it.host) }
                    .maxByOrNull { history.getValue(it.host) }
                if (best == null) {
                    logger.i(
                        "智能换源保持原线路：还没有任何线路的历史测速结果",
                        "history" to history.map { "${it.key}=${it.value.toInt()}Mbps" }.joinToString(),
                    )
                    return null
                }
                best.host to best.url
            }
        val (chosenHost, chosenUrl) = chosen

        // 选中的就是 B 站下发的原地址，没有改写的必要
        if (chosenUrl == video.baseUrl) {
            logger.i("智能换源保持原线路：它就是本次最快的", "host" to chosenHost)
            return null
        }

        logger.i("智能换源选中线路", "host" to chosenHost, "mode" to modeName(mode))
        return SmartSourceResult(
            videoUrl = chosenUrl,
            audioUrl = audio?.let { resolveAudioUrl(it, chosenHost) },
            host = chosenHost,
        )
    }

    private fun modeName(mode: Int): String = when (mode) {
        SettingConstants.PLAYER_SMART_SOURCE_OFF -> "关闭"
        SettingConstants.PLAYER_SMART_SOURCE_BEFORE_PLAY -> "起播前测速"
        else -> "后台测速"
    }

    /**
     * 应用手动指定的 upos 服务器；没指定时交给 [select]
     *
     * 手动指定优先于智能换源：用户已经明确选了线路，就不该再被自动改掉。
     */
    suspend fun resolve(
        video: SourceUrlSet,
        audio: SourceUrlSet?,
        headers: Map<String, String>,
        uposHost: String,
    ): ResolvedSourceUrls {
        if (uposHost.isNotBlank()) {
            return ResolvedSourceUrls(
                videoUrl = UrlUtil.replaceHost(video.baseUrl, uposHost),
                audioUrl = audio?.let { UrlUtil.replaceHost(it.baseUrl, uposHost) },
            )
        }
        val smart = select(video, audio, headers)
        return ResolvedSourceUrls(
            videoUrl = smart?.videoUrl ?: video.baseUrl,
            audioUrl = smart?.audioUrl ?: audio?.baseUrl,
        )
    }

    private suspend fun currentMode(): Int {
        return try {
            SettingPreferences.mapPreferences { it[SettingPreferences.PlayerSmartSourceMode] }
                ?: SettingConstants.PLAYER_SMART_SOURCE_DEFAULT
        } catch (e: Exception) {
            SettingConstants.PLAYER_SMART_SOURCE_DEFAULT
        }
    }

    /** 一条候选线路：host + 该 host 上这条流的完整地址 */
    private class HostUrl(val host: String, val url: String, val issued: Boolean)

    /**
     * 收集一条流的全部候选线路
     *
     * 两类来源：
     * 1. B 站下发的地址（`base_url` + `backup_url`）—— 签名本来就是给这些 host 签的，原样可用；
     * 2. UPOS 镜像池 —— 镜像之间签名通用，可以把下发地址的 host 换掉。仅当原来的 host
     *    也是 UPOS 镜像时才这么做；Akamai 只认给自己下发的地址，换 host 会被拒。
     */
    private fun buildCandidates(set: SourceUrlSet): List<HostUrl> {
        val byHost = LinkedHashMap<String, HostUrl>()
        (listOf(set.baseUrl) + set.backupUrls).forEach { url ->
            if (url.isBlank()) return@forEach
            val host = hostOf(url)
            if (host.isNotEmpty() && !byHost.containsKey(host)) {
                byHost[host] = HostUrl(host, url, issued = true)
            }
        }
        if (isUposHost(hostOf(set.baseUrl))) {
            BiliUposInfo.PRESETS.forEach { mirror ->
                val host = mirror.host
                // 池子里只有 upos-*.bilivideo.com 之间签名通用。Akamai 也在池中（供设置页
                // 手动选择），但它只认给自己下发的地址，这里必须按 host 形态再筛一道。
                if (host.isNotEmpty() && isUposHost(host) && !byHost.containsKey(host)) {
                    byHost[host] = HostUrl(host, UrlUtil.replaceHost(set.baseUrl, host), issued = false)
                }
            }
        }
        return byHost.values.toList()
    }

    /**
     * 挑出参与测速的线路
     *
     * B 站下发的地址先占位（它们是唯一带原生签名的），剩余名额按历史分给镜像，
     * 让「上次测出来快的」优先获得复测机会。
     */
    private fun pickProbes(candidates: List<HostUrl>, history: Map<String, Double>): List<HostUrl> {
        if (candidates.size <= MAX_PROBES) return candidates
        val issued = candidates.filter { it.issued }
        val mirrors = candidates.filterNot { it.issued }
            .sortedByDescending { history[it.host] ?: 0.0 }
        return (issued + mirrors).take(MAX_PROBES)
    }

    /** 音频流在选中线路上的地址 */
    private fun resolveAudioUrl(audio: SourceUrlSet, host: String): String? {
        buildCandidates(audio).firstOrNull { it.host == host }?.let { return it.url }
        // 音频流没有下发这条线路的地址时，用 UPOS 镜像替换（镜像之间签名通用）。
        // 选中的是非 UPOS 的下发 host 时不替换：改用原始地址，避免签名不匹配。
        return if (isUposHost(hostOf(audio.baseUrl)) && isUposHost(host)) {
            UrlUtil.replaceHost(audio.baseUrl, host)
        } else {
            null
        }
    }

    private class Probe(val host: String, val url: String, val ms: Long, val bytes: Int)

    private suspend fun probeAll(
        candidates: List<HostUrl>,
        headers: Map<String, String>,
    ): List<Probe> = coroutineScope {
        candidates
            .map { candidate -> async { probe(candidate, headers) } }
            .mapNotNull { it.await() }
    }

    /**
     * 实测一条线路：只下前 [PROBE_BYTES] 个字节，返回耗时
     *
     * 耗时从请求发出算到最后一块数据到达，首字节的等待也算在内 —— 播放器每取一个
     * 分片都要付一次这笔开销，忽略它就等于低估了高延迟线路的代价。
     */
    private suspend fun probe(
        candidate: HostUrl,
        headers: Map<String, String>,
    ): Probe? = withContext(Dispatchers.IO) {
        val startedAt = System.nanoTime()
        try {
            val request = Request.Builder()
                .url(candidate.url)
                .header("Range", "bytes=0-${PROBE_BYTES - 1}")
                .apply { headers.forEach { (name, value) -> header(name, value) } }
                .build()
            speedClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val stream = response.body?.byteStream() ?: return@withContext null
                val buffer = ByteArray(64 * 1024)
                var total = 0
                while (total < PROBE_BYTES) {
                    val read = stream.read(buffer, 0, minOf(buffer.size, PROBE_BYTES - total))
                    if (read <= 0) break
                    total += read
                }
                val ms = (System.nanoTime() - startedAt) / 1_000_000
                if (ms <= 0) return@withContext null
                Probe(candidate.host, candidate.url, ms, total)
            }
        } catch (e: Exception) {
            logger.i("换源测速失败", "host" to candidate.host, "error" to e.message)
            null
        }
    }

    /** 后台补测，结论留给下一次播放 */
    private fun probeInBackground(candidates: List<HostUrl>, headers: Map<String, String>) {
        val now = System.currentTimeMillis()
        val targets = pickProbes(candidates, scores ?: emptyMap())
            .filter { now - (lastProbeAt[it.host] ?: 0L) >= PROBE_INTERVAL_MS }
        if (targets.isEmpty()) {
            logger.i("换源后台测速跳过：这些线路刚测过（10 分钟内不重复）")
            return
        }
        targets.forEach { lastProbeAt[it.host] = now }
        logger.i("换源后台测速启动", "hosts" to targets.joinToString { it.host })
        backgroundScope.launch {
            try {
                val probes = probeAll(targets, headers)
                if (probes.isNotEmpty()) record(probes)
            } catch (e: Exception) {
                logger.e("换源后台测速失败", "error" to e.message)
            }
        }
    }

    /**
     * 线路历史分（host -> Mbps）
     *
     * 存的是整体更新时间 + 各线路得分：线路快慢几天内相对稳定，过期就整体作废重测。
     */
    @Serializable
    internal data class HostScores(
        val updatedAt: Long = 0L,
        val scores: Map<String, Double> = emptyMap(),
    )

    private suspend fun loadScores(): Map<String, Double> = scoresLock.withLock { loadScoresLocked() }

    /** 载入历史分。调用方必须已持有 [scoresLock]（[Mutex] 不可重入，不要在这里再上锁） */
    private suspend fun loadScoresLocked(): MutableMap<String, Double> {
        scores?.let { return it }
        val stored = try {
            SettingPreferences.mapPreferences { it[SettingPreferences.PlayerSourceHostScores] }
                ?.let { MiaoJson.fromJson<HostScores>(it) }
        } catch (e: Exception) {
            logger.e("读取换源历史分失败", "error" to e.message)
            null
        }
        val usable = stored?.takeIf {
            System.currentTimeMillis() - it.updatedAt < HISTORY_MAX_AGE_MS
        }
        val loaded = usable?.scores?.toMutableMap() ?: mutableMapOf()
        scores = loaded
        return loaded
    }

    private suspend fun record(probes: List<Probe>) {
        // 载入、累加、落盘放在同一个临界区：并发补测时不会互相覆盖
        scoresLock.withLock {
            val map = loadScoresLocked()
            probes.forEach { probe ->
                val mbps = if (probe.ms > 0) probe.bytes * 8.0 / probe.ms / 1000.0 else 0.0
                logger.i(
                    "换源测速结果",
                    "host" to probe.host,
                    "ms" to probe.ms,
                    "kb" to probe.bytes / 1024,
                    "mbps" to (mbps * 10).toLong() / 10.0,
                )
                // 几个字节就断掉的请求算不出速度，记进去只会污染分数
                if (probe.bytes < MIN_SAMPLE_BYTES) return@forEach
                val previous = map[probe.host]
                // 简单的对半平均：既跟得上线路变快变慢，也不会被单次抖动带跑
                map[probe.host] = if (previous == null) mbps else (previous + mbps) / 2
            }
            try {
                val json = MiaoJson.toJson(HostScores(System.currentTimeMillis(), map.toMap()))
                SettingPreferences.editPreferences { it[SettingPreferences.PlayerSourceHostScores] = json }
            } catch (e: Exception) {
                logger.e("保存换源历史分失败", "error" to e.message)
            }
        }
    }

    private fun isUposHost(host: String): Boolean = uposHostRegex.matches(host)

    /** 取 URL 的 host（小写，不含端口）。用纯字符串处理，避免为一次比较构造 URL 对象 */
    private fun hostOf(url: String): String {
        val schemeEnd = url.indexOf("://")
        if (schemeEnd < 0) return ""
        val from = schemeEnd + 3
        val slash = url.indexOf('/', from)
        val end = if (slash < 0) url.length else slash
        if (from >= end) return ""
        return url.substring(from, end).substringBefore(':').lowercase()
    }
}
