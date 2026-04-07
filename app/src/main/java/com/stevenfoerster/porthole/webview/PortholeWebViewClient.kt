package com.stevenfoerster.porthole.webview

import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import com.stevenfoerster.porthole.network.AllowlistManager
import java.io.ByteArrayInputStream

/**
 * Custom [WebViewClient] that enforces the Porthole navigation allowlist.
 *
 * Every navigation and resource request is checked against the [AllowlistManager].
 * Blocked navigations produce a visible in-app warning page rather than failing silently,
 * so the user understands why a page didn't load.
 *
 * @property allowlistManager The allowlist to check navigations against.
 * @property onNavigationBlocked Callback invoked when a navigation is blocked,
 *   with the blocked URL as the parameter. Used by the UI to show warnings.
 * @property onPageStarted Callback invoked when a page begins loading.
 * @property onPageFinished Callback invoked when a page finishes loading.
 */
class PortholeWebViewClient(
    private val allowlistManager: AllowlistManager,
    private val onNavigationBlocked: (String) -> Unit = {},
    private val onPageStarted: (String) -> Unit = {},
    private val onPageFinished: (String) -> Unit = {},
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        if (!allowlistManager.isAllowed(url)) {
            onNavigationBlocked(url)
            view.loadData(
                buildBlockedPageHtml(url),
                MIME_TYPE_HTML,
                ENCODING_UTF8,
            )
            return true
        }
        return false
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        val url = request.url.toString()
        if (!allowlistManager.isAllowed(url)) {
            onNavigationBlocked(url)
            return WebResourceResponse(
                MIME_TYPE_HTML,
                ENCODING_UTF8,
                ByteArrayInputStream(buildBlockedPageHtml(url).toByteArray()),
            )
        }
        return null
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        url?.let { onPageStarted(it) }
    }

    override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        url?.let { onPageFinished(it) }
    }

    companion object {
        private const val MIME_TYPE_HTML = "text/html"
        private const val ENCODING_UTF8 = "UTF-8"

        /**
         * Performs full session cleanup: clears cookies, web storage, and cache.
         *
         * Must be called on the main thread. This is a critical security operation —
         * no user data should persist between sessions.
         *
         * @param webView The WebView instance to clean up. Will be destroyed after cleanup.
         */
        fun performSessionCleanup(webView: WebView) {
            // Clear all cookies
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()

            // Clear web storage (localStorage, sessionStorage, databases)
            WebStorage.getInstance().deleteAllData()

            // Clear the WebView's own cache
            webView.clearCache(true)
            webView.clearHistory()
            webView.clearFormData()

            // Destroy the WebView instance — it must never be reused
            webView.destroy()
        }

        /**
         * Builds a simple HTML page explaining that navigation was blocked.
         *
         * Uses inline styles only — no external resources are loaded.
         */
        private fun buildBlockedPageHtml(blockedUrl: String): String =
            """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Navigation Blocked</title>
            <style>
                body { font-family: sans-serif; padding: 24px; background: #1a1a1a; color: #e0e0e0; }
                .container { max-width: 480px; margin: 0 auto; text-align: center; }
                h1 { color: #ff6b6b; font-size: 20px; }
                p { font-size: 14px; line-height: 1.5; color: #aaa; }
                .url { word-break: break-all; background: #2a2a2a; padding: 8px 12px;
                       border-radius: 4px; font-size: 12px; color: #888; margin: 16px 0; }
            </style>
            </head>
            <body>
            <div class="container">
                <h1>&#x1F6AB; Navigation Blocked</h1>
                <p>Porthole blocked navigation to a host outside the allowed list.
                   This keeps your session sandboxed to the captive portal.</p>
                <div class="url">${blockedUrl.replace("<", "&lt;").replace(">", "&gt;")}</div>
                <p>If this portal requires access to external domains,
                   you can switch to permissive mode in Settings.</p>
            </div>
            </body>
            </html>
            """.trimIndent()
    }
}
