package com.stevenfoerster.porthole.notification

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service that keeps the session notification alive.
 *
 * Android requires a foreground service to post persistent, non-dismissible notifications.
 * This service has no other responsibility — all session logic lives in [SessionManager].
 */
@AndroidEntryPoint
class SessionForegroundService : Service() {
    @Inject
    lateinit var notificationManager: SessionNotificationManager

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val remainingSeconds = intent?.getIntExtra(EXTRA_REMAINING_SECONDS, 0) ?: 0

        when (intent?.action) {
            ACTION_START -> {
                val notification = notificationManager.buildNotification(remainingSeconds)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        SessionNotificationManager.NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                    )
                } else {
                    startForeground(SessionNotificationManager.NOTIFICATION_ID, notification)
                }
            }
            ACTION_UPDATE -> {
                notificationManager.updateNotification(remainingSeconds)
            }
            ACTION_STOP -> {
                notificationManager.cancelNotification()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        /** Intent action to start the foreground service. */
        const val ACTION_START = "com.stevenfoerster.porthole.action.START"

        /** Intent action to update the notification countdown. */
        const val ACTION_UPDATE = "com.stevenfoerster.porthole.action.UPDATE"

        /** Intent action to stop the foreground service. */
        const val ACTION_STOP = "com.stevenfoerster.porthole.action.STOP"

        /** Intent extra key for the remaining seconds value. */
        const val EXTRA_REMAINING_SECONDS = "remaining_seconds"

        /** Creates an intent to start the service with the given remaining seconds. */
        fun startIntent(
            context: Context,
            remainingSeconds: Int,
        ): Intent =
            Intent(context, SessionForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
            }

        /** Creates an intent to update the notification countdown. */
        fun updateIntent(
            context: Context,
            remainingSeconds: Int,
        ): Intent =
            Intent(context, SessionForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
            }

        /** Creates an intent to stop the service. */
        fun stopIntent(context: Context): Intent =
            Intent(context, SessionForegroundService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
