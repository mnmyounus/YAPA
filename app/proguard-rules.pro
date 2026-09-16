# YALA is offline; keep rules minimal and defensive.
-keep class com.mnmyounus.yala.domain.model.** { *; }
-keep class com.mnmyounus.yala.data.local.db.** { *; }
-keepattributes *Annotation*
-dontwarn kotlinx.serialization.**
