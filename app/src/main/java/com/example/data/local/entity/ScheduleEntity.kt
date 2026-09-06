package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String = "student_1",
    val dayOfWeek: Int, // 1 = Lunes, 2 = Martes, 3 = Miércoles, 4 = Jueves, 5 = Viernes, 6 = Sábado, 7 = Domingo
    val startTime: String, // "08:00"
    val endTime: String, // "09:30"
    val subject: String,
    val classroomOrLocation: String = "Aula A-12",
    val teacherOrTutor: String = "Prof. Elena Ramos",
    val colorHex: Long = 0xFF2563EB,
    val isSelfStudySession: Boolean = false, // false = Horario escolar, true = Sesión de estudio personal
    val notes: String = ""
)
