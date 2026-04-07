package com.stevenfoerster.porthole.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stevenfoerster.porthole.network.ConnectivityChecker
import com.stevenfoerster.porthole.session.SessionConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Extension property to create a single DataStore instance for the application. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "porthole_prefs")

/**
 * DataStore-backed preferences wrapper for Porthole.
 *
 * Exposes the current [SessionConfig] as a [Flow] so that settings changes
 * are immediately reflected in the UI and session behavior.
 * Also manages the first-run completion flag and connectivity check URL override.
 */
@Singleton
class PortholePreferences
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.dataStore

        /** Emits the current [SessionConfig] whenever any preference changes. */
        val sessionConfig: Flow<SessionConfig> =
            dataStore.data.map { prefs ->
                SessionConfig(
                    timeoutSeconds = prefs[KEY_TIMEOUT_SECONDS] ?: SessionConfig.DEFAULT_TIMEOUT_SECONDS,
                    jsEnabled = prefs[KEY_JS_ENABLED] ?: false,
                    strictMode = prefs[KEY_STRICT_MODE] ?: true,
                )
            }

        /** Emits true if the user has completed the first-run setup. */
        val firstRunCompleted: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[KEY_FIRST_RUN_COMPLETED] ?: false
            }

        /** Emits the configured connectivity check URL, or the default if not overridden. */
        val connectivityCheckUrl: Flow<String> =
            dataStore.data.map { prefs ->
                prefs[KEY_CONNECTIVITY_CHECK_URL] ?: ConnectivityChecker.DEFAULT_CHECK_URL
            }

        /** Updates the session timeout duration. */
        suspend fun setTimeoutSeconds(seconds: Int) {
            dataStore.edit { prefs ->
                prefs[KEY_TIMEOUT_SECONDS] = seconds
            }
        }

        /** Updates the JavaScript enabled preference. */
        suspend fun setJsEnabled(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[KEY_JS_ENABLED] = enabled
            }
        }

        /** Updates the strict mode preference. */
        suspend fun setStrictMode(strict: Boolean) {
            dataStore.edit { prefs ->
                prefs[KEY_STRICT_MODE] = strict
            }
        }

        /** Marks the first-run setup as completed. Never shown again after this. */
        suspend fun setFirstRunCompleted() {
            dataStore.edit { prefs ->
                prefs[KEY_FIRST_RUN_COMPLETED] = true
            }
        }

        /** Updates the connectivity check URL override. */
        suspend fun setConnectivityCheckUrl(url: String) {
            dataStore.edit { prefs ->
                prefs[KEY_CONNECTIVITY_CHECK_URL] = url
            }
        }

        companion object {
            private val KEY_TIMEOUT_SECONDS = intPreferencesKey("timeout_seconds")
            private val KEY_JS_ENABLED = booleanPreferencesKey("js_enabled")
            private val KEY_STRICT_MODE = booleanPreferencesKey("strict_mode")
            private val KEY_FIRST_RUN_COMPLETED = booleanPreferencesKey("first_run_completed")
            private val KEY_CONNECTIVITY_CHECK_URL = stringPreferencesKey("connectivity_check_url")
        }
    }
