package com.stevenfoerster.porthole.session

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTimerTest {

    private val timer = SessionTimer()

    @Test
    fun `countdown emits decreasing values ending at zero`() = runTest {
        timer.startCountdown(3).test {
            assertEquals(3, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(1, awaitItem())
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `countdown with zero emits only zero`() = runTest {
        timer.startCountdown(0).test {
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `countdown clamps to MAX_TIMEOUT_SECONDS`() = runTest {
        val overMax = SessionConfig.MAX_TIMEOUT_SECONDS + 100
        timer.startCountdown(overMax).test {
            val first = awaitItem()
            assertEquals(SessionConfig.MAX_TIMEOUT_SECONDS, first)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `countdown with one second emits 1 then 0`() = runTest {
        timer.startCountdown(1).test {
            assertEquals(1, awaitItem())
            assertEquals(0, awaitItem())
            awaitComplete()
        }
    }
}
