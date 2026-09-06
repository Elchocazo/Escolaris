package com.example.data.local

import android.content.Context
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.FeedPostEntity
import com.example.data.local.entity.ParentObligationEntity
import com.example.data.local.entity.PostCommentEntity
import com.example.data.local.entity.UserEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gestor de respaldo local persistente fuera del ciclo de vida de SQLite Room.
 * Garantiza que ante cualquier actualización de versión o reinicio de la aplicación,
 * ningún dato de ningún usuario (Docentes, Estudiantes, Acudientes, Administradores),
 * publicaciones, medallas de honor ni deberes de padres se pierdan jamás.
 */
object EscolarisBackupManager {

    private const val PREFS_NAME = "escolaris_persistent_backup_v1"
    private const val KEY_USERS = "backup_users"
    private const val KEY_FEED_POSTS = "backup_feed_posts"
    private const val KEY_POST_COMMENTS = "backup_post_comments"
    private const val KEY_BADGES = "backup_badges"
    private const val KEY_PARENT_OBLIGATIONS = "backup_parent_obligations"

    // ==========================================
    // USUARIOS (TODOS LOS ROLES)
    // ==========================================
    fun saveUsersBackup(context: Context, users: List<UserEntity>) {
        if (users.isEmpty()) return
        try {
            val jsonArray = JSONArray()
            for (user in users) {
                val obj = JSONObject().apply {
                    put("id", user.id)
                    put("name", user.name)
                    put("lastName", user.lastName)
                    put("email", user.email)
                    put("role", user.role)
                    put("age", user.age)
                    put("studentCode", user.studentCode)
                    put("teacherCode", user.teacherCode)
                    put("avatarColorHex", user.avatarColorHex)
                    put("avatarInitials", user.avatarInitials)
                    put("gradeSection", user.gradeSection)
                    put("streakDays", user.streakDays)
                    put("xp", user.xp)
                    put("level", user.level)
                    put("credits", user.credits)
                    put("parentIncentiveCredits", user.parentIncentiveCredits)
                    put("linkedStudentId", user.linkedStudentId ?: "")
                    put("bio", user.bio)
                    put("avatarEmoji", user.avatarEmoji)
                    put("bannerGradientIndex", user.bannerGradientIndex)
                    put("photoUri", user.photoUri ?: "")
                    put("phoneNumber", user.phoneNumber)
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_USERS, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restoreUsersBackup(context: Context): List<UserEntity> {
        val result = mutableListOf<UserEntity>()
        try {
            val rawJson = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USERS, null) ?: return emptyList()
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val photoUri = obj.optString("photoUri").takeIf { it.isNotBlank() }
                val linkedStudentId = obj.optString("linkedStudentId").takeIf { it.isNotBlank() }

                result.add(
                    UserEntity(
                        id = obj.getString("id"),
                        name = obj.optString("name", "Usuario"),
                        lastName = obj.optString("lastName", ""),
                        email = obj.optString("email", ""),
                        role = obj.optString("role", "STUDENT"),
                        age = obj.optInt("age", 0),
                        studentCode = obj.optString("studentCode", ""),
                        teacherCode = obj.optString("teacherCode", ""),
                        avatarColorHex = obj.optLong("avatarColorHex", 0xFF2563EB),
                        avatarInitials = obj.optString("avatarInitials", "ES"),
                        gradeSection = obj.optString("gradeSection", "10° Grado"),
                        streakDays = obj.optInt("streakDays", 1),
                        xp = obj.optInt("xp", 50),
                        level = obj.optInt("level", 1),
                        credits = obj.optInt("credits", 100),
                        parentIncentiveCredits = obj.optInt("parentIncentiveCredits", 100),
                        linkedStudentId = linkedStudentId,
                        bio = obj.optString("bio", ""),
                        avatarEmoji = obj.optString("avatarEmoji", "🎓"),
                        bannerGradientIndex = obj.optInt("bannerGradientIndex", 0),
                        photoUri = photoUri,
                        phoneNumber = obj.optString("phoneNumber", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // ==========================================
    // MURO SOCIAL (FEED POSTS)
    // ==========================================
    fun saveFeedPostsBackup(context: Context, posts: List<FeedPostEntity>) {
        if (posts.isEmpty()) return
        try {
            val jsonArray = JSONArray()
            for (post in posts) {
                val obj = JSONObject().apply {
                    put("id", post.id)
                    put("authorId", post.authorId)
                    put("authorName", post.authorName)
                    put("authorRole", post.authorRole)
                    put("authorAvatarColorHex", post.authorAvatarColorHex)
                    put("title", post.title)
                    put("content", post.content)
                    put("postType", post.postType)
                    put("subject", post.subject)
                    put("timestamp", post.timestamp)
                    put("likesCount", post.likesCount)
                    put("isLikedByMe", post.isLikedByMe)
                    put("targetDateMillis", post.targetDateMillis ?: -1L)
                    put("attachmentsJson", post.attachmentsJson)
                    put("commentsCount", post.commentsCount)
                    put("resolvedStatus", post.resolvedStatus)
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_FEED_POSTS, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restoreFeedPostsBackup(context: Context): List<FeedPostEntity> {
        val result = mutableListOf<FeedPostEntity>()
        try {
            val rawJson = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_FEED_POSTS, null) ?: return emptyList()
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val targetDateMillis = obj.optLong("targetDateMillis", -1L).takeIf { it > 0 }

                result.add(
                    FeedPostEntity(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        authorId = obj.optString("authorId", ""),
                        authorName = obj.optString("authorName", "Docente"),
                        authorRole = obj.optString("authorRole", "TEACHER"),
                        authorAvatarColorHex = obj.optLong("authorAvatarColorHex", 0xFF2563EB),
                        title = obj.optString("title", ""),
                        content = obj.optString("content", ""),
                        postType = obj.optString("postType", "ANNOUNCEMENT"),
                        subject = obj.optString("subject", "General"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        likesCount = obj.optInt("likesCount", 0),
                        isLikedByMe = obj.optBoolean("isLikedByMe", false),
                        targetDateMillis = targetDateMillis,
                        attachmentsJson = obj.optString("attachmentsJson", ""),
                        commentsCount = obj.optInt("commentsCount", 0),
                        resolvedStatus = obj.optBoolean("resolvedStatus", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // ==========================================
    // COMENTARIOS DE PUBLICACIONES
    // ==========================================
    fun savePostCommentsBackup(context: Context, comments: List<PostCommentEntity>) {
        if (comments.isEmpty()) return
        try {
            val jsonArray = JSONArray()
            for (c in comments) {
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("postId", c.postId)
                    put("authorId", c.authorId)
                    put("authorName", c.authorName)
                    put("authorRole", c.authorRole)
                    put("content", c.content)
                    put("timestamp", c.timestamp)
                    put("attachmentDescription", c.attachmentDescription ?: "")
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_POST_COMMENTS, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restorePostCommentsBackup(context: Context): List<PostCommentEntity> {
        val result = mutableListOf<PostCommentEntity>()
        try {
            val rawJson = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_POST_COMMENTS, null) ?: return emptyList()
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val attachment = obj.optString("attachmentDescription").takeIf { it.isNotBlank() }
                result.add(
                    PostCommentEntity(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        postId = obj.getLong("postId"),
                        authorId = obj.optString("authorId", ""),
                        authorName = obj.optString("authorName", "Usuario"),
                        authorRole = obj.optString("authorRole", "STUDENT"),
                        content = obj.optString("content", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        attachmentDescription = attachment
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // ==========================================
    // MEDALLAS E INSIGNIAS DE HONOR
    // ==========================================
    fun saveBadgesBackup(context: Context, badges: List<BadgeEntity>) {
        if (badges.isEmpty()) return
        try {
            val jsonArray = JSONArray()
            for (b in badges) {
                val obj = JSONObject().apply {
                    put("id", b.id)
                    put("studentId", b.studentId)
                    put("badgeKey", b.badgeKey)
                    put("title", b.title)
                    put("description", b.description)
                    put("emoji", b.emoji)
                    put("category", b.category)
                    put("unlockedAtMillis", b.unlockedAtMillis)
                    put("unlockedByTeacher", b.unlockedByTeacher)
                    put("teacherNote", b.teacherNote)
                    put("clayColorHex", b.clayColorHex)
                    put("xpReward", b.xpReward)
                    put("creditReward", b.creditReward)
                    put("isSecretBadge", b.isSecretBadge)
                    put("photoUri", b.photoUri ?: "")
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_BADGES, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restoreBadgesBackup(context: Context): List<BadgeEntity> {
        val result = mutableListOf<BadgeEntity>()
        try {
            val rawJson = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BADGES, null) ?: return emptyList()
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val photoUri = obj.optString("photoUri").takeIf { it.isNotBlank() }
                result.add(
                    BadgeEntity(
                        id = obj.optLong("id", 0L),
                        studentId = obj.optString("studentId", ""),
                        badgeKey = obj.optString("badgeKey", ""),
                        title = obj.optString("title", "Insignia"),
                        description = obj.optString("description", ""),
                        emoji = obj.optString("emoji", "🎖️"),
                        category = obj.optString("category", "ACADEMIC"),
                        unlockedAtMillis = obj.optLong("unlockedAtMillis", System.currentTimeMillis()),
                        unlockedByTeacher = obj.optString("unlockedByTeacher", "Docente"),
                        teacherNote = obj.optString("teacherNote", ""),
                        clayColorHex = obj.optLong("clayColorHex", 0xFF6366F1),
                        xpReward = obj.optInt("xpReward", 100),
                        creditReward = obj.optInt("creditReward", 50),
                        isSecretBadge = obj.optBoolean("isSecretBadge", false),
                        photoUri = photoUri
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    // ==========================================
    // DEBERES DE PADRES Y PENSIONES
    // ==========================================
    fun saveParentObligationsBackup(context: Context, obligations: List<ParentObligationEntity>) {
        if (obligations.isEmpty()) return
        try {
            val jsonArray = JSONArray()
            for (o in obligations) {
                val obj = JSONObject().apply {
                    put("id", o.id)
                    put("parentId", o.parentId)
                    put("studentId", o.studentId)
                    put("title", o.title)
                    put("description", o.description)
                    put("category", o.category)
                    put("month", o.month)
                    put("dueDayOfMonth", o.dueDayOfMonth)
                    put("dueDateMillis", o.dueDateMillis)
                    put("isCompleted", o.isCompleted)
                    put("completedAtMillis", o.completedAtMillis ?: -1L)
                    put("completedByParentName", o.completedByParentName)
                    put("rewardBadgeKey", o.rewardBadgeKey)
                    put("rewardBadgeTitle", o.rewardBadgeTitle)
                    put("rewardBadgeEmoji", o.rewardBadgeEmoji)
                    put("rewardCredits", o.rewardCredits)
                    put("rewardXp", o.rewardXp)
                    put("whatsappMessage", o.whatsappMessage)
                    put("createdByTeacher", o.createdByTeacher)
                    put("createdAtMillis", o.createdAtMillis)
                    put("lastReminderSentMillis", o.lastReminderSentMillis)
                }
                jsonArray.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PARENT_OBLIGATIONS, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restoreParentObligationsBackup(context: Context): List<ParentObligationEntity> {
        val result = mutableListOf<ParentObligationEntity>()
        try {
            val rawJson = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PARENT_OBLIGATIONS, null) ?: return emptyList()
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val completedAtMillis = obj.optLong("completedAtMillis", -1L).takeIf { it > 0 }
                result.add(
                    ParentObligationEntity(
                        id = obj.optLong("id", 0L),
                        parentId = obj.optString("parentId", "ALL"),
                        studentId = obj.optString("studentId", "ALL"),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        category = obj.optString("category", "PENSION"),
                        month = obj.optString("month", "Octubre"),
                        dueDayOfMonth = obj.optInt("dueDayOfMonth", 5),
                        dueDateMillis = obj.optLong("dueDateMillis", System.currentTimeMillis()),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        completedAtMillis = completedAtMillis,
                        completedByParentName = obj.optString("completedByParentName", ""),
                        rewardBadgeKey = obj.optString("rewardBadgeKey", "PARENT_PENSION_OCTUBRE"),
                        rewardBadgeTitle = obj.optString("rewardBadgeTitle", "Pago Oportuno de Pensión"),
                        rewardBadgeEmoji = obj.optString("rewardBadgeEmoji", "💳"),
                        rewardCredits = obj.optInt("rewardCredits", 100),
                        rewardXp = obj.optInt("rewardXp", 150),
                        whatsappMessage = obj.optString("whatsappMessage", ""),
                        createdByTeacher = obj.optString("createdByTeacher", "Docente Titular"),
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                        lastReminderSentMillis = obj.optLong("lastReminderSentMillis", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
