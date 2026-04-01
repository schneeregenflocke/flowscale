package com.flowscale.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.flowscale.app.data.AppDatabase
import com.flowscale.app.data.IntensityRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.Instant

private const val DEFAULT_WINDOW_MINUTES = 5

class RatingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "flowscale.db",
    ).build()

    private val dao = db.intensityRecordDao()

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

    val records = dao.getAll()

    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(1_000)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val recentRecords = kotlinx.coroutines.flow.combine(ticker, _windowMinutes) { _, minutes -> minutes }
        .flatMapLatest { minutes ->
            val windowMillis = minutes.toLong() * 60 * 1_000
            dao.getSince(Instant.now().toEpochMilli() - windowMillis)
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
