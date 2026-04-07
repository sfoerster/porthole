package com.stevenfoerster.porthole.session

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var sessionTimer: SessionTimer
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        sessionTimer = SessionTimer()
        sessionManager = SessionManager(sessionTimer, testScope)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(SessionState.IDLE, sessionManager.state.value)
        assertEquals(0, sessionManager.remainingSeconds.value)
        assertNull(sessionManager.activeConfig)
    }

    @Test
    fun `startSession transitions from IDLE to ACTIVE`() =
        testScope.runTest {
            val config = SessionConfig(timeoutSeconds = 60)
            val started = sessionManager.startSession(config)

            assertTrue(started)
            assertEquals(SessionState.ACTIVE, sessionManager.state.value)
            assertEquals(60, sessionManager.remainingSeconds.value)
            assertEquals(config, sessionManager.activeConfig)
        }

    @Test
    fun `startSession returns false when already ACTIVE`() =
        testScope.runTest {
            val config = SessionConfig(timeoutSeconds = 60)
            sessionManager.startSession(config)
            advanceTimeBy(SessionTimer.TICK_INTERVAL_MS)

            val secondStart = sessionManager.startSession(config)
            assertFalse(secondStart)
        }

    @Test
    fun `closeSession transitions from ACTIVE to CLOSED`() =
        testScope.runTest {
            sessionManager.startSession(SessionConfig(timeoutSeconds = 60))
            advanceTimeBy(SessionTimer.TICK_INTERVAL_MS)

            sessionManager.closeSession()

            assertEquals(SessionState.CLOSED, sessionManager.state.value)
            assertEquals(0, sessionManager.remainingSeconds.value)
        }

    @Test
    fun `closeSession does nothing when IDLE`() {
        sessionManager.closeSession()
        assertEquals(SessionState.IDLE, sessionManager.state.value)
    }

    @Test
    fun `resetToIdle transitions from CLOSED to IDLE`() =
        testScope.runTest {
            sessionManager.startSession(SessionConfig(timeoutSeconds = 60))
            advanceTimeBy(SessionTimer.TICK_INTERVAL_MS)
            sessionManager.closeSession()

            sessionManager.resetToIdle()

            assertEquals(SessionState.IDLE, sessionManager.state.value)
            assertNull(sessionManager.activeConfig)
        }

    @Test
    fun `resetToIdle does nothing when ACTIVE`() =
        testScope.runTest {
            sessionManager.startSession(SessionConfig(timeoutSeconds = 60))
            advanceTimeBy(SessionTimer.TICK_INTERVAL_MS)

            sessionManager.resetToIdle()

            assertEquals(SessionState.ACTIVE, sessionManager.state.value)
        }

    @Test
    fun `session expires when timer reaches zero`() =
        testScope.runTest {
            val timeoutSeconds = SessionConfig.MIN_TIMEOUT_SECONDS
            sessionManager.startSession(SessionConfig(timeoutSeconds = timeoutSeconds))

            // Advance past the full countdown duration
            advanceTimeBy((timeoutSeconds + 1).toLong() * SessionTimer.TICK_INTERVAL_MS)

            assertEquals(SessionState.EXPIRED, sessionManager.state.value)
            assertEquals(0, sessionManager.remainingSeconds.value)
        }

    @Test
    fun `resetToIdle works after EXPIRED`() =
        testScope.runTest {
            val timeoutSeconds = SessionConfig.MIN_TIMEOUT_SECONDS
            sessionManager.startSession(SessionConfig(timeoutSeconds = timeoutSeconds))
            advanceTimeBy((timeoutSeconds + 1).toLong() * SessionTimer.TICK_INTERVAL_MS)

            assertEquals(SessionState.EXPIRED, sessionManager.state.value)

            sessionManager.resetToIdle()
            assertEquals(SessionState.IDLE, sessionManager.state.value)
        }

    @Test
    fun `remaining seconds counts down`() =
        testScope.runTest {
            val timeout = SessionConfig.MIN_TIMEOUT_SECONDS
            sessionManager.startSession(SessionConfig(timeoutSeconds = timeout))

            advanceTimeBy(SessionTimer.TICK_INTERVAL_MS)
            assertEquals(timeout, sessionManager.remainingSeconds.value)

            advanceTimeBy(SessionTimer.TICK_INTERVAL_MS)
            assertEquals(timeout - 1, sessionManager.remainingSeconds.value)

            advanceTimeBy(SessionTimer.TICK_INTERVAL_MS)
            assertEquals(timeout - 2, sessionManager.remainingSeconds.value)
        }
}
