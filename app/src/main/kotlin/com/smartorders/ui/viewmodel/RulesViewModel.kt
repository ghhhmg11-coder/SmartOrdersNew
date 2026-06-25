package com.smartorders.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartorders.data.preferences.AppPreferences
import com.smartorders.data.preferences.TargetApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RulesUiState(
    val minTripPrice: String      = "0",
    val maxTripPrice: String      = "9999",
    val maxPickupDistance: String = "5",
    val maxTripDistance: String   = "50",
    val targetApp: TargetApp      = TargetApp.ALL,
    val soundEnabled: Boolean     = true,
    val vibrationEnabled: Boolean = true,
    val savedSuccess: Boolean     = false
)

@HiltViewModel
class RulesViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(RulesUiState())
    val uiState: StateFlow<RulesUiState> = _uiState.asStateFlow()

    init {
        combine(
            prefs.minTripPrice,
            prefs.maxTripPrice,
            prefs.maxPickupDistance,
            prefs.maxTripDistance,
            prefs.targetApp
        ) { min, max, pickup, trip, app ->
            _uiState.value = _uiState.value.copy(
                minTripPrice      = if (min == 0.0) "0" else min.toInt().toString(),
                maxTripPrice      = if (max == 9999.0) "9999" else max.toInt().toString(),
                maxPickupDistance = pickup.toInt().toString(),
                maxTripDistance   = trip.toInt().toString(),
                targetApp         = app
            )
        }.launchIn(viewModelScope)

        combine(prefs.soundEnabled, prefs.vibrationEnabled) { sound, vib ->
            _uiState.value = _uiState.value.copy(
                soundEnabled     = sound,
                vibrationEnabled = vib
            )
        }.launchIn(viewModelScope)
    }

    fun onMinPriceChange(v: String)       { _uiState.value = _uiState.value.copy(minTripPrice      = v) }
    fun onMaxPriceChange(v: String)       { _uiState.value = _uiState.value.copy(maxTripPrice      = v) }
    fun onMaxPickupChange(v: String)      { _uiState.value = _uiState.value.copy(maxPickupDistance  = v) }
    fun onMaxTripChange(v: String)        { _uiState.value = _uiState.value.copy(maxTripDistance    = v) }
    fun onTargetAppChange(app: TargetApp) { _uiState.value = _uiState.value.copy(targetApp          = app) }
    fun onSoundChange(v: Boolean)         { _uiState.value = _uiState.value.copy(soundEnabled       = v) }
    fun onVibrationChange(v: Boolean)     { _uiState.value = _uiState.value.copy(vibrationEnabled   = v) }

    fun saveRules() {
        val s = _uiState.value
        viewModelScope.launch {
            prefs.setMinTripPrice(s.minTripPrice.toDoubleOrNull() ?: 0.0)
            prefs.setMaxTripPrice(s.maxTripPrice.toDoubleOrNull() ?: 9999.0)
            prefs.setMaxPickupDistance(s.maxPickupDistance.toDoubleOrNull() ?: 5.0)
            prefs.setMaxTripDistance(s.maxTripDistance.toDoubleOrNull() ?: 50.0)
            prefs.setTargetApp(s.targetApp)
            prefs.setSoundEnabled(s.soundEnabled)
            prefs.setVibrationEnabled(s.vibrationEnabled)
            _uiState.value = _uiState.value.copy(savedSuccess = true)
        }
    }

    fun clearSavedSuccess() { _uiState.value = _uiState.value.copy(savedSuccess = false) }
}
