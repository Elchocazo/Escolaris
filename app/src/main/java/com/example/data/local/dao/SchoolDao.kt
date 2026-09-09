package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.FeedPostEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ParentObligationEntity
import com.example.data.local.entity.PenaltyEntity
import com.example.data.local.entity.PostCommentEntity
import com.example.data.local.entity.RedemptionEntity
import com.example.data.local.entity.RewardEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.SchoolEventEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TardyRecordEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {

    // USERS
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersDirect(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserDirect(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE UPPER(studentCode) = UPPER(:code) AND role = 'STUDENT' LIMIT 1")
    suspend fun getUserByStudentCode(code: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'STUDENT' ORDER BY xp DESC, credits DESC")
    fun getLeaderboardStudents(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM users WHERE id NOT IN (:validIds)")
    suspend fun pruneUsers(validIds: List<String>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET credits = credits + :amount, xp = xp + :xpAmount WHERE id = :userId")
    suspend fun addCreditsAndXp(userId: String, amount: Int, xpAmount: Int)

    @Query("UPDATE users SET credits = credits - :amount WHERE id = :userId")
    suspend fun deductCredits(userId: String, amount: Int)

    @Query("UPDATE users SET parentIncentiveCredits = parentIncentiveCredits - :amount WHERE id = :parentId")
    suspend fun deductParentIncentiveCredits(parentId: String, amount: Int)

    @Query("UPDATE users SET credits = 100, xp = 50, level = 1, streakDays = 1 WHERE role = 'STUDENT'")
    suspend fun resetStudentPointsToDefault()

    @Query("UPDATE users SET credits = 100, parentIncentiveCredits = 100, xp = 50, level = 1, streakDays = 1 WHERE role = 'PARENT'")
    suspend fun resetParentPointsToDefault()

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    // FEED POSTS
    @Query("SELECT * FROM feed_posts ORDER BY timestamp DESC")
    fun getAllFeedPosts(): Flow<List<FeedPostEntity>>

    @Query("SELECT * FROM feed_posts ORDER BY timestamp DESC")
    suspend fun getAllFeedPostsDirect(): List<FeedPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedPost(post: FeedPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedPosts(posts: List<FeedPostEntity>)

    @Query("UPDATE feed_posts SET likesCount = likesCount + :delta, isLikedByMe = :isLiked WHERE id = :postId")
    suspend fun updatePostLike(postId: Long, delta: Int, isLiked: Boolean)

    @Query("UPDATE feed_posts SET resolvedStatus = 1 WHERE id = :postId")
    suspend fun markLateHelpResolved(postId: Long)

    @Query("DELETE FROM feed_posts WHERE id = :postId")
    suspend fun deleteFeedPost(postId: Long)

    @Query("DELETE FROM feed_posts")
    suspend fun deleteAllFeedPosts()

    @Query("DELETE FROM feed_posts WHERE id NOT IN (:validIds)")
    suspend fun deleteFeedPostsNotIn(validIds: List<Long>)

    // COMMENTS
    @Query("SELECT * FROM post_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<PostCommentEntity>>

    @Query("SELECT * FROM post_comments ORDER BY timestamp ASC")
    suspend fun getAllCommentsDirect(): List<PostCommentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: PostCommentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<PostCommentEntity>)

    @Query("DELETE FROM post_comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: Long)

    @Query("DELETE FROM post_comments WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: Long)

    @Query("DELETE FROM post_comments")
    suspend fun deleteAllComments()

    @Query("DELETE FROM post_comments WHERE id NOT IN (:validIds)")
    suspend fun deleteCommentsNotIn(validIds: List<Long>)

    @Query("UPDATE feed_posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentCount(postId: Long)

    @Query("UPDATE feed_posts SET commentsCount = CASE WHEN commentsCount > 0 THEN commentsCount - 1 ELSE 0 END WHERE id = :postId")
    suspend fun decrementCommentCount(postId: Long)

    // TASKS / HOMEWORK
    @Query("SELECT * FROM tasks ORDER BY dueDateMillis ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksDirect(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getTaskByFirestoreId(firestoreId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskByIdDirect(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE studentId = :studentId ORDER BY dueDateMillis ASC")
    fun getTasksForStudent(studentId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, completed = :completed WHERE id = :taskId")
    suspend fun updateTaskStatusWithCompleted(taskId: Long, status: String, completed: Boolean)

    @Query("UPDATE tasks SET status = :status, completed = CASE WHEN :status = 'COMPLETED' THEN 1 ELSE 0 END WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: String)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks WHERE firestoreId = :firestoreId")
    suspend fun deleteTaskByFirestoreId(firestoreId: String)

    @Query("DELETE FROM tasks WHERE firestoreId NOT IN (:validFirestoreIds) AND firestoreId != ''")
    suspend fun pruneTasks(validFirestoreIds: List<String>)

    // EXAMS
    @Query("SELECT * FROM exams ORDER BY examDateMillis DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE studentId = :studentId ORDER BY examDateMillis DESC")
    fun getExamsForStudent(studentId: String): Flow<List<ExamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<ExamEntity>)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Query("UPDATE exams SET grade = :grade, isGraded = 1, teacherFeedback = :feedback WHERE id = :examId")
    suspend fun updateExamGrade(examId: Long, grade: Double, feedback: String?)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)

    // SCHEDULES
    @Query("SELECT * FROM schedules ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getSchedulesForDay(dayOfWeek: Int): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    // REWARDS
    @Query("SELECT * FROM rewards ORDER BY costCredits ASC")
    fun getAllRewards(): Flow<List<RewardEntity>>

    @Query("SELECT COUNT(*) FROM rewards")
    suspend fun getRewardsCount(): Int

    @Query("SELECT * FROM rewards")
    suspend fun getAllRewardsList(): List<RewardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<RewardEntity>)

    @Update
    suspend fun updateReward(reward: RewardEntity)

    @Delete
    suspend fun deleteReward(reward: RewardEntity)

    // REDEMPTIONS
    @Query("SELECT * FROM redemptions WHERE studentId = :studentId ORDER BY redeemedAtMillis DESC")
    fun getRedemptionsForStudent(studentId: String): Flow<List<RedemptionEntity>>

    @Query("SELECT * FROM redemptions ORDER BY redeemedAtMillis DESC")
    fun getAllRedemptions(): Flow<List<RedemptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedemption(redemption: RedemptionEntity): Long

    @Query("UPDATE redemptions SET status = 'VALIDADO' WHERE id = :redemptionId")
    suspend fun validateRedemption(redemptionId: Long)

    @Query("UPDATE redemptions SET status = :status WHERE id = :redemptionId")
    suspend fun updateRedemptionStatus(redemptionId: Long, status: String)

    @Query("SELECT * FROM redemptions WHERE id = :redemptionId LIMIT 1")
    suspend fun getRedemptionById(redemptionId: Long): RedemptionEntity?

    // NOTIFICATIONS
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationAsRead(notificationId: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // TARDY / LATE ARRIVAL RECORDS
    @Query("SELECT * FROM tardy_records ORDER BY dateMillis DESC")
    fun getAllTardyRecords(): Flow<List<TardyRecordEntity>>

    @Query("SELECT * FROM tardy_records")
    suspend fun getAllTardyRecordsDirect(): List<TardyRecordEntity>

    @Query("SELECT * FROM tardy_records WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getTardyRecordByFirestoreId(firestoreId: String): TardyRecordEntity?

    @Query("SELECT * FROM tardy_records WHERE id = :id LIMIT 1")
    suspend fun getTardyRecordByIdDirect(id: Long): TardyRecordEntity?

    @Query("SELECT * FROM tardy_records WHERE studentId = :studentId ORDER BY dateMillis DESC")
    fun getTardyRecordsForStudent(studentId: String): Flow<List<TardyRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTardyRecord(record: TardyRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTardyRecords(records: List<TardyRecordEntity>)

    @Query("UPDATE tardy_records SET status = :status, teacherObservation = :teacherObservation WHERE id = :id")
    suspend fun updateTardyRecordStatus(id: Long, status: String, teacherObservation: String)

    @Delete
    suspend fun deleteTardyRecord(record: TardyRecordEntity)

    @Query("DELETE FROM tardy_records WHERE id = :id")
    suspend fun deleteTardyRecordById(id: Long)

    @Query("DELETE FROM tardy_records WHERE firestoreId = :firestoreId")
    suspend fun deleteTardyRecordByFirestoreId(firestoreId: String)

    @Query("DELETE FROM tardy_records WHERE firestoreId NOT IN (:validFirestoreIds) AND firestoreId != ''")
    suspend fun pruneTardies(validFirestoreIds: List<String>)

    // STUDENT BADGES / INSIGNIAS
    @Query("SELECT * FROM student_badges ORDER BY unlockedAtMillis DESC")
    fun getAllBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM student_badges ORDER BY unlockedAtMillis DESC")
    suspend fun getAllBadgesDirect(): List<BadgeEntity>

    @Query("SELECT * FROM student_badges WHERE studentId = :studentId AND badgeKey = :badgeKey LIMIT 1")
    suspend fun getBadgeByStudentAndKey(studentId: String, badgeKey: String): BadgeEntity?

    @Query("SELECT * FROM student_badges WHERE studentId = :studentId ORDER BY unlockedAtMillis DESC")
    fun getBadgesForStudent(studentId: String): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>)

    @Query("DELETE FROM student_badges WHERE (studentId || '_' || badgeKey) NOT IN (:validKeys)")
    suspend fun pruneBadges(validKeys: List<String>)

    @Delete
    suspend fun deleteBadge(badge: BadgeEntity)

    @Query("DELETE FROM student_badges WHERE id = :id")
    suspend fun deleteBadgeById(id: Long)

    @Query("DELETE FROM student_badges")
    suspend fun clearAllBadges()

    // SUBJECTS / ASIGNATURAS ESCOLARES
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    suspend fun getAllSubjectsList(): List<SubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    // SCHOOL EVENTS / EVENTOS CALENDARIO CON HORA
    @Query("SELECT * FROM school_events ORDER BY eventDateMillis ASC")
    fun getAllSchoolEvents(): Flow<List<SchoolEventEntity>>

    @Query("SELECT * FROM school_events ORDER BY eventDateMillis ASC")
    suspend fun getAllSchoolEventsList(): List<SchoolEventEntity>

    @Query("SELECT COUNT(*) FROM school_events")
    suspend fun getSchoolEventsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolEvent(event: SchoolEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolEvents(events: List<SchoolEventEntity>)

    @Delete
    suspend fun deleteSchoolEvent(event: SchoolEventEntity)

    // PENALTIES / MULTAS Y SANCIONES DISCIPLINARIAS
    @Query("SELECT * FROM penalties ORDER BY timestamp DESC")
    fun getAllPenalties(): Flow<List<com.example.data.local.entity.PenaltyEntity>>

    @Query("SELECT * FROM penalties WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getPenaltiesForStudent(studentId: String): Flow<List<com.example.data.local.entity.PenaltyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPenalty(penalty: com.example.data.local.entity.PenaltyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPenalties(penalties: List<com.example.data.local.entity.PenaltyEntity>)

    @Update
    suspend fun updatePenalty(penalty: com.example.data.local.entity.PenaltyEntity)

    @Delete
    suspend fun deletePenalty(penalty: com.example.data.local.entity.PenaltyEntity)

    @Query("DELETE FROM penalties")
    suspend fun clearAllPenalties()

    // PARENT OBLIGATIONS / DEBERES Y PENSIONES DE PADRES DE FAMILIA
    @Query("SELECT * FROM parent_obligations ORDER BY dueDateMillis ASC")
    fun getAllParentObligations(): Flow<List<ParentObligationEntity>>

    @Query("SELECT * FROM parent_obligations ORDER BY dueDateMillis ASC")
    suspend fun getAllParentObligationsDirect(): List<ParentObligationEntity>

    @Query("SELECT * FROM parent_obligations WHERE parentId = 'ALL' OR parentId = :parentId OR studentId = :studentId ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getObligationsForParent(parentId: String, studentId: String): Flow<List<ParentObligationEntity>>

    @Query("SELECT COUNT(*) FROM parent_obligations")
    suspend fun getParentObligationsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParentObligation(obligation: ParentObligationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParentObligations(obligations: List<ParentObligationEntity>)

    @Update
    suspend fun updateParentObligation(obligation: ParentObligationEntity)

    @Query("UPDATE parent_obligations SET isCompleted = :isCompleted, completedAtMillis = :completedAtMillis, completedByParentName = :parentName WHERE id = :id")
    suspend fun markObligationCompleted(id: Long, isCompleted: Boolean, completedAtMillis: Long?, parentName: String)

    @Query("UPDATE parent_obligations SET lastReminderSentMillis = :sentMillis WHERE id = :id")
    suspend fun updateObligationLastReminder(id: Long, sentMillis: Long)

    @Delete
    suspend fun deleteParentObligation(obligation: ParentObligationEntity)

    @Query("DELETE FROM parent_obligations WHERE id = :id")
    suspend fun deleteParentObligationById(id: Long)

    @Query("DELETE FROM parent_obligations WHERE id NOT IN (:validIds)")
    suspend fun pruneParentObligations(validIds: List<Long>)
}
