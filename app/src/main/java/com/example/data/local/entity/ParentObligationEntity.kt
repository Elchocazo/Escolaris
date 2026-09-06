package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_obligations")
data class ParentObligationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentId: String = "ALL", // "ALL" for all parents or specific parent user id
    val studentId: String = "ALL", // "ALL" or specific student id
    val title: String, // e.g. "Pago de Pensión Mensual - Octubre"
    val description: String, // e.g. "A partir de octubre la pensión se cancela dentro de los 5 primeros días del mes."
    val category: String = "PENSION", // "PENSION", "ACADEMIC", "DOCUMENTATION", "EVENT", "GENERAL"
    val month: String = "Octubre", // e.g. "Octubre", "Noviembre"
    val dueDayOfMonth: Int = 5, // Timely payment deadline: 5th day of month
    val dueDateMillis: Long = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000),
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val completedByParentName: String = "",
    val rewardBadgeKey: String = "PARENT_PENSION_OCTUBRE",
    val rewardBadgeTitle: String = "Pago Oportuno de Pensión",
    val rewardBadgeEmoji: String = "💳",
    val rewardCredits: Int = 100, // Escolaris credits earned for the child
    val rewardXp: Int = 150,
    val whatsappMessage: String = "¡Hola estimado acudiente! 👋 Les recordamos que a partir de octubre la pensión escolar se cancela dentro de los 5 primeros días del mes. ¡Cumple a tiempo para ganar la insignia de Pago Oportuno y créditos para tu hijo/a! ⭐",
    val createdByTeacher: String = "Tesorería Escolar & Docente Titular",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val lastReminderSentMillis: Long = 0L
)
