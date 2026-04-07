package com.stevenfoerster.porthole.session

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import javax.inject.Inject

/**
 * Coroutine-based countdown timer for Porthole sessions.
 *
 * Emits the remaining seconds as a [Flow<Int>], ticking once per second.
 * When the countdown reaches zero, the flow completes, signalling session expiry.
 * The hard maximum of [SessionConfig.MAX_TIMEOUT_SECONDS] is enforced here
 * as a defense-in-depth measure regardless of the input value.
 */
class SessionTimer
    @Inject
    constructor() {
        /**
         * Starts a countdown from [durationSeconds] to zero.
         *
         * @param durationSeconds The starting duration. Clamped to
         *   [SessionConfig.MAX_TIMEOUT_SECONDS] as a hard ceiling.
         * @return A cold [Flow] that emits remaining seconds each tick and completes at zero.
         */
        fun startCountdown(durationSeconds: Int): Flow<Int> =
            flow {
                val clamped = durationSeconds.coerceAtMost(SessionConfig.MAX_TIMEOUT_SECONDS)
                var remaining = clamped
                while (remaining > 0 && currentCoroutineContext().isActive) {
                    emit(remaining)
                    delay(TICK_INTERVAL_MS)
                    remaining--
                }
                emit(0)
            }

        companion object {
            /** Interval between countdown ticks in milliseconds. */
            const val TICK_INTERVAL_MS = 1_000L
        }
    }
