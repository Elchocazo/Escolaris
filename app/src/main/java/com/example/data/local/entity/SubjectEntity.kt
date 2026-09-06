package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "📚",
    val teacherName: String = "Prof. Titular",
    val classroom: String = "Aula Principal",
    val colorHex: Long = 0xFF2563EB,
    val description: String = ""
)
