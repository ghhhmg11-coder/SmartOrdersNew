package com.smartorders.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartorders.data.database.entities.TripLog
import com.smartorders.data.repository.TripLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogsUiState(
    val logs: List<TripLog> = emptyList(),
    val showClearConfirm: Boolean = false
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val tripLogRepository: TripLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        tripLogRepository.getRecentLogs().onEach { logs ->
            _uiState.value = _uiState.value.copy(logs = logs)
        }.launchIn(viewModelScope)
    }

    fun showClearConfirm(show: Boolean) {
        _uiState.value = _uiState.value.copy(showClearConfirm = show)
    }

    fun clearLogs() {
        viewModelScope.launch {
            tripLogRepository.clearLogs()
            _uiState.value = _uiState.value.copy(showClearConfirm = false)
        }
    }
}
