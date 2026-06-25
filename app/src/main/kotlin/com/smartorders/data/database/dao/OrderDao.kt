package com.smartorders.data.database.dao

import androidx.room.*
import com.smartorders.data.database.entities.Order
import com.smartorders.data.database.entities.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    fun getOrdersByDate(startOfDay: Long, endOfDay: Long): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Long): Order?

    @Query("SELECT COUNT(*) FROM orders WHERE createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getOrderCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM orders WHERE status IN ('PENDING','ACCEPTED','PREPARING')")
    fun getActiveOrderCount(): Flow<Int>

    @Query("SELECT SUM(totalAmount) FROM orders WHERE createdAt >= :startOfDay AND createdAt < :endOfDay AND status = 'DELIVERED'")
    suspend fun getTotalRevenueForDay(startOfDay: Long, endOfDay: Long): Double?

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getCompletedOrderCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'CANCELLED' AND createdAt >= :startOfDay AND createdAt < :endOfDay")
    suspend fun getCancelledOrderCountForDay(startOfDay: Long, endOfDay: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)

    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateOrderStatus(id: Long, status: OrderStatus, updatedAt: Long = System.currentTimeMillis())
}
