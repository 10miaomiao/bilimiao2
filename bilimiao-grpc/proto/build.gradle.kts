import com.google.protobuf.gradle.*
import java.nio.file.Paths

plugins {
    java
    id("com.google.protobuf")
}

// 纯代码生成模块：通过 protobuf-gradle-plugin 的 Java sourceSet 提供唯一的 generateProto 任务，
// 与 Android buildType/变体完全解耦（不再挂在 AGP 的 debug 变体上）。
// 生成的 pbandk 消息代码与 gRPC service stub 由 :bilimiao-grpc 模块的 commonMain 直接引用，
// 本模块自身不编译/打包这些代码。
tasks.named("compileJava") {
    enabled = false
}
tasks.named("jar") {
    enabled = false
}

sourceSets {
    main {
        proto.srcDir("src/main/proto")
    }
}

dependencies {
    // 提供 google/protobuf/*.proto（any/empty/timestamp 等）的 include 源
    protobuf("com.google.protobuf:protobuf-java:4.28.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.28.0"
    }
    plugins {
        id("pbandk") {
            artifact = "pro.streem.pbandk:protoc-gen-pbandk-jvm:${libs.versions.pbandk.get()}:jvm8@jar"
        }
    }
    generateProtoTasks {
        val generatorModule = "grpc-generator"
        val generatorClass = "cn.a10miaomiao.generator.GrpcServiceGenerator"
        // 生成器 jar：bilimiao-grpc/generator/build/libs/grpc-generator.jar
        val generatorJarFile = Paths.get(
            project(":bilimiao-grpc:generator").layout.buildDirectory.get().asFile.path,
            "libs",
            "$generatorModule.jar"
        ).toFile()
        all().forEach { task ->
            // 不需要默认的 java builtin 输出，只保留 pbandk（kotlin）输出
            task.builtins {
                remove("java")
            }
            task.plugins {
                id("pbandk") {
                    // 生成器 jar 会被 pbandk 在运行时反射加载，需保证始终为最新
                    task.dependsOn(":bilimiao-grpc:generator:jar")
                    option("log=debug")
                    var jarPath = generatorJarFile.path
                    jarPath.indexOf(':')
                        .takeIf { it != -1 }
                        ?.let {
                            // option 不能传递 `:` 符号，故 windows 情况下只能去除盘符
                            jarPath = jarPath.substring(it + 1, jarPath.length)
                        }
                    option("kotlin_service_gen=${jarPath}|$generatorClass")
                }
            }
        }
    }
}
