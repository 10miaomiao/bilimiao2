package cn.a10miaomiao.bilimiao.build

object Versions {
    const val kotlin = "1.9.22" // not working !

    // jetpack
    const val core = "1.6.0"
    const val appcompat = "1.6.1"
    const val material = "1.8.0"
    const val lifecycle = "2.5.1"
    const val navigation = "2.7.7"
    const val compose = "1.6.2"
    const val composeBOM = "2024.02.01"
    const val media3 = "1.3.0"
    const val composeDestinations = "1.10.1"
    const val coil = "2.6.0"
    const val okhttp = "4.10.0"
    const val gson = "2.10.1"
}

object Libraries {
    // jetpack库
    const val core = "androidx.core:core-ktx:${Versions.core}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val media3 = "androidx.media3:media3-common:${Versions.media3}"
    const val media3Ui = "androidx.media3:media3-ui:${Versions.media3}"
    const val media3Decoder = "androidx.media3:media3-decoder:${Versions.media3}"
    const val media3ExoPlayer = "androidx.media3:media3-exoplayer:${Versions.media3}"
    const val media3ExoPlayerDash = "androidx.media3:media3-exoplayer-dash:${Versions.media3}"
    const val browser = "androidx.browser:browser:1.7.0"

    // compose基础组件
    const val composeUi = "androidx.compose.ui:ui:${Versions.compose}"
    const val composeUiToolingPreview = "androidx.compose.ui:ui-tooling-preview:${Versions.compose}"
    const val composeMaterial = "androidx.compose.material:material:1.5.4"
    const val composeMaterial3 = "androidx.compose.material3:material3:1.2.0-rc01"
    const val composeMaterial3WindowSizeClass = "androidx.compose.material3:material3-window-size-class:1.1.2"
    const val activityCompose = "androidx.activity:activity-compose:${Versions.compose}"
    const val navigationCompose = "androidx.navigation:navigation-compose:${Versions.navigation}"
    const val composeMaterialIconsExtended = "androidx.compose.material:material-icons-extended:$${Versions.compose}"
    const val composeDestinations = "io.github.raamcosta.compose-destinations:core:${Versions.composeDestinations}"
    const val composeDestinationsKSP = "io.github.raamcosta.compose-destinations:ksp:${Versions.composeDestinations}"
    const val composeBOM = "androidx.compose:compose-bom:${Versions.composeBOM}"

    const val coil = "io.coil-kt:coil:${Versions.coil}" // Image loading with caching
    const val coilCompose = "io.coil-kt:coil-compose:${Versions.coil}"
    const val qrose = "io.github.alexzhirkevich:qrose:1.0.0-beta3" // QrCode for compose https://github.com/alexzhirkevich/qrose

    // 通用库
    const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val okhttp3 = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val grpcProtobuf = "io.grpc:grpc-protobuf-lite:1.33.0"
    const val grpcStub = "io.grpc:grpc-stub:1.33.0"


    // 测试库
    const val junit = "junit:junit:4.+"
    const val androidxJunit = "androidx.test.ext:junit:1.1.3"
    const val espresso = "androidx.test.espresso:espresso-core:3.4.0"
}
