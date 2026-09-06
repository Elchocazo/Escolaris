package com.example.data.repository

import com.example.data.local.dao.SchoolDao
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

interface ISchoolRepository {
    // USERS
    val allUsers: Flow<List<UserEntity>>
    val leaderboardStudents: Flow<List<UserEntity>>
    fun getUser(userId: String): Flow<UserEntity?>
    suspend fun getUserDirect(userId: String): UserEntity?
    suspend fun getUserByEmailDirect(email: String): UserEntity?
    suspend fun getUserByStudentCode(code: String): UserEntity?
    suspend fun updateUser(user: UserEntity)
    suspend fun insertUser(user: UserEntity)
    suspend fun deleteUser(user: UserEntity)
    suspend fun deleteUserById(userId: String)
    suspend fun addCreditsAndXp(userId: String, credits: Int, xp: Int)
    suspend fun grantParentIncentiveToStudent(parentId: String, studentId: String, amount: Int, reason: String)
    suspend fun resetAllUsersPointsToDefault()

    // FEED POSTS & COMMENTS
    val allFeedPosts: Flow<List<FeedPostEntity>>
    suspend fun getAllFeedPostsDirect(): List<FeedPostEntity>
    suspend fun createFeedPost(post: FeedPostEntity): Long
    suspend fun insertFeedPosts(posts: List<FeedPostEntity>)
    suspend fun togglePostLike(postId: Long, delta: Int, isLiked: Boolean)
    suspend fun markHelpRequestResolved(postId: Long)
    suspend fun deletePost(postId: Long)
    fun getCommentsForPost(postId: Long): Flow<List<PostCommentEntity>>
    suspend fun getAllCommentsDirect(): List<PostCommentEntity>
    suspend fun addComment(comment: PostCommentEntity): Long
    suspend fun insertComments(comments: List<PostCommentEntity>)

    // TASKS
    val allTasks: Flow<List<TaskEntity>>
    fun getTasksForStudent(studentId: String): Flow<List<TaskEntity>>
    suspend fun insertTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun updateTaskStatus(taskId: Long, status: String)
    suspend fun deleteTask(task: TaskEntity)

    // EXAMS
    val allExams: Flow<List<ExamEntity>>
    fun getExamsForStudent(studentId: String): Flow<List<ExamEntity>>
    suspend fun insertExam(exam: ExamEntity): Long
    suspend fun updateExam(exam: ExamEntity)
    suspend fun updateExamGrade(examId: Long, grade: Double, feedback: String?)
    suspend fun deleteExam(exam: ExamEntity)

    // SCHEDULES
    val allSchedules: Flow<List<ScheduleEntity>>
    fun getSchedulesForDay(dayOfWeek: Int): Flow<List<ScheduleEntity>>
    suspend fun insertSchedule(schedule: ScheduleEntity): Long
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    // REWARDS & REDEMPTIONS
    val allRewards: Flow<List<RewardEntity>>
    suspend fun insertReward(reward: RewardEntity): Long
    suspend fun updateReward(reward: RewardEntity)
    suspend fun deleteReward(reward: RewardEntity)
    val allRedemptions: Flow<List<RedemptionEntity>>
    fun getRedemptionsForStudent(studentId: String): Flow<List<RedemptionEntity>>
    suspend fun redeemReward(reward: RewardEntity, student: UserEntity): Result<RedemptionEntity>
    suspend fun approveRedemption(redemptionId: Long)
    suspend fun rejectRedemption(redemptionId: Long): Boolean
    suspend fun validateRedemption(redemptionId: Long)

    // NOTIFICATIONS
    val allNotifications: Flow<List<NotificationEntity>>
    suspend fun insertNotification(notification: NotificationEntity): Long
    suspend fun markNotificationAsRead(id: Long)
    suspend fun markAllNotificationsAsRead()
    suspend fun clearAllNotifications()

    // TARDY RECORDS
    val allTardyRecords: Flow<List<TardyRecordEntity>>
    fun getTardyRecordsForStudent(studentId: String): Flow<List<TardyRecordEntity>>
    suspend fun insertTardyRecord(record: TardyRecordEntity): Long
    suspend fun updateTardyRecordStatus(id: Long, status: String, observation: String)
    suspend fun deleteTardyRecord(record: TardyRecordEntity)

