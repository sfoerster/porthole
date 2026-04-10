package com.stevenfoerster.porthole.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.HttpURLConnection

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityCheckerTest {
    private val connectionFactory = mockk<HttpUrlConnectionFactory>()
    private val checker = ConnectivityChecker(connectionFactory)

    @Test
    fun `checkConnectivity falls back when default probe fails`() =
        runTest {
            val primaryConnection = mockk<HttpURLConnection>(relaxed = true)
            val fallbackConnection = mockk<HttpURLConnection>(relaxed = true)

            every { connectionFactory.open(ConnectivityChecker.DEFAULT_CHECK_URL) } returns primaryConnection
            every { connectionFactory.open(ConnectivityChecker.FALLBACK_CHECK_URL) } returns fallbackConnection
            every { primaryConnection.responseCode } returns 302
            every { fallbackConnection.responseCode } returns 204

            assertTrue(checker.checkConnectivity().first())

            verify(exactly = 1) { connectionFactory.open(ConnectivityChecker.DEFAULT_CHECK_URL) }
            verify(exactly = 1) { connectionFactory.open(ConnectivityChecker.FALLBACK_CHECK_URL) }
            verify(exactly = 1) { primaryConnection.disconnect() }
            verify(exactly = 1) { fallbackConnection.disconnect() }
        }

    @Test
    fun `checkConnectivity does not fall back for custom probe url`() =
        runTest {
            val customUrl = "https://portal.example.com/generate_204"
            val connection = mockk<HttpURLConnection>(relaxed = true)

            every { connectionFactory.open(customUrl) } returns connection
            every { connection.responseCode } returns 302

            assertFalse(checker.checkConnectivity(customUrl).first())

            verify(exactly = 1) { connectionFactory.open(customUrl) }
            verify(exactly = 0) { connectionFactory.open(ConnectivityChecker.FALLBACK_CHECK_URL) }
            verify(exactly = 1) { connection.disconnect() }
        }
}
