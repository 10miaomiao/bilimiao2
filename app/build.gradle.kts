plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("kotlin-android")

//    kotlin("android")
}

android {
    compileSdk = 33
//    buildToolsVersion "30.0.3"

    defaultConfig {
        applicationId = "com.a10miaomiao.bilimiao"
        minSdk = 21
        targetSdk = 33
        versionCode = 73
        versionName = "2.2.6 beta1"

        flavorDimensions("default")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
//            abiFilters "arm64-v8a", "armeabi-v7a", "armeabi", "x86", "x86_64"
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
            abiFilters.add("armeabi")
            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
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

    fun createManifestPlaceholders(
        channelName: String
    ) = mapOf(
        "APP_CHANNEL_VALUE" to channelName,
    )

    productFlavors {
        create("coolapk") {
            applicationId = "cn.a10miaomiao.bilimiao.dev"
            val manifestPlaceholders = createManifestPlaceholders("Coolapk")
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

    lintOptions {
        isCheckReleaseBuilds = false
        isAbortOnError = false
    }

}

dependencies {
    val material_version: String by rootProject.extra
    val kodein_di_version: String by rootProject.extra
    val splitties_version: String by rootProject.extra
    val nav_version: String by rootProject.extra

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:${material_version}")

    implementation("androidx.recyclerview:recyclerview:1.2.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("me.zhanghai.android.foregroundcompat:library:1.0.2")
    implementation("com.drakeet.drawer:drawer:1.0.3")
//    implementation("androidx.work:work-runtime-ktx:2.7.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    // 依赖注入
    implementation("org.kodein.di:kodein-di-framework-android-x:$kodein_di_version")

    implementation("com.louiscad.splitties:splitties-fun-pack-android-base:$splitties_version")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-base-with-views-dsl:$splitties_version")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-appcompat:$splitties_version")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-appcompat-with-views-dsl:$splitties_version")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-material-components:$splitties_version")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-material-components-with-views-dsl:$splitties_version")

    implementation("com.github.bumptech.glide:glide:4.13.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

//    implementation("com.github.li-xiaojun:XPopup:2.9.13")
    implementation("de.maxr1998:modernandroidpreferences:2.2.0")

    implementation("com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.4")
//    implementation("com.github.lihangleo2:ShadowLayout:3.2.4")

    val mojito_version: String by rootProject.extra
    val gson_version: String by rootProject.extra
    val okhttp_version: String by rootProject.extra

    // 图片预览工具
    implementation("com.github.mikaelzero.mojito:mojito:$mojito_version")
    //support long image and gif with Sketch
    implementation("com.github.mikaelzero.mojito:SketchImageViewLoader:$mojito_version")
    //load with glide
    implementation("com.github.mikaelzero.mojito:GlideImageLoader:$mojito_version")

    implementation("com.google.code.gson:gson:$gson_version")
    implementation("com.squareup.okhttp3:okhttp:$okhttp_version")
    implementation("io.grpc:grpc-protobuf-lite:1.33.0")

    // 播放器
    val gsyVideoPlayerVersion = "v8.3.4-release-jitpack"
    implementation("com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer:v8.3.4-release-jitpack")
    implementation("com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:${gsyVideoPlayerVersion}")
    //是否需要ExoPlayer模式
    implementation("com.github.CarGuo.GSYVideoPlayer:GSYVideoPlayer-exo2:${gsyVideoPlayerVersion}")


    implementation(project(":bilimiao-comm"))
    implementation(project(":bilimiao-cover"))
//    implementation project(":bilimiao-appwidget")
    implementation(project(":bilimiao-compose"))
    implementation(project(":download"))
    implementation(project(":miao-binding"))
    implementation(project(":miao-binding-android"))

    // 弹幕引擎
    implementation(project(":DanmakuFlameMaster"))
//    implementation("com.github.ctiao:DanmakuFlameMaster:0.9.25")
    implementation("com.github.ctiao:ndkbitmap-armv7a:0.9.21")
    implementation("com.github.ctiao:ndkbitmap-armv5:0.9.21")
    implementation("com.github.ctiao:ndkbitmap-x86:0.9.21")

    // ijpplayer
//    implementation project(":ijkplayer-java")
//    implementation project(":player")

    // 百度统计
    implementation("com.baidu.mobstat:mtj-sdk:latest.integration")

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}