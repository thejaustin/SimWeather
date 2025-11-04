# Add project specific ProGuard rules here.
# You can find more details about ProGuard in the official documentation:
# https://www.guardsquare.com/manual/configuration/index.html

# If you are using Kotlin, be sure to add the following rules:
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.coroutines.jvm.internal.** { *; }
