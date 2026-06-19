package com.example.gruya.data.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GruYaFirebaseService : FirebaseMessagingService() {
    override fun onNewToken(token: String){
        Log.d("FMC", "Token: $token")
    }
    override fun onMessageReceived(
        message: RemoteMessage
    ) {
        super.onMessageReceived(message)

        val title =
            message.notification?.title ?: ""

        val body =
            message.notification?.body ?: ""

        showNotification(title, body)
    }

    private fun showNotification(
        title: String,
        body: String
    ) {
        // Crear notificación Android
    }
}