package com.smartorders.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_statistics")
data class DailyStatistic(
    @PrimaryKey
    val date: String,
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val peakHour: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
