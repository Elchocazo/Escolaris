package com.example.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra("taskId", (1000..9999).random().toLong())
        val subject = intent.getStringExtra("subject") ?: "Colegio"
        val title = intent.getStringExtra("title") ?: "Tarea pendiente"

        NotificationHelper.showPushNotification(
            context = context,
            id = taskId.toInt(),
            title = "📚 Tienes tarea de $subject pendiente",
            message = "La tarea '$title' vence mañana. ¡No olvides completarla a tiempo para ganar tus créditos!",
            type = "HOMEWORK"
        )
    }
}
