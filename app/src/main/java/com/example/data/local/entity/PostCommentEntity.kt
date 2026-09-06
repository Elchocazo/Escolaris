package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "post_comments")
data class PostCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorId: String,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentDescription: String? = null
)
