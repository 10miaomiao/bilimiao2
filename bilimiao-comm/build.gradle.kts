import com.google.protobuf.gradle.*
import cn.a10miaomiao.bilimiao.build.*
import java.nio.file.Paths

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.protobuf") // proto
    id("bilimiao-build")
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
    val pbandkVersion = Versions.pbandk
    protoc {
        artifact = "com.google.protobuf:protoc:3.12.0"  // 相当于proto编译器
    }
    plugins {
        id("pbandk") {
            artifact = "pro.streem.pbandk:protoc-gen-pbandk-jvm:$pbandkVersion:jvm8@jar"
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
    implementation(Libraries.core)
    implementation(Libraries.appcompat)
    implementation(Libraries.material)
    implementation(Libraries.lifecycle)
    implementation(Libraries.lifecycleViewModel)
    implementation(Libraries.datastore)
    implementation(Libraries.browser)

    implementation(Libraries.kotlinxSerializationJson)
    implementation(Libraries.kotlinxCoroutinesAndroid)
    implementation(Libraries.kodeinDi)
    implementation(Libraries.glide)
    implementation(Libraries.dialogX) {
        exclude("com.github.kongzue.DialogX", "DialogXInterface")
    }

    implementation(Libraries.okhttp3)
    implementation(Libraries.pbandkRuntime)

    implementation("javax.annotation:javax.annotation-api:1.2")

    implementation(project(":DanmakuFlameMaster"))

    testImplementation(Libraries.junit)
    androidTestImplementation(Libraries.androidxJunit)
    androidTestImplementation(Libraries.espresso)
}