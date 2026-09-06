package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_posts")
data class FeedPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: String,
    val authorName: String,
    val authorRole: String,
    val authorAvatarColorHex: Long,
    val title: String,
    val content: String,
    val postType: String, // "ANNOUNCEMENT", "EVENT", "HOMEWORK_ALERT", "EXAM_ALERT", "LATE_HELP_REQUEST"
    val subject: String = "General",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val targetDateMillis: Long? = null,
    val attachmentsJson: String = "", // JSON list or description of attached notes/images
    val commentsCount: Int = 0,
    val resolvedStatus: Boolean = false // e.g. If someone already provided notes for help request
)
