package com.stevenfoerster.porthole.network

import android.net.wifi.WifiManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves the current WiFi gateway IP address using [WifiManager] and [DhcpInfo].
 *
 * Returns null if the device is not connected to WiFi or the gateway address
 * cannot be determined. This is used to seed the [AllowlistManager] and to
 * gate session starts — a session cannot begin without a resolvable gateway.
 */
@Singleton
class GatewayResolver @Inject constructor(
    private val wifiManager: WifiManager,
) {
    /**
     * Resolves the current WiFi gateway IP as a dotted-decimal string.
     *
     * @return The gateway IP (e.g., "192.168.1.1"), or null if unavailable.
     */
    @Suppress("deprecation") // DhcpInfo is the only non-Connectivity API available at minSdk 26
    fun resolve(): String? {
        val dhcpInfo = wifiManager.dhcpInfo ?: return null
        val gateway = dhcpInfo.gateway
        if (gateway == 0) return null
        return intToIp(gateway)
    }

    companion object {
        /**
         * Converts a little-endian integer IP address to a dotted-decimal string.
         *
         * Android's [DhcpInfo] stores IP addresses as little-endian integers,
         * so the least significant byte is the first octet.
         */
        fun intToIp(ip: Int): String =
            "${ip and OCTET_MASK}." +
                "${ip shr BITS_PER_OCTET and OCTET_MASK}." +
                "${ip shr (BITS_PER_OCTET * 2) and OCTET_MASK}." +
                "${ip shr (BITS_PER_OCTET * 3) and OCTET_MASK}"

        private const val OCTET_MASK = 0xFF
        private const val BITS_PER_OCTET = 8
    }
}
