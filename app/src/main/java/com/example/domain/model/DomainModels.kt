package com.example.domain.model

import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.FeedPostEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ParentObligationEntity
import com.example.data.local.entity.PostCommentEntity
import com.example.data.local.entity.RedemptionEntity
import com.example.data.local.entity.RewardEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.TardyRecordEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity

/**
 * Strongly-typed Enums for Domain Safety
 */
enum class UserRole(val code: String, val displayName: String, val badgeEmoji: String) {
    STUDENT("STUDENT", "Estudiante", "🎓"),
    PARENT("PARENT", "Padre / Tutor", "👨‍👩‍👧"),
    TEACHER("TEACHER", "Docente Titular & SuperAdmin", "👑");

    companion object {
        fun fromCode(code: String?): UserRole = entries.find { it.code.equals(code, ignoreCase = true) } ?: STUDENT
    }
}

enum class TaskPriority(val code: String, val displayName: String, val colorHex: Long) {
    ALTA("ALTA", "Alta", 0xFFEF4444),
    MEDIA("MEDIA", "Media", 0xFFF59E0B),
    BAJA("BAJA", "Baja", 0xFF3B82F6);

    companion object {
        fun fromCode(code: String?): TaskPriority = entries.find { it.code.equals(code, ignoreCase = true) } ?: MEDIA
    }
}

enum class TaskStatus(val code: String, val displayName: String) {
    PENDING("PENDING", "Pendiente"),
    IN_PROGRESS("IN_PROGRESS", "En progreso"),
    COMPLETED("COMPLETED", "Completada");

    companion object {
        fun fromCode(code: String?): TaskStatus = entries.find { it.code.equals(code, ignoreCase = true) } ?: PENDING
    }
}

enum class PostType(val code: String, val displayName: String, val emoji: String) {
    LATE_HELP_REQUEST("LATE_HELP_REQUEST", "Auxilio Apuntes", "🚨"),
    ANNOUNCEMENT("ANNOUNCEMENT", "Aviso General", "📢"),
    EVENT("EVENT", "Evento Escolar", "📅"),
    HOMEWORK_ALERT("HOMEWORK_ALERT", "Alerta de Tarea", "📝"),
    EXAM_ALERT("EXAM_ALERT", "Alerta de Examen", "🎯");

    companion object {
        fun fromCode(code: String?): PostType = entries.find { it.code.equals(code, ignoreCase = true) } ?: ANNOUNCEMENT
    }
}

enum class AttendanceStatus(val code: String, val displayName: String, val isApproved: Boolean) {
    JUSTIFICADO("JUSTIFICADO", "Justificado", true),
    PENDIENTE("PENDIENTE", "Pendiente de Justificante", false),
    INJUSTIFICADO("INJUSTIFICADO", "Injustificado", false);

    companion object {
        fun fromCode(code: String?): AttendanceStatus = entries.find { it.code.equals(code, ignoreCase = true) } ?: PENDIENTE
    }
}

val OFFICIAL_7TH_GRADE_SUBJECTS = listOf(
    "Biología",
    "Cátedra emocional",
    "Ciencias Sociales",
    "Dirección de grupo",
    "Educación Física",
    "Español",
    "Estética",
    "Ética",
    "Física",
    "Geometría",
    "Inglés",
    "Matemáticas",
    "Música",
    "Química",
    "Razonamiento Matemático",
    "Religión",
    "Tecnología e Informática"
)

data class SchoolRankInfo(
    val rankIndex: Int,
    val title: String,
    val minXp: Int,
    val maxXp: Int,
    val emoji: String,
    val description: String
)

val OFFICIAL_SCHOOL_RANKS = listOf(
    SchoolRankInfo(1, "Explorador Escolar", 0, 299, "🎒", "Iniciando el año escolar con entusiasmo, cuadernos listos y hábitos en formación."),
    SchoolRankInfo(2, "Estudiante Dedicado", 300, 599, "📚", "Constancia demostrada, tareas entregadas con puntualidad y cuadernos al día."),
    SchoolRankInfo(3, "Compañero Guía", 600, 899, "💡", "Solidaridad de aula, convivencia respetuosa y apoyo activo a compañeros."),
    SchoolRankInfo(4, "Líder de Clase", 900, 1199, "⭐", "Excelencia formativa, alto rendimiento académico y compromiso en proyectos."),
    SchoolRankInfo(5, "Orgullo Elisiano", 1200, Int.MAX_VALUE, "🏆", "Máxima distinción institucional por representar los valores de Madre Elisa.")
)

fun getRankForXp(xp: Int): SchoolRankInfo {
    return OFFICIAL_SCHOOL_RANKS.lastOrNull { xp >= it.minXp } ?: OFFICIAL_SCHOOL_RANKS.first()
}

