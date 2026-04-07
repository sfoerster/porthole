package com.stevenfoerster.porthole.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stevenfoerster.porthole.network.GatewayResolver
import com.stevenfoerster.porthole.session.SessionConfig
import com.stevenfoerster.porthole.session.SessionManager
import com.stevenfoerster.porthole.session.SessionState
import com.stevenfoerster.porthole.settings.PortholePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main screen.
 *
 * Bridges the [SessionManager], [GatewayResolver], and [PortholePreferences]
 * to the UI, exposing session state and handling session launch logic.
 */
@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val sessionManager: SessionManager,
        private val gatewayResolver: GatewayResolver,
        private val preferences: PortholePreferences,
    ) : ViewModel() {
        /** Current session state. */
        val sessionState: StateFlow<SessionState> = sessionManager.state

        /** Remaining seconds in the active session. */
        val remainingSeconds: StateFlow<Int> = sessionManager.remainingSeconds

        /** Current session configuration from preferences. */
        val sessionConfig: StateFlow<SessionConfig> =
            preferences.sessionConfig
                .stateIn(viewModelScope, SharingStarted.Eagerly, SessionConfig())

        /** Whether the first-run setup has been completed. */
        val firstRunCompleted: StateFlow<Boolean> =
            preferences.firstRunCompleted
                .stateIn(viewModelScope, SharingStarted.Eagerly, true)

        private val _gatewayIp = MutableStateFlow<String?>(null)

        /** The resolved WiFi gateway IP, or null if not on WiFi. */
        val gatewayIp: StateFlow<String?> = _gatewayIp.asStateFlow()

        private val _launchError = MutableStateFlow<String?>(null)

        /** Error message when session launch fails (e.g., no WiFi gateway). */
        val launchError: StateFlow<String?> = _launchError.asStateFlow()

        init {
            refreshGateway()
        }

        /** Refreshes the resolved gateway IP. */
        fun refreshGateway() {
            _gatewayIp.value = gatewayResolver.resolve()
        }

        /**
         * Attempts to launch a new captive portal session.
         *
         * Resolves the gateway first. If no gateway is available, sets [launchError].
         * Otherwise starts the session via [SessionManager].
         *
         * @return The resolved gateway IP if the session started, null otherwise.
         */
        fun launchSession(): String? {
            val gateway = gatewayResolver.resolve()
            _gatewayIp.value = gateway

            if (gateway == null) {
                _launchError.value = "No WiFi gateway detected. Connect to a WiFi network first."
                return null
            }

            val config = sessionConfig.value
            val started = sessionManager.startSession(config)
            if (!started) {
                _launchError.value = "A session is already active."
                return null
            }

            _launchError.value = null
            return gateway
        }

        /** Clears the current launch error. */
        fun clearError() {
            _launchError.value = null
        }

        /** Marks the first-run setup as completed. */
        fun completeFirstRun() {
            viewModelScope.launch {
                preferences.setFirstRunCompleted()
            }
        }
    }
