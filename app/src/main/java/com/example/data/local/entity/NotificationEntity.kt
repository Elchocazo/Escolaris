package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "HOMEWORK", "EXAM", "LATE_HELP", "REWARD", "EVENT", "STREAK"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetScreen: String? = null
)
