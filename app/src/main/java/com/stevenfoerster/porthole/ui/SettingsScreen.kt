package com.stevenfoerster.porthole.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stevenfoerster.porthole.session.SessionConfig
import com.stevenfoerster.porthole.ui.viewmodel.SettingsViewModel

/** Ordered list of timeout options for the slider, in seconds. */
private val TIMEOUT_OPTIONS = listOf(30, 60, 120, 300)

/** Number of slider steps (positions between first and last values). */
private const val TIMEOUT_SLIDER_STEPS = 2

/**
 * Settings screen for configuring Porthole session behavior.
 *
 * Provides controls for timeout duration, JavaScript toggle, strict/permissive mode,
 * and connectivity check URL override.
 *
 * @param viewModel The [SettingsViewModel] providing state and actions.
 * @param onNavigateBack Callback to navigate back to the main screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val config by viewModel.sessionConfig.collectAsState()
    val connectivityUrl by viewModel.connectivityCheckUrl.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Timeout slider
            SettingsCard(title = "Session Timeout") {
                val currentIndex = TIMEOUT_OPTIONS.indexOf(config.timeoutSeconds)
                    .coerceAtLeast(0)

                Text(
                    text = formatTimeout(config.timeoutSeconds),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Slider(
                    value = currentIndex.toFloat(),
                    onValueChange = { value ->
                        val index = value.toInt().coerceIn(0, TIMEOUT_OPTIONS.lastIndex)
                        viewModel.setTimeoutSeconds(TIMEOUT_OPTIONS[index])
                    },
                    valueRange = 0f..TIMEOUT_OPTIONS.lastIndex.toFloat(),
                    steps = TIMEOUT_SLIDER_STEPS,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = "Maximum session duration before auto-close. " +
                        "Hard limit: ${SessionConfig.MAX_TIMEOUT_SECONDS / 60} minutes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // JavaScript toggle
            SettingsCard(title = "JavaScript") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (config.jsEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = config.jsEnabled,
                        onCheckedChange = { viewModel.setJsEnabled(it) },
                    )
                }

                if (config.jsEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Warning: Enabling JavaScript increases the attack surface. " +
                            "A hostile portal operator could execute scripts in the WebView. " +
                            "Only enable this if the portal requires it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "JavaScript is disabled for security. " +
                            "Some portals may require it to function.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Strict/Permissive mode
            SettingsCard(title = "Navigation Mode") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (config.strictMode) "Strict" else "Permissive",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = config.strictMode,
                        onCheckedChange = { viewModel.setStrictMode(it) },
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (config.strictMode) {
                        "Strict mode: Only local network addresses (RFC 1918) and the WiFi " +
                            "gateway are allowed. Most captive portals work in this mode."
                    } else {
                        "Permissive mode: External domains are allowed with your confirmation. " +
                            "Use this if the portal redirects to an external authentication " +
                            "service (e.g., social login)."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Connectivity check URL
            SettingsCard(title = "Connectivity Check URL") {
                OutlinedTextField(
                    value = connectivityUrl,
                    onValueChange = { viewModel.setConnectivityCheckUrl(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("URL") },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "URL polled to detect when captive portal authentication succeeds. " +
                        "Should return HTTP 204 when internet is available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

private fun formatTimeout(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (remainingSeconds == 0) {
        "${minutes}m"
    } else {
        "${minutes}m ${remainingSeconds}s"
    }
}
