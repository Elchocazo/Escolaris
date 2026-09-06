package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "penalties")
data class PenaltyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val studentName: String,
    val reason: String,
    val category: String, // "DISCIPLINE", "CELLPHONE", "UNIFORM", "HOMEWORK", "FACILITY", "LATE", "OTHER"
    val pointsDeducted: Int,
    val teacherName: String,
    val observation: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "APLICADA", // "APLICADA" | "REVOCADA"
    val notifiedParents: Boolean = true
)
