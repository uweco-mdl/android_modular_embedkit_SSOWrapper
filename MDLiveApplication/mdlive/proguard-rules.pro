# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/sampath_k/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


# Maintain visibility of Inner class enums

-keepattributes InnerClasses
-keep class com.mdlive.mobile.Utils$* {
          *;
      }

# VSeeKit
-keep public class com.vsee.kit.** {*;}
-keep public class com.vsee.kit.evisit.** {*;}


# for API 23+ (Apache HTTP client classes have now been removed):
-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.google.android.gms.**
-dontwarn com.android.volley.toolbox.**

