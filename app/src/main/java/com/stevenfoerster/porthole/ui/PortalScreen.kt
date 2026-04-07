package com.stevenfoerster.porthole.ui

import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.stevenfoerster.porthole.network.AllowlistManager
import com.stevenfoerster.porthole.session.SessionState
import com.stevenfoerster.porthole.ui.components.SessionStatusBar
import com.stevenfoerster.porthole.ui.viewmodel.PortalViewModel
import com.stevenfoerster.porthole.webview.PortholeWebViewClient
import com.stevenfoerster.porthole.webview.WebViewSettings

/**
 * Portal screen wrapping the sandboxed WebView.
 *
 * Shows the [SessionStatusBar] with countdown, a tunnel status indicator,
 * and the WebView for captive portal interaction. The WebView is created
 * fresh for each session and destroyed on session end.
 *
 * @param viewModel The [PortalViewModel] managing session and allowlist state.
 * @param gatewayIp The resolved gateway IP to navigate to.
 * @param jsEnabled Whether JavaScript is enabled for this session.
 * @param strictMode Whether strict allowlist mode is active.
 * @param connectivityCheckUrl The URL to poll for connectivity checks.
 * @param onSessionEnded Callback invoked when the session ends (expired or closed).
 */
@Composable
fun PortalScreen(
    viewModel: PortalViewModel,
    gatewayIp: String,
    jsEnabled: Boolean,
    strictMode: Boolean,
    connectivityCheckUrl: String,
    onSessionEnded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val blockedUrl by viewModel.blockedUrl.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Start connectivity checking
    LaunchedEffect(gatewayIp) {
        viewModel.initializeSession(gatewayIp, strictMode, connectivityCheckUrl)
            .collect { connected ->
                viewModel.setConnected(connected)
            }
    }

    // Handle session end
    LaunchedEffect(sessionState) {
        if (sessionState == SessionState.EXPIRED || sessionState == SessionState.CLOSED) {
            webViewInstance?.let { PortholeWebViewClient.performSessionCleanup(it) }
            webViewInstance = null
            viewModel.resetToIdle()
            onSessionEnded()
        }
    }

    // Cleanup on disposal (back navigation, etc.)
    DisposableEffect(Unit) {
        onDispose {
            if (sessionState == SessionState.ACTIVE) {
                viewModel.closeSession()
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        SessionStatusBar(
            remainingSeconds = remainingSeconds,
            isConnected = isConnected,
            onClose = { viewModel.closeSession() },
        )

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // WebView — created fresh for each session, never reused
        AndroidView(
            factory = { context ->
                WebView(context).also { webView ->
                    WebViewSettings.apply(webView, jsEnabled)
                    webView.webViewClient =
                        PortholeWebViewClient(
                            allowlistManager = viewModel.allowlist,
                            onNavigationBlocked = { url -> viewModel.onNavigationBlocked(url) },
                            onPageStarted = { isLoading = true },
                            onPageFinished = { isLoading = false },
                        )
                    webView.loadUrl("http://$gatewayIp")
                    webViewInstance = webView
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }

    // Blocked navigation dialog
    blockedUrl?.let { url ->
        val host = AllowlistManager.extractHost(url)
        AlertDialog(
            onDismissRequest = { viewModel.dismissBlockedWarning() },
            title = { Text("Navigation Blocked") },
            text = {
                Text(
                    "The portal tried to navigate to an external host: " +
                        "${host ?: url}\n\n" +
                        if (strictMode) {
                            "Strict mode only allows local network addresses. " +
                                "Switch to permissive mode in Settings if this portal requires external domains."
                        } else {
                            "Would you like to allow this host for the current session?"
                        },
                )
            },
            confirmButton = {
                if (!strictMode && host != null) {
                    TextButton(onClick = {
                        viewModel.allowHost(host)
                        viewModel.dismissBlockedWarning()
                    }) {
                        Text("Allow")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissBlockedWarning() }) {
                    Text(if (strictMode) "OK" else "Block")
                }
            },
        )
    }
}
