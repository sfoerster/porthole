package com.stevenfoerster.porthole.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stevenfoerster.porthole.session.SessionState
import com.stevenfoerster.porthole.ui.viewmodel.MainViewModel

/**
 * Main screen showing session state, gateway status, and the launch button.
 *
 * @param viewModel The [MainViewModel] providing state and actions.
 * @param onLaunchSession Callback invoked when the session starts successfully,
 *   with the gateway IP as the parameter.
 * @param onNavigateToSettings Callback to navigate to the settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onLaunchSession: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sessionState by viewModel.sessionState.collectAsState()
    val gatewayIp by viewModel.gatewayIp.collectAsState()
    val launchError by viewModel.launchError.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(launchError) {
        launchError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Porthole") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Gateway status card
            GatewayStatusCard(
                gatewayIp = gatewayIp,
                onRefresh = { viewModel.refreshGateway() },
            )

            // Session state card
            SessionStateCard(
                sessionState = sessionState,
                remainingSeconds = remainingSeconds,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Launch button
            Button(
                onClick = {
                    val gateway = viewModel.launchSession()
                    if (gateway != null) {
                        onLaunchSession(gateway)
                    }
                },
                enabled = sessionState == SessionState.IDLE && gatewayIp != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Wifi,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Launch Portal Browser",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (gatewayIp == null) {
                Text(
                    text = "Connect to a WiFi network to detect a captive portal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GatewayStatusCard(
    gatewayIp: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (gatewayIp != null) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        ),
        onClick = onRefresh,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (gatewayIp != null) {
                    Icons.Filled.CheckCircle
                } else {
                    Icons.Filled.Error
                },
                contentDescription = null,
                tint = if (gatewayIp != null) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onErrorContainer
                },
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (gatewayIp != null) "Gateway Detected" else "No Gateway",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = gatewayIp ?: "Tap to refresh",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SessionStateCard(
    sessionState: SessionState,
    remainingSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val (label, color) = when (sessionState) {
        SessionState.IDLE -> "Ready" to MaterialTheme.colorScheme.outline
        SessionState.ACTIVE -> "Active ($remainingSeconds s)" to Color(0xFF388E3C)
        SessionState.EXPIRED -> "Expired" to MaterialTheme.colorScheme.error
        SessionState.CLOSED -> "Closed" to MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Session:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = color,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
