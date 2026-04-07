package com.stevenfoerster.porthole.network

import android.net.DhcpInfo
import android.net.wifi.WifiManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GatewayResolverTest {

    private val wifiManager = mockk<WifiManager>()
    private val resolver = GatewayResolver(wifiManager)

    @Test
    fun `resolve returns gateway IP when available`() {
        val dhcpInfo = DhcpInfo().apply {
            // 192.168.1.1 in little-endian: 1 + (168 << 8) + (192 << 16) + (0 << 24)
            // Actually: 192.168.1.1 LE = 1*1 + 1*256 + 168*65536 + 192*16777216
            // = 1 + 256 + 11010048 + 3221225472 -- that's wrong for int
            // Let's compute: 192.168.1.1 in Android LE int:
            // octet[0]=192, octet[1]=168, octet[2]=1, octet[3]=1
            // Android stores as: octet[0] | (octet[1] << 8) | (octet[2] << 16) | (octet[3] << 24)
            // Wait, Android DhcpInfo stores IPs as little-endian:
            // 192.168.1.1 -> first octet is least significant
            // = 192 + (168 << 8) + (1 << 16) + (1 << 24)
            gateway = 192 or (168 shl 8) or (1 shl 16) or (1 shl 24)
        }
        every { wifiManager.dhcpInfo } returns dhcpInfo

        assertEquals("192.168.1.1", resolver.resolve())
    }

    @Test
    fun `resolve returns null when dhcpInfo is null`() {
        every { wifiManager.dhcpInfo } returns null

        assertNull(resolver.resolve())
    }

    @Test
    fun `resolve returns null when gateway is zero`() {
        val dhcpInfo = DhcpInfo().apply { gateway = 0 }
        every { wifiManager.dhcpInfo } returns dhcpInfo

        assertNull(resolver.resolve())
    }

    @Test
    fun `intToIp converts correctly`() {
        // 10.0.0.1 in LE = 10 + (0 << 8) + (0 << 16) + (1 << 24)
        val ip = 10 or (0 shl 8) or (0 shl 16) or (1 shl 24)
        assertEquals("10.0.0.1", GatewayResolver.intToIp(ip))
    }

    @Test
    fun `intToIp handles 172 range`() {
        // 172.16.0.1 in LE
        val ip = 172 or (16 shl 8) or (0 shl 16) or (1 shl 24)
        assertEquals("172.16.0.1", GatewayResolver.intToIp(ip))
    }
}
