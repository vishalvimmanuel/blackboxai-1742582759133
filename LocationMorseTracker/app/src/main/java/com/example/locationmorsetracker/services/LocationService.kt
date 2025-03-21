package com.example.locationmorsetracker.services

import android.app.*
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.example.locationmorsetracker.LocationMorseApp.Companion.LOCATION_CHANNEL_ID
import com.example.locationmorsetracker.data.PreferencesManager
import com.example.locationmorsetracker.utils.MorseCodeConverter
import com.google.android.gms.location.*
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER
import com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocationService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var preferencesManager: PreferencesManager
    private var currentNotificationId = 1

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                handleLocationUpdate(location)
            }
        }
    }

    private val geofencingCallback = object : GeofencingClient.OnGeofencesRegisteredCallback {
        override fun onSuccess() {
            showNotification("Geofences registered successfully")
        }

        override fun onFailure(e: Exception) {
            showNotification("Failed to register geofences: ${e.message}")
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        preferencesManager = PreferencesManager(this)
        setupGeofences()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification("Location tracking active"))
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 60000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(30000)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            showNotification("Location permission not granted")
        }
    }

    private fun setupGeofences() {
        serviceScope.launch {
            val preferences = preferencesManager.userPreferencesFlow.first()
            
            val homeGeofence = Geofence.Builder()
                .setRequestId("HOME")
                .setCircularRegion(
                    preferences.homeLatitude,
                    preferences.homeLongitude,
                    1000f // 1km radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)
                .build()

            val collegeGeofence = Geofence.Builder()
                .setRequestId("COLLEGE")
                .setCircularRegion(
                    preferences.collegeLatitude,
                    preferences.collegeLongitude,
                    1000f // 1km radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(GEOFENCE_TRANSITION_ENTER or GEOFENCE_TRANSITION_EXIT)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(listOf(homeGeofence, collegeGeofence))
                .build()

            try {
                geofencingClient.addGeofences(geofencingRequest, geofencingCallback)
            } catch (e: SecurityException) {
                showNotification("Location permission not granted for geofencing")
            }
        }
    }

    private fun handleLocationUpdate(location: Location) {
        serviceScope.launch {
            val preferences = preferencesManager.userPreferencesFlow.first()
            if (preferences.periodicSharingEnabled) {
                val morseCode = MorseCodeConverter.convertCoordinatesToMorse(
                    location.latitude,
                    location.longitude
                )
                sendSmsMessage(preferences.contactNumber, morseCode)
                showNotification("Location shared: ${location.latitude}, ${location.longitude}")
            }
        }
    }

    private fun handleGeofenceTransition(geofenceId: String, transitionType: Int) {
        val status = when (transitionType) {
            GEOFENCE_TRANSITION_ENTER -> "ENTERED"
            GEOFENCE_TRANSITION_EXIT -> "EXITED"
            else -> return
        }

        serviceScope.launch {
            val preferences = preferencesManager.userPreferencesFlow.first()
            val morseCode = MorseCodeConverter.convertGeofenceStatusToMorse(geofenceId, status)
            sendSmsMessage(preferences.contactNumber, morseCode)
            showNotification("$status $geofenceId")
        }
    }

    private fun sendSmsMessage(phoneNumber: String, message: String) {
        try {
            SmsManager.getDefault().sendTextMessage(
                phoneNumber,
                null,
                message,
                null,
                null
            )
        } catch (e: Exception) {
            showNotification("Failed to send SMS: ${e.message}")
        }
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, LOCATION_CHANNEL_ID)
            .setContentTitle("Location Morse Tracker")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun showNotification(message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(currentNotificationId++, createNotification(message))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        geofencingClient.removeGeofences(listOf("HOME", "COLLEGE"))
    }
}