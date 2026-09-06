package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "redemptions")
data class RedemptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rewardId: Long,
    val rewardTitle: String,
    val studentId: String,
    val studentName: String,
    val costCredits: Int,
    val redeemedAtMillis: Long = System.currentTimeMillis(),
    val redemptionCode: String,
    val status: String = "ACTIVO" // "ACTIVO", "VALIDADO"
)
