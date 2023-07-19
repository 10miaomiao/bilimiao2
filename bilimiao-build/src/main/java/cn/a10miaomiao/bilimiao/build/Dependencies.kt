package cn.a10miaomiao.bilimiao.build

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope

object Versions {
    // jetpack
    const val core = "1.6.0"
    const val appcompat = "1.6.1"
    const val material = "1.6.1"
    const val lifecycle = "2.5.1"
    const val navigation = "2.5.3"
    const val compose = "1.3.3"
    const val media = "1.6.0"
    const val media3 = "1.0.0-rc01"

    const val accompanist = "0.29.2-rc"

    //
    const val splitties = "3.0.0-beta06"
    const val kodein_di = "7.12.0"

    //
    const val okhttp = "4.10.0"
    const val gson = "2.10.1"
    const val glide = "4.13.2"

    const val gsyVideoPlayer = "v8.3.5-release-jitpack"
}

object Libraries {
    // jetpack库
    const val core = "androidx.core:core-ktx:${Versions.core}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val work = "androidx.work:work-runtime-ktx:2.7.1"
    const val navigationUi = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    const val media = "androidx.media:media:${Versions.media}"
    const val media3 = "androidx.media3:media3-common:${Versions.media3}"
    const val media3Ui = "androidx.media3:media3-ui:${Versions.media3}"
    const val media3Decoder = "androidx.media3:media3-decoder:${Versions.media3}"
    const val media3ExoPlayer = "androidx.media3:media3-exoplayer:${Versions.media3}"
    const val media3ExoPlayerDash = "androidx.media3:media3-exoplayer-dash:${Versions.media3}"

    // compose基础组件
    const val composeUi = "androidx.compose.ui:ui:${Versions.compose}"
    const val composeUiToolingPreview = "androidx.compose.ui:ui-tooling-preview:${Versions.compose}"
    const val composeMaterial3 = "androidx.compose.material3:material3:1.0.1"
    const val composeMaterial3WindowSizeClass = "androidx.compose.material3:material3-window-size-class:1.0.1"
    const val activityCompose = "androidx.activity:activity-compose:1.3.1"
    const val navigationCompose = "androidx.navigation:navigation-compose:${Versions.navigation}"
    const val composeMaterialIconsExtended = "androidx.compose.material:material-icons-extended:$${Versions.compose}"
    const val glideCompose = "com.github.skydoves:landscapist-glide:2.0.0"
    // compose控件
    const val accompanistSwipeRefresh = "com.google.accompanist:accompanist-swiperefresh:${Versions.accompanist}"
    const val accompanistDrawablePainter = "com.google.accompanist:accompanist-drawablepainter:${Versions.accompanist}"

    // 通用库
    const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9"
    const val kodeinDi = "org.kodein.di:kodein-di-framework-android-x:${Versions.kodein_di}"
    const val kodeinDiCompose = "org.kodein.di:kodein-di-framework-compose:${Versions.kodein_di}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val okhttp3 = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val grpcProtobuf = "io.grpc:grpc-protobuf-lite:1.33.0"
    const val grpcStub = "io.grpc:grpc-stub:1.33.0"
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"

    // 通用控件
    const val recyclerview = "androidx.recyclerview:recyclerview:1.2.0"
    const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    const val baseRecyclerViewAdapterHelper = "com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.4"
    const val flexbox = "com.google.android.flexbox:flexbox:3.0.0"
    const val foregroundCompat = "me.zhanghai.android.foregroundcompat:library:1.0.2"
    const val drawer = "com.drakeet.drawer:drawer:1.0.3"
    const val modernAndroidPreferences = "de.maxr1998:modernandroidpreferences:2.2.0"
    const val dialogX = "com.kongzue.dialogx:DialogX:0.0.48"
    const val dialogXMaterialYou = "com.kongzue.dialogx.style:DialogXMaterialYouStyle:0.0.48"

    const val gsyVideoPlayer = "com.github.CarGuo.GSYVideoPlayer:gsyVideoPlayer-java:${Versions.gsyVideoPlayer}"

    // 其他：极验验证、二维码生成
    const val sensebot = "com.geetest.sensebot:sensebot:4.3.8.1"
    const val qrGenerator = "com.github.alexzhirkevich:custom-qr-generator:1.6.0"

    // 百度统计
    const val baiduMobstat = "com.baidu.mobstat:mtj-sdk:latest.integration"

    // 测试库
    const val junit = "junit:junit:4.+"
    const val androidxJunit = "androidx.test.ext:junit:1.1.3"
    const val espresso = "androidx.test.espresso:espresso-core:3.4.0"
}

private fun DependencyHandler.`implementation`(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

/**
 * VIEW DSL
 */
fun DependencyHandlerScope.implementationSplitties() {
    val splittiesVersion = Versions.splitties
    implementation("com.louiscad.splitties:splitties-fun-pack-android-base:$splittiesVersion")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-base-with-views-dsl:$splittiesVersion")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-appcompat:$splittiesVersion")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-appcompat-with-views-dsl:$splittiesVersion")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-material-components:$splittiesVersion")
    implementation("com.louiscad.splitties:splitties-fun-pack-android-material-components-with-views-dsl:$splittiesVersion")
}

/**
 * 图片预览工具
 */
fun DependencyHandlerScope.implementationMojito() {
    val mojitoVersion = "1.8.7"
    implementation("com.github.mikaelzero.mojito:mojito:$mojitoVersion")
    //support long image and gif with Sketch
    implementation("com.github.mikaelzero.mojito:SketchImageViewLoader:$mojitoVersion")
    //load with glide
    implementation("com.github.mikaelzero.mojito:GlideImageLoader:$mojitoVersion")
}