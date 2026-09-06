# Proguard Rules for Production Release - Escolaris

# Keep Line Numbers and Attributes for Crashlytics
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void <init>();
    public <methods>;
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.domain.model.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Moshi / JSON Serialization
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.JsonClass <fields>;
}

# Jetpack Compose & ViewModel
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.lifecycle.ViewModelProvider$Factory { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Firebase & Google Services
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
