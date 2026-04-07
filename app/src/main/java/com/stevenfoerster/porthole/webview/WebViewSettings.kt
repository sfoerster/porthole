package com.stevenfoerster.porthole.webview

import android.webkit.WebSettings
import android.webkit.WebView

/**
 * Centralizes all WebView security hardening for Porthole.
 *
 * Every setting here is a deliberate security decision. The WebView is configured
 * to be as restrictive as possible while still allowing captive portal authentication.
 * See docs/THREAT_MODEL.md for the rationale behind each setting.
 */
object WebViewSettings {
    /**
     * Applies the full set of hardened settings to a [WebView].
     *
     * @param webView The WebView instance to configure.
     * @param jsEnabled Whether JavaScript should be enabled. Defaults to false.
     */
    fun apply(
        webView: WebView,
        jsEnabled: Boolean = false,
    ) {
        webView.settings.apply {
            // JavaScript: off by default, only enabled when the user explicitly opts in
            @Suppress("SetJavaScriptEnabled")
            javaScriptEnabled = jsEnabled

            // File system access: always disabled to prevent local file exfiltration
            allowFileAccess = false
            allowContentAccess = false

            // Geolocation: no captive portal needs device location
            setGeolocationEnabled(false)

            // Passwords and form data: never persist credentials
            @Suppress("deprecation")
            savePassword = false
            saveFormData = false

            // Database and DOM storage: disabled to prevent tracking persistence
            databaseEnabled = false
            domStorageEnabled = jsEnabled // Only if JS is enabled, some portals need it

            // Cache: always load from network, never from cache
            cacheMode = WebSettings.LOAD_NO_CACHE

            // Mixed content: block mixed content to prevent downgrade attacks
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

            // Disable zoom controls — portal pages should render at their intended scale
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false

            // User agent: use default Android WebView user agent
            // Some portals check user agent to serve appropriate pages
            userAgentString = userAgentString

            // Block automatic media playback
            mediaPlaybackRequiresUserGesture = true
        }
    }
}
