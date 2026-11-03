-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class com.likeki.alquran.playback.PlaybackService { *; }
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.likeki.alquran.**$$serializer { *; }
-keepclassmembers class com.likeki.alquran.** { *** Companion; }
-keepclasseswithmembers class com.likeki.alquran.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlin.Metadata { *; }
-dontwarn kotlinx.coroutines.**
