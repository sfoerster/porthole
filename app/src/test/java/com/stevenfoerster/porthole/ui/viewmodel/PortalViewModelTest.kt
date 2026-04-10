package com.stevenfoerster.porthole.ui.viewmodel

import com.stevenfoerster.porthole.network.AllowlistManager
import com.stevenfoerster.porthole.network.ConnectivityChecker
import com.stevenfoerster.porthole.session.SessionManager
import com.stevenfoerster.porthole.session.SessionState
import com.stevenfoerster.porthole.settings.PortholePreferences
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PortalViewModelTest {
    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val allowlistManager = mockk<AllowlistManager>(relaxed = true)
    private val connectivityChecker = mockk<ConnectivityChecker>()
    private val preferences = mockk<PortholePreferences>()
    private lateinit var viewModel: PortalViewModel

    @Before
    fun setUp() {
        every { sessionManager.state } returns MutableStateFlow(SessionState.IDLE)
        every { sessionManager.remainingSeconds } returns MutableStateFlow(0)
        every { allowlistManager.allowedHosts } returns MutableStateFlow(emptySet())
        every { preferences.connectivityCheckUrl } returns flowOf("https://portal.example.com/generate_204")
        every { allowlistManager.initialize(any(), any()) } just runs
        every { allowlistManager.clear() } just runs

        viewModel =
            PortalViewModel(
                sessionManager = sessionManager,
                allowlistManager = allowlistManager,
                connectivityChecker = connectivityChecker,
                preferences = preferences,
            )
    }

    @Test
    fun `initializeSession uses persisted connectivity check url`() =
        runTest {
            every {
                connectivityChecker.checkConnectivity("https://portal.example.com/generate_204")
            } returns flowOf(true)

            val connected = viewModel.initializeSession("192.168.1.1", strictMode = false).first()

            assertTrue(connected)
            verify(exactly = 1) { allowlistManager.initialize("192.168.1.1", false) }
            verify(exactly = 1) {
                connectivityChecker.checkConnectivity("https://portal.example.com/generate_204")
            }
        }

    @Test
    fun `resetToIdle clears session scoped state`() {
        viewModel.onNavigationBlocked("https://blocked.example.com")
        viewModel.setConnected(true)

        viewModel.resetToIdle()

        assertNull(viewModel.blockedUrl.value)
        assertFalse(viewModel.isConnected.value)
        verify(exactly = 1) { allowlistManager.clear() }
        verify(exactly = 1) { sessionManager.resetToIdle() }
    }
}
