package com.flowscale.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RatingViewModel : ViewModel() {

    private val _startValue = MutableStateFlow(0.0)
    val startValue: StateFlow<Double> = _startValue

    private val _currentValue = MutableStateFlow(0.0)
    val currentValue: StateFlow<Double> = _currentValue

    fun setStartValue(value: Double) {
        val clamped = value.coerceIn(MIN_VALUE, MAX_VALUE)
        _startValue.value = clamped
        _currentValue.value = clamped
    }

    fun increment() {
        val next = _currentValue.value + STEP
        if (next <= MAX_VALUE) {
            _currentValue.value = next
        }
    }

    fun decrement() {
        val next = _currentValue.value - STEP
        if (next >= MIN_VALUE) {
            _currentValue.value = next
        }
    }

    companion object {
        const val STEP = 0.25
        const val MIN_VALUE = 0.0
        const val MAX_VALUE = 10.0
    }
}
