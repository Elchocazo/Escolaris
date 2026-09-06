package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val costCredits: Int,
    val category: String = "ACADÉMICO", // "ACADÉMICO", "BENEFICIOS", "COLEGIAL", "ESPECIAL"
    val iconKey: String = "STAR", // "STAR", "GRADE", "PASS", "BADGE", "CUP", "BOOK"
    val stockAvailable: Int = 10,
    val createdByRole: String = "TEACHER",
    val teacherName: String = "Prof. Elena Ramos"
)
