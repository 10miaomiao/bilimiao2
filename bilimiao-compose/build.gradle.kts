import java.io.File
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.URLClassLoader

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    jvm("desktop")

    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/bilimiaoIcons/src/commonMain/kotlin")
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.kodein.di.core)
                implementation(libs.kodein.di.compose)
                implementation(libs.kmp.lifecycle.viewmodel.compose)

                implementation(libs.okhttp3)
                implementation(libs.pbandk.runtime)
                implementation(libs.materialkolor)
                implementation(libs.reorderable)
                implementation(libs.sonner)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.okhttp)
                implementation(libs.androidx.navigation3.runtime)
                implementation(libs.navigation3.ui.jb)
                implementation(libs.androidx.lifecycle.viewmodel.navigation3)
                implementation(libs.compose.material.icons.extended.kmp)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.compose.preference)
                implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.2.0")

                implementation(project(":bilimiao-comm"))
            }
        }
        androidMain {
            kotlin.srcDir("src/androidMain/java")
            resources.srcDir("src/androidMain/res")
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.material)
                implementation(libs.androidx.browser)

                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.activity.compose)
                implementation(libs.compose.material.icons.extended)

                implementation(libs.accompanist.drawablepainter)
                implementation(libs.glide)
                implementation(libs.glide.compose)
                implementation(libs.qrose)

                implementation(libs.kodein.di)
                implementation(libs.kodein.di.compose)

                implementation(libs.androidx.room.runtime)

                implementation(project(":bilimiao-download"))
                implementation(project(":DanmakuFlameMaster"))
                implementation(project(":bilimiao-cover"))
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.kotlinx.coroutines.core)

                // qrose (二维码生成)
                implementation(libs.qrose)

                // mediamp
                implementation(libs.mediamp.api)
                implementation(libs.mediamp.mpv)

                // webviewko (极验验证码)
                implementation("com.github.winterreisender:webviewko-jvm:0.6.0")

                // 弹幕引擎
                implementation(project(":danmaku-engine"))
            }
        }
    }
}

android {
    compileSdk = 37

    defaultConfig {
        minSdk = 21
        version = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    namespace = "cn.a10miaomiao.bilimiao.compose"
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

// === SVG → Compose 图标代码生成 ===
// 将 icons/BilimiaoIcons 下的 SVG 文件转换为 Compose ImageVector Kotlin 代码
// 使用 svg-to-compose: https://github.com/DevSrSouza/svg-to-compose
// 通过 URLClassLoader 隔离加载，避免污染构建脚本的 classpath（尤其避免与 AGP 的 sdk-common 冲突）

val svg2composeConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    svg2composeConfig(libs.svg.to.compose)
    svg2composeConfig(libs.kotlinpoet)
    svg2composeConfig(libs.android.tools.sdk.common)
    svg2composeConfig(libs.android.tools.common)
    svg2composeConfig(libs.xpp3)
    svg2composeConfig(libs.guava)
}

val bilimiaoIconsOutputDir = layout.buildDirectory.dir("generated/bilimiaoIcons/src/commonMain/kotlin")

val generateBilimiaoIcons by tasks.registering {
    val svgDir = layout.projectDirectory.dir("icons/BilimiaoIcons")
    val outputDir = bilimiaoIconsOutputDir
    val config = svg2composeConfig

    inputs.dir(svgDir).withPropertyName("svgIcons")
    outputs.dir(outputDir).withPropertyName("generatedSources")

    doLast {
        val out = outputDir.get().asFile.apply {
            deleteRecursively()
            mkdirs()
        }

        // 用隔离的 ClassLoader 加载 svg-to-compose，避免与 AGP 的 sdk-common 版本冲突
        val urls = config.files.map { it.toURI().toURL() }.toTypedArray()
        val loader = URLClassLoader(urls, ClassLoader.getSystemClassLoader().parent)

        val svg2ComposeClass = loader.loadClass("br.com.devsrsouza.svg2compose.Svg2Compose")
        val vectorTypeClass = loader.loadClass("br.com.devsrsouza.svg2compose.VectorType")
        val svgType = vectorTypeClass.enumConstants.first { (it as Enum<*>).name == "SVG" }

        // Svg2Compose 是 Kotlin object，获取 INSTANCE 单例
        val instanceField = svg2ComposeClass.getDeclaredField("INSTANCE")
        instanceField.isAccessible = true
        val svg2ComposeInstance = instanceField.get(null)

        // 实现 iconNameTransformer，与 svg-to-compose 默认行为一致
        // 默认: { it, _ -> it.toKotlinPropertyName() }
        // toKotlinPropertyName 行为: 保留首字符大小写，其余转小写
        //   PlayNum -> Playnum, BiliCoin -> Bilicoin, LikeFill -> Likefill
        // 用 Proxy 实现 Function2 接口，确保由隔离 ClassLoader 加载该接口
        val function2Class = loader.loadClass("kotlin.jvm.functions.Function2")
        val handler = InvocationHandler { _, method: Method, args: Array<Any?>? ->
            if (method.name == "invoke" && args != null && args.size == 2) {
                val name = args[0] as String
                if (name.isEmpty()) name else name[0].toString() + name.substring(1).lowercase()
            } else {
                null
            }
        }
        val iconNameTransformer = Proxy.newProxyInstance(loader, arrayOf(function2Class), handler)

        // 通过名称和参数数量查找方法（避免 ClassLoader 导致的 getDeclaredMethod 类型严格匹配失败）
        val parseMethod = svg2ComposeClass.declaredMethods.first {
            it.name == "parse" && it.parameterCount == 9
        }

        parseMethod.invoke(
            svg2ComposeInstance,
            "cn.a10miaomiao.bilimiao.compose.assets",
            "BilimiaoIcons",
            out,
            svgDir.asFile,
            svgType,
            iconNameTransformer,
            "AllIcons",
            true,
            false,
        )

        // 后处理: svg-to-compose 生成的 Preview import 是 androidx.compose.ui.tooling.preview.Preview
        // 但本项目使用 Compose Multiplatform，应使用 org.jetbrains.compose.ui.tooling.preview.Preview
        out.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            if (content.contains("androidx.compose.ui.tooling.preview.Preview")) {
                file.writeText(
                    content.replace(
                        "androidx.compose.ui.tooling.preview.Preview",
                        "org.jetbrains.compose.ui.tooling.preview.Preview",
                    )
                )
            }
        }

        logger.lifecycle("Generated BilimiaoIcons to: ${out.absolutePath}")
    }
}

// 让 Kotlin 编译依赖生成任务
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(generateBilimiaoIcons)
}
