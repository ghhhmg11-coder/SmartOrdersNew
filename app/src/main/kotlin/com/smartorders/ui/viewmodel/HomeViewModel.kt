package com.smartorders.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartorders.data.database.entities.Order
import com.smartorders.data.database.entities.OrderStatus
import com.smartorders.data.repository.OrderRepository
import com.smartorders.data.repository.StatisticsRepository
import com.smartorders.data.repository.TodayStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val orders: List<Order> = emptyList(),
    val todayStats: TodayStats? = null,
    val activeOrderCount: Int = 0,
    val isLoading: Boolean = false,
    val showAddOrderDialog: Boolean = false,
    val showOrderDetails: Order? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayOrders()
        loadActiveOrderCount()
        loadTodayStats()
    }

    private fun loadTodayOrders() {
        orderRepository.getTodayOrders().onEach { orders ->
            _uiState.value = _uiState.value.copy(orders = orders)
        }.launchIn(viewModelScope)
    }

    private fun loadActiveOrderCount() {
        orderRepository.getActiveOrderCount().onEach { count ->
            _uiState.value = _uiState.value.copy(activeOrderCount = count)
        }.launchIn(viewModelScope)
    }

    private fun loadTodayStats() {
        viewModelScope.launch {
            val stats = orderRepository.getTodayStats()
            _uiState.value = _uiState.value.copy(todayStats = stats)
            statisticsRepository.refreshTodayStatistic(
                totalOrders = stats.totalOrders,
                completedOrders = stats.completedOrders,
                cancelledOrders = stats.cancelledOrders,
                totalRevenue = stats.totalRevenue
            )
        }
    }

    fun addOrder(
        customerName: String,
        items: String,
        totalAmount: Double,
        address: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val orderNumber = "ORD-${System.currentTimeMillis() % 100000}"
            val order = Order(
                orderNumber = orderNumber,
                customerName = customerName,
                items = items,
                totalAmount = totalAmount,
                address = address,
                notes = notes,
                status = OrderStatus.PENDING
            )
            orderRepository.insertOrder(order)
            loadTodayStats()
            _uiState.value = _uiState.value.copy(showAddOrderDialog = false)
        }
    }

    fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
            loadTodayStats()
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            orderRepository.deleteOrder(order)
            loadTodayStats()
        }
    }

    fun showAddOrderDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddOrderDialog = show)
    }

    fun showOrderDetails(order: Order?) {
        _uiState.value = _uiState.value.copy(showOrderDetails = order)
    }
}
