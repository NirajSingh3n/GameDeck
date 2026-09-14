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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamedeck.app.data.ProfileStore

@Composable
fun ProfileScreen(store: ProfileStore) {
    var touch by remember { mutableStateOf(store.touchSensitivity.toFloat()) }
    var aim by remember { mutableStateOf(store.aimSensitivity.toFloat()) }
    var gyro by remember { mutableStateOf(store.gyroSensitivity.toFloat()) }
    var dnd by remember { mutableStateOf(store.dndOnLaunch) }
    var overlay by remember { mutableStateOf(store.overlayOnLaunch) }
    var triggers by remember { mutableStateOf(store.triggersEnabled) }
    var voice by remember { mutableStateOf(store.voiceChanger) }
    var net by remember { mutableStateOf(store.networkBoost) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "PUBG GAME PROFILE",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text("AirTriggers / Monster Touch style configuration")
        Spacer(Modifier.height(16.dp))

        SensiSlider("Touch Sensitivity", touch) { touch = it; store.touchSensitivity = it.toInt() }
        SensiSlider("Aim Smoothing", aim) { aim = it; store.aimSensitivity = it.toInt() }
        SensiSlider("Gyro Sensitivity", gyro) { gyro = it; store.gyroSensitivity = it.toInt() }

        Spacer(Modifier.height(8.dp))
        ToggleRow("Auto Do Not Disturb on launch", dnd) { dnd = it; store.dndOnLaunch = it }
        ToggleRow("Auto-start Game Genie overlay", overlay) { overlay = it; store.overlayOnLaunch = it }
        ToggleRow("Shoulder triggers (L / R air triggers)", triggers) { triggers = it; store.triggersEnabled = it }
        ToggleRow("Voice changer (cosmetic preset)", voice) { voice = it; store.voiceChanger = it }
        ToggleRow("Network priority boost (cosmetic preset)", net) { net = it; store.networkBoost = it }

        Spacer(Modifier.height(12.dp))
        Text(
            "Enable the Game Genie overlay, then use the floating L / R buttons as shoulder triggers. Taps are performed through the accessibility service — grant it in Settings → Accessibility → GameDeck Trigger.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SensiSlider(label: String, value: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            Text(value.toInt().toString(), color = MaterialTheme.colorScheme.secondary)
        }
        Slider(value = value, onValueChange = onChange, valueRange = 0f..100f)
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
