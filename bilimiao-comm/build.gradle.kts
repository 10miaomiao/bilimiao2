plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
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
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.okhttp3)
                implementation(libs.kodein.di.core)
                implementation(libs.kmp.lifecycle.viewmodel.compose)
                implementation(libs.sonner)
                api(libs.androidx.datastore.preferences)
                api(libs.androidx.room.runtime)
                // bilibili gRPC API（proto 生成代码 + service stub），公共类型需透出给下游模块
                api(project(":bilimiao-grpc"))
                // KMP 弹幕引擎 (commonMain 的 BasePlayerSource 需要 BiliDanmakuParser)
                implementation(project(":danmaku-engine"))
            }
        }
        androidMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.material)
                implementation(libs.androidx.browser)

                implementation(libs.kodein.di)
                implementation(libs.glide)
                // mediamp (安卓端使用 ExoPlayer 后端)
                implementation(libs.mediamp.api)
                implementation(libs.mediamp.exoplayer)
                // media3 (ExoPlayerMediampPlayer 底层依赖，需显式声明用于 MergingMediaSource 等 API)
                implementation(libs.androidx.media3.exoplayer)
                implementation(libs.androidx.media3.datasource)
                // DanmakuFlameMaster 已移除
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.androidx.sqlite.bundled)

                // jna (for Windows AppData folder resolution)
                implementation(libs.jna)
                implementation(libs.jna.platform)

                // mediamp
                implementation(libs.mediamp.api)
                implementation(libs.mediamp.mpv)

                // 弹幕引擎
                implementation(project(":danmaku-engine"))
            }
        }
    }
}

// Room KSP compiler for each target
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
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
    namespace = "com.a10miaomiao.bilimiao.comm"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].java {
        srcDir("src/androidMain/java")
    }
}
