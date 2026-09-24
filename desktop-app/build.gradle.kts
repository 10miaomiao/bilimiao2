import org.jetbrains.compose.desktop.application.dsl.TargetFormat

import java.io.File

plugins {
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

// 版本号来自根项目（gradle.properties 单一来源），与 Android 端保持一致
val appVersionName = rootProject.extra["bilimiaoVersionName"] as String
val appVersionCode = rootProject.extra["bilimiaoVersionCode"] as Int
// MSI 的 ProductVersion 必须是纯数字点分格式，不能带 "beta" 这类后缀
val appPackageVersion = rootProject.extra["bilimiaoPackageVersion"] as String

val generateBuildConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/buildconfig")
    // 必须声明版本号为输入，否则改了版本号 Gradle 会误判 UP-TO-DATE，沿用旧生成的 BuildConfig
    inputs.property("versionName", appVersionName)
    inputs.property("versionCode", appVersionCode)
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        File(dir, "BuildConfig.kt").writeText(
            """
            package app.bilimiao.desktop

            object BuildConfig {
                const val VERSION_NAME = "$appVersionName"
                const val VERSION_CODE = ${appVersionCode}L
            }
            """.trimIndent()
        )
    }
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            kotlin.srcDir(generateBuildConfig)
            dependencies {
                implementation(project(":bilimiao-comm"))
                implementation(project(":bilimiao-compose"))
                implementation(compose.desktop.currentOs)
                implementation(libs.kmp.lifecycle.viewmodel.compose)
                implementation(libs.kodein.di.core)
                implementation(libs.kodein.di.compose)
                implementation(libs.kotlinx.coroutines.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
                implementation(libs.kotlinx.serialization.json)

                // mediamp
                implementation(libs.mediamp.api)
                implementation(libs.mediamp.mpv)
                // native DLLs for current platform
                runtimeOnly("org.openani.mediamp:mediamp-mpv-runtime-windows-x64:${libs.versions.mediamp.get()}")

                // jna (for window border handling)
                implementation(libs.jna)
                implementation(libs.jna.platform)

                // exclude macOS Skiko from dependencies (we only target Windows)
                configurations.all {
                    exclude(group = "org.jetbrains.skiko", module = "skiko-awt-runtime-macos-arm64")
                    exclude(group = "org.jetbrains.skiko", module = "skiko-awt-runtime-macos-x64")
                }

                // compose material3 (for title bar)
                implementation(compose.ui)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "app.bilimiao.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "bilimiao"
            packageVersion = appPackageVersion
            description = "bilimiao"
            vendor = "10miaomiao"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("appResources"))
            modules(
                "java.base",
                "java.desktop",
                "jdk.unsupported",
            )
            windows {
                iconFile.set(project.file("src/icon/bilimiao.ico"))
            }
        }
    }
}
