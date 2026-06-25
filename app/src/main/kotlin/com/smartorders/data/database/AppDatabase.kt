package com.smartorders.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.smartorders.data.database.dao.TripLogDao
import com.smartorders.data.database.entities.TripLog
import com.smartorders.data.database.entities.TripStatus

class Converters {
    @TypeConverter
    fun fromTripStatus(status: TripStatus): String = status.name

    @TypeConverter
    fun toTripStatus(value: String): TripStatus = TripStatus.valueOf(value)
}

@Database(
    entities = [TripLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripLogDao(): TripLogDao

    companion object {
        const val DATABASE_NAME = "smart_orders_driver.db"
    }
}
