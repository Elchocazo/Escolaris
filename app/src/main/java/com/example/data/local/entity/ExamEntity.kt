package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String = "student_1",
    val title: String,
    val subject: String,
    val examDateMillis: Long,
    val classroom: String = "Aula 204",
    val topics: String,
    val grade: Double? = null, // e.g. 4.8 on a 1.0 to 5.0 scale
    val maxGrade: Double = 5.0, // Scale 1.0 to 5.0
    val scannedPhotoUri: String? = null,
    val teacherFeedback: String? = null,
    val isGraded: Boolean = false,
    val rewardCreditsEarned: Int = 0
)
