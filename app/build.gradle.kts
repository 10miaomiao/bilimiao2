
import cn.a10miaomiao.bilimiao.build.Libraries

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("kotlin-android")
    id("bilimiao-build")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.a10miaomiao.bilimiao"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.a10miaomiao.bilimiao"
        minSdk = 21
        targetSdk = 34
        versionCode = 94
        versionName = "2.3.4"

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
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    buildFeatures {
        compose = true
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


    implementation(Libraries.browser)
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")

    implementation(Libraries.kotlinxCoroutinesAndroid)
    // 播放器相关
    implementation(Libraries.media3)
    implementation(Libraries.media3Decoder)
    implementation(Libraries.media3Ui)
    implementation(Libraries.media3ExoPlayer)
    implementation(Libraries.media3ExoPlayerDash)
    implementation(Libraries.gson)
    implementation(Libraries.okhttp3)
    implementation(Libraries.grpcProtobuf)

    implementation(project(":bilimiao-comm"))
    implementation(project(":bilimiao-download"))
    // Jetpack Compose
    implementation(Libraries.composeUi)
    implementation(Libraries.composeMaterial)
    implementation(Libraries.composeMaterialIconsExtended)
    implementation(Libraries.composeMaterial3)
    // implementation(Libraries.composeMaterial3Android)
    implementation(Libraries.composeMaterial3WindowSizeClass)
    implementation(Libraries.composeUiToolingPreview)
    implementation(Libraries.activityCompose)
    implementation(Libraries.composeBOM)
    implementation(Libraries.composeDestinations)
    ksp(Libraries.composeDestinationsKSP)

    implementation(Libraries.coil)
    implementation(Libraries.coilCompose)
    implementation(Libraries.qrose)

    testImplementation(Libraries.junit)
    androidTestImplementation(Libraries.androidxJunit)
    androidTestImplementation(Libraries.espresso)
}