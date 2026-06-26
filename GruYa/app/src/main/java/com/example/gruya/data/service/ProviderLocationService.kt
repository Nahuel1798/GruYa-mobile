package com.example.gruya.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.gruya.R
import com.example.gruya.data.repository.ProviderRepository
import com.example.gruya.domain.model.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProviderLocationService : Service() {

    @Inject
    lateinit var providerRepository: ProviderRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "provider_location_channel"
        const val NOTIFICATION_ID = 1002
        const val TAG = "ProviderLocationService"
        private const val UPDATE_INTERVAL_MS = 30_000L
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Starting provider location updates")
        startForegroundWithNotification()
        startPeriodicUpdates()
        return START_STICKY
    }

    private fun startPeriodicUpdates() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val location = getCurrentLocation()
                    if (location != null) {
                        val result = providerRepository.updateProviderLocation(
                            Location(location.latitude, location.longitude)
                        )
                        if (result.isSuccess) {
                            Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating location", e)
                }
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    private fun getCurrentLocation(): android.location.Location? {
        return try {
            val task = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            )
            Tasks.await(task, 10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            Log.w(TAG, "Could not get current location", e)
            null
        }
    }

    private fun startForegroundWithNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Ubicación del proveedor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Compartiendo ubicación para recibir solicitudes cercanas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.iconogruya)
            .setContentTitle("Estás disponible")
            .setContentText("Compartiendo ubicación cada 30 segundos")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        Log.d(TAG, "Provider location service destroyed")
        super.onDestroy()
    }
}
