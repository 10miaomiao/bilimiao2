import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.proto

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        version = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.62.2" // Grpc单独的编译器
        }
        id("javalite") {

            artifact = "com.google.protobuf:protoc-gen-javalite:3.0.0"
            // 官方推荐的方法，Android 适用javalite,相较于java插件，生成的代码更轻量化
        }
    }
    generateProtoTasks {
        all().forEach {
            it.builtins {
                register("java") {
                    option("lite")
                }
            }
            it.plugins {
                id("grpc") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.ktx)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.lifecycle.rt.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.browser)



    // serde & net
    implementation(libs.gson)
    implementation(libs.okhttp3)
    implementation(libs.grpc.protobuf.lite)
    implementation(libs.grpc.stub)
    implementation(libs.kotlinx.coroutines.android)
    implementation("javax.annotation:javax.annotation-api:1.3.2")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}