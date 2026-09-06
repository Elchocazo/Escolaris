package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lastName: String = "",
    val email: String,
    val role: String, // "STUDENT", "PARENT", "TEACHER"
    val age: Int = 0,
    val studentCode: String = "", // e.g. "ESC-7K9M2P" (inmutable code for student pairing)
    val avatarColorHex: Long = 0xFF2563EB,
    val avatarInitials: String = "ES",
    val gradeSection: String = "10° Grado",
    val streakDays: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val credits: Int = 0,
    val parentIncentiveCredits: Int = 100, // Credits a parent can grant to their child
    val linkedStudentId: String? = null,
    val teacherCode: String = "", // e.g. "DOC-8M2P10" (unique class code for teacher classroom)
    val linkedTeacherCode: String? = null, // Student's enrolled teacher classroom code
    val bio: String = "Miembro de la comunidad Escolaris 🚀",
    val avatarEmoji: String = "🎓",
    val photoUri: String? = null,
    val bannerGradientIndex: Int = 0,
    val phoneNumber: String = ""
) {
    val formattedName: String
        get() = com.example.domain.validation.ValidationUtils.formatProperNoun(name)

    val formattedFullName: String
        get() {
            val full = if (lastName.isNotBlank()) "$name $lastName" else name
            return com.example.domain.validation.ValidationUtils.formatProperNoun(full)
        }
}

