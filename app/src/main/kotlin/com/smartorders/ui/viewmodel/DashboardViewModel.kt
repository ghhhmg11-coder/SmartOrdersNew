package com.smartorders.ui.viewmodel

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartorders.data.preferences.AppPreferences
import com.smartorders.data.repository.TripLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val autoAcceptEnabled: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val totalDetected: Int = 0,
    val totalAccepted: Int = 0,
    val totalRejected: Int = 0,
    val totalEarnings: Double = 0.0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val tripLogRepository: TripLogRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            appPreferences.autoAcceptEnabled,
            tripLogRepository.getTotalDetected(),
            tripLogRepository.getTotalAccepted(),
            tripLogRepository.getTotalRejected(),
            tripLogRepository.getTotalEarnings()
        ) { autoAccept, detected, accepted, rejected, earnings ->
            _uiState.value = _uiState.value.copy(
                autoAcceptEnabled = autoAccept,
                totalDetected     = detected,
                totalAccepted     = accepted,
                totalRejected     = rejected,
                totalEarnings     = earnings ?: 0.0
            )
        }.launchIn(viewModelScope)
    }

    fun checkAccessibilityStatus() {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )?.contains("com.smartorders") == true
        _uiState.value = _uiState.value.copy(isAccessibilityEnabled = enabled)
    }

    fun toggleAutoAccept(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setAutoAcceptEnabled(enabled) }
    }
}
