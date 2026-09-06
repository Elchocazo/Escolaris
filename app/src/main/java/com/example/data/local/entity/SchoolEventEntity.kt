package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_events")
data class SchoolEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "INSTITUTIONAL",
    val eventDateMillis: Long,
    val endDateMillis: Long = eventDateMillis, // Soporte para eventos continuos/rangos de fechas
    val eventTime: String = "", // Hora del evento solo cuando esté especificada (e.g. "5:00 PM")
    val location: String = "",
    val description: String = "",
    val creatorName: String = "Administración",
    val colorHex: Long = 0xFF8B5CF6
)
