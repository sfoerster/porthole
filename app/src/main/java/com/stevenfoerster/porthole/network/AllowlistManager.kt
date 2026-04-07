package com.stevenfoerster.porthole.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the set of hosts that the captive portal WebView is allowed to navigate to.
 *
 * In strict mode, only RFC 1918 private addresses and the resolved gateway are permitted.
 * In permissive mode, additional hosts can be added (with user confirmation) as the
 * portal redirects through its authentication flow.
 */
@Singleton
class AllowlistManager @Inject constructor() {

    private val _allowedHosts = MutableStateFlow<Set<String>>(emptySet())

    /** The current set of allowed hostnames/IPs. */
    val allowedHosts: StateFlow<Set<String>> = _allowedHosts.asStateFlow()

    private var strictMode: Boolean = true

    /**
     * Initializes the allowlist for a new session.
     *
     * @param gatewayIp The resolved gateway IP address to always allow.
     * @param strict Whether to enforce strict (RFC 1918 only) mode.
     */
    fun initialize(gatewayIp: String, strict: Boolean) {
        strictMode = strict
        _allowedHosts.value = setOf(gatewayIp)
    }

    /**
     * Checks whether navigation to the given [url] should be permitted.
     *
     * @param url The URL the WebView is attempting to navigate to.
     * @return true if the navigation is allowed, false if it should be blocked.
     */
    fun isAllowed(url: String): Boolean {
        val host = extractHost(url) ?: return false

        // Always allow hosts already in the allowlist
        if (host in _allowedHosts.value) return true

        // Check if the host resolves to a private IP
        if (isPrivateAddress(host)) {
            _allowedHosts.value = _allowedHosts.value + host
            return true
        }

        // In permissive mode, non-private hosts can be added later via addHost
        // but are not auto-allowed here — the UI must confirm first
        return false
    }

    /**
     * Adds a host to the allowlist. Used in permissive mode after user confirmation.
     *
     * @param host The hostname to add.
     * @return true if the host was added (permissive mode), false if rejected (strict mode).
     */
    fun addHost(host: String): Boolean {
        if (strictMode) return false
        _allowedHosts.value = _allowedHosts.value + host
        return true
    }

    /** Clears the allowlist. Called on session end. */
    fun clear() {
        _allowedHosts.value = emptySet()
    }

    companion object {
        /**
         * Extracts the host portion from a URL string.
         *
         * Uses [java.net.URI] parsing to handle edge cases correctly.
         */
        fun extractHost(url: String): String? =
            try {
                java.net.URI(url).host
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                null
            }

        /**
         * Determines whether a host resolves to an RFC 1918 private address.
         *
         * RFC 1918 ranges:
         * - 10.0.0.0/8
         * - 172.16.0.0/12
         * - 192.168.0.0/16
         */
        fun isPrivateAddress(host: String): Boolean =
            try {
                val address = InetAddress.getByName(host)
                address.isSiteLocalAddress || address.isLoopbackAddress
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                false
            }
    }
}
