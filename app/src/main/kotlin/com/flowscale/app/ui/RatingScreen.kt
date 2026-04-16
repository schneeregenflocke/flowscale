package com.flowscale.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.flowscale.app.R
import com.flowscale.app.RatingViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun RatingScreen(viewModel: RatingViewModel, modifier: Modifier = Modifier) {
    val currentValue by viewModel.currentValue.collectAsState()
    val volumeKeysEnabled by viewModel.volumeKeysEnabled.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()
    val windowMinutes by viewModel.windowMinutes.collectAsState()
    val nowMillis by viewModel.nowMillis.collectAsState()
    val recordCount by viewModel.recordCount.collectAsState()
    val databaseSizeBytes by viewModel.databaseSizeBytes.collectAsState()

    val context = LocalContext.current
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.let { viewModel.exportCsv(it) }
        }
    }

    val stepLabel = formatRating(RatingViewModel.STEP)
    val intensityDescription = stringResource(R.string.current_intensity_description, formatRating(currentValue))
    val decreaseDescription = stringResource(R.string.decrease_intensity_description, stepLabel)
    val increaseDescription = stringResource(R.string.increase_intensity_description, stepLabel)

    var showAbout by remember { mutableStateOf(false) }
    var showLicenses by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = formatRating(currentValue),
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.semantics {
                contentDescription = intensityDescription
            },
        )

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Button(
                onClick = { viewModel.decrement() },
                modifier = Modifier
                    .size(width = 96.dp, height = 56.dp)
                    .semantics { contentDescription = decreaseDescription },
            ) {
                Text(
                    stringResource(R.string.decrease_button, stepLabel),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Button(
                onClick = { viewModel.increment() },
                modifier = Modifier
                    .size(width = 96.dp, height = 56.dp)
                    .semantics { contentDescription = increaseDescription },
            ) {
                Text(
                    stringResource(R.string.increase_button, stepLabel),
                    style = MaterialTheme.typography.titleMedium,
                )
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
                Text(
                    if (volumeKeysEnabled) {
                        stringResource(R.string.volume_keys_intensity)
                    } else {
                        stringResource(R.string.volume_keys_volume)
                    },
                )
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
                Text(
                    if (keepScreenOn) {
                        stringResource(R.string.screen_always_on)
                    } else {
                        stringResource(R.string.screen_auto)
                    },
                )
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

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                csvLauncher.launch("flowscale_$date.csv")
            },
        ) {
            Text(stringResource(R.string.export_csv_button))
        }

        Spacer(Modifier.height(16.dp))

        StorageInfoRow(recordCount = recordCount, databaseSizeBytes = databaseSizeBytes)
    }

        IconButton(
            onClick = { showAbout = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.info_button_description),
            )
        }
    }

    if (showAbout) {
        AboutBottomSheet(
            onDismiss = { showAbout = false },
            onOpenLicenses = {
                showAbout = false
                showLicenses = true
            },
        )
    }

    if (showLicenses) {
        LicensesDialog(onDismiss = { showLicenses = false })
    }
}

@Composable
private fun StorageInfoRow(recordCount: Long, databaseSizeBytes: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%,d".format(recordCount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.record_count_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatStorageSize(databaseSizeBytes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.storage_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatStorageSize(bytes: Long): String = when {
    bytes < 1_024L -> "$bytes B"
    bytes < 1_024L * 1_024 -> "%.1f KB".format(bytes / 1_024.0)
    else -> "%.1f MB".format(bytes / (1_024.0 * 1_024))
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
            Text(stringResource(R.string.time_window_label, selectedMinutes))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            WINDOW_OPTIONS.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.minutes_option, minutes)) },
                    onClick = {
                        onSelect(minutes)
                        expanded = false
                    },
                )
            }
        }
    }
}
