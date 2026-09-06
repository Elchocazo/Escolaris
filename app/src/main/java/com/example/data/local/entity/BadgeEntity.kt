package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_badges")
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val badgeKey: String, // e.g. "FIRST_DAY", "FIRST_GRADE_5", "FAILED_EXAM_RESILIENCE", "FLAG_RAISING", "STREAK_7_DAYS", "GOLDEN_PUNCTUALITY", "MATH_WIZARD", "PEER_HELPER", "SPECIAL_RECOGNITION"
    val title: String, // e.g. "Primer Día de Clases", "Primer Examen en 5.0", "Examen Perdido (¡A Remontar!)", "Izé Bandera"
    val description: String,
    val emoji: String, // "🎒", "⭐", "💔", "🇨🇴", "🔥", "⏰", "📐", "🤝", "🎖️"
    val category: String = "ACADEMIC", // "ACADEMIC", "CIVIC", "EFFORT", "COMMUNITY", "SPECIAL"
    val unlockedAtMillis: Long = System.currentTimeMillis(),
    val unlockedByTeacher: String = "Prof. Moz (SuperAdmin)",
    val teacherNote: String = "",
    val clayColorHex: Long = 0xFF6366F1, // Claymorphism gradient accent
    val xpReward: Int = 100,
    val creditReward: Int = 50,
    val isSecretBadge: Boolean = false,
    val photoUri: String? = null
)
