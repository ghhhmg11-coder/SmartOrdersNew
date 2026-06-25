package com.smartorders.data.repository

import com.smartorders.data.database.dao.OrderDao
import com.smartorders.data.database.entities.Order
import com.smartorders.data.database.entities.OrderStatus
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao
) {
    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()

    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>> =
        orderDao.getOrdersByStatus(status)

    fun getActiveOrderCount(): Flow<Int> = orderDao.getActiveOrderCount()

    fun getTodayOrders(): Flow<List<Order>> {
        val (start, end) = getTodayRange()
        return orderDao.getOrdersByDate(start, end)
    }

    suspend fun insertOrder(order: Order): Long = orderDao.insertOrder(order)

    suspend fun updateOrder(order: Order) = orderDao.updateOrder(order)

    suspend fun updateOrderStatus(id: Long, status: OrderStatus) =
        orderDao.updateOrderStatus(id, status)

    suspend fun deleteOrder(order: Order) = orderDao.deleteOrder(order)

    suspend fun getTodayStats(): TodayStats {
        val (start, end) = getTodayRange()
        val total = orderDao.getOrderCountForDay(start, end)
        val revenue = orderDao.getTotalRevenueForDay(start, end) ?: 0.0
        val completed = orderDao.getCompletedOrderCountForDay(start, end)
        val cancelled = orderDao.getCancelledOrderCountForDay(start, end)
        return TodayStats(total, revenue, completed, cancelled)
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val end = start + 86_400_000L
        return start to end
    }
}

data class TodayStats(
    val totalOrders: Int,
    val totalRevenue: Double,
    val completedOrders: Int,
    val cancelledOrders: Int
)
