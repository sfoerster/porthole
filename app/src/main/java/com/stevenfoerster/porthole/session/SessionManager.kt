package com.stevenfoerster.porthole.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that owns the lifecycle of a Porthole captive portal session.
 *
 * Exposes the current [SessionState] as a [StateFlow] and the remaining
 * seconds as a separate [StateFlow]. Coordinates the [SessionTimer] and
 * enforces valid state transitions.
 *
 * @property sessionTimer The countdown timer used for active sessions.
 * @property coroutineScope The scope in which the timer coroutine runs.
 *   Injected so that the timer is automatically cancelled when the scope is cancelled.
 */
@Singleton
class SessionManager
    @Inject
    constructor(
        private val sessionTimer: SessionTimer,
        private val coroutineScope: CoroutineScope,
    ) {
        private val _state = MutableStateFlow(SessionState.IDLE)

        /** The current session state. */
        val state: StateFlow<SessionState> = _state.asStateFlow()

        private val _remainingSeconds = MutableStateFlow(0)

        /** Remaining seconds in the active session. Zero when no session is active. */
        val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

        private var timerJob: Job? = null

        private var _activeConfig: SessionConfig? = null

        /** The configuration of the currently active session, or null if idle. */
        val activeConfig: SessionConfig? get() = _activeConfig

        /**
         * Starts a new captive portal session with the given [config].
         *
         * Only valid when the current state is [SessionState.IDLE].
         * Transitions to [SessionState.ACTIVE] and begins the countdown timer.
         *
         * @param config Session configuration including timeout, JS, and strict mode settings.
         * @return true if the session was started, false if a session is already active.
         */
        fun startSession(config: SessionConfig): Boolean {
            if (_state.value != SessionState.IDLE) return false

            _activeConfig = config
            _state.value = SessionState.ACTIVE
            _remainingSeconds.value = config.effectiveTimeoutSeconds

            timerJob =
                coroutineScope.launch {
                    sessionTimer.startCountdown(config.effectiveTimeoutSeconds).collect { remaining ->
                        _remainingSeconds.value = remaining
                        if (remaining == 0) {
                            expireSession()
                        }
                    }
                }
            return true
        }

        /**
         * Manually closes the active session.
         *
         * Only valid when the current state is [SessionState.ACTIVE].
         * Cancels the timer and transitions to [SessionState.CLOSED].
         * Callers should perform cleanup (WebView destruction, cookie clearing)
         * before calling [resetToIdle].
         */
        fun closeSession() {
            if (_state.value != SessionState.ACTIVE) return
            timerJob?.cancel()
            timerJob = null
            _remainingSeconds.value = 0
            _state.value = SessionState.CLOSED
        }

        /**
         * Resets the session state to [SessionState.IDLE] after cleanup.
         *
         * Only valid when the current state is [SessionState.EXPIRED] or [SessionState.CLOSED].
         */
        fun resetToIdle() {
            if (_state.value != SessionState.EXPIRED && _state.value != SessionState.CLOSED) return
            _activeConfig = null
            _state.value = SessionState.IDLE
        }

        private fun expireSession() {
            timerJob?.cancel()
            timerJob = null
            _state.value = SessionState.EXPIRED
        }
    }
