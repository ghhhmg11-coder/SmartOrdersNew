package com.smartorders.di

import android.content.Context
import androidx.room.Room
import com.smartorders.data.database.AppDatabase
import com.smartorders.data.database.dao.TripLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideTripLogDao(db: AppDatabase): TripLogDao = db.tripLogDao()
}
