package com.smartorders.data.repository

import com.smartorders.data.database.dao.StatisticsDao
import com.smartorders.data.database.entities.DailyStatistic
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val statisticsDao: StatisticsDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getAllStatistics(): Flow<List<DailyStatistic>> = statisticsDao.getAllStatistics()

    fun getLastWeekStatistics(): Flow<List<DailyStatistic>> = statisticsDao.getLastWeekStatistics()

    fun getLastMonthStatistics(): Flow<List<DailyStatistic>> = statisticsDao.getLastMonthStatistics()

    suspend fun upsertStatistic(statistic: DailyStatistic) =
        statisticsDao.insertOrUpdateStatistic(statistic)

    suspend fun refreshTodayStatistic(
        totalOrders: Int,
        completedOrders: Int,
        cancelledOrders: Int,
        totalRevenue: Double
    ) {
        val today = dateFormat.format(Date())
        val avg = if (completedOrders > 0) totalRevenue / completedOrders else 0.0
        statisticsDao.insertOrUpdateStatistic(
            DailyStatistic(
                date = today,
                totalOrders = totalOrders,
                completedOrders = completedOrders,
                cancelledOrders = cancelledOrders,
                totalRevenue = totalRevenue,
                averageOrderValue = avg
            )
        )
    }
}
