plugins {
    kotlin("multiplatform")
    id("com.android.library")
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
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                // Skia: compileOnly, 运行时由 bilimiao-compose (compose.desktop.common) 提供
                compileOnly("org.jetbrains.skiko:skiko-awt:0.8.18")
            }
        }
    }
}

android {
    compileSdk = 37

    defaultConfig {
        minSdk = 21
        version = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    namespace = "cn.a10miaomiao.bilimiao.danmaku"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].java {
        srcDir("src/androidMain/kotlin")
    }
}
