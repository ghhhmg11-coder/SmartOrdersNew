package com.smartorders.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus {
    PENDING, ACCEPTED, PREPARING, DELIVERED, CANCELLED
}

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String = "",
    val address: String = "",
    val items: String,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val source: String = "manual",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
