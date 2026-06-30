package com.example.gruya.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.gruya.MainActivity
import com.example.gruya.R
import com.example.gruya.data.SessionManager
import com.example.gruya.data.repository.NotificationRepository
import com.example.gruya.ui.navigation.EXTRA_ASSISTANCE_ID
import com.example.gruya.ui.navigation.EXTRA_NAV_TYPE
import com.example.gruya.ui.navigation.EXTRA_TRACKING_SESSION_ID
import com.example.gruya.ui.navigation.NavigationEventBus
import com.example.gruya.ui.navigation.getRequiredRole
import com.example.gruya.ui.navigation.navEventFromExtras
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

@AndroidEntryPoint
class GruYaFirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var navigationEventBus: NavigationEventBus

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    // Atomic counter for unique notification IDs (avoids currentTimeMillis collisions)
    private val notificationIdCounter = java.util.concurrent.atomic.AtomicInteger(1)

    override fun onNewToken(token: String) {
        Log.d("FMC", "Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data

        // --- Extract type and assistanceId from data payload ---
        val type = data["type"]
        val assistanceIdStr = data["assistanceId"]
        val trackingSessionId = data["trackingSessionId"]

        // --- Evaluate via pure guard function (session, role, type) ---
        val guardResult = evaluateNotification(
            jwt = sessionManager.getJwt(),
            userRole = sessionManager.getRole(),
            type = type,
            assistanceIdStr = assistanceIdStr,
            trackingSessionId = trackingSessionId
        )

        if (guardResult.shouldDrop) {
            Log.d("FMC", "Guard dropped notification: type=$type")
            return
        }

        val navEvent = guardResult.navEvent

        // Title/body come from data payload (data-only FCM)
        val title = data["title"] ?: ""
        val body = data["body"] ?: ""

        if (title.isNotEmpty() || body.isNotEmpty()) {
            if (navigationEventBus.isForeground) {
                // Foreground path: emit NavEvent or generic notification to bus for in-app snackbar
                if (navEvent != null) {
                    navigationEventBus.emitNotification(navEvent)
                    Log.d("FMC", "Emitted NavEvent (notification): $navEvent (foreground)")
                } else if (guardResult.showNotification) {
                    // Fallback for generic notifications or unknown types
                    scope.launch {
                        notificationRepository.emitNotification(title, body)
                    }
                }
            } else {
                // Background path: show system notification
                val assistanceId = assistanceIdStr?.toIntOrNull() ?: -1
                showNotification(title, body, type, assistanceId, trackingSessionId)
            }
        } else {
            Log.w("FMC", "Dropping notification with empty title/body: type=$type")
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        navType: String? = null,
        assistanceId: Int = -1,
        trackingSessionId: String? = null
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "gruya_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "GruYa Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones de la app GruYa"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (navType != null && assistanceId > 0) {
                putExtra(EXTRA_NAV_TYPE, navType)
                putExtra(EXTRA_ASSISTANCE_ID, assistanceId)
                if (trackingSessionId != null) {
                    putExtra(EXTRA_TRACKING_SESSION_ID, trackingSessionId)
                }
            }
        }

        // Use unique requestCode per notification (assistanceId or hash of trackingSessionId)
        val requestCode = if (assistanceId > 0) assistanceId else trackingSessionId?.hashCode() ?: 0
        val pendingIntent = PendingIntent.getActivity(
            this, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.iconogruya)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // Use atomic counter for notification ID to avoid collisions
        val notificationId = notificationIdCounter.getAndIncrement()
        notificationManager.notify(notificationId, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
