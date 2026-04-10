package com.stevenfoerster.porthole.ui

import com.stevenfoerster.porthole.session.SessionState
import org.junit.Assert.assertEquals
import org.junit.Test

class PortalSessionFinalizerTest {
    @Test
    fun `active disposal closes then cleans up without navigation callback`() {
        val calls = mutableListOf<String>()
        val finalizer =
            PortalSessionFinalizer(
                closeActiveSession = { calls += "close" },
                cleanupSession = { calls += "cleanup" },
                resetToIdle = { calls += "reset" },
                onSessionEnded = { calls += "ended" },
                onSessionDisposed = { calls += "disposed" },
            )

        finalizer.handleDisposal(SessionState.ACTIVE)
        finalizer.handleStateChange(SessionState.CLOSED)

        assertEquals(listOf("close", "cleanup", "reset", "disposed"), calls)
    }

    @Test
    fun `closed session finalizes once and notifies normal end`() {
        val calls = mutableListOf<String>()
        val finalizer =
            PortalSessionFinalizer(
                closeActiveSession = { calls += "close" },
                cleanupSession = { calls += "cleanup" },
                resetToIdle = { calls += "reset" },
                onSessionEnded = { calls += "ended" },
                onSessionDisposed = { calls += "disposed" },
            )

        finalizer.handleStateChange(SessionState.CLOSED)
        finalizer.handleDisposal(SessionState.CLOSED)

        assertEquals(listOf("cleanup", "reset", "ended"), calls)
    }
}
