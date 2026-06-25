package com.smartorders.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartorders.data.database.entities.DailyStatistic
import com.smartorders.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

enum class StatsPeriod { TODAY, WEEK, MONTH }

data class StatisticsUiState(
    val statistics: List<DailyStatistic> = emptyList(),
    val selectedPeriod: StatsPeriod = StatsPeriod.TODAY,
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val averageOrderValue: Double = 0.0
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadWeekStatistics()
    }

    fun selectPeriod(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        when (period) {
            StatsPeriod.TODAY -> loadTodayStatistics()
            StatsPeriod.WEEK -> loadWeekStatistics()
            StatsPeriod.MONTH -> loadMonthStatistics()
        }
    }

    private fun loadTodayStatistics() {
        statisticsRepository.getAllStatistics().onEach { stats ->
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            val todayStat = stats.firstOrNull { it.date == today }
            val list = listOfNotNull(todayStat)
            updateState(list)
        }.launchIn(viewModelScope)
    }

    private fun loadWeekStatistics() {
        statisticsRepository.getLastWeekStatistics().onEach { stats ->
            updateState(stats)
        }.launchIn(viewModelScope)
    }

    private fun loadMonthStatistics() {
        statisticsRepository.getLastMonthStatistics().onEach { stats ->
            updateState(stats)
        }.launchIn(viewModelScope)
    }

    private fun updateState(stats: List<DailyStatistic>) {
        val totalOrders = stats.sumOf { it.totalOrders }
        val totalRevenue = stats.sumOf { it.totalRevenue }
        val completed = stats.sumOf { it.completedOrders }
        val cancelled = stats.sumOf { it.cancelledOrders }
        val avg = if (completed > 0) totalRevenue / completed else 0.0

        _uiState.value = _uiState.value.copy(
            statistics = stats,
            totalOrders = totalOrders,
            totalRevenue = totalRevenue,
            completedOrders = completed,
            cancelledOrders = cancelled,
            averageOrderValue = avg
        )
    }
}
