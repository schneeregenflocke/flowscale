package com.flowscale.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.flowscale.app.data.AppDatabase
import com.flowscale.app.data.IntensityRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.Instant

private const val RECENT_WINDOW_MILLIS = 5L * 60 * 1_000

class RatingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "flowscale.db",
    ).build()

    private val dao = db.intensityRecordDao()

    private val _startValue = MutableStateFlow(0.0)
    val startValue: StateFlow<Double> = _startValue

    private val _currentValue = MutableStateFlow(0.0)
    val currentValue: StateFlow<Double> = _currentValue

    val records = dao.getAll()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recentRecords = _currentValue.flatMapLatest {
        dao.getSince(Instant.now().toEpochMilli() - RECENT_WINDOW_MILLIS)
    }

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

    fun setStartValue(value: Double) {
        val clamped = value.coerceIn(MIN_VALUE, MAX_VALUE)
        _startValue.value = clamped
        _currentValue.value = clamped
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
