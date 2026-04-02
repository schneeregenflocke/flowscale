package com.flowscale.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowscale.app.data.IntensityRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

private const val DEFAULT_WINDOW_MINUTES = 5

class RatingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = getApplication<FlowScaleApplication>().database.intensityRecordDao()

    private val _currentValue = MutableStateFlow(0.0)
    val currentValue: StateFlow<Double> = _currentValue

    init {
        viewModelScope.launch {
            dao.getLatest()?.let { _currentValue.value = it.intensity }
        }
    }

    private val _windowMinutes = MutableStateFlow(DEFAULT_WINDOW_MINUTES)
    val windowMinutes: StateFlow<Int> = _windowMinutes

    fun setWindowMinutes(minutes: Int) {
        _windowMinutes.value = minutes
    }

    private val records = dao.getAll()

    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(1_000)
        }
    }

    val recentRecords = combine(records, ticker, _windowMinutes) { allRecords, _, minutes ->
        val windowStart = Instant.now().toEpochMilli() - minutes.toLong() * 60 * 1_000
        selectRecentRecords(allRecords, windowStart)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = emptyList(),
    )

    private val _volumeKeysEnabled = MutableStateFlow(false)
    val volumeKeysEnabled: StateFlow<Boolean> = _volumeKeysEnabled

    private val _keepScreenOn = MutableStateFlow(false)
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn

    fun toggleVolumeKeys() {
        _volumeKeysEnabled.value = !_volumeKeysEnabled.value
    }

    fun toggleKeepScreenOn() {
        _keepScreenOn.value = !_keepScreenOn.value
    }

    fun increment() {
        val next = _currentValue.value + STEP
        if (next <= MAX_VALUE) {
            _currentValue.value = next
            recordIntensity(next)
        }
    }

    fun decrement() {
        val next = _currentValue.value - STEP
        if (next >= MIN_VALUE) {
            _currentValue.value = next
            recordIntensity(next)
        }
    }

    private fun recordIntensity(value: Double) {
        viewModelScope.launch {
            dao.insert(IntensityRecord(intensity = value))
        }
    }

    companion object {
        const val STEP = 0.25
        const val MIN_VALUE = 0.0
        const val MAX_VALUE = 10.0
    }
}

internal fun selectRecentRecords(
    allRecords: List<IntensityRecord>,
    windowStartMillis: Long,
): List<IntensityRecord> {
    val firstVisibleIndex = allRecords.indexOfFirst { it.recordedAt >= windowStartMillis }

    return when {
        firstVisibleIndex > 0 -> allRecords.subList(firstVisibleIndex - 1, allRecords.size)
        firstVisibleIndex == 0 -> allRecords
        allRecords.isNotEmpty() -> listOf(allRecords.last())
        else -> emptyList()
    }
}
