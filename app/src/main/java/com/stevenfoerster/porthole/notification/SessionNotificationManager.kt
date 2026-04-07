package com.stevenfoerster.porthole.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.stevenfoerster.porthole.R
import com.stevenfoerster.porthole.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the persistent notification displayed during active Porthole sessions.
 *
 * The notification serves as a constant reminder that the device is operating
 * outside the VPN tunnel. It cannot be dismissed by the user while a session
 * is active (achieved via [NotificationCompat.Builder.setOngoing]).
 *
 * The countdown is updated every [NOTIFICATION_UPDATE_INTERVAL_SECONDS] seconds
 * to avoid excessive notification updates while keeping the user informed.
 */
@Singleton
class SessionNotificationManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        init {
            createNotificationChannel()
        }

        /**
         * Builds the session notification with the current countdown.
         *
         * @param remainingSeconds The number of seconds remaining in the session.
         * @return A configured [Notification] ready to be posted.
         */
        fun buildNotification(remainingSeconds: Int): Notification {
            val minutes = remainingSeconds / SECONDS_PER_MINUTE
            val seconds = remainingSeconds % SECONDS_PER_MINUTE

            val tapIntent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    tapIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(
                    context.getString(R.string.notification_text_template, minutes, seconds),
                )
                .setContentIntent(pendingIntent)
                .setOngoing(true) // Cannot be dismissed during active session
                .setOnlyAlertOnce(true) // Don't re-alert on updates
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
        }

        /**
         * Updates the existing notification with a new countdown value.
         *
         * @param remainingSeconds The number of seconds remaining in the session.
         */
        fun updateNotification(remainingSeconds: Int) {
            notificationManager.notify(NOTIFICATION_ID, buildNotification(remainingSeconds))
        }

        /** Cancels the session notification. Called when the session ends. */
        fun cancelNotification() {
            notificationManager.cancel(NOTIFICATION_ID)
        }

        private fun createNotificationChannel() {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = context.getString(R.string.notification_channel_description)
                    setShowBadge(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            notificationManager.createNotificationChannel(channel)
        }

        companion object {
            /** Notification channel ID. */
            const val CHANNEL_ID = "porthole_session"

            /** Unique notification ID for the session notification. */
            const val NOTIFICATION_ID = 1

            /** How often the notification countdown text is refreshed, in seconds. */
            const val NOTIFICATION_UPDATE_INTERVAL_SECONDS = 10

            private const val SECONDS_PER_MINUTE = 60
        }
    }
