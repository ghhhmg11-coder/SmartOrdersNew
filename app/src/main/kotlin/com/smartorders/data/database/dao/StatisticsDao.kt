package com.smartorders.data.database.dao

import androidx.room.*
import com.smartorders.data.database.entities.DailyStatistic
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {

    @Query("SELECT * FROM daily_statistics ORDER BY date DESC")
    fun getAllStatistics(): Flow<List<DailyStatistic>>

    @Query("SELECT * FROM daily_statistics WHERE date = :date")
    suspend fun getStatisticByDate(date: String): DailyStatistic?

    @Query("SELECT * FROM daily_statistics ORDER BY date DESC LIMIT 7")
    fun getLastWeekStatistics(): Flow<List<DailyStatistic>>

    @Query("SELECT * FROM daily_statistics ORDER BY date DESC LIMIT 30")
    fun getLastMonthStatistics(): Flow<List<DailyStatistic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStatistic(statistic: DailyStatistic)

    @Delete
    suspend fun deleteStatistic(statistic: DailyStatistic)
}
