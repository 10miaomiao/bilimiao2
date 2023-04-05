import com.google.protobuf.gradle.*

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("com.google.protobuf") // proto
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
        jvmTarget =("1.8")
    }


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
                if (contains(java)) {
                    named("java") {
                        option("lite")
                    }
                } else {
                    create("java") {
                        option("lite")
                    }
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
    val material_version: String by rootProject.extra
    val lifecycle_version: String by rootProject.extra
    val kodein_di_version: String by rootProject.extra
    val gson_version: String by rootProject.extra
    val okhttp_version: String by rootProject.extra

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("com.google.android.material:material:$material_version")

    implementation("org.kodein.di:kodein-di-framework-android-x:$kodein_di_version")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    implementation("com.github.bumptech.glide:glide:4.13.2")

    implementation("com.google.code.gson:gson:$gson_version")
    implementation("com.squareup.okhttp3:okhttp:$okhttp_version")

    implementation("io.grpc:grpc-protobuf-lite:1.33.0")
    implementation("io.grpc:grpc-stub:1.33.0")
    implementation("javax.annotation:javax.annotation-api:1.2")

    // 极验验证
    implementation("com.geetest.sensebot:sensebot:4.3.8.1")

    implementation(project(":DanmakuFlameMaster"))

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}