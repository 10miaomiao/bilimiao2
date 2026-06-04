import com.google.protobuf.gradle.*
import java.nio.file.Paths

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.protobuf") // proto
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
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
            kotlin.srcDir("build/generated/source/proto/debug/pbandk")
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.okhttp3)
                implementation(libs.pbandk.runtime)
                implementation(libs.kodein.di.core)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.room.runtime)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.appcompat)
                implementation(libs.material)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.lifecycle.viewmodel.ktx)
                implementation(libs.androidx.browser)

                implementation(libs.androidx.compose.bom)
                implementation(libs.compose.ui)

                implementation(libs.kodein.di)
                implementation(libs.glide)
                implementation(libs.sonner)

                implementation(project(":DanmakuFlameMaster"))
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.androidx.sqlite.bundled)
            }
        }
    }
}

// Room KSP compiler for each target
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
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
    namespace = "com.a10miaomiao.bilimiao.comm"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].proto {
        srcDir("src/androidMain/proto")
        include("**/*.proto")
    }
    sourceSets["main"].java {
        srcDir("src/androidMain/java")
    }
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.12.0"  // 相当于proto编译器
    }
    plugins {
        id("pbandk") {
            artifact = "pro.streem.pbandk:protoc-gen-pbandk-jvm:0.16.0:jvm8@jar"
        }
    }
    generateProtoTasks {
        val generatorModule = "grpc-generator"
        val generatorClass = "cn.a10miaomiao.generator.GrpcServiceGenerator"
        // grpc-generator/build/libs/grpc-generator.jar
        val generatorJarFile = Paths.get(
            project(":grpc-generator").layout.buildDirectory.get().asFile.path,
            "libs",
            "$generatorModule.jar"
        ).toFile()
        all().forEach { task ->
            task.plugins {
                id("pbandk") {
                    if (!generatorJarFile.exists()) {
                        task.dependsOn(":$generatorModule:jar")
                    }
                    option("log=debug")
                    var jarPath = generatorJarFile.path
                    jarPath.indexOf(':')
                        .takeIf { it != -1 }
                        ?.let {
                            // option不能传递`:`符号，故windows情况下只能去除盘符
                            jarPath = jarPath.substring(it + 1, jarPath.length)
                        }
                    option("kotlin_service_gen=${jarPath}|$generatorClass")
                }
            }
        }
    }
}

// Ensure proto generation runs before desktop compilation
tasks.matching { it.name == "compileKotlinDesktop" || it.name == "kspKotlinDesktop" }.configureEach {
    dependsOn("generateDebugProto")
}