    // BADGES
    val allBadges: Flow<List<BadgeEntity>>
    fun getBadgesForStudent(studentId: String): Flow<List<BadgeEntity>>
    suspend fun insertBadge(badge: BadgeEntity): Long
    suspend fun deleteBadge(badge: BadgeEntity)
    suspend fun deleteBadgeById(id: Long)

    // SUBJECTS
    val allSubjects: Flow<List<com.example.data.local.entity.SubjectEntity>>
    suspend fun insertSubject(subject: com.example.data.local.entity.SubjectEntity): Long
    suspend fun deleteSubject(subject: com.example.data.local.entity.SubjectEntity)
    suspend fun seedInitialSubjectsIfEmpty()
    suspend fun syncOfficialFacultySubjects()

    // SCHOOL EVENTS
    val allSchoolEvents: Flow<List<com.example.data.local.entity.SchoolEventEntity>>
    suspend fun insertSchoolEvent(event: com.example.data.local.entity.SchoolEventEntity): Long
    suspend fun deleteSchoolEvent(event: com.example.data.local.entity.SchoolEventEntity)

    // PENALTIES / MULTAS Y SANCIONES
    val allPenalties: Flow<List<com.example.data.local.entity.PenaltyEntity>>
    fun getPenaltiesForStudent(studentId: String): Flow<List<com.example.data.local.entity.PenaltyEntity>>
    suspend fun insertPenalty(penalty: com.example.data.local.entity.PenaltyEntity): Long
    suspend fun updatePenalty(penalty: com.example.data.local.entity.PenaltyEntity)
    suspend fun deletePenalty(penalty: com.example.data.local.entity.PenaltyEntity)
    suspend fun deductCredits(userId: String, amount: Int)

    // PARENT OBLIGATIONS & PENSIONES
    val allParentObligations: Flow<List<ParentObligationEntity>>
    fun getObligationsForParent(parentId: String, studentId: String): Flow<List<ParentObligationEntity>>
    suspend fun insertParentObligation(obligation: ParentObligationEntity): Long
    suspend fun updateParentObligation(obligation: ParentObligationEntity)
    suspend fun markObligationCompleted(id: Long, isCompleted: Boolean, completedAtMillis: Long?, parentName: String)
    suspend fun updateObligationLastReminder(id: Long, sentMillis: Long)
    suspend fun deleteParentObligation(obligation: ParentObligationEntity)
    suspend fun deleteParentObligationById(id: Long)
}

class SchoolRepository(private val dao: SchoolDao) : ISchoolRepository {

    // USERS
    override val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
    override val leaderboardStudents: Flow<List<UserEntity>> = dao.getLeaderboardStudents()

    override fun getUser(userId: String): Flow<UserEntity?> = dao.getUserById(userId)
    override suspend fun getUserDirect(userId: String): UserEntity? = dao.getUserDirect(userId)
    override suspend fun getUserByEmailDirect(email: String): UserEntity? = dao.getUserByEmail(email.trim().lowercase())
    override suspend fun getUserByStudentCode(code: String): UserEntity? = dao.getUserByStudentCode(code)
    override suspend fun updateUser(user: UserEntity) = dao.updateUser(user)
    override suspend fun insertUser(user: UserEntity) = dao.insertUser(user)
    override suspend fun deleteUser(user: UserEntity) = dao.deleteUser(user)
    override suspend fun deleteUserById(userId: String) = dao.deleteUserById(userId)
    override suspend fun addCreditsAndXp(userId: String, credits: Int, xp: Int) = dao.addCreditsAndXp(userId, credits, xp)

