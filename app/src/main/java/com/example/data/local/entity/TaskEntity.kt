package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String = "student_1",
    val title: String,
    val subject: String,
    val description: String,
    val dueDateMillis: Long,
    val priority: String = "MEDIA", // "ALTA", "MEDIA", "BAJA"
    val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "COMPLETED"
    val rewardCredits: Int = 30,
    val submissionNotes: String? = null,
    val reminderEnabled: Boolean = true
)
