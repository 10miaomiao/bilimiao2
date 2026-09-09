import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

// bilibili gRPC API 独立模块（KMP）
// - 生成代码来源：:bilimiao-grpc:proto 模块的 generateProto 任务（Java sourceSet，单次生成）
// - commonMain 源码中仅含 GRPCMethod 定义；pbandk 消息与 service stub 均为生成代码
kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    jvm("desktop")

    sourceSets {
        commonMain {
            kotlin.srcDir(
                project(":bilimiao-grpc:proto")
                    .layout.buildDirectory
                    .dir("generated/source/proto/main/pbandk")
            )
            dependencies {
                // 生成的类型实现 pbandk.Message，作为公共 API 暴露
                api(libs.pbandk.runtime)
            }
        }
    }
}

android {
    namespace = "cn.a10miaomiao.bilimiao.grpc"
    compileSdk = 37

    defaultConfig {
        minSdk = 21
        version = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

// 确保所有 Kotlin 编译（desktop/android debug/release）前先生成 proto 代码
tasks.configureEach {
    if (name in listOf(
            "compileKotlinDesktop",
            "compileDebugKotlinAndroid",
            "compileReleaseKotlinAndroid",
        )
    ) {
        dependsOn(":bilimiao-grpc:proto:generateProto")
    }
}
