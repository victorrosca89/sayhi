# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.sayhi.** { *; }

# WebRTC
-dontwarn org.webrtc.**
-keep class org.webrtc.** { *; }

# Hilt
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *** enter(...); }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.sayhi.data.model.** { *; }

# Socket.IO
-keep class io.socket.** { *; }
-dontwarn io.socket.**
