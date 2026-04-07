package com.stevenfoerster.porthole.session

/**
 * Represents the current state of a Porthole captive portal session.
 *
 * State transitions:
 * ```
 * IDLE -> ACTIVE -> EXPIRED -> IDLE
 *                -> CLOSED  -> IDLE
 * ```
 */
enum class SessionState {
    /** No session is active. The app is in its default resting state. */
    IDLE,

    /** A captive portal session is in progress. The WebView is live and the timer is counting down. */
    ACTIVE,

    /** The session timer reached zero. Cleanup is in progress or complete. */
    EXPIRED,

    /** The user manually closed the session. Cleanup is in progress or complete. */
    CLOSED,
}
