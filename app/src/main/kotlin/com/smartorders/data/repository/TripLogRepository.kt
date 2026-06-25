package com.smartorders.data.repository

import com.smartorders.data.database.dao.TripLogDao
import com.smartorders.data.database.entities.TripLog
import com.smartorders.data.database.entities.TripStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripLogRepository @Inject constructor(
    private val tripLogDao: TripLogDao
) {
    fun getRecentLogs(limit: Int = 200): Flow<List<TripLog>> = tripLogDao.getRecentLogs(limit)
    fun getAllLogs(): Flow<List<TripLog>>   = tripLogDao.getAllLogs()
    fun getTotalDetected(): Flow<Int>       = tripLogDao.getTotalDetected()
    fun getTotalAccepted(): Flow<Int>       = tripLogDao.getTotalAccepted()
    fun getTotalRejected(): Flow<Int>       = tripLogDao.getTotalRejected()
    fun getTotalEarnings(): Flow<Double?>   = tripLogDao.getTotalEarnings()

    suspend fun logDetected(
        text: String,
        status: TripStatus,
        amount: Double = 0.0,
        pickupKm: Double = 0.0,
        tripKm: Double = 0.0,
        sourceApp: String = "",
        rejectReason: String = ""
    ): Long = tripLogDao.insertLog(
        TripLog(
            detectedText      = text,
            status            = status,
            amount            = amount,
            pickupDistanceKm  = pickupKm,
            tripDistanceKm    = tripKm,
            sourceApp         = sourceApp,
            rejectReason      = rejectReason
        )
    )

    suspend fun clearLogs() = tripLogDao.clearAllLogs()
}
