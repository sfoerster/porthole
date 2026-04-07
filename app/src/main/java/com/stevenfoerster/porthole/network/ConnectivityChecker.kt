package com.stevenfoerster.porthole.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Polls a connectivity check URL to detect when captive portal authentication has succeeded.
 *
 * The check URL should return HTTP 204 when the device has unrestricted internet access.
 * During captive portal interception, the URL typically returns a redirect (302) or
 * a portal login page (200 with HTML body).
 *
 * Uses [HttpURLConnection] directly — no third-party networking libraries.
 */
@Singleton
class ConnectivityChecker
    @Inject
    constructor() {
        /**
         * Emits a [Flow] of booleans indicating whether internet connectivity
         * has been established (i.e., captive portal authentication succeeded).
         *
         * @param checkUrl The URL to poll. Defaults to [DEFAULT_CHECK_URL].
         * @return A [Flow] that emits true when a 204 response is received, false otherwise.
         */
        fun checkConnectivity(checkUrl: String = DEFAULT_CHECK_URL): Flow<Boolean> =
            flow {
                while (currentCoroutineContext().isActive) {
                    val connected = performCheck(checkUrl)
                    emit(connected)
                    if (connected) return@flow
                    delay(POLL_INTERVAL_MS)
                }
            }

        private suspend fun performCheck(checkUrl: String): Boolean =
            withContext(Dispatchers.IO) {
                try {
                    val url = URL(checkUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.instanceFollowRedirects = false
                    connection.connectTimeout = CONNECTION_TIMEOUT_MS
                    connection.readTimeout = READ_TIMEOUT_MS
                    connection.useCaches = false
                    try {
                        connection.connect()
                        connection.responseCode == HTTP_NO_CONTENT
                    } finally {
                        connection.disconnect()
                    }
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    false
                }
            }

        companion object {
            /** Primary connectivity check URL. */
            const val DEFAULT_CHECK_URL = "https://connectivitycheck.stevenfoerster.com/generate_204"

            /** Fallback connectivity check URL (Google's public endpoint). */
            const val FALLBACK_CHECK_URL = "http://connectivitycheck.gstatic.com/generate_204"

            /** Polling interval between connectivity checks in milliseconds. */
            const val POLL_INTERVAL_MS = 5_000L

            /** HTTP connection timeout in milliseconds. */
            private const val CONNECTION_TIMEOUT_MS = 5_000

            /** HTTP read timeout in milliseconds. */
            private const val READ_TIMEOUT_MS = 5_000

            /** HTTP 204 No Content — the expected response for successful connectivity. */
            private const val HTTP_NO_CONTENT = 204
        }
    }
