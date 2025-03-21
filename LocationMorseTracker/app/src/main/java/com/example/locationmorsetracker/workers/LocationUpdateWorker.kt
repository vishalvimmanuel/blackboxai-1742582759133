package com.example.locationmorsetracker.workers

import android.content.Context
import android.location.Location
import android.telephony.SmsManager
import androidx.work.*
import com.example.locationmorsetracker.data.PreferencesManager
import com.example.locationmorsetracker.utils.MorseCodeConverter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class LocationUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WORK_NAME = "periodic_location_update"

        fun startPeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<LocationUpdateWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
        }

        fun stopPeriodicWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        try {
            val preferencesManager = PreferencesManager(applicationContext)
            val preferences = preferencesManager.userPreferencesFlow.first()

            if (!preferences.periodicSharingEnabled || preferences.contactNumber.isEmpty()) {
                return Result.success()
            }

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
            val location = Tasks.await(fusedLocationClient.lastLocation)

            location?.let {
                sendLocationUpdate(it, preferences.contactNumber)
            }

            return Result.success()
        } catch (e: Exception) {
            return if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun sendLocationUpdate(location: Location, phoneNumber: String) {
        val morseCode = MorseCodeConverter.convertCoordinatesToMorse(
            location.latitude,
            location.longitude
        )

        try {
            SmsManager.getDefault().sendTextMessage(
                phoneNumber,
                null,
                morseCode,
                null,
                null
            )
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                throw e // This will trigger a retry
            }
        }
    }
}