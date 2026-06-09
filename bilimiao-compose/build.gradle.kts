plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization")
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    jvm("desktop")

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)

                implementation(libs.kodein.di.core)
                implementation(libs.kodein.di.compose)
                implementation(libs.kmp.lifecycle.viewmodel.compose)

                implementation(libs.okhttp3)
                implementation(libs.pbandk.runtime)
                implementation(libs.materialkolor)
                implementation(libs.reorderable)
                implementation(libs.sonner)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.okhttp)
                implementation(libs.compose.navigation)
                implementation(libs.compose.material.icons.extended.kmp)
                implementation(libs.androidx.datastore.preferences)

                implementation(project(":bilimiao-comm"))
            }
        }
        androidMain {
            kotlin.srcDir("src/androidMain/java")
            resources.srcDir("src/androidMain/res")
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.material)
                implementation(libs.androidx.browser)

                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.activity.compose)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.material3.window.size)
                implementation(libs.compose.material3.adaptive)

                implementation(libs.accompanist.drawablepainter)
                implementation(libs.glide)
                implementation(libs.glide.compose)
                implementation(libs.qrose)
                implementation(libs.compose.preference)

                implementation(libs.kodein.di)
                implementation(libs.kodein.di.compose)

                implementation(libs.androidx.room.runtime)

                implementation(project(":bilimiao-download"))
                implementation(project(":DanmakuFlameMaster"))
                implementation(project(":bilimiao-cover"))
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.kotlinx.coroutines.core)

                // mediamp
                implementation(libs.mediamp.api)
                implementation(libs.mediamp.compose)
                implementation(libs.mediamp.vlc)
                implementation(libs.mediamp.vlc.compose)

                // 弹幕引擎
                implementation(project(":danmaku-engine"))
            }
        }
    }
}

android {
    compileSdk = 36

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}
