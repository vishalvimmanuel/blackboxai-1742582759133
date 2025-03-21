package com.example.locationmorsetracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager

class LocationMorseApp : Application(), Configuration.Provider {
    
    companion object {
        const val LOCATION_CHANNEL_ID = "location_channel"
        const val GEOFENCE_CHANNEL_ID = "geofence_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val locationChannel = NotificationChannel(
                LOCATION_CHANNEL_ID,
                "Location Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for location updates and coordinates sharing"
            }

            val geofenceChannel = NotificationChannel(
                GEOFENCE_CHANNEL_ID,
                "Geofence Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for geofence transition updates"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(locationChannel, geofenceChannel))
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}