# FlowScale ProGuard/R8 rules

# Room – keep generated Dao implementations
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Keep data classes used by Room
-keep class com.flowscale.app.data.IntensityRecord { *; }
