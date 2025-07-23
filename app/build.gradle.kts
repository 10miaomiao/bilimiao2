import cn.a10miaomiao.bilimiao.build.*
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("kotlin-android")
    id("bilimiao-build")
}

android {
    namespace = "com.a10miaomiao.bilimiao"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.a10miaomiao.bilimiao"
        minSdk = 21
        targetSdk = 35
        versionCode = 114
        versionName = "2.4.6"

        flavorDimensions("default")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("armeabi")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
    }

    val signingFile = file("signing.properties")
    if (signingFile.exists()) {
        val props = Properties()
        props.load(FileInputStream(signingFile))
        signingConfigs {
            create("miao") {
                keyAlias = props.getProperty("KEY_ALIAS")
                keyPassword = props.getProperty("KEY_PASSWORD")
                storeFile = file(props.getProperty("KEYSTORE_FILE"))
                storePassword = props.getProperty("KEYSTORE_PASSWORD")
            }
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            resValue("string", "app_name", "bilimiao dev")
            manifestPlaceholders["channel"] = "Development"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfigs.asMap["miao"]?.let {
                signingConfig = it
            }
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    productFlavors {
        create("full") {
            dimension = flavorDimensionList[0]
            val channelName = project.properties["channel"] ?: "Unknown"
            manifestPlaceholders["channel"] = channelName
        }
        create("foss") {
            dimension = flavorDimensionList[0]
            manifestPlaceholders["channel"] = "FOSS"
        }
    }

    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }


    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation(Libraries.core)
    implementation(Libraries.appcompat)
    implementation(Libraries.material)
    implementation(Libraries.lifecycle)
    implementation(Libraries.lifecycleViewModel)
    implementation(Libraries.datastore)
    implementation(Libraries.media)
    implementation(Libraries.browser)
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")

    implementation(Libraries.kotlinxCoroutinesAndroid)
    implementation(Libraries.kodeinDi) // 依赖注入

    implementation(Libraries.recyclerview)
    implementation(Libraries.baseRecyclerViewAdapterHelper)
    implementation(Libraries.swiperefreshlayout)
    implementation(Libraries.flexbox)
    implementation(Libraries.foregroundCompat)
    implementation(Libraries.drawer)
    implementation(Libraries.dialogX) {
        exclude("com.github.kongzue.DialogX", "DialogXInterface")
    }
    implementation(Libraries.zxingLite)
    implementation(Libraries.materialKolor)

//    implementation("com.github.li-xiaojun:XPopup:2.9.13")
//    implementation("com.github.lihangleo2:ShadowLayout:3.2.4")

    implementationSplitties()
    implementationMojito()

    // 播放器相关
    implementation(Libraries.media3)
    implementation(Libraries.media3Session)
    implementation(Libraries.media3Decoder)
    implementation(Libraries.media3Ui)
    implementation(Libraries.media3ExoPlayer)
    implementation(Libraries.media3ExoPlayerDash)
    implementation(Libraries.gsyVideoPlayer)

    implementation(Libraries.okhttp3)
    implementation(Libraries.pbandkRuntime)
    implementation(Libraries.glide)
    annotationProcessor(Libraries.glideCompiler)

    implementation(project(":bilimiao-comm"))
    implementation(project(":bilimiao-download"))
    implementation(project(":bilimiao-cover"))
//    implementation project(":bilimiao-appwidget")
    implementation(project(":bilimiao-compose"))
    implementation(project(":miao-binding"))
    implementation(project(":miao-binding-android"))
    // 弹幕引擎
    implementation(project(":DanmakuFlameMaster"))

    // 闭源库：百度统计、极验验证
    "fullImplementation"(Libraries.baiduMobstat)
    "fullImplementation"(Libraries.sensebot)
    // av1解码器：https://github.com/androidx/media/tree/release/libraries/decoder_av1
    "fullImplementation"(files("libs/lib-decoder-av1-release.aar"))

    testImplementation(Libraries.junit)
    androidTestImplementation(Libraries.androidxJunit)
    androidTestImplementation(Libraries.espresso)
}