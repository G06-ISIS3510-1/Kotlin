package com.wheels.app.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wheels.app.MainActivity
import com.wheels.app.core.session.domain.usecase.UpdateCurrentUserFcmTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WheelsFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var updateCurrentUserFcmTokenUseCase: UpdateCurrentUserFcmTokenUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            runCatching { updateCurrentUserFcmTokenUseCase(token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        ensureNotificationChannel()

        val payload = message.toHabitNotificationPayload() ?: return
        if (!canPostNotifications()) return

        NotificationManagerCompat.from(this).notify(
            payload.notificationId,
            buildNotification(payload),
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(payload: HabitNotificationPayload) =
        NotificationCompat.Builder(this, HABIT_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(resolveNotificationIconRes())
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            // Opening the app is enough for this first pass; navigation can be
            // specialized later without coupling delivery to usage tracking.
            .setContentIntent(createContentIntent())
            .build()

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_HABIT_BASED)
        }

        return PendingIntent.getActivity(
            this,
            HABIT_NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val existingChannel = notificationManager.getNotificationChannel(
            HABIT_NOTIFICATION_CHANNEL_ID,
        )
        if (existingChannel != null) return

        // A dedicated channel keeps habit-based engagement messages independent
        // from ride or system notifications that may be added later.
        val channel = NotificationChannel(
            HABIT_NOTIFICATION_CHANNEL_ID,
            HABIT_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = HABIT_NOTIFICATION_CHANNEL_DESCRIPTION
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun resolveNotificationIconRes(): Int {
        val appIcon = applicationInfo.icon
        return if (appIcon != 0) appIcon else android.R.drawable.ic_dialog_info
    }

    private fun RemoteMessage.toHabitNotificationPayload(): HabitNotificationPayload? {
        val messageType = data[KEY_NOTIFICATION_TYPE]
        if (messageType != null && messageType != NOTIFICATION_TYPE_HABIT_BASED) {
            return null
        }

        val title = notification?.title
            ?: data[KEY_NOTIFICATION_TITLE]
            ?: DEFAULT_NOTIFICATION_TITLE
        val body = notification?.body
            ?: data[KEY_NOTIFICATION_BODY]
            ?: DEFAULT_NOTIFICATION_BODY

        return HabitNotificationPayload(
            title = title,
            body = body,
            notificationId = body.hashCode(),
        )
    }

    private data class HabitNotificationPayload(
        val title: String,
        val body: String,
        val notificationId: Int,
    )

    private companion object {
        const val HABIT_NOTIFICATION_CHANNEL_ID = "habit_based_notifications"
        const val HABIT_NOTIFICATION_CHANNEL_NAME = "Habit-Based Notifications"
        const val HABIT_NOTIFICATION_CHANNEL_DESCRIPTION =
            "Notifications sent around the user's peak Wheels usage time."
        const val HABIT_NOTIFICATION_REQUEST_CODE = 1001
        const val KEY_NOTIFICATION_TYPE = "type"
        const val KEY_NOTIFICATION_TITLE = "title"
        const val KEY_NOTIFICATION_BODY = "body"
        const val NOTIFICATION_TYPE_HABIT_BASED = "habit_based_notification"
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val DEFAULT_NOTIFICATION_TITLE = "It's a great time to ride"
        const val DEFAULT_NOTIFICATION_BODY =
            "You usually open Wheels around now. Check the latest rides."
    }
}
