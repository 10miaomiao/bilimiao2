import cn.a10miaomiao.bilimiao.build.*

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("bilimiao-build")
}
kotlin {
    jvmToolchain(8)
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    namespace = "cn.a10miaomiao.bilimiao.compose"
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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

    implementation(Libraries.kotlinxCoroutinesAndroid)
    implementation(Libraries.kodeinDi) // 依赖注入
    implementation(Libraries.kodeinDiCompose) // 依赖注入

    implementation(Libraries.composeUi)
    implementation(Libraries.composeMaterial)
    implementation(Libraries.composeMaterialIconsExtended)
    implementation(Libraries.composeMaterial3)
//    implementation(Libraries.composeMaterial3Android)
    implementation(Libraries.composeMaterial3WindowSizeClass)
    implementation(Libraries.composeUiToolingPreview)
    implementation(Libraries.activityCompose)
    implementation(Libraries.navigationCompose)
    implementation(Libraries.accompanistDrawablePainter)

    implementation(Libraries.gson)
    implementation(Libraries.okhttp3)
    implementation(Libraries.glide)
    implementation(Libraries.glideCompose)
    implementation(Libraries.qrGenerator)

    implementation(Libraries.dialogX)
//    implementation(files("../app/libs/DialogX-release.aar"))

    implementation(project(":bilimiao-comm"))
    implementation(project(":bilimiao-download"))
    implementation(platform(Libraries.composeBOM))

    testImplementation(Libraries.junit)
    androidTestImplementation(Libraries.androidxJunit)
    androidTestImplementation(Libraries.espresso)
    androidTestImplementation(platform(Libraries.composeBOM))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}