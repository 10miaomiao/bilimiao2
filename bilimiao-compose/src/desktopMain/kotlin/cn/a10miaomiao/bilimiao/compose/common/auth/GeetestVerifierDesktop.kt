package cn.a10miaomiao.bilimiao.compose.common.auth

import com.github.winterreisender.webviewko.WebviewKo
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.nio.file.Files
import java.util.concurrent.CompletableFuture

class GeetestVerifierDesktop : GeetestVerifier {

    override fun startVerification(callback: GeetestCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiJson = callback.getApiJson() ?: return@launch
                val gt = apiJson["gee_gt"]?.jsonPrimitive?.content
                    ?: apiJson["gt"]?.jsonPrimitive?.content
                    ?: return@launch
                val challenge = apiJson["gee_challenge"]?.jsonPrimitive?.content
                    ?: apiJson["challenge"]?.jsonPrimitive?.content
                    ?: return@launch

                val future = CompletableFuture<GeetestResult?>()

                val thread = Thread {
                    setMainWindowEnabled(false)
                    try {
                        val webview = WebviewKo()
                        webview.title("完成人机验证")
                        webview.size(400, 500)

                        webview.bind("postMessage") { req ->
                            val parts = Json.parseToJsonElement(req).jsonArray
                            future.complete(
                                GeetestResult(
                                    geetest_challenge = parts[0].jsonPrimitive.content,
                                    geetest_validate = parts[1].jsonPrimitive.content,
                                    geetest_seccode = parts[2].jsonPrimitive.content,
                                )
                            )
                            webview.terminate()
                            ""
                        }

                        webview.bind("close") {
                            future.complete(null)
                            webview.terminate()
                            ""
                        }

                        val htmlPath = extractResourcesToTemp()
                        val fileUrl = "file:///${htmlPath.absolutePath.replace("\\", "/")}/index.html?gt=$gt&challenge=$challenge"
                        webview.url(fileUrl)
                        webview.start()
                        webview.destroy()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        future.complete(null)
                    } finally {
                        setMainWindowEnabled(true)
                    }
                }
                thread.isDaemon = true
                thread.start()

                val result = future.get()
                if (result != null) {
                    callback.onResult(result)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun extractResourcesToTemp(): File {
        val tempDir = Files.createTempDirectory("geetest-validator").toFile()
        tempDir.deleteOnExit()
        val resourceDir = "geetest-validator"
        val files = listOf("index.html", "js/gt.js", "js/jquery.js", "css/style.css")
        for (file in files) {
            val input = GeetestVerifierDesktop::class.java.classLoader
                .getResourceAsStream("$resourceDir/$file") ?: continue
            val outFile = File(tempDir, file)
            outFile.parentFile.mkdirs()
            input.use { it.copyTo(outFile.outputStream()) }
            outFile.deleteOnExit()
        }
        return tempDir
    }

    companion object {
        var mainWindowHandle: Long? = null

        private fun setMainWindowEnabled(enabled: Boolean) {
            val hwnd = mainWindowHandle ?: return
            try {
                val user32 = Native.load("user32", Win32User32::class.java, W32APIOptions.DEFAULT_OPTIONS)
                user32.EnableWindow(Pointer.createConstant(hwnd), enabled)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private interface Win32User32 : StdCallLibrary {
        fun EnableWindow(hWnd: Pointer, bEnable: Boolean): Boolean
    }
}
