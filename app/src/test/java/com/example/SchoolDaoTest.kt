package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.EscolarisDatabase
import com.example.data.local.dao.SchoolDao
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.FeedPostEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PostCommentEntity
import com.example.data.local.entity.RedemptionEntity
import com.example.data.local.entity.RewardEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.TardyRecordEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SchoolDaoTest {

    private lateinit var db: EscolarisDatabase
    private lateinit var dao: SchoolDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, EscolarisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.schoolDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetUser() = runBlocking {
        val user = UserEntity(
            id = "test_user",
            name = "Prueba Alumno",
            email = "alumno@colegio.edu",
            role = "STUDENT",
            gradeSection = "10° A",
            credits = 250,
            xp = 1200,
            level = 4,
            streakDays = 5,
            avatarColorHex = 0xFF2563EB,
            avatarInitials = "PA"
        )
        dao.insertUser(user)

        val retrieved = dao.getUserDirect("test_user")
        assertNotNull(retrieved)
        assertEquals("Prueba Alumno", retrieved?.name)
        assertEquals(250, retrieved?.credits)
    }

    @Test
    fun insertAndRetrieveTask() = runBlocking {
        val task = TaskEntity(
            studentId = "test_user",
            title = "Resolver Tarea de Álgebra",
            subject = "Matemáticas",
            description = "Ejercicios 1 al 10",
            dueDateMillis = System.currentTimeMillis() + 86400000L,
            priority = "ALTA",
            rewardCredits = 40
        )
        val id = dao.insertTask(task)

        val tasks = dao.getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("Resolver Tarea de Álgebra", tasks[0].title)

        dao.updateTaskStatus(id, "COMPLETED")
        val updatedTasks = dao.getAllTasks().first()
        assertEquals("COMPLETED", updatedTasks[0].status)
    }

    @Test
    fun insertAndRetrieveFeedPostWithComments() = runBlocking {
        val post = FeedPostEntity(
            authorId = "test_user",
            authorName = "Prueba Alumno",
            authorRole = "STUDENT",
            authorAvatarColorHex = 0xFF2563EB,
            title = "🚨 ¡Estoy atrasado! - ¿Me mandas los apuntes?",
            content = "¿Alguien me pasa la pizarra de Biología?",
            postType = "LATE_HELP_REQUEST",
            subject = "Biología"
        )
        val postId = dao.insertFeedPost(post)

        val posts = dao.getAllFeedPosts().first()
        assertEquals(1, posts.size)
        assertEquals("LATE_HELP_REQUEST", posts[0].postType)

        val comment = PostCommentEntity(
            postId = postId,
            authorId = "student_2",
            authorName = "Mateo",
            authorRole = "STUDENT",
            content = "Aquí tienes la foto de la pizarra",
            attachmentDescription = "Foto_Pizarra.jpg"
        )
        dao.insertComment(comment)
        dao.incrementCommentCount(postId)

        val comments = dao.getCommentsForPost(postId).first()
        assertEquals(1, comments.size)
        assertEquals("Foto_Pizarra.jpg", comments[0].attachmentDescription)
    }

    @Test
    fun insertAndGradeExam() = runBlocking {
        val exam = ExamEntity(
            studentId = "student_1",
            title = "Examen de Física",
            subject = "Física",
            examDateMillis = System.currentTimeMillis(),
            classroom = "Aula 204",
            topics = "Cinemática",
            grade = null,
            maxGrade = 5.0,
            isGraded = false
        )
        val id = dao.insertExam(exam)

        dao.updateExamGrade(id, 4.8, "¡Excelente procedimiento!")
        val exams = dao.getAllExams().first()
        assertEquals(1, exams.size)
        assertTrue(exams[0].isGraded)
        assertEquals(4.8, exams[0].grade ?: 0.0, 0.01)
    }

    @Test
    fun insertAndUnlockBadge() = runBlocking {
        val badge = BadgeEntity(
            studentId = "student_1",
            badgeKey = "FLAG_RAISING",
            title = "Izé Bandera",
            description = "Honor Patrio",
            emoji = "🇨🇴",
            category = "CÍVICA",
            unlockedAtMillis = System.currentTimeMillis(),
            unlockedByTeacher = "Prof. Moz",
            teacherNote = "Honor cívico",
            clayColorHex = 0xFFEAB308,
            xpReward = 150,
            creditReward = 80
        )
        dao.insertBadge(badge)

        val badges = dao.getBadgesForStudent("student_1").first()
        assertEquals(1, badges.size)
        assertEquals("FLAG_RAISING", badges[0].badgeKey)
    }

    @Test
    fun insertAndRecordTardy() = runBlocking {
        val tardy = TardyRecordEntity(
            studentId = "student_1",
            studentName = "Sofía Martínez",
            gradeSection = "10° A",
            dateMillis = System.currentTimeMillis(),
            arrivalTime = "07:45 AM",
            delayMinutes = 15,
            subject = "Matemáticas",
            reason = "Cita médica",
            status = "PENDIENTE",
            notifiedParents = true,
            teacherObservation = "Pendiente soporte"
        )
        val id = dao.insertTardyRecord(tardy)

        dao.updateTardyRecordStatus(id, "JUSTIFICADO", "Certificado recibido")
        val records = dao.getAllTardyRecords().first()
        assertEquals(1, records.size)
        assertEquals("JUSTIFICADO", records[0].status)
    }
}
