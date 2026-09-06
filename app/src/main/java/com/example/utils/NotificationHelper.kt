package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {

    private const val CHANNEL_SCHOOL_ALERTS = "school_alerts_channel"
    private const val CHANNEL_HOMEWORK = "school_homework_channel"
    private const val CHANNEL_LATE_HELP = "school_late_help_channel"
    private const val CHANNEL_PARENT_WHATSAPP = "school_parent_whatsapp_channel"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alertsChannel = NotificationChannel(
                CHANNEL_SCHOOL_ALERTS,
                "Avisos y Eventos del Colegio",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de comunicados, eventos y exámenes próximos"
            }

            val homeworkChannel = NotificationChannel(
                CHANNEL_HOMEWORK,
                "Recordatorios de Tareas y Gamificación",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios de fechas límite de tareas y rachas de estudio"
            }

            val helpChannel = NotificationChannel(
                CHANNEL_LATE_HELP,
                "Alertas de Apuntes & Solidaridad Escolar",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Peticiones de compañeros cuando faltan a clase o piden fotos de la pizarra"
            }

            val parentWhatsAppChannel = NotificationChannel(
                CHANNEL_PARENT_WHATSAPP,
                "Recordatorios de Tesorería & Pensiones (WhatsApp)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios automáticos de pagos de pensión los 5 primeros días a las 2:00 PM y deberes familiares"
                enableVibration(true)
            }

            manager.createNotificationChannel(alertsChannel)
            manager.createNotificationChannel(homeworkChannel)
            manager.createNotificationChannel(helpChannel)
            manager.createNotificationChannel(parentWhatsAppChannel)
        }
    }

    fun showPushNotification(
        context: Context,
        id: Int,
        title: String,
        message: String,
        type: String = "LATE_HELP"
    ) {
        val channelId = when (type) {
            "LATE_HELP" -> CHANNEL_LATE_HELP
            "HOMEWORK" -> CHANNEL_HOMEWORK
            "PARENT_OBLIGATION", "WHATSAPP" -> CHANNEL_PARENT_WHATSAPP
            else -> CHANNEL_SCHOOL_ALERTS
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("targetScreen", if (type == "PARENT_OBLIGATION" || type == "WHATSAPP") "parent" else "")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(id, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Muestra una notificación con formato de mensaje de WhatsApp ("Colegio Escolaris - Tesorería")
     */
    fun showWhatsAppStyleNotification(
        context: Context,
        id: Int,
        title: String = "💬 Notificación de Tesorería Escolar (WhatsApp)",
        senderName: String = "🏫 Colegio Escolaris - Tesorería",
        message: String,
        targetScreen: String = "parent"
    ) {
        initNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("targetScreen", targetScreen)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_PARENT_WHATSAPP)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(senderName)
            .setContentText(message)
            .setSubText("WhatsApp Escolar • $title")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(senderName)
                    .setSummaryText("Recordatorio de Pago de Pensión")
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(id, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * Programa el recordatorio de pensión:
     * - Durante los 5 primeros días del mes: 1 vez al día a las 2:00 PM (14:00 horas).
     * - Si no se completa, a partir del día 6: cada 3 días a las 2:00 PM.
     */
    fun scheduleParentPensionReminder(
        context: Context,
        obligationId: Long,
        month: String = "Octubre",
        customMessage: String = ""
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, PensionReminderReceiver::class.java).apply {
            putExtra("obligationId", obligationId)
            putExtra("month", month)
            putExtra("message", customMessage)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (9000 + obligationId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = java.util.Calendar.getInstance()
        val currentDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val currentHour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(java.util.Calendar.MINUTE)

        cal.set(java.util.Calendar.HOUR_OF_DAY, 14) // 2:00 PM
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)

        // Si ya pasaron las 2:00 PM hoy, calcular el próximo disparo
        if (System.currentTimeMillis() >= cal.timeInMillis) {
            if (currentDay < 5) {
                // Siguiente día a las 2:00 PM (durante los 5 primeros días)
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            } else {
                // A partir del día 6: cada 3 días a las 2:00 PM
                cal.add(java.util.Calendar.DAY_OF_MONTH, 3)
            }
        }

        val triggerAtMillis = cal.timeInMillis
        try {
            if (triggerAtMillis > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelParentPensionReminder(context: Context, obligationId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, PensionReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (9000 + obligationId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleTaskReminder(
        context: Context,
        taskId: Long,
        subject: String,
        title: String,
        dueDateMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("subject", subject)
            putExtra("title", title)
            putExtra("dueDateMillis", dueDateMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt().coerceAtLeast(1),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calcular exactamente 1 día antes de la entrega a las 3:00 PM (15:00 horas)
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = dueDateMillis
            add(java.util.Calendar.DAY_OF_YEAR, -1)
            set(java.util.Calendar.HOUR_OF_DAY, 15)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val triggerAtMillis = cal.timeInMillis
        try {
            if (triggerAtMillis > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager ?: return
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt().coerceAtLeast(1),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
