package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.TaskEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CalendarSyncHelper {

    fun syncExamToDeviceCalendar(context: Context, exam: ExamEntity) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "📝 Examen: ${exam.title} (${exam.subject})")
                putExtra(CalendarContract.Events.DESCRIPTION, "Temas a evaluar: ${exam.topics}\nAula: ${exam.classroom}")
                putExtra(CalendarContract.Events.EVENT_LOCATION, exam.classroom)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, exam.examDateMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, exam.examDateMillis + (2 * 3600000L))
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PUBLIC)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No se encontró aplicación de calendario compatible", Toast.LENGTH_SHORT).show()
        }
    }

    fun syncTaskToDeviceCalendar(context: Context, task: TaskEntity) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "📚 Entrega: ${task.title} (${task.subject})")
                putExtra(CalendarContract.Events.DESCRIPTION, task.description)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, task.dueDateMillis - 3600000L)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, task.dueDateMillis)
                putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No se encontró aplicación de calendario compatible", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportCalendarIcs(context: Context, exams: List<ExamEntity>, tasks: List<TaskEntity>) {
        try {
            val icsDateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val sb = StringBuilder()
            sb.appendLine("BEGIN:VCALENDAR")
            sb.appendLine("VERSION:2.0")
            sb.appendLine("PRODID:-//Escolaris//Calendario Académico//ES")
            sb.appendLine("CALSCALE:GREGORIAN")
            sb.appendLine("METHOD:PUBLISH")
            sb.appendLine("X-WR-CALNAME:Escolaris - Calendario Escolar")

            // Add Exams
            for (exam in exams) {
                val startUtc = icsDateFormat.format(Date(exam.examDateMillis))
                val endUtc = icsDateFormat.format(Date(exam.examDateMillis + 7200000L))
                val nowUtc = icsDateFormat.format(Date())

                sb.appendLine("BEGIN:VEVENT")
                sb.appendLine("UID:exam-${exam.id}-${exam.examDateMillis}@escolaris.app")
                sb.appendLine("DTSTAMP:$nowUtc")
                sb.appendLine("DTSTART:$startUtc")
                sb.appendLine("DTEND:$endUtc")
                sb.appendLine("SUMMARY:📝 Examen: ${exam.title} (${exam.subject})")
                sb.appendLine("DESCRIPTION:Temario: ${exam.topics} | Aula: ${exam.classroom}")
                sb.appendLine("LOCATION:${exam.classroom}")
                sb.appendLine("STATUS:CONFIRMED")
                sb.appendLine("END:VEVENT")
            }

            // Add Tasks
            for (task in tasks) {
                val dueUtc = icsDateFormat.format(Date(task.dueDateMillis))
                val startUtc = icsDateFormat.format(Date(task.dueDateMillis - 3600000L))
                val nowUtc = icsDateFormat.format(Date())

                sb.appendLine("BEGIN:VEVENT")
                sb.appendLine("UID:task-${task.id}-${task.dueDateMillis}@escolaris.app")
                sb.appendLine("DTSTAMP:$nowUtc")
                sb.appendLine("DTSTART:$startUtc")
                sb.appendLine("DTEND:$dueUtc")
                sb.appendLine("SUMMARY:📚 Tarea: ${task.title} (${task.subject})")
                sb.appendLine("DESCRIPTION:${task.description}")
                sb.appendLine("STATUS:CONFIRMED")
                sb.appendLine("END:VEVENT")
            }

            sb.appendLine("END:VCALENDAR")

            val fileDir = File(context.cacheDir, "reports")
            if (!fileDir.exists()) fileDir.mkdirs()

            val icsFile = File(fileDir, "Calendario_Escolaris.ics")
            val fos = FileOutputStream(icsFile)
            fos.write(sb.toString().toByteArray(Charsets.UTF_8))
            fos.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                icsFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/calendar"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sincronización de Calendario Escolaris (.ics)")
                putExtra(Intent.EXTRA_TEXT, "Importa este archivo en Google Calendar, iCloud Calendar o Microsoft Outlook para sincronizar tus exámenes y entregas.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Sincronizar con Google Calendar / iCloud (.ics)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "Archivo .ics generado para Google Calendar e iCloud", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al sincronizar calendario: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
