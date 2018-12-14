# Hide warnings
-dontwarn me.mauricee.pontoon.**
-dontnote me.mauricee.pontoon.**

-keepattributes Signature

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Optimization/obfuscation/shrinking
-keep ,allowoptimization,allowobfuscation,allowshrinking public class me.mauricee.pontoon.** {
    *;
}
# Webview with JS
-keepclassmembers class me.mauricee.me.pontoon.login.lttlogin.LttLoginFragment {
   public *;
}
# Kotlin
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit