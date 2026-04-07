package com.stevenfoerster.porthole.session

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionConfigTest {

    @Test
    fun `default config has expected values`() {
        val config = SessionConfig()
        assertEquals(SessionConfig.DEFAULT_TIMEOUT_SECONDS, config.timeoutSeconds)
        assertEquals(false, config.jsEnabled)
        assertEquals(true, config.strictMode)
    }

    @Test
    fun `effectiveTimeoutSeconds clamps to max`() {
        val config = SessionConfig(timeoutSeconds = 9999)
        assertEquals(SessionConfig.MAX_TIMEOUT_SECONDS, config.effectiveTimeoutSeconds)
    }

    @Test
    fun `effectiveTimeoutSeconds clamps to min`() {
        val config = SessionConfig(timeoutSeconds = 1)
        assertEquals(SessionConfig.MIN_TIMEOUT_SECONDS, config.effectiveTimeoutSeconds)
    }

    @Test
    fun `effectiveTimeoutSeconds preserves valid values`() {
        val config = SessionConfig(timeoutSeconds = 120)
        assertEquals(120, config.effectiveTimeoutSeconds)
    }

    @Test
    fun `max timeout is 600 seconds (10 minutes)`() {
        assertEquals(600, SessionConfig.MAX_TIMEOUT_SECONDS)
    }
}
