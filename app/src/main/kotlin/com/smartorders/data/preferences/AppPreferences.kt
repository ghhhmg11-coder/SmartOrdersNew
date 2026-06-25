package com.smartorders.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class TargetApp(val displayName: String, val packageName: String) {
    ALL("All Apps", ""),
    JEENY("Jeeny Driver", "com.jeeny.driver"),
    UBER("Uber Driver", "com.ubercab.driver"),
    CAREEM("Careem Captain", "com.careem.captain"),
    BOLT("Bolt Driver", "ee.mtakso.client")
}

@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val IS_LOGGED_IN         = booleanPreferencesKey("is_logged_in")
        val AUTO_ACCEPT_ENABLED  = booleanPreferencesKey("auto_accept_enabled")
        val SOUND_ENABLED        = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED    = booleanPreferencesKey("vibration_enabled")
        val MIN_TRIP_PRICE       = doublePreferencesKey("min_trip_price")
        val MAX_TRIP_PRICE       = doublePreferencesKey("max_trip_price")
        val MAX_PICKUP_DISTANCE  = doublePreferencesKey("max_pickup_distance")
        val MAX_TRIP_DISTANCE    = doublePreferencesKey("max_trip_distance")
        val TARGET_APP           = stringPreferencesKey("target_app")
    }

    val isLoggedIn:        Flow<Boolean>  = dataStore.data.map { it[IS_LOGGED_IN]        ?: false }
    val autoAcceptEnabled: Flow<Boolean>  = dataStore.data.map { it[AUTO_ACCEPT_ENABLED]  ?: false }
    val soundEnabled:      Flow<Boolean>  = dataStore.data.map { it[SOUND_ENABLED]        ?: true  }
    val vibrationEnabled:  Flow<Boolean>  = dataStore.data.map { it[VIBRATION_ENABLED]    ?: true  }
    val minTripPrice:      Flow<Double>   = dataStore.data.map { it[MIN_TRIP_PRICE]       ?: 0.0   }
    val maxTripPrice:      Flow<Double>   = dataStore.data.map { it[MAX_TRIP_PRICE]       ?: 9999.0}
    val maxPickupDistance: Flow<Double>   = dataStore.data.map { it[MAX_PICKUP_DISTANCE]  ?: 5.0   }
    val maxTripDistance:   Flow<Double>   = dataStore.data.map { it[MAX_TRIP_DISTANCE]    ?: 50.0  }
    val targetApp:         Flow<TargetApp>= dataStore.data.map {
        TargetApp.valueOf(it[TARGET_APP] ?: TargetApp.ALL.name)
    }

    suspend fun setLoggedIn(value: Boolean)         = dataStore.edit { it[IS_LOGGED_IN]        = value }
    suspend fun setAutoAcceptEnabled(value: Boolean) = dataStore.edit { it[AUTO_ACCEPT_ENABLED]  = value }
    suspend fun setSoundEnabled(value: Boolean)      = dataStore.edit { it[SOUND_ENABLED]        = value }
    suspend fun setVibrationEnabled(value: Boolean)  = dataStore.edit { it[VIBRATION_ENABLED]    = value }
    suspend fun setMinTripPrice(value: Double)       = dataStore.edit { it[MIN_TRIP_PRICE]       = value }
    suspend fun setMaxTripPrice(value: Double)       = dataStore.edit { it[MAX_TRIP_PRICE]       = value }
    suspend fun setMaxPickupDistance(value: Double)  = dataStore.edit { it[MAX_PICKUP_DISTANCE]  = value }
    suspend fun setMaxTripDistance(value: Double)    = dataStore.edit { it[MAX_TRIP_DISTANCE]    = value }
    suspend fun setTargetApp(value: TargetApp)       = dataStore.edit { it[TARGET_APP]           = value.name }
    suspend fun logout()                             = dataStore.edit { it[IS_LOGGED_IN]         = false }
}
