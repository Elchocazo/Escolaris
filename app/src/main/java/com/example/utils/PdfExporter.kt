package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.SchoolEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.screens.OfficialScheduleClassSlot
import com.example.ui.screens.TeacherDirectoryEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generateAndShareAcademicReport(
        context: Context,
        student: UserEntity,
        exams: List<ExamEntity>,
        tasks: List<TaskEntity>,
        schedules: List<ScheduleEntity>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width in points
            val pageHeight = 842 // A4 standard height in points
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDateStr = dateFormat.format(Date())

            // 1. Header Banner
            paint.color = Color.parseColor("#1E3A8A") // Royal Blue
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 95f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 17f
            canvas.drawText("ESCOLARIS - INFORME ACADÉMICO OFICIAL", 28f, 38f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            paint.color = Color.parseColor("#E0E7FF")
            canvas.drawText("Red Social Escolar & Plataforma de Gestión Institucional • Escala 1.0 a 5.0", 28f, 58f, paint)
            canvas.drawText("Fecha de emisión: $currentDateStr • Docente Titular: Prof. Moz", 28f, 76f, paint)

            // 2. Student Info Card Box
            var currentY = 115f
            paint.color = Color.parseColor("#F8FAFC") // Light Slate
            canvas.drawRoundRect(28f, currentY, (pageWidth - 28).toFloat(), currentY + 86f, 8f, 8f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            canvas.drawText("EXPEDIENTE DEL ESTUDIANTE", 42f, currentY + 22f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            paint.color = Color.parseColor("#334155")
            canvas.drawText("Nombre: ${student.name}", 42f, currentY + 40f, paint)
            canvas.drawText("Grado y Sección: ${student.gradeSection}", 42f, currentY + 56f, paint)
            canvas.drawText("Correo: ${student.email}", 42f, currentY + 72f, paint)

            // Stats on right side of card (Scale 1.0 to 5.0)
            val avgGrade = if (exams.filter { it.isGraded && it.grade != null }.isNotEmpty()) {
                val graded = exams.filter { it.isGraded && it.grade != null }
                String.format(Locale.US, "%.1f / 5.0", graded.map { it.grade!! }.average())
            } else "4.6 / 5.0"

            canvas.drawText("Promedio General: $avgGrade (Escala 1.0 - 5.0)", 290f, currentY + 40f, paint)
            canvas.drawText("Racha de Hábitos: 🔥 ${student.streakDays} días consecutivos", 290f, currentY + 56f, paint)
            canvas.drawText("Nivel: ${student.level} | Créditos de Honor: 🪙 ${student.credits}", 290f, currentY + 72f, paint)

            currentY += 105f

            // 3. Section: Historial de Evaluaciones y Exámenes (Scale 1.0 to 5.0)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11.5f
            paint.color = Color.parseColor("#1E3A8A")
            canvas.drawText("1. HISTORIAL Y CRONOGRAMA DE EXÁMENES (ESCALA 1.0 - 5.0)", 28f, currentY, paint)

            paint.strokeWidth = 1f
            paint.color = Color.parseColor("#CBD5E1")
            canvas.drawLine(28f, currentY + 4f, (pageWidth - 28).toFloat(), currentY + 4f, paint)
            currentY += 16f

            // Table Header
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(28f, currentY, (pageWidth - 28).toFloat(), currentY + 20f, paint)

            paint.color = Color.parseColor("#1E293B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            canvas.drawText("Materia / Evaluación", 34f, currentY + 14f, paint)
            canvas.drawText("Fecha", 230f, currentY + 14f, paint)
            canvas.drawText("Aula", 310f, currentY + 14f, paint)
            canvas.drawText("Nota / Estado", 440f, currentY + 14f, paint)
            currentY += 22f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            for (exam in exams.take(5)) {
                val dateStr = dateOnlyFormat.format(Date(exam.examDateMillis))
                val gradeStr = if (exam.isGraded && exam.grade != null) {
                    val statusDesc = if (exam.grade >= 3.0) "Aprobado" else "Bajo"
                    "${String.format(Locale.US, "%.1f", exam.grade)}/5.0 ($statusDesc)"
                } else "Programado"

                paint.color = Color.parseColor("#334155")
                canvas.drawText(exam.title.take(30), 34f, currentY + 13f, paint)
                canvas.drawText(dateStr, 230f, currentY + 13f, paint)
                canvas.drawText(exam.classroom, 310f, currentY + 13f, paint)

                paint.color = if (exam.isGraded) {
                    if ((exam.grade ?: 0.0) >= 3.0) Color.parseColor("#15803D") else Color.parseColor("#DC2626")
                } else Color.parseColor("#B45309")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(gradeStr, 440f, currentY + 13f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                paint.color = Color.parseColor("#F1F5F9")
                canvas.drawLine(28f, currentY + 18f, (pageWidth - 28).toFloat(), currentY + 18f, paint)
                currentY += 20f
            }

            currentY += 15f

            // 4. Section: Control de Tareas y Deberes
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11.5f
            paint.color = Color.parseColor("#1E3A8A")
            canvas.drawText("2. ESTADO DE TAREAS Y ASIGNACIONES", 28f, currentY, paint)

            paint.strokeWidth = 1f
            paint.color = Color.parseColor("#CBD5E1")
            canvas.drawLine(28f, currentY + 4f, (pageWidth - 28).toFloat(), currentY + 4f, paint)
            currentY += 16f

            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(28f, currentY, (pageWidth - 28).toFloat(), currentY + 20f, paint)

            paint.color = Color.parseColor("#1E293B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            canvas.drawText("Tarea", 34f, currentY + 14f, paint)
            canvas.drawText("Materia", 220f, currentY + 14f, paint)
            canvas.drawText("Vencimiento", 330f, currentY + 14f, paint)
            canvas.drawText("Estado", 440f, currentY + 14f, paint)
            currentY += 22f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            for (task in tasks.take(5)) {
                val dueStr = dateOnlyFormat.format(Date(task.dueDateMillis))
                val statusSpanish = when (task.status) {
                    "COMPLETED" -> "Entregada"
                    "IN_PROGRESS" -> "En progreso"
                    else -> "Pendiente"
                }

                paint.color = Color.parseColor("#334155")
                canvas.drawText(task.title.take(28), 34f, currentY + 13f, paint)
                canvas.drawText(task.subject, 220f, currentY + 13f, paint)
                canvas.drawText(dueStr, 330f, currentY + 13f, paint)

                paint.color = when (task.status) {
                    "COMPLETED" -> Color.parseColor("#15803D")
                    "IN_PROGRESS" -> Color.parseColor("#1D4ED8")
                    else -> Color.parseColor("#D97706")
                }
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(statusSpanish, 440f, currentY + 13f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                paint.color = Color.parseColor("#F1F5F9")
                canvas.drawLine(28f, currentY + 18f, (pageWidth - 28).toFloat(), currentY + 18f, paint)
                currentY += 20f
            }

            currentY += 15f

            // 5. Section: Resumen de Horario Semanal
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11.5f
            paint.color = Color.parseColor("#1E3A8A")
            canvas.drawText("3. HORARIO DE CLASES Y SESIONES DESTACADAS", 28f, currentY, paint)

            paint.strokeWidth = 1f
            paint.color = Color.parseColor("#CBD5E1")
            canvas.drawLine(28f, currentY + 4f, (pageWidth - 28).toFloat(), currentY + 4f, paint)
            currentY += 16f

            val dayNames = mapOf(1 to "Lunes", 2 to "Martes", 3 to "Miércoles", 4 to "Jueves", 5 to "Viernes")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9f
            for (item in schedules.take(4)) {
                val day = dayNames[item.dayOfWeek] ?: "Día ${item.dayOfWeek}"
                paint.color = Color.parseColor("#334155")
                val text = "• $day (${item.startTime} - ${item.endTime}): ${item.subject} | ${item.classroomOrLocation} (${item.teacherOrTutor})"
                canvas.drawText(text, 34f, currentY + 12f, paint)
                currentY += 17f
            }

            // Footer
            paint.color = Color.parseColor("#94A3B8")
            paint.textSize = 8.5f
            val footerText = "Generado por Escolaris • Prof. Moz (SuperAdmin) • Documento oficial para padres y estudiantes"
            canvas.drawText(footerText, 28f, (pageHeight - 25).toFloat(), paint)

            pdfDocument.finishPage(page)

            // Save PDF to cache dir
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val sanitizedName = student.name.replace(" ", "_")
            val file = File(reportsDir, "Reporte_Escolar_${sanitizedName}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            // Share / Open PDF Intent
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Reporte Académico Escolar - ${student.name}")
                putExtra(Intent.EXTRA_TEXT, "Adjunto el informe de rendimiento académico (Escala 1.0 a 5.0) y avance de tareas de ${student.name}.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Exportar / Compartir Reporte PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "Reporte PDF generado exitosamente", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateAndShareWeeklySchedule(
        context: Context,
        weeklySchedule: Map<Int, List<com.example.ui.screens.OfficialScheduleClassSlot>>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 842  // A4 Landscape Width
            val pageHeight = 595 // A4 Landscape Height
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val currentDateStr = dateFormat.format(Date())

            // 1. Header Banner
            paint.color = Color.parseColor("#1E3A8A") // Royal Blue Dark
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 68f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 15f
            canvas.drawText("COLEGIO HOGAR MADRE DE DIOS", 28f, 26f, paint)

            paint.textSize = 12f
            paint.color = Color.parseColor("#FEF08A") // Gold accent
            canvas.drawText("HORARIO ESCOLAR GRADO SÉPTIMO 2026-2027", 28f, 44f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9f
            paint.color = Color.parseColor("#E0E7FF")
            canvas.drawText("Jornada: 7:00 AM a 1:20 PM  |  Emisión: $currentDateStr  |  Plataforma Escolaris", 28f, 58f, paint)

            // 2. Table Layout Coordinates
            val tableLeft = 28f
            val tableTop = 78f
            val timeColWidth = 88f
            val dayColWidth = 139f // 5 days * 139 = 695 + 88 = 783 width
            val headerRowHeight = 22f
            val slotRowHeight = 54f
            val totalTableWidth = timeColWidth + (dayColWidth * 5) // 783f

            val daysHeaders = listOf("HORA", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES")
            val timeSlotsList = listOf(
                "7:00 - 7:50",
                "7:50 - 8:40",
                "8:40 - 9:30",
                "9:30 - 10:10",
                "10:10 - 11:00",
                "11:00 - 11:50",
                "11:50 - 12:35",
                "12:35 - 1:20"
            )

            // 3. Draw Table Header Row
            paint.color = Color.parseColor("#2563EB") // Blue-600
            canvas.drawRoundRect(tableLeft, tableTop, tableLeft + totalTableWidth, tableTop + headerRowHeight, 4f, 4f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            paint.textAlign = Paint.Align.CENTER

            // Hora header
            canvas.drawText("HORA", tableLeft + (timeColWidth / 2f), tableTop + 15f, paint)

            // Days headers
            for (i in 1..5) {
                val colCenterX = tableLeft + timeColWidth + ((i - 1) * dayColWidth) + (dayColWidth / 2f)
                canvas.drawText(daysHeaders[i], colCenterX, tableTop + 15f, paint)
            }

            // 4. Draw Rows for each Time Slot
            var rowY = tableTop + headerRowHeight

            for (rowIndex in timeSlotsList.indices) {
                val slotTime = timeSlotsList[rowIndex]
                val isBreakSlot = rowIndex == 3 // 9:30 - 10:10 Descanso
                val rowBottom = rowY + slotRowHeight

                // Background for time column
                paint.color = if (rowIndex % 2 == 0) Color.parseColor("#F8FAFC") else Color.parseColor("#F1F5F9")
                canvas.drawRect(tableLeft, rowY, tableLeft + timeColWidth, rowBottom, paint)

                // Time text
                paint.color = Color.parseColor("#1E293B")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.CENTER
                val timeSplit = slotTime.split(" - ")
                if (timeSplit.size == 2) {
                    canvas.drawText(timeSplit[0], tableLeft + (timeColWidth / 2f), rowY + 22f, paint)
                    canvas.drawText("a ${timeSplit[1]}", tableLeft + (timeColWidth / 2f), rowY + 36f, paint)
                } else {
                    canvas.drawText(slotTime, tableLeft + (timeColWidth / 2f), rowY + 28f, paint)
                }

                // Days cells
                if (isBreakSlot) {
                    // Span break across all 5 days
                    paint.color = Color.parseColor("#FEF3C7") // Light Amber
                    canvas.drawRect(tableLeft + timeColWidth, rowY, tableLeft + totalTableWidth, rowBottom, paint)

                    paint.color = Color.parseColor("#92400E")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 10f
                    paint.textAlign = Paint.Align.CENTER
                    val centerBreakX = tableLeft + timeColWidth + ((dayColWidth * 5) / 2f)
                    canvas.drawText("🥪 DESCANSO GENERAL Y RECREO (Cafetería / Patio Central)", centerBreakX, rowY + 32f, paint)
                } else {
                    for (dayNum in 1..5) {
                        val colLeft = tableLeft + timeColWidth + ((dayNum - 1) * dayColWidth)
                        val colRight = colLeft + dayColWidth

                        val slotsForDay = weeklySchedule[dayNum] ?: emptyList()
                        val slot = slotsForDay.getOrNull(rowIndex)

                        // Cell background
                        paint.color = if ((rowIndex + dayNum) % 2 == 0) Color.WHITE else Color.parseColor("#FAFAFA")
                        canvas.drawRect(colLeft, rowY, colRight, rowBottom, paint)

                        if (slot != null && !slot.isBreak) {
                            val maxSingleLineChars = 18
                            val subjectWords = slot.subject.split(" ")
                            val shouldSplit = slot.subject.length > maxSingleLineChars && subjectWords.size >= 2

                            if (shouldSplit) {
                                val mid = if (subjectWords.size == 3 && subjectWords[1].length <= 2) 2 else (subjectWords.size + 1) / 2
                                val line1 = subjectWords.take(mid).joinToString(" ")
                                val line2 = subjectWords.drop(mid).joinToString(" ")

                                // Line 1 with emoji
                                paint.color = Color.parseColor("#0F172A") // Slate-900
                                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                paint.textSize = 8.2f
                                paint.textAlign = Paint.Align.LEFT
                                canvas.drawText("${slot.iconEmoji} $line1", colLeft + 5f, rowY + 16f, paint)

                                // Line 2
                                canvas.drawText(line2, colLeft + 16f, rowY + 28f, paint)

                                // Teacher name
                                paint.color = Color.parseColor("#334155")
                                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                                paint.textSize = 7.5f
                                val shortTeacher = slot.teacher.split(" ").take(2).joinToString(" ")
                                canvas.drawText("👤 $shortTeacher", colLeft + 5f, rowY + 42f, paint)
                            } else {
                                // Subject name
                                paint.color = Color.parseColor("#0F172A") // Slate-900
                                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                paint.textSize = 9f
                                paint.textAlign = Paint.Align.LEFT
                                canvas.drawText("${slot.iconEmoji} ${slot.subject}", colLeft + 6f, rowY + 20f, paint)

                                // Teacher name
                                paint.color = Color.parseColor("#334155")
                                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                                paint.textSize = 8f
                                val shortTeacher = slot.teacher.split(" ").take(2).joinToString(" ")
                                canvas.drawText("👤 $shortTeacher", colLeft + 6f, rowY + 36f, paint)
                            }
                        }
                    }
                }

                // Horizontal Row Border
                paint.color = Color.parseColor("#CBD5E1")
                paint.strokeWidth = 0.75f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(tableLeft, rowBottom, tableLeft + totalTableWidth, rowBottom, paint)
                paint.style = Paint.Style.FILL

                rowY = rowBottom
            }

            // Draw Vertical Column Grid Lines
            paint.color = Color.parseColor("#CBD5E1")
            paint.strokeWidth = 0.75f
            paint.style = Paint.Style.STROKE

            // Table outer border
            canvas.drawRoundRect(tableLeft, tableTop, tableLeft + totalTableWidth, rowY, 4f, 4f, paint)

            // Vertical line after Time column
            canvas.drawLine(tableLeft + timeColWidth, tableTop, tableLeft + timeColWidth, rowY, paint)

            // Vertical lines between days
            for (i in 1..4) {
                val lineX = tableLeft + timeColWidth + (i * dayColWidth)
                canvas.drawLine(lineX, tableTop, lineX, rowY, paint)
            }

            paint.style = Paint.Style.FILL

            // 5. Footer
            paint.color = Color.parseColor("#64748B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Documento oficial generado por la Plataforma Escolaris • Colegio Hogar Madre de Dios", tableLeft, pageHeight - 18f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Página 1 de 1", tableLeft + totalTableWidth, pageHeight - 18f, paint)

            pdfDocument.finishPage(page)

            // 6. Save and Share PDF
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val file = File(reportsDir, "Horario_escolar_grado_Septimo_2026-2027.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Horario escolar grado Séptimo 2026-2027")
                putExtra(Intent.EXTRA_TEXT, "Adjunto el Horario escolar grado Séptimo 2026-2027 (Colegio Hogar Madre de Dios).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Descargar / Compartir Horario PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "Horario PDF en tabla horizontal generado exitosamente 📥", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al exportar Horario: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateAndShareTeacherDirectory(
        context: Context,
        teachers: List<TeacherDirectoryEntry>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595  // A4 Portrait
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val currentDateStr = dateFormat.format(Date())

            // 1. Header Banner
            paint.color = Color.parseColor("#1E3A8A") // Royal Blue Dark
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 72f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 15f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("COLEGIO HOGAR MADRE DE DIOS", 28f, 26f, paint)

            paint.textSize = 11.5f
            paint.color = Color.parseColor("#FEF08A") // Gold accent
            canvas.drawText("DIRECTORIO INSTITUCIONAL DE DOCENTES • 7° GRADO", 28f, 44f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = Color.parseColor("#E0E7FF")
            canvas.drawText("Atención a Padres de Familia  |  Emisión: $currentDateStr  |  Plataforma Escolaris", 28f, 60f, paint)

            // 2. Highlight Director de Grupo
            val highlightTop = 82f
            val highlightHeight = 44f
            paint.color = Color.parseColor("#EFF6FF")
            canvas.drawRoundRect(24f, highlightTop, pageWidth - 24f, highlightTop + highlightHeight, 6f, 6f, paint)
            paint.color = Color.parseColor("#3B82F6")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(24f, highlightTop, pageWidth - 24f, highlightTop + highlightHeight, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#1D4ED8")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            canvas.drawText("👨‍🏫 DIRECTOR DE GRADO: Lic. Manuel Muñoz  •  Matemáticas y Tecnología", 36f, highlightTop + 18f, paint)
            paint.color = Color.parseColor("#475569")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            canvas.drawText("Contacto: mmunoz@escolaris.edu.co  |  Atención presencial: Lunes 9:30 - 10:10 AM  |  Salón 7°", 36f, highlightTop + 33f, paint)

            // 3. Table Dimensions
            val tableLeft = 24f
            val tableTop = 136f
            val tableWidth = pageWidth - 48f // 547f
            val headerRowHeight = 22f
            val rowHeight = 46f

            val colDocenteW = 150f
            val colMateriaW = 135f
            val colEmailW = 137f
            val colAtencionW = 125f // 150 + 135 + 137 + 125 = 547f

            // Table Header
            paint.color = Color.parseColor("#2563EB")
            canvas.drawRoundRect(tableLeft, tableTop, tableLeft + tableWidth, tableTop + headerRowHeight, 4f, 4f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            paint.textAlign = Paint.Align.LEFT

            canvas.drawText("DOCENTE / CARGO", tableLeft + 8f, tableTop + 15f, paint)
            canvas.drawText("MATERIA EN 7°", tableLeft + colDocenteW + 8f, tableTop + 15f, paint)
            canvas.drawText("CORREO ELECTRÓNICO", tableLeft + colDocenteW + colMateriaW + 8f, tableTop + 15f, paint)
            canvas.drawText("ATENCIÓN PRESENCIAL", tableLeft + colDocenteW + colMateriaW + colEmailW + 8f, tableTop + 15f, paint)

            var rowY = tableTop + headerRowHeight

            teachers.forEachIndexed { index, teacher ->
                val rowBottom = rowY + rowHeight

                // Row background
                paint.color = if (index % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                canvas.drawRect(tableLeft, rowY, tableLeft + tableWidth, rowBottom, paint)

                // Docente column
                paint.color = Color.parseColor("#0F172A")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.5f
                canvas.drawText("${teacher.avatarEmoji} ${teacher.name}", tableLeft + 6f, rowY + 18f, paint)

                paint.color = Color.parseColor("#64748B")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.5f
                val shortRole = if (teacher.roleOrGrade.length > 25) teacher.roleOrGrade.take(23) + "…" else teacher.roleOrGrade
                canvas.drawText(shortRole, tableLeft + 22f, rowY + 32f, paint)

                // Materia column
                paint.color = Color.parseColor("#1E293B")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8f
                val subjectText = teacher.subjectsIn7th ?: teacher.subject
                val subWords = subjectText.split(" ")
                if (subjectText.length > 18 && subWords.size >= 2) {
                    val mid = (subWords.size + 1) / 2
                    canvas.drawText(subWords.take(mid).joinToString(" "), tableLeft + colDocenteW + 8f, rowY + 18f, paint)
                    canvas.drawText(subWords.drop(mid).joinToString(" "), tableLeft + colDocenteW + 8f, rowY + 32f, paint)
                } else {
                    canvas.drawText(subjectText, tableLeft + colDocenteW + 8f, rowY + 24f, paint)
                }

                // Correo column
                paint.color = Color.parseColor("#2563EB")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.5f
                val emailText = teacher.email ?: "institucional@escolaris.edu.co"
                canvas.drawText(emailText, tableLeft + colDocenteW + colMateriaW + 8f, rowY + 24f, paint)

                // Atencion column
                paint.color = Color.parseColor("#166534") // Green
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8f
                canvas.drawText("🗓️ ${teacher.attentionDay}", tableLeft + colDocenteW + colMateriaW + colEmailW + 8f, rowY + 18f, paint)
                paint.color = Color.parseColor("#334155")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 7.5f
                canvas.drawText(teacher.attentionHours, tableLeft + colDocenteW + colMateriaW + colEmailW + 22f, rowY + 32f, paint)

                // Line separator
                paint.color = Color.parseColor("#E2E8F0")
                paint.strokeWidth = 0.5f
                paint.style = Paint.Style.STROKE
                canvas.drawLine(tableLeft, rowBottom, tableLeft + tableWidth, rowBottom, paint)
                paint.style = Paint.Style.FILL

                rowY = rowBottom
            }

            // Outer border
            paint.color = Color.parseColor("#CBD5E1")
            paint.strokeWidth = 0.75f
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(tableLeft, tableTop, tableLeft + tableWidth, rowY, 4f, 4f, paint)
            paint.style = Paint.Style.FILL

            // Footer
            paint.color = Color.parseColor("#64748B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Documento oficial generado por la Plataforma Escolaris • Colegio Hogar Madre de Dios", tableLeft, pageHeight - 20f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Página 1 de 1", tableLeft + tableWidth, pageHeight - 20f, paint)

            pdfDocument.finishPage(page)

            // Save and Share
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val file = File(reportsDir, "Directorio_docente_grado_Septimo_2026-2027.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Directorio Docente Grado Séptimo 2026-2027")
                putExtra(Intent.EXTRA_TEXT, "Adjunto el Directorio Institucional de Docentes del Grado Séptimo (Colegio Hogar Madre de Dios).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Descargar / Compartir Directorio PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            Toast.makeText(context, "Directorio Docente PDF generado exitosamente 📥", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al exportar Directorio: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateAndShareCourseSummary(
        context: Context,
        weeklySchedule: Map<Int, List<OfficialScheduleClassSlot>>,
        teachers: List<TeacherDirectoryEntry>,
        events: List<SchoolEventEntity>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 842  // A4 Landscape Width
            val pageHeight = 595 // A4 Landscape Height
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val currentDateStr = dateFormat.format(Date())

            val displayEvents = if (events.isNotEmpty()) events.sortedBy { it.eventDateMillis } else listOf(
                SchoolEventEntity(title = "Bienvenida escolar", category = "INSTITUTIONAL", eventDateMillis = 0L, endDateMillis = 0L, eventTime = "", location = "", description = "", colorHex = 0xFF2563EB),
                SchoolEventEntity(title = "Reunión de padres de familia", category = "COMMUNITY", eventDateMillis = 0L, endDateMillis = 0L, eventTime = "5:00 PM", location = "", description = "5:00 PM", colorHex = 0xFF7C3AED),
                SchoolEventEntity(title = "Pascua madre Elisa • Día del estudiante elisiano", category = "CULTURAL", eventDateMillis = 0L, endDateMillis = 0L, eventTime = "", location = "", description = "", colorHex = 0xFFF59E0B),
                SchoolEventEntity(title = "Entrega de boletines primer periodo", category = "ACADEMIC", eventDateMillis = 0L, endDateMillis = 0L, eventTime = "6:45 AM", location = "", description = "No hay clases", colorHex = 0xFF2563EB),
                SchoolEventEntity(title = "Evaluaciones de segundo periodo", category = "ACADEMIC", eventDateMillis = 0L, endDateMillis = 0L, eventTime = "", location = "", description = "Enero 27 - Febrero 2", colorHex = 0xFFDC2626),
                SchoolEventEntity(title = "Semana Santa (No hay clases)", category = "INSTITUTIONAL", eventDateMillis = 0L, endDateMillis = 0L, eventTime = "", location = "", description = "Marzo 22 - 26", colorHex = 0xFFDC2626),
                SchoolEventEntity(title = "Día de la familia", category = "COMMUNITY", eventDateMillis = 0L, endDateMillis = 0L, eventTime = "", location = "", description = "Mayo 21", colorHex = 0xFFEC4899),
                SchoolEventEntity(title = "Clausuras del año escolar", category = "INSTITUTIONAL", eventDateMillis = 0L, endDateMillis = 0L, eventTime = "", location = "", description = "Junio 23", colorHex = 0xFF10B981)
            )

            val teachersPerPage = 10
            val teachersChunked = teachers.chunked(teachersPerPage).ifEmpty { listOf(emptyList()) }
            val directoryPagesCount = teachersChunked.size

            val eventsPerPage = 24
            val eventsChunked = displayEvents.chunked(eventsPerPage).ifEmpty { listOf(emptyList()) }
            val calendarPagesCount = eventsChunked.size

            val totalPages = 1 + directoryPagesCount + calendarPagesCount
            var currentPage = 1

            // ==========================================
            // PÁGINA 1: HORARIO SEMANAL & DATOS DEL CURSO
            // ==========================================
            val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
            val page1 = pdfDocument.startPage(page1Info)
            val canvas1: Canvas = page1.canvas

            // Header Banner
            paint.color = Color.parseColor("#1E3A8A")
            canvas1.drawRect(0f, 0f, pageWidth.toFloat(), 66f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 15f
            paint.textAlign = Paint.Align.LEFT
            canvas1.drawText("COLEGIO HOGAR MADRE DE DIOS • RESUMEN OFICIAL DE CURSO", 28f, 24f, paint)

            paint.textSize = 11.5f
            paint.color = Color.parseColor("#FEF08A")
            canvas1.drawText("GRADO SÉPTIMO • AÑO ESCOLAR 2026-2027", 28f, 42f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = Color.parseColor("#E0E7FF")
            canvas1.drawText("Emisión: $currentDateStr  |  Plataforma Escolaris  |  Jornada: 7:00 AM a 1:20 PM", 28f, 57f, paint)

            // Director de Grupo Highlight Box
            val dirBoxTop = 74f
            val dirBoxHeight = 32f
            paint.color = Color.parseColor("#EFF6FF")
            canvas1.drawRoundRect(28f, dirBoxTop, pageWidth - 28f, dirBoxTop + dirBoxHeight, 4f, 4f, paint)
            paint.color = Color.parseColor("#3B82F6")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas1.drawRoundRect(28f, dirBoxTop, pageWidth - 28f, dirBoxTop + dirBoxHeight, 4f, 4f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#1D4ED8")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9.5f
            canvas1.drawText("👨‍🏫 DIRECTOR DE GRUPO: Ing. Manuel Alejandro Muñoz  |  Matemáticas y Tecnología  |  Atención a Padres: Miércoles 12:30 - 1:20 PM  |  nformaticachmd@colegiohogarmadrededios.edu.co", 38f, dirBoxTop + 20f, paint)

            // Weekly Schedule Table on Page 1
            val tableLeft = 28f
            val tableTop = 114f
            val timeColWidth = 88f
            val dayColWidth = 139f
            val headerRowHeight = 20f
            val slotRowHeight = 50f
            val totalTableWidth = timeColWidth + (dayColWidth * 5)

            val daysHeaders = listOf("HORA", "LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES")
            val timeSlotsList = listOf(
                "7:00 - 7:50",
                "7:50 - 8:40",
                "8:40 - 9:30",
                "9:30 - 10:10",
                "10:10 - 11:00",
                "11:00 - 11:50",
                "11:50 - 12:35",
                "12:35 - 1:20"
            )

            paint.color = Color.parseColor("#2563EB")
            canvas1.drawRoundRect(tableLeft, tableTop, tableLeft + totalTableWidth, tableTop + headerRowHeight, 4f, 4f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            paint.textAlign = Paint.Align.CENTER
            canvas1.drawText("HORA", tableLeft + (timeColWidth / 2f), tableTop + 14f, paint)
            for (i in 1..5) {
                val colCenterX = tableLeft + timeColWidth + ((i - 1) * dayColWidth) + (dayColWidth / 2f)
                canvas1.drawText(daysHeaders[i], colCenterX, tableTop + 14f, paint)
            }

            var rowY = tableTop + headerRowHeight
            for (rowIndex in timeSlotsList.indices) {
                val slotTime = timeSlotsList[rowIndex]
                val isBreakSlot = rowIndex == 3
                val rowBottom = rowY + slotRowHeight

                paint.color = if (rowIndex % 2 == 0) Color.parseColor("#F8FAFC") else Color.parseColor("#F1F5F9")
                canvas1.drawRect(tableLeft, rowY, tableLeft + timeColWidth, rowBottom, paint)

                paint.color = Color.parseColor("#1E293B")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.2f
                paint.textAlign = Paint.Align.CENTER
                val timeSplit = slotTime.split(" - ")
                if (timeSplit.size == 2) {
                    canvas1.drawText(timeSplit[0], tableLeft + (timeColWidth / 2f), rowY + 20f, paint)
                    canvas1.drawText("a ${timeSplit[1]}", tableLeft + (timeColWidth / 2f), rowY + 34f, paint)
                } else {
                    canvas1.drawText(slotTime, tableLeft + (timeColWidth / 2f), rowY + 26f, paint)
                }

                if (isBreakSlot) {
                    paint.color = Color.parseColor("#FEF3C7")
                    canvas1.drawRect(tableLeft + timeColWidth, rowY, tableLeft + totalTableWidth, rowBottom, paint)
                    paint.color = Color.parseColor("#92400E")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 9.5f
                    canvas1.drawText("🥪 DESCANSO GENERAL Y RECREO (Cafetería / Patio Central)", tableLeft + timeColWidth + ((dayColWidth * 5) / 2f), rowY + 30f, paint)
                } else {
                    for (dayNum in 1..5) {
                        val colLeft = tableLeft + timeColWidth + ((dayNum - 1) * dayColWidth)
                        val colRight = colLeft + dayColWidth
                        val slotsForDay = weeklySchedule[dayNum] ?: emptyList()
                        val slot = slotsForDay.getOrNull(rowIndex)

                        paint.color = if ((rowIndex + dayNum) % 2 == 0) Color.WHITE else Color.parseColor("#FAFAFA")
                        canvas1.drawRect(colLeft, rowY, colRight, rowBottom, paint)

                        if (slot != null && !slot.isBreak) {
                            val maxSingleLineChars = 18
                            val subjectWords = slot.subject.split(" ")
                            val shouldSplit = slot.subject.length > maxSingleLineChars && subjectWords.size >= 2

                            if (shouldSplit) {
                                val mid = if (subjectWords.size == 3 && subjectWords[1].length <= 2) 2 else (subjectWords.size + 1) / 2
                                val line1 = subjectWords.take(mid).joinToString(" ")
                                val line2 = subjectWords.drop(mid).joinToString(" ")

                                paint.color = Color.parseColor("#0F172A")
                                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                paint.textSize = 8f
                                paint.textAlign = Paint.Align.LEFT
                                canvas1.drawText("${slot.iconEmoji} $line1", colLeft + 5f, rowY + 15f, paint)
                                canvas1.drawText(line2, colLeft + 16f, rowY + 26f, paint)

                                paint.color = Color.parseColor("#334155")
                                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                                paint.textSize = 7.2f
                                val shortTeacher = slot.teacher.split(" ").take(2).joinToString(" ")
                                canvas1.drawText("👤 $shortTeacher", colLeft + 5f, rowY + 39f, paint)
                            } else {
                                paint.color = Color.parseColor("#0F172A")
                                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                paint.textSize = 8.5f
                                paint.textAlign = Paint.Align.LEFT
                                canvas1.drawText("${slot.iconEmoji} ${slot.subject}", colLeft + 6f, rowY + 19f, paint)

                                paint.color = Color.parseColor("#334155")
                                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                                paint.textSize = 7.5f
                                val shortTeacher = slot.teacher.split(" ").take(2).joinToString(" ")
                                canvas1.drawText("👤 $shortTeacher", colLeft + 6f, rowY + 34f, paint)
                            }
                        }
                    }
                }

                paint.color = Color.parseColor("#CBD5E1")
                paint.strokeWidth = 0.75f
                paint.style = Paint.Style.STROKE
                canvas1.drawLine(tableLeft, rowBottom, tableLeft + totalTableWidth, rowBottom, paint)
                paint.style = Paint.Style.FILL
                rowY = rowBottom
            }

            // Vertical borders
            paint.color = Color.parseColor("#CBD5E1")
            paint.strokeWidth = 0.75f
            paint.style = Paint.Style.STROKE
            canvas1.drawRoundRect(tableLeft, tableTop, tableLeft + totalTableWidth, rowY, 4f, 4f, paint)
            canvas1.drawLine(tableLeft + timeColWidth, tableTop, tableLeft + timeColWidth, rowY, paint)
            for (i in 1..4) {
                val lineX = tableLeft + timeColWidth + (i * dayColWidth)
                canvas1.drawLine(lineX, tableTop, lineX, rowY, paint)
            }
            paint.style = Paint.Style.FILL

            // Footer Page 1
            paint.color = Color.parseColor("#64748B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.textAlign = Paint.Align.LEFT
            canvas1.drawText("Documento oficial generado por la Plataforma Escolaris • Colegio Hogar Madre de Dios", tableLeft, pageHeight - 18f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas1.drawText("Página $currentPage de $totalPages (Horario Escolar)", tableLeft + totalTableWidth, pageHeight - 18f, paint)
            pdfDocument.finishPage(page1)

            // ==========================================
            // DIRECTORIO DOCENTE INSTITUCIONAL (PAGINADO)
            // ==========================================
            val dTableLeft = 28f
            val dTableWidth = pageWidth - 56f // 786f
            val dHeaderH = 22f
            val dRowH = 36f

            val c1W = 210f // Docente
            val c2W = 190f // Asignatura
            val c3W = 196f // Correo
            val c4W = 190f // Atención

            teachersChunked.forEachIndexed { chunkIdx, teacherChunk ->
                currentPage++
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
                val dirPage = pdfDocument.startPage(pageInfo)
                val canvasDir = dirPage.canvas

                // Header Banner
                paint.color = Color.parseColor("#1E3A8A")
                canvasDir.drawRect(0f, 0f, pageWidth.toFloat(), 66f, paint)

                paint.color = Color.WHITE
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 15f
                paint.textAlign = Paint.Align.LEFT
                canvasDir.drawText("DIRECTORIO DEL PERSONAL DOCENTE Y ATENCIÓN A PADRES", 28f, 24f, paint)

                paint.textSize = 11.5f
                paint.color = Color.parseColor("#FEF08A")
                val dirSubHeader = if (teachersChunked.size > 1) {
                    "PLANTA DOCENTE OFICIAL • GRADO SÉPTIMO 2026-2027 (PARTE ${chunkIdx + 1} DE ${teachersChunked.size})"
                } else {
                    "PLANTA DOCENTE OFICIAL • GRADO SÉPTIMO 2026-2027"
                }
                canvasDir.drawText(dirSubHeader, 28f, 42f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.5f
                paint.color = Color.parseColor("#E0E7FF")
                canvasDir.drawText("Colegio Hogar Madre de Dios  |  Emisión: $currentDateStr  |  Plataforma Escolaris", 28f, 57f, paint)

                // Table Header
                val dTableTop = 78f
                paint.color = Color.parseColor("#2563EB")
                canvasDir.drawRoundRect(dTableLeft, dTableTop, dTableLeft + dTableWidth, dTableTop + dHeaderH, 4f, 4f, paint)

                paint.color = Color.WHITE
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.LEFT
                canvasDir.drawText("DOCENTE / CARGO INSTITUCIONAL", dTableLeft + 8f, dTableTop + 15f, paint)
                canvasDir.drawText("MATERIA EN 7° GRADO", dTableLeft + c1W + 8f, dTableTop + 15f, paint)
                canvasDir.drawText("CORREO ELECTRÓNICO", dTableLeft + c1W + c2W + 8f, dTableTop + 15f, paint)
                canvasDir.drawText("ATENCIÓN PRESENCIAL A PADRES", dTableLeft + c1W + c2W + c3W + 8f, dTableTop + 15f, paint)

                var dRowY = dTableTop + dHeaderH
                teacherChunk.forEachIndexed { index, t ->
                    val dRowBottom = dRowY + dRowH
                    paint.color = if (index % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                    canvasDir.drawRect(dTableLeft, dRowY, dTableLeft + dTableWidth, dRowBottom, paint)

                    paint.color = Color.parseColor("#0F172A")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 8.5f
                    canvasDir.drawText("${t.avatarEmoji} ${t.name}", dTableLeft + 8f, dRowY + 16f, paint)
                    paint.color = Color.parseColor("#64748B")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 7.5f
                    val shortRole = if (t.roleOrGrade.length > 36) t.roleOrGrade.take(34) + "…" else t.roleOrGrade
                    canvasDir.drawText(shortRole, dTableLeft + 26f, dRowY + 28f, paint)

                    paint.color = Color.parseColor("#1E293B")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 8f
                    val subjectText = t.subjectsIn7th.ifBlank { t.subject }
                    canvasDir.drawText(subjectText, dTableLeft + c1W + 8f, dRowY + 22f, paint)

                    paint.color = Color.parseColor("#2563EB")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 8f
                    canvasDir.drawText(t.email ?: "institucional@escolaris.edu.co", dTableLeft + c1W + c2W + 8f, dRowY + 22f, paint)

                    paint.color = Color.parseColor("#166534")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 8f
                    canvasDir.drawText("🗓️ ${t.attentionDay}", dTableLeft + c1W + c2W + c3W + 8f, dRowY + 16f, paint)
                    paint.color = Color.parseColor("#334155")
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.textSize = 7.5f
                    canvasDir.drawText(t.attentionHours, dTableLeft + c1W + c2W + c3W + 24f, dRowY + 28f, paint)

                    paint.color = Color.parseColor("#E2E8F0")
                    paint.strokeWidth = 0.5f
                    paint.style = Paint.Style.STROKE
                    canvasDir.drawLine(dTableLeft, dRowBottom, dTableLeft + dTableWidth, dRowBottom, paint)
                    paint.style = Paint.Style.FILL

                    dRowY = dRowBottom
                }

                paint.color = Color.parseColor("#CBD5E1")
                paint.strokeWidth = 0.75f
                paint.style = Paint.Style.STROKE
                canvasDir.drawRoundRect(dTableLeft, dTableTop, dTableLeft + dTableWidth, dRowY, 4f, 4f, paint)
                paint.style = Paint.Style.FILL

                // Footer Directory Page
                paint.color = Color.parseColor("#64748B")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.LEFT
                canvasDir.drawText("Documento oficial generado por la Plataforma Escolaris • Colegio Hogar Madre de Dios", dTableLeft, pageHeight - 18f, paint)
                paint.textAlign = Paint.Align.RIGHT
                val dirFooterLabel = if (teachersChunked.size > 1) "Directorio Docente (${chunkIdx + 1}/${teachersChunked.size})" else "Directorio Docente"
                canvasDir.drawText("Página $currentPage de $totalPages ($dirFooterLabel)", dTableLeft + dTableWidth, pageHeight - 18f, paint)
                pdfDocument.finishPage(dirPage)
            }

            // ==========================================
            // CALENDARIO ESCOLAR & EVENTOS (PAGINADO)
            // ==========================================
            val evTableTop = 78f
            val evColWidth = 385f
            val evColGap = 16f
            val evLeft1 = 28f
            val evLeft2 = evLeft1 + evColWidth + evColGap
            val evDateFormat = SimpleDateFormat("dd MMM", Locale("es", "ES"))

            eventsChunked.forEachIndexed { cChunkIdx, pageEventChunk ->
                currentPage++
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPage).create()
                val calPage = pdfDocument.startPage(pageInfo)
                val canvasCal = calPage.canvas

                // Header Banner
                paint.color = Color.parseColor("#1E3A8A")
                canvasCal.drawRect(0f, 0f, pageWidth.toFloat(), 66f, paint)

                paint.color = Color.WHITE
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 15f
                paint.textAlign = Paint.Align.LEFT
                canvasCal.drawText("CALENDARIO INSTITUCIONAL Y FECHAS IMPORTANTES 2026-2027", 28f, 24f, paint)

                paint.textSize = 11.5f
                paint.color = Color.parseColor("#FEF08A")
                val calSubHeader = if (eventsChunked.size > 1) {
                    "REUNIONES DE PADRES • EVALUACIONES • ENTREGAS DE BOLETINES • CLAUSURAS (PARTE ${cChunkIdx + 1} DE ${eventsChunked.size})"
                } else {
                    "REUNIONES DE PADRES • EVALUACIONES • ENTREGAS DE BOLETINES • CLAUSURAS"
                }
                canvasCal.drawText(calSubHeader, 28f, 42f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.5f
                paint.color = Color.parseColor("#E0E7FF")
                canvasCal.drawText("Colegio Hogar Madre de Dios  |  Emisión: $currentDateStr  |  Plataforma Escolaris", 28f, 57f, paint)

                val halfSize = (pageEventChunk.size + 1) / 2
                val col1Events = pageEventChunk.take(halfSize)
                val col2Events = pageEventChunk.drop(halfSize)

                fun drawEventsColumn(colEvents: List<SchoolEventEntity>, colLeft: Float, headerTitle: String) {
                    if (colEvents.isEmpty()) return

                    paint.color = Color.parseColor("#2563EB")
                    canvasCal.drawRoundRect(colLeft, evTableTop, colLeft + evColWidth, evTableTop + 20f, 4f, 4f, paint)
                    paint.color = Color.WHITE
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.textSize = 8.5f
                    paint.textAlign = Paint.Align.LEFT
                    canvasCal.drawText(headerTitle, colLeft + 8f, evTableTop + 14f, paint)

                    var cY = evTableTop + 20f
                    colEvents.forEachIndexed { idx, ev ->
                        val cBottom = cY + 28f
                        paint.color = if (idx % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                        canvasCal.drawRect(colLeft, cY, colLeft + evColWidth, cBottom, paint)

                        val dateStr = if (ev.eventDateMillis > 0L) evDateFormat.format(Date(ev.eventDateMillis)).uppercase() else "FECHA"
                        paint.color = Color.parseColor("#1D4ED8")
                        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        paint.textSize = 8f
                        canvasCal.drawText("🗓️ $dateStr", colLeft + 6f, cY + 18f, paint)

                        paint.color = Color.parseColor("#0F172A")
                        paint.textSize = 8f
                        val cleanTitle = if (ev.title.length > 38) ev.title.take(36) + "…" else ev.title
                        canvasCal.drawText(cleanTitle, colLeft + 64f, cY + 18f, paint)

                        if (ev.eventTime.isNotBlank() || ev.description.isNotBlank()) {
                            val detailStr = listOf(ev.eventTime, ev.description).filter { it.isNotBlank() }.joinToString(" • ")
                            val cleanDetail = if (detailStr.length > 20) detailStr.take(18) + "…" else detailStr
                            paint.color = Color.parseColor("#64748B")
                            paint.textSize = 7f
                            paint.textAlign = Paint.Align.RIGHT
                            canvasCal.drawText(cleanDetail, colLeft + evColWidth - 6f, cY + 18f, paint)
                            paint.textAlign = Paint.Align.LEFT
                        }

                        paint.color = Color.parseColor("#E2E8F0")
                        paint.strokeWidth = 0.5f
                        paint.style = Paint.Style.STROKE
                        canvasCal.drawLine(colLeft, cBottom, colLeft + evColWidth, cBottom, paint)
                        paint.style = Paint.Style.FILL

                        cY = cBottom
                    }

                    paint.color = Color.parseColor("#CBD5E1")
                    paint.strokeWidth = 0.75f
                    paint.style = Paint.Style.STROKE
                    canvasCal.drawRoundRect(colLeft, evTableTop, colLeft + evColWidth, cY, 4f, 4f, paint)
                    paint.style = Paint.Style.FILL
                }

                val col1Title = if (eventsChunked.size > 1) "PROGRAMACIÓN INSTITUCIONAL (PARTE ${cChunkIdx * 2 + 1})" else "PROGRAMACIÓN INSTITUCIONAL (PARTE 1)"
                val col2Title = if (eventsChunked.size > 1) "PROGRAMACIÓN INSTITUCIONAL (PARTE ${cChunkIdx * 2 + 2})" else "PROGRAMACIÓN INSTITUCIONAL (PARTE 2)"

                drawEventsColumn(col1Events, evLeft1, col1Title)
                drawEventsColumn(col2Events, evLeft2, col2Title)

                // Footer Calendar Page
                paint.color = Color.parseColor("#64748B")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8.5f
                paint.textAlign = Paint.Align.LEFT
                canvasCal.drawText("Documento oficial generado por la Plataforma Escolaris • Colegio Hogar Madre de Dios", 28f, pageHeight - 18f, paint)
                paint.textAlign = Paint.Align.RIGHT
                val calFooterLabel = if (eventsChunked.size > 1) "Calendario Institucional (${cChunkIdx + 1}/${eventsChunked.size})" else "Calendario Institucional"
                canvasCal.drawText("Página $currentPage de $totalPages ($calFooterLabel)", pageWidth - 28f, pageHeight - 18f, paint)
                pdfDocument.finishPage(calPage)
            }

            // Save and Share
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()

            val file = File(reportsDir, "Resumen_ejecutivo_grado_Septimo_2026-2027.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()

            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Resumen Ejecutivo de Curso - Grado Séptimo 2026-2027")
                putExtra(Intent.EXTRA_TEXT, "Adjunto el Resumen Oficial del Grado Séptimo: Horario de Clases, Directorio Docente y Calendario de Eventos (Colegio Hogar Madre de Dios).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Descargar / Compartir Resumen del Curso PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            Toast.makeText(context, "Resumen Ejecutivo del Curso PDF generado exitosamente 📥", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar Resumen del Curso: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

