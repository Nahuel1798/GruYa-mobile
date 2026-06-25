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

    override fun onNewToken(token: String) {
        Log.d("FMC", "Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data

        // --- Extract type and assistanceId from data payload ---
        val type = data["type"]
        val assistanceIdStr = data["assistanceId"]
        val assistanceId = assistanceIdStr?.toIntOrNull() ?: -1

        // --- Session guard: if no JWT, silent drop ---
        if (sessionManager.getJwt().isBlank()) {
            Log.d("FMC", "Session guard: no JWT, dropping notification")
            return
        }

        // --- Role guard: check the required role for this event type ---
        val requiredRole = type?.let { getRequiredRole(it) }
        if (requiredRole == null) {
            // Unknown event type: show notification but no NavEvent emission
            val title = message.notification?.title ?: data["title"] ?: ""
            val body = message.notification?.body ?: data["body"] ?: ""
            if (title.isNotEmpty() || body.isNotEmpty()) {
                showNotification(title, body)
                scope.launch {
                    notificationRepository.emitNotification(title, body)
                }
            }
            return
        }

        if (sessionManager.getRole() != requiredRole) {
            // Role mismatch: silent drop
            Log.d("FMC", "Role guard: role mismatch, dropping notification")
            return
        }

        // --- Build NavEvent from type + assistanceId ---
        val trackingSessionId = data["trackingSessionId"]
        val navEvent = type?.let { navEventFromExtras(it, assistanceId, trackingSessionId) }

        val title = message.notification?.title ?: data["title"] ?: ""
        val body = message.notification?.body ?: data["body"] ?: ""

        if (title.isNotEmpty() || body.isNotEmpty()) {
            // Always show notification with extras for PendingIntent (background/dead path)
            showNotification(title, body, type, assistanceId, trackingSessionId)

            // Foreground path: emit NavEvent to bus for in-app snackbar
            if (navEvent != null && navigationEventBus.isForeground) {
                navigationEventBus.emit(navEvent)
                Log.d("FMC", "Emitted NavEvent: $navEvent (foreground)")
            }
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

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
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

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
