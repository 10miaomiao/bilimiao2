# for gson https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken