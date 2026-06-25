package com.smartorders.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.smartorders.data.database.dao.OrderDao
import com.smartorders.data.database.dao.StatisticsDao
import com.smartorders.data.database.entities.DailyStatistic
import com.smartorders.data.database.entities.Order
import com.smartorders.data.database.entities.OrderStatus

class Converters {
    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String = status.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)
}

@Database(
    entities = [Order::class, DailyStatistic::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun statisticsDao(): StatisticsDao

    companion object {
        const val DATABASE_NAME = "smart_orders.db"
    }
}
