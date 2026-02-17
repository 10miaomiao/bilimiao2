import com.google.protobuf.gradle.*
import java.nio.file.Paths

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.protobuf") // proto
    kotlin("plugin.serialization")
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    namespace = "com.a10miaomiao.bilimiao.comm"

    sourceSets["main"].proto {
        srcDir("src/main/proto") // 模块下的proto文件夹
        include("**/*.proto")
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
            project(":grpc-generator").buildDir.path,
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

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.browser)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kodein.di)
    implementation(libs.glide)
    implementation(libs.kongzue.dialogx) {
        exclude("com.github.kongzue.DialogX", "DialogXInterface")
    }

    implementation(libs.okhttp3)
    implementation(libs.pbandk.runtime)

    implementation("javax.annotation:javax.annotation-api:1.2")

    implementation(project(":DanmakuFlameMaster"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}