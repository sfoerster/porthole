package com.stevenfoerster.porthole.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * First-run setup screen shown before the user can access the main app.
 *
 * Explains what Porthole does, how to configure VPN exclusion, and clearly
 * states that Porthole operates outside the VPN tunnel. The user must
 * acknowledge understanding before proceeding.
 *
 * @param onConfirm Callback invoked when the user taps "I understand".
 */
@Composable
fun FirstRunScreen(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Welcome to Porthole",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "A sandboxed captive portal browser for Android",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SectionCard(
            title = "What Porthole Does",
            content = "Porthole provides a safe, controlled way to authenticate with captive " +
                "portals (hotel WiFi, airport WiFi, coffee shops) without exposing your " +
                "regular browsing traffic. It opens a sandboxed browser session that is " +
                "isolated from the rest of your device.",
        )

        SectionCard(
            title = "What Porthole Does NOT Do",
            content = "Porthole is NOT a VPN and does NOT protect your traffic during a session. " +
                "While Porthole is active, the app intentionally operates outside your VPN " +
                "tunnel so it can reach the captive portal. All other apps remain protected " +
                "by your VPN.",
        )

        SectionCard(
            title = "Setup Required: VPN Exclusion",
            content = "For Porthole to work, you must add it to your VPN app's excluded " +
                "apps list (also called split tunneling or per-app exclusion).\n\n" +
                "In WireGuard for Android:\n" +
                "1. Open the WireGuard app\n" +
                "2. Tap your active tunnel\n" +
                "3. Tap the edit (pencil) icon\n" +
                "4. Scroll down to \"Excluded Applications\"\n" +
                "5. Check \"Porthole\" in the app list\n" +
                "6. Save the tunnel configuration\n\n" +
                "This ensures Porthole's traffic bypasses the tunnel while all " +
                "other apps remain protected.",
        )

        SectionCard(
            title = "Security Properties",
            content = "• Sessions are time-limited (max 10 minutes)\n" +
                "• The browser is destroyed after each session\n" +
                "• All cookies and data are wiped on session end\n" +
                "• JavaScript is disabled by default\n" +
                "• Navigation is restricted to local network addresses\n" +
                "• A persistent notification warns you when the tunnel is bypassed",
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            Text(
                text = "I Understand",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
