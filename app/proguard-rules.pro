# Google Filament (added in Phase 3).
# Filament's JNI layer resolves Java fields and methods BY NAME. R8 renaming or
# removing them produces a release-only native crash that debug builds never show.
# See docs/TECHNICAL_ARCHITECTURE.md §12 and docs/KNOWN_LIMITATIONS.md LIM-FILAMENT-NATIVE.
-keep class com.google.android.filament.** { *; }
-keep class com.google.android.filament.gltfio.** { *; }
-keep class com.google.android.filament.utils.** { *; }

# Kotlin coroutines debug metadata
-dontwarn kotlinx.coroutines.**
