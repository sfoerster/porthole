package com.stevenfoerster.porthole.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stevenfoerster.porthole.session.SessionConfig
import com.stevenfoerster.porthole.settings.PortholePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the settings screen.
 *
 * Reads and writes preferences via [PortholePreferences] and exposes
 * the current configuration as reactive state for the Compose UI.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PortholePreferences,
) : ViewModel() {

    /** The current session configuration. */
    val sessionConfig: StateFlow<SessionConfig> = preferences.sessionConfig
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionConfig())

    /** The current connectivity check URL. */
    val connectivityCheckUrl: StateFlow<String> = preferences.connectivityCheckUrl
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            com.stevenfoerster.porthole.network.ConnectivityChecker.DEFAULT_CHECK_URL,
        )

    /** Updates the session timeout. */
    fun setTimeoutSeconds(seconds: Int) {
        viewModelScope.launch { preferences.setTimeoutSeconds(seconds) }
    }

    /** Updates the JavaScript enabled setting. */
    fun setJsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setJsEnabled(enabled) }
    }

    /** Updates the strict mode setting. */
    fun setStrictMode(strict: Boolean) {
        viewModelScope.launch { preferences.setStrictMode(strict) }
    }

    /** Updates the connectivity check URL. */
    fun setConnectivityCheckUrl(url: String) {
        viewModelScope.launch { preferences.setConnectivityCheckUrl(url) }
    }
}
