package com.smartorders.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TripStatus { DETECTED, ACCEPTED, REJECTED }

@Entity(tableName = "trip_logs")
data class TripLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val detectedText: String,
    val status: TripStatus = TripStatus.DETECTED,
    val amount: Double = 0.0,
    val pickupDistanceKm: Double = 0.0,
    val tripDistanceKm: Double = 0.0,
    val sourceApp: String = "",
    val rejectReason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
