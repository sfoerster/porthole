# Porthole ProGuard Rules

# Preserve WebView-related classes for proper captive portal functionality
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, *);
    public boolean *(android.webkit.WebView, *);
}

-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, *);
}

-keep class android.webkit.** { *; }

# Preserve Hilt-generated components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep data classes used with DataStore
-keepclassmembers class com.stevenfoerster.porthole.session.SessionConfig { *; }
