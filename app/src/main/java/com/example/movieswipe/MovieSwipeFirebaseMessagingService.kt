package com.example.movieswipe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import androidx.core.app.NotificationCompat

class MovieSwipeFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title ?: "MovieSwipe Notification"
        val body = remoteMessage.notification?.body ?: "You have a new notification."
        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send this token to your backend to update the user's FCM token
        Log.d("MovieSwipeFCM", "New token: $token")
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "movieswipe_channel"
        val notificationId = 1
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MovieSwipe Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}

