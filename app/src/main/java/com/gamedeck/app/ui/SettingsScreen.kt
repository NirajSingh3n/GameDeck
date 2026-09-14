package com.gamedeck.app.ui

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onGrantOverlay: () -> Unit,
    onGrantDnd: () -> Unit,
    onGrantWrite: () -> Unit,
    onGrantAccessibility: () -> Unit
) {
    val context = LocalContext.current
    val accEnabled = remember {
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )?.contains("com.gamedeck.app") == true
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "PERMISSIONS & SETUP",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        PermRow("Overlay (Game Genie bubble)", Settings.canDrawOverlays(context), onGrantOverlay)
        if (Build.VERSION.SDK_INT >= 23) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            PermRow("Do Not Disturb access", nm.isNotificationPolicyAccessGranted, onGrantDnd)
            PermRow("Write Settings (brightness)", Settings.System.canWrite(context), onGrantWrite)
        }
        PermRow("Accessibility (shoulder triggers)", accEnabled, onGrantAccessibility)

        Spacer(Modifier.height(16.dp))
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("About GameDeck", color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(6.dp))
                Text(
                    "GameDeck replicates the structure of ROG Armoury Crate and iQOO Ultra Game Mode. " +
                            "Features like CPU/GPU clock control, real touch-sampling rate changes and kernel-level " +
                            "thermal tuning are restricted to device firmware — no third-party app can perform them. " +
                            "GameDeck implements every feature that is accessible to third-party apps: live FPS meter, " +
                            "Do Not Disturb, brightness control, screen recording, shoulder triggers via accessibility, " +
                            "game library and PUBG profiles."
                )
            }
        }
    }
}

@Composable
fun PermRow(name: String, granted: Boolean, onGrant: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(name)
                Text(
                    if (granted) "GRANTED ✓" else "NOT GRANTED",
                    color = if (granted) Color(0xFF00E676) else Color(0xFFFF5252),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (!granted) Button(onClick = onGrant) { Text("GRANT") }
        }
    }
}
