# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/baidu/development/adt-bundle-mac-x86_64-20140702/sdk/tools/proguard/proguard-android.txt
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
-optimizationpasses 5

-dontusemixedcaseclassnames

-dontskipnonpubliclibraryclasses

-dontoptimize

-dontpreverify

-verbose

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keepattributes *Annotation*

-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.support.v4.app.Fragment

-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-ignorewarning

-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt


-keep  class com.fantasy.coolgif.widget.* {*;}

-dontwarn okhttp3.**
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}


-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keep public class com.fantasy.coolgif.R$*{
public static final int *;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.dianxinos.DXStatService.stat.TokenManager {
public static java.lang.String getToken(android.content.Context);
}
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
        @com.google.android.gms.common.annotation.KeepName *;}
-keep class com.google.android.gms.common.GooglePlayServicesUtil {
      public <methods>;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
      public <methods>;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
      public <methods>;}


-keep class com.facebook.ads.NativeAd
-keep class com.google.android.gms.ads.formats.NativeContentAd
-keep class com.duapps.ad.**{*;}

-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.content.ContentProvider
-keep class com.dianxinos.DXStatService.stat.TokenManager { public static java.lang.String getToken(android.content.Context); }
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * { @com.google.android.gms.common.annotation.KeepName *;}
-keep class com.google.android.gms.common.GooglePlayServicesUtil { public <methods>;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { public <methods>;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { public <methods>;}
-keep class com.duapps.ad.banner.BannerListener { *; }

-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}
