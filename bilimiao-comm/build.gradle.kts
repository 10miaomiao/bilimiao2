import com.google.protobuf.gradle.*
import cn.a10miaomiao.bilimiao.build.*

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.protobuf") // proto
    id("bilimiao-build")
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32
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
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.33.0" // Grpc单独的编译器
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
    implementation(Libraries.core)
    implementation(Libraries.appcompat)
    implementation(Libraries.material)
    implementation(Libraries.lifecycle)
    implementation(Libraries.lifecycleViewModel)
    implementation(Libraries.browser)

    implementation(Libraries.kotlinxCoroutinesAndroid)
    implementation(Libraries.kodeinDi)
    implementation(Libraries.glide)
    implementation(Libraries.dialogX)
//    implementation(files("../app/libs/DialogX-release.aar")) // 新版DialogX，解决一些bug，作者未发布到mavenCentral，故先本地编译
    implementation(Libraries.dialogXMaterialYou) {
        exclude("com.kongzue.dialogx", "DialogX")
    }

    implementation(Libraries.gson)
    implementation(Libraries.okhttp3)

    implementation(Libraries.grpcProtobuf)
    implementation(Libraries.grpcStub)

    implementation("javax.annotation:javax.annotation-api:1.2")

    // 极验验证
    implementation(Libraries.sensebot)

    implementation(project(":DanmakuFlameMaster"))

    testImplementation(Libraries.junit)
    androidTestImplementation(Libraries.androidxJunit)
    androidTestImplementation(Libraries.espresso)
}