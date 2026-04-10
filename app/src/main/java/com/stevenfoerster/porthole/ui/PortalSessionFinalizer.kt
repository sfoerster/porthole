package com.stevenfoerster.porthole.ui

import com.stevenfoerster.porthole.session.SessionState

/**
 * Coordinates one-time portal session teardown across normal session end and unexpected disposal.
 */
internal class PortalSessionFinalizer(
    private val closeActiveSession: () -> Unit,
    private val cleanupSession: () -> Unit,
    private val resetToIdle: () -> Unit,
    private val onSessionEnded: () -> Unit,
    private val onSessionDisposed: () -> Unit,
) {
    private var finalized = false

    fun handleStateChange(sessionState: SessionState) {
        if (sessionState == SessionState.CLOSED || sessionState == SessionState.EXPIRED) {
            finalize(closeFirst = false, notifySessionEnded = true)
        }
    }

    fun handleDisposal(sessionState: SessionState) {
        if (sessionState != SessionState.IDLE) {
            finalize(
                closeFirst = sessionState == SessionState.ACTIVE,
                notifySessionEnded = false,
            )
        }
    }

    private fun finalize(
        closeFirst: Boolean,
        notifySessionEnded: Boolean,
    ) {
        if (finalized) return
        finalized = true

        if (closeFirst) {
            closeActiveSession()
        }

        cleanupSession()
        resetToIdle()

        if (notifySessionEnded) {
            onSessionEnded()
        } else {
            onSessionDisposed()
        }
    }
}
