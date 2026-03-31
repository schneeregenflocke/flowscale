package com.flowscale.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flowscale.app.RatingViewModel

@Composable
fun RatingScreen(viewModel: RatingViewModel, modifier: Modifier = Modifier) {
    val startValue by viewModel.startValue.collectAsState()
    val currentValue by viewModel.currentValue.collectAsState()
    val volumeKeysEnabled by viewModel.volumeKeysEnabled.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Text("Startwert", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        SpinBox(
            value = startValue,
            onValueChange = { viewModel.setStartValue(it) },
            minValue = RatingViewModel.MIN_VALUE,
            maxValue = RatingViewModel.MAX_VALUE,
            step = RatingViewModel.STEP,
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text = formatRating(currentValue),
            style = MaterialTheme.typography.displayLarge,
        )

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Button(
                onClick = { viewModel.decrement() },
                modifier = Modifier.size(width = 96.dp, height = 56.dp),
            ) {
                Text("− 0.25", style = MaterialTheme.typography.titleMedium)
            }

            Button(
                onClick = { viewModel.increment() },
                modifier = Modifier.size(width = 96.dp, height = 56.dp),
            ) {
                Text("+ 0.25", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(48.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { viewModel.toggleVolumeKeys() }) {
                Text(if (volumeKeysEnabled) "Lautstärketasten: Ein" else "Lautstärketasten: Aus")
            }

            Button(onClick = { viewModel.toggleKeepScreenOn() }) {
                Text(if (keepScreenOn) "Bildschirm an: Ein" else "Bildschirm an: Aus")
            }
        }
    }
}