    override suspend fun grantParentIncentiveToStudent(
        parentId: String,
        studentId: String,
        amount: Int,
        reason: String
    ) {
        val parent = dao.getUserDirect(parentId)
        if (parent != null) {
            val newParentCredits = (parent.credits - amount).coerceAtLeast(0)
            val newParentIncentive = (parent.parentIncentiveCredits - amount).coerceAtLeast(0)
            dao.updateUser(parent.copy(credits = newParentCredits, parentIncentiveCredits = newParentIncentive))
        }
        val student = dao.getUserDirect(studentId)
        if (student != null) {
            val newStudentCredits = student.credits + amount
            val newStudentXp = student.xp + (amount * 2)
            dao.updateUser(student.copy(credits = newStudentCredits, xp = newStudentXp))
        }
        dao.insertNotification(
            NotificationEntity(
                title = "🎁 ¡Tus Padres te han cedido Escolaris!",
                message = "Tus acudientes te han transferido +$amount 🪙 Escolaris a tu saldo escolar. Motivo: $reason",
                type = "REWARD",
                isRead = false,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun resetAllUsersPointsToDefault() = withContext(Dispatchers.IO) {
        dao.resetStudentPointsToDefault()
        dao.resetParentPointsToDefault()
    }

    // FEED POSTS & COMMENTS
    override val allFeedPosts: Flow<List<FeedPostEntity>> = dao.getAllFeedPosts()
    override suspend fun getAllFeedPostsDirect(): List<FeedPostEntity> = dao.getAllFeedPostsDirect()

    override suspend fun createFeedPost(post: FeedPostEntity): Long = dao.insertFeedPost(post)
    override suspend fun insertFeedPosts(posts: List<FeedPostEntity>) = dao.insertFeedPosts(posts)
    override suspend fun togglePostLike(postId: Long, delta: Int, isLiked: Boolean) = dao.updatePostLike(postId, delta, isLiked)
    override suspend fun markHelpRequestResolved(postId: Long) = dao.markLateHelpResolved(postId)
    override suspend fun deletePost(postId: Long) = dao.deleteFeedPost(postId)

    override fun getCommentsForPost(postId: Long): Flow<List<PostCommentEntity>> = dao.getCommentsForPost(postId)
    override suspend fun getAllCommentsDirect(): List<PostCommentEntity> = dao.getAllCommentsDirect()
    override suspend fun addComment(comment: PostCommentEntity): Long {
        val id = dao.insertComment(comment)
        dao.incrementCommentCount(comment.postId)
        return id
    }
    override suspend fun insertComments(comments: List<PostCommentEntity>) = dao.insertComments(comments)

    // TASKS
    override val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    override fun getTasksForStudent(studentId: String): Flow<List<TaskEntity>> = dao.getTasksForStudent(studentId)

    override suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    override suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    override suspend fun updateTaskStatus(taskId: Long, status: String) = dao.updateTaskStatus(taskId, status)
    override suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)

    // EXAMS
    override val allExams: Flow<List<ExamEntity>> = dao.getAllExams()
    override fun getExamsForStudent(studentId: String): Flow<List<ExamEntity>> = dao.getExamsForStudent(studentId)

    override suspend fun insertExam(exam: ExamEntity): Long = dao.insertExam(exam)
    override suspend fun updateExam(exam: ExamEntity) = dao.updateExam(exam)
    override suspend fun updateExamGrade(examId: Long, grade: Double, feedback: String?) = dao.updateExamGrade(examId, grade, feedback)
    override suspend fun deleteExam(exam: ExamEntity) = dao.deleteExam(exam)

    // SCHEDULES
    override val allSchedules: Flow<List<ScheduleEntity>> = dao.getAllSchedules()
    override fun getSchedulesForDay(dayOfWeek: Int): Flow<List<ScheduleEntity>> = dao.getSchedulesForDay(dayOfWeek)

    override suspend fun insertSchedule(schedule: ScheduleEntity): Long = dao.insertSchedule(schedule)
    override suspend fun deleteSchedule(schedule: ScheduleEntity) = dao.deleteSchedule(schedule)

    // REWARDS & REDEMPTIONS
    override val allRewards: Flow<List<RewardEntity>> = dao.getAllRewards()

    override suspend fun insertReward(reward: RewardEntity): Long = dao.insertReward(reward)
    override suspend fun updateReward(reward: RewardEntity) = dao.updateReward(reward)
    override suspend fun deleteReward(reward: RewardEntity) = dao.deleteReward(reward)

    override val allRedemptions: Flow<List<RedemptionEntity>> = dao.getAllRedemptions()
    override fun getRedemptionsForStudent(studentId: String): Flow<List<RedemptionEntity>> = dao.getRedemptionsForStudent(studentId)

    override suspend fun redeemReward(
        reward: RewardEntity,
        student: UserEntity
    ): Result<RedemptionEntity> {
        if (student.credits < reward.costCredits) {
            return Result.failure(IllegalStateException("Saldo insuficiente: Tienes ${student.credits} créditos y requieres ${reward.costCredits}."))
        }

        // Deduct credits atomically
        dao.deductCredits(student.id, reward.costCredits)

        val code = "ESC-" + UUID.randomUUID().toString().take(6).uppercase()
        val redemption = RedemptionEntity(
            rewardId = reward.id,
            rewardTitle = reward.title,
            studentId = student.id,
            studentName = student.name,
            costCredits = reward.costCredits,
            redemptionCode = code,
            status = "PENDING_APPROVAL"
        )
        val id = dao.insertRedemption(redemption)
        return Result.success(redemption.copy(id = id))
    }

    override suspend fun approveRedemption(redemptionId: Long) {
        dao.updateRedemptionStatus(redemptionId, "APROBADO")
    }

    override suspend fun rejectRedemption(redemptionId: Long): Boolean {
        val redemption = dao.getRedemptionById(redemptionId) ?: return false
        // Reembolsar créditos al estudiante
        dao.addCreditsAndXp(redemption.studentId, redemption.costCredits, 0)
        dao.updateRedemptionStatus(redemptionId, "RECHAZADO")
        return true
    }

    override suspend fun validateRedemption(redemptionId: Long) {
        dao.updateRedemptionStatus(redemptionId, "UTILIZADO")
    }

    // NOTIFICATIONS
    override val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    override suspend fun insertNotification(notification: NotificationEntity): Long = dao.insertNotification(notification)
    override suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)
    override suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
    override suspend fun clearAllNotifications() = dao.clearAllNotifications()

    // TARDY RECORDS
    override val allTardyRecords: Flow<List<TardyRecordEntity>> = dao.getAllTardyRecords()
    override fun getTardyRecordsForStudent(studentId: String): Flow<List<TardyRecordEntity>> = dao.getTardyRecordsForStudent(studentId)

    override suspend fun insertTardyRecord(record: TardyRecordEntity): Long = dao.insertTardyRecord(record)
    override suspend fun updateTardyRecordStatus(id: Long, status: String, observation: String) =
        dao.updateTardyRecordStatus(id, status, observation)
    override suspend fun deleteTardyRecord(record: TardyRecordEntity) = dao.deleteTardyRecord(record)

    // BADGES
    override val allBadges: Flow<List<BadgeEntity>> = dao.getAllBadges()
    override fun getBadgesForStudent(studentId: String): Flow<List<BadgeEntity>> = dao.getBadgesForStudent(studentId)

    override suspend fun insertBadge(badge: BadgeEntity): Long = dao.insertBadge(badge)
    override suspend fun deleteBadge(badge: BadgeEntity) = dao.deleteBadge(badge)
    override suspend fun deleteBadgeById(id: Long) = dao.deleteBadgeById(id)

    // SUBJECTS
    override val allSubjects: Flow<List<com.example.data.local.entity.SubjectEntity>> = dao.getAllSubjects()
    override suspend fun insertSubject(subject: com.example.data.local.entity.SubjectEntity): Long = dao.insertSubject(subject)
    override suspend fun deleteSubject(subject: com.example.data.local.entity.SubjectEntity) = dao.deleteSubject(subject)

    override suspend fun seedInitialSubjectsIfEmpty() {
        val officialSubjects = listOf(
            com.example.data.local.entity.SubjectEntity(name = "Matemáticas", emoji = "📐", teacherName = "Manuel Muñoz", classroom = "", colorHex = 0xFF2563EB),
            com.example.data.local.entity.SubjectEntity(name = "Tecnología e Informática", emoji = "💻", teacherName = "Manuel Muñoz", classroom = "", colorHex = 0xFF3B82F6),
            com.example.data.local.entity.SubjectEntity(name = "Español", emoji = "📖", teacherName = "Katherine Castro", classroom = "", colorHex = 0xFFE11D74),
            com.example.data.local.entity.SubjectEntity(name = "Ciencias Sociales", emoji = "🌍", teacherName = "Ibón Ocampo", classroom = "", colorHex = 0xFFF59E0B),
            com.example.data.local.entity.SubjectEntity(name = "Inglés", emoji = "🇬🇧", teacherName = "Ángela Rendón", classroom = "", colorHex = 0xFF8B5CF6),
            com.example.data.local.entity.SubjectEntity(name = "Educación Física", emoji = "⚽", teacherName = "José Benavides", classroom = "", colorHex = 0xFFEF4444),
            com.example.data.local.entity.SubjectEntity(name = "Física", emoji = "⚡", teacherName = "Víctor Toro", classroom = "", colorHex = 0xFF6366F1),
            com.example.data.local.entity.SubjectEntity(name = "Razonamiento Matemático", emoji = "🔢", teacherName = "Víctor Toro", classroom = "", colorHex = 0xFF4F46E5),
            com.example.data.local.entity.SubjectEntity(name = "Geometría", emoji = "📐", teacherName = "Carlos Erazo", classroom = "", colorHex = 0xFF059669),
            com.example.data.local.entity.SubjectEntity(name = "Biología", emoji = "🔬", teacherName = "Anna Fulí", classroom = "", colorHex = 0xFF10B981),
            com.example.data.local.entity.SubjectEntity(name = "Química", emoji = "🧪", teacherName = "Anna Fulí", classroom = "", colorHex = 0xFF14B8A6),
            com.example.data.local.entity.SubjectEntity(name = "Música", emoji = "🎵", teacherName = "Danilo Daza", classroom = "", colorHex = 0xFF8B5CF6),
            com.example.data.local.entity.SubjectEntity(name = "Religión", emoji = "🕊️", teacherName = "Mónica García", classroom = "", colorHex = 0xFFF59E0B),
            com.example.data.local.entity.SubjectEntity(name = "Estética", emoji = "🎨", teacherName = "Sirley Palta", classroom = "", colorHex = 0xFFEC4899),
            com.example.data.local.entity.SubjectEntity(name = "Cátedra emocional", emoji = "💛", teacherName = "Irnalda Tintinago", classroom = "", colorHex = 0xFFF59E0B),
            com.example.data.local.entity.SubjectEntity(name = "Ética", emoji = "🤝", teacherName = "Irnalda Tintinago", classroom = "", colorHex = 0xFF10B981),
            com.example.data.local.entity.SubjectEntity(name = "Dirección de grupo", emoji = "👨‍🏫", teacherName = "Manuel Muñoz", classroom = "", colorHex = 0xFF1E40AF)
        )
        if (dao.getSubjectCount() == 0) {
            dao.insertSubjects(officialSubjects)
        } else {
            syncOfficialFacultySubjects()
        }
    }

    override suspend fun syncOfficialFacultySubjects() {
        val obsoleteCombined = setOf(
            "Matemáticas, Tecnología e Informática",
            "Español & Literatura",
            "Ciencias Sociales & Historia",
            "Sociales",
            "Física & Razonamiento Matemático",
            "Estética & Arte",
            "Artes & Dibujo",
            "Cátedra Emocional y Ética",
            "Ética y Valores",
            "Biología & Ciencias Naturales",
            "Ciencias Naturales",
            "Sistemas"
        )
        val existing = dao.getAllSubjectsList()
        for (sub in existing) {
            if (obsoleteCombined.contains(sub.name)) {
                dao.deleteSubject(sub)
            }
        }

        val officialSubjects = listOf(
            com.example.data.local.entity.SubjectEntity(name = "Matemáticas", emoji = "📐", teacherName = "Manuel Muñoz", classroom = "", colorHex = 0xFF2563EB),
            com.example.data.local.entity.SubjectEntity(name = "Tecnología e Informática", emoji = "💻", teacherName = "Manuel Muñoz", classroom = "", colorHex = 0xFF3B82F6),
            com.example.data.local.entity.SubjectEntity(name = "Español", emoji = "📖", teacherName = "Katherine Castro", classroom = "", colorHex = 0xFFE11D74),
            com.example.data.local.entity.SubjectEntity(name = "Ciencias Sociales", emoji = "🌍", teacherName = "Ibón Ocampo", classroom = "", colorHex = 0xFFF59E0B),
            com.example.data.local.entity.SubjectEntity(name = "Inglés", emoji = "🇬🇧", teacherName = "Ángela Rendón", classroom = "", colorHex = 0xFF8B5CF6),
            com.example.data.local.entity.SubjectEntity(name = "Educación Física", emoji = "⚽", teacherName = "José Benavides", classroom = "", colorHex = 0xFFEF4444),
            com.example.data.local.entity.SubjectEntity(name = "Física", emoji = "⚡", teacherName = "Víctor Toro", classroom = "", colorHex = 0xFF6366F1),
            com.example.data.local.entity.SubjectEntity(name = "Razonamiento Matemático", emoji = "🔢", teacherName = "Víctor Toro", classroom = "", colorHex = 0xFF4F46E5),
            com.example.data.local.entity.SubjectEntity(name = "Geometría", emoji = "📐", teacherName = "Carlos Erazo", classroom = "", colorHex = 0xFF059669),
            com.example.data.local.entity.SubjectEntity(name = "Biología", emoji = "🔬", teacherName = "Anna Fulí", classroom = "", colorHex = 0xFF10B981),
            com.example.data.local.entity.SubjectEntity(name = "Química", emoji = "🧪", teacherName = "Anna Fulí", classroom = "", colorHex = 0xFF14B8A6),
            com.example.data.local.entity.SubjectEntity(name = "Música", emoji = "🎵", teacherName = "Danilo Daza", classroom = "", colorHex = 0xFF8B5CF6),
            com.example.data.local.entity.SubjectEntity(name = "Religión", emoji = "🕊️", teacherName = "Mónica García", classroom = "", colorHex = 0xFFF59E0B),
            com.example.data.local.entity.SubjectEntity(name = "Estética", emoji = "🎨", teacherName = "Sirley Palta", classroom = "", colorHex = 0xFFEC4899),
            com.example.data.local.entity.SubjectEntity(name = "Cátedra emocional", emoji = "💛", teacherName = "Irnalda Tintinago", classroom = "", colorHex = 0xFFF59E0B),
            com.example.data.local.entity.SubjectEntity(name = "Ética", emoji = "🤝", teacherName = "Irnalda Tintinago", classroom = "", colorHex = 0xFF10B981),
            com.example.data.local.entity.SubjectEntity(name = "Dirección de grupo", emoji = "👨‍🏫", teacherName = "Manuel Muñoz", classroom = "", colorHex = 0xFF1E40AF)
        )

        val remainingNames = dao.getAllSubjectsList().map { it.name }.toSet()
        for (offSub in officialSubjects) {
            if (!remainingNames.contains(offSub.name)) {
                dao.insertSubject(offSub)
            } else {
                val current = dao.getAllSubjectsList().find { it.name == offSub.name }
                if (current != null && (current.teacherName != offSub.teacherName || current.emoji != offSub.emoji)) {
                    dao.insertSubject(current.copy(teacherName = offSub.teacherName, emoji = offSub.emoji, classroom = ""))
                }
            }
        }
    }

    // SCHOOL EVENTS
    override val allSchoolEvents: Flow<List<com.example.data.local.entity.SchoolEventEntity>> = dao.getAllSchoolEvents()
    override suspend fun insertSchoolEvent(event: com.example.data.local.entity.SchoolEventEntity): Long = dao.insertSchoolEvent(event)
    override suspend fun deleteSchoolEvent(event: com.example.data.local.entity.SchoolEventEntity) = dao.deleteSchoolEvent(event)

    // PENALTIES / MULTAS Y SANCIONES
    override val allPenalties: Flow<List<com.example.data.local.entity.PenaltyEntity>> = dao.getAllPenalties()
    override fun getPenaltiesForStudent(studentId: String): Flow<List<com.example.data.local.entity.PenaltyEntity>> = dao.getPenaltiesForStudent(studentId)
    override suspend fun insertPenalty(penalty: com.example.data.local.entity.PenaltyEntity): Long = dao.insertPenalty(penalty)
    override suspend fun updatePenalty(penalty: com.example.data.local.entity.PenaltyEntity) = dao.updatePenalty(penalty)
    override suspend fun deletePenalty(penalty: com.example.data.local.entity.PenaltyEntity) = dao.deletePenalty(penalty)
    override suspend fun deductCredits(userId: String, amount: Int) = dao.deductCredits(userId, amount)

    // PARENT OBLIGATIONS & PENSIONES
    override val allParentObligations: Flow<List<ParentObligationEntity>> = dao.getAllParentObligations()
    override fun getObligationsForParent(parentId: String, studentId: String): Flow<List<ParentObligationEntity>> = dao.getObligationsForParent(parentId, studentId)
    override suspend fun insertParentObligation(obligation: ParentObligationEntity): Long = dao.insertParentObligation(obligation)
    override suspend fun updateParentObligation(obligation: ParentObligationEntity) = dao.updateParentObligation(obligation)
    override suspend fun markObligationCompleted(id: Long, isCompleted: Boolean, completedAtMillis: Long?, parentName: String) = dao.markObligationCompleted(id, isCompleted, completedAtMillis, parentName)
    override suspend fun updateObligationLastReminder(id: Long, sentMillis: Long) = dao.updateObligationLastReminder(id, sentMillis)
    override suspend fun deleteParentObligation(obligation: ParentObligationEntity) = dao.deleteParentObligation(obligation)
    override suspend fun deleteParentObligationById(id: Long) = dao.deleteParentObligationById(id)
}
