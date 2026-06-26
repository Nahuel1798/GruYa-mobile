package com.example.gruya.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.gruya.R
import com.example.gruya.data.repository.TrackingRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var trackingRepository: TrackingRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isTracking = false
    private val updateInterval = 5000L // 5 seconds
    private var locationCallback: LocationCallback? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.gruya.STOP_TRACKING"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTracking()
            return START_NOT_STICKY
        }

        Log.d("LocationTrackingService", "Starting location tracking")
        startForegroundWithNotification()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (isTracking) return
        isTracking = true

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            updateInterval
        )
            .setMinUpdateIntervalMillis(updateInterval)
            .setMaxUpdateDelayMillis(updateInterval)
            .setMinUpdateDistanceMeters(0f)
            .build()

        try {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.locations.forEach { location ->
                        Log.d("LocationTrackingService", "GPS: ${location.latitude}, ${location.longitude}")
                        trackingRepository.sendLocation(location.latitude, location.longitude)
                    }
                }
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null && isTracking) {
                    Log.d("LocationTrackingService", "Last GPS: ${location.latitude}, ${location.longitude}")
                    trackingRepository.sendLocation(location.latitude, location.longitude)
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback ?: return,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("LocationTrackingService", "Location permission denied", e)
            stopTracking()
        }
    }

    private fun startForegroundWithNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Seguimiento de ubicación en tiempo real"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, LocationTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = android.app.PendingIntent.getService(
            this, 0, stopIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.iconogruya)
            .setContentTitle("Compartiendo ubicación")
            .setContentText("Enviando ubicación cada 5 segundos")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(R.drawable.iconogruya, "Detener", stopPendingIntent)
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

    private fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d("LocationTrackingService", "Tracking stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }
}
