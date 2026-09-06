package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tardy_records")
data class TardyRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val studentName: String,
    val gradeSection: String = "10° Grado - Sección A",
    val dateMillis: Long = System.currentTimeMillis(),
    val arrivalTime: String = "07:45 AM",
    val delayMinutes: Int = 15,
    val subject: String = "Matemáticas Avanzadas",
    val reason: String = "Tráfico vehicular en vía principal",
    val status: String = "PENDIENTE", // "JUSTIFICADO", "SIN_JUSTIFICAR", "PENDIENTE"
    val notifiedParents: Boolean = true,
    val teacherObservation: String = "",
    val penaltyCredits: Int = 0
)
