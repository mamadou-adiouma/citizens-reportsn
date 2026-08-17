package com.example.citizensreportsn.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nouveau token : $token")
        
        // Optionnel : Envoyer au serveur via Retrofit si l'utilisateur est déjà connecté
        // RetrofitClient.getApiService(this).updateFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val title = message.notification?.title ?: "Mise à jour Voiceful"
        val body = message.notification?.body ?: "Vous avez une nouvelle mise à jour de statut."
        
        NotificationHelper(applicationContext).showGenericNotification(title, body)
    }
}