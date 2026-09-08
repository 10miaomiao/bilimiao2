plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    jvm("desktop")

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)

                // Compose 弹幕引擎：提供 @Composable 渲染组件（DanmakuCanvas）
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
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
                // Compose 桌面依赖：提供 org.jetbrains.skia API
                // （原 compileOnly skiko-awt 移除，版本随 compose.desktop.common 统一）
                implementation(compose.desktop.common)
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "cn.a10miaomiao.bilimiao.danmaku"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].java {
        srcDir("src/androidMain/kotlin")
    }
}
