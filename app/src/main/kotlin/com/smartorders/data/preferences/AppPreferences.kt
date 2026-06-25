package com.smartorders.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val AUTO_ACCEPT_ENABLED = booleanPreferencesKey("auto_accept_enabled")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { it[IS_LOGGED_IN] ?: false }
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val soundEnabled: Flow<Boolean> = dataStore.data.map { it[SOUND_ENABLED] ?: true }
    val autoAcceptEnabled: Flow<Boolean> = dataStore.data.map { it[AUTO_ACCEPT_ENABLED] ?: false }

    suspend fun setLoggedIn(value: Boolean) {
        dataStore.edit { it[IS_LOGGED_IN] = value }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED] = value }
    }

    suspend fun setSoundEnabled(value: Boolean) {
        dataStore.edit { it[SOUND_ENABLED] = value }
    }

    suspend fun setAutoAcceptEnabled(value: Boolean) {
        dataStore.edit { it[AUTO_ACCEPT_ENABLED] = value }
    }

    suspend fun logout() {
        dataStore.edit { it[IS_LOGGED_IN] = false }
    }
}
