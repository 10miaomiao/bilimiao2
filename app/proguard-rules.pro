# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# === 裁剪策略 ===
# 只做代码裁剪 + 优化，不做混淆：
# 这是开源项目，保留类名/方法名可读，用户反馈的崩溃日志与 logcat TAG 无需 retrace 即可定位。
-dontobfuscate

# 保留堆栈定位所需的调试信息（行号、源文件名）
-keepattributes SourceFile,LineNumberTable

# Kodein 的 TypeToken 通过反射读取泛型签名来还原类型：
# - 业务侧：匿名子类 object : TypeReference<T>()
# - Kodein 自身：JVMAbstractTypeToken.WrappingTest<List<String>> 的 workaround 探测
# R8 full mode 只会为被 keep 规则显式匹配的类保留 Signature 里的泛型参数，
# 签名被洗成裸类型后会在启动时抛 TypeToken 相关异常。
# 规则形式参考 https://developer.android.com/topic/performance/app-optimization/full-mode
-keepattributes Signature
-keep,allowobfuscation,allowshrinking,allowoptimization class org.kodein.** { *; }
-keep,allowobfuscation,allowshrinking,allowoptimization class * extends org.kodein.type.TypeReference
# Kotlin 自身的 typeOf<T>() 也用同样的 TypeReference 机制，一并保留签名
-keep,allowobfuscation,allowshrinking,allowoptimization class kotlin.jvm.internal.TypeReference { *; }
-keep,allowobfuscation,allowshrinking,allowoptimization class * extends kotlin.jvm.internal.TypeReference

# 隐藏真实源码文件名（减小体积，避免泄漏本地路径）
-renamesourcefileattribute SourceFile

# === 反射调用的构造器 ===
# CommonDsl.diViewModel 通过 VM::class.java.getDeclaredConstructor(DI::class.java) 反射创建 ViewModel，
# R8 看不到直接 new，可能把构造器当无用代码裁掉。
# 注意：-dontobfuscate 只防改名，不防删除，所以这里仍需显式 keep。
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# === WebView 与 JS 交互接口 ===
# 如启用 WebView 的 addJavascriptInterface，取消注释并把 fqcn 替换为实际的接口类名。
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
