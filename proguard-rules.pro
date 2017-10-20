-target 1.7
-dontshrink
-dontobfuscate
-dontoptimize
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# The -optimizations option disables some arithmetic simplifications that Dalvik 1.0 and 1.5 can't handle.
-optimizations !code/simplification/arithmetic

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep class com.google.inject.Binder
-keep class com.google.android.**
-keep class android.**

-keepclassmembers,allowoptimization enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
}

-keepattributes Exceptions, Signature, InnerClasses

-keep class com.stubhub.crash.** { *; }
-keep interface com.stubhub.crash.** { *; }
