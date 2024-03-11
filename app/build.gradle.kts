plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.a10miaomiao.bilimiao.compose"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.a10miaomiao.bilimiao.compose"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "3.0.0-rc"

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
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.activity.compose)
    // Jetpack Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.window.size)
    // implementation(Libraries.composeMaterial3Android)
    // 3rd party compose
    implementation(libs.compose.destinations.core)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.qrose)
    ksp(libs.compose.destinations.ksp)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.rt.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.browser)

    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    implementation(libs.kotlinx.coroutines.android)
    // media3
    implementation(libs.media3)
    implementation(libs.media3.decoder)
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.dash)

    // serde & net
    implementation(libs.gson)
    implementation(libs.okhttp3)
    implementation(libs.grpc.protobuf.lite)
    // implementation(libs.grpc.stub)

    implementation(project(":bilimiao-comm"))
    implementation(project(":bilimiao-download"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)

}