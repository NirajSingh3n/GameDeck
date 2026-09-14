package com.gamedeck.app.ui

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gamedeck.app.core.SystemStats
import com.gamedeck.app.data.ProfileStore
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun ConsoleScreen(
    store: ProfileStore,
    onLaunchPUBG: () -> Unit,
    onStartOverlay: () -> Unit,
    onStartRecording: () -> Unit
) {
    val context = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            refresh++
        }
    }

    val tick = refresh // read state so the timer triggers recomposition
    val temp = SystemStats.cpuTempC()
    val ram = SystemStats.ram(context)
    val batt = SystemStats.batteryPct(context)
    val storeFree = SystemStats.storage()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "GAMEDECK",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Text("Armoury-style gaming console", color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                "CPU TEMP",
                if (temp != null) String.format(Locale.US, "%.1f°C", temp) else "N/A",
                Modifier.weight(1f)
            )
            StatCard("BATTERY", "$batt%", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                "RAM FREE",
                "${ram.first / 1048576} MB",
                Modifier.weight(1f)
            )
            StatCard(
                "STORAGE FREE",
                "${storeFree.first / 1073741824} GB",
                Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(16.dp))

        Text("PERFORMANCE MODE", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        var mode by remember { mutableStateOf(store.performanceMode) }
        val modes = listOf("Balanced", "Performance", "Ultra", "Esports")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            modes.take(2).forEach { m ->
                ModeChip(m, mode == m, Modifier.weight(1f)) {
                    mode = m
                    store.performanceMode = m
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            modes.drop(2).forEach { m ->
                ModeChip(m, mode == m, Modifier.weight(1f)) {
                    mode = m
                    store.performanceMode = m
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "True CPU/GPU clock control and touch-sampling changes are firmware-level — only ROG/iQOO system software can do them. These modes apply the best tuning available to third-party apps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))

        Button(onClick = onLaunchPUBG, modifier = Modifier.fillMaxWidth()) {
            Text("🚀 LAUNCH PUBG")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onStartOverlay, modifier = Modifier.fillMaxWidth()) {
            Text("🎮 GAME GENIE OVERLAY (FPS / TRIGGERS / DND)")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onStartRecording, modifier = Modifier.fillMaxWidth()) {
            Text("⏺ SCREEN RECORDING")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, modifier = modifier)
}
