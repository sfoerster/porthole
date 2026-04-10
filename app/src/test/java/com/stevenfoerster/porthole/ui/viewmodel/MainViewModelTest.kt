package com.stevenfoerster.porthole.ui.viewmodel

import com.stevenfoerster.porthole.network.GatewayResolver
import com.stevenfoerster.porthole.session.SessionConfig
import com.stevenfoerster.porthole.session.SessionManager
import com.stevenfoerster.porthole.session.SessionState
import com.stevenfoerster.porthole.settings.PortholePreferences
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val sessionManager = mockk<SessionManager>()
    private val gatewayResolver = mockk<GatewayResolver>()
    private val preferences = mockk<PortholePreferences>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { sessionManager.state } returns MutableStateFlow(SessionState.IDLE)
        every { sessionManager.remainingSeconds } returns MutableStateFlow(0)
        every { gatewayResolver.resolve() } returns null
        every { preferences.sessionConfig } returns flowOf(SessionConfig())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `firstRunCompleted defaults to false before preferences emit`() {
        every { preferences.firstRunCompleted } returns emptyFlow()

        val viewModel = MainViewModel(sessionManager, gatewayResolver, preferences)

        assertFalse(viewModel.firstRunCompleted.value)
    }
}
