package com.example.citizensreportsn.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.citizensreportsn.R

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "voiceful_notifications"
        private const val CHANNEL_NAME = "Changements de statut"
    }

    fun showGenericNotification(title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    fun showStatusNotification(reportTitle: String, newStatus: String) {
        showGenericNotification("Mise à jour de votre signalement", "Le statut de '$reportTitle' est passé à : $newStatus")
    }
}