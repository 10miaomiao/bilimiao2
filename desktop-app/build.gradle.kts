import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
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
                implementation(libs.mediamp.native.loader)
                // native DLLs for current platform
                runtimeOnly("org.openani.mediamp:mediamp-mpv-runtime-windows-x64:${libs.versions.mediamp.get()}")

                // jna (for window border handling)
                implementation(libs.jna)
                implementation(libs.jna.platform)

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
            packageVersion = "2.5.0"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("appResources"))
        }
    }
}