/**
 * Pure Domain Models (Immutable, decoupled from Database / Framework specifics)
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val avatarColorHex: Long = 0xFF2563EB,
    val avatarInitials: String = "ES",
    val gradeSection: String = "10° Grado - Sección A",
    val streakDays: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val credits: Int = 0,
    val linkedStudentId: String? = null,
    val bio: String = "",
    val avatarEmoji: String = "🎓",
    val photoUri: String? = null,
    val bannerGradientIndex: Int = 0,
    val phoneNumber: String = ""
)

data class FeedPost(
    val id: Long,
    val authorId: String,
    val authorName: String,
    val authorRole: UserRole,
    val authorAvatarColorHex: Long,
    val title: String,
    val content: String,
    val postType: PostType,
    val subject: String,
    val timestamp: Long,
    val likesCount: Int,
    val isLikedByMe: Boolean,
    val targetDateMillis: Long?,
    val attachmentsJson: String,
    val commentsCount: Int,
    val resolvedStatus: Boolean
)

data class PostComment(
    val id: Long,
    val postId: Long,
    val authorId: String,
    val authorName: String,
    val authorRole: UserRole,
    val content: String,
    val timestamp: Long,
    val attachmentDescription: String?
)

data class SchoolTask(
    val id: Long,
    val studentId: String,
    val title: String,
    val subject: String,
    val description: String,
    val dueDateMillis: Long,
    val priority: TaskPriority,
    val status: TaskStatus,
    val rewardCredits: Int,
    val submissionNotes: String?
)

data class Exam(
    val id: Long,
    val studentId: String,
    val title: String,
    val subject: String,
    val examDateMillis: Long,
    val classroom: String,
    val topics: String,
    val grade: Double?,
    val maxGrade: Double = 5.0,
    val scannedPhotoUri: String?,
    val teacherFeedback: String?,
    val isGraded: Boolean,
    val rewardCreditsEarned: Int
)

data class ScheduleSlot(
    val id: Long,
    val studentId: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val classroomOrLocation: String,
    val teacherOrTutor: String,
    val isSelfStudySession: Boolean,
    val colorHex: Long,
    val notes: String
)

data class Reward(
    val id: Long,
    val title: String,
    val description: String,
    val costCredits: Int,
    val category: String,
    val iconKey: String,
    val stockAvailable: Int,
    val teacherName: String
)

data class Redemption(
    val id: Long,
    val rewardId: Long,
    val rewardTitle: String,
    val studentId: String,
    val studentName: String,
    val costCredits: Int,
    val redemptionCode: String,
    val status: String,
    val redeemedAtMillis: Long
)

data class TardyRecord(
    val id: Long,
    val studentId: String,
    val studentName: String,
    val gradeSection: String,
    val dateMillis: Long,
    val arrivalTime: String,
    val delayMinutes: Int,
    val subject: String,
    val reason: String,
    val status: AttendanceStatus,
    val notifiedParents: Boolean,
    val teacherObservation: String
)

data class Badge(
    val id: Long,
    val studentId: String,
    val badgeKey: String,
    val title: String,
    val description: String,
    val emoji: String,
    val category: String,
    val unlockedAtMillis: Long,
    val unlockedByTeacher: String,
    val teacherNote: String,
    val clayColorHex: Long,
    val xpReward: Int,
    val creditReward: Int
)

data class SchoolNotification(
    val id: Long,
    val title: String,
    val message: String,
    val type: String,
    val timestamp: Long,
    val isRead: Boolean,
    val targetScreen: String?
)

data class ParentObligation(
    val id: Long,
    val parentId: String,
    val studentId: String,
    val title: String,
    val description: String,
    val category: String,
    val month: String,
    val dueDayOfMonth: Int,
    val dueDateMillis: Long,
    val isCompleted: Boolean,
    val completedAtMillis: Long?,
    val completedByParentName: String,
    val rewardBadgeKey: String,
    val rewardBadgeTitle: String,
    val rewardBadgeEmoji: String,
    val rewardCredits: Int,
    val rewardXp: Int,
    val whatsappMessage: String,
    val createdByTeacher: String,
    val createdAtMillis: Long,
    val lastReminderSentMillis: Long
)

/**
 * Mapping Extensions (Entity -> Domain Model)
 */
fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    role = UserRole.fromCode(role),
    avatarColorHex = avatarColorHex,
    avatarInitials = avatarInitials,
    gradeSection = gradeSection,
    streakDays = streakDays,
    xp = xp,
    level = level,
    credits = credits,
    linkedStudentId = linkedStudentId,
    bio = bio,
    avatarEmoji = avatarEmoji,
    photoUri = photoUri,
    bannerGradientIndex = bannerGradientIndex,
    phoneNumber = phoneNumber
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    role = role.code,
    avatarColorHex = avatarColorHex,
    avatarInitials = avatarInitials,
    gradeSection = gradeSection,
    streakDays = streakDays,
    xp = xp,
    level = level,
    credits = credits,
    linkedStudentId = linkedStudentId,
    bio = bio,
    avatarEmoji = avatarEmoji,
    photoUri = photoUri,
    bannerGradientIndex = bannerGradientIndex,
    phoneNumber = phoneNumber
)

fun FeedPostEntity.toDomain(): FeedPost = FeedPost(
    id = id,
    authorId = authorId,
    authorName = authorName,
    authorRole = UserRole.fromCode(authorRole),
    authorAvatarColorHex = authorAvatarColorHex,
    title = title,
    content = content,
    postType = PostType.fromCode(postType),
    subject = subject,
    timestamp = timestamp,
    likesCount = likesCount,
    isLikedByMe = isLikedByMe,
    targetDateMillis = targetDateMillis,
    attachmentsJson = attachmentsJson,
    commentsCount = commentsCount,
    resolvedStatus = resolvedStatus
)

fun TaskEntity.toDomain(): SchoolTask = SchoolTask(
    id = id,
    studentId = studentId,
    title = title,
    subject = subject,
    description = description,
    dueDateMillis = dueDateMillis,
    priority = TaskPriority.fromCode(priority),
    status = TaskStatus.fromCode(status),
    rewardCredits = rewardCredits,
    submissionNotes = submissionNotes
)

fun ExamEntity.toDomain(): Exam = Exam(
    id = id,
    studentId = studentId,
    title = title,
    subject = subject,
    examDateMillis = examDateMillis,
    classroom = classroom,
    topics = topics,
    grade = grade,
    maxGrade = maxGrade,
    scannedPhotoUri = scannedPhotoUri,
    teacherFeedback = teacherFeedback,
    isGraded = isGraded,
    rewardCreditsEarned = rewardCreditsEarned
)

fun BadgeEntity.toDomain(): Badge = Badge(
    id = id,
    studentId = studentId,
    badgeKey = badgeKey,
    title = title,
    description = description,
    emoji = emoji,
    category = category,
    unlockedAtMillis = unlockedAtMillis,
    unlockedByTeacher = unlockedByTeacher,
    teacherNote = teacherNote,
    clayColorHex = clayColorHex,
    xpReward = xpReward,
    creditReward = creditReward
)

fun TardyRecordEntity.toDomain(): TardyRecord = TardyRecord(
    id = id,
    studentId = studentId,
    studentName = studentName,
    gradeSection = gradeSection,
    dateMillis = dateMillis,
    arrivalTime = arrivalTime,
    delayMinutes = delayMinutes,
    subject = subject,
    reason = reason,
    status = AttendanceStatus.fromCode(status),
    notifiedParents = notifiedParents,
    teacherObservation = teacherObservation
)

fun ParentObligationEntity.toDomain(): ParentObligation = ParentObligation(
    id = id,
    parentId = parentId,
    studentId = studentId,
    title = title,
    description = description,
    category = category,
    month = month,
    dueDayOfMonth = dueDayOfMonth,
    dueDateMillis = dueDateMillis,
    isCompleted = isCompleted,
    completedAtMillis = completedAtMillis,
    completedByParentName = completedByParentName,
    rewardBadgeKey = rewardBadgeKey,
    rewardBadgeTitle = rewardBadgeTitle,
    rewardBadgeEmoji = rewardBadgeEmoji,
    rewardCredits = rewardCredits,
    rewardXp = rewardXp,
    whatsappMessage = whatsappMessage,
    createdByTeacher = createdByTeacher,
    createdAtMillis = createdAtMillis,
    lastReminderSentMillis = lastReminderSentMillis
)

fun ParentObligation.toEntity(): ParentObligationEntity = ParentObligationEntity(
    id = id,
    parentId = parentId,
    studentId = studentId,
    title = title,
    description = description,
    category = category,
    month = month,
    dueDayOfMonth = dueDayOfMonth,
    dueDateMillis = dueDateMillis,
    isCompleted = isCompleted,
    completedAtMillis = completedAtMillis,
    completedByParentName = completedByParentName,
    rewardBadgeKey = rewardBadgeKey,
    rewardBadgeTitle = rewardBadgeTitle,
    rewardBadgeEmoji = rewardBadgeEmoji,
    rewardCredits = rewardCredits,
    rewardXp = rewardXp,
    whatsappMessage = whatsappMessage,
    createdByTeacher = createdByTeacher,
    createdAtMillis = createdAtMillis,
    lastReminderSentMillis = lastReminderSentMillis
)
