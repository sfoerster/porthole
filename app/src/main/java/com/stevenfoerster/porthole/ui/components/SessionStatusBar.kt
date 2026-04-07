package com.stevenfoerster.porthole.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Persistent status bar shown during an active Porthole session.
 *
 * Displays the countdown timer, a tunnel bypass warning in red, and a close button.
 * This composable is designed to be placed at the top of the portal screen.
 *
 * @param remainingSeconds Seconds remaining in the session.
 * @param isConnected Whether internet connectivity has been detected.
 * @param onClose Callback invoked when the user taps the close button.
 */
@Composable
fun SessionStatusBar(
    remainingSeconds: Int,
    isConnected: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val minutes = remainingSeconds / SECONDS_PER_MINUTE
    val seconds = remainingSeconds % SECONDS_PER_MINUTE

    val backgroundColor =
        when {
            isConnected -> TunnelStatusGreen
            remainingSeconds <= LOW_TIME_THRESHOLD_SECONDS -> TunnelStatusRed
            remainingSeconds <= MEDIUM_TIME_THRESHOLD_SECONDS -> TunnelStatusAmber
            else -> TunnelStatusRed
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isConnected) "CONNECTED" else "OUTSIDE TUNNEL",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )

        Text(
            text = String.format("%d:%02d", minutes, seconds),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            style = MaterialTheme.typography.headlineSmall,
        )

        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close session",
                tint = Color.White,
            )
        }
    }
}

private val TunnelStatusRed = Color(0xFFD32F2F)
private val TunnelStatusAmber = Color(0xFFF57C00)
private val TunnelStatusGreen = Color(0xFF388E3C)

private const val SECONDS_PER_MINUTE = 60
private const val LOW_TIME_THRESHOLD_SECONDS = 30
private const val MEDIUM_TIME_THRESHOLD_SECONDS = 60
