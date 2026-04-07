package com.stevenfoerster.porthole.session

/**
 * Configuration for a Porthole captive portal session.
 *
 * @property timeoutSeconds Duration in seconds before the session auto-expires.
 *   Clamped to [MIN_TIMEOUT_SECONDS]..[MAX_TIMEOUT_SECONDS] at construction.
 * @property jsEnabled Whether JavaScript execution is permitted in the WebView.
 *   Defaults to false for security — only enable when a portal requires it.
 * @property strictMode Whether the allowlist is limited to RFC 1918 addresses
 *   and the resolved gateway. When false, redirects are followed with user confirmation.
 */
data class SessionConfig(
    val timeoutSeconds: Int = DEFAULT_TIMEOUT_SECONDS,
    val jsEnabled: Boolean = false,
    val strictMode: Boolean = true,
) {
    /**
     * The effective timeout, clamped to the allowed range.
     * The hard maximum of [MAX_TIMEOUT_SECONDS] (10 minutes) is enforced
     * regardless of what the user configures.
     */
    val effectiveTimeoutSeconds: Int =
        timeoutSeconds.coerceIn(MIN_TIMEOUT_SECONDS, MAX_TIMEOUT_SECONDS)

    companion object {
        /** Minimum allowed session timeout in seconds. */
        const val MIN_TIMEOUT_SECONDS = 30

        /** Default session timeout in seconds. */
        const val DEFAULT_TIMEOUT_SECONDS = 60

        /** Hard maximum session timeout in seconds (10 minutes). Non-configurable. */
        const val MAX_TIMEOUT_SECONDS = 600
    }
}
