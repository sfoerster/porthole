package com.stevenfoerster.porthole.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.stevenfoerster.porthole.network.AllowlistManager
import com.stevenfoerster.porthole.network.ConnectivityChecker
import com.stevenfoerster.porthole.session.SessionManager
import com.stevenfoerster.porthole.session.SessionState
import com.stevenfoerster.porthole.settings.PortholePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the portal WebView screen.
 *
 * Manages the allowlist, connectivity checking, and blocked navigation warnings.
 * All WebView interactions are delegated to the composable layer (which runs on main thread),
 * while this ViewModel handles the business logic.
 */
@HiltViewModel
class PortalViewModel
    @Inject
    constructor(
        private val sessionManager: SessionManager,
        private val allowlistManager: AllowlistManager,
        private val connectivityChecker: ConnectivityChecker,
        private val preferences: PortholePreferences,
    ) : ViewModel() {
        /** Current session state. */
        val sessionState: StateFlow<SessionState> = sessionManager.state

        /** Remaining seconds in the active session. */
        val remainingSeconds: StateFlow<Int> = sessionManager.remainingSeconds

        /** The set of currently allowed hosts. */
        val allowedHosts: StateFlow<Set<String>> = allowlistManager.allowedHosts

        private val _blockedUrl = MutableStateFlow<String?>(null)

        /** The most recently blocked URL, for displaying warnings to the user. */
        val blockedUrl: StateFlow<String?> = _blockedUrl.asStateFlow()

        private val _isConnected = MutableStateFlow(false)

        /** Whether internet connectivity has been detected (portal auth succeeded). */
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        /** The [AllowlistManager] instance for use by the WebViewClient. */
        val allowlist: AllowlistManager = allowlistManager

        /**
         * Initializes the portal session: sets up the allowlist and starts connectivity checking.
         *
         * @param gatewayIp The resolved WiFi gateway IP address.
         * @param strictMode Whether to enforce strict allowlist mode.
         * @param connectivityCheckUrl The URL to poll for connectivity checks.
         * @return A [Flow] of connectivity check results.
         */
        fun initializeSession(
            gatewayIp: String,
            strictMode: Boolean,
            connectivityCheckUrl: String = ConnectivityChecker.DEFAULT_CHECK_URL,
        ): Flow<Boolean> {
            allowlistManager.initialize(gatewayIp, strictMode)
            return connectivityChecker.checkConnectivity(connectivityCheckUrl)
        }

        /** Called by the WebViewClient when a navigation is blocked. */
        fun onNavigationBlocked(url: String) {
            _blockedUrl.value = url
        }

        /** Dismisses the blocked URL warning. */
        fun dismissBlockedWarning() {
            _blockedUrl.value = null
        }

        /**
         * Attempts to add a host to the allowlist (permissive mode only).
         *
         * @param host The hostname to add.
         * @return true if added successfully.
         */
        fun allowHost(host: String): Boolean = allowlistManager.addHost(host)

        /** Updates the connectivity status. */
        fun setConnected(connected: Boolean) {
            _isConnected.value = connected
        }

        /** Manually closes the active session and performs cleanup. */
        fun closeSession() {
            allowlistManager.clear()
            sessionManager.closeSession()
        }

        /** Resets the session to idle after cleanup is complete. */
        fun resetToIdle() {
            sessionManager.resetToIdle()
        }
    }
