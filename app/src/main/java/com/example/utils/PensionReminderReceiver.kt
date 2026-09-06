package com.example.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.EscolarisDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class PensionReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val obligationId = intent.getLongExtra("obligationId", 1L)
        val month = intent.getStringExtra("month") ?: "Octubre"
        val customMessage = intent.getStringExtra("message") ?: ""

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = EscolarisDatabase.getDatabase(context)
                val dao = db.schoolDao()
                val calendar = Calendar.getInstance()
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

                val messageText = if (customMessage.isNotBlank()) {
                    customMessage
                } else if (currentDay <= 5) {
                    "¡Hola estimado acudiente! 👋 Les recordamos que la pensión de $month se cancela los 5 primeros días del mes (hoy es día $currentDay). ¡Cumple a tiempo para ganar la insignia de Pago Oportuno y 100 créditos Escolaris para tu hijo/a! ⭐"
                } else {
                    "⚠️ Recordatorio de Pensión Escolar ($month): Tu pago aún no ha sido marcado como completado. Ingresa a tu checklist para reportar el pago y estar al día."
                }

                NotificationHelper.showWhatsAppStyleNotification(
                    context = context,
                    id = 8800 + obligationId.toInt(),
                    title = "💬 Mensaje de Tesorería Escolar (WhatsApp)",
                    senderName = "🏫 Colegio Escolaris - Tesorería",
                    message = messageText,
                    targetScreen = "parent"
                )

                // Reschedule for next reminder:
                // If day <= 5 -> Daily at 2:00 PM
                // If day > 5 -> Every 3 days at 2:00 PM
                NotificationHelper.scheduleParentPensionReminder(context, obligationId, month, customMessage)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
