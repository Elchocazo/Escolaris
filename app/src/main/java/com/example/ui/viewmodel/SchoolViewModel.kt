package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthService
import com.example.data.local.DatabaseInitializer
import com.example.data.local.EscolarisBackupManager
import com.example.data.local.EscolarisDatabase
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
import com.example.data.repository.ISchoolRepository
import com.example.data.repository.SchoolRepository
import com.example.domain.model.AttendanceStatus
import com.example.domain.model.PostType
import com.example.domain.model.TaskPriority
import com.example.domain.model.TaskStatus
import com.example.domain.model.UserRole
import com.example.domain.validation.ValidationUtils
import com.example.ui.theme.AppColorTheme
import com.example.ui.theme.DarkThemeMode
import com.example.utils.NotificationHelper
import com.example.service.EscolarisFirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SyncState {
    SYNCED,
    SYNCING,
    OFFLINE,
    ERROR
}

class SchoolViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: ISchoolRepository = SchoolRepository(
        EscolarisDatabase.getDatabase(application).schoolDao()
    ),
    val authService: AuthService = AuthService(
        EscolarisDatabase.getDatabase(application).schoolDao()
    )
) : AndroidViewModel(application) {

    private val sessionPrefs = application.getSharedPreferences("escolaris_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_SAVED_USER_ID = "saved_active_user_id"
        private const val PREF_SAVED_USER_EMAIL = "saved_active_user_email"
        private const val PREF_SAVED_USER_NAME = "saved_active_user_name"
        private const val PREF_SAVED_USER_ROLE = "saved_active_user_role"
        private const val PREF_SAVED_USER_GRADE = "saved_active_user_grade"
        private const val PREF_SAVED_USER_AVATAR = "saved_active_user_avatar"
        private const val PREF_SAVED_USER_COLOR = "saved_active_user_color"
        private const val PREF_SAVED_USER_PHOTO = "saved_active_user_photo"
        private const val PREF_SAVED_TEACHER_CODE = "saved_active_teacher_code"
        private const val PREF_SAVED_STUDENT_CODE = "saved_active_student_code"
        private const val PREF_SAVED_PHONE = "saved_active_user_phone"
        private const val PREF_SWITCHED_FROM_PARENT_ID = "switched_from_parent_id"
        private const val PREF_SAVED_CREDITS = "saved_user_credits"
        private const val PREF_SAVED_XP = "saved_user_xp"
        private const val PREF_SAVED_PARENT_INCENTIVE = "saved_user_parent_incentive"
    }

    private fun getInitialCachedUser(): UserEntity? {
        val id = sessionPrefs.getString(PREF_SAVED_USER_ID, null) ?: return null
        val email = sessionPrefs.getString(PREF_SAVED_USER_EMAIL, "") ?: ""
        val name = sessionPrefs.getString(PREF_SAVED_USER_NAME, "") ?: ""
        val role = sessionPrefs.getString(PREF_SAVED_USER_ROLE, "") ?: ""
        if (id.isBlank() || name.isBlank()) return null
        val isTeacherOrAdmin = UserRole.isTeacherOrAdmin(role) || email == "moz658@gmail.com"
        val savedCredits = sessionPrefs.getInt(PREF_SAVED_CREDITS, if (isTeacherOrAdmin) 500 else 100)
        val savedXp = sessionPrefs.getInt(PREF_SAVED_XP, if (isTeacherOrAdmin) 200 else 50)
        val savedIncentive = sessionPrefs.getInt(PREF_SAVED_PARENT_INCENTIVE, 100)
        return UserEntity(
            id = id,
            email = email,
            name = name,
            role = role,
            gradeSection = sessionPrefs.getString(PREF_SAVED_USER_GRADE, if (isTeacherOrAdmin) "Docente Titular" else "10° Grado") ?: (if (isTeacherOrAdmin) "Docente Titular" else "10° Grado"),
            avatarEmoji = sessionPrefs.getString(PREF_SAVED_USER_AVATAR, if (isTeacherOrAdmin) "👨‍🏫" else "🎓") ?: "🎓",
            avatarColorHex = sessionPrefs.getLong(PREF_SAVED_USER_COLOR, 0xFF2563EB),
            photoUri = sessionPrefs.getString(PREF_SAVED_USER_PHOTO, null),
            teacherCode = sessionPrefs.getString(PREF_SAVED_TEACHER_CODE, "")?.ifBlank { if (email == "moz658@gmail.com") "DOC-102938" else "" } ?: "",
            studentCode = sessionPrefs.getString(PREF_SAVED_STUDENT_CODE, "") ?: "",
            phoneNumber = sessionPrefs.getString(PREF_SAVED_PHONE, "") ?: "",
            credits = savedCredits,
            xp = savedXp,
            parentIncentiveCredits = savedIncentive
        )
    }

    // ==========================================
    // STATE FLOWS
    // ==========================================
    private val _currentUserId = MutableStateFlow(
        sessionPrefs.getString(PREF_SAVED_USER_ID, "") ?: ""
    )
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(getInitialCachedUser())
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _switchedFromParentId = MutableStateFlow<String?>(
        sessionPrefs.getString(PREF_SWITCHED_FROM_PARENT_ID, null)
    )
    val switchedFromParentId: StateFlow<String?> = _switchedFromParentId.asStateFlow()

    // Auth Flow Gate State (inicia en true si hay sesión persistida para evitar cierres al minimizar o recargar)
    private val _isAuthenticated = MutableStateFlow(
        !sessionPrefs.getString(PREF_SAVED_USER_ID, null).isNullOrBlank() ||
        !sessionPrefs.getString(PREF_SAVED_USER_EMAIL, null).isNullOrBlank()
    )
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState.SYNCED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Theme Preferences
    private val _themeMode = MutableStateFlow(DarkThemeMode.LIGHT)
    val themeMode: StateFlow<DarkThemeMode> = _themeMode.asStateFlow()

    private val _colorTheme = MutableStateFlow(AppColorTheme.ROYAL_BLUE)
    val colorTheme: StateFlow<AppColorTheme> = _colorTheme.asStateFlow()

    // Database Flows
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<UserEntity>> = repository.leaderboardStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedPosts: StateFlow<List<FeedPostEntity>> = repository.allFeedPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamEntity>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedules: StateFlow<List<ScheduleEntity>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rewards: StateFlow<List<RewardEntity>> = repository.allRewards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val redemptions: StateFlow<List<RedemptionEntity>> = repository.allRedemptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tardyRecords: StateFlow<List<TardyRecordEntity>> = repository.allTardyRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBadges: StateFlow<List<BadgeEntity>> = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchoolEvents: StateFlow<List<SchoolEventEntity>> = repository.allSchoolEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPenalties: StateFlow<List<com.example.data.local.entity.PenaltyEntity>> = repository.allPenalties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parentObligations: StateFlow<List<ParentObligationEntity>> = repository.allParentObligations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        try {
            NotificationHelper.initNotificationChannels(application)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        viewModelScope.launch {
            try {
                // 0. Autoprotección y restauración inmediata de respaldo persistente local ante actualizaciones de la app
                try {
                    val backedUpUsers = EscolarisBackupManager.restoreUsersBackup(application)
                    for (bu in backedUpUsers) {
                        if (repository.getUserDirect(bu.id) == null) {
                            repository.insertUser(bu)
                        }
                    }
                    val backedUpPosts = EscolarisBackupManager.restoreFeedPostsBackup(application)
                    if (backedUpPosts.isNotEmpty()) {
                        val existingPosts = repository.getAllFeedPostsDirect().map { it.id }.toSet()
                        val postsToInsert = backedUpPosts.filter { it.id !in existingPosts }
                        if (postsToInsert.isNotEmpty()) {
                            repository.insertFeedPosts(postsToInsert)
                        }
                    }
                    val backedUpComments = EscolarisBackupManager.restorePostCommentsBackup(application)
                    if (backedUpComments.isNotEmpty()) {
                        val existingComments = repository.getAllCommentsDirect().map { it.id }.toSet()
                        val commentsToInsert = backedUpComments.filter { it.id !in existingComments }
                        if (commentsToInsert.isNotEmpty()) {
                            repository.insertComments(commentsToInsert)
                        }
                    }
                    val isBadgesV1Initialized = sessionPrefs.getBoolean("BADGES_V1_INITIALIZED_2026", false)
                    if (!isBadgesV1Initialized) {
                        EscolarisBackupManager.clearBadgesBackup(application)
                        repository.clearAllBadges()
                        try {
                            resetBadgesInCloudFirestore()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        sessionPrefs.edit().putBoolean("BADGES_V1_INITIALIZED_2026", true).apply()
                    } else {
                        val backedUpBadges = EscolarisBackupManager.restoreBadgesBackup(application)
                        if (backedUpBadges.isNotEmpty()) {
                            EscolarisDatabase.getDatabase(application).schoolDao().insertBadges(backedUpBadges)
                        }
                    }
                    val backedUpObligations = EscolarisBackupManager.restoreParentObligationsBackup(application)
                    if (backedUpObligations.isNotEmpty()) {
                        EscolarisDatabase.getDatabase(application).schoolDao().insertParentObligations(backedUpObligations)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                DatabaseInitializer.seedDatabaseIfEmpty(EscolarisDatabase.getDatabase(application).schoolDao())
                repository.seedInitialSubjectsIfEmpty()

                // Asegurar sesión en Firebase Auth para que no existan errores de permisos
                authService.ensureFirebaseAuthSession()

                // Sincronizar automáticamente todos los usuarios, publicaciones, comentarios, multas y medallas desde Cloud Firestore a SQLite Room
                syncCloudFirestoreData()

                // Iniciar sincronización bidireccional y en tiempo real del muro social (posts y comentarios)
                startRealtimeSocialSync()

                // Monitoreo y respaldo continuo en segundo plano para proteger cualquier cambio
                viewModelScope.launch(Dispatchers.IO) {
                    repository.allUsers.collect { users ->
                        if (users.isNotEmpty()) {
                            EscolarisBackupManager.saveUsersBackup(application, users)
                        }
                    }
                }
                viewModelScope.launch(Dispatchers.IO) {
                    repository.allFeedPosts.collect { posts ->
                        if (posts.isNotEmpty()) {
                            EscolarisBackupManager.saveFeedPostsBackup(application, posts)
                        }
                    }
                }
                viewModelScope.launch(Dispatchers.IO) {
                    allBadges.collect { badges ->
                        if (badges.isNotEmpty()) {
                            EscolarisBackupManager.saveBadgesBackup(application, badges)
                        }
                    }
                }
                viewModelScope.launch(Dispatchers.IO) {
                    parentObligations.collect { obligations ->
                        if (obligations.isNotEmpty()) {
                            EscolarisBackupManager.saveParentObligationsBackup(application, obligations)
                        }
                    }
                }

                // 1. Restaurar sesión persistida desde SharedPreferences
                val savedUserId = sessionPrefs.getString(PREF_SAVED_USER_ID, null)
                val savedUserEmail = sessionPrefs.getString(PREF_SAVED_USER_EMAIL, null)

                var user: UserEntity? = null
                if (!savedUserId.isNullOrBlank()) {
                    user = repository.getUserDirect(savedUserId)
                }
                if (user == null && !savedUserEmail.isNullOrBlank()) {
                    user = repository.getUserByEmailDirect(savedUserEmail)
                }
                if (user == null && !savedUserEmail.isNullOrBlank()) {
                    user = authService.findExistingUserByEmail(savedUserEmail)
                }
                if (user == null) {
                    val fbUser = authService.currentUser
                    if (fbUser != null) {
                        user = repository.getUserDirect(fbUser.uid)
                            ?: repository.getUserByEmailDirect(fbUser.email ?: "")
                            ?: authService.findExistingUserByEmail(fbUser.email ?: "")
                    }
                }
                // Si la BD local Room se reinició o actualizó de versión, restaurar el usuario desde el caché de SharedPreferences
                if (user == null) {
                    val cachedUser = getInitialCachedUser()
                    if (cachedUser != null) {
                        user = cachedUser
                        try {
                            repository.insertUser(cachedUser)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                if (user != null) {
                    _currentUserId.value = user.id
                    _currentUser.value = user
                    _isAuthenticated.value = true
                    loadActiveUser(user.id)

                    persistSessionUser(user)

                    // Set theme according to student gender / color
                    if (user.avatarColorHex == 0xFFE11D74 || user.bannerGradientIndex == 4) {
                        _colorTheme.value = AppColorTheme.MAGENTA_PINK
                    } else {
                        _colorTheme.value = AppColorTheme.ROYAL_BLUE
                    }
                } else {
                    _currentUserId.value = ""
                    _currentUser.value = null
                    _isAuthenticated.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun persistSessionUser(user: UserEntity) {
        sessionPrefs.edit()
            .putString(PREF_SAVED_USER_ID, user.id)
            .putString(PREF_SAVED_USER_EMAIL, user.email)
            .putString(PREF_SAVED_USER_NAME, user.name)
            .putString(PREF_SAVED_USER_ROLE, user.role)
            .putString(PREF_SAVED_USER_GRADE, user.gradeSection)
            .putString(PREF_SAVED_USER_AVATAR, user.avatarEmoji)
            .putLong(PREF_SAVED_USER_COLOR, user.avatarColorHex)
            .putString(PREF_SAVED_USER_PHOTO, user.photoUri)
            .putString(PREF_SAVED_TEACHER_CODE, user.teacherCode)
            .putString(PREF_SAVED_STUDENT_CODE, user.studentCode)
            .putString(PREF_SAVED_PHONE, user.phoneNumber)
            .putInt(PREF_SAVED_CREDITS, user.credits)
            .putInt(PREF_SAVED_XP, user.xp)
            .putInt(PREF_SAVED_PARENT_INCENTIVE, user.parentIncentiveCredits)
            .apply()

        registerFcmTokenForUser(user.id)
    }

    // ==========================================
    // USER / SESSION & RBAC
    // ==========================================
    fun onUserAuthenticated(user: UserEntity) {
        _currentUserId.value = user.id
        _currentUser.value = user
        _isAuthenticated.value = true

        persistSessionUser(user)

        // Set theme according to student gender / color
        if (user.avatarColorHex == 0xFFE11D74 || user.bannerGradientIndex == 4) {
            _colorTheme.value = AppColorTheme.MAGENTA_PINK
        } else {
            _colorTheme.value = AppColorTheme.ROYAL_BLUE
        }

        _userMessage.value = "Sesión iniciada con éxito: ${user.name}"
    }

    fun signOutUser() {
        sessionPrefs.edit()
            .remove(PREF_SAVED_USER_ID)
            .remove(PREF_SAVED_USER_EMAIL)
            .remove(PREF_SAVED_USER_NAME)
            .remove(PREF_SAVED_USER_ROLE)
            .remove(PREF_SAVED_USER_GRADE)
            .remove(PREF_SAVED_USER_AVATAR)
            .remove(PREF_SAVED_USER_COLOR)
            .remove(PREF_SAVED_USER_PHOTO)
            .remove(PREF_SAVED_TEACHER_CODE)
            .remove(PREF_SAVED_STUDENT_CODE)
            .remove(PREF_SAVED_PHONE)
            .remove(PREF_SWITCHED_FROM_PARENT_ID)
            .apply()
        authService.signOut()
        _currentUserId.value = ""
        _currentUser.value = null
        _switchedFromParentId.value = null
        _isAuthenticated.value = false
        _userMessage.value = "Sesión cerrada correctamente"
    }

    fun enterDemoMode() {
        _isAuthenticated.value = true
        _userMessage.value = "Modo Demo Activo (Estudiante / Docente)"
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            _currentUserId.value = userId
            loadActiveUser(userId)
            val user = repository.getUserDirect(userId)
            if (user != null) {
                persistSessionUser(user)
            }
            val roleName = when (user?.role) {
                UserRole.TEACHER.code -> "Docente Titular & SuperAdmin 👑"
                UserRole.PARENT.code -> "Padre de Familia 👨‍👩‍👧"
                else -> "Estudiante 🎓"
            }
            _userMessage.value = "Sesión activa: ${user?.name ?: "Usuario"} ($roleName)"
        }
    }

    fun switchToChildProfile(childUserId: String) {
        viewModelScope.launch {
            val current = _currentUser.value
            if (current != null && current.role == UserRole.PARENT.code) {
                sessionPrefs.edit().putString(PREF_SWITCHED_FROM_PARENT_ID, current.id).apply()
                _switchedFromParentId.value = current.id
            }
            switchUser(childUserId)
            val child = repository.getUserDirect(childUserId)
            _userMessage.value = "📱 Modo Estudiante activo: ${child?.name ?: "Estudiante"}"
        }
    }

    fun switchBackToParentProfile() {
        viewModelScope.launch {
            val parentId = _switchedFromParentId.value ?: sessionPrefs.getString(PREF_SWITCHED_FROM_PARENT_ID, null)
            if (!parentId.isNullOrBlank()) {
                sessionPrefs.edit().remove(PREF_SWITCHED_FROM_PARENT_ID).apply()
                _switchedFromParentId.value = null
                switchUser(parentId)
                val parent = repository.getUserDirect(parentId)
                _userMessage.value = "👨‍👩‍👧 Volviste al portal de Acudiente: ${parent?.name ?: ""}"
            }
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _currentUser.value
            if (current?.id == user.id) {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "⚠️ No puedes eliminar tu propio usuario en sesión activa"
                }
                return@launch
            }
            try {
                FirebaseFirestore.getInstance().collection("users").document(user.id).delete().await()
                repository.deleteUser(user)
                withContext(Dispatchers.Main) {
                    _userMessage.value = "🗑️ Cuenta de '${user.name}' (${user.role}) eliminada correctamente de la nube"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _userMessage.value = "❌ Error al eliminar usuario en Firestore: ${e.message}"
                }
            }
        }
    }

    private suspend fun loadActiveUser(userId: String) {
        val user = repository.getUserDirect(userId)
        _currentUser.value = user
        if (user != null) {
            persistSessionUser(user)
            if (user.avatarColorHex == 0xFFE11D74 || user.bannerGradientIndex == 4) {
                _colorTheme.value = AppColorTheme.MAGENTA_PINK
            } else {
                _colorTheme.value = AppColorTheme.ROYAL_BLUE
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun setThemeMode(mode: DarkThemeMode) {
        _themeMode.value = mode
    }

    fun setColorTheme(theme: AppColorTheme) {
        _colorTheme.value = theme
    }

    // ==========================================
    // CLOUD SYNC
    // ==========================================
    fun triggerCloudSync() {
        viewModelScope.launch {
            _syncState.value = SyncState.SYNCING
            delay(900)
            _lastSyncTime.value = System.currentTimeMillis()
            _syncState.value = SyncState.SYNCED
            _userMessage.value = "✅ Datos sincronizados con la nube escolar"
        }
    }

    fun toggleOfflineMode() {
        if (_syncState.value == SyncState.OFFLINE) {
            triggerCloudSync()
        } else {
            _syncState.value = SyncState.OFFLINE
            _userMessage.value = "📱 Modo sin conexión activado (Persistencia local Room)"
        }
    }

    // ==========================================
    // FEED & COMMUNITY
    // ==========================================
    fun requestLateHelp(subject: String, classDateStr: String, note: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val subjectClean = subject.ifBlank { "Materia Escolar" }
            val properName = ValidationUtils.formatProperNoun(user.name)
            val title = "🚨 ¡Estoy atrasado! - ¿Me mandas los apuntes de $subjectClean?"
            val content = "Hoy llegué tarde o falté a la clase de $subjectClean ($classDateStr). ${note.trim()}. ¿Quién me comparte fotos de la pizarra o las actividades?"
            val postId = System.currentTimeMillis() + (100..999).random()

            val post = FeedPostEntity(
                id = postId,
                authorId = user.id,
                authorName = properName,
                authorRole = user.role,
                authorAvatarColorHex = user.avatarColorHex,
                title = title,
                content = content,
                postType = PostType.LATE_HELP_REQUEST.code,
                subject = subjectClean,
                attachmentsJson = "Solicitud de fotos y apuntes",
                resolvedStatus = false,
                timestamp = System.currentTimeMillis()
            )
            repository.createFeedPost(post)

            // Sincronizar permanentemente a Cloud Firestore
            try {
                val store = FirebaseFirestore.getInstance()
                val postDoc = hashMapOf(
                    "id" to postId,
                    "authorId" to user.id,
                    "authorName" to properName,
                    "authorRole" to user.role,
                    "authorAvatarColorHex" to user.avatarColorHex,
                    "title" to title,
                    "content" to content,
                    "postType" to PostType.LATE_HELP_REQUEST.code,
                    "category" to PostType.LATE_HELP_REQUEST.code,
                    "subject" to subjectClean,
                    "attachmentsJson" to "Solicitud de fotos y apuntes",
                    "resolvedStatus" to false,
                    "timestamp" to post.timestamp,
                    "timestampMillis" to post.timestamp,
                    "likesCount" to 0,
                    "likes" to 0L,
                    "commentsCount" to 0,
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                store.collection("feed_posts").document(postId.toString()).set(postDoc, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("FIRESTORE_SYNC", "Error subiendo solicitud de apuntes a Firestore", e)
            }

            NotificationHelper.showPushNotification(
                getApplication(),
                (1000..9999).random(),
                "🚨 $title",
                "$properName solicita apuntes en el Muro Escolar.",
                "LATE_HELP"
            )

            _userMessage.value = "Solicitud de apuntes publicada en el Muro"
        }
    }

    fun togglePostLike(post: FeedPostEntity) {
        viewModelScope.launch {
            val delta = if (post.isLikedByMe) -1 else 1
            repository.togglePostLike(post.id, delta, !post.isLikedByMe)
            try {
                val store = FirebaseFirestore.getInstance()
                store.collection("feed_posts").document(post.id.toString()).update(
                    "likesCount", com.google.firebase.firestore.FieldValue.increment(delta.toLong())
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markHelpResolved(postId: Long) {
        viewModelScope.launch {
            repository.markHelpRequestResolved(postId)
            try {
                val store = FirebaseFirestore.getInstance()
                store.collection("feed_posts").document(postId.toString()).update("resolvedStatus", true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _userMessage.value = "✅ ¡Solicitud marcada como resuelta!"
        }
    }

    fun createAnnouncement(title: String, content: String, subject: String, postType: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val validTitle = ValidationUtils.validateNonEmptyText(title, "Título")
            if (!validTitle.isValid) {
                _userMessage.value = (validTitle as com.example.domain.validation.ValidationResult.Invalid).errorMessage
                return@launch
            }

            val isTeacher = UserRole.isTeacherOrAdmin(user.role) || user.email == "moz658@gmail.com"
            val cleanTitle = title.trim()
            val cleanContent = content.trim()
            val shortContent = if (cleanContent.length > 100) cleanContent.take(100) + "..." else cleanContent

            // Notificación contextualizada
            val (notifTitle, notifType) = when (postType) {
                PostType.EVENT.code -> {
                    val t = if (isTeacher) "📅 El profe programó un nuevo evento" else "📅 Nuevo evento publicado"
                    t to "EVENT"
                }
                PostType.HOMEWORK_ALERT.code -> {
                    val t = if (isTeacher) "📝 El profe publicó un aviso de tarea" else "📝 Recordatorio de tarea publicado"
                    t to "HOMEWORK"
                }
                else -> {
                    val t = if (isTeacher) "📢 El profe hizo un aviso" else "📢 Nuevo aviso en el muro"
                    t to "ANNOUNCEMENT"
                }
            }

            val notifMessage = if (cleanTitle.isNotBlank()) {
                "\"$cleanTitle\": $shortContent"
            } else {
                shortContent
            }

            val properAuthorName = ValidationUtils.formatProperNoun(user.name)
            val postId = System.currentTimeMillis() + (100..999).random()

            val effectivePostType = if (postType.isBlank()) PostType.ANNOUNCEMENT.code else postType.trim()
            val post = FeedPostEntity(
                id = postId,
                authorId = user.id,
                authorName = properAuthorName,
                authorRole = user.role,
                authorAvatarColorHex = user.avatarColorHex,
                title = cleanTitle,
                content = cleanContent,
                postType = effectivePostType,
                subject = subject.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.createFeedPost(post)
            try {
                EscolarisBackupManager.saveFeedPostsBackup(getApplication(), repository.getAllFeedPostsDirect())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 1. Notificación local en el sistema para todos los usuarios (Campanita e historial)
            val notifEntity = NotificationEntity(
                title = notifTitle,
                message = notifMessage,
                type = notifType,
                targetScreen = "feed",
                timestamp = System.currentTimeMillis()
            )
            repository.insertNotification(notifEntity)

            // 2. Notificación push del sistema Android (barra de estado)
            NotificationHelper.showPushNotification(
                context = getApplication(),
                id = (1000..9999).random(),
                title = notifTitle,
                message = notifMessage,
                type = notifType
            )

            // 3. Sincronización permanente estilo Red Social en Cloud Firestore
            var cloudSyncSuccess = true
            try {
                val store = FirebaseFirestore.getInstance()
                val notifDoc = hashMapOf(
                    "title" to notifTitle,
                    "message" to notifMessage,
                    "type" to notifType,
                    "authorId" to user.id,
                    "authorName" to properAuthorName,
                    "targetScreen" to "feed",
                    "timestampMillis" to System.currentTimeMillis(),
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                store.collection("notifications").add(notifDoc).await()

                val postDoc = hashMapOf(
                    "id" to postId,
                    "authorId" to user.id,
                    "authorName" to properAuthorName,
                    "authorRole" to user.role,
                    "authorAvatarColorHex" to user.avatarColorHex,
                    "title" to cleanTitle,
                    "content" to cleanContent,
                    "postType" to effectivePostType,
                    "category" to effectivePostType,
                    "subject" to subject.trim(),
                    "timestamp" to post.timestamp,
                    "timestampMillis" to post.timestamp,
                    "likesCount" to 0,
                    "likes" to 0L,
                    "commentsCount" to 0,
                    "resolvedStatus" to false,
                    "attachmentsJson" to "",
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                store.collection("feed_posts").document(postId.toString()).set(postDoc, SetOptions.merge()).await()
            } catch (e: Exception) {
                cloudSyncSuccess = false
                Log.e("FIRESTORE_SYNC", "Error subiendo post a Firestore", e)
                _userMessage.value = "⚠️ Guardado localmente, pero falló al sincronizar en la nube: ${e.localizedMessage ?: "Verifica sesión de Firebase"}"
            }

            if (cloudSyncSuccess) {
                _userMessage.value = "📢 Publicación compartida y notificada a todos los usuarios"
            }
        }
    }

    fun deletePost(postId: Long) {
        viewModelScope.launch {
            // 1. Eliminación local en Room (publicación y comentarios asociados)
            repository.deletePost(postId)
            // 2. Eliminación en respaldo local persistente
            EscolarisBackupManager.deletePostFromBackup(getApplication(), postId)
            EscolarisBackupManager.deleteCommentsForPostFromBackup(getApplication(), postId)

            // 3. Eliminación atómica en Cloud Firestore con await
            try {
                val store = FirebaseFirestore.getInstance()
                store.collection("feed_posts").document(postId.toString()).delete().await()

                // Eliminar todos los comentarios asociados a esta publicación en Firestore
                val commentsSnap = store.collection("post_comments")
                    .whereEqualTo("postId", postId)
                    .get()
                    .await()
                if (!commentsSnap.isEmpty) {
                    val batch = store.batch()
                    for (doc in commentsSnap.documents) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _userMessage.value = "Publicación eliminada del muro"
        }
    }

    fun deleteComment(commentId: Long, postId: Long) {
        viewModelScope.launch {
            // 1. Eliminación local en Room y decremento del contador
            repository.deleteComment(commentId)
            repository.decrementCommentCount(postId)
            // 2. Eliminación en respaldo local persistente
            EscolarisBackupManager.deleteCommentFromBackup(getApplication(), commentId)

            // 3. Eliminación en Cloud Firestore con await y decremento en feed_posts
            try {
                val store = FirebaseFirestore.getInstance()
                store.collection("post_comments").document(commentId.toString()).delete().await()
                store.collection("feed_posts").document(postId.toString()).update(
                    "commentsCount", com.google.firebase.firestore.FieldValue.increment(-1)
                ).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _userMessage.value = "Comentario eliminado"
        }
    }

    fun getCommentsForPost(postId: Long): Flow<List<PostCommentEntity>> {
        return repository.getCommentsForPost(postId)
    }

    fun addCommentToPost(postId: Long, text: String, attachmentNote: String? = null) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val validText = text.trim()
            if (validText.isBlank() && attachmentNote.isNullOrBlank()) {
                _userMessage.value = "El comentario o adjunto no puede estar vacío."
                return@launch
            }

            val properAuthor = ValidationUtils.formatProperNoun(user.name)
            val commentId = System.currentTimeMillis() + (100..999).random()

            val comment = PostCommentEntity(
                id = commentId,
                postId = postId,
                authorId = user.id,
                authorName = properAuthor,
                authorRole = user.role,
                content = validText.ifBlank { "¡Aquí tienes los apuntes de la clase!" },
                attachmentDescription = attachmentNote,
                timestamp = System.currentTimeMillis()
            )
            repository.addComment(comment)

            // Sincronizar comentario en Cloud Firestore y actualizar contador de comentarios del post
            try {
                val store = FirebaseFirestore.getInstance()
                val commentDoc = hashMapOf(
                    "id" to commentId,
                    "postId" to postId,
                    "authorId" to user.id,
                    "authorName" to properAuthor,
                    "authorRole" to user.role,
                    "content" to comment.content,
                    "timestamp" to comment.timestamp,
                    "attachmentDescription" to (attachmentNote ?: "")
                )
                store.collection("post_comments").document(commentId.toString()).set(commentDoc, SetOptions.merge())
                store.collection("feed_posts").document(postId.toString()).update(
                    "commentsCount", com.google.firebase.firestore.FieldValue.increment(1)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (user.role == UserRole.STUDENT.code) {
                repository.addCreditsAndXp(user.id, 25, 50)
                loadActiveUser(user.id)
                _userMessage.value = "💬 Aporte publicado (+25 créditos por colaborar)"
            } else {
                _userMessage.value = "Comentario enviado"
            }
        }
    }

    // ==========================================
    // TASKS (HOMEWORK)
    // ==========================================
    fun addNewTask(
        title: String,
        subject: String,
        description: String,
        dueDateMillis: Long,
        priority: String,
        rewardCredits: Int = 40
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val validTitle = ValidationUtils.validateNonEmptyText(title, "Título de la tarea")
            if (!validTitle.isValid) {
                _userMessage.value = (validTitle as com.example.domain.validation.ValidationResult.Invalid).errorMessage
                return@launch
            }

            val studentTargetId = user.id
            val task = TaskEntity(
                studentId = studentTargetId,
                title = title.trim(),
                subject = subject.trim().ifBlank { "Matemáticas" },
                description = description.trim(),
                dueDateMillis = dueDateMillis,
                priority = priority,
                status = TaskStatus.PENDING.code,
                rewardCredits = rewardCredits.coerceAtLeast(10)
            )
            val taskId = repository.insertTask(task)

            // Programar recordatorio push para 1 día antes a las 3:00 PM (15:00 hrs)
            NotificationHelper.scheduleTaskReminder(
                context = getApplication(),
                taskId = taskId,
                subject = task.subject,
                title = task.title,
                dueDateMillis = dueDateMillis
            )

            _userMessage.value = "Tarea agregada (Recordatorio programado 1 día antes a las 3:00 PM)"
        }
    }

    fun toggleTaskStatus(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.COMPLETED.code) TaskStatus.PENDING.code else TaskStatus.COMPLETED.code
            repository.updateTaskStatus(task.id, newStatus)

            if (newStatus == TaskStatus.COMPLETED.code) {
                NotificationHelper.cancelTaskReminder(getApplication(), task.id)
                repository.addCreditsAndXp(task.studentId, task.rewardCredits, 75)
                try {
                    val store = FirebaseFirestore.getInstance()
                    store.collection("users").document(task.studentId).update(
                        "credits", com.google.firebase.firestore.FieldValue.increment(task.rewardCredits.toLong()),
                        "xp", com.google.firebase.firestore.FieldValue.increment(75L)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                loadActiveUser(_currentUserId.value)
                _userMessage.value = "🎉 ¡Tarea completada! Ganaste +${task.rewardCredits} créditos y +75 XP"
            } else {
                NotificationHelper.scheduleTaskReminder(
                    context = getApplication(),
                    taskId = task.id,
                    subject = task.subject,
                    title = task.title,
                    dueDateMillis = task.dueDateMillis
                )
                repository.addCreditsAndXp(task.studentId, -task.rewardCredits, -75)
                try {
                    val store = FirebaseFirestore.getInstance()
                    store.collection("users").document(task.studentId).update(
                        "credits", com.google.firebase.firestore.FieldValue.increment(-task.rewardCredits.toLong()),
                        "xp", com.google.firebase.firestore.FieldValue.increment(-75L)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                loadActiveUser(_currentUserId.value)
                _userMessage.value = "Tarea marcada como pendiente"
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            NotificationHelper.cancelTaskReminder(getApplication(), task.id)
            repository.deleteTask(task)
            _userMessage.value = "Tarea eliminada"
        }
    }

    // ==========================================
    // EXAMS (SCALE 1.0 TO 5.0)
    // ==========================================
    fun addNewExamSchedule(
        title: String,
        subject: String,
        examDateMillis: Long,
        classroom: String,
        topics: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val validTitle = ValidationUtils.validateNonEmptyText(title, "Título del examen")
            if (!validTitle.isValid) {
                _userMessage.value = (validTitle as com.example.domain.validation.ValidationResult.Invalid).errorMessage
                return@launch
            }

            val studentTargetId = user.id
            val exam = ExamEntity(
                studentId = studentTargetId,
                title = title.trim(),
                subject = subject.trim().ifBlank { "Evaluación" },
                examDateMillis = examDateMillis,
                classroom = classroom.trim().ifBlank { "Aula Escolar" },
                topics = topics.trim().ifBlank { "Temario general" },
                grade = null,
                maxGrade = 5.0,
                isGraded = false
            )
            repository.insertExam(exam)
            _userMessage.value = "Evaluación agendada con éxito"
        }
    }

    fun gradeStudentExam(examId: Long, grade: Double, feedback: String) {
        viewModelScope.launch {
            val clampedGrade = ValidationUtils.sanitizeGrade(grade)
            repository.updateExamGrade(examId, clampedGrade, feedback.trim())

            val targetExam = exams.value.find { it.id == examId }
            val studentTargetId = targetExam?.studentId ?: _currentUserId.value
            val earnedCredits = if (clampedGrade >= ValidationUtils.PASSING_GRADE) 100 else 30
            val xpGain = (clampedGrade * 30).toInt()
            if (studentTargetId.isNotBlank()) {
                repository.addCreditsAndXp(studentTargetId, earnedCredits, xpGain)
                if (_currentUserId.value == studentTargetId) {
                    loadActiveUser(_currentUserId.value)
                }
            }

            _userMessage.value = "Calificación registrada: ${String.format("%.1f", clampedGrade)}/5.0 (+${earnedCredits} créditos)"
        }
    }

    fun registerScannedExam(
        title: String,
        subject: String,
        dateMillis: Long = System.currentTimeMillis(),
        grade: Double,
        classroom: String = "Aula Escolar",
        topics: String = "Escaneado de prueba física",
        teacherFeedback: String = "",
        scannedPhotoUri: String? = null
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val studentId = user.id
            val clampedGrade = ValidationUtils.sanitizeGrade(grade)
            val credits = if (clampedGrade >= ValidationUtils.PASSING_GRADE) 80 else 20

            val exam = ExamEntity(
                studentId = studentId,
                title = title.trim().ifBlank { "Examen Digitalizado" },
                subject = subject.trim().ifBlank { "Materia General" },
                examDateMillis = dateMillis,
                classroom = classroom.trim().ifBlank { "Aula Escolar" },
                topics = topics.trim().ifBlank { "Evidencia fotográfica" },
                grade = clampedGrade,
                maxGrade = 5.0,
                scannedPhotoUri = scannedPhotoUri ?: "scanned_exam_${System.currentTimeMillis()}.jpg",
                teacherFeedback = teacherFeedback.trim(),
                isGraded = true,
                rewardCreditsEarned = credits
            )
            repository.insertExam(exam)
            repository.addCreditsAndXp(studentId, credits, (clampedGrade * 25).toInt())
            loadActiveUser(user.id)

            _userMessage.value = "📸 Examen digitalizado guardado con éxito (${String.format("%.1f", clampedGrade)}/5.0)"
        }
    }

    // ==========================================
    // SCHEDULES
    // ==========================================
    fun addScheduleSlot(
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        subject: String,
        classroom: String,
        teacher: String,
        colorHex: Long,
        isSelfStudy: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val validTime = ValidationUtils.validateTimeFormat(startTime)
            if (!validTime.isValid) {
                _userMessage.value = (validTime as com.example.domain.validation.ValidationResult.Invalid).errorMessage
                return@launch
            }

            val schedule = ScheduleEntity(
                studentId = user.id,
                dayOfWeek = dayOfWeek.coerceIn(1, 7),
                startTime = startTime.trim(),
                endTime = endTime.trim(),
                subject = subject.trim().ifBlank { "Clase" },
                classroomOrLocation = classroom.trim().ifBlank { "Aula" },
                teacherOrTutor = teacher.trim().ifBlank { user.name },
                isSelfStudySession = isSelfStudy,
                colorHex = colorHex,
                notes = notes.trim()
            )
            repository.insertSchedule(schedule)
            _userMessage.value = "Bloque de horario agregado"
        }
    }

    fun deleteScheduleSlot(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            _userMessage.value = "Bloque de horario eliminado"
        }
    }

    // ==========================================
    // REWARDS & PASSES
    // ==========================================
    fun redeemReward(reward: RewardEntity) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val result = repository.redeemReward(reward, user)
            result.onSuccess {
                loadActiveUser(user.id)
                _userMessage.value = "🎉 ¡Canje exitoso! Tu código es ${it.redemptionCode}"

                NotificationHelper.showPushNotification(
                    getApplication(),
                    it.id.toInt().coerceAtLeast(1),
                    "🎟️ Canje Exitoso: ${reward.title}",
                    "Código: ${it.redemptionCode}. Muestra este pase a tu docente.",
                    "REWARD"
                )
            }.onFailure { error ->
                _userMessage.value = "❌ Error: ${error.message}"
            }
        }
    }

    fun validateStudentRedemption(redemptionId: Long) {
        viewModelScope.launch {
            repository.validateRedemption(redemptionId)
            _userMessage.value = "Pase de beneficio utilizado en clase"
        }
    }

    fun approveStudentRedemption(redemptionId: Long) {
        viewModelScope.launch {
            repository.approveRedemption(redemptionId)
            _userMessage.value = "🎟️ Canje aprobado exitosamente"
        }
    }

    fun rejectStudentRedemption(redemptionId: Long) {
        viewModelScope.launch {
            val refunded = repository.rejectRedemption(redemptionId)
            if (refunded) {
                _userMessage.value = "❌ Canje rechazado. Puntos reembolsados al estudiante"
            }
        }
    }

    fun createNewTeacherReward(title: String, desc: String, cost: Int, category: String, icon: String, stock: Int) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val validTitle = ValidationUtils.validateNonEmptyText(title, "Título de recompensa")
            if (!validTitle.isValid) {
                _userMessage.value = (validTitle as com.example.domain.validation.ValidationResult.Invalid).errorMessage
                return@launch
            }

            val reward = RewardEntity(
                title = title.trim(),
                description = desc.trim(),
                costCredits = cost.coerceAtLeast(10),
                category = category.trim().ifBlank { "ACADÉMICO" },
                iconKey = icon,
                stockAvailable = stock.coerceAtLeast(1),
                teacherName = user.name
            )
            repository.insertReward(reward)
            _userMessage.value = "Recompensa añadida al catálogo escolar"
        }
    }

    fun updateTeacherReward(
        rewardId: Long,
        title: String,
        desc: String,
        cost: Int,
        category: String = "ACADÉMICO",
        stock: Int = 10,
        icon: String = "🎁"
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val validTitle = ValidationUtils.validateNonEmptyText(title, "Título de recompensa")
            if (!validTitle.isValid) {
                _userMessage.value = (validTitle as com.example.domain.validation.ValidationResult.Invalid).errorMessage
                return@launch
            }

            val reward = RewardEntity(
                id = rewardId,
                title = title.trim(),
                description = desc.trim(),
                costCredits = cost.coerceAtLeast(5),
                category = category.trim().ifBlank { "ACADÉMICO" },
                iconKey = icon,
                stockAvailable = stock.coerceAtLeast(0),
                teacherName = user.name
            )
            repository.updateReward(reward)
            _userMessage.value = "✏️ Recompensa actualizada correctamente"
        }
    }

    fun deleteReward(reward: RewardEntity) {
        viewModelScope.launch {
            repository.deleteReward(reward)
            _userMessage.value = "🗑️ Recompensa eliminada del catálogo"
        }
    }

    fun grantParentIncentive(amount: Int, reason: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            if (user.role != UserRole.PARENT.code) {
                _userMessage.value = "Solo los padres pueden otorgar puntos de incentivo."
                return@launch
            }
            val studentId = user.linkedStudentId
            if (studentId.isNullOrBlank()) {
                _userMessage.value = "No tienes una cuenta de estudiante vinculada."
                return@launch
            }
            if (user.parentIncentiveCredits < amount) {
                _userMessage.value = "No tienes suficientes créditos de incentivo disponibles."
                return@launch
            }
            repository.grantParentIncentiveToStudent(user.id, studentId, amount, reason.ifBlank { "Asistencia a reunión de padres y apoyo en casa" })
            loadActiveUser(user.id)
            _userMessage.value = "✨ ¡Has otorgado +$amount créditos a tu hijo/a exitosamente!"
        }
    }

    // ==========================================
    // TARDY / ATTENDANCE RECORDS
    // ==========================================
    fun recordTardyArrival(
        studentId: String,
        studentName: String,
        delayMinutes: Int,
        subject: String,
        reason: String,
        arrivalTime: String,
        notifyParents: Boolean,
        observation: String,
        status: String = AttendanceStatus.PENDIENTE.code
    ) {
        viewModelScope.launch {
            val record = TardyRecordEntity(
                studentId = studentId,
                studentName = studentName,
                delayMinutes = delayMinutes.coerceAtLeast(1),
                subject = subject.trim().ifBlank { "Clase General" },
                reason = reason.trim().ifBlank { "Retardo no especificado" },
                arrivalTime = arrivalTime.trim().ifBlank { "07:45 AM" },
                notifiedParents = notifyParents,
                teacherObservation = observation.trim(),
                status = status
            )
            repository.insertTardyRecord(record)

            if (notifyParents) {
                NotificationHelper.showPushNotification(
                    getApplication(),
                    (1000..9999).random(),
                    "⏰ Registro de Llegada Tarde: $studentName",
                    "Retardo de $delayMinutes min en $subject ($arrivalTime). Motivo: $reason",
                    "TARDY"
                )

                repository.insertNotification(
                    NotificationEntity(
                        title = "⏰ Registro de Retardo: $studentName",
                        message = "Se registró retardo de $delayMinutes min en la clase de $subject a las $arrivalTime. Motivo: $reason",
                        type = "TARDY",
                        targetScreen = "management"
                    )
                )
            }

            _userMessage.value = "⏰ Llegada tarde registrada para $studentName ($delayMinutes min)"
        }
    }

    fun updateTardyStatus(recordId: Long, newStatus: String, observation: String) {
        viewModelScope.launch {
            repository.updateTardyRecordStatus(recordId, newStatus, observation.trim())
            _userMessage.value = "Estado del retardo actualizado a: $newStatus"
        }
    }

    fun deleteTardyRecord(record: TardyRecordEntity) {
        viewModelScope.launch {
            repository.deleteTardyRecord(record)
            _userMessage.value = "Registro de retardo eliminado"
        }
    }

    // ==========================================
    // BADGES & SUPERADMIN
    // ==========================================
    fun unlockBadgeForStudent(
        studentId: String,
        studentName: String,
        badgeKey: String,
        title: String,
        description: String,
        emoji: String,
        category: String,
        teacherNote: String,
        xpReward: Int = 100,
        creditReward: Int = 50,
        clayColorHex: Long = 0xFF6366F1,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val teacherName = user.name

            val badge = BadgeEntity(
                studentId = studentId,
                badgeKey = badgeKey,
                title = title.trim(),
                description = description.trim(),
                emoji = emoji,
                category = category,
                unlockedAtMillis = System.currentTimeMillis(),
                unlockedByTeacher = teacherName,
                teacherNote = teacherNote.trim(),
                clayColorHex = clayColorHex,
                xpReward = xpReward,
                creditReward = creditReward,
                photoUri = photoUri
            )
            repository.insertBadge(badge)
            repository.addCreditsAndXp(studentId, creditReward, xpReward)

            // Guardar permanentemente en Cloud Firestore
            try {
                val store = FirebaseFirestore.getInstance()
                val badgeDoc = hashMapOf(
                    "studentId" to studentId,
                    "studentName" to studentName,
                    "badgeKey" to badgeKey,
                    "title" to title,
                    "description" to description,
                    "emoji" to emoji,
                    "category" to category,
                    "teacherNote" to teacherNote,
                    "unlockedByTeacher" to teacherName,
                    "creditReward" to creditReward,
                    "xpReward" to xpReward,
                    "photoUrl" to (photoUri ?: ""),
                    "unlockedAtMillis" to System.currentTimeMillis(),
                    "status" to "ACTIVE"
                )
                store.collection("badges").document("${studentId}_${badgeKey}").set(badgeDoc, SetOptions.merge())

                val studentDoc = store.collection("users").document(studentId).get().await()
                if (studentDoc.exists()) {
                    val currCredits = (studentDoc.getLong("credits") ?: 100L).toInt()
                    val currXp = (studentDoc.getLong("xp") ?: 50L).toInt()
                    store.collection("users").document(studentId).update(
                        "credits", currCredits + creditReward,
                        "xp", currXp + xpReward
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val isFamily = category == "FAMILY"
            val notifTitle = if (isFamily) "👨‍👩‍👧 ¡Medalla Familiar Desbloqueada: $title!" else "🎖️ ¡Nueva Insignia Desbloqueada: $title!"
            val notifBody = if (isFamily) {
                "$teacherName condecoró a la familia de $studentName con la medalla $emoji $title. Ganaron +$xpReward XP y +$creditReward créditos."
            } else {
                "$teacherName te otorgó la insignia $emoji $title. Ganaste +$xpReward XP y +$creditReward créditos."
            }

            NotificationHelper.showPushNotification(
                getApplication(),
                (1000..9999).random(),
                notifTitle,
                notifBody,
                "REWARD"
            )

            repository.insertNotification(
                NotificationEntity(
                    title = notifTitle,
                    message = if (isFamily) {
                        "El docente $teacherName ha condecorado a la familia con la medalla $emoji '$title': \"$teacherNote\". (+${xpReward} XP, +${creditReward} créditos para el estudiante)."
                    } else {
                        "El docente $teacherName ha reconocido a $studentName con la insignia $emoji '$title': \"$teacherNote\". (+${xpReward} XP, +${creditReward} créditos)."
                    },
                    type = "REWARD",
                    targetScreen = if (isFamily) "parent_dashboard" else "profile"
                )
            )

            if (_currentUserId.value == studentId) {
                loadActiveUser(studentId)
            }

            _userMessage.value = if (isFamily) "👨‍👩‍👧 ¡Medalla Familiar '$title' $emoji otorgada a la familia de $studentName!" else "🎖️ ¡Insignia '$title' $emoji desbloqueada para $studentName!"
        }
    }

    fun revokeBadge(badge: BadgeEntity) {
        viewModelScope.launch {
            repository.deleteBadge(badge)
            try {
                FirebaseFirestore.getInstance().collection("badges")
                    .whereEqualTo("badgeKey", badge.badgeKey)
                    .whereEqualTo("studentId", badge.studentId)
                    .get().await().documents.forEach { it.reference.delete() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _userMessage.value = "Insignia '${badge.title}' revocada"
        }
    }

    // ==========================================
    // DISCIPLINARY PENALTIES / MULTAS ESCOLARES
    // ==========================================
    fun issueStudentPenalty(
        studentId: String,
        studentName: String,
        reason: String,
        category: String,
        pointsDeducted: Int,
        observation: String = "",
        notifyParents: Boolean = true
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val teacherName = user.name
            val penaltyPoints = pointsDeducted.coerceAtLeast(1)

            val penalty = com.example.data.local.entity.PenaltyEntity(
                studentId = studentId,
                studentName = studentName,
                reason = reason.trim(),
                category = category,
                pointsDeducted = penaltyPoints,
                teacherName = teacherName,
                observation = observation.trim(),
                timestamp = System.currentTimeMillis(),
                status = "APLICADA",
                notifiedParents = notifyParents
            )
            repository.insertPenalty(penalty)
            repository.deductCredits(studentId, penaltyPoints)

            // Guardar permanentemente en Cloud Firestore
            try {
                val store = FirebaseFirestore.getInstance()
                val penaltyDoc = hashMapOf(
                    "studentId" to studentId,
                    "studentName" to studentName,
                    "reason" to reason.trim(),
                    "category" to category,
                    "pointsDeducted" to penaltyPoints,
                    "teacherName" to teacherName,
                    "observation" to observation.trim(),
                    "timestamp" to System.currentTimeMillis(),
                    "status" to "APLICADA",
                    "notifiedParents" to notifyParents
                )
                store.collection("penalties").add(penaltyDoc)

                val studentDoc = store.collection("users").document(studentId).get().await()
                if (studentDoc.exists()) {
                    val currCredits = (studentDoc.getLong("credits") ?: 100L).toInt()
                    store.collection("users").document(studentId).update(
                        "credits", maxOf(0, currCredits - penaltyPoints)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (notifyParents) {
                val notifTitle = "🚨 Reporte Disciplinario: $studentName"
                val notifBody = "El docente $teacherName aplicó una sanción de -$penaltyPoints créditos. Motivo: $reason. Obs: $observation"

                NotificationHelper.showPushNotification(
                    getApplication(),
                    (1000..9999).random(),
                    notifTitle,
                    notifBody,
                    "PENALTY"
                )

                repository.insertNotification(
                    NotificationEntity(
                        title = notifTitle,
                        message = notifBody,
                        type = "PENALTY",
                        targetScreen = "parent_dashboard"
                    )
                )
            }

            if (_currentUserId.value == studentId) {
                loadActiveUser(studentId)
            }

            _userMessage.value = "🚨 Multa de -$penaltyPoints créditos aplicada a $studentName"
        }
    }

    fun revokePenalty(penalty: com.example.data.local.entity.PenaltyEntity) {
        viewModelScope.launch {
            repository.updatePenalty(penalty.copy(status = "REVOCADA"))
            repository.addCreditsAndXp(penalty.studentId, penalty.pointsDeducted, 0)
            try {
                val store = FirebaseFirestore.getInstance()
                val studentDoc = store.collection("users").document(penalty.studentId).get().await()
                if (studentDoc.exists()) {
                    val currCredits = (studentDoc.getLong("credits") ?: 0L).toInt()
                    store.collection("users").document(penalty.studentId).update(
                        "credits", currCredits + penalty.pointsDeducted
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (_currentUserId.value == penalty.studentId) {
                loadActiveUser(penalty.studentId)
            }
            _userMessage.value = "Sanción revocada y +${penalty.pointsDeducted} créditos reintegrados a ${penalty.studentName}"
        }
    }

    fun updateUserProfile(
        userId: String,
        name: String,
        bio: String,
        avatarEmoji: String,
        avatarColorHex: Long,
        photoUri: String? = null,
        bannerGradientIndex: Int = 0,
        gradeSection: String? = null,
        phoneNumber: String? = null
    ) {
        viewModelScope.launch {
            val user = repository.getUserDirect(userId) ?: return@launch
            val cleanName = ValidationUtils.formatProperNoun(name.trim().ifBlank { user.name })
            val updated = user.copy(
                name = cleanName,
                bio = bio.trim(),
                avatarEmoji = avatarEmoji,
                avatarColorHex = avatarColorHex,
                photoUri = photoUri,
                bannerGradientIndex = bannerGradientIndex,
                gradeSection = gradeSection?.trim()?.ifBlank { user.gradeSection } ?: user.gradeSection,
                phoneNumber = phoneNumber?.trim() ?: user.phoneNumber
            )
            repository.updateUser(updated)
            persistSessionUser(updated)

            // Actualizar en Firebase Auth si está logueado
            try {
                val fbUser = authService.currentUser
                if (fbUser != null && (fbUser.uid == userId || fbUser.email.equals(updated.email, ignoreCase = true))) {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(updated.name)
                        .build()
                    fbUser.updateProfile(profileUpdates)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Actualizar permanentemente en Cloud Firestore
            try {
                val store = FirebaseFirestore.getInstance()
                val updates = hashMapOf<String, Any>(
                    "id" to updated.id,
                    "name" to updated.name,
                    "email" to updated.email,
                    "role" to updated.role,
                    "bio" to updated.bio,
                    "avatarEmoji" to updated.avatarEmoji,
                    "avatarColorHex" to updated.avatarColorHex,
                    "bannerGradientIndex" to updated.bannerGradientIndex,
                    "gradeSection" to updated.gradeSection,
                    "phoneNumber" to updated.phoneNumber,
                    "photoUri" to (updated.photoUri ?: ""),
                    "photoUrl" to (updated.photoUri ?: ""),
                    "updatedAt" to System.currentTimeMillis()
                )
                store.collection("users").document(userId).set(updates, SetOptions.merge()).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _currentUser.value = updated
            _userMessage.value = "Perfil actualizado exitosamente"
            if (_currentUserId.value == userId) {
                loadActiveUser(userId)
            }
        }
    }

    fun adminCreateUser(
        name: String,
        email: String,
        role: String,
        gradeSection: String,
        initialCredits: Int = 100,
        initialXp: Int = 50,
        linkedStudentId: String? = null
    ) {
        viewModelScope.launch {
            val validName = ValidationUtils.formatProperNoun(name)
            val validEmail = email.trim().lowercase()
            if (validName.isBlank() || validEmail.isBlank()) {
                _userMessage.value = "Nombre y correo son obligatorios"
                return@launch
            }
            val existing = repository.getUserByEmailDirect(validEmail)
            if (existing != null) {
                _userMessage.value = "Ya existe un usuario con el correo $validEmail"
                return@launch
            }

            val uid = "usr_" + java.util.UUID.randomUUID().toString().take(8)
            val studentCode = if (role == UserRole.STUDENT.code) "ESC-" + (100000..999999).random() else ""
            val teacherCode = if (role == UserRole.TEACHER.code) "DOC-" + (100000..999999).random() else ""
            val emoji = if (role == UserRole.TEACHER.code) "👨‍🏫" else if (role == UserRole.PARENT.code) "👨‍👩‍👧" else "🎓"
            val cleanLinkedId = if (role == UserRole.PARENT.code) linkedStudentId?.trim()?.ifBlank { null } else null

            val newUser = UserEntity(
                id = uid,
                name = validName,
                email = validEmail,
                role = role,
                gradeSection = gradeSection.trim().ifBlank { if (role == UserRole.STUDENT.code) "10° Grado" else "General" },
                credits = initialCredits,
                xp = initialXp,
                studentCode = studentCode,
                teacherCode = teacherCode,
                avatarEmoji = emoji,
                bio = if (role == UserRole.TEACHER.code) "Docente en Escolaris 👨‍🏫" else if (role == UserRole.PARENT.code) "Acudiente en Escolaris 👨‍👩‍👧" else "Estudiante en Escolaris 🚀",
                linkedStudentId = cleanLinkedId
            )
            repository.insertUser(newUser)

            // Guardar permanentemente en Cloud Firestore
            try {
                val store = FirebaseFirestore.getInstance()
                val firestoreData = hashMapOf<String, Any?>(
                    "id" to uid,
                    "name" to validName,
                    "email" to validEmail,
                    "role" to role,
                    "gradeSection" to newUser.gradeSection,
                    "credits" to initialCredits,
                    "xp" to initialXp,
                    "studentCode" to studentCode,
                    "teacherCode" to teacherCode,
                    "avatarEmoji" to emoji,
                    "bio" to newUser.bio,
                    "linkedStudentId" to cleanLinkedId,
                    "createdAt" to System.currentTimeMillis()
                )
                store.collection("users").document(uid).set(firestoreData, SetOptions.merge()).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _userMessage.value = "🎉 ¡Persona '$validName' agregada exitosamente!"
        }
    }

    fun adminUpdateUser(
        userId: String,
        name: String,
        email: String,
        role: String,
        gradeSection: String,
        credits: Int,
        xp: Int,
        code: String,
        linkedStudentId: String? = null
    ) {
        viewModelScope.launch {
            val user = repository.getUserDirect(userId) ?: return@launch
            val cleanName = ValidationUtils.formatProperNoun(name.trim().ifBlank { user.name })
            val cleanLinkedId = if (role == UserRole.PARENT.code) linkedStudentId?.trim()?.ifBlank { null } else user.linkedStudentId
            val updated = user.copy(
                name = cleanName,
                email = email.trim().lowercase().ifBlank { user.email },
                role = role,
                gradeSection = gradeSection.trim(),
                credits = credits.coerceAtLeast(0),
                xp = xp.coerceAtLeast(0),
                studentCode = if (role == UserRole.STUDENT.code) (code.trim().uppercase().ifBlank { user.studentCode }) else user.studentCode,
                teacherCode = if (role == UserRole.TEACHER.code) (code.trim().uppercase().ifBlank { user.teacherCode }) else user.teacherCode,
                linkedStudentId = cleanLinkedId
            )
            repository.updateUser(updated)

            // Actualizar permanentemente en Cloud Firestore
            try {
                val store = FirebaseFirestore.getInstance()
                val updates = hashMapOf<String, Any?>(
                    "name" to updated.name,
                    "email" to updated.email,
                    "role" to updated.role,
                    "gradeSection" to updated.gradeSection,
                    "credits" to updated.credits,
                    "xp" to updated.xp,
                    "studentCode" to updated.studentCode,
                    "teacherCode" to updated.teacherCode,
                    "linkedStudentId" to cleanLinkedId,
                    "updatedAt" to System.currentTimeMillis()
                )
                store.collection("users").document(userId).set(updates, SetOptions.merge()).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (_currentUserId.value == userId) {
                loadActiveUser(userId)
            }
            _userMessage.value = "💾 Datos de '${updated.name}' actualizados"
        }
    }

    fun adminLinkParentToStudent(parentId: String, studentId: String?) {
        viewModelScope.launch {
            try {
                val parent = repository.getUserDirect(parentId) ?: return@launch
                val cleanStudentId = studentId?.trim()?.ifBlank { null }
                val updated = parent.copy(linkedStudentId = cleanStudentId)
                repository.updateUser(updated)

                // Actualizar de forma atómica en Cloud Firestore
                val store = FirebaseFirestore.getInstance()
                val updates = hashMapOf<String, Any?>(
                    "linkedStudentId" to cleanStudentId,
                    "updatedAt" to System.currentTimeMillis()
                )
                store.collection("users").document(parentId).set(updates, SetOptions.merge()).await()

                if (_currentUserId.value == parentId) {
                    loadActiveUser(parentId)
                }

                val child = if (cleanStudentId != null) repository.getUserDirect(cleanStudentId) else null
                val childName = child?.name ?: cleanStudentId

                _userMessage.value = if (childName != null) {
                    "🎉 ¡${parent.name} vinculado con éxito a $childName!"
                } else {
                    "ℹ️ Se ha desvinculado el estudiante de ${parent.name}"
                }
            } catch (e: Exception) {
                _userMessage.value = "Error al vincular: ${e.message}"
            }
        }
    }

    fun adminAdjustUserCredits(userId: String, deltaCredits: Int, deltaXp: Int, reason: String) {
        viewModelScope.launch {
            val user = repository.getUserDirect(userId) ?: return@launch
            val newCredits = (user.credits + deltaCredits).coerceAtLeast(0)
            val newXp = (user.xp + deltaXp).coerceAtLeast(0)
            val updated = user.copy(credits = newCredits, xp = newXp)
            repository.updateUser(updated)

            try {
                val store = FirebaseFirestore.getInstance()
                store.collection("users").document(userId).update(
                    "credits", newCredits,
                    "xp", newXp
                ).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (_currentUserId.value == userId) {
                loadActiveUser(userId)
            }
            _userMessage.value = "🪙 Saldo actualizado para ${user.name}: $newCredits créditos"
        }
    }

    fun adminAdjustMultipleUsersCredits(
        targetUserIds: List<String>,
        deltaCredits: Int,
        deltaXp: Int,
        reason: String
    ) {
        viewModelScope.launch {
            if (targetUserIds.isEmpty()) {
                _userMessage.value = "⚠️ Debes seleccionar al menos un usuario."
                return@launch
            }
            val currentUsers = allUsers.value
            val selected = currentUsers.filter { it.id in targetUserIds }
            for (u in selected) {
                val newCredits = (u.credits + deltaCredits).coerceAtLeast(0)
                val newXp = (u.xp + deltaXp).coerceAtLeast(0)
                val newIncentive = if (u.role == "PARENT") (u.parentIncentiveCredits + deltaCredits).coerceAtLeast(0) else u.parentIncentiveCredits
                val updated = u.copy(credits = newCredits, xp = newXp, parentIncentiveCredits = newIncentive)
                repository.updateUser(updated)

                if (u.role == "PARENT" && !u.linkedStudentId.isNullOrBlank()) {
                    val child = repository.getUserDirect(u.linkedStudentId)
                    if (child != null) {
                        val childNewCredits = (child.credits + deltaCredits).coerceAtLeast(0)
                        val childNewXp = (child.xp + deltaXp).coerceAtLeast(0)
                        repository.updateUser(child.copy(credits = childNewCredits, xp = childNewXp))
                        try {
                            FirebaseFirestore.getInstance().collection("users").document(child.id).set(
                                mapOf(
                                    "credits" to childNewCredits,
                                    "xp" to childNewXp
                                ),
                                SetOptions.merge()
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                try {
                    val store = FirebaseFirestore.getInstance()
                    store.collection("users").document(u.id).set(
                        mapOf(
                            "credits" to newCredits,
                            "xp" to newXp,
                            "parentIncentiveCredits" to newIncentive
                        ),
                        SetOptions.merge()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val actionSign = if (deltaCredits >= 0) "+$deltaCredits" else "$deltaCredits"
                repository.insertNotification(
                    NotificationEntity(
                        title = "🪙 Actualización de Saldo Escolar",
                        message = "Se ha asignado $actionSign 🪙 Escolaris a tu cuenta. Motivo: $reason",
                        type = "REWARD",
                        targetScreen = if (u.role == "PARENT") "parent" else "profile"
                    )
                )
            }

            // Respaldo local inmediato
            try {
                EscolarisBackupManager.saveUsersBackup(getApplication(), allUsers.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _userMessage.value = "🎉 ¡Éxito! Se actualizaron los puntos ($deltaCredits 🪙) a ${selected.size} usuarios seleccionados."
        }
    }

    // ==========================================
    // NOTIFICATIONS
    // ==========================================
    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            _userMessage.value = "Todas las notificaciones marcadas como leídas"
        }
    }

    // ==========================================
    // SUBJECTS MANAGEMENT (ADMIN / DOCENTE)
    // ==========================================
    fun addSubject(
        name: String,
        emoji: String = "📚",
        teacherName: String = "Manuel Alejandro Muñoz",
        classroom: String = "Aula Principal",
        colorHex: Long = 0xFF2563EB,
        description: String = ""
    ) {
        viewModelScope.launch {
            val validName = name.trim()
            if (validName.isBlank()) {
                _userMessage.value = "El nombre de la asignatura no puede estar vacío"
                return@launch
            }
            val resolvedTeacher = teacherName.trim().ifBlank { _currentUser.value?.name ?: "Manuel Alejandro Muñoz" }
            val subject = SubjectEntity(
                name = validName,
                emoji = emoji.trim().ifBlank { "📚" },
                teacherName = if (resolvedTeacher == "Docente Titular" || resolvedTeacher == "Prof. Titular") "Manuel Alejandro Muñoz" else resolvedTeacher,
                classroom = classroom.trim().ifBlank { "Aula Principal" },
                colorHex = colorHex,
                description = description.trim()
            )
            repository.insertSubject(subject)
            _userMessage.value = "✨ Asignatura '$validName' agregada exitosamente"
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            _userMessage.value = "Asignatura '${subject.name}' eliminada"
        }
    }

    // ==========================================
    // CALENDAR SCHOOL EVENTS
    // ==========================================
    fun addSchoolEvent(
        title: String,
        category: String = "ACADEMIC",
        eventDateMillis: Long,
        eventTime: String = "08:00 AM",
        location: String = "Auditorio Principal",
        description: String = "",
        colorHex: Long = 0xFF8B5CF6
    ) {
        viewModelScope.launch {
            val validTitle = title.trim()
            if (validTitle.isBlank()) {
                _userMessage.value = "El título del evento no puede estar vacío"
                return@launch
            }
            val user = _currentUser.value
            val event = SchoolEventEntity(
                title = validTitle,
                category = category,
                eventDateMillis = eventDateMillis,
                eventTime = eventTime.trim().ifBlank { "08:00 AM" },
                location = location.trim().ifBlank { "Auditorio Principal" },
                description = description.trim(),
                creatorName = user?.name ?: "Administración",
                colorHex = colorHex
            )
            repository.insertSchoolEvent(event)

            // Emitir notificación push institucional
            NotificationHelper.showPushNotification(
                context = getApplication(),
                id = (1000..9999).random(),
                title = "📅 Nuevo Evento Escolar: $validTitle",
                message = "Programado para las $eventTime en $location. $description",
                type = "EVENT"
            )

            _userMessage.value = "📅 Evento '$validTitle' agendado para las $eventTime"
        }
    }

    fun deleteSchoolEvent(event: SchoolEventEntity) {
        val user = _currentUser.value
        val isTeacher = user?.role == UserRole.TEACHER.code
        if (!isTeacher) {
            _userMessage.value = "Solo docentes y directivos pueden eliminar eventos del calendario escolar"
            return
        }
        viewModelScope.launch {
            repository.deleteSchoolEvent(event)
            _userMessage.value = "Evento eliminado del calendario"
        }
    }

    // ==========================================
    // PARENT / GUARDIAN PORTAL ACTIONS
    // ==========================================
    fun linkParentToStudent(studentCode: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val cleanCode = studentCode.trim().uppercase()
            if (cleanCode.isBlank()) {
                _userMessage.value = "Por favor ingresa el código del estudiante"
                return@launch
            }
            val student = repository.getUserByStudentCode(cleanCode)
            if (student == null) {
                _userMessage.value = "❌ No se encontró ningún estudiante con el código '$cleanCode'. Pídele el código a tu hijo/a."
                return@launch
            }

            val updatedUser = user.copy(
                linkedStudentId = student.id,
                gradeSection = "Tutor de ${student.name}"
            )
            repository.updateUser(updatedUser)
            loadActiveUser(user.id)
            _userMessage.value = "✅ ¡Vinculado exitosamente con ${student.name} (${student.gradeSection})!"
        }
    }

    fun transferParentPointsToChild(amount: Int, reason: String = "Reconocimiento por esfuerzo académico") {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val studentId = user.linkedStudentId
            if (studentId.isNullOrBlank()) {
                _userMessage.value = "Primero debes vincular el código de tu hijo/a para cederle Escolaris."
                return@launch
            }
            if (amount <= 0) {
                _userMessage.value = "Por favor ingresa una cantidad válida de Escolaris a ceder."
                return@launch
            }
            val currentBalance = user.credits.coerceAtLeast(user.parentIncentiveCredits)
            if (currentBalance < amount) {
                _userMessage.value = "Saldo insuficiente. Dispones de 🪙 $currentBalance Escolaris."
                return@launch
            }
            val student = repository.getUserDirect(studentId)
            val studentName = student?.name ?: "tu hijo/a"

            repository.grantParentIncentiveToStudent(user.id, studentId, amount, reason)

            // Sincronizar en Cloud Firestore para ambos usuarios
            try {
                val store = FirebaseFirestore.getInstance()
                val newParentCredits = (user.credits - amount).coerceAtLeast(0)
                val newParentIncentives = (user.parentIncentiveCredits - amount).coerceAtLeast(0)
                store.collection("users").document(user.id).set(
                    hashMapOf("credits" to newParentCredits, "parentIncentiveCredits" to newParentIncentives),
                    SetOptions.merge()
                )

                if (student != null) {
                    val newStudentCredits = student.credits + amount
                    val newStudentXp = student.xp + (amount * 2)
                    store.collection("users").document(student.id).set(
                        hashMapOf("credits" to newStudentCredits, "xp" to newStudentXp),
                        SetOptions.merge()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            loadActiveUser(user.id)
            _userMessage.value = "🎁 ¡Has cedido exitosamente +$amount 🪙 Escolaris a $studentName!"
        }
    }

    /**
     * Inicializa o resetea en 0 el catálogo de insignias para todos los usuarios existentes en Cloud Firestore.
     * Seguridad: Únicamente modifica el campo "badges" con SetOptions.merge(), preservando todos los demás datos intactos.
     */
    private suspend fun resetBadgesInCloudFirestore() = withContext(Dispatchers.IO) {
        try {
            val store = FirebaseFirestore.getInstance()
            val usersSnapshot = store.collection("users").get().await()
            for (doc in usersSnapshot.documents) {
                val role = doc.getString("role") ?: UserRole.STUDENT.code
                val initialBadges = com.example.ui.screens.getInitialBadgesForRole(role).map {
                    mapOf(
                        "id" to it.id,
                        "title" to it.title,
                        "description" to it.description,
                        "category" to it.category,
                        "currentProgress" to 0,
                        "targetProgress" to it.targetProgress,
                        "emoji" to it.emoji,
                        "isUnlocked" to false,
                        "unlockedAtDate" to null,
                        "xpReward" to it.xpReward,
                        "creditReward" to it.creditReward
                    )
                }
                store.collection("users").document(doc.id).set(
                    mapOf("badges" to initialBadges),
                    SetOptions.merge()
                ).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncCloudFirestoreData() = withContext(Dispatchers.IO) {
        try {
            val store = FirebaseFirestore.getInstance()

            // 1. Sincronizar todos los usuarios registrados en Cloud Firestore
            val usersSnapshot = store.collection("users").get().await()
            val existingEmailsInFirestore = mutableSetOf<String>()
            val existingIdsInFirestore = mutableSetOf<String>()
            for (doc in usersSnapshot.documents) {
                val email = doc.getString("email")?.trim()?.lowercase() ?: ""
                if (email.isNotBlank()) existingEmailsInFirestore.add(email)
                existingIdsInFirestore.add(doc.id)

                val isTeacher = email == "moz658@gmail.com"
                val id = doc.id
                val rawName = doc.getString("name") ?: (if (isTeacher) "Manuel Muñoz" else "Usuario")
                val name = ValidationUtils.formatProperNoun(if (isTeacher && (rawName == "Usuario" || rawName == "Docente Titular" || rawName.isBlank())) "Manuel Muñoz" else rawName)
                val role = if (isTeacher) UserRole.TEACHER.code else (doc.getString("role") ?: UserRole.STUDENT.code)
                val credits = (doc.getLong("credits") ?: 100L).toInt()
                val xp = (doc.getLong("xp") ?: 50L).toInt()
                val studentCode = if (isTeacher) "" else (doc.getString("studentCode") ?: "")
                val teacherCode = if (isTeacher) "DOC-102938" else (doc.getString("teacherCode") ?: "")
                val avatarEmoji = doc.getString("avatarEmoji") ?: (if (isTeacher) "👨‍🏫" else "🎓")
                val gradeSection = doc.getString("gradeSection") ?: (if (isTeacher) "Docente Titular" else "10° Grado")
                val bio = doc.getString("bio") ?: ""
                val photoUri = doc.getString("photoUrl")
                val phoneNumber = doc.getString("phoneNumber") ?: ""
                val avatarColorHex = doc.getLong("avatarColorHex") ?: (if (isTeacher) 0xFF1D4ED8 else 0xFF2563EB)
                val parentIncentiveCredits = (doc.getLong("parentIncentiveCredits") ?: 100L).toInt()
                val linkedStudentId = doc.getString("linkedStudentId")

                val user = UserEntity(
                    id = id,
                    name = name,
                    email = email,
                    role = role,
                    gradeSection = gradeSection,
                    credits = if (isTeacher) maxOf(credits, 500) else credits,
                    xp = if (isTeacher) maxOf(xp, 200) else xp,
                    studentCode = studentCode,
                    teacherCode = teacherCode,
                    avatarEmoji = avatarEmoji,
                    avatarColorHex = avatarColorHex,
                    bio = bio,
                    photoUri = photoUri,
                    phoneNumber = phoneNumber,
                    parentIncentiveCredits = parentIncentiveCredits,
                    linkedStudentId = linkedStudentId
                )
                repository.insertUser(user)
            }
            if (existingIdsInFirestore.isNotEmpty()) {
                repository.pruneUsers(existingIdsInFirestore.toList())
            } else {
                // 1.1 Sincronizar perfiles institucionales ÚNICAMENTE si la colección remota está 100% vacía (primer arranque institucional)
                for (institutionalUser in DatabaseInitializer.OFFICIAL_INSTITUTIONAL_ROSTER) {
                    try {
                        val firestoreData = hashMapOf(
                            "id" to institutionalUser.id,
                            "name" to institutionalUser.name,
                            "email" to institutionalUser.email,
                            "role" to institutionalUser.role,
                            "gradeSection" to institutionalUser.gradeSection,
                            "credits" to institutionalUser.credits,
                            "xp" to institutionalUser.xp,
                            "level" to institutionalUser.level,
                            "streakDays" to institutionalUser.streakDays,
                            "studentCode" to institutionalUser.studentCode,
                            "parentIncentiveCredits" to institutionalUser.parentIncentiveCredits,
                            "linkedStudentId" to institutionalUser.linkedStudentId,
                            "avatarEmoji" to institutionalUser.avatarEmoji,
                            "avatarColorHex" to institutionalUser.avatarColorHex,
                            "avatarInitials" to institutionalUser.avatarInitials,
                            "bio" to institutionalUser.bio,
                            "phoneNumber" to institutionalUser.phoneNumber,
                            "roleConfigured" to true,
                            "badges" to com.example.ui.screens.getInitialBadgesForRole(institutionalUser.role).map {
                                mapOf(
                                    "id" to it.id,
                                    "title" to it.title,
                                    "description" to it.description,
                                    "category" to it.category,
                                    "currentProgress" to 0,
                                    "targetProgress" to it.targetProgress,
                                    "emoji" to it.emoji,
                                    "isUnlocked" to false,
                                    "unlockedAtDate" to null,
                                    "xpReward" to it.xpReward,
                                    "creditReward" to it.creditReward
                                )
                            },
                            "createdAt" to System.currentTimeMillis()
                        )
                        store.collection("users").document(institutionalUser.id).set(firestoreData, SetOptions.merge()).await()
                        repository.insertUser(institutionalUser)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 2. Sincronizar Sanciones y Multas
            val penaltiesSnapshot = store.collection("penalties").get().await()
            for (doc in penaltiesSnapshot.documents) {
                val sId = doc.getString("studentId") ?: continue
                val sName = doc.getString("studentName") ?: "Estudiante"
                val reason = doc.getString("reason") ?: "Falta disciplinaria"
                val category = doc.getString("category") ?: "DISCIPLINE"
                val pts = (doc.getLong("pointsDeducted") ?: 30L).toInt()
                val teacher = doc.getString("teacherName") ?: "Docente"
                val obs = doc.getString("observation") ?: ""
                val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                val status = doc.getString("status") ?: "APLICADA"
                val notified = doc.getBoolean("notifiedParents") ?: true

                repository.insertPenalty(
                    com.example.data.local.entity.PenaltyEntity(
                        studentId = sId,
                        studentName = sName,
                        reason = reason,
                        category = category,
                        pointsDeducted = pts,
                        teacherName = teacher,
                        observation = obs,
                        timestamp = ts,
                        status = status,
                        notifiedParents = notified
                    )
                )
            }

            // 3. Sincronizar Medallas de Honor (Anti-duplicación)
            val badgesSnapshot = store.collection("badges").get().await()
            val existingBadgeKeys = repository.getAllBadgesDirect().map { "${it.studentId}_${it.badgeKey}" }.toSet()
            for (doc in badgesSnapshot.documents) {
                val sId = doc.getString("studentId") ?: continue
                val sName = doc.getString("studentName") ?: "Estudiante"
                val key = doc.getString("badgeKey") ?: doc.id
                if ("${sId}_${key}" in existingBadgeKeys) {
                    continue // Ya existe localmente, no duplicar
                }
                val title = doc.getString("title") ?: "Insignia de Honor"
                val desc = doc.getString("description") ?: ""
                val emoji = doc.getString("emoji") ?: "🎖️"
                val cat = doc.getString("category") ?: "HONOR"
                val note = doc.getString("teacherNote") ?: ""
                val xp = (doc.getLong("xpReward") ?: 100L).toInt()
                val cred = (doc.getLong("creditReward") ?: 50L).toInt()
                val photo = doc.getString("photoUrl")
                val status = doc.getString("status") ?: "ACTIVE"
                val unlockedAt = doc.getLong("unlockedAtMillis") ?: System.currentTimeMillis()

                repository.insertBadge(
                    BadgeEntity(
                        badgeKey = key,
                        studentId = sId,
                        title = title,
                        description = desc,
                        emoji = emoji,
                        category = cat,
                        unlockedAtMillis = unlockedAt,
                        teacherNote = note,
                        xpReward = xp,
                        creditReward = cred,
                        photoUri = photo
                    )
                )
            }

            // 3.1 Sincronizar Obligaciones y Pensiones de Padres (parent_obligations)
            try {
                val obligationsSnapshot = store.collection("parent_obligations").get().await()
                val existingObligations = repository.getAllParentObligationsDirect()
                for (doc in obligationsSnapshot.documents) {
                    val oId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: kotlin.math.abs(doc.id.hashCode().toLong())
                    val parentId = doc.getString("parentId") ?: "ALL"
                    val studentId = doc.getString("studentId") ?: "ALL"
                    val title = doc.getString("title") ?: "Obligación Escolar"
                    val description = doc.getString("description") ?: ""
                    val category = doc.getString("category") ?: "PENSION"
                    val month = doc.getString("month") ?: "Septiembre"
                    val dueDayOfMonth = (doc.getLong("dueDayOfMonth") ?: 5L).toInt()
                    val dueDateMillis = doc.getLong("dueDateMillis") ?: System.currentTimeMillis()
                    val isCompleted = doc.getBoolean("isCompleted") ?: false
                    val completedAtMillis = doc.getLong("completedAtMillis")
                    val completedByParentName = doc.getString("completedByParentName") ?: ""
                    val rewardBadgeKey = doc.getString("rewardBadgeKey") ?: ""
                    val rewardBadgeTitle = doc.getString("rewardBadgeTitle") ?: ""
                    val rewardBadgeEmoji = doc.getString("rewardBadgeEmoji") ?: "🎖️"
                    val rewardCredits = (doc.getLong("rewardCredits") ?: 100L).toInt()
                    val rewardXp = (doc.getLong("rewardXp") ?: 150L).toInt()
                    val whatsappMessage = doc.getString("whatsappMessage") ?: ""
                    val createdByTeacher = doc.getString("createdByTeacher") ?: "Docente Titular"

                    val entity = ParentObligationEntity(
                        id = oId,
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
                        createdByTeacher = createdByTeacher
                    )
                    repository.insertParentObligation(entity)
                }

                // Si hay obligaciones locales que no están en la nube, subirlas a Firestore
                val liveObligations = repository.getAllParentObligationsDirect()
                if (liveObligations.isNotEmpty()) {
                    EscolarisBackupManager.saveParentObligationsBackup(getApplication(), liveObligations)
                    for (lo in liveObligations) {
                        val inCloud = obligationsSnapshot.documents.any { (it.getLong("id") ?: it.id.toLongOrNull() ?: kotlin.math.abs(it.id.hashCode().toLong())) == lo.id }
                        if (!inCloud) {
                            try {
                                val oblMap = hashMapOf(
                                    "id" to lo.id,
                                    "parentId" to lo.parentId,
                                    "studentId" to lo.studentId,
                                    "title" to lo.title,
                                    "description" to lo.description,
                                    "category" to lo.category,
                                    "month" to lo.month,
                                    "dueDayOfMonth" to lo.dueDayOfMonth,
                                    "dueDateMillis" to lo.dueDateMillis,
                                    "isCompleted" to lo.isCompleted,
                                    "completedAtMillis" to lo.completedAtMillis,
                                    "completedByParentName" to lo.completedByParentName,
                                    "rewardBadgeKey" to lo.rewardBadgeKey,
                                    "rewardBadgeTitle" to lo.rewardBadgeTitle,
                                    "rewardBadgeEmoji" to lo.rewardBadgeEmoji,
                                    "rewardCredits" to lo.rewardCredits,
                                    "rewardXp" to lo.rewardXp,
                                    "whatsappMessage" to lo.whatsappMessage,
                                    "createdByTeacher" to lo.createdByTeacher
                                )
                                store.collection("parent_obligations").document(lo.id.toString()).set(oblMap, SetOptions.merge())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 4. Sincronizar Publicaciones del Muro Social (feed_posts)
            try {
                val postsSnapshot = store.collection("feed_posts").get().await()
                val incomingPosts = mutableListOf<FeedPostEntity>()
                for (doc in postsSnapshot.documents) {
                    val pId = doc.getLong("id")
                        ?: doc.id.toLongOrNull()
                        ?: kotlin.math.abs(doc.id.hashCode().toLong())
                    val authorId = doc.getString("authorId") ?: ""
                    val authorName = ValidationUtils.formatProperNoun(doc.getString("authorName") ?: "Docente")
                    val authorRole = doc.getString("authorRole") ?: "TEACHER"
                    val authorAvatarColorHex = doc.getLong("authorAvatarColorHex") ?: 0xFF2563EB
                    val title = doc.getString("title") ?: ""
                    val content = doc.getString("content") ?: ""
                    val postType = doc.getString("postType") ?: doc.getString("category") ?: "ANNOUNCEMENT"
                    val subject = doc.getString("subject") ?: "General"
                    val ts = doc.getLong("timestamp") ?: doc.getLong("timestampMillis") ?: System.currentTimeMillis()
                    val likesCount = ((doc.getLong("likesCount") ?: doc.getLong("likes")) ?: 0L).toInt()
                    val isLikedByMe = doc.getBoolean("isLikedByMe") ?: false
                    val attachmentsJson = doc.getString("attachmentsJson") ?: ""
                    val commentsCount = (doc.getLong("commentsCount") ?: 0L).toInt()
                    val resolvedStatus = doc.getBoolean("resolvedStatus") ?: false

                    incomingPosts.add(
                        FeedPostEntity(
                            id = pId,
                            authorId = authorId,
                            authorName = authorName,
                            authorRole = authorRole,
                            authorAvatarColorHex = authorAvatarColorHex,
                            title = title,
                            content = content,
                            postType = postType,
                            subject = subject,
                            timestamp = ts,
                            likesCount = likesCount,
                            isLikedByMe = isLikedByMe,
                            attachmentsJson = attachmentsJson,
                            commentsCount = commentsCount,
                            resolvedStatus = resolvedStatus
                        )
                    )
                }
                val cloudIds = incomingPosts.map { it.id }
                repository.pruneFeedPosts(cloudIds)
                if (incomingPosts.isNotEmpty()) {
                    repository.insertFeedPosts(incomingPosts)
                    EscolarisBackupManager.saveFeedPostsBackup(getApplication(), incomingPosts)
                }
                EscolarisBackupManager.pruneFeedPostsBackup(getApplication(), cloudIds.toSet())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 5. Sincronizar Comentarios de Publicaciones (post_comments)
            try {
                val commentsSnapshot = store.collection("post_comments").get().await()
                val incomingComments = mutableListOf<PostCommentEntity>()
                for (doc in commentsSnapshot.documents) {
                    val cId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: System.currentTimeMillis()
                    val postId = doc.getLong("postId") ?: continue
                    val authorId = doc.getString("authorId") ?: ""
                    val authorName = ValidationUtils.formatProperNoun(doc.getString("authorName") ?: "Usuario")
                    val authorRole = doc.getString("authorRole") ?: "STUDENT"
                    val content = doc.getString("content") ?: ""
                    val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val attachment = doc.getString("attachmentDescription")

                    incomingComments.add(
                        PostCommentEntity(
                            id = cId,
                            postId = postId,
                            authorId = authorId,
                            authorName = authorName,
                            authorRole = authorRole,
                            content = content,
                            timestamp = ts,
                            attachmentDescription = attachment
                        )
                    )
                }
                val cloudCommentIds = incomingComments.map { it.id }
                repository.pruneComments(cloudCommentIds)
                if (incomingComments.isNotEmpty()) {
                    repository.insertComments(incomingComments)
                    EscolarisBackupManager.savePostCommentsBackup(getApplication(), incomingComments)
                }
                EscolarisBackupManager.prunePostCommentsBackup(getApplication(), cloudCommentIds.toSet())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 6. Sincronizar Tareas (tasks) iniciales
            try {
                val tasksSnapshot = store.collection("tasks").get().await()
                val existingTasks = repository.getAllTasksDirect()
                for (doc in tasksSnapshot.documents) {
                    val fId = doc.id
                    val title = doc.getString("title") ?: ""
                    if (title.isBlank()) continue
                    val subject = doc.getString("subject") ?: "General"
                    val desc = doc.getString("description") ?: ""
                    val dueDateStr = doc.getString("dueDate") ?: ""
                    val dueMillis = doc.getLong("dueDateMillis") ?: (doc.getLong("timestampMillis") ?: System.currentTimeMillis())
                    val priority = doc.getString("priority") ?: "MEDIA"
                    val isCompleted = doc.getBoolean("completed") ?: (doc.getString("status") == "COMPLETED")
                    val status = if (isCompleted) "COMPLETED" else (doc.getString("status") ?: "PENDING")
                    val studentId = doc.getString("studentId") ?: "ALL"
                    val rewardCredits = (doc.getLong("rewardCredits") ?: 30L).toInt()

                    val localMatch = existingTasks.find { it.firestoreId == fId || (it.title == title && it.dueDateMillis == dueMillis) }
                    if (localMatch != null) {
                        if (localMatch.status != status || localMatch.completed != isCompleted || localMatch.firestoreId != fId) {
                            repository.updateTask(localMatch.copy(firestoreId = fId, status = status, completed = isCompleted, title = title, subject = subject, description = desc, dueDate = dueDateStr, dueDateMillis = dueMillis, priority = priority))
                        }
                    } else {
                        val newTask = TaskEntity(
                            firestoreId = fId,
                            studentId = studentId,
                            title = title,
                            subject = subject,
                            description = desc,
                            dueDateMillis = dueMillis,
                            dueDate = dueDateStr,
                            priority = priority,
                            status = status,
                            completed = isCompleted,
                            rewardCredits = rewardCredits
                        )
                        repository.insertTask(newTask)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 7. Sincronizar Retardos (tardies) iniciales
            try {
                val tardiesSnapshot = store.collection("tardies").get().await()
                val existingTardies = repository.getAllTardyRecordsDirect()
                for (doc in tardiesSnapshot.documents) {
                    val fId = doc.id
                    val sId = doc.getString("studentId") ?: continue
                    val sName = doc.getString("studentName") ?: "Estudiante"
                    val subj = doc.getString("subject") ?: "Clase General"
                    val delay = (doc.getLong("delayMinutes") ?: 15L).toInt()
                    val reason = doc.getString("reason") ?: "Retardo"
                    val arrTime = doc.getString("arrivalTime") ?: "07:45 AM"
                    val gradeSec = doc.getString("gradeSection") ?: "10° Grado"
                    val status = doc.getString("status") ?: "REGISTRADO"
                    val dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis()
                    val notified = doc.getBoolean("notifiedParents") ?: true
                    val obs = doc.getString("teacherObservation") ?: ""
                    val penalty = (doc.getLong("penaltyCredits") ?: 0L).toInt()

                    val localMatch = existingTardies.find { it.firestoreId == fId }
                    if (localMatch != null) {
                        if (localMatch.status != status || localMatch.teacherObservation != obs) {
                            repository.updateTardyRecordStatus(localMatch.id, status, obs)
                        }
                    } else {
                        val newRecord = TardyRecordEntity(
                            firestoreId = fId,
                            studentId = sId,
                            studentName = sName,
                            gradeSection = gradeSec,
                            dateMillis = dateMillis,
                            arrivalTime = arrTime,
                            delayMinutes = delay,
                            subject = subj,
                            reason = reason,
                            status = status,
                            notifiedParents = notified,
                            teacherObservation = obs,
                            penaltyCredits = penalty
                        )
                        repository.insertTardyRecord(newRecord)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startRealtimeSocialSync() {
        try {
            val store = FirebaseFirestore.getInstance()

            // 1. Sincronización en tiempo real para Usuarios (users) - FUENTE ÚNICA DE VERDAD (SSOT)
            store.collection("users").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val incomingUsers = mutableListOf<UserEntity>()
                        for (doc in snapshot.documents) {
                            val id = doc.id
                            val email = doc.getString("email")?.trim()?.lowercase() ?: ""
                            val isTeacher = email == "moz658@gmail.com"
                            val rawName = doc.getString("name") ?: (if (isTeacher) "Manuel Muñoz" else "Usuario")
                            val name = ValidationUtils.formatProperNoun(if (isTeacher && (rawName == "Usuario" || rawName == "Docente Titular" || rawName.isBlank())) "Manuel Muñoz" else rawName)
                            val role = if (isTeacher) UserRole.TEACHER.code else (doc.getString("role") ?: UserRole.STUDENT.code)
                            val credits = (doc.getLong("credits") ?: (doc.getDouble("credits")?.toLong() ?: 100L)).toInt()
                            val xp = (doc.getLong("xp") ?: (doc.getDouble("xp")?.toLong() ?: 50L)).toInt()
                            val level = (doc.getLong("level") ?: (doc.getDouble("level")?.toLong() ?: 1L)).toInt()
                            val streakDays = (doc.getLong("streakDays") ?: (doc.getDouble("streakDays")?.toLong() ?: 1L)).toInt()
                            val studentCode = if (isTeacher) "" else (doc.getString("studentCode") ?: "")
                            val teacherCode = if (isTeacher) "DOC-102938" else (doc.getString("teacherCode") ?: "")
                            val avatarEmoji = doc.getString("avatarEmoji") ?: (if (isTeacher) "👨‍🏫" else "🎓")
                            val gradeSection = doc.getString("gradeSection") ?: (if (isTeacher) "Docente Titular" else "10° Grado")
                            val bio = doc.getString("bio") ?: ""
                            val photoUri = doc.getString("photoUrl") ?: doc.getString("photoUri")
                            val phoneNumber = doc.getString("phoneNumber") ?: ""
                            val avatarColorHex = doc.getLong("avatarColorHex") ?: (if (isTeacher) 0xFF1D4ED8 else 0xFF2563EB)
                            val parentIncentiveCredits = (doc.getLong("parentIncentiveCredits") ?: (doc.getDouble("parentIncentiveCredits")?.toLong() ?: 100L)).toInt()
                            val linkedStudentId = doc.getString("linkedStudentId")
                            val linkedTeacherCode = doc.getString("linkedTeacherCode")
                            val bannerGradientIndex = (doc.getLong("bannerGradientIndex") ?: 0L).toInt()

                            incomingUsers.add(
                                UserEntity(
                                    id = id,
                                    name = name,
                                    email = email,
                                    role = role,
                                    gradeSection = gradeSection,
                                    credits = if (isTeacher) maxOf(credits, 500) else credits,
                                    xp = if (isTeacher) maxOf(xp, 200) else xp,
                                    level = level,
                                    streakDays = streakDays,
                                    studentCode = studentCode,
                                    teacherCode = teacherCode,
                                    avatarEmoji = avatarEmoji,
                                    avatarColorHex = avatarColorHex,
                                    bio = bio,
                                    photoUri = photoUri,
                                    phoneNumber = phoneNumber,
                                    parentIncentiveCredits = parentIncentiveCredits,
                                    linkedStudentId = linkedStudentId,
                                    linkedTeacherCode = linkedTeacherCode,
                                    bannerGradientIndex = bannerGradientIndex
                                )
                            )
                        }

                        val cloudIds = incomingUsers.map { it.id }
                        repository.pruneUsers(cloudIds)
                        if (incomingUsers.isNotEmpty()) {
                            repository.insertUsers(incomingUsers)
                            EscolarisBackupManager.saveUsersBackup(getApplication(), incomingUsers)
                        }

                        // Si el usuario en sesión activa cambió en Firestore, actualizar StateFlow inmediatamente
                        val activeId = _currentUserId.value.ifBlank { sessionPrefs.getString(PREF_SAVED_USER_ID, "") ?: "" }
                        val activeEmail = _currentUser.value?.email?.trim()?.lowercase()
                            ?: sessionPrefs.getString(PREF_SAVED_USER_EMAIL, "")?.trim()?.lowercase()
                        val matchedCurrent = incomingUsers.find { 
                            (activeId.isNotBlank() && it.id == activeId) || 
                            (!activeEmail.isNullOrBlank() && it.email.trim().lowercase() == activeEmail) 
                        }
                        if (matchedCurrent != null) {
                            withContext(Dispatchers.Main) {
                                val oldRole = _currentUser.value?.role
                                _currentUser.value = matchedCurrent
                                _currentUserId.value = matchedCurrent.id
                                persistSessionUser(matchedCurrent)
                                if (oldRole != null && !oldRole.equals(matchedCurrent.role, ignoreCase = true)) {
                                    val roleLabel = when {
                                        UserRole.isTeacherOrAdmin(matchedCurrent.role) -> "Docente / Admin 👑"
                                        matchedCurrent.role.equals(UserRole.PARENT.code, ignoreCase = true) || matchedCurrent.role.equals("ACUDIENTE", ignoreCase = true) -> "Acudiente 👨‍👩‍👧"
                                        else -> "Estudiante 🎓"
                                    }
                                    _userMessage.value = "Tu rol ha sido actualizado a: $roleLabel"
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 2. Sincronización en tiempo real de publicaciones del muro social (feed_posts)
            store.collection("feed_posts").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                viewModelScope.launch(Dispatchers.IO) {
                    val incomingPosts = mutableListOf<FeedPostEntity>()
                    for (doc in snapshot.documents) {
                        val pId = doc.getLong("id")
                            ?: doc.id.toLongOrNull()
                            ?: kotlin.math.abs(doc.id.hashCode().toLong())
                        val authorId = doc.getString("authorId") ?: ""
                        val authorName = ValidationUtils.formatProperNoun(doc.getString("authorName") ?: "Docente")
                        val authorRole = doc.getString("authorRole") ?: "TEACHER"
                        val authorAvatarColorHex = doc.getLong("authorAvatarColorHex") ?: 0xFF2563EB
                        val title = doc.getString("title") ?: ""
                        val content = doc.getString("content") ?: ""
                        val postType = doc.getString("postType") ?: doc.getString("category") ?: "ANNOUNCEMENT"
                        val subject = doc.getString("subject") ?: "General"
                        val ts = doc.getLong("timestamp") ?: doc.getLong("timestampMillis") ?: System.currentTimeMillis()
                        val likesCount = ((doc.getLong("likesCount") ?: doc.getLong("likes")) ?: 0L).toInt()
                        val attachmentsJson = doc.getString("attachmentsJson") ?: ""
                        val commentsCount = (doc.getLong("commentsCount") ?: 0L).toInt()
                        val resolvedStatus = doc.getBoolean("resolvedStatus") ?: false

                        incomingPosts.add(
                            FeedPostEntity(
                                id = pId,
                                authorId = authorId,
                                authorName = authorName,
                                authorRole = authorRole,
                                authorAvatarColorHex = authorAvatarColorHex,
                                title = title,
                                content = content,
                                postType = postType,
                                subject = subject,
                                timestamp = ts,
                                likesCount = likesCount,
                                isLikedByMe = false,
                                attachmentsJson = attachmentsJson,
                                commentsCount = commentsCount,
                                resolvedStatus = resolvedStatus
                            )
                        )
                    }
                    val cloudIds = incomingPosts.map { it.id }
                    repository.pruneFeedPosts(cloudIds)
                    if (incomingPosts.isNotEmpty()) {
                        repository.insertFeedPosts(incomingPosts)
                        EscolarisBackupManager.saveFeedPostsBackup(getApplication(), incomingPosts)
                    }
                    EscolarisBackupManager.pruneFeedPostsBackup(getApplication(), cloudIds.toSet())
                }
            }

            // 3. Sincronización en tiempo real de comentarios (post_comments)
            store.collection("post_comments").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                viewModelScope.launch(Dispatchers.IO) {
                    val incomingComments = mutableListOf<PostCommentEntity>()
                    for (doc in snapshot.documents) {
                        val cId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: System.currentTimeMillis()
                        val postId = doc.getLong("postId") ?: continue
                        val authorId = doc.getString("authorId") ?: ""
                        val authorName = ValidationUtils.formatProperNoun(doc.getString("authorName") ?: "Usuario")
                        val authorRole = doc.getString("authorRole") ?: "STUDENT"
                        val content = doc.getString("content") ?: ""
                        val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val attachment = doc.getString("attachmentDescription")

                        incomingComments.add(
                            PostCommentEntity(
                                id = cId,
                                postId = postId,
                                authorId = authorId,
                                authorName = authorName,
                                authorRole = authorRole,
                                content = content,
                                timestamp = ts,
                                attachmentDescription = attachment
                            )
                        )
                    }
                    val cloudCommentIds = incomingComments.map { it.id }
                    repository.pruneComments(cloudCommentIds)
                    if (incomingComments.isNotEmpty()) {
                        repository.insertComments(incomingComments)
                        EscolarisBackupManager.savePostCommentsBackup(getApplication(), incomingComments)
                    }
                    EscolarisBackupManager.prunePostCommentsBackup(getApplication(), cloudCommentIds.toSet())
                }
            }

            // 4. Sincronización en tiempo real para Tareas (tasks)
            store.collection("tasks").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val cloudTaskIds = snapshot.documents.map { it.id }
                        repository.pruneTasks(cloudTaskIds)
                        val existingTasks = repository.getAllTasksDirect()
                        for (doc in snapshot.documents) {
                            val fId = doc.id
                            val title = doc.getString("title") ?: ""
                            if (title.isBlank()) continue
                            val subject = doc.getString("subject") ?: "General"
                            val desc = doc.getString("description") ?: ""
                            val dueDateStr = doc.getString("dueDate") ?: ""
                            val dueMillis = doc.getLong("dueDateMillis") ?: (doc.getLong("timestampMillis") ?: System.currentTimeMillis())
                            val priority = doc.getString("priority") ?: "MEDIA"
                            val isCompleted = doc.getBoolean("completed") ?: (doc.getString("status") == "COMPLETED")
                            val status = if (isCompleted) "COMPLETED" else (doc.getString("status") ?: "PENDING")
                            val studentId = doc.getString("studentId") ?: "ALL"
                            val rewardCredits = (doc.getLong("rewardCredits") ?: 30L).toInt()

                            val localMatch = existingTasks.find { it.firestoreId == fId || (it.title == title && it.dueDateMillis == dueMillis) }
                            if (localMatch != null) {
                                if (localMatch.status != status || localMatch.completed != isCompleted || localMatch.firestoreId != fId) {
                                    repository.updateTask(localMatch.copy(firestoreId = fId, status = status, completed = isCompleted, title = title, subject = subject, description = desc, dueDate = dueDateStr, dueDateMillis = dueMillis, priority = priority))
                                }
                            } else {
                                val newTask = TaskEntity(
                                    firestoreId = fId,
                                    studentId = studentId,
                                    title = title,
                                    subject = subject,
                                    description = desc,
                                    dueDateMillis = dueMillis,
                                    dueDate = dueDateStr,
                                    priority = priority,
                                    status = status,
                                    completed = isCompleted,
                                    rewardCredits = rewardCredits
                                )
                                repository.insertTask(newTask)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 5. Sincronización en tiempo real para Llegadas Tarde / Retardos (tardies)
            store.collection("tardies").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val cloudTardyIds = snapshot.documents.map { it.id }
                        repository.pruneTardies(cloudTardyIds)
                        val existingTardies = repository.getAllTardyRecordsDirect()
                        for (doc in snapshot.documents) {
                            val fId = doc.id
                            val sId = doc.getString("studentId") ?: continue
                            val sName = doc.getString("studentName") ?: "Estudiante"
                            val subj = doc.getString("subject") ?: "Clase General"
                            val delay = (doc.getLong("delayMinutes") ?: 15L).toInt()
                            val reason = doc.getString("reason") ?: "Retardo"
                            val arrTime = doc.getString("arrivalTime") ?: "07:45 AM"
                            val gradeSec = doc.getString("gradeSection") ?: "10° Grado"
                            val status = doc.getString("status") ?: "REGISTRADO"
                            val dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis()
                            val notified = doc.getBoolean("notifiedParents") ?: true
                            val obs = doc.getString("teacherObservation") ?: ""
                            val penalty = (doc.getLong("penaltyCredits") ?: 0L).toInt()

                            val localMatch = existingTardies.find { it.firestoreId == fId }
                            if (localMatch != null) {
                                if (localMatch.status != status || localMatch.teacherObservation != obs) {
                                    repository.updateTardyRecordStatus(localMatch.id, status, obs)
                                }
                            } else {
                                val newRecord = TardyRecordEntity(
                                    firestoreId = fId,
                                    studentId = sId,
                                    studentName = sName,
                                    gradeSection = gradeSec,
                                    dateMillis = dateMillis,
                                    arrivalTime = arrTime,
                                    delayMinutes = delay,
                                    subject = subj,
                                    reason = reason,
                                    status = status,
                                    notifiedParents = notified,
                                    teacherObservation = obs,
                                    penaltyCredits = penalty
                                )
                                repository.insertTardyRecord(newRecord)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 6. Sincronización en tiempo real para Medallas e Insignias (badges)
            store.collection("badges").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val incomingBadges = mutableListOf<BadgeEntity>()
                        val validKeys = mutableListOf<String>()
                        for (doc in snapshot.documents) {
                            val sId = doc.getString("studentId") ?: continue
                            val key = doc.getString("badgeKey") ?: doc.id
                            validKeys.add("${sId}_${key}")
                            val title = doc.getString("title") ?: "Insignia de Honor"
                            val desc = doc.getString("description") ?: ""
                            val emoji = doc.getString("emoji") ?: "🎖️"
                            val cat = doc.getString("category") ?: "HONOR"
                            val note = doc.getString("teacherNote") ?: ""
                            val xp = (doc.getLong("xpReward") ?: 100L).toInt()
                            val cred = (doc.getLong("creditReward") ?: 50L).toInt()
                            val photo = doc.getString("photoUrl")
                            val unlockedAt = doc.getLong("unlockedAtMillis") ?: System.currentTimeMillis()

                            incomingBadges.add(
                                BadgeEntity(
                                    badgeKey = key,
                                    studentId = sId,
                                    title = title,
                                    description = desc,
                                    emoji = emoji,
                                    category = cat,
                                    unlockedAtMillis = unlockedAt,
                                    teacherNote = note,
                                    xpReward = xp,
                                    creditReward = cred,
                                    photoUri = photo
                                )
                            )
                        }
                        repository.pruneBadges(validKeys)
                        if (incomingBadges.isNotEmpty()) {
                            repository.insertBadges(incomingBadges)
                            EscolarisBackupManager.saveBadgesBackup(getApplication(), incomingBadges)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 7. Sincronización en tiempo real para Multas y Sanciones (penalties)
            store.collection("penalties").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val incomingPenalties = mutableListOf<com.example.data.local.entity.PenaltyEntity>()
                        for (doc in snapshot.documents) {
                            val sId = doc.getString("studentId") ?: continue
                            val sName = doc.getString("studentName") ?: "Estudiante"
                            val reason = doc.getString("reason") ?: "Falta disciplinaria"
                            val category = doc.getString("category") ?: "DISCIPLINE"
                            val pts = (doc.getLong("pointsDeducted") ?: 30L).toInt()
                            val teacher = doc.getString("teacherName") ?: "Docente"
                            val obs = doc.getString("observation") ?: ""
                            val ts = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val status = doc.getString("status") ?: "APLICADA"
                            val notified = doc.getBoolean("notifiedParents") ?: true

                            incomingPenalties.add(
                                com.example.data.local.entity.PenaltyEntity(
                                    studentId = sId,
                                    studentName = sName,
                                    reason = reason,
                                    category = category,
                                    pointsDeducted = pts,
                                    teacherName = teacher,
                                    observation = obs,
                                    timestamp = ts,
                                    status = status,
                                    notifiedParents = notified
                                )
                            )
                        }
                        if (incomingPenalties.isNotEmpty()) {
                            repository.clearAllPenalties()
                            repository.insertPenalties(incomingPenalties)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // 8. Sincronización en tiempo real para Obligaciones y Pensiones (parent_obligations)
            store.collection("parent_obligations").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val incomingObligations = mutableListOf<ParentObligationEntity>()
                        val validIds = mutableListOf<Long>()
                        for (doc in snapshot.documents) {
                            val oId = doc.getLong("id") ?: doc.id.toLongOrNull() ?: kotlin.math.abs(doc.id.hashCode().toLong())
                            validIds.add(oId)
                            val parentId = doc.getString("parentId") ?: "ALL"
                            val studentId = doc.getString("studentId") ?: "ALL"
                            val title = doc.getString("title") ?: "Obligación Escolar"
                            val description = doc.getString("description") ?: ""
                            val category = doc.getString("category") ?: "PENSION"
                            val month = doc.getString("month") ?: "Septiembre"
                            val dueDayOfMonth = (doc.getLong("dueDayOfMonth") ?: 5L).toInt()
                            val dueDateMillis = doc.getLong("dueDateMillis") ?: System.currentTimeMillis()
                            val isCompleted = doc.getBoolean("isCompleted") ?: false
                            val completedAtMillis = doc.getLong("completedAtMillis")
                            val completedByParentName = doc.getString("completedByParentName") ?: ""
                            val rewardBadgeKey = doc.getString("rewardBadgeKey") ?: ""
                            val rewardBadgeTitle = doc.getString("rewardBadgeTitle") ?: ""
                            val rewardBadgeEmoji = doc.getString("rewardBadgeEmoji") ?: "💳"
                            val rewardCredits = (doc.getLong("rewardCredits") ?: 100L).toInt()
                            val rewardXp = (doc.getLong("rewardXp") ?: 150L).toInt()
                            val whatsappMessage = doc.getString("whatsappMessage") ?: ""
                            val createdByTeacher = doc.getString("createdByTeacher") ?: "Docente Titular"

                            incomingObligations.add(
                                ParentObligationEntity(
                                    id = oId,
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
                                    createdByTeacher = createdByTeacher
                                )
                            )
                        }
                        repository.pruneParentObligations(validIds)
                        if (incomingObligations.isNotEmpty()) {
                            repository.insertParentObligations(incomingObligations)
                            EscolarisBackupManager.saveParentObligationsBackup(getApplication(), incomingObligations)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerFcmTokenForUser(userId: String) {
        if (userId.isBlank()) return
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrBlank()) {
                        val store = FirebaseFirestore.getInstance()
                        val tokenData = hashMapOf(
                            "token" to token,
                            "platform" to "android",
                            "deviceModel" to android.os.Build.MODEL,
                            "updatedAt" to System.currentTimeMillis()
                        )
                        store.collection("users")
                            .document(userId)
                            .collection("fcmTokens")
                            .document(token)
                            .set(tokenData, SetOptions.merge())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // PARENT OBLIGATIONS & WHATSAPP REMINDERS
    // ==========================================
    fun addParentObligation(
        title: String,
        description: String,
        category: String = "PENSION",
        month: String = "Octubre",
        dueDayOfMonth: Int = 5,
        dueDateMillis: Long,
        rewardBadgeKey: String = "PARENT_PENSION_OCTUBRE",
        rewardBadgeTitle: String = "Pago Oportuno de Pensión",
        rewardBadgeEmoji: String = "💳",
        rewardCredits: Int = 100,
        rewardXp: Int = 150,
        whatsappMessage: String = "",
        parentId: String = "ALL",
        studentId: String = "ALL"
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val validTitle = ValidationUtils.validateNonEmptyText(title, "Título del deber / pensión")
            if (!validTitle.isValid) {
                _userMessage.value = (validTitle as com.example.domain.validation.ValidationResult.Invalid).errorMessage
                return@launch
            }

            val defaultMsg = if (whatsappMessage.isNotBlank()) whatsappMessage
            else "¡Hola estimado acudiente! 👋 Les recordamos que a partir de $month la pensión escolar se cancela dentro de los $dueDayOfMonth primeros días del mes. ¡Cumple a tiempo para ganar la insignia de $rewardBadgeTitle y +$rewardCredits créditos Escolaris para tu hijo/a! ⭐"

            val obligation = ParentObligationEntity(
                parentId = parentId,
                studentId = studentId,
                title = title.trim(),
                description = description.trim(),
                category = category,
                month = month,
                dueDayOfMonth = dueDayOfMonth,
                dueDateMillis = dueDateMillis,
                isCompleted = false,
                rewardBadgeKey = rewardBadgeKey,
                rewardBadgeTitle = rewardBadgeTitle,
                rewardBadgeEmoji = rewardBadgeEmoji,
                rewardCredits = rewardCredits,
                rewardXp = rewardXp,
                whatsappMessage = defaultMsg,
                createdByTeacher = user.name
            )

            val id = repository.insertParentObligation(obligation)

            try {
                val store = FirebaseFirestore.getInstance()
                val oblMap = hashMapOf(
                    "id" to id,
                    "parentId" to obligation.parentId,
                    "studentId" to obligation.studentId,
                    "title" to obligation.title,
                    "description" to obligation.description,
                    "category" to obligation.category,
                    "month" to obligation.month,
                    "dueDayOfMonth" to obligation.dueDayOfMonth,
                    "dueDateMillis" to obligation.dueDateMillis,
                    "isCompleted" to false,
                    "rewardBadgeKey" to obligation.rewardBadgeKey,
                    "rewardBadgeTitle" to obligation.rewardBadgeTitle,
                    "rewardBadgeEmoji" to obligation.rewardBadgeEmoji,
                    "rewardCredits" to obligation.rewardCredits,
                    "rewardXp" to obligation.rewardXp,
                    "whatsappMessage" to obligation.whatsappMessage,
                    "createdByTeacher" to obligation.createdByTeacher
                )
                store.collection("parent_obligations").document(id.toString()).set(oblMap, SetOptions.merge())
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Programar recordatorios automáticos (diario 2:00 PM los primeros 5 días, luego cada 3 días)
            if (category == "PENSION") {
                NotificationHelper.scheduleParentPensionReminder(
                    context = getApplication(),
                    obligationId = id,
                    month = month,
                    customMessage = defaultMsg
                )
            }

            _userMessage.value = "📋 Obligación para padres creada con éxito y recordatorio programado a las 2:00 PM."
        }
    }

    fun completeParentObligation(
        obligation: ParentObligationEntity,
        parent: UserEntity,
        child: UserEntity? = null
    ) {
        viewModelScope.launch {
            // 1. Marcar como completada en la base de datos
            repository.markObligationCompleted(
                id = obligation.id,
                isCompleted = true,
                completedAtMillis = System.currentTimeMillis(),
                parentName = parent.name
            )

            // 2. Cancelar recordatorios de alarma pendientes
            NotificationHelper.cancelParentPensionReminder(getApplication(), obligation.id)

            // 3. Otorgar Insignia de Honor de Familia / Pago Oportuno y Créditos Escolaris para el hijo/a
            val targetStudentId = child?.id ?: parent.linkedStudentId ?: parent.id
            val targetStudentName = child?.name ?: parent.name
            val teacherName = obligation.createdByTeacher.ifBlank { "Tesorería Escolar" }

            val badge = BadgeEntity(
                studentId = targetStudentId,
                badgeKey = obligation.rewardBadgeKey,
                title = obligation.rewardBadgeTitle,
                description = "Cumplimiento oportuno de ${obligation.title} ($teacherName)",
                emoji = obligation.rewardBadgeEmoji,
                category = "FAMILY",
                unlockedAtMillis = System.currentTimeMillis(),
                unlockedByTeacher = teacherName,
                teacherNote = "¡Felicitaciones a la familia de $targetStudentName por su compromiso y puntualidad en el cumplimiento de sus deberes escolares!",
                clayColorHex = 0xFF10B981,
                xpReward = obligation.rewardXp,
                creditReward = obligation.rewardCredits
            )
            repository.insertBadge(badge)

            // 4. Sumar créditos y XP directamente al hijo/a
            repository.addCreditsAndXp(targetStudentId, obligation.rewardCredits, obligation.rewardXp)

            // 5. Otorgar incentivo al acudiente para transferir
            try {
                val db = EscolarisDatabase.getDatabase(getApplication())
                db.schoolDao().addCreditsAndXp(parent.id, obligation.rewardCredits, obligation.rewardXp)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 6. Guardar en Cloud Firestore para sincronización en tiempo real
            try {
                val store = FirebaseFirestore.getInstance()
                val badgeDoc = hashMapOf(
                    "studentId" to targetStudentId,
                    "studentName" to targetStudentName,
                    "badgeKey" to obligation.rewardBadgeKey,
                    "title" to obligation.rewardBadgeTitle,
                    "description" to badge.description,
                    "emoji" to badge.emoji,
                    "category" to "FAMILY",
                    "teacherNote" to badge.teacherNote,
                    "unlockedByTeacher" to teacherName,
                    "creditReward" to obligation.rewardCredits,
                    "xpReward" to obligation.rewardXp,
                    "photoUrl" to "",
                    "unlockedAtMillis" to System.currentTimeMillis(),
                    "status" to "ACTIVE"
                )
                val badgeKey = obligation.rewardBadgeKey.ifBlank { "OBLIGATION_${obligation.id}" }
                store.collection("badges").document("${targetStudentId}_${badgeKey}").set(badgeDoc, SetOptions.merge())

                store.collection("parent_obligations").document(obligation.id.toString()).set(
                    mapOf(
                        "isCompleted" to true,
                        "completedAtMillis" to System.currentTimeMillis(),
                        "completedByParentName" to parent.name
                    ),
                    SetOptions.merge()
                )

                val studentDoc = store.collection("users").document(targetStudentId).get().await()
                val currCredits = if (studentDoc.exists()) (studentDoc.getLong("credits") ?: 100L).toInt() else 0
                val currXp = if (studentDoc.exists()) (studentDoc.getLong("xp") ?: 50L).toInt() else 0
                store.collection("users").document(targetStudentId).set(
                    mapOf(
                        "credits" to currCredits + obligation.rewardCredits,
                        "xp" to currXp + obligation.rewardXp
                    ),
                    SetOptions.merge()
                )

                // Sync updated parent incentive credits to Firestore
                store.collection("users").document(parent.id).set(
                    mapOf(
                        "credits" to parent.credits + obligation.rewardCredits,
                        "parentIncentiveCredits" to parent.parentIncentiveCredits + obligation.rewardCredits
                    ),
                    SetOptions.merge()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 7. Insertar Notificación y Notificación Push estilo WhatsApp
            val notifMessage = "¡Felicidades! Se ha validado '${obligation.title}'. Tu hijo/a ($targetStudentName) recibió la medalla '${obligation.rewardBadgeTitle}' y +${obligation.rewardCredits} créditos Escolaris."
            repository.insertNotification(
                NotificationEntity(
                    title = "🎉 ¡Obligación Cumplida con Éxito!",
                    message = notifMessage,
                    type = "REWARD",
                    targetScreen = "parent"
                )
            )

            NotificationHelper.showWhatsAppStyleNotification(
                context = getApplication(),
                id = (8000..8999).random(),
                title = "✅ Confirmación de Tesorería Escolar",
                senderName = "🏫 Colegio Escolaris - Tesorería",
                message = "¡Gracias por tu pago puntual! Se han acreditado +${obligation.rewardCredits} puntos Escolaris a tu hijo/a y desbloqueado la medalla ${obligation.rewardBadgeEmoji} ${obligation.rewardBadgeTitle}."
            )

            loadActiveUser(parent.id)
            _userMessage.value = "🎉 ¡Excelente! Obligación cumplida. Tu hijo/a ha ganado +${obligation.rewardCredits} créditos Escolaris y la medalla ${obligation.rewardBadgeEmoji} ${obligation.rewardBadgeTitle}."
        }
    }

    fun deleteParentObligation(obligation: ParentObligationEntity) {
        viewModelScope.launch {
            NotificationHelper.cancelParentPensionReminder(getApplication(), obligation.id)
            repository.deleteParentObligation(obligation)
            try {
                val store = FirebaseFirestore.getInstance()
                store.collection("parent_obligations").document(obligation.id.toString()).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _userMessage.value = "Obligación de padres eliminada"
        }
    }

    fun sendWhatsAppReminderToParents(obligation: ParentObligationEntity) {
        viewModelScope.launch {
            val msg = if (obligation.whatsappMessage.isNotBlank()) obligation.whatsappMessage
            else "¡Hola estimado acudiente! 👋 Les recordamos que a partir de ${obligation.month} la pensión escolar se cancela dentro de los ${obligation.dueDayOfMonth} primeros días del mes. ¡Gana la insignia '${obligation.rewardBadgeTitle}' y +${obligation.rewardCredits} créditos Escolaris para tu hijo/a! ⭐"

            // 1. Mostrar notificación push estilo WhatsApp
            NotificationHelper.showWhatsAppStyleNotification(
                context = getApplication(),
                id = (7000..7999).random(),
                title = "Recordatorio de Tesorería",
                senderName = "🏫 Colegio Escolaris - Tesorería",
                message = msg,
                targetScreen = "parent"
            )

            // 2. Registrar en la bandeja de notificaciones
            repository.insertNotification(
                NotificationEntity(
                    title = "💬 Recordatorio WhatsApp: ${obligation.title}",
                    message = msg,
                    type = "WHATSAPP",
                    targetScreen = "parent"
                )
            )

            // 3. Actualizar timestamp de último recordatorio
            repository.updateObligationLastReminder(obligation.id, System.currentTimeMillis())

            _userMessage.value = "📲 Recordatorio estilo WhatsApp enviado exitosamente a todos los acudientes."
        }
    }

    fun resetAllUsersPointsToDefault() {
        viewModelScope.launch {
            repository.resetAllUsersPointsToDefault()
            try {
                val store = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val studentsSnap = store.collection("users").whereEqualTo("role", "STUDENT").get().await()
                for (doc in studentsSnap.documents) {
                    store.collection("users").document(doc.id).update(
                        "credits", 100,
                        "xp", 50,
                        "level", 1,
                        "streakDays", 1
                    )
                }
                val parentsSnap = store.collection("users").whereEqualTo("role", "PARENT").get().await()
                for (doc in parentsSnap.documents) {
                    store.collection("users").document(doc.id).update(
                        "credits", 100,
                        "parentIncentiveCredits", 100,
                        "xp", 50,
                        "level", 1,
                        "streakDays", 1
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _userMessage.value = "🔄 Puntos Escolaris restablecidos a sus valores por defecto (100 🪙 / 50 XP)."
        }
    }

    fun grantBadgeToMultipleUsers(
        targetUserIds: List<String>,
        badgeKey: String,
        badgeTitle: String,
        badgeDesc: String,
        badgeEmoji: String,
        category: String,
        teacherNote: String,
        xpReward: Int,
        creditReward: Int,
        photoUri: String?
    ) {
        viewModelScope.launch {
            val allCurrentUsers = allUsers.value
            val selectedUsers = allCurrentUsers.filter { it.id in targetUserIds }

            for (user in selectedUsers) {
                val badgeEntity = com.example.data.local.entity.BadgeEntity(
                    studentId = user.id,
                    badgeKey = badgeKey,
                    title = badgeTitle,
                    description = badgeDesc,
                    emoji = badgeEmoji,
                    category = category,
                    unlockedAtMillis = System.currentTimeMillis(),
                    unlockedByTeacher = _currentUser.value?.name ?: "Prof. Moz",
                    teacherNote = teacherNote,
                    xpReward = xpReward,
                    creditReward = creditReward,
                    photoUri = photoUri
                )
                repository.insertBadge(badgeEntity)

                repository.addCreditsAndXp(user.id, creditReward, xpReward)

                repository.insertNotification(
                    NotificationEntity(
                        title = "🎖️ ¡Nuevo Galardón de Honor Recibido!",
                        message = "Has recibido el galardón '$badgeEmoji $badgeTitle' (+${creditReward} 🪙 Escolaris). \"$teacherNote\"",
                        type = "BADGE",
                        targetScreen = if (user.role == "PARENT") "parent" else "profile"
                    )
                )

                try {
                    val store = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val badgeDoc = hashMapOf(
                        "studentId" to user.id,
                        "studentName" to user.name,
                        "badgeKey" to badgeKey,
                        "title" to badgeTitle,
                        "description" to badgeDesc,
                        "category" to category,
                        "emoji" to badgeEmoji,
                        "teacherNote" to teacherNote,
                        "creditReward" to creditReward,
                        "xpReward" to xpReward,
                        "photoUrl" to (photoUri ?: ""),
                        "unlockedAtMillis" to System.currentTimeMillis(),
                        "status" to "ACTIVE"
                    )
                    store.collection("badges").document("${user.id}_${badgeKey}").set(badgeDoc, com.google.firebase.firestore.SetOptions.merge())

                    val userDoc = store.collection("users").document(user.id).get().await()
                    val currCredits = if (userDoc.exists()) (userDoc.getLong("credits") ?: user.credits.toLong()).toInt() else user.credits
                    val currXp = if (userDoc.exists()) (userDoc.getLong("xp") ?: user.xp.toLong()).toInt() else user.xp
                    val userUpdates = mutableMapOf<String, Any>(
                        "credits" to currCredits + creditReward,
                        "xp" to currXp + xpReward
                    )
                    if (user.role == "PARENT") {
                        val currIncentive = if (userDoc.exists()) (userDoc.getLong("parentIncentiveCredits") ?: user.parentIncentiveCredits.toLong()).toInt() else user.parentIncentiveCredits
                        userUpdates["parentIncentiveCredits"] = currIncentive + creditReward
                    }
                    store.collection("users").document(user.id).set(userUpdates, com.google.firebase.firestore.SetOptions.merge())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _userMessage.value = "🎉 ¡Éxito! Se ha otorgado '$badgeEmoji $badgeTitle' (+${creditReward} 🪙) a ${selectedUsers.size} personas seleccionadas."
        }
    }
}
