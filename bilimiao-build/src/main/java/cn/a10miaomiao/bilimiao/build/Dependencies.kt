package cn.a10miaomiao.bilimiao.build

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope

object Versions {
    // jetpack
    const val core = "1.13.1"
    const val appcompat = "1.6.1"
    const val material = "1.12.0"
    const val lifecycle = "2.8.3"
    const val navigation = "2.8.0"
//    const val compose = "1.7.0-beta04"
    const val composeBom = "2025.05.00"
    const val datastore = "1.1.1"
    const val media = "1.6.0"
    const val media3 = "1.5.0"

    const val serialization = "1.7.3"
    const val accompanist = "0.35.0-alpha"

    //
    const val splitties = "3.0.0-beta06"
    const val kodein_di = "7.12.0"

    //
    const val okhttp = "4.10.0"
    const val glide = "4.13.2"
    const val pbandk = "0.16.1-SNAPSHOT"

    const val gsyVideoPlayer = "v10.0.0"
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
    const val datastore = "androidx.datastore:datastore-preferences:${Versions.datastore}"
    const val media = "androidx.media:media:${Versions.media}"
    const val media3 = "androidx.media3:media3-common:${Versions.media3}"
    const val media3Ui = "androidx.media3:media3-ui:${Versions.media3}"
    const val media3Decoder = "androidx.media3:media3-decoder:${Versions.media3}"
    const val media3ExoPlayer = "androidx.media3:media3-exoplayer:${Versions.media3}"
    const val media3ExoPlayerDash = "androidx.media3:media3-exoplayer-dash:${Versions.media3}"
    const val media3Session = "androidx.media3:media3-session:${Versions.media3}"
    const val browser = "androidx.browser:browser:1.7.0"

    // compose基础组件
    const val composeBom = "androidx.compose:compose-bom:${Versions.composeBom}"
    const val composeUi = "androidx.compose.ui:ui"
    const val composeFoundation = "androidx.compose.foundation:foundation"
    const val composeUiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
    const val composeAnimation = "androidx.compose.animation:animation"
    const val composeMaterial = "androidx.compose.material:material"
    const val composeMaterial3 = "androidx.compose.material3:material3"
    const val composeMaterial3WindowSizeClass = "androidx.compose.material3:material3-window-size-class"
    const val composeMaterial3Adaptive = "androidx.compose.material3.adaptive:adaptive-android"
    const val activityCompose = "androidx.activity:activity-compose:1.9.0"
    const val navigationCompose = "androidx.navigation:navigation-compose:${Versions.navigation}"
    const val composeMaterialIconsExtended = "androidx.compose.material:material-icons-extended"
    const val glideCompose = "com.github.bumptech.glide:compose:1.0.0-beta01"
    // compose控件
    const val accompanistDrawablePainter = "com.google.accompanist:accompanist-drawablepainter:${Versions.accompanist}"
    const val accompanistAdaptive = "com.google.accompanist:accompanist-adaptive:${Versions.accompanist}"

    // 通用库
    const val kotlinxDatetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.6.1"
    const val kotlinxSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}"
    const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0"
    const val kodeinDi = "org.kodein.di:kodein-di-framework-android-x:${Versions.kodein_di}"
    const val kodeinDiCompose = "org.kodein.di:kodein-di-framework-compose:${Versions.kodein_di}"
    const val okhttp3 = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
//    const val grpcProtobuf = "io.grpc:grpc-protobuf-lite:1.33.0"
//    const val grpcStub = "io.grpc:grpc-stub:1.33.0"
    const val pbandkRuntime = "pro.streem.pbandk:pbandk-runtime:${Versions.pbandk}"
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"
    const val materialKolor = "com.materialkolor:material-kolor:2.0.2"

    // 通用控件
    const val recyclerview = "androidx.recyclerview:recyclerview:1.2.0"
    const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    const val baseRecyclerViewAdapterHelper = "com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.4"
    const val flexbox = "com.google.android.flexbox:flexbox:3.0.0"
    const val foregroundCompat = "me.zhanghai.android.foregroundcompat:library:1.0.2"
    const val drawer = "com.drakeet.drawer:drawer:1.0.3"
    const val dialogX = "com.github.kongzue:DialogX:0.0.50.beta37"
    const val dialogXMaterialYou = "com.github.kongzue.DialogX:DialogXMaterialYou:0.0.50.beta20"
    const val zxingLite = "com.github.jenly1314:zxing-lite:2.1.0"

    const val gsyVideoPlayer = "com.github.CarGuo.GSYVideoPlayer:gsyvideoplayer-java:${Versions.gsyVideoPlayer}"

    // 其他：极验验证
    const val sensebot = "com.geetest.sensebot:sensebot:4.3.8.1"

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