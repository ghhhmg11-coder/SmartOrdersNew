package com.smartorders.ui.viewmodel

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartorders.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val autoAcceptEnabled: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val showLogoutDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        combine(
            appPreferences.notificationsEnabled,
            appPreferences.soundEnabled,
            appPreferences.autoAcceptEnabled
        ) { notifications, sound, autoAccept ->
            Triple(notifications, sound, autoAccept)
        }.onEach { (notifications, sound, autoAccept) ->
            _uiState.value = _uiState.value.copy(
                notificationsEnabled = notifications,
                soundEnabled = sound,
                autoAcceptEnabled = autoAccept
            )
        }.launchIn(viewModelScope)
    }

    fun checkAccessibilityStatus() {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        val isEnabled = enabledServices.contains("com.smartorders")
        _uiState.value = _uiState.value.copy(isAccessibilityEnabled = isEnabled)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setNotificationsEnabled(enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setSoundEnabled(enabled) }
    }

    fun setAutoAcceptEnabled(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setAutoAcceptEnabled(enabled) }
    }

    fun showLogoutDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLogoutDialog = show)
    }

    fun logout() {
        viewModelScope.launch {
            appPreferences.logout()
        }
    }
}
