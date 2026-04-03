package com.flowscale.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.flowscale.app.RatingViewModel

@Composable
fun RatingScreen(viewModel: RatingViewModel, modifier: Modifier = Modifier) {
    val currentValue by viewModel.currentValue.collectAsState()
    val volumeKeysEnabled by viewModel.volumeKeysEnabled.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()
    val windowMinutes by viewModel.windowMinutes.collectAsState()
    val nowMillis by viewModel.nowMillis.collectAsState()

    val stepLabel = formatRating(RatingViewModel.STEP)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Text(
            text = formatRating(currentValue),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.semantics {
                contentDescription = "Aktuelle Intensität: ${formatRating(currentValue)}"
            },
        )

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Button(
                onClick = { viewModel.decrement() },
                modifier = Modifier
                    .size(width = 96.dp, height = 56.dp)
                    .semantics { contentDescription = "Intensität um $stepLabel verringern" },
            ) {
                Text("− $stepLabel", style = MaterialTheme.typography.titleMedium)
            }

            Button(
                onClick = { viewModel.increment() },
                modifier = Modifier
                    .size(width = 96.dp, height = 56.dp)
                    .semantics { contentDescription = "Intensität um $stepLabel erhöhen" },
            ) {
                Text("+ $stepLabel", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(48.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { viewModel.toggleVolumeKeys() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (volumeKeysEnabled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (volumeKeysEnabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
            ) {
                Text(if (volumeKeysEnabled) "Lautstärketasten → Intensität" else "Lautstärketasten → Lautstärke")
            }

            Button(
                onClick = { viewModel.toggleKeepScreenOn() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (keepScreenOn) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (keepScreenOn) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
            ) {
                Text(if (keepScreenOn) "Bildschirm: immer an" else "Bildschirm: auto")
            }
        }

        Spacer(Modifier.height(32.dp))

        TimeWindowDropdown(
            selectedMinutes = windowMinutes,
            onSelect = { viewModel.setWindowMinutes(it) },
        )

        Spacer(Modifier.height(16.dp))

        IntensityChart(
            records = recentRecords,
            nowMillis = nowMillis,
            windowMillis = windowMinutes.toLong() * 60 * 1_000,
            currentIntensity = currentValue,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

private val WINDOW_OPTIONS = listOf(1, 5, 10, 30, 60, 120)

@Composable
private fun TimeWindowDropdown(
    selectedMinutes: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Zeitfenster: $selectedMinutes min")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            WINDOW_OPTIONS.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text("$minutes min") },
                    onClick = {
                        onSelect(minutes)
                        expanded = false
                    },
                )
            }
        }
    }
}
