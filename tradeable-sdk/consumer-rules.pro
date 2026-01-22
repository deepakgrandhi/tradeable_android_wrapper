# Consumer ProGuard Rules for Tradeable SDK
# These rules are automatically included in apps that use this AAR

# Keep all public SDK classes
-keep class com.tradeable.sdk.** { *; }

# Flutter rules
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }
