package com.example.locationmorsetracker.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationmorsetracker.data.PreferencesManager
import com.example.locationmorsetracker.services.LocationService
import com.example.locationmorsetracker.workers.LocationUpdateWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationUiState(
    val contactNumber: String = "",
    val homeLatitude: String = "",
    val homeLongitude: String = "",
    val collegeLatitude: String = "",
    val collegeLongitude: String = "",
    val isPeriodicSharingEnabled: Boolean = false,
    val isLocationServiceRunning: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesManager.userPreferencesFlow.collect { preferences ->
                _uiState.update { currentState ->
                    currentState.copy(
                        contactNumber = preferences.contactNumber,
                        homeLatitude = preferences.homeLatitude.toString(),
                        homeLongitude = preferences.homeLongitude.toString(),
                        collegeLatitude = preferences.collegeLatitude.toString(),
                        collegeLongitude = preferences.collegeLongitude.toString(),
                        isPeriodicSharingEnabled = preferences.periodicSharingEnabled
                    )
                }
            }
        }
    }

    fun updateContactNumber(number: String) {
        viewModelScope.launch {
            try {
                preferencesManager.updateContactNumber(number)
                clearError()
            } catch (e: Exception) {
                showError("Failed to update contact number")
            }
        }
    }

    fun updateHomeLocation(latitude: String, longitude: String) {
        viewModelScope.launch {
            try {
                val lat = latitude.toDoubleOrNull()
                val lon = longitude.toDoubleOrNull()
                
                if (lat != null && lon != null) {
                    preferencesManager.updateHomeLocation(lat, lon)
                    clearError()
                } else {
                    showError("Invalid coordinates format")
                }
            } catch (e: Exception) {
                showError("Failed to update home location")
            }
        }
    }

    fun updateCollegeLocation(latitude: String, longitude: String) {
        viewModelScope.launch {
            try {
                val lat = latitude.toDoubleOrNull()
                val lon = longitude.toDoubleOrNull()
                
                if (lat != null && lon != null) {
                    preferencesManager.updateCollegeLocation(lat, lon)
                    clearError()
                } else {
                    showError("Invalid coordinates format")
                }
            } catch (e: Exception) {
                showError("Failed to update college location")
            }
        }
    }

    fun togglePeriodicSharing(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.updatePeriodicSharing(enabled)
                if (enabled) {
                    LocationUpdateWorker.startPeriodicWork(getApplication())
                } else {
                    LocationUpdateWorker.stopPeriodicWork(getApplication())
                }
                clearError()
            } catch (e: Exception) {
                showError("Failed to update periodic sharing settings")
            }
        }
    }

    fun startLocationService() {
        val context = getApplication<Application>()
        Intent(context, LocationService::class.java).also { intent ->
            context.startForegroundService(intent)
        }
        _uiState.update { it.copy(isLocationServiceRunning = true) }
    }

    fun stopLocationService() {
        val context = getApplication<Application>()
        Intent(context, LocationService::class.java).also { intent ->
            context.stopService(intent)
        }
        _uiState.update { it.copy(isLocationServiceRunning = false) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}