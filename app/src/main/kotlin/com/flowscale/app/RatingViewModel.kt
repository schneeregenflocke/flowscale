package com.flowscale.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.flowscale.app.data.AppDatabase
import com.flowscale.app.data.IntensityRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

private const val DEFAULT_WINDOW_MINUTES = 5
private const val PREFS_NAME = "flowscale_prefs"
private const val KEY_VOLUME_KEYS = "volume_keys_enabled"
private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"

class RatingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = getApplication<FlowScaleApplication>().database.intensityRecordDao()
    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentValue = MutableStateFlow(0.0)
    val currentValue: StateFlow<Double> = _currentValue

    private val _nowMillis = MutableStateFlow(Instant.now().toEpochMilli())
    val nowMillis: StateFlow<Long> = _nowMillis

    init {
        viewModelScope.launch {
            try {
                dao.getLatest()?.let { _currentValue.value = it.intensity }
            } catch (e: Exception) {
                Log.e("RatingViewModel", "Failed to load latest record", e)
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                _nowMillis.value = Instant.now().toEpochMilli()
            }
        }
    }

    private val _windowMinutes = MutableStateFlow(DEFAULT_WINDOW_MINUTES)
    val windowMinutes: StateFlow<Int> = _windowMinutes

    fun setWindowMinutes(minutes: Int) {
        if (minutes > 0) _windowMinutes.value = minutes
    }

    private val countFlow = dao.getCount()

    val recordCount = countFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = 0L,
        )

    val databaseSizeBytes: StateFlow<Long> = countFlow
        .map { computeDatabaseSize() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = 0L,
        )

    private fun computeDatabaseSize(): Long {
        val dbPath = getApplication<FlowScaleApplication>().getDatabasePath(AppDatabase.NAME)
        val mainSize = dbPath.length()
        val walSize = File(dbPath.path + "-wal").length()
        val shmSize = File(dbPath.path + "-shm").length()
        return mainSize + walSize + shmSize
    }

    val recentRecords = combine(_nowMillis, _windowMinutes) { now, minutes ->
        now - minutes.toLong() * 60 * 1_000
    }.flatMapLatest { windowStart ->
        dao.getWindowedRecords(windowStart)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = emptyList(),
    )

    private val _volumeKeysEnabled = MutableStateFlow(prefs.getBoolean(KEY_VOLUME_KEYS, false))
    val volumeKeysEnabled: StateFlow<Boolean> = _volumeKeysEnabled

    private val _keepScreenOn = MutableStateFlow(prefs.getBoolean(KEY_KEEP_SCREEN_ON, false))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn

    fun toggleVolumeKeys() {
        val newValue = !_volumeKeysEnabled.value
        _volumeKeysEnabled.value = newValue
        prefs.edit().putBoolean(KEY_VOLUME_KEYS, newValue).apply()
    }

    fun toggleKeepScreenOn() {
        val newValue = !_keepScreenOn.value
        _keepScreenOn.value = newValue
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, newValue).apply()
    }

    fun increment() {
        val next = normalizeToTenths(_currentValue.value + STEP)
        if (next <= MAX_VALUE) {
            _currentValue.value = next
            recordIntensity(next)
        }
    }

    fun decrement() {
        val next = normalizeToTenths(_currentValue.value - STEP)
        if (next >= MIN_VALUE) {
            _currentValue.value = next
            recordIntensity(next)
        }
    }

    private fun normalizeToTenths(value: Double): Double =
        BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).toDouble()

    private fun recordIntensity(value: Double) {
        viewModelScope.launch {
            try {
                dao.insert(IntensityRecord(intensity = value))
            } catch (e: Exception) {
                Log.e("RatingViewModel", "Failed to insert intensity record", e)
            }
        }
    }

    companion object {
        const val STEP = 0.1
        const val MIN_VALUE = 0.0
        const val MAX_VALUE = 10.0
    }
}
