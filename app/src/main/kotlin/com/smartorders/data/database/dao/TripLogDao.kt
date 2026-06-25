package com.smartorders.data.database.dao

import androidx.room.*
import com.smartorders.data.database.entities.TripLog
import com.smartorders.data.database.entities.TripStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TripLogDao {

    @Query("SELECT * FROM trip_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TripLog>>

    @Query("SELECT * FROM trip_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 200): Flow<List<TripLog>>

    @Query("SELECT COUNT(*) FROM trip_logs WHERE status = 'DETECTED' OR status = 'ACCEPTED' OR status = 'REJECTED'")
    fun getTotalDetected(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trip_logs WHERE status = 'ACCEPTED'")
    fun getTotalAccepted(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trip_logs WHERE status = 'REJECTED'")
    fun getTotalRejected(): Flow<Int>

    @Query("SELECT SUM(amount) FROM trip_logs WHERE status = 'ACCEPTED'")
    fun getTotalEarnings(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TripLog): Long

    @Query("DELETE FROM trip_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM trip_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)
}
