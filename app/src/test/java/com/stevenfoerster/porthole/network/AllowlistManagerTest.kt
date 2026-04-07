package com.stevenfoerster.porthole.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AllowlistManagerTest {

    private lateinit var manager: AllowlistManager

    @Before
    fun setUp() {
        manager = AllowlistManager()
    }

    @Test
    fun `initialize sets gateway in allowlist`() {
        manager.initialize("192.168.1.1", strict = true)

        assertTrue(manager.allowedHosts.value.contains("192.168.1.1"))
    }

    @Test
    fun `isAllowed returns true for gateway`() {
        manager.initialize("192.168.1.1", strict = true)

        assertTrue(manager.isAllowed("http://192.168.1.1/login"))
    }

    @Test
    fun `isAllowed returns false for external host in strict mode`() {
        manager.initialize("192.168.1.1", strict = true)

        assertFalse(manager.isAllowed("https://example.com/auth"))
    }

    @Test
    fun `isAllowed returns false for invalid URL`() {
        manager.initialize("192.168.1.1", strict = true)

        assertFalse(manager.isAllowed("not a url"))
    }

    @Test
    fun `addHost is rejected in strict mode`() {
        manager.initialize("192.168.1.1", strict = true)

        assertFalse(manager.addHost("example.com"))
        assertFalse(manager.allowedHosts.value.contains("example.com"))
    }

    @Test
    fun `addHost is accepted in permissive mode`() {
        manager.initialize("192.168.1.1", strict = false)

        assertTrue(manager.addHost("portal.example.com"))
        assertTrue(manager.allowedHosts.value.contains("portal.example.com"))
    }

    @Test
    fun `isAllowed returns true after addHost in permissive mode`() {
        manager.initialize("192.168.1.1", strict = false)
        manager.addHost("portal.example.com")

        assertTrue(manager.isAllowed("https://portal.example.com/login"))
    }

    @Test
    fun `clear removes all hosts`() {
        manager.initialize("192.168.1.1", strict = false)
        manager.addHost("portal.example.com")

        manager.clear()

        assertTrue(manager.allowedHosts.value.isEmpty())
    }

    @Test
    fun `extractHost parses URL correctly`() {
        assertEquals("example.com", AllowlistManager.extractHost("https://example.com/path"))
        assertEquals("192.168.1.1", AllowlistManager.extractHost("http://192.168.1.1/login"))
        assertNull(AllowlistManager.extractHost("not a url at all"))
    }

    @Test
    fun `isPrivateAddress identifies RFC 1918 ranges`() {
        assertTrue(AllowlistManager.isPrivateAddress("192.168.1.1"))
        assertTrue(AllowlistManager.isPrivateAddress("10.0.0.1"))
        assertTrue(AllowlistManager.isPrivateAddress("172.16.0.1"))
        assertTrue(AllowlistManager.isPrivateAddress("127.0.0.1"))
    }

    @Test
    fun `isPrivateAddress rejects public addresses`() {
        assertFalse(AllowlistManager.isPrivateAddress("8.8.8.8"))
        assertFalse(AllowlistManager.isPrivateAddress("1.1.1.1"))
    }

    @Test
    fun `isAllowed auto-adds private IP hosts`() {
        manager.initialize("192.168.1.1", strict = true)

        // Another private IP should be auto-allowed
        assertTrue(manager.isAllowed("http://10.0.0.1/portal"))
        assertTrue(manager.allowedHosts.value.contains("10.0.0.1"))
    }
}
