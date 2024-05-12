import cn.a10miaomiao.bilimiao.build.*

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("kotlin-android")
    id("bilimiao-build")
}

android {
    namespace = "com.a10miaomiao.bilimiao"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.a10miaomiao.bilimiao"
        minSdk = 21
        targetSdk = 34
        versionCode = 98
        versionName = "2.3.6.2"

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

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    fun createManifestPlaceholders(
        channelName: String
    ) = mapOf(
        "APP_CHANNEL_VALUE" to channelName,
    )

    productFlavors {
        create("dev") {
            val manifestPlaceholders = createManifestPlaceholders("Development")
            addManifestPlaceholders(manifestPlaceholders)
        }
        create("github") {
            val manifestPlaceholders = createManifestPlaceholders("Github")
            addManifestPlaceholders(manifestPlaceholders)
        }
        create("gitee") {
            val manifestPlaceholders = createManifestPlaceholders("Gitee")
            addManifestPlaceholders(manifestPlaceholders)
        }
        create("qq") {
            val manifestPlaceholders = createManifestPlaceholders("QQ")
            addManifestPlaceholders(manifestPlaceholders)
        }
        create("miao") {
            val manifestPlaceholders = createManifestPlaceholders("10miaomiao")
            addManifestPlaceholders(manifestPlaceholders)
        }
    }

    compileOptions {
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
}

dependencies {
    implementation(Libraries.core)
    implementation(Libraries.appcompat)
    implementation(Libraries.material)
    implementation(Libraries.lifecycle)
    implementation(Libraries.lifecycleViewModel)
    implementation(Libraries.navigationFragment)
    implementation(Libraries.navigationUi)
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
    implementation(Libraries.modernAndroidPreferences)
    implementation(Libraries.dialogX) {
        exclude("com.github.kongzue.DialogX", "DialogXInterface")
    }
    implementation(Libraries.zxingLite)

//    implementation("com.github.li-xiaojun:XPopup:2.9.13")
//    implementation("com.github.lihangleo2:ShadowLayout:3.2.4")

    implementationSplitties()
    implementationMojito()

    // 播放器相关
    implementation(Libraries.media3)
    implementation(Libraries.media3Decoder)
    implementation(Libraries.media3Ui)
    implementation(Libraries.media3ExoPlayer)
    implementation(Libraries.media3ExoPlayerDash)
    implementation(Libraries.gsyVideoPlayer)
    implementation(files("libs/lib-decoder-av1-release.aar"))

    implementation(Libraries.gson)
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

    // 百度统计
    implementation(Libraries.baiduMobstat)

    testImplementation(Libraries.junit)
    androidTestImplementation(Libraries.androidxJunit)
    androidTestImplementation(Libraries.espresso)
}