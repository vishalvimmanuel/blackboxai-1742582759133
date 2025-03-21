package com.example.locationmorsetracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val CONTACT_NUMBER = stringPreferencesKey("contact_number")
        private val HOME_LATITUDE = doublePreferencesKey("home_latitude")
        private val HOME_LONGITUDE = doublePreferencesKey("home_longitude")
        private val COLLEGE_LATITUDE = doublePreferencesKey("college_latitude")
        private val COLLEGE_LONGITUDE = doublePreferencesKey("college_longitude")
        private val PERIODIC_SHARING_ENABLED = booleanPreferencesKey("periodic_sharing_enabled")
    }

    data class UserPreferences(
        val contactNumber: String = "",
        val homeLatitude: Double = 0.0,
        val homeLongitude: Double = 0.0,
        val collegeLatitude: Double = 0.0,
        val collegeLongitude: Double = 0.0,
        val periodicSharingEnabled: Boolean = false
    )

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                contactNumber = preferences[CONTACT_NUMBER] ?: "",
                homeLatitude = preferences[HOME_LATITUDE] ?: 0.0,
                homeLongitude = preferences[HOME_LONGITUDE] ?: 0.0,
                collegeLatitude = preferences[COLLEGE_LATITUDE] ?: 0.0,
                collegeLongitude = preferences[COLLEGE_LONGITUDE] ?: 0.0,
                periodicSharingEnabled = preferences[PERIODIC_SHARING_ENABLED] ?: false
            )
        }

    suspend fun updateContactNumber(contactNumber: String) {
        context.dataStore.edit { preferences ->
            preferences[CONTACT_NUMBER] = contactNumber
        }
    }

    suspend fun updateHomeLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[HOME_LATITUDE] = latitude
            preferences[HOME_LONGITUDE] = longitude
        }
    }

    suspend fun updateCollegeLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[COLLEGE_LATITUDE] = latitude
            preferences[COLLEGE_LONGITUDE] = longitude
        }
    }

    suspend fun updatePeriodicSharing(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PERIODIC_SHARING_ENABLED] = enabled
        }
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}